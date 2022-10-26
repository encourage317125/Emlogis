package com.emlogis.model;

import com.emlogis.common.security.ACLProtected;
import com.emlogis.common.security.Permissions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
public abstract class ACEProtectedEntity extends BaseEntity implements Serializable, ACLProtected {
	
	@Column(nullable = false)
	private	String 	path = "no path set";

	private	transient Set<Permissions> permissions;			// permissions associated with this object when using ACL

	public ACEProtectedEntity() {
		super();
	}

	public ACEProtectedEntity(PrimaryKey primaryKey) {
		super(primaryKey);
	}


	/* (non-Javadoc)
	 * @see com.emlogis.model.ACLProtected#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		if (path != null && !path.startsWith(PATH_SEPARATOR)) {
			path = PATH_SEPARATOR + path;
		}
		this.path = path;
	}

	public String setPath(String ... pathcomponent) {
		String fullpath = "";
		for (int i = 0; i < pathcomponent.length; i++) {
			fullpath += (PATH_SEPARATOR + pathcomponent[i]);
		}
		path = fullpath;
		return path;
	}
	
	/* (non-Javadoc)
	 * @see com.emlogis.model.ACLProtected#getPermissions()
	 */
	@Override
	public Set<Permissions> getPermissions() {
		return permissions;
	}
	
	/* (non-Javadoc)
	 * @see com.emlogis.model.ACLProtected#hasPermission(com.emlogis.common.security.Permissions)
	 */
	@Override
	public boolean hasPermission(Permissions perm) {
		return permissions.contains(perm);
	}

	@Override
	public void setPermissions(Set<Permissions> permissions) {
		this.permissions = permissions;
	}

	@Override
	public void initPermissions() {
		if (permissions == null) {
			permissions = new HashSet<Permissions>();
		}
	}
	
	@Override
	public void addPermission(Permissions permission) {
		initPermissions();
		this.permissions.add(permission);
	}
	
	@Override
	public void removePermission(Permissions permission) {
		this.permissions.remove(permission);
	}

}
