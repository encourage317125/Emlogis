package com.emlogis.common;

import com.emlogis.model.employee.CDAvailabilityTimeFrame;
import com.emlogis.model.employee.CIAvailabilityTimeFrame;
import org.joda.time.*;

import java.util.*;

/** 
 * Utilities for Time Calculations
 * @author rjackson
 *
 */

public class TimeUtil {
	
	// Check to see if a start time and duration spans more than one day
	public static boolean spansMultipleDays(DateTime startDateTime, LocalTime startTime, Minutes minutes) {
		boolean retVal = false;		
		
		if (minutes.getMinutes() > DateTimeConstants.MINUTES_PER_DAY) {
			retVal = true;
		} else {
			DateTime startDateAndTime = new DateTime(startDateTime.getYear(), startDateTime.getMonthOfYear(),
                    startDateTime.getDayOfMonth(), startTime.getHourOfDay(), startTime.getMinuteOfHour(),
                    startTime.getSecondOfMinute());
			
			DateTime endDateAndTIme = startDateAndTime.plus(minutes);
			
			DateTime beginningOfNextDay = getStartOfDay(startDateAndTime).plusMinutes(DateTimeConstants.MINUTES_PER_DAY);
			
			if (endDateAndTIme.isAfter(beginningOfNextDay)) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public static DateTime getStartOfDay(DateTime currentDateTime) {
		DateTime retVal = null;
		
		if (currentDateTime != null) {
            retVal = new DateTime(currentDateTime.getYear(), currentDateTime.getMonthOfYear(),
                    currentDateTime.getDayOfMonth(), 0, 0, 0);
		}
		
		return retVal;
	}

	public static DateTime getNextDayStart(DateTime currentDateTime) {
		DateTime retVal = null;
		
		if (currentDateTime != null) {
            retVal = new DateTime(currentDateTime.getYear(), currentDateTime.getMonthOfYear(),
                    currentDateTime.getDayOfMonth(), 0, 0, 0).plus(Days.ONE);
		}
		
		return retVal;
	}
	
	public static boolean timeOverlaps(DateTime firstStartDateTime, Minutes firstDuration, DateTime secondStartDateTime,
                                       Minutes secondDuration) {
		boolean retVal = false;
		
		DateTime firstEndDateTime = firstStartDateTime.plus(firstDuration);
		
		DateTime secondEndDateTime = secondStartDateTime.plus(secondDuration);
		
		if (firstStartDateTime.isBefore(secondEndDateTime) && firstEndDateTime.isAfter(secondStartDateTime)) {
			retVal = true;
		}
		
		return retVal;
	}
	
	public static boolean timeOverlaps(LocalTime firstStartTime, Minutes firstDuration, LocalTime secondStartTime,
                                       Minutes secondDuration) {
		boolean retVal = false;
		
		LocalTime firstEndTime = firstStartTime.plus(firstDuration);
		
		LocalTime secondEndTime = secondStartTime.plus(secondDuration);
		
		if (firstStartTime.isBefore(secondEndTime) && firstEndTime.isAfter(secondStartTime)) {
			retVal = true;
		}
		
		return retVal;
	}
	
	public static boolean checkCDCollectionForTimeOverLap(DateTime startDateTime, Minutes firstDuration,
                                                          Collection<CDAvailabilityTimeFrame> timeFrames) {
		boolean retVal = false;
		
		for(CDAvailabilityTimeFrame timeFrame: timeFrames) {
			if (timeOverlaps(startDateTime, firstDuration, timeFrame.getStartDateTime(), timeFrame.getDurationInMinutes())) {
				retVal = true;
				break;
			}
		}
				
		return retVal;
	}
	
	public static boolean checkCICollectionForTimeOverLap(LocalTime firstStartTime, Minutes firstDuration,
                                                          Collection<CIAvailabilityTimeFrame> timeFrames) {
		boolean retVal = false;
		
		for (CIAvailabilityTimeFrame timeFrame: timeFrames) {
			if (timeOverlaps(firstStartTime, firstDuration, timeFrame.getStartTime(), timeFrame.getDurationInMinutes())) {
				retVal = true;
				break;
			}
		}
				
		return retVal;
	}

    public static SimpleDateTimeFrame getIntersection(SimpleDateTimeFrame timeFrame1,
                                                      SimpleDateTimeFrame timeFrame2) {
        DateTime start;
        if (timeFrame1.getStartDateTime() != null) {
            start = timeFrame1.getStartDateTime();
        } else {
            start = timeFrame2.getStartDateTime();
        }

        DateTime end;
        if (timeFrame1.getEndDateTime() != null) {
            end = timeFrame1.getEndDateTime();
        } else {
            end = timeFrame2.getEndDateTime();
        }

        DateTime crossingPartStart;
        if (timeFrame2.getStartDateTime().isBefore(start)) {
            crossingPartStart = start;
        } else {
            crossingPartStart = timeFrame2.getStartDateTime();
        }

        DateTime crossingPartEnd;
        if (timeFrame2.getEndDateTime().isBefore(end)) {
            crossingPartEnd = timeFrame2.getEndDateTime();
        } else {
            crossingPartEnd = end;
        }

        return new SimpleDateTimeFrame(crossingPartStart, crossingPartEnd);
    }

    public static DateTime toServerDateTime(Long time) {
        return time != null ? new DateTime(time) : null;
    }
		
    public static long calculateTimeDelta(long firstDateTime, long secondDateTime) {
        return new LocalTime(firstDateTime).getMillisOfDay() - new LocalTime(secondDateTime).getMillisOfDay();
    }

    public static boolean sameTime(long firstDateTime, long secondDateTime) {
        return firstDateTime == secondDateTime || new LocalTime(firstDateTime).equals(new LocalTime(secondDateTime));
    }

    public static long datePlusDays(long date, int days) {
        return date + days * 1000L * 60 * 60 * 24;
    }

    public static long truncateDate(long date) {
        return new DateTime(date).withMillisOfDay(0).getMillis();
    }

    /**
     *
     * @param dayOfWeek - MONDAY = 1,..., SUNDAY = 7
     * @param fromDate
     * @param dateTimeZone
     * @return date of dayOfWeek
     */
    public static long getFirstDayOfWeekBefore(int dayOfWeek, long fromDate, DateTimeZone dateTimeZone) {
        LocalDate resultLocalDate = new LocalDate(fromDate, dateTimeZone);
        int startDayOfWeek = resultLocalDate.getDayOfWeek();
        int distance = 0;
        int resultDayOfWeek = startDayOfWeek;
        do {
            resultDayOfWeek = resultDayOfWeek == 1 ? 7 : resultDayOfWeek - 1;
            distance++;
        } while (resultDayOfWeek != dayOfWeek);
        return resultLocalDate.minusDays(distance).toDate().getTime();
    }

    /**
     *
     * @param dayOfWeek - MONDAY = 1,..., SUNDAY = 7
     * @param fromDate
     * @param dateTimeZone
     * @return date of dayOfWeek
     */
    public static long getFirstDayOfWeekAfter(int dayOfWeek, long fromDate, DateTimeZone dateTimeZone) {
        LocalDate resultLocalDate = new LocalDate(fromDate, dateTimeZone);
        int startDayOfWeek = resultLocalDate.getDayOfWeek();
        int distance = 0;
        int resultDayOfWeek = startDayOfWeek;
        do {
            resultDayOfWeek = resultDayOfWeek == 7 ? 1 : resultDayOfWeek + 1;
            distance++;
        } while (resultDayOfWeek != dayOfWeek);
        return resultLocalDate.plusDays(distance).toDate().getTime();
    }

}
