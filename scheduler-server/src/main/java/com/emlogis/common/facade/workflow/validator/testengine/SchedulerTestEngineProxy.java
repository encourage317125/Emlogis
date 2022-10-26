package com.emlogis.common.facade.workflow.validator.testengine;

import com.emlogis.SchedulerTestEngine;

import javax.ejb.*;

/**
 * Created by user on 01.10.15.
 */
@Startup
@Singleton
@DependsOn({"StartupServiceBean"})
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SchedulerTestEngineProxy {

    private SchedulerTestEngine testEngine;

    public static final String SERVER_STARTED = "STARTED";
    public static final String SERVER_STARTING = "STARTING";
    public static final String SERVER_FAILED = "FAILED";
    public static final String SERVER_STOPPED = "STOPPED";
    public static final String SERVER_STOPPING = "STOPPING";

    public String start() {
        String result = null;
        if (testEngine == null) {
            testEngine = new SchedulerTestEngine();
            result = testEngine.start();
        } else {
            if (testEngine.getServer().getState().equals(SERVER_STOPPED) ||
                    testEngine.getServer().getState().equals(SERVER_FAILED)) {
                result = testEngine.start();
            }
        }
        return result == null ? testEngine.getServer().getURI() + " " + testEngine.getServer().getState() : result;
    }

    public String stop() {
        String result = null;
        if (testEngine != null) {
            if (testEngine.getServer().getState().equals(SERVER_STARTED)) {
                result = testEngine.stop();
            }
            return result == null ? testEngine.getServer().getURI() + " " + testEngine.getServer().getState() : result;
        }
        return SERVER_STOPPED;
    }

}
