package com.emlogis.model.schedule.dto;

import java.util.List;

public class ScheduleDayViewDto extends ScheduleViewBaseDto {

    private List<Object[]> shifts;
    private List<Integer> resourceAllocation;
    private List<PostedOpenShiftInfo> postedOpenShifts;

    public static class PostedOpenShiftInfo {
        private String shiftId;
        private long posted;
        private boolean requested;

        public String getShiftId() {
            return shiftId;
        }

        public void setShiftId(String shiftId) {
            this.shiftId = shiftId;
        }

        public long getPosted() {
            return posted;
        }

        public void setPosted(long posted) {
            this.posted = posted;
        }

        public boolean isRequested() {
            return requested;
        }

        public void setRequested(boolean requested) {
            this.requested = requested;
        }
    }

    public List<Object[]> getShifts() {
        return shifts;
    }

    public void setShifts(List<Object[]> shifts) {
        this.shifts = shifts;
    }

    public List<Integer> getResourceAllocation() {
        return resourceAllocation;
    }

    public void setResourceAllocation(List<Integer> resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }

    public List<PostedOpenShiftInfo> getPostedOpenShifts() {
        return postedOpenShifts;
    }

    public void setPostedOpenShifts(List<PostedOpenShiftInfo> postedOpenShifts) {
        this.postedOpenShifts = postedOpenShifts;
    }
}
