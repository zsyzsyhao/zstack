package org.zstack.rest;

import org.reflections.Reflections;
import org.zstack.core.Platform;
import org.zstack.header.Component;
import org.zstack.header.rest.Rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by xing5 on 2016/12/7.
 */
public class RestServer implements Component {

    class Api {
        Class apiClass;
        Rest annotation;
    }

    private Map<Class, Api> apis = new HashMap<>();

    @Override
    public boolean start() {
        build();
        return true;
    }

    private void build() {
        Reflections reflections = Platform.getReflections();
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rest.class);

        for (Class clz : classes) {
            Rest at = (Rest) clz.getAnnotation(Rest.class);
        }
    }

    @Override
    public boolean stop() {
        return true;
    }
}
