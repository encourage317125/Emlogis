package com.emlogis.model.schedule.dto;

import com.emlogis.model.schedule.AssignmentMode;

import java.io.Serializable;

public class ScheduleDuplicateDto implements Serializable {

	private String name;

    private long startDate;

    private AssignmentMode mode = AssignmentMode.NOASSIGNMENT;

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public AssignmentMode getMode() {
        return mode;
    }

    public void setMode(AssignmentMode mode) {
        this.mode = mode;
    }
}
