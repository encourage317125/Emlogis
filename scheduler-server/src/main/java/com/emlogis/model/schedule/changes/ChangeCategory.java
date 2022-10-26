package com.emlogis.model.schedule.changes;

/**
 * Created by emlogis on 7/10/14.
 */
public enum ChangeCategory {

    AssignmentChange("AssignmentChange"),
    ShiftChange("ShiftChange"),
    ScheduleChange("ScheduleChange");

    private String value;

    private ChangeCategory(String value) {
        this.value = value;
    }

    private void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
