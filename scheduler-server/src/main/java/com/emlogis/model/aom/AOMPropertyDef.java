package com.emlogis.model.aom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AOMPropertyDef {

	private	String name;
	
	private	String label;

	private	AOMPropertyType type;

	private boolean isUnique;
	
	private boolean isArray;
	
	private boolean isEnum;
	
	private boolean hasDefaultValue = false;
	
	private	Map<String,Object> enumValues = new HashMap<>();

	public AOMPropertyDef() {}

	public AOMPropertyDef(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public AOMPropertyType getType() {
		return type;
	}

	public void setType(AOMPropertyType type) {
		this.type = type;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	public boolean isEnum() {
		return isEnum;
	}

	public void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}

	public Map<String, Object> getEnumValues() {
		return enumValues;
	}

	public void setEnumValues(Map<String, Object> enumValues) {
		this.enumValues = enumValues;
	}

	public boolean isHasDefaultValue() {
		return hasDefaultValue;
	}

	public void setHasDefaultValue(boolean hasDefaultValue) {
		this.hasDefaultValue = hasDefaultValue;
	}
    
}
