package com.emlogis.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.Logger;
import org.elasticsearch.common.inject.internal.UniqueAnnotations;
import org.joda.time.DateTime;

import com.emlogis.model.imports.ImportConfiguration;
import com.emlogis.model.imports.ImportMapping;
import com.emlogis.model.imports.ImportOrganizationConfig;

@Startup
@Singleton

public class StartupService {
	private final static Logger logger = Logger.getLogger(StartupService.class);
	
	@EJB
	ConfigurationService configurationService;
	
	@EJB
	ImportProcessService processService;
	
	@EJB
	FileService fileService;
	
	@PostConstruct
    void init() {
		logger.debug("Starting Startup Service");
		
		ImportConfiguration importConfiguration = null;
		
		importConfiguration = configurationService.getConfiguration();
		
		if(importConfiguration == null) {
			importConfiguration = new ImportConfiguration("*/5", "sftp.emlogis.net", "egs",
					"29E646D0BF254A0461A01808456E1DD9", "ESPROD_SERVICES/EmployeeSync", "xsjqcOzhNi4w7xktI4GtR5Fmu+LFBf247UuYxC+PhPU=",
					"sjytvBYg+wEHZB4AHY+1rkYgjRXcVMRVgNEFdw0Rv0qBmE0wQVFhbEpfaeOLdwKy");
			configurationService.saveConfiguration(importConfiguration);
		}
		
		fileService.startFileService(importConfiguration);
		
		List<ImportOrganizationConfig> orgConfigList = null;
		
		List<ImportMapping> orgMappingList = null;
		
		orgConfigList = configurationService.getOrgConfigs();
		
		if(orgConfigList == null || orgConfigList.size() == 0) {
			
			orgConfigList = new ArrayList<ImportOrganizationConfig>();
			
			ImportOrganizationConfig importOrgConfig = new ImportOrganizationConfig("tmp", new DateTime(),
					"", LocationType.SFTP, "dev.mercury.emlogis.com/dropbox","");
			
			orgConfigList.add(importOrgConfig);
			
			configurationService.saveOrgConfiguration(importOrgConfig);
			
			importOrgConfig = new ImportOrganizationConfig("lcso", new DateTime(),
					"", LocationType.SFTP, "dev1.mercury.emlogis.com/dropbox", "");
			
			orgConfigList.add(importOrgConfig);
			
			configurationService.saveOrgConfiguration(importOrgConfig);
			
			importOrgConfig = new ImportOrganizationConfig("rocko", new DateTime(),
					"", LocationType.S3, "dev3.mercury.emlogis.com/dropbox", "");
			
			orgConfigList.add(importOrgConfig);
			
			configurationService.saveOrgConfiguration(importOrgConfig);
		}
		
	//	processService.startImportService(orgMappingList);
	}
}
