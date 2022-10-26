package com.emlogis.common.facade.notification;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import com.emlogis.common.UniqueId;
import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.notification.MsgDeliveryProviderSettingsService;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.model.notification.MsgDeliveryProviderStatus;
import com.emlogis.model.notification.ProviderAttributeMetadata;
import com.emlogis.model.notification.dto.MsgDeliveryProviderSettingsCreateDto;
import com.emlogis.model.notification.dto.MsgDeliveryProviderSettingsDto;
import com.emlogis.model.notification.dto.MsgDeliveryProviderSettingsUpdateDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class MsgDeliveryProviderSettingsFacade extends BaseFacade {
	
    @EJB
    private MsgDeliveryProviderSettingsService msgDeliveryProviderService;

    /**
     * Getter for the skillService field
     *
     * @return
     */
    protected MsgDeliveryProviderSettingsService getMsgDeliveryProviderSettingsService() {
        return msgDeliveryProviderService;
    }

	/**
	 * @param select
	 * @param filter
	 * @param offset
	 * @param limit
	 * @param orderBy
	 * @param orderDir
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSetDto<MsgDeliveryProviderSettingsDto> getObjects(
			String select, String filter, int offset, int limit,
			String orderBy, String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        SimpleQuery simpleQuery = new SimpleQuery(null);
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        ResultSet<MsgDeliveryProviderSettings> resultSet =
                msgDeliveryProviderService.findMsgDeliveryProviderSettings(simpleQuery);
        return toResultSetDto(resultSet, MsgDeliveryProviderSettingsDto.class);
	}	

	/**
	 * @param createDto
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public MsgDeliveryProviderSettingsDto createObject(MsgDeliveryProviderSettingsCreateDto createDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (createDto == null) {
			throw new ValidationException("Missing creation paramters");			
		}
		if (StringUtils.isBlank(createDto.getId())) {
			createDto.setId(UniqueId.getId());
		}
		MsgDeliveryProviderSettings providerSettings =
                msgDeliveryProviderService.getMsgDeliveryProvider(createDto.getId());
		if (providerSettings != null) {
			throw new ValidationException("Invalid MsgDeliveryProvider Identifier. entity already exist");
		}

        // create entity
		providerSettings = msgDeliveryProviderService.createMsgDeliveryProviderSettings(createDto.getId());
		providerSettings.setCreated(System.currentTimeMillis());
		providerSettings.setDeliveryType(createDto.getDeliveryType());
		providerSettings.setProviderType(createDto.getProviderType());

		MsgDeliveryProviderSettingsUpdateDto updateDto = createDto.getUpdateDto();
        if (updateDto != null) {
            updateObject(providerSettings.getId(), updateDto);
        }
        return toDto(providerSettings, MsgDeliveryProviderSettingsDto.class);
	}

	/**
	 * @param id
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public MsgDeliveryProviderSettingsDto getObject(String id) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
		MsgDeliveryProviderSettings providerSettings = msgDeliveryProviderService.getMsgDeliveryProvider(id);
		if (providerSettings == null) {
			throw new ValidationException("Invalid MsgDeliveryProvider Identifier. entity doesn't exist");
		}
        return toDto(providerSettings, MsgDeliveryProviderSettingsDto.class);	
    }

	/**
	 * @param id
	 * @param updateDto
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public MsgDeliveryProviderSettingsDto updateObject(String id, MsgDeliveryProviderSettingsUpdateDto updateDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		long now = System.currentTimeMillis();
		
		MsgDeliveryProviderSettings providerSettings = msgDeliveryProviderService.getMsgDeliveryProvider(id);
		if (providerSettings == null) {
			throw new ValidationException("Invalid MsgDeliveryProvider Identifier. entity doesn't exist");
		}
		boolean modified = false;
		if (StringUtils.isNotBlank(updateDto.getName())
                && !StringUtils.equals(providerSettings.getName(), updateDto.getName())) {
			modified = true;
			providerSettings.setName(updateDto.getName());
		}
		if (StringUtils.isNotBlank(updateDto.getDescription())
                && !StringUtils.equals(providerSettings.getDescription(), updateDto.getDescription())) {
			modified = true;
			providerSettings.setDescription(updateDto.getDescription());
		}		
		if (providerSettings.isActive() != updateDto.isActive() ) {
			modified = true;
			providerSettings.setActive(updateDto.isActive());
			providerSettings.setActivationChanged(now);
		}
		Map<String,String> updateSettings = updateDto.getSettings();
		Map<String,String> currentSettings = providerSettings.getSettings();
		if (updateSettings != null) {
			boolean settingsModified = false;
			Map<String,ProviderAttributeMetadata> metadata = providerSettings.getProviderAttributeMetadata();
			for (ProviderAttributeMetadata  settingMetadata : metadata.values()) {
				String key = settingMetadata.getName();
				System.out.println("Modifying setting: " + key + " with: " + updateSettings.get(key));
				if (updateSettings.get(key) != null && !StringUtils.equals(currentSettings.get(key), updateSettings.get(key))) {
					settingsModified = true;
					currentSettings.put(key, updateSettings.get(key));
				}
			}
			if (settingsModified) {
				modified = true;
				providerSettings.setSettings(currentSettings);	
			}
		}

        if (modified) {
        	providerSettings.touch();
        	providerSettings.setUpdatedBy(getActualUserName());
            providerSettings = msgDeliveryProviderService.update(providerSettings);
        }

        return toDto(providerSettings, MsgDeliveryProviderSettingsDto.class);
	}

	/**
	 * @param id
	 * @return
	 */
	public void deleteObject(String id) {
		MsgDeliveryProviderSettings providerSettings = msgDeliveryProviderService.getMsgDeliveryProvider(id);
		if (providerSettings == null) {
			throw new ValidationException("Invalid MsgDeliveryProvider Identifier. entity doesn't exist");
		}
		
		// TODO if there is a tenant referencing this provider, can't delete this provider.
		// check thre is no MsgDeliveryTenantSettings.deliveryProviderSettings == providerSettings
		if (false) {
			throw new ValidationException("Cannot delete MsgDeliveryProvider, as it is used by Tenants");			
		}
		msgDeliveryProviderService.delete(providerSettings);
	}

	/**
	 * @param id
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	
	// TODO fake implementation, sleep few secs and set  a random status

	public MsgDeliveryProviderSettingsDto check(String id) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
		MsgDeliveryProviderSettings providerSettings = msgDeliveryProviderService.getMsgDeliveryProvider(id);
		if (providerSettings == null) {
			throw new ValidationException("Invalid MsgDeliveryProvider Identifier. entity doesn't exist");
		}
		
		long now = System.currentTimeMillis();
		long val = (now % 5);
		try {
			Thread.sleep(val * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		val %= 2;
		if (val == 0) {
			providerSettings.setStatus(MsgDeliveryProviderStatus.OK);
			providerSettings.setStatusInfo("OK. now=" + now + " -> val=" + val);
		}
		else {
			providerSettings.setStatus(MsgDeliveryProviderStatus.ERROR);
			providerSettings.setStatusInfo("Failed: now=" + now + " -> val=" + val);
		}
		providerSettings.setLastChecked(now);
        providerSettings = msgDeliveryProviderService.update(providerSettings);
        return toDto(providerSettings, MsgDeliveryProviderSettingsDto.class);
	}

}
    
