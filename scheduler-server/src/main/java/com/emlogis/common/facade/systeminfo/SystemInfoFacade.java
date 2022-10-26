package com.emlogis.common.facade.systeminfo;

import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.systeminfo.SystemInfoService;
import com.emlogis.model.dto.systeminfo.HazelcastInfoDto;
import com.emlogis.model.dto.systeminfo.NotificationInfoDto;
import com.emlogis.model.notification.ArchivedReceiveQueueNotification;
import com.emlogis.model.notification.ArchivedSendQueueNotification;
import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.scheduler.engine.communication.HzConstants;

import javax.ejb.*;
import java.util.List;
import java.util.Map;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SystemInfoFacade extends BaseFacade {

    @EJB
    private SystemInfoService systemInfoService;

    @EJB
    private HazelcastClientService hazelcastClientService;

    public Map dbSummary() {
        return systemInfoService.dbInfo("");
    }

    public List dbPerCustomer() {
        return systemInfoService.dbPerCustomer();
    }

    public HazelcastInfoDto hzInfo() {
        HazelcastInfoDto hazelcastInfoDto = new HazelcastInfoDto();
        hazelcastInfoDto.setGlobalAssignmentReqQueueSize(hazelcastClientService
                .getQueueSize(HzConstants.REQUEST_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX));
        //not impl
        hazelcastInfoDto.setGlobalQualificationReqQueueSize(0);
        hazelcastInfoDto.setGlobalResponseQueueSize(hazelcastClientService
                .getQueueSize(HzConstants.RESPONSE_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX));
        hazelcastInfoDto.setRequestDataMapSize(hazelcastClientService.getMapSize(HzConstants.REQUEST_DATA_MAP));
        hazelcastInfoDto.setResponseDataMapSize(hazelcastClientService.getMapSize(HzConstants.RESPONSE_DATA_MAP));
        hazelcastInfoDto.setQualificationTrackerMapSize(hazelcastClientService.getMapSize(HzConstants.QUALIFICATION_TRACKING_MAP));
        hazelcastInfoDto.setAbortMapSize(hazelcastClientService.getMapSize(HzConstants.ABORT_MAP));
        hazelcastInfoDto.setShutdownMapSize(hazelcastClientService.getMapSize(HzConstants.SHUTDOWN_MAP));
        hazelcastInfoDto.setAppServers(hazelcastClientService.getMapSize(HzConstants.APP_SERVER_MAP));
        hazelcastInfoDto.setEngines(hazelcastClientService.getMapSize(HzConstants.ENGINE_MAP));
        return hazelcastInfoDto;
    }

    public NotificationInfoDto notificationInfo() {
        NotificationInfoDto notificationInfoDto = new NotificationInfoDto();
        notificationInfoDto.setSend(systemInfoService.entityCount(SendNotification.class.getSimpleName(), "", ""));
        notificationInfoDto.setReceive(systemInfoService.entityCount(ReceiveNotification.class.getSimpleName(), "", ""));
        notificationInfoDto.setArchivedSendQueue(systemInfoService.entityCount(ArchivedSendQueueNotification.class.getSimpleName(), "", ""));
        notificationInfoDto.setArchivedReceiveQueue(systemInfoService.entityCount(ArchivedReceiveQueueNotification.class.getSimpleName(), "", ""));
        return notificationInfoDto;
    }
}
