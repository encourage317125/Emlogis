package com.emlogis.model.notification;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import java.util.HashMap;
import java.util.Map;


/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
public  class MsgDeliveryTenantSettings extends BaseEntity {
	
    public static final String TENANT_MAILBOX 				= "tenantMailBox";
    public static final String TWILIO_PROVIDER_FROMNUMBER 	= "tenantCallNumber";
	
    private MsgDeliveryType		type;					// this field is a bit redundant  with MsgDeliveryProviderSettings 
    													// ie, not really needed,  but helpful when looking at DB content

    @Column(unique = true, length = 1024)
    private String 				settings;                // json serialized settings (key values)

    @ManyToOne(fetch = FetchType.LAZY)
    private MsgDeliveryProviderSettings deliveryProviderSettings;
    
    private MsgDeliveryProviderStatus status;	// status of tenant specific config (ex tenant message box for email)

    private String				statusInfo;
    
    private boolean				isActive = true;
    
    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime 		activationChanged = new DateTime(0); // provider last activation status change date/time UTC

    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastChecked = new DateTime(0);    // provider last check date/time UTC

    /**
     * Required fields constructor.
     * 
     * @param primaryKey
     * @param data.name
     * @param abbreviation
     * @param description
     */
    public MsgDeliveryTenantSettings(PrimaryKey primaryKey, MsgDeliveryType type) {
		super(primaryKey);
		this.type = type;    	
		setupDefaultSettings();
	}
    
    public MsgDeliveryTenantSettings() {
        setCreated(System.currentTimeMillis());
        setStatus(MsgDeliveryProviderStatus.NOTCONFIGURED);    
    }

	private void setupDefaultSettings() {
		this.setSettings(getDefaultSettings());
	}

    private Map<String,String> getDefaultSettings() {
    	
    	return new HashMap<String,String>();
	}
    
    public Map<String,String> getSettings() {
    	if (StringUtils.isBlank(settings)) {
    		return getDefaultSettings();
    	} else {
    		return  (Map<String,String>)EmlogisUtils.fromJsonString(settings);
    	}
	}
    
	public void setSettings(Map<String,String> settings) {
		this.settings = EmlogisUtils.toJsonString(settings);
	}	    
	
	public void setSettings(String settings) {
		if (StringUtils.isBlank(settings)) {
			setSettings(getDefaultSettings());
		}
		else {
			// deserialize json to make sure it looks valid
			Map<String,String> settingsList = (Map<String,String>)EmlogisUtils.fromJsonString(settings); 
			this.settings = settings;
		}
	}
    
    public void touch() {
        this.setUpdated(System.currentTimeMillis());
    }

	public long getLastChecked() {
		return lastChecked.getMillis();
	}

	public void setLastChecked(long lastChecked) {
		this.lastChecked = new DateTime(lastChecked);
	}

    public void setLastChecked(DateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

	public MsgDeliveryType getType() {
		return type;
	}

	public void setType(MsgDeliveryType type) {
		this.type = type;
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

	public MsgDeliveryProviderSettings getDeliveryProviderSettings() {
		return deliveryProviderSettings;
	}

	public void setDeliveryProviderSettings(
			MsgDeliveryProviderSettings deliveryProviderSettings) {
		this.deliveryProviderSettings = deliveryProviderSettings;
		setType(deliveryProviderSettings.getDeliveryType());
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public long getActivationChanged() {
		return activationChanged.getMillis();
	}

	public void setActivationChanged(DateTime activationChanged) {
		this.activationChanged = activationChanged;
	}

	public void setActivationChanged(long activationChanged) {
		this.activationChanged = new DateTime(activationChanged);
	}

	public String getClName() {                        // used and required by toDto conversion
        return this.getClass().getSimpleName();
    }

    public void setClName(String cName) {
    }

	public Map<String, ProviderAttributeMetadata> getTenantProviderAttributeMetadata() {
		return deliveryProviderSettings.getTenantProviderAttributeMetadata();
	}
    
/*
	public Class<TenantDto> getReadDtoClass() {
		return TenantDto.class;
	}
*/
}


