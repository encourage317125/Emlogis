package com.emlogis.model.aom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AOMTypeDef {

	private	String aomType;
	
	private	String label;
	
	private	Map<String,AOMPropertyDef> 	propertyDefs = new HashMap<>();		// collection of property definitions keyed by prperty name

	public AOMTypeDef() {}

	public AOMTypeDef(String aomType) {
		setAomType(aomType);
	}

	public String getAomType() {
		return aomType;
	}

	public void setAomType(String aomType) {
		this.aomType = aomType;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, AOMPropertyDef> getPropertyDefs() {
		return propertyDefs;
	}

	public void setPropertyDefs(Map<String, AOMPropertyDef> propertyDefs) {
		this.propertyDefs = propertyDefs;
	}

}
