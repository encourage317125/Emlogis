package com.emlogis.common.availability;

import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.common.TimeFrameProcessor;
import com.emlogis.model.employee.AvailabilityTimeFrame;
import org.joda.time.DateTimeZone;

/**
 * Created by Andrii Mozharovskyi on 9/15/15.
 */
public abstract class AvailabilityTimeFrameProcessor
        <TimeFrame extends AvailcalViewDto.TimeFrame, BaseTimeFrame extends AvailabilityTimeFrame>
        extends TimeFrameProcessor<TimeFrame> {

    private DateTimeZone timeZone;

    public AvailabilityTimeFrameProcessor(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public abstract boolean isAllDayLong(BaseTimeFrame baseTimeFrame);
    public abstract SimpleTimeFrame toSimpleTimeFrame(BaseTimeFrame baseTimeFrame);

    public static <T extends AvailcalViewDto.TimeFrame> void dos() {

    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
