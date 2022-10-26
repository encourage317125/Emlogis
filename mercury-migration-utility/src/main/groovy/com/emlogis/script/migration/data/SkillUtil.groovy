package com.emlogis.script.migration.data

import groovy.util.logging.Log4j

/**
 * Created by rjackson on 1/27/2015.
 */

@Log4j
class SkillUtil {

    def static uniqueSkillMap = [:]
    def static uniqueSkillList = []

    public static List processSkills(List legacySkillsList, List reqSkillList) {

        def  tempSkill
        def tempName
        def tempAbbrev

        //Default skill description for now

        for (skill in legacySkillsList) {
            if (!skill.description) {
                skill.description = skill.name
            }
            skill.isActive = true
            skill.name = skill.name.trim()
            skill.abbreviation = skill.abbreviation.trim()
        }

        // add the remaining skills to the list

        tempSkill = null

        for(skill in legacySkillsList) {

            tempSkill = uniqueSkillList.find{ ( it.name.equalsIgnoreCase(skill.name) ) ||
                (it.abbreviation.equalsIgnoreCase(skill.abbreviation) )
            }

            // Check  if the skill should map to a priority skill
            if(tempSkill) {
                uniqueSkillMap.put(skill.aspenId, tempSkill.aspenId)
            }  else {
                uniqueSkillList.add(skill)
                uniqueSkillMap.put(skill.aspenId, skill.aspenId)
            }
        }
        return uniqueSkillList
    }

    public static  getUniqueSkill(skillId) {
        def skill = null

        def uniqueId

        if(skillId) {
           uniqueId = uniqueSkillMap.get(skillId)

            if(uniqueId) {
                skill = uniqueSkillList.find{it.aspenId == uniqueId}

            }else {
                log.error("No matching unique skill was found in getUniqueSkill ")
            }
        } else {
            log.error("This skill Id passed to getUniqueSkill is null/emtpy ")
        }

        return skill;
    }
}
