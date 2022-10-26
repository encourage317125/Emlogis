package com.emlogis.script.migration

import groovy.sql.Sql
import org.joda.time.DateTime

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 11/12/14
 * Time: 4:24 PM
 * To change this template use File | Settings | File Templates.
 */


// Setup MercurySQL

def databaseURL = "jdbc:mysql://localhost:3308/EGS_Migration_LCSO_Assigned_Shifts"
def databaseUser = "root"
def databasePassword = "root"
def databaseDriver =  "com.mysql.jdbc.Driver"


def sql = Sql.newInstance(databaseURL, databaseUser, databasePassword, databaseDriver)

// Setup Hickory SQL

def databaseHickURL = "jdbc:jtds:sqlserver://127.0.0.1:1433;databaseName=lcsoGenerator"
def databasHickeUser = "sa"
def databaseHIckPassword = "EmLogis123"
def databaseHIckDriver =  "net.sourceforge.jtds.jdbc.Driver"

def sqlHick = Sql.newInstance(databaseHickURL, databasHickeUser, databaseHIckPassword, databaseHIckDriver)

def count = 0
def matcher
def ciAvailSet = [] as Set

def inCIAvail= false

def employeeIdMap = [:]
def employeeNameList = []
def employeeNameMap

def logFile = new File('C:\\\\dev\\\\migration\\\\logs\\\\migration_LCSO_Assigned_Shifts.log').eachLine { line ->

    if(line ==~ /.*CI Availability Timeframes.*/)  {
        inCIAvail = true;
    }

    if(line ==~ /.*Teams completed.*/) {
        inCIAvail = false;
    }

    if(inCIAvail) {
        if( (matcher = line =~ /\"employeeId\":\"(.*)\"/) ) {
            println("The next employee ID is: " + matcher[0][1])
            ciAvailSet.add(matcher[0][1])
        }
    }
}

def ciAvailWhere  = 'where '
def ciAvailCount = 0

for(employeeId in ciAvailSet) {
    employeeIdMap['enployeeId'] = employeeId

    // Query Mercury for Employee Names
    sql.eachRow('select lastName, firstName from Employee where Employee.id =:enployeeId', employeeIdMap){ row->
       employeeNameMap = [:]
       employeeNameMap['lastName']   = row.lastName
       employeeNameMap['firstName']   = row.firstName
       employeeNameList.add(employeeNameMap)

        if(ciAvailCount++) {
            ciAvailWhere += "OR\n"
        }

       ciAvailWhere +=  "(dbo.T_Employee.LastName = '${row.lastName}' AND dbo.T_Employee.FirstName = '${row.firstName}') "


    }


}


def ciAvailUserSQL = """  SELECT
    dbo.T_EmployeeCIAvailability.WeekdayNumber,
    dbo.T_EmployeeCIAvailability.AvailabilityStatus,
    dbo.T_EmployeeCIAvailability.StartTime,
    dbo.T_EmployeeCIAvailability.EndTime,
    dbo.T_Employee.LastName,
    dbo.T_Employee.FirstName,
    dbo.T_Employee.MiddleName,
    dbo.T_Employee.IsActive,
    dbo.T_Employee.IsDeleted,
    dbo.T_Employee.IsSchedulable,
    dbo.T_Employee.IsPooled,
    dbo.T_EmployeeCIAvailability.EmployeeCIAvailabilityID,
    dbo.T_Employee.EmployeeID
FROM
    dbo.T_EmployeeCIAvailability
INNER JOIN
    dbo.T_Employee
ON
    (
        dbo.T_EmployeeCIAvailability.EmployeeID = dbo.T_Employee.EmployeeID)

${ciAvailWhere}

ORDER BY
    dbo.T_Employee.EmployeeID ASC,
    dbo.T_EmployeeCIAvailability.WeekdayNumber ASC   """

println "The CI SQL check is: \n\n" + ciAvailUserSQL