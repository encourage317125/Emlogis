package com.emlogis.model.workflow.notification.common;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTimeZone;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by user on 23.07.15.
 */
public final class MessageEmployeeInfo implements Serializable {

    private String id;
    private String tenantId;
    private String name;
    private DateTimeZone timeZone;
    private String lang;
    private String country;

    public MessageEmployeeInfo(
            String id,
            String tenantId,
            String name,
            DateTimeZone timeZone,
            String lang,
            String country
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.timeZone = timeZone;
        this.lang = lang;
        this.country = country;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public String getLang() {
        return lang;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageEmployeeInfo) {
            MessageEmployeeInfo other = (MessageEmployeeInfo) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getName(), other.getName());
            builder.append(getTimeZone(), other.getTimeZone());
            builder.append(getLang(), other.getLang());
            builder.append(getCountry(), other.getCountry());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getTenantId());
        builder.append(getName());
        builder.append(getTimeZone());
        builder.append(getLang());
        builder.append(getCountry());
        return builder.toHashCode();
    }

    public Locale locale() {
        if (country != null && lang != null) {
            return new Locale(lang, country);
        } else if (country == null && lang != null) {
            return new Locale(lang);
        } else {
            return new Locale("en", "US");
        }
    }
}
