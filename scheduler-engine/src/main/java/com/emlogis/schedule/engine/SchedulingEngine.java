package com.emlogis.schedule.engine;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.emlogis.engine.domain.communication.EngineEventService;
import com.emlogis.engine.domain.communication.NotificationService;
import com.emlogis.scheduler.engine.communication.ComponentRole;
import com.emlogis.shared.services.eventservice.EventService;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import com.emlogis.util.graphite.GraphiteSender;
import com.emlogis.util.graphite.GraphiteTCP;

public class SchedulingEngine {

	private final static Logger logger = Logger.getLogger(SchedulingEngine.class.getSimpleName());
	private static String GRAPHITE_HOST = "graphiteHost";
	private static String GRAPHITE_PORT = "graphitePort";


	private static boolean keepRunning = true;

	public static void main(String[] args) throws InterruptedException {
		String filename = System.getProperty("log4j.configuration");
		
		if(null!=filename && !filename.trim().equals("")) {
			File file = new File(filename);
			if(!file.exists())  {
				logger.error("log4j properties file not found !" + file.getAbsolutePath());
			} else {
				PropertyConfigurator.configure(file.getAbsolutePath());
			}
		}
		logger.info("log4j properties file :" +filename);
		while (keepRunning) {
			runEngine();
		}

		System.exit(0);
	}

	private static void runEngine() throws InterruptedException {
		try {
			// initialize Hazelcast Service (engine side)
			HazelcastService hzService = new EngineHazelcastService();
			hzService.init();

			// initialize Event Service (engine side)
			EventService eventService = new EngineEventService();
			eventService.init(hzService, ComponentRole.Engine);

			// initialize Notification Service
			NotificationService notificationService = new NotificationService(eventService);

			// initialize ElasticSearch Service if specified
			ESService esService = null;
			String esProp = System.getProperty(ESService.ESLOGGINGACTIVATION);
			if (StringUtils.equals(esProp, "true")) {
				esService= new ESService();
				esService.init();
			}

			// Open Graphite connection if specified
			String 	graphiteHost =  System.getProperty(GRAPHITE_HOST);
			String 	graphitePortStr =  System.getProperty(GRAPHITE_PORT);
			int graphitePort = 2003;
			GraphiteSender graphiteSender = null;
			if (!StringUtils.isEmpty(graphitePortStr)) {
				try {
					graphitePort = Integer.parseInt(graphitePortStr);			
				}
				catch (Exception e) {
					System.out.println("Unable to parse the " + GRAPHITE_PORT + "=" +  graphitePortStr + ", value. must be an integer.");
					logger.error("Error while initiating graphite client.", e);
					graphiteHost = null;
				}
			}
			if (!StringUtils.isEmpty(graphiteHost)) {
				// open Graphite connection
				try {
					graphiteSender = new GraphiteTCP(graphiteHost, graphitePort);
					graphiteSender.connect();
				}
				catch (Exception e) {
					System.out.println("Failed to connect to Graphite:" + graphiteHost + ":" + graphitePort);
					logger.error("Error while connecting to Graphite.", e);
					e.printStackTrace();
					graphiteHost = null;
					graphiteSender = null;
				}
			}
			if (StringUtils.isEmpty(graphiteHost)) {
				System.out.println("********* SENDING OF METRICS TO GRAPHITE IS DISABLED *********");		
				logger.warn("********* SENDING OF METRICS TO GRAPHITE IS DISABLED *********");
			}


			// loop on processing requests
			RequestHandlerService handlerService = new RequestHandlerService(hzService, notificationService, esService, graphiteSender);
			handlerService.start();
		} catch (Throwable t) {
			t.printStackTrace();

			// wait 10 sec and run again
			Thread.sleep(10 * 1000);
		}
	}

}
