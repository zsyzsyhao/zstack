package org.zstack.rest;

import org.apache.commons.lang.StringUtils;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.message.GsonTransient;
import org.zstack.header.message.NoJsonSchema;
import org.zstack.utils.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by xing5 on 2016/12/12.
 */
public class JsonSchemaBuilder {
    Object object;

    private Map<String, List<String>> schema = new HashMap<>();

    public JsonSchemaBuilder(Object object) {
        this.object = object;
    }

    private boolean isSkip(Field f) {
        return f.isAnnotationPresent(NoJsonSchema.class) || Modifier.isStatic(f.getModifiers())
                || f.isAnnotationPresent(GsonTransient.class);
    }

    private void build(Object o, Stack<String> paths) throws IllegalAccessException {
        List<Field> fields = FieldUtils.getAllFields(o.getClass());

        for (Field f : fields) {
            if (isSkip(f)) {
                continue;
            }

            f.setAccessible(true);
            Object value = f.get(o);
            if (value == null) {
                // null value
                continue;
            }

            if (value.getClass().getName().startsWith("java.")) {
                // for JRE classes, only deal with Collection and Map
                if (value instanceof List) {
                    List c = (List) value;

                    for (int i=0; i<c.size(); i++) {
                        paths.push(String.format("%s[%s]", f.getName(), i));
                        build(c.get(i), paths);
                        paths.pop();
                    }

                } else if (value instanceof Map) {
                    for (Object me : ((Map) value).entrySet()) {
                        Map.Entry e = (Map.Entry) me;
                        paths.push(String.format("%s.%s", f.getName(), e.getKey().toString()));
                        build(e.getValue(), paths);
                        paths.pop();
                    }
                }

                // don't record standard JRE classes
                continue;
            }


            paths.push(f.getName());
            build(value, paths);
            paths.pop();
        }

        if (!paths.isEmpty()) {
            List<String> all = schema.computeIfAbsent(o.getClass().getName(), k -> new ArrayList<>());
            all.add(StringUtils.join(paths, "."));
        }
    }

    public Map<String, List<String>> build() {
        try {
            if (!object.getClass().getName().startsWith("org.zstack")) {
                throw new CloudRuntimeException(String.format("only a org.zstack.* object can be built schema, %s is not", object.getClass()));
            }

            build(object, new Stack<>());
            return schema;
        } catch (IllegalAccessException e) {
            throw new CloudRuntimeException(e);
        }
    }
}
