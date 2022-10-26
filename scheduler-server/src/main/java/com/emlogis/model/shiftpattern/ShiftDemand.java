package com.emlogis.model.shiftpattern;

import com.emlogis.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.LocalTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ShiftDemand extends BaseEntity {

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    private LocalTime startTime;

    private int lengthInMin;

    private int employeeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "shiftPatternTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "shiftPatternId", referencedColumnName = "id")
    })
    private ShiftPattern shiftPattern;

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getLengthInMin() {
        return lengthInMin;
    }

    public void setLengthInMin(int lengthInMin) {
        this.lengthInMin = lengthInMin;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public ShiftPattern getShiftPattern() {
        return shiftPattern;
    }

    public void setShiftPattern(ShiftPattern shiftPattern) {
        this.shiftPattern = shiftPattern;
    }
}
