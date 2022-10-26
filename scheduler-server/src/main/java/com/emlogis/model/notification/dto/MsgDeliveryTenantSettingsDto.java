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
public  class MsgDeliveryTenantSettingsDto extends ReadDto {
	
	// tenant specific attributes
    private String 					id;

    private Map<String,String> 		settings;
    
    private boolean					isActive;
    
    private MsgDeliveryProviderStatus status;

    private String					statusInfo;
    
    private long			 		activationChanged;
    
    private long 					lastChecked;    

    private Map<String,ProviderAttributeMetadata> tenantProvidersMetaData;
	
    
    // provider attributes	
    private String 					providerId;   
            
    private MsgDeliveryType			deliveryType;			

    private MsgProviderType 		providerType;
    
    private String 					providerName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public Map<String, ProviderAttributeMetadata> getTenantProvidersMetaData() {
		return tenantProvidersMetaData;
	}

	public void setTenantProvidersMetaData(
			Map<String, ProviderAttributeMetadata> tenantProvidersMetaData) {
		this.tenantProvidersMetaData = tenantProvidersMetaData;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
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

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}    


}

