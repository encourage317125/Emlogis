package com.emlogis.model.tenant;

import com.emlogis.model.structurelevel.Holiday;
import com.emlogis.model.tenant.settings.SchedulingSettings;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Organization is the base class for Customers 
 * 
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Organization extends Tenant {

    @OneToMany
    private Set<Holiday> holidays = new HashSet<>();

    @JsonIgnore
    @OneToOne(targetEntity = SchedulingSettings.class, fetch = FetchType.LAZY)
    @JoinColumns( {
		@JoinColumn(name = "orgschedulingsettings_tenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "orgschedulingsettings_id", referencedColumnName = "id")
    })
    private SchedulingSettings schedulingSettings;
        
    public Organization() {
		super();
	}

	public Set<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(Set<Holiday> holidays) {
        this.holidays = holidays;
    }

	public SchedulingSettings getSchedulingSettings() {
		return schedulingSettings;
	}

	public void setSchedulingSettings(SchedulingSettings schedulingSettings) {
		this.schedulingSettings = schedulingSettings;
	}
    
}


