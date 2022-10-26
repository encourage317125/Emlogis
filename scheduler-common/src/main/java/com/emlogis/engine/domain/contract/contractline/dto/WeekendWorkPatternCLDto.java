package com.emlogis.engine.domain.contract.contractline.dto;

public class WeekendWorkPatternCLDto extends PatternCLDto {

    private String daysOffAfter;
    private String daysOffBefore;

    public String getDaysOffAfter() {
        return daysOffAfter;
    }

    public void setDaysOffAfter(String daysOffAfter) {
        this.daysOffAfter = daysOffAfter;
    }

    public String getDaysOffBefore() {
        return daysOffBefore;
    }

    public void setDaysOffBefore(String daysOffBefore) {
        this.daysOffBefore = daysOffBefore;
    }
}
