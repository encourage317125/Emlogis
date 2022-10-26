package com.emlogis.model.schedule;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Team;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "ShiftStructure")
public class ShiftStructure extends BaseEntity {

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "teamTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "teamId", referencedColumnName = "id")
    })
    private Team team;

    @OneToMany
    private Set<ShiftReqOld> shiftReqs = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ShiftStructure_Schedule",
            joinColumns = {@JoinColumn(name = "ShiftStructure_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "ShiftStructure_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "schedules_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "schedules_id", referencedColumnName = "id")})
    private Set<Schedule> schedules;

    public ShiftStructure() {
    }

    public ShiftStructure(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public Long getStartDate() {
        return startDate == null ? null : startDate.toDate().getTime();
    }

    public void setStartDate(long startDate) {
        this.startDate = new DateTime(startDate);
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Set<ShiftReqOld> getShiftReqs() {
        return shiftReqs;
    }

    public void setShiftReqs(Set<ShiftReqOld> shiftReqs) {
        this.shiftReqs = shiftReqs;
    }

    public Set<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(Set<Schedule> schedules) {
        this.schedules = schedules;
    }
}
