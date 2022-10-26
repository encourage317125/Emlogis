package com.emlogis.script.migration.data

import com.emlogis.script.migration.MigrationConstants
import groovy.json.JsonOutput
import groovy.util.logging.Log4j
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.LocalTime
import org.joda.time.Minutes

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 11/3/14
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 */

@Log4j
class ScheduleUtil {
    public  static processShiftStructures(shiftStructureList, teamsList) {
        def team

        for(shiftStructure in shiftStructureList) {
            // find team
            team = teamsList.find{it.TeamID == shiftStructure.TeamID}
            DateTime clientDateTime

            if(team) {
                shiftStructure.teamId = team.id

                if(shiftStructure.StartDate) {
                    clientDateTime = DateTime.parse(shiftStructure.StartDate)
                    shiftStructure.startDate = clientDateTime.getMillis()
                } else {
                    log.error( "Blank start date found for Aspen Scheduled Team : " + JsonOutput.toJson(shiftStructure) )
                }
            }  else {
                log.error( "No matching team found for Aspen Scheduled Team : " + JsonOutput.toJson(shiftStructure) )
            }
        }
    }

    public static processSchedules(scheduleList, scheduledTeamList, migrateScheduleReqOnly) {
        def updateDto
        def teamList, teamIds
        def updateFields = ['name','description']
        DateTime startDateTime
        DateTime endDateTime

        for(schedule in scheduleList) {
          schedule.scheduleType = 'ShiftStructureBased'

          if(schedule.StartDate) {
              startDateTime = DateTime.parse(schedule.StartDate)
              schedule.startDate = startDateTime.getMillis()

              if(schedule.EndDate) {
                  endDateTime = DateTime.parse(schedule.EndDate)
                  schedule.scheduleLengthInDays = Days.daysBetween(startDateTime, endDateTime).getDays() + 1
              }

              updateDto = [:]
              if(!migrateScheduleReqOnly) {
                  if(schedule.posted)
                  {
                      updateDto.status = 'Posted'
                  }  else {
                      updateDto.status = 'Production'
                  }
              }

              updateFields.each {
                  updateDto[it] = schedule.remove(it)
              }

              teamList = scheduledTeamList.findAll{it.SiteScheduleID == schedule.SiteScheduleID}
              teamIds = teamList.collect{it.teamId}
              schedule.put('teamIds', teamIds)

              schedule.put('updateDto', updateDto)

          } else {
              log.error( "No start date for Aspen Schedule Team : " + JsonOutput.toJson(schedule) )
          }
        }

    }

    public static void processScheduleRequirements(shiftRequirementList, shiftStructureList, shiftLengthList) {
        def shiftStructure, skill, shiftLength
        def timeList, scheduleDate, reqDate
        boolean addDayDueToMidnight


        for(shiftReq in shiftRequirementList) {

            addDayDueToMidnight = false

             shiftStructure = shiftStructureList.find{it.SiteScheduleID == shiftReq.SiteScheduleID &&
                it.TeamID == shiftReq.TeamID}

            if(shiftStructure) {
                shiftReq.shiftStructureId = shiftStructure.id
                if(shiftReq.shift_startTime >= 2400) {
                    log.info("Had to add a day due to shiftReq starttime of ${shiftReq.shift_startTime}  ")
                    shiftReq.shift_startTime = 0
                    addDayDueToMidnight = true
                }

                shiftReq.startTime = TimeUtil.get24HourTime(shiftReq.shift_startTime).getMillisOfDay()

                scheduleDate = DateTime.parse(shiftReq.StartDate)

                reqDate = DateTime.parse(shiftReq.Require_Date)

                reqDate = (addDayDueToMidnight) ? reqDate.plusDays(1) : reqDate;

                shiftReq.dayIndex = Days.daysBetween(scheduleDate, reqDate).getValue()

                shiftReq.employeeCount = 1

                skill = SkillUtil.getUniqueSkill(shiftReq.SkillID)

                if(skill) {
                    shiftReq.skillId = skill.id
                    shiftReq.skillName = skill.name
                } else {
                    log.error( "Could not find a skill for Aspen Shift Req : " + JsonOutput.toJson(shiftReq) )
                }

                shiftLength = getUniqueShiftLength(shiftLengthList, shiftReq.ShiftGroupID)

               if(shiftLength) {
                   shiftReq.shiftLengthId = shiftLength.id
                   shiftReq.shiftLengthName = shiftLength.name
                   shiftReq.durationInMins =  shiftLength.updateDto.lengthInMin
               }else {
                   log.error( "Could not find a shift type for Aspen Shift Req : " + JsonOutput.toJson(shiftReq) )
               }
            } else {
                log.error( "Could not find a shift structure for Aspen Shift Req : " + JsonOutput.toJson(shiftReq) )
            }
        }
    }

    public static void processShiftAssignments(shiftRequirementList, shiftStructureList, shiftLengthList,
        employeeList, scheduleList, siteArray) {

        def shiftStructure, skill, shiftLength, timeList, employee, schedule, site

        DateTime startDateTime
        DateTime mercuryDateTime


        for(shift in shiftRequirementList) {

            schedule = scheduleList.find{it.SiteScheduleID == shift.SiteScheduleID}

            if(schedule) {
                shift.scheduleId = schedule.id
            }   else {
                log.error( "Could not find a schedule for Aspen Shift  : " + JsonOutput.toJson(shift) )
                continue;
            }

            shift.scheduleStatus = schedule.updateDto.status
            // fiond the site
            site = siteArray.find{it.aspenId == shift.SiteID }

            shift.siteName = site.updateDto.name

            shiftStructure = shiftStructureList.find{it.SiteScheduleID == shift.SiteScheduleID &&
                    it.TeamID == shift.TeamID}

            if(shiftStructure) {
                shift.shiftStructureId = shiftStructure.id
                shift.teamId = shiftStructure.teamId
            }  else {
                log.error( "Could not find a shift structure for Aspen Shift  : " + JsonOutput.toJson(shift) )
            }

            skill =  SkillUtil.getUniqueSkill(shift.SkillID)

            if(skill) {
                shift.skillId = skill.id
            } else {
                log.error( "Could not find a skill for Aspen Shift : " + JsonOutput.toJson(shift) )
            }

            shiftLength = getUniqueShiftLength(shiftLengthList, shift.ShiftGroupID)

            if(shiftLength) {
                shift.shiftLengthId = shiftLength.id
                shift.shiftLengthName = shiftLength.name
                shift.paidTime = shiftLength.updateDto.paidTimeInMin

                startDateTime = TimeUtil.getStartDateTime(shift.Require_Date, shift.shift_startTime)
                mercuryDateTime = startDateTime

                shift.startDateTime =  startDateTime.getMillis()
                shift.endDateTime =   (mercuryDateTime.plusMinutes((int)shiftLength.updateDto.lengthInMin)).getMillis()
                log.info ""
            }else {
                log.error( "Could not find a shift length for Aspen Shift : " + JsonOutput.toJson(shift) )
            }

            employee = employeeList.find{it.aspenId == shift.EmployeeID}

            if(employee) {
                shift.employeeId = employee.id
            }
//            else {
//                log.info( "Could not find an employee for Aspen Shift : " + JsonOutput.toJson(shift) )
//            }

        }
    }

    public static List processShiftLengths(shiftLengthsList, sitesList, sitesToIgnore) {

        def updateDto
        def aspenSiteId
        def uniqueShiftLengthList = []
        def duplicateShiftLength = [:]

        shiftLengthsList.removeAll{ sitesToIgnore.contains(it.SiteId) }


        for(shiftLength in shiftLengthsList) {
            updateDto = [:]
            updateDto.lengthInMin = (shiftLength.lengthInMin * MigrationConstants.MINUTES_PER_HUOR) / MigrationConstants.ASPEN_HOUR_MULTIPLIER
            updateDto.paidTimeInMin = (shiftLength.paidTimeInMin * MigrationConstants.MINUTES_PER_HUOR) / MigrationConstants.ASPEN_HOUR_MULTIPLIER

            shiftLength.remove('lengthInMin')
            shiftLength.remove('paidTimeInMin')

            shiftLength.put('updateDto', updateDto)

            aspenSiteId = shiftLength.SiteId

            shiftLength.mercurySiteId =  MigrationData.getMercurySiteId(aspenSiteId, sitesList)

            duplicateShiftLength = uniqueShiftLengthList.find{it.updateDto.lengthInMin == shiftLength.updateDto.lengthInMin && it.mercurySiteId == shiftLength.mercurySiteId}

            if(duplicateShiftLength) {
                // don't add to unique list
                shiftLength.unique = duplicateShiftLength.aspenId
            }  else {
               //  add to unique list
                shiftLength.unique = null
                uniqueShiftLengthList.add(shiftLength)
            }
        }
        uniqueShiftLengthList
    }

    public static void processShiftTypes(shiftTypeList, shiftLengthsList, sitesList, sitesToIgnore) {
        def shiftLength
        def updateDto
        LocalTime startTime
        def mercuryId
        def siteId

        // remove shift types for ignored sites

        shiftTypeList.removeAll{ sitesToIgnore.contains(it.SiteId) }

        for(shiftType in shiftTypeList) {
            siteId = shiftType.SiteId

            updateDto = [:]
            updateDto.name =  shiftType.remove('name')

            shiftLength= getUniqueShiftLength(shiftLengthsList, shiftType.ShiftGroupID)

            if(shiftLength) {
                shiftType.shiftLengthId = shiftLength.id
            } else {
                log.error( "Could not find a shift length for Aspen Shift : " + JsonOutput.toJson(shiftType) )
            }

            if(shiftType.StartTime >= 2400) shiftType.StartTime = 0

            startTime = TimeUtil.get24HourTime(shiftType.StartTime)
            updateDto.startTime = startTime.getMillisOfDay()

            shiftType.put('updateDto', updateDto)

            shiftType.mercurySiteId =  MigrationData.getMercurySiteId(shiftType.SiteId, sitesList)
        }

    }

    public static Map getUniqueShiftLength(shiftLengthsList, id) {
        def shiftLength = null

        shiftLength = shiftLengthsList.find{it.aspenId == id}

        if(shiftLength.unique != null) {
            shiftLength = shiftLengthsList.find{it.aspenId == shiftLength.unique}
        }

        shiftLength
    }
}
