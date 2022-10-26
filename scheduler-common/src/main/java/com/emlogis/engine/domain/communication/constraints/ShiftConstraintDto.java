package com.emlogis.engine.domain.communication.constraints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.emlogis.engine.domain.solver.RuleName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @Type(value = CITimeOffShiftConstraintDto.class, name = "ciTimeOffShiftConstraintDto"),
        @Type(value = CDTimeOffShiftConstraintDto.class, name = "cdTimeOffShiftConstraintDto"),
        @Type(value = MinShiftConstraintDto.class, name = "minShiftConstraintDto"),
        @Type(value = MaxShiftConstraintDto.class, name = "maxShiftConstraintDto"),
        @Type(value = SkillShiftConstraintDto.class, name = "skillShiftConstraintDto"),
        @Type(value = TeamShiftConstraintDto.class, name = "teamShiftConstraintDto"),
        @Type(value = DaysAfterWeekendConstraintDto.class, name = "daysAfterWeekendConstraintDto"),
        @Type(value = DaysBeforeWeekendConstraintDto.class, name = "daysBeforeWeekendConstraintDto"),
        @Type(value = CoupledWeekendConstraintDto.class, name = "coupledWeekendConstraintDto"),
        @Type(value = WeekdayRotationConstraintDto.class, name = "weekdayRotationConstraintDto")
})
public class ShiftConstraintDto implements Serializable {
    private RuleName constraintName;
    private int weight;
    private Collection<String> involvedShifts = new ArrayList<String>();

    /**
     * @return the constraintName
     */
    public RuleName getConstraintName() {
        return constraintName;
    }

    /**
     * @param constraintName the constraintName to set
     */
    public void setConstraintName(RuleName constraintName) {
        this.constraintName = constraintName;
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * @return the involvedShifts
     */
    public Collection<String> getInvolvedShifts() {
        return involvedShifts;
    }

    /**
     * @param involvedShifts the involvedShifts to set
     */
    public void setInvolvedShifts(Collection<String> involvedShifts) {
        this.involvedShifts = involvedShifts;
    }

    @Override
    public String toString() {
        return "ShiftConstraintDto [constraintName=" + constraintName + ", weight=" + weight + ", involvedShifts="
                + involvedShifts + "]";
    }


}
