package com.emlogis.model.schedule;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.LocalTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "ShiftReqOld")
public class ShiftReqOld extends BaseEntity implements Cloneable {

    private String shiftStructureId;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    private LocalTime startTime;

    private int durationInMins;

    private int dayIndex;

    private boolean night;

    private boolean excess;

    @Column(insertable = false, updatable = false)
    private String shiftLengthId;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "shiftLengthId", referencedColumnName = "id"),
            @JoinColumn(name = "shiftLengthTenantId", referencedColumnName = "tenantId")
    })
    private ShiftLength shiftLength;

    private int employeeCount;

    private String skillId;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "skillId", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "tenantId", referencedColumnName = "tenantId", insertable = false, updatable = false)
    })
    private Skill skill;

    private int skillProficiencyLevel;

    private String skillName;

    private String shiftLengthName;

    public String getShiftStructureId() {
        return shiftStructureId;
    }

    public void setShiftStructureId(String shiftStructureId) {
        this.shiftStructureId = shiftStructureId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getDurationInMins() {
        return durationInMins;
    }

    public void setDurationInMins(int durationInMins) {
        this.durationInMins = durationInMins;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public boolean isNight() {
        return night;
    }

    public void setNight(boolean night) {
        this.night = night;
    }

    public boolean isExcess() {
        return excess;
    }

    public void setExcess(boolean excess) {
        this.excess = excess;
    }

    public String getShiftLengthId() {
        return shiftLengthId;
    }

    public void setShiftLengthId(String shiftLengthId) {
        this.shiftLengthId = shiftLengthId;
    }

    public ShiftLength getShiftLength() {
        return shiftLength;
    }

    public void setShiftLength(ShiftLength shiftLength) {
        this.shiftLength = shiftLength;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public int getSkillProficiencyLevel() {
        return skillProficiencyLevel;
    }

    public void setSkillProficiencyLevel(int skillProficiencyLevels) {
        this.skillProficiencyLevel = skillProficiencyLevels;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getShiftLengthName() {
        return shiftLengthName;
    }

    public void setShiftLengthName(String shiftTypeName) {
        this.shiftLengthName = shiftTypeName;
    }

    @Override
    public ShiftReqOld clone() throws CloneNotSupportedException {
        // because of lazy init
        getSkill();
        getShiftLength();
        return (ShiftReqOld) super.clone();
    }
}
