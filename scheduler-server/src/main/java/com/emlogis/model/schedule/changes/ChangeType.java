package com.emlogis.model.schedule.changes;

/**
 * Created by emlogis on 7/10/14.
 */
public enum ChangeType {

    WIP (ChangeCategory.AssignmentChange, "Wip"),
    SWAP (ChangeCategory.AssignmentChange, "Swap"),
    SHIFTDROP (ChangeCategory.AssignmentChange, "ShiftDrop"),
    SHIFTASSIGN (ChangeCategory.AssignmentChange, "ShiftAssign"),
    SHIFTEDIT (ChangeCategory.ShiftChange, "ShiftEdit"),
    SHIFTDELETE (ChangeCategory.ShiftChange, "ShiftDelete"),
    SHIFTADD (ChangeCategory.ShiftChange, "ShiftAdd"),

    SCHEDULECREATE (ChangeCategory.ScheduleChange, "ScheduleCreate"),
    SCHEDULERUN (ChangeCategory.ScheduleChange, "ScheduleRun"),
    SCHEDULERERUN (ChangeCategory.ScheduleChange, "ScheduleRerun"),
    SCHEDULECLEAR (ChangeCategory.ScheduleChange, "ScheduleClear"),
    SCHEDULEDELETE (ChangeCategory.ScheduleChange, "ScheduleDelete"),
    SCHEDULEPROMOTE (ChangeCategory.ScheduleChange, "SchedulePromote"),
    SCHEDULEPOST (ChangeCategory.ScheduleChange, "SchedulePost");

    private ChangeCategory category;

    private String value;

    private ChangeType(ChangeCategory category, String value) {
        this.category = category;
        this.value = value;
    }

    private void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isInChangeCategory(ChangeCategory category) {
        return this.category == category;
    }

}
