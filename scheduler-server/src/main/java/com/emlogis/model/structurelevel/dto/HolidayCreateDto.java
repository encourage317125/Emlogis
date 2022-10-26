package com.emlogis.model.structurelevel.dto;

import com.emlogis.model.dto.CreateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayCreateDto extends CreateDto<HolidayUpdateDto> implements Serializable {

    public static final String EFFECTIVE_START_DATE = "effectiveStartDate";
    public static final String UPDATE_EFFECTIVE_START_DATE = "updateDto.effectiveStartDate";
    public static final String NAME = "name";

    private	String name;
    private long effectiveStartDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(long effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }
}
