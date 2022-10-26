package com.emlogis.model.structurelevel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.emlogis.common.security.Permissions;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDto extends StructureLevelDto {
	
	private	Set<Permissions> permissions;			// permissions associated with this object when using ACL

    private boolean isDeleted;
    
    private	String	abbreviation;
    
    private	boolean	isActive;
    
    private long 	startDate;     

    private long 	endDate;     

    public Set<Permissions> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permissions> permissions) {
		this.permissions = permissions;
	}

	/**
	 * @return the isDeleted
	 */
	public boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

}