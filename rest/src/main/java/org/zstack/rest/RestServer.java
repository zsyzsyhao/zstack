package org.zstack.rest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.WebUtils;
import org.zstack.core.Platform;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.cloudbus.CloudBusCallBack;
import org.zstack.core.cloudbus.CloudBusEventListener;
import org.zstack.core.logging.Log;
import org.zstack.header.Component;
import org.zstack.header.apimediator.ApiMediatorConstant;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.identity.SessionInventory;
import org.zstack.header.identity.SuppressCredentialCheck;
import org.zstack.header.message.*;
import org.zstack.header.rest.RESTFacade;
import org.zstack.header.rest.RestRequest;
import org.zstack.header.rest.RestResponse;
import org.zstack.header.zone.APICreateZoneMsg;
import org.zstack.rest.sdk.JavaSdkTemplate;
import org.zstack.rest.sdk.SdkFile;
import org.zstack.utils.DebugUtils;
import org.zstack.utils.GroovyUtils;
import org.zstack.utils.Utils;
import org.zstack.utils.gson.JSONObjectUtil;
import org.zstack.utils.logging.CLogger;
import org.zstack.utils.path.PathUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by xing5 on 2016/12/7.
 */
public class RestServer implements Component, CloudBusEventListener, HandlerInterceptor {
    private static final CLogger logger = Utils.getLogger(RestServer.class);
    private static final Logger requestLogger = LogManager.getLogger("api.request");
    private static final Logger responseLogger = LogManager.getLogger("api.response");

    @Autowired
    private CloudBus bus;
    @Autowired
    private AsyncRestApiStore asyncStore;
    @Autowired
    private RESTFacade restf;

    private static final String ASYNC_JOB_PATH_PATTERN = String.format("%s/%s/{uuid}", RestConstants.API_VERSION, RestConstants.ASYNC_JOB_PATH);

    public static void generateJavaSdk() {
        String path = PathUtil.join(System.getProperty("user.home"), "zstack-sdk/java");
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try {
            Class clz = GroovyUtils.getClass("scripts/SdkApiTemplate.groovy", RestServer.class.getClassLoader());
            JavaSdkTemplate tmp = (JavaSdkTemplate) clz.getConstructor(Class.class).newInstance(APICreateZoneMsg.class);
            List<SdkFile> files = tmp.generate();
            for (SdkFile f : files) {
                String fpath = PathUtil.join(path, f.getFileName());
                FileUtils.writeStringToFile(new File(fpath), f.getContent());
            }

            tmp = GroovyUtils.loadClass("scripts/SdkDataStructureGenerator.groovy", RestServer.class.getClassLoader());
            for (SdkFile f : tmp.generate()) {
                //logger.debug(String.format("\n%s", f.getContent()));
                String fpath = PathUtil.join(path, f.getFileName());
                FileUtils.writeStringToFile(new File(fpath), f.getContent());
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new CloudRuntimeException(e);
        }
    }

    @Override
    public boolean handleEvent(Event e) {
        if (e instanceof APIEvent) {
            asyncStore.complete((APIEvent) e);
        }

        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (requestLogger.isTraceEnabled()) {
            HttpServletRequest req = new ContentCachingRequestWrapper(httpServletRequest);
            StringBuilder sb = new StringBuilder(String.format("request from %s (to %s), ", req.getRemoteHost(), req.getRequestURI()));
            HttpEntity<String> entity = toHttpEntity(req);
            sb.append(String.format(" Headers: %s,", entity.getHeaders()));
            sb.append(String.format(" Body: %s", entity.getBody()));

            requestLogger.trace(sb.toString());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if (requestLogger.isTraceEnabled()) {
            ContentCachingResponseWrapper rsp = WebUtils.getNativeResponse(httpServletResponse, ContentCachingResponseWrapper.class);
            StringBuilder sb = new StringBuilder(String.format("response to %s (%s),", httpServletRequest.getRemoteHost(), httpServletRequest.getRequestURI()));
            sb.append(String.format(" Status Code: %s,", rsp.getStatusCode()));

            byte[] buf = rsp.getContentAsByteArray();
            if (buf.length > 0) {
                String body = new String(buf, 0, buf.length, rsp.getCharacterEncoding());
                rsp.copyBodyToResponse();
                sb.append(String.format(" Body: %s", body));
            }

            requestLogger.trace(sb.toString());
        }
    }

    class Api {
        Class apiClass;
        Class apiResponseClass;
        RestRequest requestAnnotation;
        RestResponse responseAnnotation;
        Map<String, String> requestMappingFields;
        String path;

        Api(Class clz, RestRequest at) {
            apiClass = clz;
            requestAnnotation = at;
            apiResponseClass = at.responseClass();
            path = String.format("%s%s", RestConstants.API_VERSION, at.path());

            if (at.mappingFields().length > 0) {
                requestMappingFields = new HashMap<>();

                for (String mf : at.mappingFields()) {
                    String[] kv = mf.split("=");
                    if (kv.length != 2) {
                        throw new CloudRuntimeException(String.format("bad requestMappingField[%s] of %s", mf, apiClass));
                    }

                    requestMappingFields.put(kv[0].trim(), kv[1].trim());
                }
            }
            responseAnnotation = (RestResponse) apiResponseClass.getAnnotation(RestResponse.class);
            DebugUtils.Assert(responseAnnotation != null, String.format("%s must be annotated with @RestResponse", apiResponseClass));


        }

        String getMappingField(String key) {
            if (requestMappingFields == null) {
                return null;
            }

            return requestMappingFields.get(key);
        }
    }

    class RestException extends Exception {
        private int statusCode;
        private String error;

        public RestException(int statusCode, String error) {
            this.statusCode = statusCode;
            this.error = error;
        }
    }

    class RestResponseWrapper {
        RestResponse annotation;
        Map<String, String> responseMappingFields = new HashMap<>();
        Class apiResponseClass;

        public RestResponseWrapper(RestResponse annotation, Class apiResponseClass) {
            this.annotation = annotation;
            this.apiResponseClass = apiResponseClass;

            if (annotation.mappingFields().length > 0) {
                responseMappingFields = new HashMap<>();

                for (String mf : annotation.mappingFields()) {
                    String[] kv = mf.split("=");
                    if (kv.length != 2) {
                        throw new CloudRuntimeException(String.format("bad mappingFields[%s] of %s", mf, apiResponseClass));
                    }

                    responseMappingFields.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
    }

    void init() throws IllegalAccessException, InstantiationException {
        bus.subscribeEvent(this, new APIEvent());
    }

    private AntPathMatcher matcher = new AntPathMatcher();

    private Map<String, Object> apis = new HashMap<>();
    private Map<Class, RestResponseWrapper> responseAnnotationByClass = new HashMap<>();

    private HttpEntity<String> toHttpEntity(HttpServletRequest req) {
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                sb.append(line);
            }
            req.getReader().close();

            HttpHeaders header = new HttpHeaders();
            for (Enumeration e = req.getHeaderNames(); e.hasMoreElements() ;) {
                String name = e.nextElement().toString();
                header.add(name, req.getHeader(name));
            }

            return new HttpEntity<>(sb.toString(), header);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new CloudRuntimeException(e);
        }
    }

    private void sendError(int statusCode, String body, HttpServletResponse rsp) throws IOException {
        rsp.setStatus(statusCode);
        rsp.getWriter().write(body);
    }

    private String getDecodedUrl(HttpServletRequest req) {
        try {
            return URLDecoder.decode(req.getRequestURI(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CloudRuntimeException(e);
        }
    }

    void handle(HttpServletRequest req, HttpServletResponse rsp) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String path = getDecodedUrl(req);

        if (matcher.match(ASYNC_JOB_PATH_PATTERN, path)) {
            handleJobQuery(req, rsp);
            return;
        }

        HttpEntity<String> entity = toHttpEntity(req);

        Object api = apis.get(path);
        if (api == null) {
            for (String p : apis.keySet()) {
                if (matcher.match(p, path)) {
                    api = apis.get(p);
                    break;
                }
            }
        }

        if (api == null) {
            sendError(HttpStatus.NOT_FOUND.value(), String.format("no api mapping to %s", path), rsp);
            return;
        }

        try {
            if (api instanceof Api) {
                handleUniqueApi((Api) api, entity, req, rsp);
            } else {
                handleNonUniqueApi((Collection)api, entity, req, rsp);
            }
        } catch (RestException e) {
            sendError(e.statusCode, e.error, rsp);
        } catch (Exception e) {
            logger.warn(String.format("failed to handle API to %s", path), e);
            sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), rsp);
        }
    }

    private void handleJobQuery(HttpServletRequest req, HttpServletResponse rsp) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (!req.getMethod().equals(HttpMethod.GET.name())) {
            sendError(HttpStatus.METHOD_NOT_ALLOWED.value(), "only GET method is allowed for querying job status", rsp);
            return;
        }

        Map<String, String> vars = matcher.extractUriTemplateVariables(ASYNC_JOB_PATH_PATTERN, getDecodedUrl(req));
        String uuid = vars.get("uuid");
        AsyncRestQueryResult ret = asyncStore.query(uuid);

        if (ret.getState() == AsyncRestState.expired) {
            sendError(HttpStatus.NOT_FOUND.value(), "the job has been expired", rsp);
            return;
        }

        ApiResponse response = new ApiResponse();

        if (ret.getState() == AsyncRestState.processing) {
            sendResponse(HttpStatus.ACCEPTED.value(), response, rsp);
            return;
        }

        // task is done
        APIEvent evt = ret.getResult();
        if (evt.isSuccess()) {
            writeResponse(response, responseAnnotationByClass.get(evt.getClass()), ret.getResult());
            sendResponse(HttpStatus.OK.value(), response, rsp);
        } else {
            response.setError(evt.getErrorCode());
            sendResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), response, rsp);
        }
    }

    private void sendResponse(int statusCode, ApiResponse response, HttpServletResponse rsp) throws IOException {
        rsp.setStatus(statusCode);
        rsp.getWriter().write(response.isEmpty() ? "" : JSONObjectUtil.toJsonString(response));
    }

    private void handleNonUniqueApi(Collection apis, HttpEntity<String> entity, HttpServletRequest req, HttpServletResponse rsp) throws RestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Map m = JSONObjectUtil.toObject(entity.getBody(), LinkedHashMap.class);

        Optional<Api> o = apis.stream().filter(a -> m.containsKey(((Api)a).requestAnnotation.actionName())).findAny();
        if (!o.isPresent()) {
            throw new RestException(HttpStatus.BAD_REQUEST.value(), String.format("the body doesn't contain any action mapping" +
                    " to the URL[%s]", getDecodedUrl(req)));
        }

        Api api = o.get();
        handleApi(api, m, api.requestAnnotation.actionName(), entity, req, rsp);
    }

    private void handleApi(Api api, Map body, String parameterName, HttpEntity<String> entity, HttpServletRequest req, HttpServletResponse rsp) throws RestException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
        String sessionId = null;
        if (!api.apiClass.isAnnotationPresent(SuppressCredentialCheck.class)) {
            String auth = entity.getHeaders().getFirst("Authorization");
            if (auth == null) {
                throw new RestException(HttpStatus.BAD_REQUEST.value(), "missing header 'Authorization'");
            }

            auth = auth.trim();
            if (!auth.startsWith("OAuth")) {
                throw new RestException(HttpStatus.BAD_REQUEST.value(), "Authorization type must be 'OAuth'");
            }

            sessionId = auth.replaceFirst("OAuth", "").trim();
        }

        Map<String, String> vars = matcher.extractUriTemplateVariables(api.path, getDecodedUrl(req));
        Object parameter = body.get(parameterName);

        APIMessage msg;
        if (parameter == null) {
            msg = (APIMessage) api.apiClass.newInstance();
        } else {
            msg = JSONObjectUtil.rehashObject(parameter, (Class<APIMessage>) api.apiClass);
        }

        if (sessionId != null) {
            SessionInventory session = new SessionInventory();
            session.setUuid(sessionId);
            PropertyUtils.setProperty(msg, "session", session);
        }

        Object systemTags = body.get("systemTags");
        if (systemTags != null) {
            PropertyUtils.setProperty(msg, "systemTags", systemTags);
        }

        Object userTags = body.get("userTags");
        if (userTags != null) {
            PropertyUtils.setProperty(msg, "userTags", systemTags);
        }

        for (Map.Entry<String, String> e : vars.entrySet()) {
            // set fields parsed from the URL
            String key = e.getKey();
            String mappingKey = api.getMappingField(key);
            PropertyUtils.setProperty(msg, mappingKey == null ? key : mappingKey, e.getValue());
        }

        msg.setServiceId(ApiMediatorConstant.SERVICE_ID);
        sendMessage(msg, api, rsp);
    }

    private void handleUniqueApi(Api api, HttpEntity<String> entity, HttpServletRequest req, HttpServletResponse rsp) throws RestException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
        handleApi(api, JSONObjectUtil.toObject(entity.getBody(), LinkedHashMap.class), api.requestAnnotation.parameterName(), entity, req, rsp);
    }

    private void writeResponse(ApiResponse response, RestResponseWrapper w, Object replyOrEvent) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (!w.annotation.mappingAllTo().equals("")) {
            response.put(w.annotation.mappingAllTo(),
                    PropertyUtils.getProperty(replyOrEvent, w.annotation.mappingAllTo()));
        } else {
            for (Map.Entry<String, String> e : w.responseMappingFields.entrySet()) {
                response.put(e.getKey(),
                        PropertyUtils.getProperty(replyOrEvent, e.getValue()));
            }
        }
    }

    private void sendReplyResponse(MessageReply reply, Api api, HttpServletResponse rsp) {
        try {
            ApiResponse response = new ApiResponse();

            if (!reply.isSuccess()) {
                response.setError(reply.getError());
                sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), JSONObjectUtil.toJsonString(response), rsp);
                return;
            }

            // the api succeeded

            writeResponse(response, responseAnnotationByClass.get(api.apiResponseClass), reply);
            sendResponse(HttpStatus.OK.value(), response, rsp);
        } catch (IOException e) {
            logger.warn("unhandled IO error happened", e);
        } catch (Throwable t) {
            logger.warn("unhandled error", t);

            try {
                sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), t.getMessage(), rsp);
            } catch (IOException e) {
                logger.warn("unhandled IO error happened", e);
            }
        }
    }

    private void sendMessage(APIMessage msg, Api api, HttpServletResponse rsp) throws IOException {
        if (msg instanceof APISyncCallMessage) {
            bus.send(msg, new CloudBusCallBack() {
                @Override
                public void run(MessageReply reply) {
                    sendReplyResponse(reply, api, rsp);
                }
            });
        } else {
            String apiUuid = asyncStore.save(msg);
            UriComponentsBuilder ub = UriComponentsBuilder.fromHttpUrl(restf.getBaseUrl());
            ub.path(RestConstants.API_VERSION);
            ub.path(RestConstants.ASYNC_JOB_PATH);
            ub.path("/" + apiUuid);

            ApiResponse response = new ApiResponse();
            response.setLocation(ub.build().toUriString());

            bus.send(msg);

            rsp.setStatus(HttpStatus.ACCEPTED.value());
            rsp.getWriter().write(JSONObjectUtil.toJsonString(response));
        }
    }

    @Override
    public boolean start() {
        build();
        return true;
    }

    private void build() {
        Reflections reflections = Platform.getReflections();
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RestRequest.class);

        for (Class clz : classes) {
            RestRequest at = (RestRequest) clz.getAnnotation(RestRequest.class);
            Api api = new Api(clz, at);

            if (!apis.containsKey(api.path)) {
                apis.put(api.path, api);
            } else {
                Object c = apis.get(api.path);
                List lst;
                if (c instanceof Api) {
                    lst = new ArrayList();
                    lst.add(c);
                    apis.put(api.path, lst);
                } else {
                    lst = (List) c;
                }
                lst.add(api);
            }

            responseAnnotationByClass.put(api.apiResponseClass, new RestResponseWrapper(api.responseAnnotation, api.apiResponseClass));
        }
    }

    @Override
    public boolean stop() {
        return true;
    }
}
