package com.emlogis.model.contract.dto;

import com.emlogis.model.dto.ReadDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class ContractDTO extends ReadDto implements Serializable  {
	
	private String name;
	private String id;
	private boolean defaultContract;

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
	
	public boolean isDefaultContract() {
		return defaultContract;
	}

	public void setDefaultContract(boolean isDefaultContract) {
		this.defaultContract = isDefaultContract;
	}	
}
