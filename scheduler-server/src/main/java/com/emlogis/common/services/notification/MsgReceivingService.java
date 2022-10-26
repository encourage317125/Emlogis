package com.emlogis.common.services.notification;

import javax.ejb.*;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)

public class MsgReceivingService {

	public void onMessage() {
		
	}
	
	public void registerListener() {
		
	}
	
	public void processMessages() {
		
	}
}
