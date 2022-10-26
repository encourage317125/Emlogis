package com.emlogis.common.availability;

import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.common.TimeUtil;
import com.emlogis.model.employee.AvailabilityTimeFrame;
import com.emlogis.model.employee.CDAvailabilityTimeFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * Created by Andrii Mozharovskyi on 9/14/15.
 */
public class AvailCDTimeFrameProcessor extends CDTimeFrameProcessor<AvailcalViewDto.AvailCDTimeFrame> {


    public AvailCDTimeFrameProcessor(DateTimeZone timeZone) {
        super(timeZone);
    }

    @Override
    public List<AvailcalViewDto.AvailCDTimeFrame> invert(List<AvailcalViewDto.AvailCDTimeFrame> timeFrames) {
        return super.invert(timeFrames);
    }

    @Override
    public AvailcalViewDto.AvailCDTimeFrame compose(CDAvailabilityTimeFrame timeFrame) {
        SimpleTimeFrame simpleTimeFrameInTZ = toSimpleTimeFrame(timeFrame);
        DateTime dateTimeInTZ = new DateTime( timeFrame.getStartDateTime().toInstant(), getTimeZone());

        AvailcalViewDto.AvailCDTimeFrame availCdTimeFrame = new AvailcalViewDto.AvailCDTimeFrame();

        if(isAllDayLong(timeFrame)){
            availCdTimeFrame.setDateTime(dateTimeInTZ.toInstant().getMillis());
            availCdTimeFrame.setStartTime(simpleTimeFrameInTZ.getStartTime());
            availCdTimeFrame.setEndTime(simpleTimeFrameInTZ.getEndTime());

            if (timeFrame.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.UnAvail)){
                if (timeFrame.getIsPTO() == true){
                    availCdTimeFrame.setPTO(true);
                }
                if (timeFrame.getAbsenceType() != null){
                    availCdTimeFrame.setAbsenceTypeName(timeFrame.getAbsenceType().getName());
                }
                availCdTimeFrame.setAvailType(AvailcalViewDto.AvailType.DAY_OFF);
            } else {
                availCdTimeFrame.setAvailType(AvailcalViewDto.AvailType.AVAIL);
                return null;
                // TODO - Consider whether to include CD Avails for display.
                //        Technically unnecessary since employee is implicitly available anyway!
                //        We'll exclude it for now.
//							availCalDto.getAvailCDTimeFrames().add(availCdTimeFrame);
            }
        } else {
            // Must have one or more unavailability timeframes, so let's transform it to
            // an AvailSimpleTimeFrame for the list that will be transformed to the
            // inverse timeframes...

            availCdTimeFrame.setStartTime(simpleTimeFrameInTZ.getStartTime());
            availCdTimeFrame.setEndTime(simpleTimeFrameInTZ.getEndTime());

            availCdTimeFrame.setDateTime(dateTimeInTZ.toInstant().getMillis());
            availCdTimeFrame.setAvailType(AvailcalViewDto.AvailType.AVAIL);
        }

        availCdTimeFrame.setEmployeeId(timeFrame.getEmployeeId());

        return availCdTimeFrame;
    }
}
