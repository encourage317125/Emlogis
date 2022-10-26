package com.emlogis.script.migration.data

import groovy.sql.Sql
import groovy.util.logging.Log4j

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 9/19/14
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

@Log4j
class ExportAspenData {

    def aspenConfig
    def exportFilepath
    def exportFile

    def databaseURL
    def databaseUser
    def databasePassword
    def databaseDriver
    def pgfdatabaseURL
    def pgfdatabaseUser
    def pgfdatabasePassword
    def limitEmployees
    def limitSchedule
    def scheduleLimitDate
    def hickory

    MigrationData migrationData = new MigrationData()

    def sql
    def pgfSql

    ExportAspenData(String exportFilePath, aspenConfig, limitEmployees, limitSchedule, scheduleLimitDate, hickory) {
        this.aspenConfig = aspenConfig
        this.exportFilepath = exportFilePath
        this.databaseURL = aspenConfig.databaseURL
        this.databaseUser = aspenConfig.databaseUser
        this.databasePassword = aspenConfig.databasePassword
        this.databaseDriver = aspenConfig.databaseDriver

        this.pgfdatabaseURL = aspenConfig.pgfdatabaseURL
        this.pgfdatabaseUser = aspenConfig.pgfdatabaseUser
        this.pgfdatabasePassword = aspenConfig.pgfdatabasePassword
        this.limitEmployees = limitEmployees
        this.limitSchedule = limitSchedule
        this.scheduleLimitDate = scheduleLimitDate
        this.hickory = hickory

        log.info("The Aspen Database URL is: ${databaseURL}")
        log.info("The Aspen Database User is: ${databaseUser}")
        log.info("The Aspen Database Password is: ${databasePassword}")
        log.info("The Aspen Database Database driver is: ${databaseDriver}")

        log.info("The Aspen pgf Database URL is: ${pgfdatabaseURL}")
        log.info("The Aspen pgf Database User is: ${pgfdatabaseUser}")
        log.info("The Aspen pgf Database Password is: ${pgfdatabasePassword}")
    }

    void getAspenData() {

        def aspenShiftTypesArray = []
        def aspenShiftsArray= []
        def aspenSkillArray = []
        def skillsFromReqArray = []
        def absenceTypeArray = []
        def siteArray = []
        def employeeArray = []
        def userAccountarray = []
        def weekDayRotationsArray = []
        def weekendWorkPatternsArray = []
        def employeeSkillsArray = []
        def aspenCDAvailabilityArray = []
        def aspenCIAvailabilityArray = []
        def aspenTeamEmployeesArray = []
        def aspenTeamsArray = []
        def aspenSiteTeamsArray = []
        def aspenTeamSkillsArray = []
        def schedulesArray = []
        def aspenScheduledTeamsArray = []
        def shiftRequirementsArray = []
        def adminUserAccountsArray = []

        def  limitEmployeeSQL =  ""
        def limitEmployeeJoinSQL = ""
        def limitScheduleSQL = ""

        if(limitEmployees) {
            limitEmployeeSQL =  " WHERE EmployeeID < 330 and EmployeeID > 320 "
            limitEmployeeJoinSQL = """ WHERE
                dbo.T_Employee.EmployeeID < 330 and
                dbo.T_Employee.EmployeeID > 320 """
        }

        if(limitSchedule) {
            // Schedule limit for Aspen
            limitScheduleSQL = " and T_SiteSchedule.StartDate  > '${scheduleLimitDate}'"
        }

        //def localeSQL = ', locale'
        def localeSQL = ''
        def untilSQL = ', UntilDate'
        def teamIsDeletedSelectSQL = ',  dbo.T_Team.IsDeleted'
        def isDeletedTeamWhereSQL = "AND dbo.T_Team.IsDeleted = 0"

        if(hickory) {
            localeSQL = ''
            untilSQL = ''
            teamIsDeletedSelectSQL = ''
            isDeletedTeamWhereSQL = ''
        }

        sql = Sql.newInstance(databaseURL, databaseUser, databasePassword, databaseDriver)
        pgfSql = Sql.newInstance(pgfdatabaseURL, pgfdatabaseUser, pgfdatabasePassword, databaseDriver)


        // Get legacy shift lengths
        sql.eachRow('''  SELECT
                dbo.T_ShiftGroup.Name         AS name,
                dbo.T_ShiftGroup.Duration     AS lengthInMin,
                dbo.T_ShiftGroup.PaidHours    AS paidTimeInMin,
                dbo.T_ShiftGroup.ShiftGroupID AS aspenId,
                dbo.T_SiteShiftGroup.SiteID   AS SiteId
            FROM
                dbo.T_SiteShiftGroup
            INNER JOIN
                dbo.T_ShiftGroup
            ON
                (
                    dbo.T_SiteShiftGroup.ShiftGroupID = dbo.T_ShiftGroup.ShiftGroupID)
            WHERE
                dbo.T_ShiftGroup.IsActive = 1
            ORDER BY
                aspenId ASC''') {aspenShiftTypesArray << it.toRowResult()}

        migrationData.shiftLengths = aspenShiftTypesArray

        log.info('Finished loading Aspen shift groups')

        sql.eachRow(''' SELECT DISTINCT
            dbo.T_Shift.ShiftID,
            dbo.T_Shift.Description AS name,
            dbo.T_Shift.HrsEquiv,
            dbo.T_Shift.StartTime,
            dbo.T_Shift.ShiftType,
            dbo.T_Shift.Abbreviation,
            dbo.T_Shift.ShiftGroupID,
            dbo.T_SiteShift.SiteID as SiteId
        FROM
            dbo.T_SiteRequirement
        INNER JOIN
            dbo.T_SiteShift
        ON
            (dbo.T_SiteRequirement.SiteShiftID = dbo.T_SiteShift.SiteShiftID)
        INNER JOIN
            dbo.T_Shift
        ON
            ( dbo.T_SiteShift.ShiftID = dbo.T_Shift.ShiftID)
        INNER JOIN
            T_SiteSchedule
        ON
            (dbo.T_SiteSchedule.SiteScheduleID = dbo.T_SiteRequirement.SiteScheduleID)
        WHERE
            dbo.T_SiteSchedule.Status = 1
        ORDER BY
            dbo.T_Shift.ShiftID ASC '''){
            aspenShiftsArray << it.toRowResult()
        }

        migrationData.shiftTypes = aspenShiftsArray

        log.info('Finished loading Aspen shifts')


        sql.eachRow('SELECT Name as name, Description as description, Abbreviation as abbreviation, SkillID as aspenId  FROM dbo.T_Skill') {
            aspenSkillArray << it.toRowResult()
        }

        migrationData.skills = aspenSkillArray
        log.info('Finished loading Aspen skills')

        sql.eachRow('SELECT  distinct SiteSkillID FROM T_SiteRequirement') {
            skillsFromReqArray << it.toRowResult()
        }

        migrationData.skillsFromReqs = skillsFromReqArray
        log.info('Finished loading Aspen skills from Shift Requirements')

        sql.eachRow('SELECT AbsenceTypeID as aspenId, SiteID as aspenSiteId, Name as name, Description as description, HoursToDeduct as timeToDeductInMin FROM T_AbsenceType') {
            absenceTypeArray << it.toRowResult()
        }

        migrationData.absenceTypes = absenceTypeArray
        log.info('Finished loading Aspen absence types')

        sql.eachRow('SELECT SiteID as aspenId, Name as name, Description as description, BeginOverTimeDay, BeginOverTimeTwoWeek, ' +
                'BeginOverTimeWeek, OvertimeStartDate, HoursOffBetweenDays, EnableNotifications, ShiftDuration, BackToBack, ' +
                'FirstDayOfWeek as firstDayOfWeek FROM T_Site') {
            siteArray << it.toRowResult()
        }

        migrationData.sites = siteArray

        log.info('Finished loading Aspen sites')

        sql.eachRow(''' SELECT
            FirstName          AS firstName,
            LastName           AS lastName,
            EmployeeIdentifier AS employeeIdentifier,
            MiddleName         AS middleName,
            IsSchedulable      AS IsSchedulable,
            IsPooled           AS IsPooled,
            IsActive           AS IsActive,
            EmployeeID         AS aspenId,
            Email              AS email,
            LoginName,
            Address            AS address,
            Address2           AS address2,
            city,
            State               AS state,
            Zip                 AS zip,
            ECRelationship      AS ecRelationship,
            ECPhoneNumber       AS  ecPhoneNumber,
            EmergencyContact    AS  emergencyContact,
            Gender              AS  gender,
            HomePhone           AS homePhone,
            MobilePhone         AS mobilePhone,
            HomeEmail           AS homeEmail,
            ProfessionalLabel   AS professionalLabel,
            PrimaryContactIndicator AS primaryContactIndicator,
            HireDate            AS hireDate,
            StartDate            AS startDate,
            EndDate             AS endDate,
            HourlyRate          AS hourlyRate,
            MinHoursWeek,
            MaxHoursWeek,
            MinHoursDay,
            MaxHoursDay,
            MaxDaysWeek,
            MinHoursWeekPrimarySkill,
            MaxConsecutiveDays,
            BeginOvertimeDay,
            BeginOvertimeWeek,
            BeginOvertimeTwoWeek,
            IsDeleted,
            HomeTeamID,
            NotificationOptions,
            T_RoleType.Description AS RoleType_Description
        FROM
            T_Employee

        INNER JOIN
            dbo.T_Role
        ON
            ( dbo.T_Employee.RoleID = dbo.T_Role.RoleID)
        INNER JOIN
            dbo.T_RoleType
        ON
            (dbo.T_Role.RoleTypeID = dbo.T_RoleType.RoleTypeID) ''' + limitEmployeeSQL) {
            employeeArray << it.toRowResult()
        }

        migrationData.employees = employeeArray

        log.info('Finished loading Employees')

        String  userAccountSQL = """SELECT username as login, email, fullname as name, pgf_users_login.user_password
                         as password ${localeSQL} FROM pgf_users_login"""

        pgfSql.eachRow(userAccountSQL) {
            userAccountarray << it.toRowResult()
        }

        migrationData.userAccounts = userAccountarray

        log.info("Finished loading user accounts")

        String adminUserAccountsSQL = """

        SELECT
           dbo.T_SiteEmployee.SiteEmployeeID,
            dbo.T_SiteEmployee.EmployeeID,
            dbo.T_Employee.FirstName,
            dbo.T_Employee.LastName
        FROM
            dbo.T_SiteEmployee
        INNER JOIN
            dbo.T_Employee
        ON
            (
                dbo.T_SiteEmployee.EmployeeID = dbo.T_Employee.EmployeeID)
        LEFT OUTER JOIN
            dbo.T_SiteEmployeeTeam
        ON
            (
                dbo.T_SiteEmployee.SiteEmployeeID = dbo.T_SiteEmployeeTeam.SiteEmployeeID)
        WHERE
            dbo.T_SiteEmployeeTeam.SiteEmployeeTeamID IS NULL """

        sql.eachRow(adminUserAccountsSQL) {
            adminUserAccountsArray << it.toRowResult()
        }

        migrationData.adminUserAccounts = adminUserAccountsArray

        log.info("Finished loading Admin user accounts")

        sql.eachRow('SELECT EmployeeCIRotationID as aspenId, EmployeeID, WeekdayNumber as dayOfWeek, RotationValue FROM T_EmployeeCIRotation') {
            weekDayRotationsArray << it.toRowResult()
        }

        migrationData.weekDayRotations = weekDayRotationsArray

        log.info("Finished loading aspen weekday rotations")

        sql.eachRow('SELECT EmployeeWeekendsID as aspenId , EmployeeID, CoupleWeekends as enabled, DaysAfter as daysOffAfter, DaysBefore as daysOffBefore FROM T_EmployeeWeekends') {
            weekendWorkPatternsArray << it.toRowResult()
        }

        migrationData.weekendWorkPatterns = weekendWorkPatternsArray

       log.info("Finished loading aspen coupled weekend rotations")

        sql.eachRow(""" SELECT EmployeeCDAvailabilityID, EmployeeID, AvailabilityDate, AvailabilityStatus, StartTime,
                        EndTime, AbsenceTypeID, availabilityEndDate
                        FROM T_EmployeeCDAvailability """ + limitEmployeeSQL) {
            aspenCDAvailabilityArray  << it.toRowResult()
        }

        migrationData.aspenCDAvailability = aspenCDAvailabilityArray

        log.info("Finished loading CD Availability")

        String employeeCIAvailSQL = """ SELECT EmployeeCIAvailabilityID, EmployeeID, WeekdayNumber, AvailabilityStatus, StartTime,
                        EndTime ${untilSQL} FROM T_EmployeeCIAvailability ${limitEmployeeSQL} """

        sql.eachRow(employeeCIAvailSQL) {
            aspenCIAvailabilityArray  << it.toRowResult()
        }

        migrationData.aspenCIAvailability = aspenCIAvailabilityArray

        log.info("Finished loading CI Availability")

        sql.eachRow(''' SELECT
            dbo.T_SiteEmployeeSkill.SkillScore,
            dbo.T_SiteEmployeeSkill.IsPrimary,
            dbo.T_Skill.SkillID,
            dbo.T_Employee.EmployeeID,
            dbo.T_Skill.Name,
            dbo.T_Skill.Description,
            dbo.T_Skill.Abbreviation,
            dbo.T_Site.SiteID,
            dbo.T_SiteEmployee.SiteEmployeeID
        FROM
            dbo.T_SiteSkill
        INNER JOIN
            dbo.T_SiteEmployeeSkill
        ON
            (
                dbo.T_SiteSkill.SiteSkillID = dbo.T_SiteEmployeeSkill.SiteSkillID)
        INNER JOIN
            dbo.T_Skill
        ON
            (
                dbo.T_SiteSkill.SkillID = dbo.T_Skill.SkillID)
        INNER JOIN
            dbo.T_SiteEmployee
        ON
            (
                dbo.T_SiteEmployeeSkill.SiteEmployeeID = dbo.T_SiteEmployee.SiteEmployeeID)
        INNER JOIN
            dbo.T_Employee
        ON
            (
                dbo.T_SiteEmployee.EmployeeID = dbo.T_Employee.EmployeeID)
        INNER JOIN
            dbo.T_Site
        ON
            (
                dbo.T_SiteEmployee.SiteID = dbo.T_Site.SiteID)  '''  + limitEmployeeJoinSQL + '''

        ORDER BY
            dbo.T_Employee.EmployeeID ASC,
            dbo.T_Skill.SkillID ASC ''') {
            employeeSkillsArray << it.toRowResult()
        }

        migrationData.employeeSkills = employeeSkillsArray

        log.info("Finished loading employee skills")


        String teamSQL = """ SELECT
            dbo.T_Team.TeamID,
            dbo.T_Team.Name as name,
            dbo.T_Team.Description as description ${teamIsDeletedSelectSQL}
        FROM
            dbo.T_Team
        ORDER BY
            dbo.T_Team.TeamID ASC """

        sql.eachRow(teamSQL) {
            aspenTeamsArray  << it.toRowResult()
        }

        migrationData.aspenTeams = aspenTeamsArray

        log.info("Finished loading Teams")

        sql.eachRow(""" SELECT
            dbo.T_SiteTeam.SiteTeamID,
            dbo.T_SiteTeam.SiteID,
            dbo.T_SiteTeam.TeamID
        FROM
            dbo.T_SiteTeam """ ) {
            aspenSiteTeamsArray  << it.toRowResult()
        }

        migrationData.aspenSiteTeams = aspenSiteTeamsArray

        log.info("Finished loading Site Teams")

        String teamEmployeesSQL = """ SELECT
            dbo.T_SiteEmployee.EmployeeID,
            dbo.T_SiteEmployee.SiteEmployeeID,
            dbo.T_SiteEmployee.SiteID,
            dbo.T_Team.TeamID,
            dbo.T_Team.Name,
            dbo.T_Team.Description ${teamIsDeletedSelectSQL},
            dbo.T_SiteEmployeeTeam.IsFloat
        FROM
            dbo.T_SiteEmployee
        INNER JOIN
            dbo.T_SiteEmployeeTeam
        ON
            (
                dbo.T_SiteEmployee.SiteEmployeeID = dbo.T_SiteEmployeeTeam.SiteEmployeeID)
        INNER JOIN
            dbo.T_SiteTeam
        ON
            (
                dbo.T_SiteEmployeeTeam.SiteTeamID = dbo.T_SiteTeam.SiteTeamID)
        INNER JOIN
            dbo.T_Employee
        ON
            (
                dbo.T_SiteEmployee.EmployeeID = dbo.T_Employee.EmployeeID)
        AND (
                dbo.T_SiteEmployee.SiteID = dbo.T_Employee.SiteID)
        INNER JOIN
            dbo.T_Team
        ON
            (dbo.T_SiteTeam.TeamID = dbo.T_Team.TeamID) ${limitEmployeeJoinSQL}

        ORDER BY
            dbo.T_SiteTeam.TeamID ASC,
            dbo.T_SiteEmployee.EmployeeID ASC """

        sql.eachRow(teamEmployeesSQL)  {
            aspenTeamEmployeesArray  << it.toRowResult()
        }

        migrationData.aspenTeamEmployees = aspenTeamEmployeesArray

        log.info("Finished loading Employee Teams ")

        sql.eachRow(''' SELECT
            dbo.T_SiteTeam.SiteID,
            dbo.T_SiteTeam.TeamID,
            dbo.T_SiteSkill.SkillID
        FROM
            dbo.T_SiteTeamSkill T_SiteTeamSkill_alias1
        INNER JOIN
            dbo.T_SiteTeam
        ON
            (
                T_SiteTeamSkill_alias1.SiteTeamID = dbo.T_SiteTeam.SiteTeamID)
        INNER JOIN
            dbo.T_SiteTeamSkill T_SiteTeamSkill_alias2
        ON
            (
                T_SiteTeamSkill_alias1.SiteSkillID = T_SiteTeamSkill_alias2.SiteSkillID)
        AND (
                T_SiteTeamSkill_alias1.SiteTeamID = T_SiteTeamSkill_alias2.SiteTeamID)
        INNER JOIN
            dbo.T_SiteSkill
        ON
            (
                T_SiteTeamSkill_alias2.SiteSkillID = dbo.T_SiteSkill.SiteSkillID)
        ORDER BY
            dbo.T_SiteTeam.SiteID ASC,
            T_SiteTeamSkill_alias2.SiteTeamID ASC ''') {
            aspenTeamSkillsArray << it.toRowResult()
        }

        migrationData.teamSkills = aspenTeamSkillsArray

        log.info('Finished loading Team Skills')

        String aspenScheduleSQL = """ SELECT
            SiteScheduleID,
            SiteID,
            Name AS name,
            Description AS description,
            StartDate,
            EndDate,
            (DATEDIFF(DAY, StartDate, EndDate) + 1) AS scheduleLengthInDays,
            CompletionState,
            Posted AS posted
        FROM
            T_SiteSchedule
        WHERE
            status = 1 ${limitScheduleSQL}
        Order by StartDate desc  """

        sql.eachRow(aspenScheduleSQL) {
            schedulesArray<< it.toRowResult()
        }

        migrationData.schedules = schedulesArray

        log.info('Finished loading Schedules')

        String aspenScheduledTeamsSQL =  """ SELECT
            dbo.T_SiteSchedule.SiteScheduleID,
            dbo.T_SiteSchedule.Name,
            dbo.T_SiteTeam.TeamID,
            dbo.T_SiteSchedule.StartDate,
            dbo.T_Team.Name
        FROM
            dbo.T_SiteTeam
        INNER JOIN
            dbo.T_Team
        ON
            (
                dbo.T_SiteTeam.TeamID = dbo.T_Team.TeamID)
        INNER JOIN
            dbo.T_SiteScheduleTeam
        ON
            (
                dbo.T_SiteTeam.SiteTeamID = dbo.T_SiteScheduleTeam.SiteTeamID)
        INNER JOIN
            dbo.T_SiteSchedule
        ON
            (
                dbo.T_SiteScheduleTeam.SiteScheduleID = dbo.T_SiteSchedule.SiteScheduleID)
        WHERE
           dbo.T_SiteSchedule.Status = 1 ${limitScheduleSQL} """

        sql.eachRow(aspenScheduledTeamsSQL) {
            aspenScheduledTeamsArray << it.toRowResult()
        }

        migrationData.aspenScheduledTeams = aspenScheduledTeamsArray

        log.info('Finished loading Scheduled Teams ')

        String shiftRequirementsSQL = """ SELECT
            dbo.T_Team.TeamID,
            dbo.T_SiteRequirement.SiteScheduleID,
            dbo.T_SiteSchedule.StartDate AS StartDate,
            dbo.T_SiteRequirement.Date AS Require_Date,
            dbo.T_Skill.SkillID,
            dbo.T_Skill.Name         AS skill_name,
            dbo.T_Skill.Description  AS skill_Desc,
            dbo.T_Skill.Abbreviation AS skillAbbrev,
            dbo.T_SiteSkill.SiteID AS SiteID,
            dbo.T_Team.Name          AS team_name,
            dbo.T_Shift.Description     AS shift_description,
            dbo.T_Shift.HrsEquiv        AS shift_hrs,
            dbo.T_Shift.StartTime       AS shift_startTime,
            dbo.T_Shift.EndTime         AS shift_endtime,
            dbo.T_Shift.ShiftType       AS shift_type,
            dbo.T_ShiftGroup.Name       AS shift_group_name,
            dbo.T_ShiftGroup.Duration   AS shift_group_duration,
            dbo.T_ShiftGroup.PaidHours  AS shift_group_paid_hours,
            dbo.T_ShiftGroup.BaseLength AS shiftdgroup_base_length,
            dbo.T_ShiftGroup.GroupType  AS shiftgroup_type,
            dbo.T_ShiftGroup.ShiftGroupID,
            dbo.T_Employee.EmployeeID
        FROM
            dbo.T_SiteRequirement
        INNER JOIN
            dbo.T_SiteSkill
        ON
            (
                dbo.T_SiteRequirement.SiteSkillID = dbo.T_SiteSkill.SiteSkillID)
        INNER JOIN
            dbo.T_Skill
        ON
            (
                dbo.T_SiteSkill.SkillID = dbo.T_Skill.SkillID)
        INNER JOIN
            dbo.T_SiteTeam
        ON
            (
                dbo.T_SiteRequirement.SiteTeamID = dbo.T_SiteTeam.SiteTeamID)
        INNER JOIN
            dbo.T_Team
        ON
            (
                dbo.T_SiteTeam.TeamID = dbo.T_Team.TeamID)
        INNER JOIN
            dbo.T_SiteShift
        ON
            (
                dbo.T_SiteRequirement.SiteShiftID = dbo.T_SiteShift.SiteShiftID)
        INNER JOIN
            dbo.T_Shift
        ON
            (
                dbo.T_SiteShift.ShiftID = dbo.T_Shift.ShiftID)
        INNER JOIN
            dbo.T_ShiftGroup
        ON
            (
                dbo.T_Shift.ShiftGroupID = dbo.T_ShiftGroup.ShiftGroupID)
        INNER JOIN
            dbo.T_SiteSchedule
        ON
            (
                dbo.T_SiteRequirement.SiteScheduleID = dbo.T_SiteSchedule.SiteScheduleID)
        INNER JOIN
            dbo.T_SiteScheduleAssignment
        ON
            (
                dbo.T_SiteRequirement.SiteRequirementID =
                dbo.T_SiteScheduleAssignment.SiteRequirementID)
        LEFT OUTER JOIN
            dbo.T_SiteResource
        ON
            (
                dbo.T_SiteScheduleAssignment.SiteResourceID = dbo.T_SiteResource.SiteResourceID)
        LEFT OUTER JOIN
            dbo.T_SiteEmployee
        ON
            (
                dbo.T_SiteResource.SiteEmployeeID = dbo.T_SiteEmployee.SiteEmployeeID)
        LEFT OUTER JOIN
            dbo.T_Employee
        ON
            (
                dbo.T_SiteEmployee.EmployeeID = dbo.T_Employee.EmployeeID)
        WHERE
            dbo.T_SiteSchedule.Status = 1 ${limitScheduleSQL}
        ORDER BY
            dbo.T_SiteRequirement.SiteScheduleID ASC,
            dbo.T_Shift.ShiftID ASC,
            dbo.T_Team.TeamID ASC,
            dbo.T_SiteSkill.SkillID ASC """

        sql.eachRow(shiftRequirementsSQL) {
            shiftRequirementsArray << it.toRowResult()
        }

        migrationData.shiftRequirements = shiftRequirementsArray

        log.info('Finished loading Schedule Requirements ')

    }

    void saveAspenData() {

        log.info("!!!Starting Aspen Export!!!")
        exportFile = new File(exportFilepath)

        def jsonBuilder = new groovy.json.JsonBuilder()
        jsonBuilder(migrationData: migrationData)
        exportFile.write(jsonBuilder.toPrettyString())
        log.info("!!!Aspen Export Completed!!!")
    }
}
