package org.zstack.rest;

import org.zstack.header.errorcode.ErrorCode;

import java.util.HashMap;

/**
 * Created by xing5 on 2016/12/8.
 */
public class ApiResponse extends HashMap {
    private String location;
    private ErrorCode error;

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
}
