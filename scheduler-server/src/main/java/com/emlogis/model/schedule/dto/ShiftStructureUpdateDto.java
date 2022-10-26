package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.UpdateDto;

public class ShiftStructureUpdateDto extends UpdateDto {

    private long startDate;

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

}
