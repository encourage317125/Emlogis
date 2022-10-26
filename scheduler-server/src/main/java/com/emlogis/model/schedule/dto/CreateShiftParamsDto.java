package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.schedule.ManageShiftAction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class CreateShiftParamsDto implements Serializable {

    private ShiftCreateDto shiftInfo;
    private String comment;
    private ManageShiftAction action;
    private boolean force;
    private Map<ConstraintOverrideType, Boolean> overrideOptions;
    private String employeeId;
    private Collection<String> employeeIds;

    public ShiftCreateDto getShiftInfo() {
        return shiftInfo;
    }

    public void setShiftInfo(ShiftCreateDto shiftInfo) {
        this.shiftInfo = shiftInfo;
    }

    public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ManageShiftAction getAction() {
        return action;
    }

    public void setAction(ManageShiftAction action) {
        this.action = action;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
        return overrideOptions;
    }

    public void setOverrideOptions(Map<ConstraintOverrideType, Boolean> overrideOptions) {
        this.overrideOptions = overrideOptions;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

	public Collection<String> getEmployeeIds() {
		return employeeIds;
	}

	public void setEmployeeIds(Collection<String> employeeIds) {
		this.employeeIds = employeeIds;
	}
}
