package com.emlogis.model.tenant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;


/**
 * ServiceProvider is the class that captures the organization managing this system and customers 
 * 
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ServiceProvider extends Tenant {
	
	public ServiceProvider() {
		super();
		this.setProductLicense(new ModuleLicense("EGS Management", ModuleStatus.Subscribed, -1));		
	}
    
}


