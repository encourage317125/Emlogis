package com.emlogis.model.dto;

public abstract class ReadDto extends Dto {

    private String clName;			// entity class name

    public String getClName() {
        return clName;
    }

    public void setClName(String clName) {
        this.clName = clName;
    }

}
