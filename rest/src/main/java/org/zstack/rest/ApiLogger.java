package org.zstack.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;
import org.zstack.header.exception.CloudRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * Created by xing5 on 2016/12/11.
 */
public class ApiLogger implements HandlerInterceptor {
    private static final Logger requestLogger = LogManager.getLogger("api.request");

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
            requestLogger.warn(e.getMessage(), e);
            throw new CloudRuntimeException(e);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        //if (requestLogger.isTraceEnabled()) {
            HttpServletRequest req = new ContentCachingRequestWrapper(httpServletRequest);
            StringBuilder sb = new StringBuilder(String.format("request from %s (to %s), ", req.getRemoteHost(), req.getRequestURI()));
            HttpEntity<String> entity = toHttpEntity(req);
            sb.append(String.format(" Headers: %s,", entity.getHeaders()));
            sb.append(String.format(" Body: %s", entity.getBody()));

            requestLogger.trace(sb.toString());
        //}

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
}
