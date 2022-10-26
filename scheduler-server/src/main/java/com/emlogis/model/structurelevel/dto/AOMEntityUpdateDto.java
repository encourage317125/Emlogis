package com.emlogis.model.structurelevel.dto;


import com.emlogis.model.dto.UpdateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AOMEntityUpdateDto extends UpdateDto implements Serializable {

	private	String aomEntityType;
	
//	private	 Map<String,Object> properties;	// aom properties 

	public String getAomEntityType() {
		return aomEntityType;
	}

	public void setAomEntityType(String aomEntityType) {
		this.aomEntityType = aomEntityType;
	}
/*
	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
*/
}