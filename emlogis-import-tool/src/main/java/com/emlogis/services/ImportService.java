package com.emlogis.services;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.emlogis.common.Constants;
import com.emlogis.model.imports.ImportLog;
import com.emlogis.model.imports.ImportOrganizationConfig;

@Singleton
public class ImportService {
	private final static Logger logger = Logger.getLogger(ImportService.class);
	
	@PersistenceContext(unitName=Constants.EMLOGIS_IMPORT_UNIT_NAME)
    private EntityManager entityManager;
		
	@EJB 
	FileService fileService;
	
	public void serviceTenantImport(ImportOrganizationConfig orgConfig) {
		
		String tenant = orgConfig.getTenantId();
		byte[] importfile = null;
		
		logger.debug("Import service for tenant: " + tenant);
		importfile = fileService.getImportFile(orgConfig);
		
		if(importfile != null) {
			ImportLog importLog = new ImportLog();
			
		}
		
	}
	
	public void saveOrgConfiguration(ImportLog importLog) {
		entityManager.persist(importLog);
	}
	
	public void updateOrgConfiguration(ImportLog importLog) {
		entityManager.merge(importLog);
	}	

}
