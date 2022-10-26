package com.emlogis.common.validation.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface ValidateAll {

    Validate[] value() default {};

    ValidateDate[] dates() default {};

    ValidateNumeric[] numerics() default {};

    ValidatePaging[] pagings() default {};

    ValidatePassword[] passwords() default {};

    ValidateRegex[] regexes() default {};

    ValidateStrLength[] strLengths() default {};

    ValidateUnique[] uniques() default {};

}
