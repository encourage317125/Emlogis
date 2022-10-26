package com.emlogis.model.schedule.changes;

import com.emlogis.model.dto.Dto;
import com.emlogis.rest.auditing.AuditContext;
import com.emlogis.rest.auditing.AuditRecord;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class BaseChangeEntity {

    @EmbeddedId
    private ChangePrimaryKey primaryKey;
    
    private String changeRequestId;					// Id of associated  changeRequest (if change done by workflow)

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime created = new DateTime(0);    // entity creation date/time UTC

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updated = new DateTime(0);    // entity last update date/time UTC

    private String ownedBy;                                  // account id of owner

    private String createdBy;                                // account id of creator

    private String updatedBy;                                // account id of last account that modified that object

    public BaseChangeEntity() {
        setCreated(new DateTime());
    }

    public BaseChangeEntity(ChangePrimaryKey primaryKey) {
        this();
        this.primaryKey = primaryKey;
    }

    public ChangePrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ChangePrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTenantId() {
        return primaryKey.getTenantId();
    }

    public String getId() {
        return primaryKey.getId();
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

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public void setCreated(long created) {
        this.created = new DateTime(created);
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public void setUpdated(long updated) {
        this.updated = new DateTime(updated);
    }

    public String getChangeRequestId() {
		return changeRequestId;
	}

	public void setChangeRequestId(String changeRequestId) {
		this.changeRequestId = changeRequestId;
	}

	public void touch() {
        setUpdated(new DateTime());
    }

    public String getClName() {                        // used and required by toDto conversion
        return this.getClass().getSimpleName();
    }

    public Class<? extends Dto> getReadDtoClass() {				// used to get the default Read Dto class
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseChangeEntity be = (BaseChangeEntity) o;

        if (primaryKey != null ? !primaryKey.equals(be.primaryKey) : be.primaryKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return primaryKey != null ? primaryKey.hashCode() : 0;
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
    public void preUpdate(){
        setUpdated(new DateTime());
        AuditRecord auditRecord = AuditContext.get();
        if (auditRecord != null) {
            setUpdatedBy(auditRecord.getUserId());
        }
    }
}
