package com.emlogis.script.migration.data.proto

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * Created by rjackson on 1/2/2015.
 */
class TimeDSTCheck {
    public static boolean isInOverlap(LocalDateTime ldt, DateTimeZone dtz) {
        DateTime dt1 = ldt.toDateTime(dtz).withEarlierOffsetAtOverlap();
        DateTime dt2 = dt1.withLaterOffsetAtOverlap();
        return dt1.getMillis() != dt2.getMillis();
    }

    public static boolean timeOverlaps(DateTime firstStartDateTime, Minutes firstDuration, DateTime secondStartDateTime, Minutes secondDuration) {
        boolean retVal = false;

        DateTime firstEndDateTime = firstStartDateTime.plus(firstDuration);

        DateTime secondEndDateTime = secondStartDateTime.plus(secondDuration);

        if( firstStartDateTime.isBefore(secondEndDateTime) && firstEndDateTime.isAfter(secondStartDateTime)) {
            retVal = true;
        }

        return retVal;
    }

    static int getHickoryDayWeekValue(int jodaDayOfWeek) {

        int retVal = 0;
        // Sunday, Monday, Tues, Wed, Thurs, Friday, Sat
        def dayValuesArray = [7:0,1:1, 2:2, 3:3, 4:4,5:5, 6:6 ]

        return dayValuesArray.get(jodaDayOfWeek)
    }


    public static void main(String[] args) {
        LocalTime startTime = LocalTime.MIDNIGHT
        LocalTime endTime = startTime.plusMinutes(120)
        LocalTime endOfDay = LocalTime.MIDNIGHT.minusMinutes(1)

        int intMinutes = Math.abs( Minutes.minutesBetween(endTime, LocalTime.MIDNIGHT).getMinutes() )

        intMinutes = Math.abs( Minutes.minutesBetween(endTime, endOfDay).getMinutes() ) + 1

        DateTimeZone.setDefault(DateTimeZone.forID("America/Chicago"));
        DateTimeZone tz = DateTimeZone.getDefault();

        final DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss")
        final DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss:ssZ")

        System.out.println "Start time just before DST "
        // Starts just before time change
        DateTime startDateTime1 = DateTime.parse("2014-03-09 00:00:00", inputFormatter)

        int dayOfWeek = startDateTime1.getDayOfWeek()

        int legacyValue = getHickoryDayWeekValue(dayOfWeek)

        Minutes minutePeriod1 = Minutes.minutes(1440)

        DateTime endDateTime1 = startDateTime1.plus(minutePeriod1)

        // This calc ignores the time change or at least goes to the next day
        DateTime endDateTime2 = startDateTime1.plusDays(1)

        System.out.println "The startDateTime1 is: " + startDateTime1.toString(outputFormatter)
        System.out.println "The period is: " + minutePeriod1.toString()

        System.out.println "The endDateTime1 is: " + endDateTime1.toString(outputFormatter)
        System.out.println "The endDateTime2 is: " + endDateTime2.toString(outputFormatter)

        Minutes minutesBetween = Minutes.minutesBetween(startDateTime1, endDateTime1)
        Minutes minutesBetween2 = Minutes.minutesBetween(startDateTime1, endDateTime2)

        System.out.println "The minutes calculated are: " + minutesBetween.toString()
        System.out.println "The minutes2 calculated are: " + minutesBetween2.toString()

        Duration calcDuration = new Duration(startDateTime1, endDateTime1)
        System.out.println "The calcDuration is : " + calcDuration.toString()

        Interval calcInterval = new Interval (startDateTime1,endDateTime1)
        System.out.println "The calcInterval is : " + calcInterval.toString()
        System.out.println "The calcInterval duration is : " + calcInterval.toDuration().toString()

        // See if a time change has happened
        boolean timeChanged = false

        if(startDateTime1.getZone().nextTransition(startDateTime1.getMillis()) < endDateTime1.getMillis()) timeChanged=true

        System.out.println "Time Changed = " + timeChanged.toString()

        System.out.println "\n"

        System.out.println "No DST "
        // No Time Change
        DateTime startDateTime3 = DateTime.parse("2014-01-09 00:00:00", inputFormatter)
        DateTime endDateTime3 = startDateTime3.plus(minutePeriod1)

        System.out.println "The startDateTime3 is: " + startDateTime3.toString(outputFormatter)
        System.out.println "The endDateTime3 is: " + endDateTime3.toString(outputFormatter)

        timeChanged = false

        if(startDateTime3.getZone().nextTransition(startDateTime3.getMillis()) < endDateTime3.getMillis()) timeChanged=true

        System.out.println "Time Changed = " + timeChanged.toString()

        System.out.println "\n"
        System.out.println "Start Time Just before DST Changes Back"
        // No Time Change
        DateTime startDateTime4 = DateTime.parse("2014-11-02 00:00:00", inputFormatter)
        DateTime endDateTime4 = startDateTime4.plus(minutePeriod1)

        System.out.println "The startDateTime4 is: " + startDateTime4.toString(outputFormatter)
        System.out.println "The endDateTime4 is: " + endDateTime4.toString(outputFormatter)

        timeChanged = false

        if(startDateTime4.getZone().nextTransition(startDateTime4.getMillis()) < endDateTime4.getMillis()) timeChanged=true

        System.out.println "Time Changed = " + timeChanged.toString()

    }
}
