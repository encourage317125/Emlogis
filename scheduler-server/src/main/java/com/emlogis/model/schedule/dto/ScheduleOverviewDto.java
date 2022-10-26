package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class ScheduleOverviewDto extends BaseEntityDto{

    private OverviewRow shifts = new OverviewRow();
    private OverviewRow shiftsAssignments = new OverviewRow();
    private OverviewRow hours = new OverviewRow();
    private OverviewRow hoursAssignments= new OverviewRow();

    public OverviewRow getShifts() {
        return shifts;
    }

    public void setShifts(OverviewRow shifts) {
        this.shifts = shifts;
    }

    public OverviewRow getShiftsAssignments() {
        return shiftsAssignments;
    }

    public void setShiftsAssignments(OverviewRow shiftsAssignments) {
        this.shiftsAssignments = shiftsAssignments;
    }

    public OverviewRow getHours() {
        return hours;
    }

    public void setHours(OverviewRow hours) {
        this.hours = hours;
    }

    public OverviewRow getHoursAssignments() {
        return hoursAssignments;
    }

    public void setHoursAssignments(OverviewRow hoursAssignments) {
        this.hoursAssignments = hoursAssignments;
    }

    public class OverviewRow {

        private Object[] regular;
        private Object[] excess;
        private Object[] total;
        private Object[] employees;

        public Object[] getRegular() {
            return regular;
        }

        public void setRegular(Object[] regular) {
            this.regular = regular;
        }

        public Object[] getExcess() {
            return excess;
        }

        public void setExcess(Object[] excess) {
            this.excess = excess;
        }

        public Object[] getTotal() {
            return total;
        }

        public void setTotal(Object[] total) {
            this.total = total;
        }

        public Object[] getEmployees() {
            return employees;
        }

        public void setEmployees(Object[] employees) {
            this.employees = employees;
        }
    }
}
