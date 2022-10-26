package com.emlogis.server.services;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.scheduler.engine.communication.AppServerStatus;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.shared.services.hazelcastservice.HazelcastServiceImpl;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.concurrent.TimeUnit;

@Startup
@Singleton
@AccessTimeout(10000)	// 10 secs delay: allow more than the default 5sec for client EJBs to connect to this Service 
						// as connection to Hazelcast on startup can take few secs and potentially more than 5secs
public class HazelcastServiceBean extends HazelcastServiceImpl implements ASHazelcastService {

    private final static Logger logger = Logger.getLogger(HazelcastServiceBean.class);

    public HazelcastServiceBean() {}

	@Override
	public String getAppServerName() {
		return getComponentName();
	}

    @Override
    public void init() {
		logger.debug("***************************** HazelcastServiceBean - Init:  ....");
		StopWatch watch = new StopWatch();
		watch.start();

        String appServerName = System.getProperty(HzConstants.APP_SERVER_ID_PARAMNAME);
        if (StringUtils.isBlank(appServerName)) {
            logger.info(String.format("AppServer.HazelcastService initialization: " +
                    "Unable to find name for this Service, '%s' system property missing.",
                    HzConstants.APP_SERVER_ID_PARAMNAME));
        }
    	this.init(appServerName, true);
    	watch.stop();
		logger.debug("***************************** HazelcastServiceBean - Init done in " + watch.getTime() + "ms");
    }

	protected void registerComponentInMap(String ip) {
        AppServerStatus appServerStatus = new AppServerStatus();
        appServerStatus.setName(getComponentName());
        appServerStatus.setIp(ip);
        appServerStatus.setUpdated(System.currentTimeMillis());

        String appServerStatusJson = EmlogisUtils.toJsonString(appServerStatus);

        getComponentMap().put(getComponentName(), appServerStatusJson, 1, TimeUnit.MINUTES);
    }

}

