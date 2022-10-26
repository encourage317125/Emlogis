package com.emlogis.model.notification.dto;

import com.emlogis.model.dto.ReadDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;


/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public  class MsgDeliveryTenantSettingsUpdateDto extends ReadDto {
	
	// tenant specific attributes
    private Map<String,String> 		settings;   
    private boolean					isActive;	
    
    // provider attributes	
    private String 					providerId;

	public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}   
            

}

