package com.emlogis.common.validation.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface ValidateStrLength {

    String field();

    int min() default 0;

    int max() default Integer.MAX_VALUE;

    boolean passNull() default false;

}
