package com.emlogis.model.structurelevel.dto;

import com.emlogis.model.employee.dto.SkillDto;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Andrii Mozharovskyi on 9/24/15.
 */
public class TeamSkillsDto extends TeamDto {
    private Collection<SkillDto> skills;

    public Collection<SkillDto> getSkills() {
        return skills;
    }

    public void setSkills(Collection<SkillDto> skills) {
        this.skills = skills;
    }
}
