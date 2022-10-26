package com.emlogis.model.notification.dto;


import com.emlogis.model.dto.CreateDto;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.MsgProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsgDeliveryProviderSettingsCreateDto extends CreateDto<MsgDeliveryProviderSettingsUpdateDto> implements Serializable {

    private String 					id;   
            
    private MsgDeliveryType			deliveryType;			

    private MsgProviderType 		providerType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MsgDeliveryType getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(MsgDeliveryType deliveryType) {
		this.deliveryType = deliveryType;
	}

	public MsgProviderType getProviderType() {
		return providerType;
	}

	public void setProviderType(MsgProviderType providerType) {
		this.providerType = providerType;
	}
            
}