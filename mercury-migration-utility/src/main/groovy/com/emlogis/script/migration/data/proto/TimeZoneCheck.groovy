package com.emlogis.script.migration.data.proto

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 12/22/14
 * Time: 8:04 AM
 * To change this template use File | Settings | File Templates.
 */
class TimeZoneCheck {
    public static void main(String[] args) {
        DateTimeZone.setDefault(DateTimeZone.forID("America/Chicago"));
        DateTimeZone tz = DateTimeZone.getDefault();

        final DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss")
        final DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss:ssZ")

        DateTime aspenDateTimeDST = DateTime.parse("2014-07-04 00:00:00", inputFormatter)

        Long localMillis = tz.convertLocalToUTC(aspenDateTimeDST.getMillis(), true )

        DateTime MercuryDateTimeDST = new DateTime(localMillis)

        System.out.println "The DST AspenDateTime is: " + aspenDateTimeDST.toString(outputFormatter)
        System.out.println "The DST MercuryDateTime equivalent is: " + MercuryDateTimeDST.toString(outputFormatter)

        DateTime aspenDateTime = DateTime.parse("2014-01-04 00:00:00", inputFormatter)

        localMillis = tz.convertLocalToUTC(aspenDateTime.getMillis(), true )

        DateTime MercuryDateTime = new DateTime(localMillis)

        System.out.println "The AspenDateTime is: " + aspenDateTime.toString(outputFormatter)
        System.out.println "The MercuryDateTime equivalent is: " + MercuryDateTime.toString(outputFormatter)

        LocalTime aspenStartTime = LocalTime.parse("20:00")

        LocalTime aspenEndTime = LocalTime.parse("23:00")

        Minutes durationInMinutes = Minutes.minutesBetween(aspenStartTime, aspenEndTime)

        DateTime aspenStartDateTime = new LocalDate(aspenDateTimeDST).toDateTime(aspenStartTime)

        DateTime mercuryStartDateTime = aspenStartDateTime.toDateTime(DateTimeZone.forID("UTC"))

        LocalDate startDate = mercuryStartDateTime.toLocalDate()
        LocalTime startTime = mercuryStartDateTime.toLocalTime()

        // Converting Back to Central Time
        DateTimeZone.setDefault(DateTimeZone.forID("UTC"));
        DateTime mercuryStartDateTime2 =  new LocalDate(startDate).toDateTime(startTime)
        DateTime aspenStartDateTime2 = aspenStartDateTime.toDateTime(DateTimeZone.forID("America/Chicago"))
        System.out.println("")
    }
}
