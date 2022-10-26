package com.emlogis.script.migration.data.proto

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.Minutes
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * Created by rjackson on 1/6/2015.
 */
class TimeDSTExample {

    public static void main(String[] args) {
        DateTimeZone.setDefault(DateTimeZone.forID("America/Chicago"));
        DateTimeZone tz = DateTimeZone.getDefault();

        final DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss")
        final DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss:ssZ")

        Minutes dayDurationMinutes = Minutes.minutes(1440)

        // Starts just before time change
        DateTime cdAvail1StartDateTime = DateTime.parse("2014-03-08 00:00:00", inputFormatter)
        DateTime cdAvail2StartDateTime = DateTime.parse("2014-03-09 00:00:00", inputFormatter)
        DateTime cdAvail3StartDateTime = DateTime.parse("2014-03-10 00:00:00", inputFormatter)

        System.out.println "Using current CD Avail Calculations"
        System.out.println "\n"
        System.out.println "The cdAvail1StartDateTime is: " + cdAvail1StartDateTime.toString(outputFormatter)
        System.out.println "The cdAvail2StartDateTime is: " + cdAvail2StartDateTime.toString(outputFormatter)
        System.out.println "The cdAvail3StartDateTime is: " + cdAvail3StartDateTime.toString(outputFormatter)

        DateTime cdAvail1EndDateTime  =  cdAvail1StartDateTime.plus(dayDurationMinutes)
        DateTime cdAvail2EndDateTime  =  cdAvail2StartDateTime.plus(dayDurationMinutes)
        DateTime cdAvail3EndDateTime  =  cdAvail3StartDateTime.plus(dayDurationMinutes)

        System.out.println "\n"
        System.out.println "The cdAvail1EndDateTime is: " + cdAvail1EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail2EndDateTime is: " + cdAvail2EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail3EndDateTime is: " + cdAvail3EndDateTime.toString(outputFormatter)

        DateTime cdAvail1StartDateTimeM = cdAvail1StartDateTime.toDateTime(DateTimeZone.forID("UTC"))
        DateTime cdAvail2StartDateTimeM = cdAvail2StartDateTime.toDateTime(DateTimeZone.forID("UTC"))
        DateTime cdAvail3StartDateTimeM = cdAvail3StartDateTime.toDateTime(DateTimeZone.forID("UTC"))

        System.out.println "\n"
        System.out.println "The cdAvail1StartDateTimeM is: " + cdAvail1StartDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail2StartDateTimeM is: " + cdAvail2StartDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail3StartDateTimeM is: " + cdAvail3StartDateTimeM.toString(outputFormatter)

        DateTime cdAvail1EndDateTimeM  =  cdAvail1StartDateTimeM.plus(dayDurationMinutes)
        DateTime cdAvail2EndDateTimeM  =  cdAvail2StartDateTimeM.plus(dayDurationMinutes)
        DateTime cdAvail3EndDateTimeM  =  cdAvail3StartDateTimeM.plus(dayDurationMinutes)

        System.out.println "\n"
        System.out.println "The cdAvail1EndDateTimeM is: " + cdAvail1EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail2EndDateTimeM is: " + cdAvail2EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail3EndDateTime is: " + cdAvail3EndDateTimeM.toString(outputFormatter)

        cdAvail1EndDateTime  =  cdAvail1StartDateTime.plusDays(1)
        cdAvail2EndDateTime  =  cdAvail2StartDateTime.plusDays(1)
        cdAvail3EndDateTime  =  cdAvail3StartDateTime.plusDays(1)

        System.out.println "\n\nUsing proposed CD Avail Calculations"

        System.out.println "\n"
        System.out.println "The cdAvail1EndDateTime is: " + cdAvail1EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail2EndDateTime is: " + cdAvail2EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail3EndDateTime is: " + cdAvail3EndDateTime.toString(outputFormatter)

        Minutes cdAvail1Duration = Minutes.minutesBetween(cdAvail1StartDateTime, cdAvail1EndDateTime)
        Minutes cdAvail2Duration = Minutes.minutesBetween(cdAvail2StartDateTime, cdAvail2EndDateTime)
        Minutes cdAvail3Duration = Minutes.minutesBetween(cdAvail3StartDateTime, cdAvail3EndDateTime)


        System.out.println "\n"
        System.out.println "The cdAvail1Duration is: " + cdAvail1Duration.toString()
        System.out.println "The cdAvail2Duration is: " + cdAvail2Duration.toString()
        System.out.println "The cdAvail3Duration is: " + cdAvail3Duration.toString()

        cdAvail1EndDateTimeM  =  cdAvail1StartDateTimeM.plus(Minutes.minutes(1440))
        cdAvail2EndDateTimeM  =  cdAvail2StartDateTimeM.plus(Minutes.minutes(1380))
        cdAvail3EndDateTimeM  =  cdAvail3StartDateTimeM.plus(Minutes.minutes(1440))

        System.out.println "\n"
        System.out.println "The cdAvail1EndDateTimeM is: " + cdAvail1EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail2EndDateTimeM is: " + cdAvail2EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail3EndDateTime is: " + cdAvail3EndDateTimeM.toString(outputFormatter)

        DateTime cdAvail4StartDateTime = DateTime.parse("2014-11-01 00:00:00", inputFormatter)
        DateTime cdAvail5StartDateTime = DateTime.parse("2014-11-02 00:00:00", inputFormatter)
        DateTime cdAvail6StartDateTime = DateTime.parse("2014-11-03 00:00:00", inputFormatter)

        System.out.println "November 2014 DST Ends - Example - Proposed Calculations"
        System.out.println "\n"
        System.out.println "The cdAvail4StartDateTime is: " + cdAvail4StartDateTime.toString(outputFormatter)
        System.out.println "The cdAvail5StartDateTime is: " + cdAvail5StartDateTime.toString(outputFormatter)
        System.out.println "The cdAvail6StartDateTime is: " + cdAvail6StartDateTime.toString(outputFormatter)

        DateTime cdAvail4EndDateTime  =  cdAvail4StartDateTime.plusDays(1)
        DateTime cdAvail5EndDateTime  =  cdAvail5StartDateTime.plusDays(1)
        DateTime cdAvail6EndDateTime  =  cdAvail6StartDateTime.plusDays(1)

        System.out.println "\n"
        System.out.println "The cdAvail4EndDateTime is: " + cdAvail4EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail5EndDateTime is: " + cdAvail5EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail6EndDateTime is: " + cdAvail6EndDateTime.toString(outputFormatter)

        Minutes cdAvail4Duration = Minutes.minutesBetween(cdAvail4StartDateTime, cdAvail4EndDateTime)
        Minutes cdAvail5Duration = Minutes.minutesBetween(cdAvail5StartDateTime, cdAvail5EndDateTime)
        Minutes cdAvail6Duration = Minutes.minutesBetween(cdAvail6StartDateTime, cdAvail6EndDateTime)


        System.out.println "\n"
        System.out.println "The cdAvail4Duration is: " + cdAvail4Duration.toString()
        System.out.println "The cdAvail5Duration is: " + cdAvail5Duration.toString()
        System.out.println "The cdAvail6Duration is: " + cdAvail6Duration.toString()

        DateTime cdAvail4StartDateTimeM = cdAvail4StartDateTime.toDateTime(DateTimeZone.forID("UTC"))
        DateTime cdAvail5StartDateTimeM = cdAvail5StartDateTime.toDateTime(DateTimeZone.forID("UTC"))
        DateTime cdAvail6StartDateTimeM = cdAvail6StartDateTime.toDateTime(DateTimeZone.forID("UTC"))

        System.out.println "\n"
        System.out.println "The cdAvail4StartDateTimeM is: " + cdAvail4StartDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail5StartDateTimeM is: " + cdAvail5StartDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail6StartDateTimeM is: " + cdAvail6StartDateTimeM.toString(outputFormatter)

        DateTime cdAvail4EndDateTimeM  =  cdAvail4StartDateTimeM.plus(Minutes.minutes(1440))
        DateTime cdAvail5EndDateTimeM  =  cdAvail5StartDateTimeM.plus(Minutes.minutes(1500))
        DateTime cdAvail6EndDateTimeM  =  cdAvail6StartDateTimeM.plus(Minutes.minutes(1440))

        System.out.println "\n"
        System.out.println "The cdAvail4EndDateTimeM is: " + cdAvail4EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail5EndDateTimeM is: " + cdAvail5EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail6EndDateTimeM is: " + cdAvail6EndDateTimeM.toString(outputFormatter)

        Period  cdAvail1Period = new Period( cdAvail1StartDateTime, cdAvail1StartDateTime.plus(Minutes.minutes(1440)) )
        boolean equalsOneDay = cdAvail1Period.equals(Period.days(1))

        System.out.println "\n"
        System.out.println "The cdAvail1 day check is: " + equalsOneDay

        //cdAvail2StartDateTime = cdAvail2StartDateTime.minusHours(1)

        Period  cdAvail2Period = new Period( cdAvail2StartDateTime, cdAvail2StartDateTime.plus(Minutes.minutes(1380)) )
        equalsOneDay = cdAvail2Period.equals(Period.days(1))

        System.out.println "\n"
        System.out.println "The cdAvail2 day check is: " + equalsOneDay

        // 8 hour shifts around DST Start
        DateTime cdAvail7StartDateTime = DateTime.parse("2014-03-08 16:00:00", inputFormatter)
        DateTime cdAvail8StartDateTime = DateTime.parse("2014-03-09 00:00:00", inputFormatter)
        DateTime cdAvail9StartDateTime = DateTime.parse("2014-03-09 08:00:00", inputFormatter)

        Minutes eightHourDurationMinutes = Minutes.minutes(480)

        System.out.println "\n\n 8 hour shifts around DST Start"
        System.out.println "\n"
        System.out.println "The cdAvail7StartDateTime is: " + cdAvail7StartDateTime.toString(outputFormatter)
        System.out.println "The cdAvail8StartDateTime is: " + cdAvail8StartDateTime.toString(outputFormatter)
        System.out.println "The cdAvail9StartDateTime is: " + cdAvail9StartDateTime.toString(outputFormatter)

        DateTime cdAvai71EndDateTime  =  cdAvail7StartDateTime.plus(eightHourDurationMinutes)
        DateTime cdAvail8EndDateTime  =  cdAvail8StartDateTime.plus(eightHourDurationMinutes)
        DateTime cdAvail9EndDateTime  =  cdAvail9StartDateTime.plus(eightHourDurationMinutes)

        System.out.println "\n"
        System.out.println "The cdAvai71EndDateTime is: " + cdAvai71EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail8EndDateTime is: " + cdAvail8EndDateTime.toString(outputFormatter)
        System.out.println "The cdAvail9EndDateTime is: " + cdAvail9EndDateTime.toString(outputFormatter)

        DateTime cdAvail7StartDateTimeM = cdAvail7StartDateTime.toDateTime(DateTimeZone.forID("UTC"))
        DateTime cdAvail8StartDateTimeM = cdAvail8StartDateTime.toDateTime(DateTimeZone.forID("UTC"))
        DateTime cdAvail9StartDateTimeM = cdAvail9StartDateTime.toDateTime(DateTimeZone.forID("UTC"))

        System.out.println "\n"
        System.out.println "The cdAvail7StartDateTimeM is: " + cdAvail7StartDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail8StartDateTimeM is: " + cdAvail8StartDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail9StartDateTimeM is: " + cdAvail9StartDateTimeM.toString(outputFormatter)

        DateTime cdAvail7EndDateTimeM  =  cdAvail7StartDateTimeM.plus(eightHourDurationMinutes)
        DateTime cdAvail8EndDateTimeM  =  cdAvail8StartDateTimeM.plus(eightHourDurationMinutes)
        DateTime cdAvail9EndDateTimeM  =  cdAvail9StartDateTimeM.plus(eightHourDurationMinutes)

        System.out.println "\n"
        System.out.println "The cdAvail7EndDateTimeM is: " + cdAvail7EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail8EndDateTimeM is: " + cdAvail8EndDateTimeM.toString(outputFormatter)
        System.out.println "The cdAvail9EndDateTimeM is: " + cdAvail9EndDateTimeM.toString(outputFormatter)

    }
}
