package com.emlogis.common.availability;

import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.model.employee.AvailabilityTimeFrame;
import com.emlogis.model.employee.CIAvailabilityTimeFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public class PrefCITimeFrameProcessor extends CITimeFrameProcessor<AvailcalViewDto.PrefCITimeFrame> {

    public PrefCITimeFrameProcessor(DateTimeZone timeZone) {
        super(timeZone);
    }

    @Override
    public List<AvailcalViewDto.PrefCITimeFrame> invert(List<AvailcalViewDto.PrefCITimeFrame> timeFrames) {
        return timeFrames;
    }

    @Override
    public AvailcalViewDto.PrefCITimeFrame compose(CIAvailabilityTimeFrame timeFrame, AvailcalViewDto.CITimeFrame.GroupKey key,
                                                   DateTime effectiveViewDateRangeStartInTZ, DateTime effectiveViewDateRangeEndInTZ) {

        AvailcalViewDto.PrefCITimeFrame preferenceCiTimeFrame = new AvailcalViewDto.PrefCITimeFrame();

        int dayOfWeekJodaValue = key.getDayOfTheWeek().getJodaValue();

        if (isAllDayLong(timeFrame)){
            // Just this one all-day timeframe, so it will become a PrefCITimeFrame ...

            preferenceCiTimeFrame.getTimeFrameInstances().addAll(
                    populateTimeFrameInstances(dayOfWeekJodaValue,  new SimpleTimeFrame(),
                            effectiveViewDateRangeStartInTZ, effectiveViewDateRangeEndInTZ)
            );

            // Populate rest of the PrefCITimeFrame attributes ...
            preferenceCiTimeFrame.setStartTime(null);
            preferenceCiTimeFrame.setEndTime(null);
            if (key.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.AvailPreference)) {
                preferenceCiTimeFrame.setPrefType(AvailcalViewDto.PrefType.PREFER_DAY);
            } else {
                preferenceCiTimeFrame.setPrefType(AvailcalViewDto.PrefType.AVOID_DAY);
            }
        } else {
            SimpleTimeFrame simpleTimeFrame = toSimpleTimeFrame(timeFrame);

            preferenceCiTimeFrame.getTimeFrameInstances().addAll(populateTimeFrameInstances(dayOfWeekJodaValue, simpleTimeFrame,
                    effectiveViewDateRangeStartInTZ, effectiveViewDateRangeEndInTZ));

            // Populate rest of the PrefCITimeFrame attributes ...
            preferenceCiTimeFrame.setStartTime(simpleTimeFrame.getStartTime());
            preferenceCiTimeFrame.setEndTime(simpleTimeFrame.getEndTime());
            if (key.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.AvailPreference)) {
                preferenceCiTimeFrame.setPrefType(AvailcalViewDto.PrefType.PREFER_TIMEFRAME);
            } else {
                preferenceCiTimeFrame.setPrefType(AvailcalViewDto.PrefType.AVOID_TIMEFRAME);
            }
        }

        preferenceCiTimeFrame.setEmployeeId(key.getEmployeeId());
        preferenceCiTimeFrame.setDayOfTheWeek(key.getDayOfTheWeek());
        if (key.getStartDateTime().equals(new DateTime(0L))) {
            preferenceCiTimeFrame.setEffectiveDateRangeStart(null);
        } else {
            preferenceCiTimeFrame.setEffectiveDateRangeStart(key.getStartDateTime().getMillis());
        }
        if (key.getEndDateTime().equals( new DateTime(Long.MAX_VALUE))) {
            preferenceCiTimeFrame.setEffectiveDateRangeEnd(null);
        } else {
            preferenceCiTimeFrame.setEffectiveDateRangeEnd(key.getEndDateTime().getMillis());
        }

        return preferenceCiTimeFrame;
    }
}
