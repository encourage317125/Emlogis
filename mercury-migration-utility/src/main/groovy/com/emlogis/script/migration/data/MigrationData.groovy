package com.emlogis.script.migration.data

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 9/24/14
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */
class MigrationData {
    // Aspen Shift Types
    def shiftLengths

    // Aspen Shifts
    def shiftTypes
    def skills
    def skillsFromReqs
    def absenceTypes
    def employees
    def sites
    def userAccounts
    def adminUserAccounts
    def weekDayRotations
    def weekendWorkPatterns
    def employeeSkills
    def aspenCDAvailability
    def aspenCIAvailability
    def aspenTeamEmployees
    def aspenTeams
    def aspenSiteTeams
    def teamSkills
    def schedules
    def aspenScheduledTeams
    def shiftRequirements

    public static String getMercurySiteId(aspenId, entityList) {
       def mercuryId
       for(entity in entityList) {
           if(entity.aspenId == aspenId) {
             mercuryId = entity.id
             break
           }
       }
        return mercuryId
    }


}
