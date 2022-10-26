package com.emlogis.model.structurelevel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.emlogis.model.dto.BaseEntityDto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AOMEntityDto extends BaseEntityDto {

	private	String aomEntityType;
	
	private	 Map<String,Object> properties;	// aom properties 

	public String getAomEntityType() {
		return aomEntityType;
	}

	public void setAomEntityType(String aomEntityType) {
		this.aomEntityType = aomEntityType;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

}