package org.zstack.rest;

import org.zstack.header.errorcode.ErrorCode;

import java.util.HashMap;

/**
 * Created by xing5 on 2016/12/8.
 */
public class ApiResponse extends HashMap {
    private String location;
    private ErrorCode error;
    private AsyncRestState state;
    private Object result;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        put("location", location);
    }

    public ErrorCode getError() {
        return error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
        put("error", error);
    }

    public AsyncRestState getState() {
        return state;
    }

    public void setState(AsyncRestState state) {
        this.state = state;
        put("state", state);
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
        put("result", result);
    }
}
