package com.emlogis.model.employee;

import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationDeliveryMethod;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class NotificationConfig extends BaseEntity {
	
    //bi-directional many-to-one association to Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "employeeTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "employeeId", referencedColumnName = "id")
    })
    private Employee employee;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "userAccountTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "userAccountId", referencedColumnName = "id")
    })
    private UserAccount userAccount;
    
    @Column
	private Boolean enabled;

    @Column
	private NotificationDeliveryMethod method;

    @Column
	private NotificationDeliveryFormat format = NotificationDeliveryFormat.PLAIN_TEXT;
	
	public NotificationConfig() {}
		
	public NotificationConfig(PrimaryKey primaryKey, Boolean enabled,
			NotificationDeliveryMethod method, NotificationDeliveryFormat format) {
		super(primaryKey);
		this.enabled = enabled;
		this.method = method;
		this.format = format;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public NotificationDeliveryMethod getMethod() {
		return method;
	}

	public void setMethod(NotificationDeliveryMethod method) {
		this.method = method;
	}

	public NotificationDeliveryFormat getFormat() {
		return format;
	}

	public void setFormat(NotificationDeliveryFormat format) {
		this.format = format;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}
}
