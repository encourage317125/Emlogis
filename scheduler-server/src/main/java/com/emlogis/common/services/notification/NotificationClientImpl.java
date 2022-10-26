package com.emlogis.common.services.notification;


import javax.ejb.EJB;

import org.joda.time.DateTime;

import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.server.services.eventservice.NotificationClient;

@Authenticated
public class NotificationClientImpl implements NotificationClient {
	
	@EJB
	NotificationService notificationService;
	
	@EJB
	EmployeeService employeeService;
	
    @Override
    public void notify(NotificationMessageDTO receivedNotificationMessageDTO) {
    	NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
    	
    	Employee employee;
    	
    	employee = getEmployeeByEmail("kim@voyager.com", receivedNotificationMessageDTO.getTenantId());
    	
    	String kimId = employee.getUserAccount().getId();
    	
        employee = getEmployeeByEmail("janeway@voyager.com", receivedNotificationMessageDTO.getTenantId());
    	
    	String janeWayId = employee.getUserAccount().getId();
    	
    	notificationMessageDTO.setTenantId("jcso");
    	notificationMessageDTO.setSenderUserId(janeWayId);
    	notificationMessageDTO.setReceiverUserId(kimId);
    	notificationMessageDTO.setTobeDeliveredOn(new DateTime());

    	notificationMessageDTO.setNotificationOperation(NotificationOperation.POST);
    	notificationMessageDTO.setNotificationCategory(NotificationCategory.OPEN_SHIFTS);
    	

    	// notificationService.sendNotification(notificationMessageDTO);
    }
    
    private Employee getEmployeeByEmail(String email, String tenantId) {
    	Employee employee = null;
    	
    	SimpleQuery simpleQuery = new SimpleQuery(tenantId);
    	
    	simpleQuery.addFilter("workEmail='" + email + "'");
    	
    	try {
			ResultSet<Employee> rs = employeeService.findEmployees(simpleQuery);
			employee = rs.getResult().iterator().next();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return employee;
    	
    }

}
