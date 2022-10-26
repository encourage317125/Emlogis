package com.emlogis.model.notification.dto;

import com.emlogis.model.dto.ReadDto;
import com.emlogis.model.notification.MsgDeliveryProviderStatus;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.MsgProviderType;
import com.emlogis.model.notification.ProviderAttributeMetadata;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;


/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public  class MsgDeliveryProviderSettingsDto extends ReadDto {
	

    private String 					id;   
    
    private String 					name;    

    private String 					description; 
        
    private MsgDeliveryType			deliveryType;			

    private MsgProviderType 		providerType;
    
    private boolean					isActive;
    
    private Map<String,String> 		settings;               
    
    private MsgDeliveryProviderStatus status;

    private String					statusInfo;
    
    private long			 		activationChanged;
    
    private long 					lastChecked;    

    private Map<String,ProviderAttributeMetadata> providerAttributeMetadata;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public MsgDeliveryType getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(MsgDeliveryType deliveryType) {
		this.deliveryType = deliveryType;
	}

	public MsgProviderType getProviderType() {
		return providerType;
	}

	public void setProviderType(MsgProviderType providerType) {
		this.providerType = providerType;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Map<String,String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String,String> settings) {
		this.settings = settings;
	}

	public MsgDeliveryProviderStatus getStatus() {
		return status;
	}

	public void setStatus(MsgDeliveryProviderStatus status) {
		this.status = status;
	}

	public String getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(String statusInfo) {
		this.statusInfo = statusInfo;
	}

	public long getActivationChanged() {
		return activationChanged;
	}

	public void setActivationChanged(long activationChanged) {
		this.activationChanged = activationChanged;
	}

	public long getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(long lastChecked) {
		this.lastChecked = lastChecked;
	}

	public Map<String, ProviderAttributeMetadata> getProviderAttributeMetadata() {
		return providerAttributeMetadata;
	}

	public void setProviderAttributeMetadata(
			Map<String, ProviderAttributeMetadata> providerAttributeMetadata) {
		this.providerAttributeMetadata = providerAttributeMetadata;
	}
	
}

