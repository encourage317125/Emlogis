package com.emlogis.model.tenant.settings.scheduling;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = BooleanOptimizationSetting.class, name = "booleanOption"),
	@Type(value = OptimPreferenceOptimizationSetting.class, name = "optimPrefOption")
})


public abstract class OptimizationSetting {
	
	private OptimizationSettingName name;

	public OptimizationSetting() {
		super();
	}

	public OptimizationSetting(OptimizationSettingName name) {
		super();
		this.name = name;
	}

	public OptimizationSettingName getName() {
		return name;
	}

	public void setName(OptimizationSettingName name) {
		this.name = name;
	}
	
}
