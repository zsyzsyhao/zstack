package org.zstack.utils;

import groovy.lang.GroovyClassLoader;

import java.io.InputStream;

/**
 */
public class GroovyUtils {
    public static <T> T loadClass(String scriptPath, ClassLoader parent) {
        try {
            Class clz = getClass(scriptPath, parent);
            return (T)clz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class getClass(String scriptPath, ClassLoader parent) {
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        InputStream in =  parent.getResourceAsStream(scriptPath);
        String script = StringDSL.inputStreamToString(in);
        return loader.parseClass(script);
    }
}
