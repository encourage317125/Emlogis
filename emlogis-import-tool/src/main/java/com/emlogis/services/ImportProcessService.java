package com.emlogis.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.apache.log4j.Logger;

import com.emlogis.model.imports.ImportConfiguration;
import com.emlogis.model.imports.ImportMapping;
import static com.emlogis.model.imports.ImportMapping.*;
import com.emlogis.model.imports.ImportOrganizationConfig;

import static com.emlogis.model.imports.ImportOrganizationConfig.*;


@Singleton
public class ImportProcessService {
	private final static Logger logger = Logger.getLogger(ImportProcessService.class);
	
	@Resource
	TimerService timerService;
	
	@EJB
	ConfigurationService configurationService;
	
	@EJB
	ImportService importService;
	
	private Map<String, AtomicBoolean> busyMap = new HashMap<String, AtomicBoolean>();
	Map<String, ImportMapping> orgMappingMap = new HashMap<String, ImportMapping>();
	
	public void startImportService(List<ImportMapping> orgMappingList) {
		logger.debug("Starting Process Service");
		
		// Setup Org Map and Busy map
		
		AtomicBoolean busyBoolean = null;
		
		for(ImportMapping importMap: orgMappingList) {
			String busyId = importMap.getId();
			
			busyBoolean = new AtomicBoolean();			
			busyMap.put(busyId, busyBoolean);
			
			orgMappingMap.put(busyId, importMap);
		}
		
		Map<String,String> scheduleMap = null;
		ScheduleExpression scheduleExpression = null;
		TimerConfig timerConfig = null;
		
		// Start Import timers for each tenant
		
		for(String mapID : orgMappingMap.keySet()) {
			ImportMapping importMapping = orgMappingMap.get(mapID);
			
			scheduleMap = importMapping.getSchedule();
			scheduleExpression = new ScheduleExpression();
			scheduleExpression.dayOfWeek(scheduleMap.get(DAY_OF_WEEK));
			scheduleExpression.hour(scheduleMap.get(HOUR_OF_DAY));
			scheduleExpression.minute(scheduleMap.get(MINUTE));
			
			timerConfig = new TimerConfig(mapID, false);
			timerService.createCalendarTimer(scheduleExpression, timerConfig);
			
			//TODO: Set next import time
		}
	}
	
	@Timeout
	@Lock(LockType.READ)
	public void serviceImports(Timer timer) {
		
		String mapId = (String) timer.getInfo();		
		AtomicBoolean busy = busyMap.get(mapId);
		
		// Don't start an import for a tenant if one is already running for that tenant
		
		if (!busy.compareAndSet(false, true)) {
            return;
        }
		try {
			logger.debug("Begin Servicing Import - mapId: " + mapId);
//			
//			ImportOrganizationConfig orgConfig = orgMappingMap.get(mapId);
//			importService.serviceTenantImport(orgConfig);
//		
//			logger.debug("Complete Servicing Import - tenant: "+ tenant);
		} finally {
			busy.set(false);
		}			
	}

}
