package com.emlogis.script.migration.data

import com.emlogis.script.migration.MigrationConstants
import groovy.util.logging.Log4j
import org.joda.time.DateTime

/**
 * Created by rjackson on 1/13/2015.
 */

@Log4j
class SiteUtil {

    def static updateFields = ['twoWeeksOvertimeStartDate','timeOffBetweenShifts','shiftIncrements','shiftOverlaps',
        'timeZone','description','firstDayOfWeek','isNotificationEnabled','name']
    
    public static List getContractLines (siteArray) {

        def contractLineList = []
        
        for(siteMap in siteArray) {

            // check for Daily Overtime Contraint
            if(siteMap.BeginOverTimeDay && siteMap.BeginOverTimeDay > MigrationConstants.emptyValue &&
                    siteMap.BeginOverTimeDay < MigrationConstants.naValue ) {
                def contractLine = [:]
                contractLine.contractLineType =  'DAILY_OVERTIME'
                contractLine.name = 'DAILY_OVERTIME'
                contractLine.contractId = siteMap.contractId
                contractLine.siteId = siteMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = siteMap.BeginOverTimeDay
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

            // check for Daily Overtime Contraint
            if(siteMap.BeginOverTimeWeek && siteMap.BeginOverTimeWeek > MigrationConstants.emptyValue &&
                    siteMap.BeginOverTimeWeek < MigrationConstants.naValue) {
                def contractLine = [:]
                contractLine.contractLineType =  'WEEKLY_OVERTIME'
                contractLine.name = 'WEEKLY_OVERTIME'
                contractLine.contractId = siteMap.contractId
                contractLine.siteId = siteMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = siteMap.BeginOverTimeWeek
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

            // check for Daily Overtime Contraint
            if(siteMap.BeginOverTimeTwoWeek && siteMap.BeginOverTimeTwoWeek > MigrationConstants.emptyValue &&
                    siteMap.BeginOverTimeTwoWeek < MigrationConstants.naValue ) {
                def contractLine = [:]
                contractLine.contractLineType =  'TWO_WEEK_OVERTIME'
                contractLine.name = 'TWO_WEEK_OVERTIME'
                contractLine.contractId = siteMap.contractId
                contractLine.siteId = siteMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = siteMap.BeginOverTimeTwoWeek
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

        }   // Iterate though employee array

        return contractLineList
    }
    
    public static  getSiteConstraintValue (legacySiteValue)   {
        def retVal  = null

        if(legacySiteValue != null &&
                legacySiteValue != MigrationConstants.naValue &&
                legacySiteValue != MigrationConstants.emptyValue) {

            retVal =  legacySiteValue
        }
        
        return retVal
    }

    public static void processSiteList(siteArray, clientTimeZoneId) {
        def tempVal
        def updateDto

        for (site in siteArray) {
            if (site.OvertimeStartDate) {
                site.twoWeeksOvertimeStartDate = DateTime.parse(site.OvertimeStartDate).getMillis()
            }

            // It's hours in Hickory and minutes in Mercury
            tempVal = SiteUtil.getSiteConstraintValue(site.HoursOffBetweenDays)
            site.timeOffBetweenShifts = tempVal ? (tempVal * 60) : 0

            site.isNotificationEnabled = site.EnableNotifications ? site.EnableNotifications : false

            tempVal = SiteUtil.getSiteConstraintValue(site.ShiftDuration)
            site.shiftIncrements = tempVal ? TimeUtil.getMinutesFromLegacyValue(tempVal) : 0
            tempVal = SiteUtil.getSiteConstraintValue(site.BackToBack)
            site.shiftOverlaps = tempVal  ? tempVal : 0
            site.timeZone = clientTimeZoneId

            // Move fields to update DTO
            updateDto = [:]

            updateFields.each {
                updateDto[it] = site.remove(it)
            }

            site.put('updateDto', updateDto)
        }
    }
}
