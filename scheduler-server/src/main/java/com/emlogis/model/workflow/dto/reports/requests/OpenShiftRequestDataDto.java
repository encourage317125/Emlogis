package com.emlogis.model.workflow.dto.reports.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 2/27/15.
 */
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenShiftRequestDataDto implements Serializable {

    private Long shiftStartTime;

    private Long shiftEndTime;
    //@XmlElement(name = "skill")
    private String skill;
   // @XmlElement(name = "date")
    private Long date;
    private String status;

    public OpenShiftRequestDataDto() {
    }

    public OpenShiftRequestDataDto(Long shiftStartTime, Long shiftEndTime, String skill, Long date, String status) {
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shiftEndTime;
        this.skill = skill;
        this.date = date;
        this.status = status;
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

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
