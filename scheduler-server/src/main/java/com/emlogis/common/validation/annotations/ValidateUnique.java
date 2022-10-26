package com.emlogis.common.validation.annotations;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InterceptorBinding
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface ValidateUnique {

    boolean globally() default false;

    String[] fields();

    boolean expectedResult() default true;

    Class type();

}
