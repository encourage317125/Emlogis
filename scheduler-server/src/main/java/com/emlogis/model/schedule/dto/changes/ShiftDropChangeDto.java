package com.emlogis.model.schedule.dto.changes;

public class ShiftDropChangeDto extends BaseScheduleChangeDto {

    private String droppedShiftId;

    private String droppedShiftCopy;

    public String getDroppedShiftId() {
        return droppedShiftId;
    }

    public void setDroppedShiftId(String droppedShiftId) {
        this.droppedShiftId = droppedShiftId;
    }

    public String getDroppedShiftCopy() {
        return droppedShiftCopy;
    }

    public void setDroppedShiftCopy(String droppedShiftCopy) {
        this.droppedShiftCopy = droppedShiftCopy;
    }
}
