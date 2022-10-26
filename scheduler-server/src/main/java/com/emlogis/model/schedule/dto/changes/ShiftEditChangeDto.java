package com.emlogis.model.schedule.dto.changes;

public class ShiftEditChangeDto extends BaseScheduleChangeDto {

    private String shiftId;

    private String previousShiftCopy;

    private String newShiftCopy;

    private String changedAtributes;

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getPreviousShiftCopy() {
        return previousShiftCopy;
    }

    public void setPreviousShiftCopy(String previousShiftCopy) {
        this.previousShiftCopy = previousShiftCopy;
    }

    public String getNewShiftCopy() {
        return newShiftCopy;
    }

    public void setNewShiftCopy(String newShiftCopy) {
        this.newShiftCopy = newShiftCopy;
    }

    public String getChangedAtributes() {
        return changedAtributes;
    }

    public void setChangedAtributes(String changedAtributes) {
        this.changedAtributes = changedAtributes;
    }
}
