package com.emlogis.engine.domain.dto;

import java.io.Serializable;

public class EmployeeSkillDto implements Serializable {

    private String employeeId;
    private String skillId;
    private String skillLevel;
    private boolean isPrimarySkill;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public boolean isPrimarySkill() {
        return isPrimarySkill;
    }

    public void setPrimarySkill(boolean isPrimarySkill) {
        this.isPrimarySkill = isPrimarySkill;
    }
}
