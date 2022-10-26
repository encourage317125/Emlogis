package com.emlogis.common.services.notification;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.model.notification.MsgDeliveryTenantSettings;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.tenant.Tenant;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)

public class NotificationConfigurationService {
	
	@EJB
    private TenantService tenantService;
	
	@EJB
	MsgDeliveryProviderSettingsService msgDeliveryProviderService;
	
	
	public MsgDeliveryTenantSettings getRecipientConfig(String tenantId, MsgDeliveryType deliveryType) {
		MsgDeliveryTenantSettings tenantSettings = null;
		
		Tenant tenant = tenantService.getTenant(tenantId);
		
		if(deliveryType == MsgDeliveryType.EMAIL) {
			tenantSettings = tenant.getEmailDeliveryTenantSettings();			
		} else if(deliveryType == MsgDeliveryType.SMS) {
			tenantSettings = tenant.getSmsDeliveryTenantSettings();			
		} 

		
		return tenantSettings;		
	}
	
	public MsgDeliveryProviderSettings getProviderSettings (String providerId)  {
		MsgDeliveryProviderSettings providerSettings = msgDeliveryProviderService.getMsgDeliveryProvider(providerId);
		return providerSettings;
	}

}
