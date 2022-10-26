package com.emlogis.model.common;

public class CacheConstants {
	
	public static final String ORG_HOLIDAYS_CACHE = "OrgHolidaysCache"; // cache of list of holidays per organization

    public static final String EMP_AVAILABILITY_CACHE = "EmpAvailabilityCache"; // cache of list of availability records per employee * organization
    public static final int EMP_AVAILABILITY_CACHE_MONTHS = 24;

    public static final String ALLOWED_BY_ACL_TEAM_IDS_CACHE = "AllowedByAclTeamIdsCache";

}
