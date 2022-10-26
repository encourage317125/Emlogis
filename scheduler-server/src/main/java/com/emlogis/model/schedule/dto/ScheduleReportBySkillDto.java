package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.Dto;

public class ScheduleReportBySkillDto extends Dto {

    private SummaryItem shifts = new SummaryItem();
    private SummaryItem hours = new SummaryItem();
    private SummaryItem assignedShifts = new SummaryItem();
    private SummaryItem assignedHours = new SummaryItem();
    int resources;
    double[] resourcesHours;
    int resourcesAssignments;

    public SummaryItem getShifts() {
        return shifts;
    }

    public void setShifts(SummaryItem shifts) {
        this.shifts = shifts;
    }

    public SummaryItem getHours() {
        return hours;
    }

    public void setHours(SummaryItem hours) {
        this.hours = hours;
    }

    public SummaryItem getAssignedShifts() {
        return assignedShifts;
    }

    public void setAssignedShifts(SummaryItem assignedShifts) {
        this.assignedShifts = assignedShifts;
    }

    public SummaryItem getAssignedHours() {
        return assignedHours;
    }

    public void setAssignedHours(SummaryItem assignedHours) {
        this.assignedHours = assignedHours;
    }

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    public double[] getResourcesHours() {
        return resourcesHours;
    }

    public void setResourcesHours(double[] resourcesHours) {
        this.resourcesHours = resourcesHours;
    }

    public int getResourcesAssignments() {
        return resourcesAssignments;
    }

    public void setResourcesAssignments(int resourcesAssignments) {
        this.resourcesAssignments = resourcesAssignments;
    }

    public class SummaryItem {
        int regular;
        int excess;
        int total;

        public int getRegular() {
            return regular;
        }

        public void setRegular(int regular) {
            this.regular = regular;
        }

        public int getExcess() {
            return excess;
        }

        public void setExcess(int excess) {
            this.excess = excess;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }

}
