package com.emlogis.script.migration

import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 10/28/14
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */


// Setup SQL

def databaseURL = "jdbc:mysql://localhost:3308/EGS"
def databaseUser = "root"
def databasePassword = "root"
def databaseDriver =  "com.mysql.jdbc.Driver"
DateTime startDateTime
DateTime endDateTime
def created

def sql = Sql.newInstance(databaseURL, databaseUser, databasePassword, databaseDriver)

def row = sql.firstRow(' SELECT min(created) as created FROM Skill ')

endDateTime = new DateTime(row.created).plusSeconds(5)

DateTimeFormatter fmt = DateTimeFormat.forPattern('yyyy-MM-dd HH:mm:ss')

def deleteOps = []

deleteOps.add("delete FROM Shift WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM ShiftStructure_ShiftReqOld  where ShiftStructure_id not in ('east-st','north-st','west-st') ")
deleteOps.add("delete FROM ShiftReqOld WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("delete FROM ShiftType WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM EmployeeSkill WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM Team_Skill WHERE teams_id not in ('East','North','East','West') ")
deleteOps.add("DELETE FROM Site_Skill  WHERE sites_id not in ('Mapple Ridge')")
deleteOps.add("DELETE FROM Skill WHERE created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM CDAvailabilityTimeFrame WHERE  created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM CIAvailabilityTimeFrame WHERE  created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM AbsenceType WHERE created > ${endDateTime.toString(fmt)} " )
deleteOps.add("DELETE FROM SiteContract WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM User_Group   WHERE user_id not in ('adminscheduler','Joe','strategicadminscheduler'," +
        "'strategicadminscheduler','svcsupport','tacticaladminscheduler','test','admin','svcadmin') ")
deleteOps.add("DELETE FROM Site_Employee  where employee_id not in ('AlexanderDavis','BryanCarter','JaneSmars'," +
        "'JudithJones','MaryHill','RogerWilson','SallyBarnes')   ")
deleteOps.add("DELETE FROM Site WHERE created > ${endDateTime.toString(fmt)}   ")
deleteOps.add("DELETE FROM IntMinMaxCL WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM BooleanCL WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM WeekdayRotationPatternCL  WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM WeekendWorkPatternCL  WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM EmployeeContract WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM TeamContract WHERE created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM EmployeeTeam WHERE created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM ShiftStructure_Schedule")
deleteOps.add("DELETE FROM ShiftStructure WHERE created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM Team_Schedule   where Team_id not in ('East', 'North', 'West')")
deleteOps.add("DELETE FROM Schedule WHERE created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM Team WHERE created > ${endDateTime.toString(fmt)} ")
deleteOps.add("DELETE FROM Employee_notificationTypes   where employee_id not in ('AlexanderDavis','BryanCarter'," +
        "'JaneSmars','JudithJones','MaryHill','RogerWilson','SallyBarnes') ")

deleteOps.add("DELETE FROM Employee_notificationDeliveryMethods   where employee_id not in ('AlexanderDavis','BryanCarter'," +
        "'JaneSmars','JudithJones','MaryHill','RogerWilson','SallyBarnes') ")
deleteOps.add("DELETE FROM Employee WHERE created > ${endDateTime.toString(fmt)}  ")
deleteOps.add("DELETE FROM UserAccount WHERE created > ${endDateTime.toString(fmt)} ")


for(deleteOp in deleteOps) {
    sql.execute(deleteOp)
}

