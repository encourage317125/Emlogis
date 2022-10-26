package com.emlogis.model;

import com.emlogis.common.UniqueId;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PrimaryKey implements Serializable {
	
	private static final long serialVersionUID = 1L;

    @Column(name = "tenantId", nullable = false, length = 64)
	private	String tenantId;
    
    @Column(name = "id", nullable = false)  
	private	String id;
	
	public PrimaryKey() {}

	public PrimaryKey(String tenantId, String id) {
		super();
		this.tenantId = tenantId;
		this.id = (id != null ? id : UniqueId.getId());
	}

	public PrimaryKey(String tenantId) {
		this(tenantId, null);
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
		if (obj == null || !(obj instanceof PrimaryKey)) return false;
		PrimaryKey pk = (PrimaryKey) obj;
		return StringUtils.equals(tenantId, pk.getTenantId()) && StringUtils.equals(id, pk.getId());
    }

    @Override
    public String toString() {
        return "PrimaryKey{" +
                "tenantId='" + tenantId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

}
