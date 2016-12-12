package org.zstack.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;
import org.zstack.utils.gson.JSONObjectUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xing5 on 2016/12/11.
 */
public class ApiLogger implements HandlerInterceptor {
    private static final Logger requestLogger = LogManager.getLogger("api.request");

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (requestLogger.isTraceEnabled()) {
            ContentCachingRequestWrapper w = WebUtils.getNativeRequest(new ContentCachingRequestWrapper(req), ContentCachingRequestWrapper.class);

            String body = null;

            if (w != null) {
                byte[] buf = w.getContentAsByteArray();
                if (buf.length > 0) {
                    body = new String(buf, 0, buf.length, w.getCharacterEncoding());
                }
            }

            Map<String, String> headers = new HashMap<>();
            for (Enumeration e = req.getHeaderNames(); e.hasMoreElements() ;) {
                String name = e.nextElement().toString();
                headers.put(name, req.getHeader(name));
            }

            StringBuilder sb = new StringBuilder(String.format("[ID: %s] Request from %s (to %s), ", req.getSession().getId(),
                    req.getRemoteHost(), URLDecoder.decode(req.getRequestURI(), "UTF-8")));
            sb.append(String.format(" Headers: %s,", JSONObjectUtil.toJsonString(headers)));
            sb.append(String.format(" Body: %s", body));

            requestLogger.trace(sb.toString());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (requestLogger.isTraceEnabled()) {
            ContentCachingResponseWrapper r = new ContentCachingResponseWrapper(httpServletResponse);
            ContentCachingResponseWrapper w = WebUtils.getNativeResponse(r, ContentCachingResponseWrapper.class);
            StringBuilder sb = new StringBuilder(String.format("[ID: %s] Response to %s (%s),", httpServletRequest.getSession().getId(),
                    httpServletRequest.getRemoteHost(), URLDecoder.decode(httpServletRequest.getRequestURI(), "UTF-8")));
            sb.append(String.format(" Status Code: %s,", r.getStatusCode()));

            String body = null;
            if (w != null) {
                byte[] buf = w.getContentAsByteArray();
                if (buf.length > 0) {
                    body = new String(buf, 0, buf.length, w.getCharacterEncoding());
                    w.copyBodyToResponse();
                }
            }

            sb.append(String.format(" Body: %s", body));

            requestLogger.trace(sb.toString());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
