package com.emlogis.model.notification.dto;

import com.emlogis.model.dto.UpdateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsgDeliveryProviderSettingsUpdateDto extends UpdateDto implements Serializable {
	
    private String 					name;    

    private String 					description; 
            
    private boolean					isActive;
    
    private Map<String,String> 		settings;               
    
	public MsgDeliveryProviderSettingsUpdateDto() {}

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

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}


}