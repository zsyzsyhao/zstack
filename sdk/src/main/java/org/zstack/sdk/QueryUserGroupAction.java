package org.zstack.sdk;

import java.util.HashMap;
import java.util.Map;

public class QueryUserGroupAction extends QueryAction {

    private static final HashMap<String, Parameter> parameterMap = new HashMap<>();

    public static class Result {
        public ErrorCode error;
        public QueryUserGroupMsgResult value;
    }



    public Result call() {
        ApiResult res = ZSClient.call(this);
        Result ret = new Result();
        if (res.error != null) {
            ret.error = res.error;
            return ret;
        }
        
        QueryUserGroupMsgResult value = res.getResult(QueryUserGroupMsgResult.class);
        ret.value = value == null ? new QueryUserGroupMsgResult() : value;
        return ret;
    }

    public void call(final Completion<Result> completion) {
        ZSClient.call(this, new InternalCompletion() {
            @Override
            public void complete(ApiResult res) {
                Result ret = new Result();
                if (res.error != null) {
                    ret.error = res.error;
                    completion.complete(ret);
                    return;
                }
                
                QueryUserGroupMsgResult value = res.getResult(QueryUserGroupMsgResult.class);
                ret.value = value == null ? new QueryUserGroupMsgResult() : value;
                completion.complete(ret);
            }
        });
    }

    Map<String, Parameter> getParameterMap() {
        return parameterMap;
    }

    RestInfo getRestInfo() {
        RestInfo info = new RestInfo();
        info.httpMethod = "GET";
        info.path = "/accounts/groups";
        info.needSession = true;
        info.needPoll = false;
        info.parameterName = "";
        return info;
    }

}
