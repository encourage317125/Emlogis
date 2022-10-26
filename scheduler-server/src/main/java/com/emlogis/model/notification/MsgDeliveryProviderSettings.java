package com.emlogis.model.notification;

import com.emlogis.common.EmlogisUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public /*abstract*/ class MsgDeliveryProviderSettings implements Serializable {
	
    public static final String TWILIO_PROVIDER_ID 		= "twillio";
    public static final String PRIMARYEMAIL_PROVIDER_ID = "primaryEmail";
    public static final String SECONDARYEMAIL_PROVIDER_ID = "secondaryEmail";

    // Twillio related constants
    public static final String TWILIO_PROVIDER_Name 		= "Twillio";
    public static final String TWILIO_PROVIDER_ACCOUNTID 	= "accountId";
    public static final String TWILIO_PROVIDER_SUBACCOUNTID 	= "subaccountId";
    public static final String TWILIO_PROVIDER_AUTHKEY 		= "authKey";
    public static final String TWILIO_PROVIDER_FROMNUMBER 	= "fromNumber";
    
    public static final String TENANT_TWILIO_TENANTCALLNUMBER 	= "tenantCallNumber";
    
    
    // POPSMSTP email related constants
    public static final String POPSMTP_PROVIDER_Name 			= "POPSMSTP";
    public static final String POPSMTP_PROVIDER_SENDHOST 		= "sendHost";
    public static final String POPSMTP_PROVIDER_SENDPORT 		= "sendPort";
    public static final String POPSMTP_PROVIDER_RECEIVEHOST 	= "receiveHost";
    public static final String POPSMTP_PROVIDER_RECEIVEPORT 	= "receivePort";
    public static final String POPSMTP_PROVIDER_USERNAME 		= "userName";
    public static final String POPSMTP_PROVIDER_PASSWORD 		= "password";
    public static final String POPSMTP_PROVIDER_FROM 		    = "fromAddress";
    public static final String POPSMTP_PROVIDER_REPLYTO 		= "replytoAddress";
    
    public static final String TENANT_MAILBOX 				= "tenantMailBox";

    
    // this map defines the list of attributes and their type, per provider type
    private static Map<String, Map<String,ProviderAttributeMetadata>>	providersMetaData = new HashMap();	// map of provider metadata, keyed by MsgDeliveryType.providerName 
    static {
    	String key = MsgDeliveryType.SMS.toString() + "." + MsgProviderType.Twillio.toString();
    	Map<String,ProviderAttributeMetadata> providerMetaData = new HashMap();
    	providerMetaData.put(TWILIO_PROVIDER_ACCOUNTID, new ProviderAttributeMetadata(TWILIO_PROVIDER_ACCOUNTID, "Access Id", "string", null));
    	providerMetaData.put(TWILIO_PROVIDER_AUTHKEY, new ProviderAttributeMetadata(TWILIO_PROVIDER_AUTHKEY, "Access Key", "string", null));
    	providerMetaData.put(TWILIO_PROVIDER_FROMNUMBER, new ProviderAttributeMetadata(TWILIO_PROVIDER_FROMNUMBER, "From number", "phoneNb", null));
    	providersMetaData.put(key, providerMetaData);
    	
    	key = MsgDeliveryType.EMAIL.toString() + "." + MsgProviderType.POPSMTPEmail.toString();
    	providerMetaData = new HashMap();
    	providerMetaData.put(POPSMTP_PROVIDER_SENDHOST, new ProviderAttributeMetadata(POPSMTP_PROVIDER_SENDHOST, "Send Host Name", "string", null));
    	providerMetaData.put(POPSMTP_PROVIDER_SENDPORT, new ProviderAttributeMetadata(POPSMTP_PROVIDER_SENDPORT, "Send Port number", "int", "25"));
    	providerMetaData.put(POPSMTP_PROVIDER_RECEIVEHOST, new ProviderAttributeMetadata(POPSMTP_PROVIDER_RECEIVEHOST, "Receive Host Name", "string", null));
    	providerMetaData.put(POPSMTP_PROVIDER_RECEIVEPORT, new ProviderAttributeMetadata(POPSMTP_PROVIDER_RECEIVEPORT, "Receive Port number", "int", "7993"));
    	providerMetaData.put(POPSMTP_PROVIDER_USERNAME, new ProviderAttributeMetadata(POPSMTP_PROVIDER_USERNAME, "User Name", "string", null));
    	providerMetaData.put(POPSMTP_PROVIDER_PASSWORD, new ProviderAttributeMetadata(POPSMTP_PROVIDER_PASSWORD, "User Password", "string", null));
    	providerMetaData.put(POPSMTP_PROVIDER_FROM, new ProviderAttributeMetadata(POPSMTP_PROVIDER_FROM, "From Address", "string", null));
    	providerMetaData.put(POPSMTP_PROVIDER_REPLYTO, new ProviderAttributeMetadata(POPSMTP_PROVIDER_REPLYTO, "ReplyTo Adddress", "string", null));
    	providersMetaData.put(key, providerMetaData); 	
    }
    
    // this map defines the list of tenant specific attributes and their type, per provider type
    private static Map<String, Map<String,ProviderAttributeMetadata>>	tenantProvidersMetaData = new HashMap();	// map of provider metadata, keyed by MsgDeliveryType.providerName 
    static {
    	String key = MsgDeliveryType.SMS.toString() + "." + MsgProviderType.Twillio.toString();
    	Map<String,ProviderAttributeMetadata> providerMetaData = new HashMap();
    	providerMetaData.put(TENANT_TWILIO_TENANTCALLNUMBER, new ProviderAttributeMetadata(TENANT_TWILIO_TENANTCALLNUMBER, "Tenant From number", "phoneNb", null));
    	tenantProvidersMetaData.put(key, providerMetaData);
    	
    	key = MsgDeliveryType.EMAIL.toString() + "." + MsgProviderType.POPSMTPEmail.toString();
    	providerMetaData = new HashMap();
    	providerMetaData.put(TENANT_MAILBOX, new ProviderAttributeMetadata(TENANT_MAILBOX, "Tenant Mailbox", "string", null));
    	tenantProvidersMetaData.put(key, providerMetaData); 	
    }
    
    @Id()
    @Column(unique = true, length = 64)
    private String 			id;              		// unique provider id
    
    private String 			name;                  	// provider Name

    private String 			description;           	// provider short description
    
    private MsgDeliveryType	deliveryType;			// delivery type (SMS | EMAIL)

    private MsgProviderType providerType;			// provider type (class)
    
    private boolean			isActive = false;
    
    @Column(unique = true, length = 1024)
    private String 			settings;                // json serialized settings (key values)
    
    private MsgDeliveryProviderStatus status;

    private String			statusInfo;

    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime 		activationChanged = new DateTime(0); // provider last activation status change date/time UTC

    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime 		lastChecked = new DateTime(0);    // provider last check date/time UTC
    
    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime 		created = new DateTime(0);    // entity creation date/time UTC

    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime 		updated = new DateTime(0);    // entity last update date/time UTC
    
    private String 			updatedBy;
    
    
    public MsgDeliveryProviderSettings() {
        setCreated(System.currentTimeMillis());
        setStatus(MsgDeliveryProviderStatus.NOTCONFIGURED);   
		setupDefaultSettings();
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
	
    public long getCreated() {
        return created.getMillis();
    }

    public void setCreated(long created) {
        this.created = new DateTime(created);
        setUpdated(created);
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }
    
    public long getUpdated() {
        return updated.getMillis();
    }

    public void setUpdated(long updated) {
        this.updated = new DateTime(updated);
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }
    
    public void touch() {
        this.setUpdated(System.currentTimeMillis());
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
	
	public long getLastChecked() {
		return lastChecked.getMillis();
	}

	public void setLastChecked(long lastChecked) {
		this.lastChecked = new DateTime(lastChecked);
	}

    public void setLastChecked(DateTime lastChecked) {
        this.lastChecked = lastChecked;
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
    

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
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
	
	/**
	 * @return the provider metadata associated to the current instance
	 */
	public Map<String,ProviderAttributeMetadata> getProviderAttributeMetadata() {
		return getProviderAttributeMetadataByType(deliveryType, providerType);
	}

	/**
	 * @return the provider tenant specific metadata associated to a provider type
	 * 
	 * Note: This static method is named a little differently although it could have same name as the one above, because
	 * having the same name causes pbs in DtoMapper which doesn;t differentiate static/instance methods
	 */
	public static Map<String,ProviderAttributeMetadata> getProviderAttributeMetadataByType(MsgDeliveryType deliveryType, MsgProviderType providerType) {
		String key = deliveryType.toString() + "." + providerType.toString();
		return providersMetaData.get(key);
	}

	/**
	 * @return the provider tenant specific metadata associated to the current instance
	 */
	public Map<String,ProviderAttributeMetadata> getTenantProviderAttributeMetadata() {
		return getTenantProviderAttributeMetadataByType(deliveryType, providerType);
	}

	/**
	 * @return the provider tenant specific metadata associated to a provider type
	 * 
	 * Note: This static method is named a little differently although it could have same name as the one above, because
	 * having the same name causes pbs in DtoMapper which doesn;t differentiate static/instance methods
	 */
	public static Map<String,ProviderAttributeMetadata> getTenantProviderAttributeMetadataByType(MsgDeliveryType deliveryType, MsgProviderType providerType) {
		String key = deliveryType.toString() + "." + providerType.toString();
		return tenantProvidersMetaData.get(key);
	}

	public String getClName() {                        // used and required by toDto conversion
        return this.getClass().getSimpleName();
    }

    public void setClName(String cName) {
    }

    public String settingsValue(String name){
        return getSettings().get(name);
    }
    
/*
	public Class<TenantDto> getReadDtoClass() {
		return TenantDto.class;
	}
*/
    
    
}


