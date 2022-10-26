package com.emlogis.model.shiftpattern;

import com.emlogis.common.ModelUtils;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.schedule.Schedule;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class PatternElt extends BaseEntity {

    private int dayOffset;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime cdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "shiftPatternId", referencedColumnName = "id"),
            @JoinColumn(name = "shiftPatternTenantId", referencedColumnName = "tenantId")
    })
    private ShiftPattern shiftPattern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "scheduleId", referencedColumnName = "id"),
            @JoinColumn(name = "scheduleTenantId", referencedColumnName = "tenantId")
    })
    private Schedule schedule;

    public PatternElt() {}

    public PatternElt(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public int getDayOffset() {
        return dayOffset;
    }

    public void setDayOffset(int dayOffset) {
        this.dayOffset = dayOffset;
    }

    public DateTime getCdDate() {
        return cdDate;
    }

    public void setCdDate(DateTime cdDate) {
        this.cdDate = ModelUtils.cutDateIfLessPredefined(cdDate);
    }

    public ShiftPattern getShiftPattern() {
        return shiftPattern;
    }

    public void setShiftPattern(ShiftPattern shiftPattern) {
        this.shiftPattern = shiftPattern;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
