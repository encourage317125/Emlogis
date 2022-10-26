package com.emlogis.model.structurelevel.dto;

import com.emlogis.model.dto.UpdateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayUpdateDto extends UpdateDto implements Serializable {

    private	String name;
    private	String abbreviation;
    private	String description;
    private int timeToDeductInMin;
    private long effectiveStartDate;
    private long effectiveEndDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTimeToDeductInMin() {
		return timeToDeductInMin;
	}

	public void setTimeToDeductInMin(int timeToDeductInMin) {
		this.timeToDeductInMin = timeToDeductInMin;
	}

	public long getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(long effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public long getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(long effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }
}
