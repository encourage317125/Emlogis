package com.emlogis.log4j.appenders;


import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;


public class ESClientAppender extends AppenderSkeleton {

	private ExecutorService threadPool = Executors.newSingleThreadExecutor();
	private String applicationName = "application-default";
	private String hostName = "127.0.0.1";
	private String elasticIndex = "logstash-default";
	private String elasticType = "logs-default";
	private int elasticPort = 9999;

	private Client esclient;		// elastic search client

	/**
	 * Submits LoggingEvent for insert the document if it reaches severity
	 * threshold.
	 *
	 * @param loggingEvent
	 */
	@Override
	protected void append(LoggingEvent loggingEvent) {
		if (isAsSevereAsThreshold(loggingEvent.getLevel())) {
			threadPool.submit(new AppenderTask(loggingEvent));
		}
	}

	public Client getESClient() {
		if ( esclient == null) {
			// init Elastic Search client
			esclient = new TransportClient( /*settings*/)
			.addTransportAddress(new InetSocketTransportAddress(hostName, elasticPort));
		}
		return esclient;
	}
	
	public int getElasticPort() {
		return elasticPort;
	}

	public void setElasticPort(int elasticPort) {
		this.elasticPort = elasticPort;
	}

	/**
	 * Elastic Search index.
	 *
	 * @return
	 */
	public String getElasticIndex() {
		return elasticIndex;
	}

	/**
	 * Elastic Search index.
	 *
	 * @param elasticIndex
	 */
	public void setElasticIndex(String elasticIndex) {
		this.elasticIndex = elasticIndex;
	}

	/**
	 * Elastic Search type.
	 *
	 * @return Type
	 */
	public String getElasticType() {
		return elasticType;
	}

	/**
	 * Elastic Search type.
	 *
	 * @param elasticType
	 */
	public void setElasticType(String elasticType) {
		this.elasticType = elasticType;
	}

	/**
	 * Name application using log4j.
	 *
	 * @return
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * Name application using log4j.
	 *
	 * @param applicationId
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Host name application run.
	 *
	 * @return
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Host name application run.
	 *
	 * @param ip
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Close Elastic Search client.
	 */
	@Override
	public void close() {
		esclient.close();
	}

	/**
	 * Ensures that a Layout property is not required
	 *
	 * @return
	 */
	@Override
	public boolean requiresLayout() {
		return true;
	}

	/**
	 * Simple Callable class that insert the document into ElasticSearch
	 */
	class AppenderTask implements Callable<LoggingEvent> {

		LoggingEvent loggingEvent;

		AppenderTask(LoggingEvent loggingEvent) {
			this.loggingEvent = loggingEvent;
		}

		/**
		 * Method is called by ExecutorService and insert the document into
		 * ElasticSearch
		 *
		 * @return
		 * @throws Exception
		 */
		@Override
		public LoggingEvent call() throws Exception {
			try {
				IndexResponse response = getESClient()
						.prepareIndex(
								getElasticIndex() 
								+ "-" + ISO_DATE_FORMAT.format(loggingEvent.getTimeStamp())
								,getElasticType(), UUID.randomUUID().toString())
						.setSource(layout.format(loggingEvent))
						.setTimestamp(Long.toString(loggingEvent.getTimeStamp()))
						.execute()
						.actionGet();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return loggingEvent;
		}
	}
	
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	public static final FastDateFormat ISO_DATE_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd", UTC);
	
}
