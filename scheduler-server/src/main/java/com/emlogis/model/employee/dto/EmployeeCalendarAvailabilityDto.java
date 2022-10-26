package com.emlogis.model.employee.dto;

public class EmployeeCalendarAvailabilityDto extends EmployeeAvailabilityAndShiftsDto implements Cloneable {

    private AvailcalViewDto availcalViewDto;

    public AvailcalViewDto getAvailcalViewDto() {
        return availcalViewDto;
    }

    public void setAvailcalViewDto(AvailcalViewDto availcalViewDto) {
        this.availcalViewDto = availcalViewDto;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
