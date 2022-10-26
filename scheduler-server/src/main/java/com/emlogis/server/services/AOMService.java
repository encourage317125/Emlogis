package com.emlogis.server.services;

import com.emlogis.model.aom.*;

import java.io.IOException;
import java.util.Map;


public interface AOMService {
	
	public AOMTenantMetamodel createAOMTenantMetamodel(String tenantId, String createdBy, String ownedBy) throws IOException;
	public AOMTenantMetamodel getAOMTenantMetamodel(String tenantId);
	public void deleteAOMTenantMetamodel(String tenantId) throws IOException;
	
	public AOMRelationshipDef addAOMRelationshipDef(String tenantId, AOMRelationshipDef def, String updatedBy) throws IOException;
	public AOMRelationshipDef getAOMRelationshipDef(String tenantId, String relationshipType);
	public Map<String,AOMRelationshipDef> getAOMRelationshipDefs(String tenantId);
	public void deleteAOMRelationshipDef(String tenantId, String relationshipType, String updatedBy) throws IOException;
	
	public AOMEntityDef addAOMEntityDef(String tenantId, AOMEntityDef def, String updatedBy) throws IOException;
	public AOMEntityDef getAOMEntityDef(String tenantId, String aomEntityType);
	public Map<String,AOMEntityDef> getAOMEntityDefs(String tenantId);
	public void deleteAOMEntityDef(String tenantId, String aomEntityType, String updatedBy) throws IOException;
	
	public AOMTypeDef addAOMTypeDef(String tenantId, String aomEntityType, AOMTypeDef def, String updatedBy) throws IOException;
	public AOMTypeDef getAOMTypeDef(String tenantId, String aomEntityType, String aomType);
	public Map<String,AOMTypeDef> getAOMTypeDefs(String tenantId, String aomEntityType);
	public void deleteAOMTypeDef(String tenantId, String aomEntityType, String aomType, String updatedBy) throws IOException;
	
	public AOMPropertyDef addAOMPropertyDef(String tenantId, String aomEntityType, AOMPropertyDef def, String updatedBy) throws IOException;
	public AOMPropertyDef getAOMTypeDef(String tenantId, String aomEntityType, String aomType, String propertyName);
	public Map<String,AOMPropertyDef> getAOMTypeDefs(String tenantId, String aomEntityType, String aomType);
	public void deleteAOMPropertyDef(String tenantId, String aomEntityType, String aomType, String propertyName, String updatedBy) throws IOException;

}