package com.emlogis.model.aom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AOMEntityDef {

	private	String aomEntityType;
	
	private	Map<String,AOMTypeDef> typeDefs = new HashMap<>();		// collection of type definitions keyed by type name
		
	public AOMEntityDef() {}

	public AOMEntityDef(String entityType) {
		setAomEntityType(entityType);
	}

	public String getAomEntityType() {
		return aomEntityType;
	}

	public void setAomEntityType(String aomEntityType) {
		this.aomEntityType = aomEntityType;
	}

	public Map<String, AOMTypeDef> getTypeDefs() {
		return typeDefs;
	}

	public void setTypeDefs(Map<String, AOMTypeDef> typeDefs) {
		this.typeDefs = typeDefs;
	}

    
}
