package com.emlogis.rest.security;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//import javax.interceptor.InterceptorBinding;

//@Inherited
//@InterceptorBinding
@NameBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
//@Retention(value = RetentionPolicy.RUNTIME)

public @interface Authenticated {

}

