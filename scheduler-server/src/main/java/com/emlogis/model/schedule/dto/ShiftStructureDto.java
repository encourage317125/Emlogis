package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class ShiftStructureDto extends BaseEntityDto {

    public final static String START_DATE = "startDate";

    private long startDate;

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

}
