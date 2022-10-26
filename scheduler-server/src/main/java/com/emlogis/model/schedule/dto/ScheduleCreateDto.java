package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.CreateDto;
import com.emlogis.model.schedule.ScheduleType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleCreateDto extends CreateDto<ScheduleUpdateDto> implements Serializable {

    public static final String START_DATE = "startDate";
    public static final String NAME = UPDATE_DTO + ".name";
    
	private	ScheduleType scheduleType = ScheduleType.ShiftPatternBased;
    private long startDate;
    private int	scheduleLengthInDays = 7;
    private Set<String> teamIds;

    public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(ScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}

	public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public int getScheduleLengthInDays() {
		return scheduleLengthInDays;
	}

	public void setScheduleLengthInDays(int scheduleLengthInDays) {
		this.scheduleLengthInDays = scheduleLengthInDays;
	}

	public Set<String> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(Set<String> teamIds) {
        this.teamIds = teamIds;
    }
}
