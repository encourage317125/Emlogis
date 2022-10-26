package com.emlogis.common.validation.annotations;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface ValidatePassword {

    String tenantField() default StringUtils.EMPTY;

    String passwordField();

}
