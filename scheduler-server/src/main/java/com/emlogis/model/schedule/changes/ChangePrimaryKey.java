package com.emlogis.model.schedule.changes;

import com.emlogis.common.UniqueId;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@Embeddable
public class ChangePrimaryKey implements Serializable {

    @Column(name = "year", nullable = false)
    private int year;

    private static final long serialVersionUID = 1L;

    @Column(name = "tenantId", nullable = false, length = 64)
    private	String tenantId;

    @Column(name = "id", nullable = false)
    private	String id;

    public ChangePrimaryKey() {}

    public ChangePrimaryKey(String tenantId) {
        this(tenantId, null);
        year = Calendar.getInstance().get(Calendar.YEAR);
    }

    public ChangePrimaryKey(String tenantId, String id) {
        this();
        this.tenantId = tenantId;
        this.id = (id != null ? id : UniqueId.getId());
        year = Calendar.getInstance().get(Calendar.YEAR);
    }

    public ChangePrimaryKey(int year, String tenantId, String id) {
        this(tenantId, id);
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int hashCode() {
        return (tenantId + id).hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof ChangePrimaryKey)) return false;
        ChangePrimaryKey pk = (ChangePrimaryKey) obj;
        return StringUtils.equals(tenantId, pk.getTenantId()) && StringUtils.equals(id, pk.getId()) && year == pk.year;
    }

    @Override
    public String toString() {
        return "ChangePrimaryKey{" +
                "year=" + year +
                ", tenantId='" + tenantId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
