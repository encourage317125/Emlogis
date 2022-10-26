package com.emlogis.server.services.notification;

import com.emlogis.common.services.notification.MsgDeliveryProviderSettingsService;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Startup
@Singleton
@DependsOn("EventServiceBean")
//@AccessTimeout(10000)	// 10 secs delay: allow more than the default 5sec for client EJBs to connect to this Service 
						// as connection to Hazelcast on startup can take few secs and potentially more than 5secs
public class NotificationConfigurationServiceBean {
	
	private final Logger logger = LoggerFactory.getLogger(NotificationConfigurationServiceBean.class);
		
	@Inject
	private HazelcastService hzService;
	
    @EJB
    MsgDeliveryProviderSettingsService msgDeliveryProviderService;
		

    @PostConstruct
    void init() {
    }
    

    /**
     * Get queried collection of Skills
     *
     * @param tenantId
     * @param select
     * @param filter
     * @param offset
     * @param limit
     * @param orderBy
     * @param orderDir
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    /*
    public ResultSetDto<MsgDeliveryProviderSettingsDto> getObjects()
    		
        List<MsgDeliveryProviderSettings> l = msgDeliveryProviderService.findAll();
        return toResultSetDto(l, MsgDeliveryProviderSettingsDto.class);
    }
*/


}


