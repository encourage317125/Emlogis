package com.emlogis.common.validation.annotations;

import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface Validate {

    Class<? extends Validator> validator();

    String name() default ValidationObject.DEFAULT_VALUE;

    String group() default "";

    boolean expectedResult() default true;

    Class type() default Object.class;

}
