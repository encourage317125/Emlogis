package com.emlogis.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.emlogis.common.Constants;
import com.emlogis.model.imports.ImportConfiguration;
import com.emlogis.model.imports.ImportOrganizationConfig;

@Singleton
public class ConfigurationService {
	
	private final static Logger logger = Logger.getLogger(ConfigurationService.class);
	
	@PersistenceContext(unitName=Constants.EMLOGIS_IMPORT_UNIT_NAME)
    private EntityManager entityManager;
	
	private ImportConfiguration importConfiguration = null;
	
	private List<ImportOrganizationConfig> orgConfigList = null;
	
	private void loadConfiguration() {	
		String configurationQuery = "Select ic from ImportConfiguration ic";
		
		try {
			importConfiguration = entityManager.createQuery(configurationQuery, ImportConfiguration.class).getSingleResult();
		} catch (Exception e) {
			logger.warn("No import config data yet");
		}
	}
	
	public ImportConfiguration getConfiguration() {
		if(importConfiguration == null) {
			loadConfiguration();
		}
		return importConfiguration;
	}
	
	public void saveConfiguration(ImportConfiguration configuration) {
		entityManager.persist(configuration);
	}
	
	public void updateConfiguration(ImportConfiguration configuration) {
		entityManager.merge(configuration);
	}
	
	public List<ImportOrganizationConfig> getOrgConfigs() {
		if(orgConfigList == null || orgConfigList.size() == 0) {
			loadOrgConfigs();
		}
		return orgConfigList;
	}
	
	private void loadOrgConfigs() {
		
		String orgConfigQuery = "Select ioc from ImportOrganizationConfig ioc";
		
		try {
			orgConfigList = entityManager.createQuery(orgConfigQuery, ImportOrganizationConfig.class).getResultList();
		} catch (Exception e) {
			logger.warn("No import org config data yet");
		} 
	}
	
	public void saveOrgConfiguration(ImportOrganizationConfig configuration) {
		entityManager.persist(configuration);
	}
	
	public void updateOrgConfiguration(ImportOrganizationConfig configuration) {
		entityManager.merge(configuration);
	}

}
