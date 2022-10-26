package com.emlogis.engine.domain.dto;

import java.io.Serializable;

public class SkillDto implements Serializable {

    private String id;
    private String name;
    private String abbreviation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
}
