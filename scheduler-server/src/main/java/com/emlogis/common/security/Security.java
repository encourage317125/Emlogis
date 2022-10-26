package com.emlogis.common.security;

public class Security {
	
	public static final String TOKEN_HEADER_NAME = "EmlogisToken";
	
	// constants used for 'long' (rememberme) sessions
	public static final String REMEMBERME_HEADER_NAME = "rememberMe";
	public static final String REMEMBERMECLIENTID_HEADER_NAME = "rememberMeClientId";
	public static final String REMEMBERMECLIENTDESCR_HEADER_NAME = "rememberMeClientDescr";
	
	// constants used for extracting the tenantId from URL
	public static final String TENANTID_HEADER_NAME = "tenantId";
	public static final String EMLOGIS_DOMAINNAME = "emlogis.com";
	public static final String EMLOGIS_CLOUD_ID = "cloud";

}
