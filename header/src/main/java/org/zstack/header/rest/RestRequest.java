package org.zstack.header.rest;

import org.springframework.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by xing5 on 2016/12/7.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestRequest {
    String path();
    HttpMethod method() default HttpMethod.PUT;
    String actionName() default "";
    String parameterName();
    String[] mappingFields() default {};
    Class responseClass();
}
