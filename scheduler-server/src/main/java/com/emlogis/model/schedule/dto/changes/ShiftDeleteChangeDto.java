package com.emlogis.model.schedule.dto.changes;

public class ShiftDeleteChangeDto extends BaseScheduleChangeDto {

    private String shiftId;

    private String previousShiftCopy;

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
}
