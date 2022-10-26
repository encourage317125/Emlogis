package com.emlogis.engine.domain.contract.contractline.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;

public class WeekdayRotationPatternCLDto extends PatternCLDto {

    private DayOfWeek dayOfWeek;
    private int numberOfDays;
    private int outOfTotalDays;
    private WeekdayRotationPattern.RotationPatternType rotationType;

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public int getOutOfTotalDays() {
        return outOfTotalDays;
    }

    public void setOutOfTotalDays(int outOfTotalDays) {
        this.outOfTotalDays = outOfTotalDays;
    }

    public WeekdayRotationPattern.RotationPatternType getRotationType() {
        return rotationType;
    }

    public void setRotationType(WeekdayRotationPattern.RotationPatternType rotationType) {
        this.rotationType = rotationType;
    }
}
