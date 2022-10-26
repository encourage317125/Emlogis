package com.emlogis.model.aom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AOMTenantMetamodel implements Serializable {

	private	Map<String,AOMEntityDef> entityDefs = new HashMap<>();		// collection of entities keyed by entity name
	
	private	Map<String,AOMRelationshipDef> relationshipDefs = new HashMap<>();	// collection of relationship types keyed by relationship name

	public AOMTenantMetamodel() { 
		super(); 
	}
	
	public Map<String, AOMEntityDef> getEntityDefs() {
		return entityDefs;
	}

	public void setEntityDefs(Map<String, AOMEntityDef> entityDefs) {
		this.entityDefs = entityDefs;
	}

	public Map<String, AOMRelationshipDef> getRelationshipDefs() {
		return relationshipDefs;
	}

	public void setRelationshipDefs(Map<String, AOMRelationshipDef> relationshipDefs) {
		this.relationshipDefs = relationshipDefs;
	}
	
	

}
