package org.zstack.sdk;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xing5 on 2016/12/9.
 */
public class ZSClient {
    private static final OkHttpClient http = new OkHttpClient();

    private static ZSConfig config;

    public static ZSConfig getConfig() {
        return config;
    }

    public static void setConfig(ZSConfig c) {
        config = c;
    }

    static OkHttpClient getHttp() {
        return http;
    }

    static class Api {
        AbstractAction action;
        RestInfo info;

        Api(AbstractAction action) {
            this.action = action;
            info = action.getRestInfo();
        }

        private String substituteUrl(String url, Map<String, Object> tokens) {
            Pattern pattern = Pattern.compile("\\{(.+?)\\}");
            Matcher matcher = pattern.matcher(url);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                String varName = matcher.group(1);
                Object replacement = tokens.get(varName);
                if (replacement == null) {
                    throw new ApiException(String.format("cannot find value for URL variable[%s]", varName));
                }

                matcher.appendReplacement(buffer, "");
                buffer.append(replacement.toString());
            }

            matcher.appendTail(buffer);
            return buffer.toString();
        }

        private List<String> getVarNamesFromUrl(String url) {
            Pattern pattern = Pattern.compile("\\{(.+?)\\}");
            Matcher matcher = pattern.matcher(url);

            List<String> urlVars = new ArrayList<String>();
            while (matcher.find()) {
                urlVars.add(matcher.group(1));
            }

            return urlVars;
        }

        ApiResult call() {
            action.checkParameters();

            HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host(config.getHostname())
                    .port(config.getPort())
                    .addPathSegment("/v1")
                    .addPathSegment(info.path)
                    .build();

            String urlstr = null;
            List<String> varNames = getVarNamesFromUrl(url.toString());
            if (!varNames.isEmpty()) {
                Map<String, Object> vars = new HashMap<>();
                for (String vname : varNames) {
                    Object value = action.getParameterValue(vname);

                    if (value == null) {
                        throw new ApiException(String.format("missing required field[%s]", vname));
                    }

                    vars.put(vname, value);
                }

                urlstr = substituteUrl(url.toString(), vars);
            } else {
                urlstr = url.toString();
            }

            Map<String, Object> body = new HashMap<>();
            for (String pname : action.getAllParameterNames()) {
                if (varNames.contains(pname) || Constants.SESSION_ID.equals(pname)) {
                    // the field is set in URL variables
                    continue;
                }

                Object value = action.getParameterValue(pname);
                if (value != null) {
                    body.put(pname, value);
                }
            }

            return null;
        }
    }

    private static void errorIfNotConfigured() {
        if (config == null) {
            throw new RuntimeException("setConfig() must be called before any methods");
        }
    }

    static ApiResult call(AbstractAction action) {
        errorIfNotConfigured();
        return new Api(action).call();
    }
}
