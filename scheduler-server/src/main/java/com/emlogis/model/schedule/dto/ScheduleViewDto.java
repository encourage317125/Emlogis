package com.emlogis.model.schedule.dto;

import java.util.Collection;
import java.util.Map;

public class ScheduleViewDto extends ScheduleViewBaseDto {

    private Collection<Map<String, Object>> weeks;

    public Collection<Map<String, Object>> getWeeks() {
        return weeks;
    }

    public void setWeeks(Collection<Map<String, Object>> weeks) {
        this.weeks = weeks;
    }
}
