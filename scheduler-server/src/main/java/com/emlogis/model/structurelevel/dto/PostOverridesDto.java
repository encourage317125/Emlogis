package com.emlogis.model.structurelevel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.dto.ReadDto;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostOverridesDto extends ReadDto {

	private	String name;
	
	private Map<ConstraintOverrideType, Boolean> overrideOptions;

	public PostOverridesDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
		return overrideOptions;
	}

	public void setOverrideOptions(
			Map<ConstraintOverrideType, Boolean> overrideOptions) {
		this.overrideOptions = overrideOptions;
	}

}