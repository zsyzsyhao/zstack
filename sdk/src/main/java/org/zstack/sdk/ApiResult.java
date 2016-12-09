package org.zstack.sdk;

/**
 * Created by xing5 on 2016/12/9.
 */
public class ApiResult<T> {
    private ErrorCode error;
    private T result;

    public ErrorCode getError() {
        return error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
