package com.emlogis.common.availability;

import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.common.TimeUtil;
import com.emlogis.model.employee.CIAvailabilityTimeFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public class AvailCITimeFrameProcessor extends CITimeFrameProcessor<AvailcalViewDto.AvailCITimeFrame> {

    public AvailCITimeFrameProcessor(DateTimeZone timeZone) {
        super(timeZone);
    }

    @Override
    public List<AvailcalViewDto.AvailCITimeFrame> invert(List<AvailcalViewDto.AvailCITimeFrame> timeFrames) {
        return super.invert(timeFrames);
    }

    @Override
    public AvailcalViewDto.AvailCITimeFrame compose(CIAvailabilityTimeFrame timeFrame,
                                                    AvailcalViewDto.CITimeFrame.GroupKey key,
                                                    DateTime effectiveViewDateRangeStartInTZ,
                                                    DateTime effectiveViewDateRangeEndInTZ) {

        AvailcalViewDto.AvailCITimeFrame availCiTimeFrame = new AvailcalViewDto.AvailCITimeFrame();

        int dayOfWeekJodaValue = key.getDayOfTheWeek().getJodaValue();
        DateTime indexDateTime = effectiveViewDateRangeStartInTZ.withTimeAtStartOfDay();

        if (isAllDayLong(timeFrame)){   // Just this one all-day timeframe, so it will become an AvailType.DAY_OFF AvailCITimeFrame ...
            while (!indexDateTime.isAfter(effectiveViewDateRangeEndInTZ)){
                if (indexDateTime.getDayOfWeek() == dayOfWeekJodaValue  /*&&  cdAvailTimeFramesMap.get(indexDateTime) == null*/){
                    AvailcalViewDto.TimeFrameInstance timeFrameInstance = new AvailcalViewDto.TimeFrameInstance();
                    timeFrameInstance.setStartDateTime( indexDateTime.toInstant().getMillis() );
                    availCiTimeFrame.getTimeFrameInstances().add(timeFrameInstance);
                }
                indexDateTime = indexDateTime.plusDays(1);
            }

            // Populate rest of the AvailCITimeFrame attributes ...
            availCiTimeFrame.setAvailType(AvailcalViewDto.AvailType.DAY_OFF);
            availCiTimeFrame.setStartTime(null);
            availCiTimeFrame.setEndTime(null);
        } else {
            SimpleTimeFrame simpleTimeFrame = toSimpleTimeFrame(timeFrame);

            availCiTimeFrame.getTimeFrameInstances().addAll(populateTimeFrameInstances(dayOfWeekJodaValue, simpleTimeFrame,
                    effectiveViewDateRangeStartInTZ, effectiveViewDateRangeEndInTZ));

            // Populate rest of the AvailCITimeFrame attributes ...
            availCiTimeFrame.setAvailType(AvailcalViewDto.AvailType.AVAIL);
            availCiTimeFrame.setStartTime(simpleTimeFrame.getStartTime());
            availCiTimeFrame.setEndTime(simpleTimeFrame.getEndTime());
        }

        //Common properties
        availCiTimeFrame.setEmployeeId(key.getEmployeeId());
        availCiTimeFrame.setDayOfTheWeek(key.getDayOfTheWeek());
        if (key.getStartDateTime().equals(new DateTime(0L))) {
            availCiTimeFrame.setEffectiveDateRangeStart(null);
        } else {
            availCiTimeFrame.setEffectiveDateRangeStart(key.getStartDateTime().getMillis());
        }
        if (key.getEndDateTime().equals( new DateTime(Long.MAX_VALUE))) {
            availCiTimeFrame.setEffectiveDateRangeEnd(null);
        } else {
            availCiTimeFrame.setEffectiveDateRangeEnd(key.getEndDateTime().getMillis());
        }

        return availCiTimeFrame;
    }
}
