package com.emlogis.script.migration.data

import com.emlogis.script.migration.MigrationConstants
import com.emlogis.script.migration.data.legacy.NotificationOptions
import com.emlogis.script.migration.data.legacy.NotificationType
import groovy.json.JsonOutput
import groovy.util.logging.Log4j
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 10/5/14
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
@Log4j
class EmployeeUtil {
    def
    static excludedFieldsArray = ['aspenId', 'IsPooled', 'IsSchedulable', 'IsActive', 'email', 'IsDeleted', 'MinHoursWeek',
                                  'MaxHoursWeek', 'MinHoursDay', 'MaxHoursDay', 'MaxDaysWeek', 'MinHoursWeekPrimarySkill', 'MaxConsecutiveDays', 'HomeTeamID',
                                  'RoleType_Description', 'BeginOvertimeTwoWeek', 'BeginOvertimeWeek', 'BeginOvertimeDay', 'LoginName', 'NotificationOptions']

    def static excludedFieldsUAArray = ['SiteEmployeeID', 'EmployeeID', 'LastName']

    def static excludedEmployeesArray = ['administrator', 'Beebe', 'sysadmin']
    def static updateFields = ['activityType', 'middleName', 'address', 'address2',
                               'city', 'state', 'zip', 'ecRelationship', 'ecPhoneNumber', 'emergencyContact', 'gender', 'homePhone', 'homeEmail', 'professionalLabel',
                               'primaryContactIndicator', 'hireDate', 'startDate', 'endDate', 'hourlyRate', 'mobilePhone']

    def static emailPattern = ~/(?i)^([a-z0-9'_.-]+)@([\da-z.-]+)[.]([a-z.]{2,6})$/
    def static loginPattern = ~/(?i)^[a-z0-9'+_.-]{3,60}[@]{0,1}[\da-z+_.-]*[.]*[a-z]{0,6}$/


    public static removeExcludedEmployees(employeeArray, adminUserAccounts, userAccountOnlyEmployees) {
        def employeesToRemove = []
        def employee

        for (excludedEmployee in excludedEmployeesArray) {
            employee = employeeArray.find { it.lastName == excludedEmployee }

            if (employee) {
                employeesToRemove.add(employee)
            }
        }

        employeeArray.removeAll(employeesToRemove)

        employeesToRemove = []

        // Remove Deleted employees
        for (nextEmployee in employeeArray) {
            if (nextEmployee.IsDeleted) {
                employeesToRemove.add(nextEmployee)
            }
        }

        employeeArray.removeAll(employeesToRemove)

        employeesToRemove = []

        for (adminUser in adminUserAccounts) {
            employee = employeeArray.find { it.aspenId == adminUser.EmployeeID }

            if (employee) {
                if (!employee.IsDeleted) {
                    userAccountOnlyEmployees.add(employee)
                }
                employeesToRemove.add(employee)
            }
        }

        employeeArray.removeAll(employeesToRemove)
    }

    public static updateAdminAccounts(adminEmployeeArray, userAccountArray) {
        def adminUserAccounts = []
        def adminUserAccount

        for (employee in adminEmployeeArray) {
            // Trim Employee Email
            adminUserAccount = findUserAccount(userAccountArray, employee)

            if (adminUserAccount) {

                adminUserAccount.workEmail = determineEmailAddress(adminUserAccount.email)

                adminUserAccount.firstName = employee.firstName.replaceAll("[^a-zA-Z0-9_-]+", "_") ?: null
                adminUserAccount.lastName = employee.lastName.replaceAll("[^a-zA-Z0-9_-]+", "_") ?: null
                adminUserAccount.middleName = employee.middleName.replaceAll("[^a-zA-Z0-9_-]+", "_") ?: null

                if (!loginPattern.matcher(adminUserAccount.login).matches()) {
                    log.debug("Invalid user account email for this user account: " + JsonOutput.toJson(adminUserAccount))
                    adminUserAccount.login = null
                }

                adminUserAccount.mobilePhone = employee.mobilePhone ?: null
                adminUserAccount.address = employee.address ?: null
                adminUserAccount.address2 = employee.address2 ?: null
                adminUserAccount.city = employee.city ?: null
                adminUserAccount.state = employee.state ?: null

                adminUserAccount.zip = employee.zip ?: null

                adminUserAccount.gender = employee.gender ?: null
                adminUserAccount.homePhone = employee.homePhone ?: null
                adminUserAccount.homeEmail = determineEmailAddress(employee.homeEmail)

                // Remove fields that don't match UA DTO
                adminUserAccount.remove("email")
                adminUserAccount.remove("name")
                adminUserAccount.remove("password")

                adminUserAccounts.add(adminUserAccount)

            } else {
                log.error("No user account for this Admin employee: " + JsonOutput.toJson(employee))
            }

        }
        adminUserAccounts
    }

    public static updateMercuryFields(employeeArray, userAccountArray) {
        boolean isSchedulable, isPooled, isActive
        def updateDto

        for (employee in employeeArray) {

            employee.firstName = employee.firstName.replaceAll("[^a-zA-Z0-9_-]+", "_") ?: null
            employee.lastName = employee.lastName.replaceAll("[^a-zA-Z0-9_-]+", "_") ?: null
            employee.middleName = employee.middleName.replaceAll("[^a-zA-Z0-9_-]+", "_") ?: null

            // Update Scheduable Fields
            isSchedulable = employee.IsSchedulable
            isPooled = employee.IsPooled

            if (isSchedulable && isPooled) {
                employee.activityType = 'Pooled'
            } else if (isSchedulable && !isPooled) {
                employee.activityType = 'Active'
            } else if (!isSchedulable && !isPooled) {
                employee.activityType = 'Inactive'
            } else if (!isSchedulable && isPooled) {
                // Invalid state
                employee.activityType = 'Inactive'
                log.error("Invalid isSchedulable and isPooled combination, activityType will be set Inactive: " + JsonOutput.toJson(employee))
            }

            // Transform dates to long
            if (employee.endDate) {
                employee.endDate = DateTime.parse(employee.endDate).getMillis()
            }
            if (employee.startDate) {
                employee.startDate = DateTime.parse(employee.startDate).getMillis()
            }
            if (employee.hireDate) {
                employee.hireDate = DateTime.parse(employee.hireDate).getMillis()
            }

            employee.email = determineEmailAddress(employee.email)
            // Move fields to update
            updateDto = [:]

            updateFields.each {
                updateDto[it] = employee.remove(it)
            }
            updateDto.workEmail = employee.email

            employee.put('updateDto', updateDto)

            // Look for matching userAccounts
            // for employees that are not deleted
            if (!employee.IsDeleted) {
                def userAccount = findUserAccount(userAccountArray, employee)

                if (userAccount) {

                    // Check for Valid Login
                    if (!loginPattern.matcher(userAccount.login).matches()) {
                        log.error("Invalid user account login for this user account:_" + JsonOutput.toJson(userAccount))
                        userAccount.login = null
                    }

                    if (userAccount.email) {
                        userAccount.email = userAccount.email.trim()

                        if (!emailPattern.matcher(userAccount.email).matches()) {
                            log.debug("Invalid user account email for this user account: " + JsonOutput.toJson(userAccount))
                            userAccount.email = null
                        }
                    } else {
                        userAccount.email = null
                    }

                    // Todo need to update password
                    userAccount.remove("password")

                    employee.put("userAccountDto", userAccount)

                } else {
                    log.error("Unable to find User account for this employee: " + JsonOutput.toJson(employee))
                }
            } else {
                // deleted employee
                updateDto.isDeleted = true
            }

        }   // Each employee
    } // updateMercuryFields

    private static Map findUserAccount(List userAccounts, Map employee) {
        def Map userAccount = null
        def inactivityPeriod

        for (account in userAccounts) {
            if (account['login']?.equalsIgnoreCase(employee.LoginName)) {
                userAccount = account
                break
            }
        }
        return userAccount
    }


    public static Long getInactivityPeriod(lastLoginString) {
        Long inactivityPeriod = null

        if (lastLoginString) {
            DateTime lastLogin = DateTime.parse(lastLoginString);
            DateTime currentTime = DateTime.now()

            Duration duration = new Duration(lastLogin, currentTime);

            inactivityPeriod = duration.getMillis()
        }

        return inactivityPeriod
    }

    public static List getContractLines(employeeArray) {
        def contractLineList = []

        for (employeeMap in employeeArray) {
            // check for hours per week
            if (employeeMap.MinHoursWeek || employeeMap.MaxHoursWeek) {
                def contractLine = [:]
                contractLine.contractLineType = 'HOURS_PER_WEEK'
                contractLine.name = 'HOURS_PER_WEEK'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id
                if (employeeMap.MinHoursWeek) {
                    contractLine.minimumEnabled = true
                    contractLine.minimumValue = employeeMap.MinHoursWeek * 60
                    contractLine.minimumWeight = -1
                } else {
                    contractLine.minimumEnabled = false
                }

                if (employeeMap.MaxHoursWeek) {
                    contractLine.maximumEnabled = true
                    contractLine.maximumValue = employeeMap.MaxHoursWeek * 60
                    contractLine.maximumWeight = -1
                } else {
                    contractLine.maximumEnabled = false
                }

                contractLineList.add(contractLine)
            }

            // check for hours per day
            if (employeeMap.MinHoursDay || employeeMap.MaxHoursDay) {
                def contractLine = [:]
                contractLine.contractLineType = 'HOURS_PER_DAY'
                contractLine.name = 'HOURS_PER_DAY'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id
                if (employeeMap.MinHoursDay) {
                    contractLine.minimumEnabled = true
                    contractLine.minimumValue = employeeMap.MinHoursDay * 60
                    contractLine.minimumWeight = -1
                } else {
                    contractLine.minimumEnabled = false
                }

                if (employeeMap.MaxHoursDay) {
                    contractLine.maximumEnabled = true
                    contractLine.maximumValue = employeeMap.MaxHoursDay * 60
                    contractLine.maximumWeight = -1
                } else {
                    contractLine.maximumEnabled = false
                }

                contractLineList.add(contractLine)
            }

            // check for max days per week
            if (employeeMap.MaxDaysWeek) {
                def contractLine = [:]
                contractLine.contractLineType = 'DAYS_PER_WEEK'
                contractLine.name = 'DAYS_PER_WEEK'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = employeeMap.MaxDaysWeek
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

            // check for min hours on primary skill
            if (employeeMap.MinHoursWeekPrimarySkill) {
                def contractLine = [:]
                contractLine.contractLineType = 'HOURS_PER_WEEK_PRIME_SKILL'
                contractLine.name = 'HOURS_PER_WEEK_PRIME_SKILL'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id

                contractLine.minimumEnabled = true
                contractLine.minimumValue = employeeMap.MinHoursWeekPrimarySkill * 60
                contractLine.minimumWeight = -1

                contractLine.maximumEnabled = false

                contractLineList.add(contractLine)
            }

            // check for max consecutive days
            if (employeeMap.MaxConsecutiveDays && employeeMap.MaxConsecutiveDays != MigrationConstants.MAX_CONSECUTIVE_DAYS_NA) {
                def contractLine = [:]
                contractLine.contractLineType = 'CONSECUTIVE_WORKING_DAYS'
                contractLine.name = 'CONSECUTIVE_WORKING_DAYS'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = employeeMap.MaxConsecutiveDays
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

            // check for Daily Overtime Contraint
            if (employeeMap.BeginOvertimeDay && employeeMap.BeginOvertimeDay > MigrationConstants.emptyValue &&
                    employeeMap.BeginOvertimeWeek < MigrationConstants.naValue) {
                def contractLine = [:]
                contractLine.contractLineType = 'DAILY_OVERTIME'
                contractLine.name = 'DAILY_OVERTIME'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = employeeMap.BeginOvertimeDay
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

            // check for Daily Overtime Contraint
            if (employeeMap.BeginOvertimeWeek && employeeMap.BeginOvertimeWeek > MigrationConstants.emptyValue &&
                    employeeMap.BeginOvertimeWeek < MigrationConstants.naValue) {
                def contractLine = [:]
                contractLine.contractLineType = 'WEEKLY_OVERTIME'
                contractLine.name = 'WEEKLY_OVERTIME'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = employeeMap.BeginOvertimeWeek
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

            // check for Daily Overtime Contraint
            if (employeeMap.BeginOvertimeTwoWeek && employeeMap.BeginOvertimeTwoWeek > MigrationConstants.emptyValue &&
                    employeeMap.BeginOvertimeWeek < MigrationConstants.naValue) {
                def contractLine = [:]
                contractLine.contractLineType = 'TWO_WEEK_OVERTIME'
                contractLine.name = 'TWO_WEEK_OVERTIME'
                contractLine.contractId = employeeMap.contractId
                contractLine.employeeId = employeeMap.id

                contractLine.maximumEnabled = true
                contractLine.maximumValue = employeeMap.BeginOvertimeTwoWeek
                contractLine.maximumWeight = -1

                contractLine.minimumEnabled = false

                contractLineList.add(contractLine)
            }

        }   // Iterate though employee array

        return contractLineList;
    }

    public static boolean processWeekDayRotationContractLines(employeeArray, weekdayArray) {
        def contractId
        def employee
        boolean status = true

        for (weekdayRotation in weekdayArray) {
            // find the contractId
            employee = employeeArray.find { it.aspenId == weekdayRotation.EmployeeID }

            if (employee) {
                weekdayRotation.contractId = employee.contractId
                weekdayRotation.rotationType = 'DAYS_OFF_PATTERN'
                weekdayRotation.contractLineType = 'CUSTOM'
                weekdayRotation.weight = -1

                switch (weekdayRotation.RotationValue) {
                    case 2:
                        weekdayRotation.numberOfDays = 1
                        weekdayRotation.outOfTotalDays = 2
                        break

                    case 3:
                        weekdayRotation.numberOfDays = 1
                        weekdayRotation.outOfTotalDays = 3
                        break

                    case 4:
                        weekdayRotation.numberOfDays = 2
                        weekdayRotation.outOfTotalDays = 4
                        break

                    default:
                        log.error("Unknown value for weekday Rotation:" + JsonOutput.toJson(weekdayRotation))
                }
            } else {
                log.error("Cannot find an employee for this weekday rotation: " + weekdayRotation)
                status = false
            }
        }

        return status

    }

    public static boolean processWeekendRotationContractLines(employeeArray, weekendRotationArray) {
        def contractId
        def employee
        boolean status = true

        for (weekendRotation in weekendRotationArray) {
            // find the contractId
            employee = employeeArray.find { it.aspenId == weekendRotation.EmployeeID }

            if (employee) {
                weekendRotation.contractId = employee.contractId

                // Set the remaining fields
                weekendRotation.contractLineType = 'COMPLETE_WEEKENDS'
                weekendRotation.daysOffAfter = TimeUtil.getDayValuesFromBinary(weekendRotation.daysOffAfter)
                weekendRotation.daysOffBefore = TimeUtil.getDayValuesFromBinary(weekendRotation.daysOffBefore)
                weekendRotation.weight = -1
            } else {
                log.error("Cannot find an employee for this weekendRotation rotation: " + weekendRotation)
                status = false
            }
        }
        return status
    }

    public static List getEmployeeSkillList(employeeArray, aspenEmployeeSkillsArray) {
        def mercuryEmployeeSkills = []
        def employeeSkill
        def employee
        def skill
        def dupEmployeeSkill

        for (aspenEmployeeSkill in aspenEmployeeSkillsArray) {
            employee = employeeArray.find { it.aspenId == aspenEmployeeSkill.EmployeeID }

            if (employee) {
                // We found a matching Employee and skill so we can create a mercury skill
                employeeSkill = [:]
                employeeSkill.isPrimarySkill = aspenEmployeeSkill.IsPrimary
                employeeSkill.employeeId = employee."${MigrationConstants.MERCURY_ID}"
                employeeSkill.skillScore = aspenEmployeeSkill.SkillScore

                if (employeeSkill.skillScore == null) {
                    employeeSkill.skillScore = 0
                }

                // find skill

                skill = SkillUtil.getUniqueSkill(aspenEmployeeSkill.SkillID)

                if (skill) {
                    employeeSkill.skillId = skill.id

                    // check that we don't have a duplicate
                    dupEmployeeSkill = mercuryEmployeeSkills.find {
                        it.skillId == employeeSkill.skillId && it.employeeId == employeeSkill.employeeId
                    }

                    if (!dupEmployeeSkill) {
                        mercuryEmployeeSkills.add(employeeSkill)
                    } else {
                        log.error("Duplicate Employee Skill Ignored: " + JsonOutput.toJson(employeeSkill))
                    }
                } else {
                    log.error("No matching skill for EmployeeSkill: " + JsonOutput.toJson(aspenEmployeeSkill))
                }
            } // check for employee
        }

        return mercuryEmployeeSkills
    }

    public static List getNotificationOptions(employeeArray) {
        log.info("Creating Employee Notifications Options")

        def employeeNotificationList = []

        for (employeeMap in employeeArray) {
            def employeeNotification = [:]
            def legacyNotificationOptions = employeeMap.NotificationOptions

            employeeNotification.employeeId = employeeMap.id

            employeeNotification.mobilePhone = null
            employeeNotification.homeEmail = null
            employeeNotification.workEmail = null
            boolean isNotificationEnabled = isBitSet(NotificationOptions.NOTIFICATIONS_ENABLED.getBitMask(), legacyNotificationOptions)

            employeeNotification.isNotificationEnabled = isNotificationEnabled

            def notificationConfigs = []

            def notificationConfig = [:]
            notificationConfig = getNotificationConfig(notificationConfig, legacyNotificationOptions, NotificationOptions.USE_SMS,
                    "SMS", "SMS_TEXT")
            notificationConfigs.add(notificationConfig)

            notificationConfig = [:]
            notificationConfig = getNotificationConfig(notificationConfig, legacyNotificationOptions, NotificationOptions.USE_CORPORATE_EMAIL_ADDRESS,
                    "CorporateEmail", "HTML")
            notificationConfigs.add(notificationConfig)

            notificationConfig = [:]
            notificationConfig = getNotificationConfig(notificationConfig, legacyNotificationOptions, NotificationOptions.USE_PERSONAL_EMAIL_ADDRESS,
                    "PersonalEmail", "HTML")
            notificationConfigs.add(notificationConfig)

            employeeNotification.notificationConfigs = notificationConfigs

            def notificationTypeMap = [:]

            for (NotificationType notificationType : NotificationType.values()) {
                boolean enabled = isBitSet(notificationType.getBitMask(), legacyNotificationOptions)
                notificationTypeMap.put(notificationType.name(), enabled)
            }

            employeeNotification.notificationTypes = notificationTypeMap

            employeeNotificationList.add(employeeNotification)
        }

        employeeNotificationList
    }

    private
    static LinkedHashMap getNotificationConfig(notificationConfig, long legacyNotificationOptions, NotificationOptions option, String method, String format) {
        boolean enabled = isBitSet(option.getBitMask(), legacyNotificationOptions)

        notificationConfig.format = format
        notificationConfig.method = method
        notificationConfig.enabled = enabled
        notificationConfig
    }

    public static boolean isBitSet(long notificationValue, long checkValue) {
        return (notificationValue & checkValue) > 0;
    }

    public static long setBit(long notificationValue, long setValue) {
        return (notificationValue | setValue);
    }

    public
    static List getCDAvailabilityList(employeeArray, aspenCdAvailabilityList, aspenCIAvailArray, absenceTypeList) {
        def mercuryCDAvailabilityList = []
        def aspenCDAvailUniqueList = []
        def employee

        def mercuryCDAvailItemList = []
        def absenceType
        def nextAvailItem
        def duplicateCount = 0

        // Remove duplicates
        log.info("Checking for CD Duplicates")
        for (availItem in aspenCdAvailabilityList) {
            nextAvailItem = aspenCDAvailUniqueList.find {
                it.EmployeeID == availItem.EmployeeID &&
                        it.AvailabilityDate == availItem.AvailabilityDate && it.StartTime == availItem.StartTime &&
                        it.EndTime == availItem.EndTime
            }

            if (nextAvailItem) {
                log.error("Duplicate CD Avail Items found: " + ++duplicateCount)
                log.error(JsonOutput.toJson(availItem))
                log.error(JsonOutput.toJson(nextAvailItem))
            } else {
                aspenCDAvailUniqueList.add(availItem)
            }

        }

        for (availItem in aspenCDAvailUniqueList) {
            // find the employee
            employee = employeeArray.find { it.aspenId == availItem.EmployeeID }

            if (employee) {
                mercuryCDAvailItemList = TimeUtil.getMercuryCDMap(employee, aspenCIAvailArray, availItem.AvailabilityDate, availItem.AvailabilityStatus,
                        availItem.StartTime, availItem.EndTime);

                for (mercuryCDAvailItem in mercuryCDAvailItemList) {
                    if (!mercuryCDAvailItem) continue

                    mercuryCDAvailItem.employeeId = employee.id

                    if (availItem.AbsenceTypeID) {
                        absenceType = absenceTypeList.find { it.aspenId == availItem.AbsenceTypeID }

                        if (absenceType) {
                            mercuryCDAvailItem.absenceTypeId = absenceType.id

                        } else {
                            log.error("No matching Absence Type for Aspen CD Avail item : " + JsonOutput.toJson(availItem))
                        }
                    }
                    mercuryCDAvailabilityList.add(mercuryCDAvailItem)
                }
            } else {
                log.error("No matching employee for Aspen CD Avail item : " + JsonOutput.toJson(availItem))
            }
        }

        return mercuryCDAvailabilityList
    }

    public static List getCIAvailabilityList(employeeArray, aspenCIAvailabilityList) {
        def mercuryCIAvailabilityList = []
        def employee
        def mercuryCIAvailList


        for (availItem in aspenCIAvailabilityList) {
            employee = employeeArray.find { it.aspenId == availItem.EmployeeID }

            if (employee) {
                try {
                    // This will be a list now and I'll need to handle an empty list
                    mercuryCIAvailList = TimeUtil.getMercuryCIList(availItem.WeekdayNumber, availItem.AvailabilityStatus,
                            availItem.StartTime, availItem.EndTime, availItem.UntilDate)
                } catch (Exception e) {
                    log.error("An exception occurred processing the current CI Item:" + JsonOutput.toJson(availItem))
                }

                for (mercuryCIAvailIem in mercuryCIAvailList) {
                    mercuryCIAvailIem.employeeId = employee.id
                    mercuryCIAvailabilityList.add(mercuryCIAvailIem)
                }
            } else {
                log.error("No matching employee for Aspen CD Avail item : " + JsonOutput.toJson(availItem))
            }
        }


        return mercuryCIAvailabilityList
    }

    public static List getEmployeeAdmins(employeeList) {
        List employeeAdmins = []

        employeeAdmins = employeeList.findAll { it.RoleType_Description == 'Administrator' && (!it?.IsDeleted) }
        return employeeAdmins
    }

    public static determineEmailAddress(legacyEmail) {
        String checkedEmail = legacyEmail?.trim() ?: null

        if (checkedEmail && !emailPattern.matcher(checkedEmail).matches()) {
            log.error("Invalid Employee or User Account Email address: " + checkedEmail)
            checkedEmail = null
        }
        checkedEmail
    }
}
