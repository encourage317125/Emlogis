package com.emlogis.model.schedule.dto.changes;

public class ShiftAddChangeDto extends BaseScheduleChangeDto {

    private String shiftId;

    private String shiftCopy;

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftCopy() {
        return shiftCopy;
    }

    public void setShiftCopy(String shiftCopy) {
        this.shiftCopy = shiftCopy;
    }
}
