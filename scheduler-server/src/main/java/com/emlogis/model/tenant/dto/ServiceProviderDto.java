package com.emlogis.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Organization is the base class for Customers 
 * 
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceProviderDto extends TenantDto implements Serializable { }


