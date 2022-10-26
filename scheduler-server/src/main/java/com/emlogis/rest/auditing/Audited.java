package com.emlogis.rest.auditing;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

@Inherited
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
	LogRecordCategory 	type() default LogRecordCategory.Application;
	String 				label() default "";
	ApiCallCategory 	callCategory() default ApiCallCategory.Unclassified;
	ParametersLogging 	paramsLogging() default ParametersLogging.All;
}

