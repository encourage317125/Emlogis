package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class ShiftLengthDto extends BaseEntityDto {

    public final static String NAME = "name";

    private String name;
    private String description;
    private int lengthInMin;
    private boolean active;

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

    public int getLengthInMin() {
        return lengthInMin;
    }

    public void setLengthInMin(int lengthInMin) {
        this.lengthInMin = lengthInMin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
