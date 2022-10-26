package com.emlogis.model.shiftpattern;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Site;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.LocalTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ShiftType extends BaseEntity {

    private String name;

    private String description;

    private int paidTimeInMin;

    private boolean	isActive = true;
    
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    private LocalTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "shiftLengthTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "shiftLengthId", referencedColumnName = "id")
    })
    private ShiftLength shiftLength;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shiftType")
    private Set<ShiftReq> shiftReqs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "siteTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "siteId", referencedColumnName = "id")
    })
    private Site site;
    
    public ShiftType() {}

    public ShiftType(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPaidTimeInMin() {
		return paidTimeInMin;
	}

	public void setPaidTimeInMin(int paidTimeInMin) {
		this.paidTimeInMin = paidTimeInMin;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getShiftLengthId() {
        return getShiftLength() == null ? null : shiftLength.getId();
    }

    public Integer getShiftLengthLength() {
        return getShiftLength() == null ? null : shiftLength.getLengthInMin();
    }

    public ShiftLength getShiftLength() {
        return shiftLength;
    }

    public void setShiftLength(ShiftLength shiftLength) {
        this.shiftLength = shiftLength;
    }

    public Set<ShiftReq> getShiftReqs() {
        return shiftReqs;
    }

    public void setShiftReqs(Set<ShiftReq> shiftReqs) {
        this.shiftReqs = shiftReqs;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
