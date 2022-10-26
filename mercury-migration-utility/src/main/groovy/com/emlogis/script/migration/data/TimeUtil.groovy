package com.emlogis.script.migration.data

import com.emlogis.script.migration.MigrationConstants
import groovy.util.logging.Log4j
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter;

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 10/16/14
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */

@Log4j
class TimeUtil {

    /**
     * Convert from Joda Day of week value to Emlogis Day of week value
     *
      * @param jodaDayOfWeek
     * @return
     */
  static int getHickoryDayWeekValue(int jodaDayOfWeek) {

       int retVal = 0;
       // Sunday, Monday, Tues, Wed, Thurs, Friday, Sat
       def dayValuesArray = [7:0,1:1, 2:2, 3:3, 4:4,5:5, 6:6 ]

       return dayValuesArray.get(jodaDayOfWeek)
    }

    /**
     * Method: getDayValuesFromBinary
     *
     *  The day values for Coupled days off and days after were stored in binary
     *  Examples:
     *
     * DaysBefore = 24 = 16+8 --> In binary: 11000 = Thursday and Friday
     * DaysAfter = 3 = 2 +1 --> In binary: 11 = Monday and Tuesday
     *
     * This method converts these values into the usual Day Integer values (i.e. Monday = 1, Friday =5)
     * Examples:
     *
     * 24 -> "4, 5"
     * 3 -> "1, 2"
     *
     * This calculation is only necessary for days Mon-Fri
     *
     * @param binaryValue
     * @return  Comma separated string of day integer values
     */
    public static String getDayValuesFromBinary (int binaryValue) {
        def retVal = ""

        // Monday, Tuesday, Wednesday, Thursday, Friday
        def dayValuesArray = [1:1,2:2, 4:3, 8:4, 16:5 ]

        for(dayMap in dayValuesArray) {
            if(dayMap.key & binaryValue) {
                if(retVal.size() > 0) retVal += ", "
                retVal += dayMap.value
            }
        }

      return retVal
    }

    public static List getMercuryCDMap(employee, aspenCIAvailArray, availabilityDate, availabilityStatus, mercuryStartTime ,mercuryEndTime) {
        def cdMap = [:]

        DateTime startDateTime
        DateTime mercuryDateTime
        def durationInMinutes
        LocalTime startTime
        def availabilityType
        def cdList = []

        def aspenEmployeeId = employee.aspenId

        boolean durationIsAWholeDay = false

        startDateTime = DateTime.parse(availabilityDate)

        switch(availabilityStatus) {
            case 2:
                cdMap['availabilityType'] = 'UnAvail'
                startTime = LocalTime.MIDNIGHT
                cdMap['startTime'] = startTime
                cdMap['isPTO'] = true
                durationIsAWholeDay = true
                cdMap['durationIsAWholeDay'] = durationIsAWholeDay

                cdList.add(cdMap)
                break;

            case 1:
                cdMap['availabilityType'] = 'UnAvail'

                if(mercuryStartTime == -1) {
                    startTime = LocalTime.MIDNIGHT
                    durationIsAWholeDay = true
                }  else {
                    ( startTime, durationInMinutes) = calculateMercuryDuration(mercuryStartTime, mercuryEndTime)
                    cdMap['durationInMinutes'] = durationInMinutes
                }
                cdMap['startTime'] = startTime
                cdMap['durationIsAWholeDay'] = durationIsAWholeDay
                cdList.add(cdMap)
                break

            case 0:
                cdMap['availabilityType'] = 'Avail'

                // If the Hickory avail is for the whole day, we will pass it as is
                // otherwise we need to convert the avail to 1 or 2 UnAvail timeframes for Mercury
                if(mercuryStartTime == -1) {

                    // Only want to create a full day CD Avail if there is matching CI on that day for this employee
                    if(checkCIExistsForFullAvail(startDateTime, employee.aspenId, aspenCIAvailArray)) {
                        cdMap['startTime'] = LocalTime.MIDNIGHT
                        cdMap['durationIsAWholeDay'] = true
                        cdList.add(cdMap)
                    }  else {
                        // Did not find a matching CI
                        log.warn("No Matching CI to this CD All Day Avail Record - employee ID: " + employee.aspenId + ", date: " + availabilityDate)
                    }

                }  else {
                    ( startTime, durationInMinutes) = calculateMercuryDuration(mercuryStartTime, mercuryEndTime)

                    LocalTime endOfDay = LocalTime.MIDNIGHT.minusMinutes(1) // 23:59
                    LocalTime endTime = startTime.plusMinutes(durationInMinutes)

                    if(startTime > LocalTime.MIDNIGHT) {
                        cdMap['availabilityType'] = 'UnAvail'
                        cdMap['durationIsAWholeDay']  = false
                        cdMap['startTime'] = LocalTime.MIDNIGHT
                        cdMap['durationInMinutes'] = Minutes.minutesBetween(LocalTime.MIDNIGHT, startTime).getMinutes()
                        cdList.add(cdMap)
                    }

                    cdMap = [:]

                   if(endTime < endOfDay && endTime != LocalTime.MIDNIGHT) {
                       cdMap['availabilityType'] = 'UnAvail'
                       cdMap['durationIsAWholeDay']  = false
                       cdMap['startTime'] = endTime
                       cdMap['durationInMinutes'] = Math.abs( Minutes.minutesBetween(endTime, endOfDay).getMinutes() ) + 1
                       cdList.add(cdMap)
                   }
                }

               break

            default:
                cdMap = null

                log.error("Invalid Aspen availabilityStatus: " + availabilityStatus)
        }

        setCDAvailStartDateTime(startDateTime, cdList)

        return cdList
    }

    private static void setCDAvailStartDateTime(startDateOnly, List cdMapList)  {

        DateTime startDateTime
        LocalTime startTime
        for(cdMap in cdMapList) {
            startTime = cdMap['startTime']

            startDateTime = new LocalDate(startDateOnly).toDateTime(startTime)
            cdMap['startDateTime'] = startDateTime.getMillis()

            // We are calculating duration this way to handle DST
            if(cdMap['durationIsAWholeDay']) {
                cdMap['durationInMinutes'] = Minutes.minutesBetween(startDateTime, startDateTime.plusDays(1)).getMinutes()
            }

            cdMap.remove("startTime")
            cdMap.remove("durationIsAWholeDay")
        }
    }

    /**
     *    Check that a CI record exist for this calendar day otherwise
     *    we really don't need a CD Avail record for the whole day
     * @return  boolean
     */
    public static Map checkCIExistsForFullAvail(startDateTime, employeeId, aspenCIAvailArray) {

        def ciItem = null
        int mercuryDay = new DateTime(startDateTime).getDayOfWeek()
        int legacyDay = getHickoryDayWeekValue(mercuryDay)

        ciItem = aspenCIAvailArray.find{it.WeekdayNumber == legacyDay && it.EmployeeID == employeeId}
        return ciItem
    }

    public static List getMercuryCIList(weekDayNumber, availabilityStatus, mercuryStartTime ,mercuryEndTime, untilDate) {
        def ciList = []

        def durationInMinutes
        def startTime
        def availabilityType
        def endTime
        def endDate

        DateTime defaultStartDateTime = new DateTime(2015,1,1,0,0,0,0)

        switch(availabilityStatus) {
            case 1:
                def ciMap = [:]
                ciMap['availabilityType'] = 'UnAvail'
                ciMap['dayOfTheWeek']  = weekDayNumber
                ciMap['startDateTime'] = defaultStartDateTime.getMillis()
                if(untilDate) {
                    ciMap['endDateTime'] = DateTime.parse(untilDate).getMillis()
                } else {
                    ciMap['endDateTime'] = 0
                }


                if(mercuryStartTime == -1) {
                    ciMap['startTime'] = LocalTime.MIDNIGHT.getMillisOfDay()
                    ciMap['durationInMinutes'] = DateTimeConstants.MINUTES_PER_DAY
                }  else {
                    caculateMercuryTime(mercuryStartTime, ciMap, mercuryEndTime)
                }
                ciList.add(ciMap)
                break

            case 0:
                if(mercuryStartTime == -1) {
                    // Available all day, don't have to calculate unavail
                }  else {

                   startTime =  get24HourTime(mercuryStartTime)

                    if(startTime.isAfter(LocalTime.MIDNIGHT)) {
                        def ciMap = [:]
                        ciMap['startDateTime'] = defaultStartDateTime.getMillis()
                        ciMap['dayOfTheWeek']  = weekDayNumber
                        ciMap['availabilityType'] = 'UnAvail'
                        ciMap['startTime'] = LocalTime.MIDNIGHT.getMillisOfDay()
                        ciMap['durationInMinutes'] = Minutes.minutesBetween(LocalTime.MIDNIGHT, startTime).getValue()
                        if(untilDate) {
                            ciMap['endDateTime'] = DateTime.parse(untilDate).getMillis()
                        } else {
                            ciMap['endDateTime'] = 0
                        }
                        ciList.add(ciMap)
                    }

                    if(mercuryEndTime < 2400){
                        endTime = get24HourTime(mercuryEndTime)
                        def ciMap = [:]
                        ciMap['startDateTime'] = defaultStartDateTime.getMillis()
                        ciMap['availabilityType'] = 'UnAvail'
                        ciMap['dayOfTheWeek']  = weekDayNumber
                        ciMap['startTime'] = endTime.getMillisOfDay()
                        ciMap['startDateTime'] = defaultStartDateTime.getMillis()

                        // We are adding a extra minuteto calculate end time to the beginning of the next day
                        // which is the Emlogis convention
                        ciMap['durationInMinutes'] = Minutes.minutesBetween(endTime, LocalTime.MIDNIGHT.minusMinutes(1)).getValue() + 1

                        if(untilDate) {
                            ciMap['endDateTime'] = DateTime.parse(untilDate).getMillis()
                        } else {
                            ciMap['endDateTime'] = 0
                        }


                        ciList.add(ciMap)
                    }
                }

                break
        }
        return ciList
    }



    private static void caculateMercuryTime(mercuryStartTime, Map cdMap, mercuryEndTime) {
        def (LocalTime startTime, int duration) = calculateMercuryDuration(mercuryStartTime, mercuryEndTime)

        cdMap['startTime'] = startTime.getMillisOfDay()
        cdMap['durationInMinutes'] = duration
    }

    public static List calculateMercuryDuration(mercuryStartTime, mercuryEndTime) {
        def startTime, endTime, duration
        startTime = get24HourTime(mercuryStartTime)

        if (mercuryEndTime >= 2400) {
            endTime = new LocalTime(23, 59)
            duration = Minutes.minutesBetween(startTime, endTime).getValue() + 1
        } else {
            endTime = get24HourTime(mercuryEndTime)
            duration = Minutes.minutesBetween(startTime, endTime).getValue()
        }
        [startTime, duration]
    }

    public static LocalTime get24HourTime(aspenTimeInt) {
        LocalTime retVal
        def aspenHours
        def aspenMinutes

        aspenHours = (int) ( new Integer(aspenTimeInt)/ MigrationConstants.ASPEN_HOUR_MULTIPLIER)
        aspenMinutes = (int) ((( new Integer(aspenTimeInt) % MigrationConstants.ASPEN_HOUR_MULTIPLIER)  * MigrationConstants.MINUTES_PER_HUOR) / MigrationConstants.ASPEN_HOUR_MULTIPLIER)

        retVal = new LocalTime(aspenHours,aspenMinutes)

        return retVal
    }

    public static DateTime getNextDayStart(DateTime currentDateTime) {
        DateTime retval = null;

        if(currentDateTime != null) {
            retval = new DateTime(currentDateTime.getYear(), currentDateTime.getMonthOfYear(), currentDateTime.getDayOfMonth(),
                    0, 0, 0).plus(Days.ONE);
        }
    }

    public static DateTime getStartOfDay(DateTime currentDateTime) {
        DateTime retval = null;

        if(currentDateTime != null) {
            retval = new DateTime(currentDateTime.getYear(), currentDateTime.getMonthOfYear(), currentDateTime.getDayOfMonth(),
                    0, 0, 0);
        }

        return retval;
    }


    public static DateTime getStartDateTime(requireDate, mercuryStartTime) {
        def addDayDueToMidnight
        LocalTime startTime
        DateTime shiftDate
        DateTime startDateTime

        shiftDate = DateTime.parse(requireDate)
        if (mercuryStartTime >= 2400) {
            log.info("Had to add a day to request date becuase starttime of ${mercuryStartTime}  ")
            mercuryStartTime = 0
            shiftDate = shiftDate.plusDays(1)
        }

        startTime = TimeUtil.get24HourTime(mercuryStartTime)

        startDateTime = shiftDate.plusMillis(startTime.getMillisOfDay())
        startDateTime
    }

    public static void main(String[] args) {


        def startTime = get24HourTime('2000')
        def duration =  240

        def  requestDate = '2014-06-17T05:00:00+0000'
        def requestedDateTime = getStartOfDay( DateTime.parse(requestDate) )

        def startDateTime = requestedDateTime.plusMillis(startTime.getMillisOfDay())
        def endDateTime = startDateTime.plusMinutes(duration)

        def requestedLADateTime = requestedDateTime.withZone(DateTimeZone.forID("America/Los_Angeles"))
        def requestedLondonDateTime = requestedDateTime.withZone(DateTimeZone.forID("Europe/London"))
        def requestedParisDateTime = requestedDateTime.withZone(DateTimeZone.forID("Europe/Paris"))
        def requestedUTCDateTime = requestedDateTime.withZone(DateTimeZone.forID("Etc/UTC"))
        def requestManilaDateTime = requestedDateTime.withZone(DateTimeZone.forID("Asia/Manila"))

        final DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss")

        print "The Houston request DateTime is: "  + outputFormatter.print(requestedDateTime)   + "\n\n"
        print "The LA request DateTime is: "  + outputFormatter.print(requestedLADateTime)   + "\n\n"

        print "The London request DateTime is: " + outputFormatter.print(requestedLondonDateTime) + "\n\n"
        print "The Paris request DateTime is: " + outputFormatter.print(requestedParisDateTime) + "\n\n"
        print "The Manila request DateTime is: " + outputFormatter.print(requestManilaDateTime) + "\n\n"

        print "The UTC request DateTime is: " + outputFormatter.print(requestedUTCDateTime) + "\n\n"

        [startDateTime,endDateTime]
        int testValue  = 3
        int testValue2 = 24

        println "The day value of ${testValue} would be " + TimeUtil.getDayValuesFromBinary(testValue)
        println "The day value of ${testValue2} would be " + TimeUtil.getDayValuesFromBinary(testValue2)

        def test3 ='1150'
        def test4 =  '0750'
        def test5 = '0600'
        println "test 3 is "  + get24HourTime(test3)
        println "test 4 is "  + get24HourTime(test4)
        println "test 5 is "  + get24HourTime(test5)

        DateTime startDate = new DateTime(2014, 3, 9, 0 , 0)
        Minutes durationMinutes = new Minutes(DateTimeConstants.MINUTES_PER_DAY)

        DateTime endTime = startDate.plus(durationMinutes)

        DateTime endTime2 = startDate.plus(durationMinutes + 1)

        DateTime endTime3 = startDate.plus(durationMinutes - 1)

        DateTime tmpDateTime = startDate.plusDays(1)

        DateTime beginningOfNextDay = getStartOfDay(startDate).plusMinutes(DateTimeConstants.MINUTES_PER_DAY);

        if(endTime.isAfter(beginningOfNextDay)) {
            println "Endtime is after beginging of next day"
        }

        if(endTime2.isAfter(beginningOfNextDay)) {
            println "endTime2 is after beginging of next day"
        }

        if(endTime3.isAfter(beginningOfNextDay)) {
            println "endTime3 is after beginging of next day"
        }

       // LocalTime startTime = get24HourTime("0400")
        LocalTime endTime4 = get24HourTime("2359")

        Minutes durationInMinutes = Minutes.minutesBetween(startTime,LocalTime.MIDNIGHT.minusMinutes(1))

        Minutes durationInMinutesStartingAtMidnight = Minutes.minutesBetween(LocalTime.MIDNIGHT, startTime)

        println "The start time is " + startTime.toString("HHmm")

        println("The minutes in between ending at midnight are : " + durationInMinutes.getValue())

        println("The minutes in between beginning at midnight are : " + durationInMinutesStartingAtMidnight.getValue())

    }

    public static int getMinutesFromLegacyValue(legacyValue) {
        int retVal = 0

        retVal = (legacyValue * 60)/100

        return retVal
    }


}
