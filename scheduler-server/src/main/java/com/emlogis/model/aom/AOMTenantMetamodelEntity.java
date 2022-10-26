package com.emlogis.model.aom;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;

/**
 * AOMTenantMetamodelEntity is just a 'wrapper' around AOMTenantMetamodel, and is used to store a AOMTenantMetamodel instance as a blob
 * this allows manipulating the AOMTenantMetamodel instance in memory more easily as it is just a regular hierarchy of pojos
 * without any relationship to persistence.
 * Also, modifications to the metamodel are pretty rare, thus performance implications of storing a full tenant metamodel on each
 * modification is not a significant hit.
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class AOMTenantMetamodelEntity extends BaseEntity implements Serializable {

	public static final String METAMODEL_ID = "metamodel";
	
	@Column() 
    @Lob
	private	String aomTenantMetamodel;

	public AOMTenantMetamodelEntity() {}

	public AOMTenantMetamodelEntity(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	public String getAomTenantMetamodel() {
		return aomTenantMetamodel; 
	}

    public void setAomTenantMetamodel(String aomTenantMetamodel) {
        this.aomTenantMetamodel = aomTenantMetamodel;
    }

	public AOMTenantMetamodel getAomTenantMetamodelObj() throws IOException {
		if (aomTenantMetamodel == null) { 
			return null; 
		}
		ObjectMapper objMapper = new ObjectMapper();
		try {
			AOMTenantMetamodel aomTenantMetamodelObj;
			aomTenantMetamodelObj = objMapper.readValue(aomTenantMetamodel, AOMTenantMetamodel.class);
	        return aomTenantMetamodelObj;
	    } catch (IOException e) {
	    	// we are in trouble ; Unable to load  metamodel
	        e.printStackTrace();
	        throw e;
	    } 	
	}

	public void setAomTenantMetamodelObj(AOMTenantMetamodel aomTenantMetamodelObj) throws IOException {
		ObjectMapper objMapper = new ObjectMapper();
		try {
			aomTenantMetamodel = objMapper.writeValueAsString(aomTenantMetamodelObj);
	    } catch (IOException e) {
	        e.printStackTrace();
	        throw e;
	    } 
	}
	
}
