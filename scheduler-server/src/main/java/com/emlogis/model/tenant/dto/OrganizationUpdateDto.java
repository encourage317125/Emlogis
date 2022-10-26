package com.emlogis.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * 
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationUpdateDto extends TenantUpdateDto implements Serializable {
}


