package com.emlogis.common.security;

import java.util.Set;

public interface ACLProtected {

	public static final String PATH_SEPARATOR = "/";

	public abstract String getPath();

	public abstract Set<Permissions> getPermissions();

	public abstract boolean hasPermission(Permissions perm);

	void setPermissions(Set<Permissions> permissions);

	void initPermissions();

	void addPermission(Permissions permission);

	void removePermission(Permissions permission);

}