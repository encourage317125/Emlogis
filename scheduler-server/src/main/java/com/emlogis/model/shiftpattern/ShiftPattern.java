package com.emlogis.model.shiftpattern;

import com.emlogis.common.ModelUtils;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.structurelevel.Team;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ShiftPattern extends BaseEntity {

    private String name;

    private String description;

    private DayOfWeek dayOfWeek;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime cdDate;

    private ShiftPatternType type;

    private int maxEmployeeCount;

    private String shiftLengthList;

    private boolean isShiftStructureGenerated;
    
    private boolean	isValid = false;			// an indication that pattern has all info to be currently used in a schedule
    											// ie: if Manual pattern, must have at least one ShiftReq
    											//     if Demand pattern, must have ShiftReqs generated and at least 1 ShiftReq

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "skillId", referencedColumnName = "id"),
            @JoinColumn(name = "skillTenantId", referencedColumnName = "tenantId")
    })
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "teamId", referencedColumnName = "id"),
            @JoinColumn(name = "teamTenantId", referencedColumnName = "tenantId")
    })
    private Team team;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shiftPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PatternElt> patternElts;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shiftPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShiftReq> shiftReqs;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shiftPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShiftDemand> shiftDemands;

    public ShiftPattern() {}

    public ShiftPattern(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public DateTime getCdDate() {
        return cdDate;
    }

    public void setCdDate(DateTime cdDate) {
        this.cdDate = ModelUtils.cutDateIfLessPredefined(cdDate);
    }

    public ShiftPatternType getType() {
        return type;
    }

    public void setType(ShiftPatternType type) {
        this.type = type;
    }

    public int getMaxEmployeeCount() {
        return maxEmployeeCount;
    }

    public void setMaxEmployeeCount(int maxEmployeeCount) {
        this.maxEmployeeCount = maxEmployeeCount;
    }

    public String getShiftLengthList() {
        return shiftLengthList;
    }

    public void setShiftLengthList(String shiftLengthList) {
        this.shiftLengthList = shiftLengthList;
    }

    public boolean isShiftStructureGenerated() {
        return isShiftStructureGenerated;
    }

    public void setShiftStructureGenerated(boolean isShiftStructureGenerated) {
        this.isShiftStructureGenerated = isShiftStructureGenerated;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Set<ShiftReq> getShiftReqs() {
        return shiftReqs;
    }

    public void setShiftReqs(Set<ShiftReq> shiftReqs) {
        this.shiftReqs = shiftReqs;
    }

    public Set<ShiftDemand> getShiftDemands() {
        return shiftDemands;
    }

    public void setShiftDemands(Set<ShiftDemand> shiftDemands) {
        this.shiftDemands = shiftDemands;
    }

    public Set<PatternElt> getPatternElts() {
        return patternElts;
    }

    public void setPatternElts(Set<PatternElt> patternElts) {
        this.patternElts = patternElts;
    }

	public boolean isValid() {
		return isValid;
	}

	public String getTeamId() {
		return team.getId();
	}
	
}
