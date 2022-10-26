package com.emlogis.common.availability;

import com.emlogis.common.SimpleDateTimeFrame;
import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.common.TimeUtil;
import com.emlogis.model.employee.CIAvailabilityTimeFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public abstract class CITimeFrameProcessor<CITimeFrame extends AvailcalViewDto.CITimeFrame>
        extends AvailabilityTimeFrameProcessor<CITimeFrame, CIAvailabilityTimeFrame> {

    public CITimeFrameProcessor(DateTimeZone timeZone) {
        super(timeZone);
    }

    @Override
    public boolean isAllDayLong(CIAvailabilityTimeFrame timeFrame){
        long durationInMillis = TimeUnit.MINUTES.toMillis(timeFrame.getDurationInMinutes().getMinutes());
        return durationInMillis == TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public SimpleTimeFrame toSimpleTimeFrame(CIAvailabilityTimeFrame timeFrame) {
        long startTime = timeFrame.getStartTime().getMillisOfDay();
        long endTime = startTime + TimeUnit.MINUTES.toMillis(timeFrame.getDurationInMinutes().getMinutes());
        return new SimpleTimeFrame(startTime, endTime);
    }

    public abstract CITimeFrame compose(CIAvailabilityTimeFrame timeFrame, CITimeFrame.GroupKey key,
                            DateTime effectiveViewDateRangeStartInTZ,
                            DateTime effectiveViewDateRangeEndInTZ);

    public Map<CITimeFrame.GroupKey, List<CIAvailabilityTimeFrame>> group(Collection<CIAvailabilityTimeFrame> ciTimeFrames) {

        Map<CITimeFrame.GroupKey, List<CIAvailabilityTimeFrame>> ciUnavailTimeFramesMap = new HashMap<>();
        for (CIAvailabilityTimeFrame ciUnavailableTimeFrame : ciTimeFrames){
            CITimeFrame.GroupKey key = new CITimeFrame.GroupKey();

            if (ciUnavailableTimeFrame.getStartDateTime() != null) {
                key.setStartDateTime(ciUnavailableTimeFrame.getStartDateTime());
            } else {
                key.setStartDateTime(new DateTime(0L));
            }

            if (ciUnavailableTimeFrame.getEndDateTime() != null) {
                key.setEndDateTime(ciUnavailableTimeFrame.getEndDateTime());
            } else {
                key.setEndDateTime( new DateTime(Long.MAX_VALUE) );
            }

            key.setDayOfTheWeek(ciUnavailableTimeFrame.getDayOfTheWeek());
            key.setAvailabilityType(ciUnavailableTimeFrame.getAvailabilityType());
            key.setEmployeeId(ciUnavailableTimeFrame.getEmployeeId());

            if (!ciUnavailTimeFramesMap.containsKey(key)){
                ciUnavailTimeFramesMap.put(key, new ArrayList<CIAvailabilityTimeFrame>());
            }
            ciUnavailTimeFramesMap.get(key).add(ciUnavailableTimeFrame);
        }

        return ciUnavailTimeFramesMap;
    }

    public List<CITimeFrame> buildFromGroups(Map<CITimeFrame.GroupKey, List<CIAvailabilityTimeFrame>> ciAvailTimeFrameGroups,
                                             DateTime viewStartDateTimeInTZ,
                                             DateTime viewEndDateTimeInTZ) {
        List<CITimeFrame> ciAvailabilityTimeFrames = new ArrayList<>();

        for (CITimeFrame.GroupKey key : ciAvailTimeFrameGroups.keySet()) {
            List<CIAvailabilityTimeFrame> groupTimeFrames = ciAvailTimeFrameGroups.get(key);

            SimpleDateTimeFrame effectiveViewDateTimeFrame = getEffectiveViewDateTimeFrameInTZ(
                    new SimpleDateTimeFrame(key.getStartDateTime(), key.getEndDateTime()),
                    new SimpleDateTimeFrame(viewStartDateTimeInTZ, viewEndDateTimeInTZ)
            );

            List<CITimeFrame> preparedCITimeFrames = new ArrayList<>();
            for (CIAvailabilityTimeFrame groupTimeFrame : groupTimeFrames) {
                CITimeFrame preparedTimeFrame = compose(groupTimeFrame, key,
                        effectiveViewDateTimeFrame.getStartDateTime(), effectiveViewDateTimeFrame.getEndDateTime());
                if (preparedTimeFrame != null) {
                    preparedCITimeFrames.add(preparedTimeFrame);
                }
            }

            ciAvailabilityTimeFrames.addAll(invert(preparedCITimeFrames));
        }

        return ciAvailabilityTimeFrames;
    }

    public List<AvailcalViewDto.TimeFrameInstance> populateTimeFrameInstances(int dayOfWeekJodaValue,
                                                                              SimpleTimeFrame simpleTimeFrame,
                                                                              DateTime effectiveViewDateRangeStartInTZ,
                                                                              DateTime effectiveViewDateRangeEndInTZ) {
        List<AvailcalViewDto.TimeFrameInstance> instances = new ArrayList<>();

        DateTime indexDateTime = effectiveViewDateRangeStartInTZ.withTimeAtStartOfDay();

        Long startDateTime = simpleTimeFrame.getStartTime() != null ? indexDateTime.toInstant().getMillis() : null;
        Long endDateTime = simpleTimeFrame.getEndTime() != null ?
                indexDateTime.toInstant().getMillis() + simpleTimeFrame.getEndTime() :
                null;

        while (!indexDateTime.isAfter(effectiveViewDateRangeEndInTZ)){
            if (indexDateTime.getDayOfWeek() == dayOfWeekJodaValue  /*&&  cdAvailTimeFramesMap.get(indexDateTime) == null*/){
                AvailcalViewDto.TimeFrameInstance timeFrameInstance = new AvailcalViewDto.TimeFrameInstance();
                timeFrameInstance.setStartDateTime( startDateTime );
                timeFrameInstance.setEndDateTime( endDateTime );
                instances.add(timeFrameInstance);
            }
            indexDateTime = indexDateTime.plusDays(1);
        }

        return instances;
    }

    public SimpleDateTimeFrame getEffectiveViewDateTimeFrameInTZ(SimpleDateTimeFrame timeFrame,
                                                                 SimpleDateTimeFrame viewTimeFrame) {

        SimpleDateTimeFrame timeFrameInTZ = new SimpleDateTimeFrame(new DateTime(timeFrame.getStartDateTime().toInstant(),
                getTimeZone()), new DateTime(timeFrame.getEndDateTime().toInstant(), getTimeZone()));

        //TODO: possibly change time zone also for "viewTimeFrame"

        return TimeUtil.getIntersection(timeFrameInTZ, viewTimeFrame);
    }

}
