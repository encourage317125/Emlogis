package com.emlogis.rest.security;

import com.emlogis.common.security.Permissions;

import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

@Inherited
@NameBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)

public @interface RequirePermissions {
	Permissions[] permissions();
}

