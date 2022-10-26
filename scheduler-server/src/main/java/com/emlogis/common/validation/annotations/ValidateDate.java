package com.emlogis.common.validation.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface ValidateDate {

    String field();

    String after() default "1970/01/01 00:00:00";

    String before() default "2100/01/01 00:00:00";

    String format() default "yyyy/MM/dd hh:mm:ss";

    boolean passNull() default false;

}
