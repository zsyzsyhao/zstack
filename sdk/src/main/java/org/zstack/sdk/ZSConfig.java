package org.zstack.sdk;

/**
 * Created by xing5 on 2016/12/9.
 */
public class ZSConfig {
    private String hostname = "localhost";
    private int port = 8080;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
