package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmployeeAvailabilityDto implements Serializable {

    private List<ScheduleDto> schedules = new ArrayList<>();
    private List<OrgHolidayDto> orgHolidays = new ArrayList<>();
    private List<EmployeeUnavailabilityDto> empUnavailabilities = new ArrayList<>();

    private Map<String,Integer>	openShiftsByDays;	// map of days with OpenShifts available:
    // k=date range (as startDate-endDate ), v=nb of OpenShifts
    // with startDate & endDate = date time in millis

    public static class BaseDetailDto implements Serializable {

        private long startDate;
        private long endDate;

        public long getStartDate() {
            return startDate;
        }

        public void setStartDate(long startDate) {
            this.startDate = startDate;
        }

        public long getEndDate() {
            return endDate;
        }

        public void setEndDate(long endDate) {
            this.endDate = endDate;
        }
    }

    public static class ScheduleDto extends BaseDetailDto {

		private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

	public static class OrgHolidayDto extends BaseDetailDto {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
	
	public static class EmployeeUnavailabilityDto extends BaseDetailDto {
        private Boolean pto;

        public Boolean isPto() {
            return pto == null ? false : pto;
        }

        public void setPto(Boolean pto) {
            this.pto = pto;
        }
    }

	public List<ScheduleDto> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<ScheduleDto> schedules) {
		this.schedules = schedules;
	}

	public List<OrgHolidayDto> getOrgHolidays() {
		return orgHolidays;
	}

	public void setOrgHolidays(List<OrgHolidayDto> orgHolidays) {
		this.orgHolidays = orgHolidays;
	}

	public List<EmployeeUnavailabilityDto> getEmpUnavailabilities() {
		return empUnavailabilities;
	}

	public void setEmpUnavailabilities(List<EmployeeUnavailabilityDto> empUnavailabilities) {
		this.empUnavailabilities = empUnavailabilities;
	}

	public Map<String, Integer> getOpenShiftsByDays() {
		return openShiftsByDays;
	}

	public void setOpenShiftsByDays(Map<String, Integer> openShiftsByDays) {
		this.openShiftsByDays = openShiftsByDays;
	}

}
