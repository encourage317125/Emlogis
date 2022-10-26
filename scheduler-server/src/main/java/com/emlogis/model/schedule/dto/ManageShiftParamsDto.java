package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.schedule.ManageShiftAction;

import java.util.Collection;
import java.util.Map;

public class ManageShiftParamsDto {

    private String comment;
    private boolean force;
    private Map<ConstraintOverrideType, Boolean> overrideOptions;
    private ShiftInfo shiftInfo;
    private OpenShiftInfo osShiftInfo;

    public static class ShiftInfo {

        private long newStartDateTime;
        private long newEndDateTime;

        public long getNewStartDateTime() {
            return newStartDateTime;
        }

        public void setNewStartDateTime(long newStartDateTime) {
            this.newStartDateTime = newStartDateTime;
        }

        public long getNewEndDateTime() {
            return newEndDateTime;
        }

        public void setNewEndDateTime(long newEndDateTime) {
            this.newEndDateTime = newEndDateTime;
        }
    }

    public static class OpenShiftInfo {

        private ManageShiftAction action;
        private long startDateTime;
        private long endDateTime;
        private String employeeId;
        private Collection<String> employeeIds;
        private String absenceTypeId;

        public ManageShiftAction getAction() {
            return action;
        }

        public void setAction(ManageShiftAction action) {
            this.action = action;
        }

        public long getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(long startDateTime) {
            this.startDateTime = startDateTime;
        }

        public long getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(long endDateTime) {
            this.endDateTime = endDateTime;
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

        public String getAbsenceTypeId() {
            return absenceTypeId;
        }

        public void setAbsenceTypeId(String absenceTypeId) {
            this.absenceTypeId = absenceTypeId;
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public ShiftInfo getShiftInfo() {
        return shiftInfo;
    }

    public void setShiftInfo(ShiftInfo shiftInfo) {
        this.shiftInfo = shiftInfo;
    }

    public OpenShiftInfo getOsShiftInfo() {
        return osShiftInfo;
    }

    public void setOsShiftInfo(OpenShiftInfo osShiftInfo) {
        this.osShiftInfo = osShiftInfo;
    }
}
