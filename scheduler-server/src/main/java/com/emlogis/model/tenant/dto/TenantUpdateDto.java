package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.UpdateDto;
import com.emlogis.model.notification.dto.MsgDeliveryTenantSettingsUpdateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Tenant is the base class for Customers & ServiceProviders 
 * 
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantUpdateDto extends UpdateDto implements Serializable {

    private String name;								// tenant Name

    private String description;							// tenant short description
    
    private	long inactivityPeriod = -10;				// consider -10 as pecial value = unset by client

    // Value's examples: Pacific/Auckland, Etc/GMT+12 etc.
    // Possible values can be found by link http://joda-time.sourceforge.net/timezones.html (Canonical ID)
    private String timeZone;

    private	String language;

    private	String geo;
    
 	private String address;
    
 	private String address2;
    
 	private String city;
    
 	private String state;
       
 	private String country;

 	private String zip;
    
    private	ModuleLicenseDto			productLicenseInfo;
    
    private List<ModuleLicenseDto>		modulesLicenseInfo;	

    private MsgDeliveryTenantSettingsUpdateDto smsDeliveryTenantSettingsDto;
    private MsgDeliveryTenantSettingsUpdateDto emailDeliveryTenantSettingsDto; 
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public long getInactivityPeriod() {
        return inactivityPeriod;
    }

    public void setInactivityPeriod(long inactivityPeriod) {
        this.inactivityPeriod = inactivityPeriod;
    }

    public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getGeo() {
		return geo;
	}

	public void setGeo(String geo) {
		this.geo = geo;
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

	public ModuleLicenseDto getProductLicenseInfo() {
		return productLicenseInfo;
	}

	public void setProductLicenseInfo(ModuleLicenseDto productLicenseInfo) {
		this.productLicenseInfo = productLicenseInfo;
	}

	public List<ModuleLicenseDto> getModulesLicenseInfo() {
		return modulesLicenseInfo;
	}

	public void setModulesLicenseInfo(List<ModuleLicenseDto> modulesLicenseInfo) {
		this.modulesLicenseInfo = modulesLicenseInfo;
	}

	public MsgDeliveryTenantSettingsUpdateDto getSmsDeliveryTenantSettingsDto() {
		return smsDeliveryTenantSettingsDto;
	}

	public void setSmsDeliveryTenantSettingsDto(
			MsgDeliveryTenantSettingsUpdateDto smsDeliveryTenantSettingsDto) {
		this.smsDeliveryTenantSettingsDto = smsDeliveryTenantSettingsDto;
	}

	public MsgDeliveryTenantSettingsUpdateDto getEmailDeliveryTenantSettingsDto() {
		return emailDeliveryTenantSettingsDto;
	}

	public void setEmailDeliveryTenantSettingsDto(
			MsgDeliveryTenantSettingsUpdateDto emailDeliveryTenantSettingsDto) {
		this.emailDeliveryTenantSettingsDto = emailDeliveryTenantSettingsDto;
	}
    
}


