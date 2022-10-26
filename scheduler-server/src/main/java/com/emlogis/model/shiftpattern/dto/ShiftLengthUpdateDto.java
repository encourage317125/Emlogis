package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.UpdateDto;

public class ShiftLengthUpdateDto extends UpdateDto {

    private String name;
    private String description;
    private Integer lengthInMin;
    private Integer paidTimeInMin;
    private boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLengthInMin() {
        return lengthInMin;
    }

    public void setLengthInMin(Integer lengthInMin) {
        this.lengthInMin = lengthInMin;
    }

    public Integer getPaidTimeInMin() {
        return paidTimeInMin;
    }

    public void setPaidTimeInMin(Integer paidTimeInMin) {
        this.paidTimeInMin = paidTimeInMin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
