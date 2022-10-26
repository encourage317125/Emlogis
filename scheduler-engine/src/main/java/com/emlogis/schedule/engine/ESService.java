package com.emlogis.schedule.engine;

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

import com.fasterxml.jackson.databind.ObjectMapper;

public class ESService  {
	
	private static final String ELASTIC_SEARCH_ADDRESS 	= "elasticsearch.address";
	private static final String ELASTIC_SEARCH_PORT 	= "elasticsearch.port";
	public static final  String ESLOGGINGACTIVATION 	= "LogExecution";	// System property name to activate ES logging
	private static final String OPT_ENVIRONMENT_NAME 	= "envName";

	private final Logger logger = LoggerFactory.getLogger(ESService.class);
	
	private Client esclient;		// elastic search client
	private	String	envName = "local";		// environment name (local, qa, prod, uat, etc)

	public ESService() {
		super();
	}

	void init(){
		
		String env = System.getProperty(OPT_ENVIRONMENT_NAME);
		if (!StringUtils.isEmpty(env)) {
			envName = env;
		}

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
    	logger.debug("ES Engine Client created:"+address+":"+port);
	}


	/* (non-Javadoc)
	 * 
	 */
	public void indexEngineRecord(String tenantId, EngineRecord record) {
		// Note: This method MUST NOT fail, otherwise it might impact/kill the engine
		if (record == null) {
			return;
		}
        String text = null;
        try {
            String docId = record.getRequestId();
            if (record.getTimestamp() == 0) {
                record.setTimestamp(System.currentTimeMillis());
            }
            record.setEnvName(envName);
            // format timestamp to ES liking  ex: 2014-04-30T19:58:32.148
            DateTime dateTime = new DateTime(record.getTimestamp());
            DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecondMillis().withZoneUTC();
            String timestamp = formatter.print(dateTime);
            record.set_timestamp(timestamp);
            ObjectMapper objMapper = new ObjectMapper();
			text = objMapper.writeValueAsString(record);
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.yearMonthDay();
			String date = dateTimeFormatter.print(dateTime).replaceAll("-", ".");
			// build a daily rollup simple index name 
			String regularIdxName = envName + ".engine-" + date;
			logger.debug(regularIdxName + ": --> Indexing Engine record: " + text);
			
			// note that ES index names MUST be lowercase
            BulkRequestBuilder bulkRequest = esclient.prepareBulk();
            bulkRequest.add(
                    esclient.prepareIndex(regularIdxName, "allrequesttypes" /*StringUtils.lowerCase(record.getRequestType())*/, docId)
                            .setSource(text)
                            .setTimestamp(timestamp));
            bulkRequest.setReplicationType(ReplicationType.ASYNC).execute().actionGet();
		} catch (Throwable t) {
			logger.error("Exception while saving Engine Record: " + text, t);
		}
    }
}
