package org.zstack.sdk;

/**
 * Created by xing5 on 2016/12/9.
 */
public class ApiResult {
    ErrorCode error;
    private String resultString;

    public ErrorCode getError() {
        return error;
    }

    void setError(ErrorCode error) {
        this.error = error;
    }

    void setResultString(String resultString) {
        this.resultString = resultString;
    }

    <T> T getResult(Class<T> clz) {
        return resultString == null || resultString.isEmpty() ? null : ZSClient.gson.fromJson(resultString, clz);
    }
}
