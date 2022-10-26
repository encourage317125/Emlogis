package com.emlogis.script.migration.data

import com.emlogis.script.migration.MigrationConstants
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

import static groovyx.net.http.ContentType.*

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 9/22/14
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
@Log4j
class ImportMercuryData {
    def importFilePath
    def importFile

    def mercuryConfig
    def restURL
    def tenantId
    def excludedEntities
    def migrateScheduleReqOnly

    def mercuryEmployeeTeams
    def clientTimeZoneId

    def sitesToIgnore = []

    def token

    MigrationData migrationData = new MigrationData()


    RESTClient restClient


    int totalEntityCount
    int TENSECONDS  = 10*1000;
    int THIRTYSECONDS = 30*1000;

    ImportMercuryData(importFilePath, tenantId, mercuryConfig, excludedEntities, migrateScheduleReqOnly, clientTimeZoneId) {
        this.importFilePath = importFilePath
        this.mercuryConfig = mercuryConfig
        this.restURL = mercuryConfig.restURL
        this.tenantId = tenantId;
        this.excludedEntities = excludedEntities
        this.migrateScheduleReqOnly = migrateScheduleReqOnly
        this.clientTimeZoneId = clientTimeZoneId

        DateTimeZone.setDefault(DateTimeZone.forID(clientTimeZoneId))
    }

    void getData() {
        def importFile = new File(importFilePath)

        JsonSlurper jsonSlurper = new JsonSlurper()
        def jsonResult = jsonSlurper.parseText(importFile.text)

        migrationData = jsonResult.migrationData
        log.info("Loaded Aspen data")

    }

    // Main Import data method

    void importData() {

        def jsonBuilder = new groovy.json.JsonBuilder()

        // Login to Mercury
        def sessionURL = restURL + "/sessions"
        restClient = new RESTClient(sessionURL)
        // restClient.defaultRequestHeaders.'Content-Type' = JSON

        restClient.defaultContentType = JSON

        restClient.getClient().getParams().setParameter("http.connection.timeout", new Integer(TENSECONDS))
        restClient.getClient().getParams().setParameter("http.socket.timeout", new Integer(THIRTYSECONDS))

        def response

        try {
            response = restClient.post(
                    body: ["tenantId": tenantId, "login": "migration", "password": "migration"]
            )
        } catch (Exception e) {
            log.error("Encountered error logging into Mercury: " + e.getResponse().responseData.message)
            return
        }

        token = response.data.token
        log.info("The token is ${token}")
        restClient.defaultRequestHeaders.'EmlogisToken' = token

        def excludedKeys = ['aspenId']

        totalEntityCount = 0
        def startTime =  System.nanoTime()

        importOrg(restClient)
        if(!excludedEntities.contains('sites')) importSites(restClient)
        if(!excludedEntities.contains('shiftLengths')) importShiftLengths(restClient, excludedKeys)

        if(!excludedEntities.contains('shiftTypes')) importShiftTypes(restClient)

        if(!excludedEntities.contains('skills')) importSkills(restClient, excludedKeys)

        if(!excludedEntities.contains('sitesContractLines')) importSiteContractLines(restClient)
        if(!excludedEntities.contains('absenceTypes')) importAbsenceTypes(restClient)

        if(!excludedEntities.contains('employees')) importEmployees(restClient)

        if(!excludedEntities.contains('CIAvailabilty')) importCIAvailabilty()

        if(!excludedEntities.contains('CDAvailabilty')) importCDAvailabilty()

        if(!excludedEntities.contains('notfications')) importEmployeeNotifications(restClient)

        if(!excludedEntities.contains('contractLines')) importEmployeeContractLines(restClient)

        if(!excludedEntities.contains('employeeSkills')) importEmployeeSkills()

        if(!excludedEntities.contains('teams')) importTeams(restClient)

        if(!excludedEntities.contains('teamSkills')) importTeamSkills(restClient)

        if(!excludedEntities.contains('shiftstructures')) importShiftStructures(restClient)

        if(!excludedEntities.contains('schedules')) importSchedules(restClient, migrateScheduleReqOnly)

        if(migrateScheduleReqOnly) {
            if(!excludedEntities.contains('shiftReqs')) importShiftRequirements(restClient)
        }  else {
            if(!excludedEntities.contains('shifts')) importShiftAssignments(restClient)
        }

        def endTIme =  System.nanoTime()

        def elapsedTime = (endTIme - startTime) / 1000000000

        def elapsedMinutes = elapsedTime/60

        closeSession()

        log.info("\n\n****Migration Import is complete***")
        log.info ("Total duration in minutes is: " + elapsedMinutes )
        log.info ("The average Entity Rate/Second is: " + totalEntityCount/elapsedTime )
    }

    private void importOrg(RESTClient restClient) {
        // In this method we are going to add the timezone to the Org
        def orgRestPath = '/org'
        def org = [:]
        restClient.setUri(restURL + orgRestPath)

        org.timeZone = clientTimeZoneId

        putEntity("", org)
        log.info("Updated Org Timezone")
    }

    private void importSites(RESTClient restClient) {
        // Load sites
        def siteRestPath = '/sites'
        restClient.setUri(restURL + siteRestPath)

        def excludedKeys = ['aspenId','BeginOverTimeDay','BeginOverTimeTwoWeek','BeginOverTimeWeek','OvertimeStartDate',
        'OvertimeStartDate','HoursOffBetweenDays','EnableNotifications','ShiftDuration','BackToBack']

        def siteArray = migrationData.sites

        if(siteArray) {
            // filter default site
            def defaultSite = siteArray.find{it.name == "Default Site"}
            if(defaultSite) {
                sitesToIgnore.add(defaultSite.aspenId)
                siteArray.remove(defaultSite)
            }

            SiteUtil.processSiteList(siteArray, clientTimeZoneId)

            sendSimpleEntity(siteArray, excludedKeys, "Sites")

            log.info("Inserted Sites")
        }
    }



    private void importAbsenceTypes(RESTClient restClient) {
    // Load AbsenceTypes
        def absenceTypeRestPath = '/absencetypes'
        def siteRestPath = '/sites'
        restClient.setUri(restURL + absenceTypeRestPath)

        def aspenAbsenceTypeArray = migrationData.absenceTypes



        def excludedKeys = ['aspenId', 'aspenSiteId']
        def aspenSiteId
        def mercurySiteId
        def entityPath

        def arraySize = aspenAbsenceTypeArray?.size
        def count = 0

        // Remove Absence Types associated with sites we don't want

        aspenAbsenceTypeArray.removeAll{ sitesToIgnore.contains(it.aspenSiteId) }

        for(absenceType in aspenAbsenceTypeArray) {

            //Default skill description for now
            if (!absenceType.description) {
                absenceType.description = absenceType.name
            }

            absenceType.timeToDeductInMin = absenceType.hoursToDeduct ? absenceType.hoursToDeduct * 60 : 0;
            absenceType.remove('hoursToDeduct')

            // Get the new siteId
            aspenSiteId = absenceType.aspenSiteId

            mercurySiteId = MigrationData.getMercurySiteId(aspenSiteId, migrationData.sites)
            entityPath = restURL + siteRestPath + "/"  + mercurySiteId +  absenceTypeRestPath
            restClient.setUri(entityPath)

            sendEntity(excludedKeys, absenceType)

            ++count
            if( count % MigrationConstants.COLLECTION_FEEDBACK_SIZE == 0) {
                log.info("${count} of ${arraySize} Absence Types completed")
            }
        }

        totalEntityCount += count

        log.info("Inserted Absence Types")
    }

    private void importSkills(RESTClient restClient, ArrayList<String> excludedKeys) {
    // Load Skills
        def skillRestPath = '/skills'
        restClient.setUri(restURL + skillRestPath)


        def aspenSkillArray = migrationData.skills
        def reqSkillList = migrationData.skillsFromReqs
        def uniqueSkillList

        if(aspenSkillArray) {
            uniqueSkillList =  SkillUtil.processSkills(aspenSkillArray, reqSkillList)
            sendSimpleEntity(uniqueSkillList, excludedKeys, "Skills")
        }

        log.info("Inserted skills")
    }

    private void importShiftLengths(RESTClient restClient, ArrayList<String> excludedKeys) {
    // Load Shift Lengths
        def updateDto
        def shiftLengthsList = migrationData.shiftLengths
        def sitesList = migrationData.sites
        def shiftLengthExcludedKeys = excludedKeys + ['SiteId', 'unique', 'mercurySiteId']
        def uniqueShiftLengthList = []

        def shiftLengthRestPath = '/shiftlengths'
        def siteRestPath = '/sites'
        def entityPath

        def aspenSiteId
        def mercurySiteId

        def count = 0

        def arraySize = shiftLengthsList?.size

        if(shiftLengthsList) {
            uniqueShiftLengthList = ScheduleUtil.processShiftLengths(shiftLengthsList, sitesList, sitesToIgnore)
            sendSubEntity(uniqueShiftLengthList, siteRestPath, shiftLengthRestPath, shiftLengthExcludedKeys, "mercurySiteId", "Shift Lengths")
        }

        log.info("Inserted Shift Lengths")
    }

    private void importShiftTypes(RESTClient restClient)  {
        def shiftTypeList = migrationData.shiftTypes
        def shiftLengthsList = migrationData.shiftLengths
        def shiftTypeRestPath = '/shifttypes'
        def shiftTypetURL = restURL + shiftTypeRestPath
        def sitesList = migrationData.sites
        def siteRestPath = '/sites'

        def excludedKeys = ['ShiftID', 'HrsEquiv', 'StartTime', 'ShiftType', 'Abbreviation',
        'ShiftGroupID','SiteId','mercurySiteId']

        if(shiftTypeList) {

            ScheduleUtil.processShiftTypes(shiftTypeList, shiftLengthsList, sitesList, sitesToIgnore)

            sendSubEntity(shiftTypeList, siteRestPath, shiftTypeRestPath, excludedKeys, "mercurySiteId", "Shift Types")

            log.info("Inserted Shift Types")
        }
    }

    private void importEmployees(RESTClient restClient)   {

        def employeeArray = migrationData.employees
        def userAccountArray = migrationData.userAccounts
        def employeeRestPath = '/employees'
        def userAccountRestPath = '/useraccounts'
        def employeeURL = restURL + employeeRestPath
        def userAccountURL = restURL + userAccountRestPath
        def userAccountOnlyEmployees = []
        def adminUserAccounts = migrationData.adminUserAccounts

        def adminUserDTOArray

        if(employeeArray) {
            restClient.setUri(employeeURL)

            // remove non Employee records
            EmployeeUtil.removeExcludedEmployees(employeeArray, adminUserAccounts, userAccountOnlyEmployees)

            // Update Mercury fields for REST API
            EmployeeUtil.updateMercuryFields(employeeArray, userAccountArray)

            sendSimpleEntity(employeeArray, EmployeeUtil.excludedFieldsArray, "Employees")

            log.info("Imported employees")

            restClient.setUri(userAccountURL)
            adminUserDTOArray =  EmployeeUtil.updateAdminAccounts(userAccountOnlyEmployees, userAccountArray)
            sendSimpleEntity(adminUserDTOArray, [], "Admin Employees User Accounts")

            log.info("Imported Admin user account employees")
        }
    }

    private void importEmployeeAdmins(RESTClient restClient) {
        def employeeList = migrationData.employees
        def emmloyeeAdmins
        def adminAccountList = []
        def employeeRestPath = '/employees'
        def accountRestPath = '/useraccount'
        def requestAccountRestPath
        def addToAdminGroupAccountRestPath = '/groupaccounts/adminschedulergroup/ops/adduser'
        def employeeId
        def accountReqList
        def userAccountId

        emmloyeeAdmins = EmployeeUtil.getEmployeeAdmins(employeeList)

        for (employee in emmloyeeAdmins) {
                employeeId = employee.id
                requestAccountRestPath = restURL + employeeRestPath + "/"  + employeeId + accountRestPath
                restClient.setUri(requestAccountRestPath)
                accountReqList = requestDataByGet()
                if(accountReqList) {
                    userAccountId = accountReqList.id
                    adminAccountList.add(userAccountId)
                }  else {
                    log.error( "No Admin account data could be retrieved for  employee : " + JsonOutput.toJson(employee) )
                }

        }

        restClient.setUri(restURL + addToAdminGroupAccountRestPath)

        postEntityList(adminAccountList, [], "Employee Admins")
    }

    private void importEmployeeNotifications(RESTClient restClient1)  {
        def employeeNotificationsList

        def employeePath = '/employees'
        def notificationsPath = '/notificationsettings'
        def aspenEmployeeArray = migrationData.employees

        def excludedKeys = ['employeeId']

        log.info("About to migrate employee notifications")

        // migrate notification preferences for each employee
        if(aspenEmployeeArray) {
            employeeNotificationsList = EmployeeUtil.getNotificationOptions(aspenEmployeeArray)
            putSubEntity(employeeNotificationsList, employeePath, notificationsPath, excludedKeys, "employeeId", "Employee Notification preferences")
        }
    }

    private void importEmployeeContractLines(RESTClient restClient1) {
        def contractLineArray
        def employeeRestPath = '/employees'
        def contractPath = '/contracts'
        def contractLinePath = '/contractlines'
        def contractRequestURL
        def contractLineURL
        def aspenEmployeeArray = migrationData.employees
        def employeeId
        def contractList
        def contract

        def excludedKeys = ['employeeId', 'contractId']

        log.info("About to create Employee contract lines from Aspen Data. This may take a while.")

        // get contracts for the contract lines
        for(employeeMap in aspenEmployeeArray) {
           employeeId = employeeMap.id

           contractRequestURL =   restURL + employeeRestPath + "/"  + employeeId + contractPath
           restClient.setUri(contractRequestURL)

           contractList = requestDataByGet()
           contract = contractList.result[0]
           employeeMap.contractId = contract.id
        }

        // Build contract lines for Employees
        contractLineArray = EmployeeUtil.getContractLines(aspenEmployeeArray)

        sendSubEntity(contractLineArray, contractPath, contractLinePath, excludedKeys, "contractId", "Employee Contract lines")

        // Process WeekDay Rotations
        excludedKeys = ['aspenId', 'EmployeeID', 'RotationValue','contractId' ]

        def weekDayRotationArray = migrationData.weekDayRotations

        if(EmployeeUtil.processWeekDayRotationContractLines(aspenEmployeeArray, weekDayRotationArray)){
            sendSubEntity(weekDayRotationArray, contractPath, contractLinePath, excludedKeys, "contractId", "Weekday Rotation Contract lines")
        }

        // Process Weekend Rotations
        excludedKeys = ['aspenId', 'EmployeeID', 'contractId' ]
        def weekendWorkPatternsArray = migrationData.weekendWorkPatterns

        if(EmployeeUtil.processWeekendRotationContractLines(aspenEmployeeArray, weekendWorkPatternsArray)) {
            sendSubEntity(weekendWorkPatternsArray, contractPath, contractLinePath, excludedKeys, "contractId", "Weekend Rotation Contract lines")
        }


    }

    private void importSiteContractLines (RESTClient restClient1)  {
        def contractLineArray
        def siteRestPath = '/sites'
        def contractPath = '/contracts'
        def contractLinePath = '/contractlines'
        def contractRequestURL
        def contractLineURL
        def siteArray = migrationData.sites
        def siteId
        def contractList
        def contract

        def excludedKeys = ['siteId', 'contractId']

        log.info("About to create Site contract lines from Aspen Data. This may take a while.")

        // get contracts for sites
        for (siteMap in siteArray) {
            siteId = siteMap.id

            contractRequestURL =   restURL + siteRestPath + "/"  + siteId + contractPath
            restClient.setUri(contractRequestURL)

            contractList = requestDataByGet()
            contract = contractList.result[0]
            siteMap.contractId = contract.id

        }

        // Build site contract lines
        contractLineArray = SiteUtil.getContractLines(siteArray)

        sendSubEntity(contractLineArray, contractPath, contractLinePath, excludedKeys, "contractId", "Site Contract lines")
    }

    private importEmployeeSkills() {
        def aspenEmployeeSkillsArray  = migrationData.employeeSkills
        def mercuryEmployeeSkills
        def employeeRestPath = '/employees'
        def employeeSkillsPath = '/skills'

        def employeeSkillsURL
        def aspenEmployeeArray = migrationData.employees

        def excludedKeys = ['employeeId']

        // Build Employee Skills Association
        mercuryEmployeeSkills = EmployeeUtil.getEmployeeSkillList(aspenEmployeeArray,aspenEmployeeSkillsArray)

        sendSubEntity(mercuryEmployeeSkills, employeeRestPath, employeeSkillsPath, excludedKeys, "employeeId", "Employee Skills")
    }

    private importCDAvailabilty() {
        def aspenCDAvailArray  = migrationData.aspenCDAvailability
        def aspenCIAvailArray  = migrationData.aspenCIAvailability
        def aspenEmployeeArray = migrationData.employees
        def absenceTypeArray = migrationData.absenceTypes

        def mercuryCDAvailList
        def employeeRestPath = '/employees'
        def cdAvailRestpath = '/cdavailability'

        def excludedKeys = ['employeeId']

        mercuryCDAvailList  = EmployeeUtil.getCDAvailabilityList(aspenEmployeeArray, aspenCDAvailArray, aspenCIAvailArray, absenceTypeArray)

        sendSubEntity(mercuryCDAvailList, employeeRestPath, cdAvailRestpath, excludedKeys, "employeeId", "Employee CD Availability Timeframes")

    }

    private importCIAvailabilty() {
        def aspenCIAvailArray  = migrationData.aspenCIAvailability
        def aspenEmployeeArray = migrationData.employees

        def mercuryCIAvailList
        def employeeRestPath = '/employees'
        def ciAvailRestpath = '/ciavailability'

        def excludedKeys = ['employeeId']

        mercuryCIAvailList  = EmployeeUtil.getCIAvailabilityList(aspenEmployeeArray, aspenCIAvailArray)

        sendSubEntity(mercuryCIAvailList, employeeRestPath, ciAvailRestpath, excludedKeys, "employeeId", "Employee CI Availability Timeframes")

    }

    private  void importTeams(RESTClient restClient) {
        def teamRestpath = '/teams'
        restClient.setUri(restURL + teamRestpath)

        def excludedKeys  =  ['TeamID', 'IsDeleted', 'description', 'name']

        def teamArray = migrationData.aspenTeams
        def sites = migrationData.sites
        def siteTeams = migrationData.aspenSiteTeams

        TeamUtil.processTeams(teamArray, sites, siteTeams)

        if(teamArray) sendSimpleEntity(teamArray, excludedKeys, "Teams")

        log.info("Inserted Teams")

        mercuryEmployeeTeams
        def aspenEmployeeTeams = migrationData.aspenTeamEmployees
        def employeeList = migrationData.employees
        def employeeRestPath = '/employees'

        mercuryEmployeeTeams = TeamUtil.getMercuryEmployeeTeams(aspenEmployeeTeams,employeeList, teamArray)

        excludedKeys =['employeeId']

        sendSubEntity(mercuryEmployeeTeams, employeeRestPath, teamRestpath, excludedKeys, "employeeId", "Employee Teams")
    }

    private void importTeamSkills(RESTClient restClient) {
        def teamRestPath = '/teams'
        def skillRestPath = '/ops/addskill'

        def teamSkillsList = migrationData.teamSkills
        def teamList = migrationData.aspenTeams

        // get the mercury IDs for team skill list
        TeamUtil.processTeamSkills(teamSkillsList, teamList)

        sendSubEntityQuery(teamSkillsList, teamRestPath, skillRestPath,  "teamId", "skillId", "Team Skills")


    }

    private void importShiftStructures(RESTClient restClient){
        def shiftStructureList, teamList
        def shiftStructureRstPath = '/shiftstructures'

        shiftStructureList = migrationData.aspenScheduledTeams
        def excludedEntities = ['Name','SiteScheduleID','TeamID','StartDate']

        restClient.setUri(restURL + shiftStructureRstPath)

        if(shiftStructureList) {
            teamList = migrationData.aspenTeams
            ScheduleUtil.processShiftStructures(shiftStructureList, teamList)
            sendSimpleEntity(shiftStructureList, excludedEntities, "Shift Structures")
        }

    }

    private void importSchedules(RESTClient restClient, migrateScheduleReqOnly) {
        def scheduleList = migrationData.schedules
        def scheduleRestPath = '/schedules'
        def scheduledTeams = migrationData.aspenScheduledTeams

        def excludedEntities = ['SiteScheduleID','SiteID','StartDate','EndDate','CompletionState','posted']
        restClient.setUri(restURL + scheduleRestPath)

        if(scheduleList) {
          ScheduleUtil.processSchedules(scheduleList, scheduledTeams, migrateScheduleReqOnly)
          sendSimpleEntity(scheduleList, excludedEntities, "Schedules")
        }
    }

    private void importShiftRequirements(RESTClient restClient){
        def shiftRequirementList = migrationData.shiftRequirements
        def  shiftStructureList = migrationData.aspenScheduledTeams

        def shiftLengthList = migrationData.shiftLengths

        def shiftStructureRstPath = '/shiftstructures'
        def scheduleReqRestPath =   '/shiftreqs'

        def excludedEntities = ['TeamID','SiteScheduleID','StartDate','Require_Date','SkillID','skill_name',
        'skill_Desc','skill_abbrev','team_name','shift_description','shift_hrs','shift_startTime','shift_endtime','shift_type',
        'shift_group_name','shift_group_duration','shift_group_paid_hours','shiftdgroup_base_length','shiftgroup_type',
        'ShiftGroupID','EmployeeID','SiteID', 'skillAbbrev']

        if(shiftRequirementList) {
            ScheduleUtil.processScheduleRequirements(shiftRequirementList, shiftStructureList, shiftLengthList)
            sendSubEntity(shiftRequirementList, shiftStructureRstPath, scheduleReqRestPath, excludedEntities, 'shiftStructureId', "Shift Requirements")
        }


    }

    private void importShiftAssignments(RESTClient restClient) {
        def shiftRequirementList = migrationData.shiftRequirements
        def  shiftStructureList = migrationData.aspenScheduledTeams
        def shiftLengthList = migrationData.shiftLengths
        def employeeList = migrationData.employees
        def scheduleList = migrationData.schedules
        def siteArray = migrationData.sites

        def scheduleRestPath = '/schedules'
        def shiftRestPath = '/shifts/miigrate'

        def excludedEntities = ['TeamID','SiteScheduleID','StartDate','Require_Date','SkillID','skill_name',
                'skill_Desc','team_name','shift_description','shift_hrs','shift_startTime','shift_endtime','shift_type',
                'shift_group_name','shift_group_duration','shift_group_paid_hours','shiftdgroup_base_length','shiftgroup_type',
                'ShiftGroupID','EmployeeID','scheduleId','SiteID']

        if(shiftRequirementList) {
            ScheduleUtil.processShiftAssignments(shiftRequirementList, shiftStructureList, shiftLengthList,
                employeeList, scheduleList, siteArray)
            sendSubEntity(shiftRequirementList, scheduleRestPath, shiftRestPath, excludedEntities, 'scheduleId', "Assigned Shifts")
        }
    }

    private void sendSubEntity(ArrayList subEntityArray, String parentEntityPath, String subEntityPath, ArrayList<String> excludedKeys, String parentKey, String subName) {
        def subEntityURL
        def arraySize = subEntityArray?.size()
        def count = 0
        long startTime = System.nanoTime();

        for (subEntity in subEntityArray) {
            subEntityURL = restURL + parentEntityPath + "/" + subEntity."${parentKey}" + subEntityPath
            restClient.setUri(subEntityURL)

            try {
                sendEntity(excludedKeys, subEntity)
            } catch (Exception e) {
                log.error("An exception occurred processing the current Entity :" + JsonOutput.toJson(subEntity), e )
            }

            ++count
            if (count % MigrationConstants.COLLECTION_FEEDBACK_SIZE == 0) {
                startTime = caculateEntityCreationRate(startTime)
                log.info("${count} of ${arraySize} ${subName} completed")
            }
        }
        totalEntityCount += count
    }



    private void sendSimpleEntity(entityArray, excludedKeys, typeNamePlural) {
        def response, aspenId, mercuryId

        def arraySize = entityArray.size
        def count = 0
        long startTime = System.nanoTime();

        for (entity in entityArray) {
            sendEntity(excludedKeys, entity)

            ++count
            if( count % MigrationConstants.COLLECTION_FEEDBACK_SIZE == 0) {
                startTime = caculateEntityCreationRate(startTime)
                log.info("${count} of ${arraySize} ${typeNamePlural} completed")
            }
        }
        totalEntityCount += count
    }

    private void postEntityList(entityArray, excludedKeys, typeNamePlural) {
        def response, aspenId, mercuryId

        def arraySize = entityArray.size
        def count = 0
        long startTime = System.nanoTime();

        for (entity in entityArray) {
            postEntity(excludedKeys, entity)

            ++count
            if( count % MigrationConstants.COLLECTION_FEEDBACK_SIZE == 0) {
                startTime = caculateEntityCreationRate(startTime)
                log.info("${count} of ${arraySize} ${typeNamePlural} completed")
            }
        }
        totalEntityCount += count
    }



    private void sendEntity(excludedKeys, entity) {
        def mercuryId
        Object response = postEntity(excludedKeys, entity)

        // entity[MigrationConstants.ASPENID] = aspenId

        // Add Mercury Id
        mercuryId = response.data[MigrationConstants.MERCURY_ID]
        entity[MigrationConstants.MERCURY_ID] = mercuryId
    }

    private Object postEntity(excludedKeys, entity) {
        def mercuryId, response

        def excludedMap = [:]
        excludedMap.clear()
        excludedKeys.each {
            excludedMap[it] = entity.remove(it);
        }

        try {
            try {
                response = restClient.post(
                        body: entity
                )
            } catch (SocketTimeoutException se) {
                log.warn("Socket Timeout .. retrying")

                response = restClient.post(
                        body: entity
                )
            }
        } catch (HttpResponseException e) {
            log.error("Encountered error getting response: " + e.toString())
            log.error("The body is: " + JsonOutput.toJson(entity))
            log.error("The extra key values are: " + JsonOutput.toJson(excludedMap))
            log.error("The URL is: " +  restClient.getUri())
            log.error("The error is: " + e.getResponse().responseData.message)

        }

        // Add back Aspen Id
        excludedKeys.each {
            entity[it] = excludedMap[it]
        }
        response
    }

    private void putSubEntity(ArrayList subEntityArray, String parentEntityPath, String subEntityPath, ArrayList<String> excludedKeys, String parentKey, String subName) {
        def subEntityURL
        def arraySize = subEntityArray?.size()
        def count = 0
        long startTime = System.nanoTime();

        for (subEntity in subEntityArray) {
            subEntityURL = restURL + parentEntityPath + "/" + subEntity."${parentKey}" + subEntityPath
            restClient.setUri(subEntityURL)

            try {
                putEntity(excludedKeys, subEntity)
            } catch (Exception e) {
                log.error("An exception occurred processing the current Entity :" + JsonOutput.toJson(subEntity), e )
            }

            ++count
            if (count % MigrationConstants.COLLECTION_FEEDBACK_SIZE == 0) {
                startTime = caculateEntityCreationRate(startTime)
                log.info("${count} of ${arraySize} ${subName} completed")
            }
        }
        totalEntityCount += count
    }


    private Object putEntity(excludedKeys, entity) {
        def mercuryId, response

        def excludedMap = [:]
        excludedMap.clear()
        excludedKeys.each {
            excludedMap[it] = entity.remove(it);
        }

        try {
            response = restClient.put(
                    body: entity
            )
        } catch (HttpResponseException e) {
            log.error("Encountered error getting response: " + e.toString())
            log.error("The body is: " + JsonOutput.toJson(entity))
            log.error("The extra key values are: " + JsonOutput.toJson(excludedMap))
            log.error("The error is: " + e.getResponse().responseData.message)

        }

        // Add back Aspen Id
        excludedKeys.each {
            entity[it] = excludedMap[it]
        }
        response
    }

    private void sendSubEntityQuery(ArrayList subEntityArray, String parentEntityPath, String subEntityPath,  String parentKey, String childParam, String subName) {
        def subEntityURL
        def arraySize = subEntityArray?.size()
        def count = 0
        long startTime = System.nanoTime();

        for (subEntity in subEntityArray) {
            subEntityURL = restURL + parentEntityPath + "/" + subEntity."${parentKey}" + subEntityPath + '?' + childParam + '=' + subEntity."${childParam}"
            restClient.setUri(subEntityURL)

            sendEntityByQuery(subEntity, childParam)

            ++count
            if (count % MigrationConstants.COLLECTION_FEEDBACK_SIZE == 0) {
                startTime = caculateEntityCreationRate(startTime)
                log.info("${count} of ${arraySize} ${subName} completed")
            }
        }
        totalEntityCount += count
    }

    private void sendEntityByQuery(entity, queryParam) {
        def mercuryId, response
        def queryString = entity."${queryParam}"


        try {
            response = restClient.post(
                    contentType: JSON
            )
        } catch (HttpResponseException e) {
            log.error("Encountered error getting response: " + e.toString())
            log.error("The URI is: " +  JsonOutput.toJson(restClient.getUri() ))
            log.error("The entity is: " + JsonOutput.toJson(entity))
            log.error("The query param is: " + JsonOutput.toJson(queryParam))
            log.error("The error is: " + e.getResponse().responseData.message)
        }
    }

    private def requestDataByGet() {
         def response

        try {
            response = restClient.get(contentType: JSON)
        } catch (HttpResponseException e) {
            log.error("Encountered an error: " + e.getResponse().responseData.message)
        }

       return response.data
    }


    private long caculateEntityCreationRate(long startTime) {
        long elapsedTime = System.nanoTime() - startTime;
        long totalTimeInSeconds = elapsedTime / 1000000
        def calculatedRate =1000*( MigrationConstants.COLLECTION_FEEDBACK_SIZE / totalTimeInSeconds)

//        if(calculatedRate < 2) {
//            sleep(1000 * 60)
//            loginAndGetToken()
//        }
        log.info("The current rate Entities/Second is: " + calculatedRate)
        startTime = System.nanoTime()
        return startTime
    }

    private void closeSession(){

        def response
        def sessionURL = restURL + "/sessions"
        restClient.setUri(sessionURL)

        try {
            response = restClient.delete (
                    contentType: JSON
            )
        } catch (Exception e) {
            log.error("Encountered error logging out of Mercury: " + e.getResponse().responseData.message)
        }
    }


    private void loginAndGetToken() {
        def response
        def token
        def savedURI

        savedURI = restClient.getUri()

        def sessionURL = restURL + "/sessions"
        restClient.setUri(sessionURL)

        try {
            response = restClient.post(
                    body: ["tenantId": tenantId, "login": "admin", "password": "admin"]
            )
        } catch (Exception e) {
            log.error("Encountered error logging into Mercury: " + e.getResponse().responseData.message)
            return
        }

        token = response.data.token
        log.info("The token is ${token}")
        restClient.defaultRequestHeaders.'EmlogisToken' = token
        restClient.setUri(savedURI)

    }
}
