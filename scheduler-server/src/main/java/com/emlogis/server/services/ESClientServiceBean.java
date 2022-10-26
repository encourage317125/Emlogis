package com.emlogis.server.services;

import com.emlogis.common.Constants;
import com.emlogis.common.UniqueId;
import com.emlogis.model.notification.Notification;
import com.emlogis.model.notification.NotificationLogEntry;
import com.emlogis.rest.auditing.AuditRecord;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.support.replication.ReplicationType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ESClientServiceBean implements ESClientService  {
	
	public static final String ELASTIC_SEARCH_ENABLE = "elasticsearch.enable";	// System property name to activate ES logging
	private static final String ELASTIC_SEARCH_ADDRESS = "elasticsearch.address";
	private static final String ELASTIC_SEARCH_PORT = "elasticsearch.port";
	private static final String OPT_ENVIRONMENT_NAME = "envName";

	private final Logger logger = LoggerFactory.getLogger(ESClientServiceBean.class);

    //private final Logger logger = Logger.getLogger(ESClientServiceBean.class);
	
	private boolean esEnabled = false;		// must be true to enable ElasticSearch
	private Client esclient;				// elastic search client
	
	private	String	envName = "local";		// environment name (local, qa, prod, uat, etc)
	
	
	// map of class names of output params to be filtered out
	// this is used to avoid loggin into ES output params that are too large and would both impact performances and usability of the audit log  
	private static Set<String> filteredOutputClassMap;


	public ESClientServiceBean() {
		super();
	}

	@PostConstruct
	void init(){
		logger.debug("ESClientService - Created");
		
		String env = System.getProperty(OPT_ENVIRONMENT_NAME);
		if (!StringUtils.isEmpty(env)) {
			envName = env;
		}
		String esProp = System.getProperty(ELASTIC_SEARCH_ENABLE);
		esEnabled = StringUtils.equals(esProp, "true");
	}

	@Override
	public Client getESClient() {
		
		logger.debug("ESClientServiceBean.getESClient()");
		if (esclient == null && esEnabled) {
			// init Elastic Search client
			String address = System.getProperty(ELASTIC_SEARCH_ADDRESS);
			if (StringUtils.isEmpty(address)) {
				address="localhost";
			}
			String port = System.getProperty(ELASTIC_SEARCH_PORT);
			if (StringUtils.isEmpty(port)) {
				port="9300";
			}
	    	esclient = new TransportClient()
	    		.addTransportAddress(new InetSocketTransportAddress(address, Integer.parseInt(port)));
	    	logger.debug("Client created");
		}
		return esclient;
	}

	/* (non-Javadoc)
	 * 
	 * @see com.emlogis.server.services.ESClientService#indexAuditRecord(java.lang.String, java.lang.Object)
	 */
	@Override
	public void indexAuditRecord(String tenantId, AuditRecord fullRecord) {
		// Note: This method MUST NOT fail, otherwise it would impact the application
		if (!esEnabled || fullRecord == null) {
			return;
		}
        String recordAsText = null;
        String fullRecordAsText = null;
        try {
            // TODO implement a better Document Id generator
            String docId = UniqueId.getId();
            fullRecord.setId(docId);
            if (fullRecord.getTimestamp() == 0) {
                fullRecord.setTimestamp(System.currentTimeMillis());
            }
            limitOutputParamSize(fullRecord);
            
            // format timestamp to ES liking  ex: 2014-04-30T19:58:32.148
            DateTime dateTime = new DateTime(fullRecord.getTimestamp());
            DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecondMillis().withZoneUTC();
            String timestamp = formatter.print(dateTime);
            fullRecord.set_timestamp(timestamp);   
            fullRecord.setEnvName(envName);
            
            // creates a smaller record with no input/output/stacktrace fields
            AuditRecord record = fullRecord.clone();
            record.setId(UniqueId.getId());
            record.clearInputParams();
            record.clearOutputParams();
            record.setStackTrace(null);
            
            ObjectMapper objMapper = new ObjectMapper();
			recordAsText = objMapper.writeValueAsString(record);
			fullRecordAsText = objMapper.writeValueAsString(fullRecord);
			
			// build a daily rollup index name 
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.yearMonthDay();
			String date = dateTimeFormatter.print(dateTime).replaceAll("-", ".");
			String regularIdxName =  envName + ".api-" + date;
			String fullIdxName = envName + ".apifull-" + date;
			logger.debug(regularIdxName + ", " + fullIdxName + ": --> Indexing audit record: " + recordAsText);
			// note that ES index names MUST be lowercase

            BulkRequestBuilder bulkRequest = getESClient().prepareBulk();
            bulkRequest.add(
                    getESClient().prepareIndex(regularIdxName, "audit", docId)
                            .setSource(recordAsText)
                            .setTimestamp(timestamp));
            bulkRequest.add(
                    getESClient().prepareIndex(fullIdxName, "audit", docId)
                            .setSource(fullRecordAsText)
                            .setTimestamp(timestamp));
            bulkRequest.setReplicationType(ReplicationType.ASYNC).execute().actionGet();
		} catch (Throwable t) {
			logger.error("Exception while saving Audit Record: " + recordAsText, t);
		}
    }
	
	public void indexNotification(Notification notification, String category) {
		
		// Check that ES notification logging is turned on
		if (!esEnabled || notification == null) {
			return;
		}
		String notificationLogging = System.getProperty(Constants.LOG_NOTIFICATIONS);
		if(StringUtils.isBlank(notificationLogging) || notificationLogging.equalsIgnoreCase("false")) {
			return;
		}
		
		
		String text = null;
		NotificationLogEntry logEntry = new NotificationLogEntry();
		
		logEntry.setNotification(notification);
		logEntry.setCategory(category);
		
        try {
            // TODO implement a better Document Id generator
            String docId = UniqueId.getId();
            logEntry.setId(docId);
            
            DateTime dateTime = new DateTime(System.currentTimeMillis());
            DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecondMillis().withZoneUTC();
            String timestamp = formatter.print(dateTime);
            
            ObjectMapper objMapper = new ObjectMapper();
			text = objMapper.writeValueAsString(logEntry);
			
			// build a daily rollup index name derived from tenant id
			String tenantId = notification.getTenantId();
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.yearMonthDay();
			String date = dateTimeFormatter.print(dateTime).replaceAll("-", ".");
			String regularIdxName = /*StringUtils.lowerCase(tenantId) + "-*/ envName + ".notifications-" + date;			
//			String logstashIdxName = "logstash-notifications-" + date;		
//			logger.debug(regularIdxName + ", " + logstashIdxName + ": --> Indexing notification record: " + text);
			//note that ES index names MUST be lowercase

            BulkRequestBuilder bulkRequest = getESClient().prepareBulk();
            bulkRequest.add(
                    getESClient().prepareIndex(regularIdxName, "notifications", docId)
                            .setSource(text)
                            .setTimestamp(timestamp));
            /* we do not need to long into logstash  index anymore since kibana offers same UI capabilities on non logstash indexes 
            bulkRequest.add(
                    getESClient().prepareIndex(logstashIdxName, "notification", docId)
                            .setSource(text)
                            .setTimestamp(timestamp));
            */
            bulkRequest.setReplicationType(ReplicationType.ASYNC).execute().actionGet();
		} catch (Throwable t) {
			logger.error("Exception while saving Notification Record: " + text, t);
		}
		
		
	}

	/**
	 * limitOutputParamSize truncates String type params which size exceeds a limit, so as to avoid sending way too large objects to ES
	 * Test is done on output param of class name referenced in filteredOutputClassMap, or on param length if param of type String 
	 * @param record
	 */
	private void limitOutputParamSize(AuditRecord record) {
		if (filteredOutputClassMap == null) {
			// TODO
			// adding hard coded class names is a bit of a hack but is done intentionally for now to avoid depedencies
			// until a clean solution is identified
			filteredOutputClassMap = new HashSet();
			filteredOutputClassMap.add("ScheduleReportDto");
		}
		
		Map<String,Object> outputMap = record.getOutputParams();
		if (outputMap != null) {
			for (String key : outputMap.keySet()) {
				Object val = outputMap.get(key);
				if (val != null) {
					if (val instanceof String) {
						String param = (String)val;
						int limit = 1024 * 10;
						if (param.length() > limit) {
							val = param.substring(0, limit) + "... (truncated because content too large).";
							outputMap.put(key, val);					
						}
					}
					else if (filteredOutputClassMap.contains(val.getClass().getSimpleName())) {
						val = "instance of '" + val.getClass().getSimpleName() +  "' suppressed because content too large.";
						outputMap.put(key, val);					
					}
				}
			}
		}
	}
}
