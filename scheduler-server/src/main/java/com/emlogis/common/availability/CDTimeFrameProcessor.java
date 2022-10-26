package com.emlogis.common.availability;

import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.model.employee.CDAvailabilityTimeFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public abstract class CDTimeFrameProcessor<CDTimeFrame extends AvailcalViewDto.CDTimeFrame>
        extends AvailabilityTimeFrameProcessor<CDTimeFrame, CDAvailabilityTimeFrame> {

    public CDTimeFrameProcessor(DateTimeZone timeZone) {
        super(timeZone);
    }

    @Override
    public boolean isAllDayLong(CDAvailabilityTimeFrame timeFrame){
        long durationInMillis = TimeUnit.MINUTES.toMillis(timeFrame.getDurationInMinutes().getMinutes());
        return durationInMillis == TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public SimpleTimeFrame toSimpleTimeFrame(CDAvailabilityTimeFrame timeFrame) {
        if(isAllDayLong(timeFrame)) {
            return new SimpleTimeFrame();
        }
        DateTime startDateTimeInTZ = new DateTime(new DateTime( timeFrame.getStartDateTime().toInstant(), getTimeZone()));
        long startTime = startDateTimeInTZ.toLocalTime().getMillisOfDay();
        long endTime = startTime + TimeUnit.MINUTES.toMillis(timeFrame.getDurationInMinutes().getMinutes());
        return new SimpleTimeFrame(startTime, endTime);
    }

    public abstract CDTimeFrame compose(CDAvailabilityTimeFrame timeFrame);

    public Map<CDTimeFrame.GroupKey, List<CDAvailabilityTimeFrame>> group(Collection<CDAvailabilityTimeFrame> cdTimeFrames) {

        Map<CDTimeFrame.GroupKey, List<CDAvailabilityTimeFrame>> cdTimeFramesMap = new HashMap<>();
        for (CDAvailabilityTimeFrame cdTimeFrame : cdTimeFrames){
            DateTime startDateTimeInTZ = new DateTime( cdTimeFrame.getStartDateTime().toInstant(), getTimeZone());
            DateTime dayDateTimeInTZ = new DateTime(startDateTimeInTZ.withTimeAtStartOfDay());
            CDTimeFrame.GroupKey key = new CDTimeFrame.GroupKey();
            key.setEmployeeId(cdTimeFrame.getEmployeeId());
            key.setAvailabilityType(cdTimeFrame.getAvailabilityType());
            key.setDateTime(dayDateTimeInTZ);

            if (!cdTimeFramesMap.containsKey(key)){
                cdTimeFramesMap.put(key, new ArrayList<CDAvailabilityTimeFrame>());
            }
            cdTimeFramesMap.get(key).add(cdTimeFrame);
        }

        return cdTimeFramesMap;
    }

    public List<CDTimeFrame> buildFromGroups(Map<CDTimeFrame.GroupKey, List<CDAvailabilityTimeFrame>> cdAvailTimeFrameGroups) {
        List<CDTimeFrame> cdAvailabilityTimeFrames = new ArrayList<>();

        for (CDTimeFrame.GroupKey key : cdAvailTimeFrameGroups.keySet()){
            List<CDAvailabilityTimeFrame> groupTimeFrames = cdAvailTimeFrameGroups.get(key);
            List<CDTimeFrame> preparedTimeFramesForDate = new ArrayList<>();

            // Transform our Unavail CDAvailabilityTimeFrames into AvailSimpleTimeFrames..
            for (CDAvailabilityTimeFrame groupTimeFrame : groupTimeFrames){
                CDTimeFrame preparedTimeFrame = compose(groupTimeFrame);
                if(preparedTimeFrame != null) {
                    preparedTimeFramesForDate.add(preparedTimeFrame);
                }
            }

            cdAvailabilityTimeFrames.addAll(invert(preparedTimeFramesForDate));
        }

        return cdAvailabilityTimeFrames;
    }
}
