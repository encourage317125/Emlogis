package com.emlogis.model.aom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to capture relationships between StructureLevel entities.
 * 
 * For instance, the Site to Teams relationships would be captured as:
 * 		name = Site_Team
 * 		isComposite = true
 * 		srcEntityName = Site
 * 		srcEntityName = Team
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AOMRelationshipDef {
	
	public final static String	SITE_TEAM_REL	= "Site_Team";	// relationship type to associate a Team to a Site

	private	String 	type;
	
	private	String 	label;

	private boolean isComposite;
	
    @Column(nullable = false)
	private String 	srcEntityType;

	private String 	srcCardinality;
	
    @Column(nullable = false)
	private String 	dstEntityType;
	
	private String 	dstCardinality;
	

	public AOMRelationshipDef() {}

	public AOMRelationshipDef(String type) {
		setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isComposite() {
		return isComposite;
	}

	public void setComposite(boolean isComposite) {
		this.isComposite = isComposite;
	}


	public String getSrcEntityType() {
		return srcEntityType;
	}

	public void setSrcEntityType(String srcEntityType) {
		this.srcEntityType = srcEntityType;
	}

	public String getSrcCardinality() {
		return srcCardinality;
	}

	public void setSrcCardinality(String srcCardinality) {
		this.srcCardinality = srcCardinality;
	}

	public String getDstEntityType() {
		return dstEntityType;
	}

	public void setDstEntityType(String dstEntityType) {
		this.dstEntityType = dstEntityType;
	}

	public String getDstCardinality() {
		return dstCardinality;
	}

	public void setDstCardinality(String dstCardinality) {
		this.dstCardinality = dstCardinality;
	}
    
}
