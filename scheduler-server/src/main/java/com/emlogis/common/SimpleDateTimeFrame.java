package com.emlogis.common;

import org.joda.time.DateTime;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public class SimpleDateTimeFrame {

    private DateTime startDateTime;
    private DateTime endDateTime;

    public SimpleDateTimeFrame() {
    }

    public SimpleDateTimeFrame(DateTime startDateTime, DateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(DateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
}
