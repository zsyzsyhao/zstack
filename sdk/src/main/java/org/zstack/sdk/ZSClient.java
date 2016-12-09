package org.zstack.sdk;

import okhttp3.OkHttpClient;

/**
 * Created by xing5 on 2016/12/9.
 */
public class ZSClient {
    private static final OkHttpClient http = new OkHttpClient();

    private ZSConfig config;

    public ZSConfig getConfig() {
        return config;
    }

    public void setConfig(ZSConfig config) {
        this.config = config;
    }

    static OkHttpClient getHttp() {
        return http;
    }
}
