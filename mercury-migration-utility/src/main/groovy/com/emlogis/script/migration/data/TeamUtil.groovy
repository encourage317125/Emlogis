package com.emlogis.script.migration.data

import groovy.json.JsonOutput
import groovy.util.logging.Log4j

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 10/24/14
 * Time: 3:42 AM
 * To change this template use File | Settings | File Templates.
 */

@Log4j
class TeamUtil {

    public static void processTeams(teamArray,sites , siteTeams)  {
        def siteTeam
        def site
        def updateDto

        // Add Merucry site id to teams
        for(team in teamArray) {

           updateDto = [:]
           updateDto.name = team.name
           updateDto.description = team.description

           team['updateDto'] = updateDto

           siteTeam = siteTeams.find{it.TeamID == team.TeamID}

           if(siteTeam) {
               site = sites.find{it.aspenId == siteTeam.SiteID}

               if(site){
                   team.siteId = site.id
               }   else {
                   log.error("Unable to find site for this site team: ${JsonOutput.toJson(siteTeam)}" )
               }

           }   else {
               log.error("Unable to find site team for this team: ${JsonOutput.toJson(team)}" )
           }
        }   // teams

    }

    public static List getMercuryEmployeeTeams(aspenTeamEmployeeList, employeeList, aspenTeamList) {
        def mercuryEmployeeTeams = []
        def mercuryEmployeeTeam
        def employee
        def team

        for(employeeTeam in aspenTeamEmployeeList) {
            mercuryEmployeeTeam =[:]

            mercuryEmployeeTeam.isFloating = employeeTeam.IsFloat
            mercuryEmployeeTeam.isSchedulable = true

            employee = employeeList.find{it.aspenId == employeeTeam.EmployeeID}
            def tempEmployee = employeeList.find{it.aspenId == 165}
            if(employee) {
                mercuryEmployeeTeam.employeeId = employee.id
                // Check for Home Team
                if(employee.HomeTeamID == employeeTeam.TeamID ) {
                    mercuryEmployeeTeam.isHomeTeam = true
                } else {
                    mercuryEmployeeTeam.isHomeTeam = false
                }

                team = aspenTeamList.find{it.TeamID == employeeTeam.TeamID}

                if(team) {
                    mercuryEmployeeTeam.teamId = team.id

                    if(!employeeTeam.IsDeleted){
                        mercuryEmployeeTeams.add(mercuryEmployeeTeam)
                    } else {
                        log.error("This team is marked in Aspen/Hickory as delted : ${JsonOutput.toJson(employeeTeam)}" )
                    }

                }   else {
                    log.error("Unable to find team for this employee team: ${JsonOutput.toJson(employeeTeam)}" )
                }

            }   else {
                log.error("Unable to find employee for this employee team: ${JsonOutput.toJson(employeeTeam)}" )
            }

        }   // employee team list

        return mercuryEmployeeTeams
    }

    public static processTeamSkills(teamSkillsList, teamList) {
        def team
        def skill

        for(teamSkill in teamSkillsList) {
            // find team
            team = teamList.find{it.TeamID == teamSkill.TeamID}

            if(team){
                teamSkill.teamId = team.id

                // find skill
                skill = SkillUtil.getUniqueSkill(teamSkill.SkillID)

                if(skill) {
                    teamSkill.skillId = skill.id

                } else {
                    log.error("Unable to find skill for this teamskill: ${JsonOutput.toJson(teamSkill)}" )
                }
            }  else {
                log.error("Unable to find team for this teamskill: ${JsonOutput.toJson(teamSkill)}" )
            }
        }
    } // process team skills
}
