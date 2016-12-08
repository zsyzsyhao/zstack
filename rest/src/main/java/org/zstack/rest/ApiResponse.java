package org.zstack.rest;

import org.zstack.header.errorcode.ErrorCode;

import java.util.HashMap;

/**
 * Created by xing5 on 2016/12/8.
 */
public class ApiResponse extends HashMap {
    public String location;
    public ErrorCode error;
}
