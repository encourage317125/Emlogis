package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.CreateDto;

public class ShiftStructureCreateDto extends CreateDto<ShiftStructureUpdateDto> {

    public static final String START_DATE = "startDate";
    public static final String TEAM_ID = "teamId";

    private long startDate;
    private String teamId;

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
