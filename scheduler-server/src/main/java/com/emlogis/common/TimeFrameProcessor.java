package com.emlogis.common;

import com.emlogis.common.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public abstract class TimeFrameProcessor
        <TimeFrame extends SimpleTimeFrame> {

    /**
     * Validates list of timeframes and returns them as a sorted list.
     * @param timeFrames
     * @return
     */
    public List<TimeFrame> sort( List<TimeFrame> timeFrames ) {

        final long millisInADay = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);

        // First let's get them into ascending order by start time...
        List<TimeFrame> sortedTimeFrames = new ArrayList<>(timeFrames);
        Collections.sort(sortedTimeFrames, SimpleTimeFrame.getNullableStartTimeComparator());

        TimeFrame firstTimeFrame = sortedTimeFrames.get(0);
        if(firstTimeFrame.getStartTime() == null || firstTimeFrame.getEndTime() == null) {
            if(sortedTimeFrames.size() == 1) {  //If the list contains just one "whole day" time frame.
                return sortedTimeFrames;
            } else {
                throw new ValidationException("Invalid time frames! A whole day time frame is not the single for the day.");
            }
        }

        // Second, let's validate timeframe (end times later than start times, none overlap, etc.) ...
        for (int i = 0;  i < sortedTimeFrames.size();  i++){
            TimeFrame thisTimeFrame = sortedTimeFrames.get(i);
            TimeFrame nextTimeFrame = (i != sortedTimeFrames.size() - 1) ? sortedTimeFrames.get(i+1) : null;

            if ( thisTimeFrame.getStartTime() < 0 ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
            if ( thisTimeFrame.getEndTime()   < 0 ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
            if ( thisTimeFrame.getStartTime() > millisInADay ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
            if ( thisTimeFrame.getEndTime()   > millisInADay ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
            if ( thisTimeFrame.getStartTime() >= thisTimeFrame.getEndTime() ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }

            if (nextTimeFrame != null){
                if ( nextTimeFrame.getStartTime() < 0 ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
                if ( nextTimeFrame.getEndTime()   < 0 ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
                if ( nextTimeFrame.getStartTime() > millisInADay ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
                if ( nextTimeFrame.getEndTime()   > millisInADay ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
                if ( nextTimeFrame.getStartTime() >= nextTimeFrame.getEndTime() ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
                if ( thisTimeFrame.getEndTime() >= nextTimeFrame.getStartTime() ){ throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
            }
        }
        return sortedTimeFrames;
    }


    /**
     * Returns sorted list of inverse timeframes for a day.
     * Important! Time frames should contain the same info except SimpleTimeFrame fields.
     * So for instance, if the input list represented 7am-1pm & 4pm-8pm
     * then the returned list would represent 12am-7am, 1pm-4pm, and 8pm-12am (day's end).
     *
     * @param timeFrames
     * @return
     */
    public List<TimeFrame> invert(List<TimeFrame> timeFrames) {
        if(timeFrames == null || timeFrames.size() == 0) {
            return new ArrayList<>();
        }

        // Let's make sure they are valid and sorted first...
        List<TimeFrame> sortedTimeFrames = sort(timeFrames);

        final long millisInADay = TimeUnit.DAYS.toMillis(1);

        // Let's get the inverse timeframes...
        ArrayList<TimeFrame> newTimeFrames = new ArrayList<>();
        for (int i = 0;  i < sortedTimeFrames.size();  i++){
            TimeFrame thisTimeFrame = sortedTimeFrames.get(i);

            //Add whole day time frame without processing, because it must be the single one
            if(thisTimeFrame.getStartTime() == null && thisTimeFrame.getEndTime() == null) {
                newTimeFrames.add(thisTimeFrame);
                return newTimeFrames;
            }

            //Last time frame. Our inverse list is complete, no need for further processing!
            if (thisTimeFrame.getEndTime() == millisInADay){
                break;
            }

            TimeFrame nextTimeFrame = (i != sortedTimeFrames.size() - 1) ? sortedTimeFrames.get(i+1) : null;

            TimeFrame newTimeFrame = null;

            // handle case of inverse timeframe starting the day...
            if (i == 0  &&  thisTimeFrame.getStartTime() != 0){
                try {
                    newTimeFrame = /*(TimeFrame) new SimpleTimeFrame()*/ SimpleTimeFrame.copy(thisTimeFrame);
                } catch (CloneNotSupportedException e) {
                    throw new ValidationException("Wrong time frame classes. Failed to copy objects!");
                }
                newTimeFrame.setStartTime(0l);
                newTimeFrame.setEndTime(thisTimeFrame.getStartTime());
                newTimeFrames.add(newTimeFrame);
            }

            try {
                newTimeFrame = /*(TimeFrame) new SimpleTimeFrame()*/ SimpleTimeFrame.copy(thisTimeFrame);
            } catch (CloneNotSupportedException e) {
                throw new ValidationException("Wrong time frame classes. Failed to copy objects!");
            }
            newTimeFrame.setStartTime( thisTimeFrame.getEndTime() );
            newTimeFrame.setEndTime( nextTimeFrame != null ? nextTimeFrame.getStartTime() : millisInADay);
            newTimeFrames.add(newTimeFrame);

            if (newTimeFrame.getEndTime() == millisInADay){
                break;  // Our inverse list is complete, no need for further processing!
            }
        }

        return newTimeFrames;
    }

}

