package scripts

import org.apache.commons.lang.StringUtils
import org.springframework.util.AntPathMatcher
import org.zstack.header.identity.SuppressCredentialCheck
import org.zstack.header.message.APIParam
import org.zstack.header.rest.APINoSee
import org.zstack.header.rest.RestRequest
import org.zstack.rest.JavaSdkTemplate
import org.zstack.utils.FieldUtils

import java.lang.reflect.Field

/**
 * Created by xing5 on 2016/12/9.
 */
class SdkApiTemplate implements JavaSdkTemplate {
    Class apiMessageClass
    RestRequest requestAnnotation

    SdkApiTemplate(Class apiMessageClass) {
        this.apiMessageClass = apiMessageClass
        this.requestAnnotation = apiMessageClass.getAnnotation(RestRequest.class)
    }

    def generateClassName() {
        def name = StringUtils.stripStart(apiMessageClass.getSimpleName(), "API")
        name = StringUtils.stripEnd(name, "Msg")
        name = StringUtils.capitalize(name)
        return String.format("%sAction", name)
    }

    def generateFields() {
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
                    annotationFields.add(String.format("validValues = {%s}", apiParam.validValues().join(",")))
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

        return output.join("\n")
    }

    def generateMethods() {
        def ms = []
        ms.add("""\
    public ApiResult call() {
        return ZSClient.call(this);
    }
""")
        ms.add("""\
    RestInfo getRestInfo() {
        RestInfo info = new RestInfo()
        info.httpMethod = ${requestAnnotation.method().name()}
        info.path = ${requestAnnotation.path()}
        return info
    }
""")

        return ms.join("\n")
    }

    def generateUrlParameters() {
        AntPathMatcher matcher = new AntPathMatcher()
        matcher.extractUriTemplateVariables()
    }

    @Override
    String generate() {
        return """package org.zstack.sdk;

public class ${generateClassName()} extends AbstractAction {
${generateFields()}

${generateMethods()}
}
""".toString()
    }
}
