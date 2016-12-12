package scripts

import org.apache.commons.lang.StringUtils
import org.reflections.Reflections
import org.zstack.core.Platform
import org.zstack.header.exception.CloudRuntimeException
import org.zstack.header.rest.APINoSee
import org.zstack.header.rest.RestResponse
import org.zstack.rest.sdk.JavaSdkTemplate
import org.zstack.rest.sdk.SdkFile
import org.zstack.utils.FieldUtils
import org.zstack.utils.Utils
import org.zstack.utils.logging.CLogger

import java.lang.reflect.Field

/**
 * Created by xing5 on 2016/12/11.
 */
class SdkDataStructureGenerator implements JavaSdkTemplate {
    CLogger logger = Utils.getLogger(SdkDataStructureGenerator.class)

    Set<Class> responseClasses
    Map<Class, SdkFile> sdkFileMap = [:]
    Set<Class> laterResolvedClasses = []

    Map<String, String> sourceClassMap = [:]

    Reflections reflections = Platform.reflections

    SdkDataStructureGenerator() {
        Reflections reflections = Platform.getReflections()
        responseClasses = reflections.getTypesAnnotatedWith(RestResponse.class)
    }

    @Override
    List<SdkFile> generate() {
        responseClasses.each { c -> generateResponseClass(c) }
        resolveAllClasses()
        generateSourceDestClassMap()

        def ret = sdkFileMap.values() as List
        ret.add(generateSourceDestClassMap())

        return ret
    }

    def generateSourceDestClassMap() {
        def srcToDst = []
        def dstToSrc = []

        sourceClassMap.each { k, v ->
            srcToDst.add("""\t\t\tput("${k}", "${v}");""")
            dstToSrc.add("""\t\t\tput("${v}", "${k}");""")
        }

        SdkFile f = new SdkFile()
        f.fileName = "SourceClassMap.java"
        f.content = """package org.zstack.sdk;

import java.util.HashMap;

public class SourceClassMap {
    final static HashMap<String, String> srcToDstMapping = new HashMap() {
        {
${srcToDst.join("\n")}
        }
    };

    final static HashMap<String, String> dstToSrcMapping = new HashMap() {
        {
${dstToSrc.join("\n")}
        }
    };
}
"""
        return f
    }

    def resolveAllClasses() {
        if (laterResolvedClasses.isEmpty()) {
            return
        }

        Set<Class> toResolve = []
        toResolve.addAll(laterResolvedClasses)

        toResolve.each { Class clz ->
            resolveClass(clz)
            laterResolvedClasses.remove(clz)
        }

        resolveAllClasses()
    }

    def resolveClass(Class clz) {
        if (sdkFileMap.containsKey(clz)) {
            return
        }

        if (!Object.class.isAssignableFrom(clz.superclass)) {
            addToLaterResolvedClassesIfNeed(clz.superclass)
        }

        def output = []
        for (Field f : clz.getDeclaredFields()) {
            if (f.isAnnotationPresent(APINoSee.class)) {
                continue
            }

            output.add(makeFieldText(f.name, f))
        }

        SdkFile file = new SdkFile()
        file.fileName = "${clz.simpleName}.java"
        file.content = """package org.zstack.sdk;

public class ${clz.simpleName} ${Object.class.isAssignableFrom(clz.superclass) ? "" : clz.superclass.simpleName} {

${output.join("\n")}
}
"""
        sourceClassMap[clz.name] = "org.zstack.sdk.${clz.simpleName}"
        sdkFileMap.put(clz, file)
    }

    def isZStackClass(Class clz) {
        if (clz.name.startsWith("java.")) {
            return false
        } else if (clz.name.startsWith("org.zstack")) {
            return true
        } else {
            throw new CloudRuntimeException("${clz.name} is neither JRE class nor ZStack class")
        }
    }

    def addToLaterResolvedClassesIfNeed(Class clz) {
        if (!sdkFileMap.containsKey(clz)) {
            laterResolvedClasses.add(clz)
        }
    }

    def generateResponseClass(Class responseClass) {
        logger.debug("generating class: ${responseClass.name}")

        RestResponse at = responseClass.getAnnotation(RestResponse.class)

        def fields = [:]

        def addToFields = { String fname, Field f ->
            if (isZStackClass(f.type)) {
                addToLaterResolvedClassesIfNeed(f.type)
                fields[fname] = f
            } else {
                fields[fname] = f
            }
        }

        if (!at.mappingAllTo().isEmpty()) {
            Field f = responseClass.getDeclaredField(at.mappingAllTo())
            addToFields(at.mappingAllTo(), f)
        } else {
            at.mappingFields().each { s ->
                def ss = s.split("=")
                def dst = ss[0].trim()
                def src = ss[1].trim()

                Field f = responseClass.getDeclaredField(src)
                addToFields(dst, f)
            }
        }
        
        def output = []
        fields.each { String name, Field f ->
            output.add(makeFieldText(name, f))
        }

        def className = responseClass.simpleName
        className = StringUtils.removeStart(className, "API")
        className = StringUtils.removeEnd(className, "Event")
        className = StringUtils.removeEnd(className, "Reply")
        className = StringUtils.capitalize(className)
        className = "${className}Result"

        SdkFile file = new SdkFile()
        file.fileName = "${className}.java"
        file.content = """package org.zstack.sdk;

public class ${className} {
${output.join("\n")}
}
"""
        sdkFileMap[responseClass] = file
    }

    def makeFieldText(String fname, Field field) {
        // zstack type
        if (isZStackClass(field.type)) {
            addToLaterResolvedClassesIfNeed(field.type)

            return """\
    public ${field.type.simpleName} ${fname};
"""
        }

        // java type
        if (Collection.class.isAssignableFrom(field.type)) {
            Class genericType = FieldUtils.getGenericType(field)

            if (genericType != null) {
                if (isZStackClass(genericType)) {
                    addToLaterResolvedClassesIfNeed(genericType)
                }

                return """\
    public ${field.type.name}<${genericType.simpleName}> ${fname};
"""
            } else {
                return """\
    public ${field.type.name} ${fname};
"""
            }
        } else if (Map.class.isAssignableFrom(field.type)) {
            Class genericType = FieldUtils.getGenericType(field)

            if (genericType != null) {
                if (isZStackClass(genericType)) {
                    addToLaterResolvedClassesIfNeed(genericType)
                }

                return """\
    public ${field.type.name}<String, ${genericType.simpleName}> ${fname};
"""
            } else {
                return """\
    public ${field.type.name} ${fname};
"""
            }
        } else {
            return """\
    public ${field.type.name} ${fname};
"""
        }
    }
}
