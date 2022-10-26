package com.emlogis.monitoring;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.scheduler.engine.communication.AppServerStatus;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.shared.services.hazelcastservice.HazelcastInstanceBuilder;
import com.emlogis.util.graphite.GraphiteSender;
import com.emlogis.util.graphite.GraphiteTCP;
import com.emlogis.utils.cmdline.CmdLineUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;

public class HazelcastMonitoring {
	
	class Metric {
		private String	name;
		private long	value;
		private long	timestamp;		
		
		public Metric(String name, long value, long timestamp) {
			super();
			this.name = name;
			this.value = value;
			this.timestamp = timestamp;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getValue() {
			return value;
		}

		public void setValue(long value) {
			this.value = value;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}	
	}
	
	
	
	static final String UTIL_NAME 				= "HazelcastMonitoring";		
	static final String UTIL_VERSION 			= "v0.2";		
	static final String OPT_VERBOSE 			= "-verbose:";		

	static final String OPT_ENVIRONMENT_NAME 	= "-envName:";		
	static final String OPT_POLLINGFREQUENCY_SEC 	= "-pollingFrequencySec:";		// -pollingFrequencySec:nn  Polling rate in sec 
	static final String OPT_HAZELCAST_HOST 		= "-hazelcastHost:";		
	static final String OPT_HAZELCAST_PORT 		= "-hazelcastPort:";	
		
	static final String OPT_GRAPHITE_HOST 		= "-graphiteHost:";		
	static final String OPT_GRAPHITE_PORT 		= "-graphitePort:";				

	
	// config options & default values
	private String 	envName = "local";
	private long 	pollingFrequencySec = 5;		// 5s 
	
	private String 	hazelcastHost = "localhost";
	private int 	hazelcastPort =  9042;
	
	private String 	graphiteHost = "localhost";
	private int 	graphitePort =  2003;
	
	private boolean verbose = false;
	
    private HazelcastInstance hazelcastInstance;
	private GraphiteSender graphiteSender;

	
	
	public static void main(String[] args) {
		
		HazelcastMonitoring monitoringUtility = new HazelcastMonitoring();
		monitoringUtility.init(args);
		monitoringUtility.run();
	}
	
	public HazelcastInstance getHazelcastInstance() {
    	if (hazelcastInstance == null) {
        	// connect to Hazelcast
    		// TODO specify which Hz instance to connect to
            hazelcastInstance = HazelcastInstanceBuilder.newHazelcastClient();
    	}
    	return hazelcastInstance;
    }

	
	private void init(String[] args) {
		Set<String> options = new HashSet();
		for (String option: args) { options.add(option); }
		
		try {
			
			envName = CmdLineUtils.getStringOptionWithDefault(options, OPT_ENVIRONMENT_NAME, envName);
			pollingFrequencySec = (long)CmdLineUtils.getIntOptionWithDefault(options, OPT_POLLINGFREQUENCY_SEC, (int)pollingFrequencySec);
			
			hazelcastHost = CmdLineUtils.getStringOptionWithDefault(options, OPT_HAZELCAST_HOST, hazelcastHost);
			hazelcastPort = CmdLineUtils.getIntOptionWithDefault(options, OPT_HAZELCAST_PORT, hazelcastPort);
			
			graphiteHost = CmdLineUtils.getStringOptionWithDefault(options, OPT_GRAPHITE_HOST, graphiteHost);
			graphitePort = CmdLineUtils.getIntOptionWithDefault(options, OPT_GRAPHITE_PORT, graphitePort);

			String s = 	CmdLineUtils.getStringOptionWithDefault(options, OPT_VERBOSE, "false");
			verbose = StringUtils.equals(s, "true");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing options: " + e.getMessage());
			usage();
			System.exit(-1);
		}
		if (options.size() > 0 ) {
			String s = "";
			for (String option : options) { s+= (option +  " "); }
			System.out.println("Unknown  options: " + s + ", ignoring ...");
			usage();			
		}
				
		// TODO printout options ....
		System.out.println("Executing " + UTIL_NAME  + " " + UTIL_VERSION + " with options: ");

		System.out.println(OPT_ENVIRONMENT_NAME + " " + envName);
		System.out.println(OPT_POLLINGFREQUENCY_SEC + " " + pollingFrequencySec + "ms");
		
		System.out.println(OPT_HAZELCAST_HOST + " " + hazelcastHost);
		System.out.println(OPT_HAZELCAST_PORT + " "  + hazelcastPort);
		
		System.out.println(OPT_GRAPHITE_HOST + " " + graphiteHost);
		System.out.println(OPT_GRAPHITE_PORT + " "  + graphitePort);
		
		// open Graphite connection
		// TODO, put this into a method so that we can retry it from time to time on failure.
		try {
			graphiteSender = new GraphiteTCP(graphiteHost, graphitePort);
			graphiteSender.connect();
		}
		catch (Exception e) {
			System.out.println("Failed to connect to Graphite:");
			e.printStackTrace();
			System.out.println("************* WRITING TO Graphite WILL BE SKIPPED *** Data Will be printed out to Console *************");
			graphiteSender = null;
		}

		// open Hazelcast connection
		// TODO, put this into a method so that we can retry it in case of intermitent failure.
		try {
			HazelcastInstance hz =  getHazelcastInstance();
			if (hz == null) {
				System.out.println("************* Unable to connect to Hazelcast, EXITING ! *************");
				System.exit(-1);				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("************* Unable to connect to Hazelcast, EXITING ! *************");
			System.exit(-1);
		}
		
//		System.out.println("TEST: ************* WRITING TO Graphite WILL BE SKIPPED *** Data Will be printed out to Console **********");
//		graphiteSender = null;

	}


	private void usage() {
		System.out.println("Usage: TODO ...");
		
		String name = UTIL_NAME;
		System.out.println("Usage: " + name  + " " + UTIL_VERSION + " "
				+ " -envName:<name of environment (local, demo, qa, prod, emlogis,..> = prefix of metric names"
				+ " -pollingFrequencySec:<nb of secs between measurements>"
				+ " -hazelcastHost:<hazelcast IP or Hostname> -hazelcastPort:<hazelcast listen port>"
				+ " -graphiteHost:<graphite IP or Hostname> -graphitePort:<graphite listen port>"
		);
		System.out.println("Ex   : " + name + " -envName:demo -pollingFrequencySec:10 -hazelcastHost:localhost -hazelcastPort:9042 (default) -graphiteHost:localhost -graphitePort:2003 (default)");
	}
	
	protected void run() {
	
        //get map of component status
		if (getHazelcastInstance() == null) {
			System.out.println("Unable to connect to Hazelcast Instance, EXITING !!!");
			System.exit(0);
		}
	    IMap<String, String> appServerMap = getHazelcastInstance().getMap(HzConstants.APP_SERVER_MAP);
	    IMap<String, String> engineMap = getHazelcastInstance().getMap(HzConstants.ENGINE_MAP);
	    
		IQueue<String> 			globalRequestQueue = getHazelcastInstance().getQueue(HzConstants.REQUEST_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX);
		IMap<String, String>	requestDataMap = getHazelcastInstance().getMap(HzConstants.REQUEST_DATA_MAP);
        IQueue<String> 			globalResponseQueue  = getHazelcastInstance().getQueue(HzConstants.RESPONSE_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX);
		IMap<String, String>	responseDataMap = getHazelcastInstance().getMap(HzConstants.RESPONSE_DATA_MAP);
        IMap<String, Object> 	abortMap = getHazelcastInstance().getMap(HzConstants.ABORT_MAP);
        ITopic<byte[]> 			globalEventQueue = getHazelcastInstance().getTopic(HzConstants.GLOBAL_EVENT_TOPIC);
        IMap<Serializable, byte[]> activeSessionsMap = getHazelcastInstance().getMap(HzConstants.SESSION_MAP);

	    boolean exit = false;		// infinite loop ... for now
	    StopWatch watch = new StopWatch();
	    watch.start();
		try {
			String metricPrefix = envName + ".hazelcast.";
			long start = System.currentTimeMillis();
		    while (!exit) {
				long now = System.currentTimeMillis();
				
				List<Metric> metrics = new ArrayList();
				
				int appServerCount = appServerMap.size();
				metrics.add(new Metric(metricPrefix + "appServerCount", appServerCount, now));
				int engineCount = engineMap.size();
				metrics.add(new Metric(metricPrefix + "engineCount", engineCount, now));

				int globalRequestQueueSize = globalRequestQueue.size();		
				metrics.add(new Metric(metricPrefix + "globalRequestQueueSize", globalRequestQueueSize, now));

				int requestDataMapSize = requestDataMap.size();		
				metrics.add(new Metric(metricPrefix + "requestDataMapSize", requestDataMapSize, now));

				int globalResponseQueueSize = globalResponseQueue.size();	
				metrics.add(new Metric(metricPrefix + "globalResponseQueueSize", globalResponseQueueSize, now));

				int responseDataMapSize = responseDataMap.size();		
				metrics.add(new Metric(metricPrefix + "responseDataMapSize", responseDataMapSize, now));

				int abortMapSize = abortMap.size();		
				metrics.add(new Metric(metricPrefix + "abortMapSize", abortMapSize, now));

				int activeSessionsMapSize = activeSessionsMap.size();	
				metrics.add(new Metric(metricPrefix + "activeSessionsCount", activeSessionsMapSize, now));

//				int eventQueueSize = eventQueue..size();	 // this item has no real size ....	
				
				
				// get list of active AppServers to monitor their response queue sizes 
				for (String  key : appServerMap.keySet()) {
					AppServerStatus appServerStatus = EmlogisUtils.fromJsonString(appServerMap.get(key), AppServerStatus.class);
					String appServerName = appServerStatus.getName();
					String appServerQueueName  =  HzConstants.RESPONSE_QUEUE_PREFIX + appServerName;
					int queueSize = getHazelcastInstance().getQueue(appServerQueueName).size();
					metrics.add(new Metric(metricPrefix + "appServer." + appServerName + ".responseQueueSize", queueSize, now));
				}
				
				// send to console
				StringBuffer sb = new StringBuffer();
				sb.append(new Date().toString() + "\n");
				boolean dumpToConsole = false;
				// send data to graphite if live
				for (Metric m : metrics) {
					if (graphiteSender != null) {
						sendMetricToGraphite(graphiteSender, m);
					}
					if (graphiteSender == null || verbose) {
						// send to console if graphite down Or verbose mode
						dumpToConsole = true;
						sb.append(m.getName() + ": " + m.getValue() + "\n");
					}
				}
				if (dumpToConsole) {
					System.out.println(sb.toString());
				}
				Thread.sleep(1000 * pollingFrequencySec);				
		    } 
			watch.stop();
			graphiteSender.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);	    
	    


	}

	private void sendMetricToGraphite(GraphiteSender graphiteSender2, Metric m) throws IOException {
		graphiteSender.send(m.getName(), String.valueOf(m.getValue()), m.getTimestamp() /  1000L);
	}
	

/*	
	private void startMeasurement(EngineRequest engineRequest) {
		
		if (graphiteSender == null) return;		// skip if no graphite defined
		
		int reqGauge = 1;
		int reqTypeGauge = -1; // -1 is for unknown request types
		
		// start time = current time (in seconds), minus 1 second (to avoid peaks that last less than 1 sec, 
		// thus under graphite resolution, and thus not visible)
        long now = (System.currentTimeMillis() / 1000L) -1; 
        try {
        	graphiteSender.send(metricPrefix + engineName + ".activity", "1", now);
	        if (engineRequest.getRequestType() == RequestType.Assignment) {
	        	graphiteSender.send(metricPrefix + engineName + ".assignment", "1", now);
	        	reqTypeGauge = 2;
	        } else if(engineRequest.getRequestType() == RequestType.OpenShiftEligibility){
	        	graphiteSender.send(metricPrefix + engineName + ".openshifteligibility", "1", now);
	        	reqTypeGauge = 4;
	        } else if(engineRequest.getRequestType() == RequestType.ShiftSwapEligibility){
	        	graphiteSender.send(metricPrefix + engineName + ".shiftswapeligibility", "1", now);
	        	reqTypeGauge = 6;
	        } else if(engineRequest.getRequestType() == RequestType.Qualification){
	        	graphiteSender.send(metricPrefix + engineName + ".qualification", "1", now);
	        	reqTypeGauge = 8;
	        }
        	graphiteSender.send(metricPrefix + engineName + ".reqType", "" + reqTypeGauge, now);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to send data to Graphite.");
			System.out.println("********* SHUTING DOWN SENDING OF METRICS TO GRAPHITE  *********");	
			graphiteSender = null;
		}
	}
	
    private void stopMeasurement() {

    	if (graphiteSender == null) return;		// skip if no graphite defined
		
        long now = System.currentTimeMillis() / 1000L; // time in sec.
        try {
        	graphiteSender.send(metricPrefix + engineName + ".activity", "0", now);
        	graphiteSender.send(metricPrefix + engineName + ".reqType", "0", now);
        	graphiteSender.send(metricPrefix + engineName + ".assignment", "0", now);
        	graphiteSender.send(metricPrefix + engineName + ".openshifteligibility", "0", now);
        	graphiteSender.send(metricPrefix + engineName + ".shiftswapeligibility", "0", now);
        	graphiteSender.send(metricPrefix + engineName + ".qualification", "0", now);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to send data to Graphite.");
			System.out.println("********* SHUTING DOWN SENDING OF METRICS TO GRAPHITE  *********");	
			graphiteSender = null;
		}
	}
**/

}
