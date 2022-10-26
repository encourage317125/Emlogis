package com.emlogis.model.tenant;

import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.model.notification.MsgDeliveryTenantSettings;
import com.emlogis.model.notification.dto.MsgDeliveryTenantSettingsDto;
import com.emlogis.model.tenant.dto.ModuleLicenseDto;
import com.emlogis.model.tenant.dto.TenantDto;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Tenant is the base class for Customers (Organization class) and Service Provider  (ServiceProvider class)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@_tenantId")
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "Tenant")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Tenant implements Serializable {

    public static final String DEFAULT_SERVICEPROVIDER_ID = "emlogisservice";

    @Id()
    @Column(unique = true, length = 64)
    private String tenantId;              // tenant (organization) Id

    private String name;                  // tenant Name

    private String description;           // tenant short description

    private long inactivityPeriod = 120;  // max inactivity period in minutes, for users (unless overriden at useraccount level)

    private String language = "en";

    private DateTimeZone timeZone = DateTimeZone.UTC;    // tenant default TimeZone.

    private String geo;

    @Column
    private String address;

    @Column
    private String address2;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String country;

    @Column
    private String zip;

    private ModuleLicense productLicense = new ModuleLicense("EGS", ModuleStatus.Trial, -1);        // ModuleLicense for the core product

//    @Column(length = 512)
//    private	String moduleLicenses;		// jsonified ModuleLicense for the optional modules (like ShiftBidding)

    @ElementCollection
    @CollectionTable(name = "tenant_modules", joinColumns = @JoinColumn(name = "tenantId"))
    private List<ModuleLicense> moduleLicenses = new ArrayList<ModuleLicense>();

    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastLoggingDate = new DateTime(0);    // entity creation date/time UTC

    private String lastLoggingUserName;        //

    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime created = new DateTime(0);    // entity creation date/time UTC

    @Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updated = new DateTime(0);    // entity last update date/time UTC

    @JsonIgnore
    @OneToOne(targetEntity = PasswordPolicies.class, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "pwdpolicies_tenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "pwdpolicies_id", referencedColumnName = "id")
    })
    private PasswordPolicies passwordPolicies;

    // relationships to SMS & Email tenant specific settings 
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "smssettings_tenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "smssettings_id", referencedColumnName = "id")
    })
    private MsgDeliveryTenantSettings smsDeliveryTenantSettings;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "emailsettings_tenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "emailsettings_id", referencedColumnName = "id")
    })
    private MsgDeliveryTenantSettings emailDeliveryTenantSettings;

    public Tenant() {
        setCreated(System.currentTimeMillis());
    }

    public long getCreated() {
        return created.getMillis();
    }

    public void setCreated(long created) {
        this.created = new DateTime(created);
        setUpdated(created);
    }

    public long getUpdated() {
        return updated.getMillis();
    }

    public void setUpdated(long updated) {
        this.updated = new DateTime(updated);
    }

    public void touch() {
        this.setUpdated(System.currentTimeMillis());
    }

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

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public ModuleLicense getProductLicense() {
        return productLicense;
    }

    public void setProductLicense(ModuleLicense productLicense) {
        this.productLicense = productLicense;
    }

    public ModuleLicenseDto getProductLicenseInfo() {
        return new ModuleLicenseDto(productLicense.getModuleName(), productLicense.getModuleStatus(), productLicense.getModuleExpirationDate());
    }

    public void setProductLicenseInfo(ModuleLicenseDto productLicenseInfo) {
        this.productLicense = new ModuleLicense(productLicenseInfo.getModuleName(), productLicenseInfo.getModuleStatus(), productLicenseInfo.getModuleExpirationDate());
    }


    public PasswordPolicies getPasswordPolicies() {
        return passwordPolicies;
    }

    public List<ModuleLicense> getModuleLicenses() {
        return moduleLicenses;
    }

    public void setModuleLicenses(List<ModuleLicense> moduleLicenses) {
        this.moduleLicenses = moduleLicenses;
    }

    public List<ModuleLicenseDto> getModulesLicenseInfo() {
        List<ModuleLicenseDto> moduleInfos = new ArrayList();
        if (moduleLicenses != null) {
            for (ModuleLicense module : moduleLicenses) {
                moduleInfos.add(new ModuleLicenseDto(module.getModuleName(), module.getModuleStatus(), module.getModuleExpirationDate()));
            }
        }
        return moduleInfos;
    }

    public void setModulesLicenseInfo(List<ModuleLicenseDto> modulesLicenseInfo) {
        moduleLicenses = new ArrayList();
        if (modulesLicenseInfo != null) {
            for (ModuleLicenseDto moduleInfo : modulesLicenseInfo) {
                moduleLicenses.add(new ModuleLicense(moduleInfo.getModuleName(), moduleInfo.getModuleStatus(), moduleInfo.getModuleExpirationDate()));
            }
        }
        return;
    }

    public void setPasswordPolicies(PasswordPolicies passwordPolicies) {
        this.passwordPolicies = passwordPolicies;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public long getLastLoggingDate() {
        return lastLoggingDate.getMillis();
    }

    public void setLastLoggingDate(long lastLoggingDate) {
        this.lastLoggingDate = new DateTime(lastLoggingDate);
    }

    public void setLastLoggingDate(DateTime lastLoggingDate) {
        this.lastLoggingDate = lastLoggingDate;
    }

    public String getLastLoggingUserName() {
        return lastLoggingUserName;
    }

    public void setLastLoggingUserName(String lastLoggingUserName) {
        this.lastLoggingUserName = lastLoggingUserName;
    }

    public String getAddress() {
        return address;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public MsgDeliveryTenantSettings getSmsDeliveryTenantSettings() {
        return smsDeliveryTenantSettings;
    }

    public void setSmsDeliveryTenantSettings(
            MsgDeliveryTenantSettings smsDeliveryTenantSettings) {
        this.smsDeliveryTenantSettings = smsDeliveryTenantSettings;
    }

    public MsgDeliveryTenantSettings getEmailDeliveryTenantSettings() {
        return emailDeliveryTenantSettings;
    }

    public void setEmailDeliveryTenantSettings(
            MsgDeliveryTenantSettings emailDeliveryTenantSettings) {
        this.emailDeliveryTenantSettings = emailDeliveryTenantSettings;
    }

    public MsgDeliveryTenantSettingsDto getSmsDeliveryTenantSettingsDto() {
        return buildMsgDeliveryTenantSettingsDto(smsDeliveryTenantSettings);
    }

    public MsgDeliveryTenantSettingsDto getEmailDeliveryTenantSettingsDto() {
        return buildMsgDeliveryTenantSettingsDto(emailDeliveryTenantSettings);
    }

    private MsgDeliveryTenantSettingsDto buildMsgDeliveryTenantSettingsDto(
            MsgDeliveryTenantSettings deliveryTenantSettings) {

        MsgDeliveryTenantSettingsDto dto = new MsgDeliveryTenantSettingsDto();
        if (deliveryTenantSettings == null) {
            return dto;
        }
        // return tenant / provider asscoiation data
        dto.setId(deliveryTenantSettings.getId());
        dto.setSettings(deliveryTenantSettings.getSettings());
        dto.setActive(deliveryTenantSettings.isActive());
        dto.setActivationChanged(deliveryTenantSettings.getActivationChanged());
        dto.setStatus(deliveryTenantSettings.getStatus());
        dto.setStatusInfo(deliveryTenantSettings.getStatusInfo());
        dto.setLastChecked(deliveryTenantSettings.getLastChecked());

        // return provider data
        MsgDeliveryProviderSettings providerSettings = deliveryTenantSettings.getDeliveryProviderSettings();
        if (providerSettings == null) {
            return dto;
        }
        dto.setTenantProvidersMetaData(providerSettings.getTenantProviderAttributeMetadata());
        dto.setProviderId(providerSettings.getId());
        dto.setDeliveryType(providerSettings.getDeliveryType());
        dto.setProviderType(providerSettings.getProviderType());
        dto.setProviderName(providerSettings.getName());
        return dto;
    }


    public String getClName() {                        // used and required by toDto conversion
        return this.getClass().getSimpleName();
    }

    public void setClName(String cName) {
    }

    public Class<TenantDto> getReadDtoClass() {
        return TenantDto.class;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tenant) {
            Tenant other = (Tenant) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getName(), other.getName());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getTenantId());
        builder.append(getName());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getTenantId()).
                append("name", getName()).
                toString();
    }

}


