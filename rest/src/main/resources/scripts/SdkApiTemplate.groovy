package scripts

import org.apache.commons.lang.StringUtils
import org.springframework.util.AntPathMatcher
import org.zstack.header.identity.SuppressCredentialCheck
import org.zstack.header.message.APIParam
import org.zstack.header.message.APISyncCallMessage
import org.zstack.header.query.APIQueryMessage
import org.zstack.header.rest.APINoSee
import org.zstack.header.rest.RestRequest
import org.zstack.header.rest.RestResponse
import org.zstack.rest.sdk.JavaSdkTemplate
import org.zstack.rest.sdk.SdkFile
import org.zstack.utils.FieldUtils
import org.zstack.utils.TypeUtils

import java.lang.reflect.Field

/**
 * Created by xing5 on 2016/12/9.
 */
class SdkApiTemplate implements JavaSdkTemplate {
    Class apiMessageClass
    RestRequest requestAnnotation

    String resultClassName
    boolean isQueryApi;

    SdkApiTemplate(Class apiMessageClass) {
        this.apiMessageClass = apiMessageClass
        this.requestAnnotation = apiMessageClass.getAnnotation(RestRequest.class)

        resultClassName = requestAnnotation.responseClass().simpleName
        resultClassName = StringUtils.removeStart(resultClassName, "API")
        resultClassName = StringUtils.removeEnd(resultClassName, "Event")
        resultClassName = StringUtils.removeEnd(resultClassName, "Reply")
        resultClassName = StringUtils.capitalize(resultClassName)
        resultClassName = "${resultClassName}Result"

        isQueryApi = APIQueryMessage.class.isAssignableFrom(apiMessageClass);
    }

    def normalizeApiName() {
        def name = StringUtils.removeStart(apiMessageClass.getSimpleName(), "API")
        name = StringUtils.removeEnd(name, "Msg")
        return StringUtils.capitalize(name)
    }

    def generateClassName() {
        return String.format("%sAction", normalizeApiName())
    }

    def generateFields() {
        if (isQueryApi) {
            return ""
        }

        def fields = FieldUtils.getAllFields(apiMessageClass)

        def output = []

        for (Field f : fields) {
            if (f.isAnnotationPresent(APINoSee.class)) {
                continue
            }

            APIParam apiParam = f.getAnnotation(APIParam.class)

            def annotationFields = []
            if (apiParam != null) {
                annotationFields.add(String.format("required = %s", apiParam.required()))
                if (apiParam.validValues().length > 0) {
                    annotationFields.add(String.format("validValues = {%s}", { ->
                        def vv = []
                        for (String v : apiParam.validValues()) {
                            vv.add("\"${v}\"")
                        }
                        return vv.join(",")
                    }()))
                }
                if (!apiParam.validRegexValues().isEmpty()) {
                    annotationFields.add(String.format("validRegexValues = %s", apiParam.validRegexValues()))
                }
                if (apiParam.maxLength() != Integer.MIN_VALUE) {
                    annotationFields.add(String.format("maxLength = %s", apiParam.maxLength()))
                }
                annotationFields.add(String.format("nonempty = %s", apiParam.nonempty()))
                annotationFields.add(String.format("nullElements = %s", apiParam.nullElements()))
                annotationFields.add(String.format("emptyString = %s", apiParam.emptyString()))
                if (apiParam.numberRange().length > 0) {
                    def nr = apiParam.numberRange() as Integer[]
                    annotationFields.add(String.format("numberRange = {%s}", nr.join(",")))
                }
                annotationFields.add(String.format("noTrim = %s", apiParam.noTrim()))
            } else {
                annotationFields.add(String.format("required = false"))
            }

            def fs = """\
    @Param(${annotationFields.join(", ")})
    public ${f.getType().getName()} ${f.getName()};
"""
            output.add(fs.toString())
        }

        if (!apiMessageClass.isAnnotationPresent(SuppressCredentialCheck.class)) {
            output.add("""\
    @Param(required = true)
    public String sessionId;
""")
        }

        if (!APISyncCallMessage.class.isAssignableFrom(apiMessageClass)) {
            output.add("""\
    public long timeout;
    
    public long pollingInterval;
""")
        }

        return output.join("\n")
    }

    def generateMethods() {
        def ms = []
        ms.add("""\
    public Result call() {
        ApiResult res = ZSClient.call(this);
        Result ret = new Result();
        if (res.error != null) {
            ret.error = res.error;
            return ret;
        }
        
        ${resultClassName} value = res.getResult(${resultClassName}.class);
        ret.value = value == null ? new ${resultClassName}() : value;
        return ret;
    }
""")

        ms.add("""\
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
                
                ${resultClassName} value = res.getResult(${resultClassName}.class);
                ret.value = value == null ? new ${resultClassName}() : value;
                completion.complete(ret);
            }
        });
    }
""")

        ms.add("""\
    Map<String, Parameter> getParameterMap() {
        return parameterMap;
    }
""")

        ms.add("""\
    RestInfo getRestInfo() {
        RestInfo info = new RestInfo();
        info.httpMethod = "${requestAnnotation.method().name()}";
        info.path = "${requestAnnotation.path()}";
        info.needSession = ${!apiMessageClass.isAnnotationPresent(SuppressCredentialCheck.class)};
        info.needPoll = ${!APISyncCallMessage.class.isAssignableFrom(apiMessageClass)};
        info.parameterName = "${requestAnnotation.parameterName()}";
        return info;
    }
""")

        return ms.join("\n")
    }

    def generateAction() {
        def f = new SdkFile()
        f.fileName = "${generateClassName()}.java"
        f.content = """package org.zstack.sdk;

import java.util.HashMap;
import java.util.Map;

public class ${generateClassName()} extends ${isQueryApi ? "QueryAction" : "AbstractAction"} {

    private static final HashMap<String, Parameter> parameterMap = new HashMap<>();

    public static class Result {
        public ErrorCode error;
        public ${resultClassName} value;
    }

${generateFields()}

${generateMethods()}
}
""".toString()

        return f
    }

    @Override
    List<SdkFile> generate() {
        return [generateAction()]
    }
}
