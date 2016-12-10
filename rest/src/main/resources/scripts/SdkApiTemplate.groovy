package scripts

import org.apache.commons.lang.StringUtils
import org.springframework.util.AntPathMatcher
import org.zstack.header.identity.SuppressCredentialCheck
import org.zstack.header.message.APIParam
import org.zstack.header.message.APISyncCallMessage
import org.zstack.header.rest.APINoSee
import org.zstack.header.rest.RestRequest
import org.zstack.header.rest.RestResponse
import org.zstack.rest.sdk.JavaSdkTemplate
import org.zstack.rest.sdk.SdkFile
import org.zstack.utils.FieldUtils

import java.lang.reflect.Field

/**
 * Created by xing5 on 2016/12/9.
 */
class SdkApiTemplate implements JavaSdkTemplate {
    static Map<String, SdkFile> generatedFiles = [:]

    Class apiMessageClass
    RestRequest requestAnnotation
    RestResponse responseAnnotation
    Class responseClass

    SdkApiTemplate(Class apiMessageClass) {
        this.apiMessageClass = apiMessageClass
        this.requestAnnotation = apiMessageClass.getAnnotation(RestRequest.class)
        responseClass = requestAnnotation.responseClass()
        responseAnnotation = responseClass.getAnnotation(RestResponse.class)
    }

    def normalizeApiName() {
        def name = StringUtils.stripStart(apiMessageClass.getSimpleName(), "API")
        name = StringUtils.stripEnd(name, "Msg")
        return StringUtils.capitalize(name)
    }

    def generateClassName() {
        return String.format("%sAction", normalizeApiName())
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
    public ApiResult call() {
        return ZSClient.call(this);
    }
""")
        ms.add("""\
    RestInfo getRestInfo() {
        RestInfo info = new RestInfo()
        info.httpMethod = ${requestAnnotation.method().name()}
        info.path = ${requestAnnotation.path()}
        info.needSession = ${apiMessageClass.isAnnotationPresent(SuppressCredentialCheck.class)}
        info.needPoll = ${!APISyncCallMessage.class.isAssignableFrom(apiMessageClass)}
        return info
    }
""")

        return ms.join("\n")
    }

    def generateAction() {
        def f = new SdkFile()
        f.fileName = "${generateClassName()}.java"
        f.content = """package org.zstack.sdk;

public class ${generateClassName()} extends AbstractAction {
${generateFields()}

${generateMethods()}
}
""".toString()

        return f
    }

    def generateResultClasses() {
        def m = [:]

        if (!responseAnnotation.mappingAllTo().isEmpty()) {
            m[responseAnnotation.mappingAllTo()] = responseAnnotation.mappingAllTo()
            return generateClass("${normalizeApiName()}Result".toString(), m, responseClass)
        } else {
            for (String mf : responseAnnotation.mappingFields()) {
                def mfs = mf.split("=")
                m[mfs[0].trim()] = mfs[1].trim()
            }

            return generateClass("${normalizeApiName()}Result".toString(), m, responseClass)
        }
    }

    def generateClass(String className, Map<String, String> fieldMapping, Class sourceClass) {
        if (generatedFiles.containsKey(className)) {
            return [generatedFiles[className]]
        }

        def newFields = []
        fieldMapping.each { k, v ->
            Field sourceField = sourceClass.getField(v)
            if (sourceField.type.name.startsWith("java.")) {
                if (Collection.class.isAssignableFrom(sourceField.type)) {

                } else if (Map.class.isAssignableFrom(sourceField.type)) {

                } else {
                    // normal java type
                    newFields.add("""\
    public ${sourceField.type.name} ${k};
""")
                }
            }
        }
    }

    @Override
    List<SdkFile> generate() {
        def files = []
        files.add(generateAction())

        return files
    }
}
