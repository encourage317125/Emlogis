package com.emlogis.model.workflow.dto.reports.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 2/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflReportActorDto extends WflReportBaseActorDto implements Serializable {

    private String shiftId;
    private Long shiftStartTime;
    private Long shiftEndTime;
    private String shiftName;
    private Long shiftDate;
    private String shiftSkill;

    public WflReportActorDto() {
        super();
    }

    public WflReportActorDto(String name, String teamName, String status) {
        super(name, teamName, status);
    }

    public WflReportActorDto(
            String name, String teamName, String shiftId, Long shiftStartTime, Long shiftEndTime, String shiftName, Long shiftDate,
            String shiftSkill, String status) {
        super(name, teamName, status);
        this.shiftId = shiftId;
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shiftEndTime;
        this.shiftName = shiftName;
        this.shiftDate = shiftDate;
        this.shiftSkill = shiftSkill;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public Long getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(Long shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public Long getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(Long shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public Long getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(Long shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftSkill() {
        return shiftSkill;
    }

    public void setShiftSkill(String shiftSkill) {
        this.shiftSkill = shiftSkill;
    }
}
