package org.zstack.header.rest;

import org.springframework.http.HttpMethod;

/**
 * Created by xing5 on 2016/12/7.
 */
public @interface Rest {
    String path();
    HttpMethod method() default HttpMethod.PUT;
    String actionName() default "";
    String parameterName();
    String[] requestMappingFields() default {};
    String responseMappingAllTo() default "";
    String[] responseMappingFields() default {};
}
