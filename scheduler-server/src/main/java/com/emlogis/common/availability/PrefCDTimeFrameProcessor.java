package com.emlogis.common.availability;

import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.model.employee.AvailabilityTimeFrame;
import com.emlogis.model.employee.CDAvailabilityTimeFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public class PrefCDTimeFrameProcessor extends CDTimeFrameProcessor<AvailcalViewDto.PrefCDTimeFrame> {

    public PrefCDTimeFrameProcessor(DateTimeZone timeZone) {
        super(timeZone);
    }

    @Override
    public List<AvailcalViewDto.PrefCDTimeFrame> invert(List<AvailcalViewDto.PrefCDTimeFrame> timeFrames) {
        return timeFrames;
    }

    @Override
    public AvailcalViewDto.PrefCDTimeFrame compose(CDAvailabilityTimeFrame timeFrame) {
        SimpleTimeFrame simpleTimeFrameInTZ = toSimpleTimeFrame(timeFrame);
        DateTime dateTimeInTZ = new DateTime( timeFrame.getStartDateTime().toInstant(), getTimeZone());

        AvailcalViewDto.PrefCDTimeFrame prefCdTimeFrame = new AvailcalViewDto.PrefCDTimeFrame();

        if (isAllDayLong(timeFrame)){
            if (timeFrame.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.AvailPreference)){
                prefCdTimeFrame.setPrefType(AvailcalViewDto.PrefType.PREFER_DAY);
            } else 	if (timeFrame.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.UnAvailPreference)){
                prefCdTimeFrame.setPrefType(AvailcalViewDto.PrefType.AVOID_DAY);
            } else {
                // TODO - i18n ValidationException for invalid AvailabilityType for a Preference CD timeframe
                throw new ValidationException("Invalid AvailabiliyType for a Preference CD timeframe");
            }
        } else {  // NOT all day long, so PrefType.
            if (timeFrame.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.AvailPreference)){
                prefCdTimeFrame.setPrefType(AvailcalViewDto.PrefType.PREFER_TIMEFRAME);
            } else 	if (timeFrame.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.UnAvailPreference)){
                prefCdTimeFrame.setPrefType(AvailcalViewDto.PrefType.AVOID_TIMEFRAME);
            } else {
                // TODO - i18n ValidationException for invalid AvailabilityType for a Preference CD timeframe
                throw new ValidationException("Invalid AvailabiliyType for a Preference CD timeframe");
            }
        }

        prefCdTimeFrame.setDateTime(dateTimeInTZ.toInstant().getMillis());
        prefCdTimeFrame.setStartTime(simpleTimeFrameInTZ.getStartTime());
        prefCdTimeFrame.setEndTime(simpleTimeFrameInTZ.getEndTime());

        prefCdTimeFrame.setEmployeeId(timeFrame.getEmployeeId());

        return prefCdTimeFrame;
    }
}
