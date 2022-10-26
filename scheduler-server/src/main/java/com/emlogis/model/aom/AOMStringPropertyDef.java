package com.emlogis.model.aom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AOMStringPropertyDef extends AOMPropertyDef  {

	private int minLength = -1;

	private int maxLength = -1;

	private String validationRegex;
	
	private String defaultValue;
	

	public AOMStringPropertyDef() {}

	public AOMStringPropertyDef(String tenantId) {
		super(tenantId);
		setType(AOMPropertyType.String);
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public String getValidationRegex() {
		return validationRegex;
	}

	public void setValidationRegex(String validationRegex) {
		this.validationRegex = validationRegex;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
    
	
}
