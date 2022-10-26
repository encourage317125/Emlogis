package com.emlogis.model.employee.dto;

public class EmployeeSkillAssociationDto {

    private String skillId;
    private boolean isPrimarySkill;
    private int skillScore;

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public boolean isPrimarySkill() {
        return isPrimarySkill;
    }

    public void setPrimarySkill(boolean isPrimarySkill) {
        this.isPrimarySkill = isPrimarySkill;
    }

    public int getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(int skillScore) {
        this.skillScore = skillScore;
    }
}
