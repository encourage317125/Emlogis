package com.emlogis.model.workflow.notification.common;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

import static com.emlogis.workflow.WflUtil.messagePrm;

/**
 * Created by user on 23.07.15.
 */
public final class MessageParametersShiftInfo implements Serializable {

    private MessageEmployeeInfo owner;
    private String shiftId;
    private String shiftDate;
    private String shiftStartTime;
    private String shiftEndTime;
    private String shiftTeamId;
    private String shiftTeamName;
    private String shiftSkillId;
    private String shiftSkillName;


    public MessageParametersShiftInfo(
            MessageEmployeeInfo owner,
            String shiftId,
            String shiftTeamId,
            String shiftTeamName,
            String shiftSkillId,
            String shiftSkillName,
            String shiftDate,
            String shiftStartTime,
            String shuftEndTime
    ) {
        this.owner = owner;
        this.shiftId = shiftId;
        this.shiftTeamId = shiftTeamId;
        this.shiftTeamName = shiftTeamName;
        this.shiftSkillId = shiftSkillId;
        this.shiftSkillName = shiftSkillName;
        this.shiftDate = shiftDate;
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shuftEndTime;
    }

    public void setOwner(MessageEmployeeInfo owner) {
        this.owner = owner;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public void setShiftTeamId(String shiftTeamId) {
        this.shiftTeamId = shiftTeamId;
    }

    public void setShiftTeamName(String shiftTeamName) {
        this.shiftTeamName = shiftTeamName;
    }

    public void setShiftSkillId(String shiftSkillId) {
        this.shiftSkillId = shiftSkillId;
    }

    public void setShiftSkillName(String shiftSkillName) {
        this.shiftSkillName = shiftSkillName;
    }

    public MessageEmployeeInfo getOwner() {
        return owner;
    }

    public String getShiftId() {
        return shiftId;
    }

    public String getShiftTeamId() {
        return shiftTeamId;
    }

    public String getShiftTeamName() {
        return shiftTeamName;
    }

    public String getShiftSkillId() {
        return shiftSkillId;
    }

    public String getShiftSkillName() {
        return shiftSkillName;
    }

    public String getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(String shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(String shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public String getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(String shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageParametersShiftInfo) {
            MessageParametersShiftInfo other = (MessageParametersShiftInfo) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getOwner(), other.getOwner());
            builder.append(getShiftId(), other.getShiftId());
            builder.append(getShiftDate(), other.getShiftDate());
            builder.append(getShiftStartTime(), other.getShiftStartTime());
            builder.append(getShiftEndTime(), other.getShiftEndTime());
            builder.append(getShiftTeamId(), other.getShiftTeamId());
            builder.append(getShiftTeamName(), other.getShiftTeamName());
            builder.append(getShiftSkillId(), other.getShiftSkillId());
            builder.append(getShiftSkillName(), other.getShiftSkillName());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getOwner());
        builder.append(getShiftId());
        builder.append(getShiftDate());
        builder.append(getShiftStartTime());
        builder.append(getShiftEndTime());
        builder.append(getShiftTeamId());
        builder.append(getShiftTeamName());
        builder.append(getShiftSkillId());
        builder.append(getShiftSkillName());
        return builder.toHashCode();
    }
}
