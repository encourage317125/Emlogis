package com.emlogis.model.structurelevel;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Holiday extends BaseEntity {

    @Column(nullable = false)
    private	String name;
    private	String abbreviation;
    private	String description;
    private int timeToDeductInMin;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime effectiveStartDate = new DateTime(0);     

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime effectiveEndDate = new DateTime(0);     

    public Holiday() {}

    public Holiday(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTimeToDeductInMin() {
		return timeToDeductInMin;
	}

	public void setTimeToDeductInMin(int timeToDeductInMin) {
		this.timeToDeductInMin = timeToDeductInMin;
	}

	public long getEffectiveStartDate() {
		return effectiveStartDate  == null ? 0 : effectiveStartDate.toDate().getTime();
    }

    public void setEffectiveStartDate(long effectiveStartDate) {
		this.effectiveStartDate = new DateTime(effectiveStartDate);
    }

    public long getEffectiveEndDate() {
		return effectiveEndDate  == null ? 0 : effectiveEndDate.toDate().getTime();
    }

    public void setEffectiveEndDate(long effectiveEndDate) {
		this.effectiveEndDate = new DateTime(effectiveEndDate);
    }
}
