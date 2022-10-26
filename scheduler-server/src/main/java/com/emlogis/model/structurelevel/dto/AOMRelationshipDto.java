package com.emlogis.model.structurelevel.dto;


import com.emlogis.model.dto.ReadDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AOMRelationshipDto extends ReadDto implements Serializable {
	

	private String 	id;

	private	String 	type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}				
	    
	

}