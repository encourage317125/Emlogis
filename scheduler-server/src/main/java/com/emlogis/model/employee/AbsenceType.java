package com.emlogis.model.employee;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Site;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "AbsenceType")
public class AbsenceType extends BaseEntity {
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String description;
	
	@Column(nullable = false)
	private int timeToDeductInMin;
	
	private	boolean	isActive = true;
	
    // Many-to-one association to Site
	// You must have a employee associated Employee Availability timeFrames
    @ManyToOne(optional = false)
    @JoinColumns({
        @JoinColumn(name = "siteTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "siteId", referencedColumnName = "id")
    })
    private Site site;	
	
	public AbsenceType() {}
	
	public AbsenceType(PrimaryKey primaryKey) {
		super(primaryKey);		
	}
	
	public AbsenceType(PrimaryKey primaryKey, String name, String description, int timeToDeductInMin, boolean isActive, Site site) {
		super(primaryKey);
		this.name = name;
		this.description = description;
		this.timeToDeductInMin = timeToDeductInMin;
		this.isActive =	isActive;
		this.site = site;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTimeToDeductInMin() {
		return timeToDeductInMin;
	}

	public void setTimeToDeductInMin(int timeToDeductInMin) {
		this.timeToDeductInMin = timeToDeductInMin;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}	
	
	public String getSiteId() {
		return site.getId();
	}
	
}
