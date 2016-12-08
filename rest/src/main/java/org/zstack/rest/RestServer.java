package org.zstack.rest;

import org.apache.commons.beanutils.PropertyUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UriComponentsBuilder;
import org.zstack.core.Platform;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.cloudbus.CloudBusCallBack;
import org.zstack.core.cloudbus.CloudBusEventListener;
import org.zstack.header.Component;
import org.zstack.header.apimediator.ApiMediatorConstant;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.identity.SessionInventory;
import org.zstack.header.identity.SuppressCredentialCheck;
import org.zstack.header.message.*;
import org.zstack.header.rest.RESTFacade;
import org.zstack.header.rest.Rest;
import org.zstack.utils.Utils;
import org.zstack.utils.gson.JSONObjectUtil;
import org.zstack.utils.logging.CLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by xing5 on 2016/12/7.
 */
public class RestServer implements Component, CloudBusEventListener {
    private static final CLogger logger = Utils.getLogger(RestServer.class);

    @Autowired
    private CloudBus bus;
    @Autowired
    private AsyncRestApiStore asyncStore;
    @Autowired
    private RESTFacade restf;

    private static final String ASYNC_JOB_PATH_PATTERN = String.format("%s/%s/{uuid}", RestConstants.API_VERSION, RestConstants.ASYNC_JOB_PATH);

    @Override
    public boolean handleEvent(Event e) {
        if (e instanceof APIEvent) {
            asyncStore.complete((APIEvent) e);
        }

        return false;
    }

    class Api {
        Class apiClass;
        Rest annotation;
        Map<String, String> requestMappingFields;
        Map<String, String> responseMappingFields = new HashMap<>();
        String path;

        Api(Class clz, Rest at) {
            apiClass = clz;
            annotation = at;
            path = String.format("%s%s", RestConstants.API_VERSION, at.path());

            if (at.requestMappingFields().length > 0) {
                requestMappingFields = new HashMap<>();

                for (String mf : at.requestMappingFields()) {
                    String[] kv = mf.split("=");
                    if (kv.length != 2) {
                        throw new CloudRuntimeException(String.format("bad requestMappingField[%s] of %s", mf, apiClass));
                    }

                    requestMappingFields.put(kv[0].trim(), kv[1].trim());
                }
            }

            if (at.responseMappingFields().length > 0) {
                responseMappingFields = new HashMap<>();

                for (String mf : at.responseMappingFields()) {
                    String[] kv = mf.split("=");
                    if (kv.length != 2) {
                        throw new CloudRuntimeException(String.format("bad responseMappingField[%s] of %s", mf, apiClass));
                    }

                    responseMappingFields.put(kv[0].trim(), kv[1].trim());
                }
            }
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

    void init() throws IllegalAccessException, InstantiationException {
        bus.subscribeEvent(this, new APIEvent());
    }

    private AntPathMatcher matcher = new AntPathMatcher();

    private Map<String, Object> apis = new HashMap<>();

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

    void handle(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        if (matcher.match(ASYNC_JOB_PATH_PATTERN, req.getRequestURI())) {
            handleJobQuery(req, rsp);
            return;
        }

        HttpEntity<String> entity = toHttpEntity(req);
        String path = req.getRequestURI();

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
            rsp.sendError(HttpStatus.NOT_FOUND.value(), String.format("no api mapping to %s", path));
            return;
        }

        try {
            if (api instanceof Api) {
                handleUniqueApi((Api) api, entity, req, rsp);
            } else {
                handleNonUniqueApi((Collection)api, entity, req, rsp);
            }
        } catch (RestException e) {
            rsp.sendError(e.statusCode, e.error);
        } catch (Exception e) {
            logger.warn(String.format("failed to handle API to %s", path), e);
            rsp.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    private void handleJobQuery(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        if (!req.getMethod().equals(HttpMethod.GET.name())) {
            rsp.sendError(HttpStatus.METHOD_NOT_ALLOWED.value(), "only GET method is allowed for querying job status");
            return;
        }

        Map<String, String> vars = matcher.extractUriTemplateVariables(ASYNC_JOB_PATH_PATTERN, req.getRequestURI());
        String uuid = vars.get("uuid");
        AsyncRestQueryResult ret = asyncStore.query(uuid);

        if (ret.getState() == AsyncRestState.expired) {
            rsp.sendError(HttpStatus.NOT_FOUND.value(), "the job has been expired");
            return;
        }

        rsp.setStatus(HttpStatus.OK.value());
        rsp.getWriter().write(JSONObjectUtil.toJsonString(ret));
    }

    private void handleNonUniqueApi(Collection apis, HttpEntity<String> entity, HttpServletRequest req, HttpServletResponse rsp) throws RestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Map m = JSONObjectUtil.toObject(entity.getBody(), LinkedHashMap.class);

        Optional<Api> o = apis.stream().filter(a -> m.containsKey(((Api)a).annotation.actionName())).findAny();
        if (!o.isPresent()) {
            throw new RestException(HttpStatus.BAD_REQUEST.value(), String.format("the body doesn't contain any action mapping" +
                    " to the URL[%s]", req.getRequestURI()));
        }

        Api api = o.get();
        handleApi(api, m, api.annotation.actionName(), entity, req, rsp);
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

        Map<String, String> vars = matcher.extractUriTemplateVariables(api.path, req.getRequestURI());
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
        handleByMessage(msg, api, rsp);
    }

    private void handleUniqueApi(Api api, HttpEntity<String> entity, HttpServletRequest req, HttpServletResponse rsp) throws RestException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
        handleApi(api, JSONObjectUtil.toObject(entity.getBody(), LinkedHashMap.class), api.annotation.parameterName(), entity, req, rsp);
    }

    private void writeReplyResponse(MessageReply reply, Api api, HttpServletResponse rsp) {
        try {
            ApiResponse response = new ApiResponse();

            if (!reply.isSuccess()) {
                response.error = reply.getError();
                rsp.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), JSONObjectUtil.toJsonString(response));
                return;
            }

            // the api succeeded

            if (!api.annotation.responseMappingAllTo().equals("")) {
                PropertyUtils.setProperty(response, api.annotation.responseMappingAllTo(),
                        PropertyUtils.getProperty(reply, api.annotation.responseMappingAllTo()));
            } else {
                for (Map.Entry<String, String> e : api.responseMappingFields.entrySet()) {
                    PropertyUtils.setProperty(response, e.getKey(),
                            PropertyUtils.getProperty(reply, e.getValue()));
                }
            }

            rsp.setStatus(HttpStatus.OK.value());
            rsp.getWriter().write(JSONObjectUtil.toJsonString(response));
        } catch (IOException e) {
            logger.warn("unhandled IO error happened", e);
        } catch (Throwable t) {
            logger.warn("unhandled error", t);

            try {
                rsp.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), t.getMessage());
            } catch (IOException e) {
                logger.warn("unhandled IO error happened", e);
            }
        }
    }

    private void handleByMessage(APIMessage msg, Api api, HttpServletResponse rsp) throws IOException {
        if (msg instanceof APISyncCallMessage) {
            bus.send(msg, new CloudBusCallBack() {
                @Override
                public void run(MessageReply reply) {
                    writeReplyResponse(reply, api, rsp);
                }
            });
        } else {
            String apiUuid = asyncStore.save(msg);
            UriComponentsBuilder ub = UriComponentsBuilder.fromHttpUrl(restf.getBaseUrl());
            ub.path(RestConstants.API_VERSION);
            ub.path(RestConstants.ASYNC_JOB_PATH);
            ub.path(apiUuid);

            ApiResponse response = new ApiResponse();
            response.location = ub.build().toUriString();

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
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rest.class);

        for (Class clz : classes) {
            Rest at = (Rest) clz.getAnnotation(Rest.class);
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
        }
    }

    @Override
    public boolean stop() {
        return true;
    }
}
