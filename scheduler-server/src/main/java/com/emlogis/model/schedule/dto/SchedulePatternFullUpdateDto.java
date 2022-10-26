package com.emlogis.model.schedule.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchedulePatternFullUpdateDto implements Serializable {

    private List<String> teamIds;

    private List<SchedulePatternDto> schedulePatternDtos;

    private ScheduleUpdateDto scheduleUpdateDto;

    public List<String> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(List<String> teamIds) {
        this.teamIds = teamIds;
    }

    public List<SchedulePatternDto> getSchedulePatternDtos() {
        return schedulePatternDtos;
    }

    public void setSchedulePatternDtos(List<SchedulePatternDto> schedulePatternDtos) {
        this.schedulePatternDtos = schedulePatternDtos;
    }

    public ScheduleUpdateDto getScheduleUpdateDto() {
        return scheduleUpdateDto;
    }

    public void setScheduleUpdateDto(ScheduleUpdateDto scheduleUpdateDto) {
        this.scheduleUpdateDto = scheduleUpdateDto;
    }
}
