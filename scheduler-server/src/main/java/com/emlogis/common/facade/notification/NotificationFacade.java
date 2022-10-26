package com.emlogis.common.facade.notification;

import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.model.notification.Notification;
import com.emlogis.model.notification.dto.NotificationDTO;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class NotificationFacade extends BaseFacade {
	
    @EJB
    private NotificationService notificationService;
    
    
    protected NotificationService getNotificationService() {
    	return notificationService;
    }
    
    public NotificationDTO createObject(NotificationDTO notificationDTO) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	
    	NotificationDTO retDTO = null;
    	
    	Notification notification = notificationService.createNotification(notificationDTO);
    	
    	return toDto(notification, NotificationDTO.class);
    	
    }
    
    public List<NotificationDTO> createObjects(List<NotificationDTO> notificationDTOList) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	
    	List<NotificationDTO> retNotDtos = new ArrayList<NotificationDTO>();
    	
    	List<Notification> notifList = new ArrayList<Notification>();
    	
    	notifList = notificationService.createNotifications(notificationDTOList);
    	
    	NotificationDTO retNotificationDTO = null;
    	
    	for(Notification notification : notifList) {
    		retNotificationDTO = toDto(notification, NotificationDTO.class);
    		retNotDtos.add(retNotificationDTO);
    	}    	    			    
    	
    	return retNotDtos;
    	
    }
    
    

}
