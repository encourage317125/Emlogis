package com.emlogis.scheduler.engine.communication;

public class HzConstants {

	public static final String APP_SERVER_ID_PARAMNAME	= "AppServerId";	// name of AppServerId parameter
    public static final String ENGINE_ID_PARAMNAME		= "EngineId";		// name of EngineId parameter

    public static final String APP_SERVER_MAP			= "AppServerMap";
    public static final long APP_SERVER_MAP_UPDATE_PERIOD	= 30 ;	// refresh rate in seconds for the APP_SERVER_MAP

    public static final String ENGINE_MAP				= "EngineMap";
    public static final long ENGINE_MAP_UPDATE_PERIOD	= 30 ;	// refresh rate in seconds for the ENGINE_MAP

    // Request Queue name. For now, it will be just: REQUEST_QUEUE_PREFIX
    // When we have multiple Engine Types, actual name will be REQUEST_QUEUE_PREFIX + '_' EngineType (ex RequestQueue_Generic)
    public static final String REQUEST_QUEUE_PREFIX	= "RequestQueue";
    public static final String REQUEST_DATA_MAP		= "RequestDataMap";

    // Response Queue name. For now, it will be just: RESPONSE_QUEUE_PREFIX
    // When we have different versions of AppServers  , actual name will be RESPONSE_QUEUE_PREFIX + '_' AppServerType (ex AppServer_2Beta, or AppServer_JCSO)
    public static final String RESPONSE_QUEUE_PREFIX	= "ResponseQueue";
    public static final String RESPONSE_DATA_MAP		= "ResponseDataMap";

    public static final String QUALIFICATION_TRACKING_MAP   = "QualificationTrackingMap";  // used to track requests for BOTH Qualification AND Eligibility 

    // Notification Queue name.
    // Actual name is  NOTIFICATION_QUEUE_PREFIX + '_' AppServerId
    public static final String NOTIFICATION_QUEUE_PREFIX	= "NotificationQueue";
    
    public static final String NOTIFICATION_SEND_QUEUE = "SendQueue";
    
    public static final String NOTIFICATION_RECEIVE_QUEUE = "ReceiveQueue";
    
    public static final String NOTIFICATION_RECEIVE_EMAIL = "ReceiveEmail";
    
    public static final String NOTIFICATION_RECEIVE_SMS = "ReceiveSMS";
    
    public static final int NOTIFICATION_QUEUE_WAIT_TIME = 10;
    
    public static final String LOCK_MAP				= "LockMap";
    
    public static final String GLOBAL_EVENT_TOPIC 	= "GlobalEventTopic";		// name of topic used by Notification Service for cluster wide event communication
    public static final String AS_EVENT_TOPIC 		= "AppSrvEventTopic";		// name of topic used by Notification Service for sending events to AppServers only
    public static final String ENGINE_EVENT_TOPIC 	= "EngineEventTopic";		// name of topic used by Notification Service for sending events to Engines only

    public static final String COMMON_SUFFIX 		= "_common";

    

    public static final String ABORT_MAP = "abortMap";
    public static final String SHUTDOWN_MAP = "shutdownMap";

    public static final String SESSION_MAP = "sessionMap";
    public static final String SESSION_NOTIFICATION_MAP = "sessionNotificationMap";
	public static final String HAZELCAST_TIMEOUT = "hazelcast.timeout";
	public static final String HAZELCAST_ADDRESS = "hazelcast.address";
	public static final String HAZELCAST_ATTEMP_LIMIT = "hazelcast.attemp.limit";
	public static final String HAZELCAST_ATTEMP_PERIOD = "hazelcast.attemp.period";
	public static final String HAZELCAST_REDO_OPERATION = "hazelcast.redo.operation";

}
