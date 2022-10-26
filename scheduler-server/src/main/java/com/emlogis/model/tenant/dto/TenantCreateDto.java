package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.CreateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Tenant is the base class for Customers & ServiceProviders 
 * 
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantCreateDto<T extends TenantUpdateDto> extends CreateDto<TenantUpdateDto> implements Serializable {

	public static final String TENANT_ID = "tenantId";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ADDRESS = "address";
	public static final String ADDRESS2 = "address2";
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String ZIP = "zip";

	private String tenantId;
	private String name;
/*
	private String description;
	private String address;
	private String address2;
	private String city;
	private String state;
	private String country;
	private String zip;
	private String timeZone;
*/
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
/*
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
*/
}


