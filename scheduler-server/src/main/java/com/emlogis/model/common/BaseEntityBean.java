package com.emlogis.model.common;

import com.emlogis.rest.auditing.AuditContext;
import com.emlogis.rest.auditing.AuditRecord;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
//@Entity(name = "BaseEntityBean")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseEntityBean implements SimpleKeyBaseEntity {

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime created = new DateTime(0);

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updated = new DateTime(0);

    private String ownedBy;                                  // account id of owner

    private String createdBy;                                // account id of creator

    private String updatedBy;                                // account id of last account that modified that object


    public BaseEntityBean() {
        setCreated(new DateTime());
    }

    @Override
    public DateTime getCreated() {
        return created;
    }

    @Override
    public void setCreated(DateTime created) {
        this.created = created;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(String ownedBy) {
        this.ownedBy = ownedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @PrePersist
    public void prePersist() {
        DateTime timestamp = new DateTime();
        setUpdated(timestamp);
        setCreated(timestamp);

        AuditRecord auditRecord = AuditContext.get();
        if (auditRecord != null) {
            String userId = auditRecord.getUserId();
            setCreatedBy(userId);
            setUpdatedBy(userId);
            setOwnedBy(userId);     // by default, make creator the owner
        }
    }

    @PreUpdate
    public void preUpdate() {
        setUpdated(new DateTime());
        AuditRecord auditRecord = AuditContext.get();
        if (auditRecord != null) {
            setUpdatedBy(auditRecord.getUserId());

        }
    }
}
