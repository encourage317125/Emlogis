package com.emlogis.engine.database.loader;

import java.util.List;

import org.joda.time.LocalDate;

import com.emlogis.engine.domain.EmployeeRosterInfo;
import com.emlogis.engine.domain.EmployeeSchedule;

public abstract class AbstractEngineLoader {
	protected long scheduleId;
	protected int siteId;
	protected List<Long> teamIds;
	protected LocalDate planningStartDate;
	protected LocalDate scheduleEndDate;
	protected EmployeeRosterInfo employeeRoster;

	public AbstractEngineLoader(long scheduleId, int siteId, List<Long> teamIds, LocalDate scheduleStartDate,
			LocalDate scheduleEndDate) {
		this.scheduleId = scheduleId;
		this.siteId = siteId;
		this.teamIds = teamIds;
		this.planningStartDate = scheduleStartDate;
		this.scheduleEndDate = scheduleEndDate;
		
		employeeRoster = new EmployeeRosterInfo();
	}

	public abstract EmployeeSchedule getStartingSchedule();
	
	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public LocalDate getScheduleStartDate() {
		return planningStartDate;
	}

	public void setScheduleStartDate(LocalDate scheduleStartDate) {
		this.planningStartDate = scheduleStartDate;
	}

	public LocalDate getScheduleEndDate() {
		return scheduleEndDate;
	}

	public void setScheduleEndDate(LocalDate scheduleEndDate) {
		this.scheduleEndDate = scheduleEndDate;
	}

	public List<Long> getTeamIds() {
		return teamIds;
	}

	public void setTeamIds(List<Long> teamIds) {
		this.teamIds = teamIds;
	}

	public LocalDate getPlanningStartDate() {
		return planningStartDate;
	}

	public void setPlanningStartDate(LocalDate planningStartDate) {
		this.planningStartDate = planningStartDate;
	}

	public EmployeeRosterInfo getEmployeeRoster() {
		return employeeRoster;
	}

	public void setEmployeeRoster(EmployeeRosterInfo employeeRoster) {
		this.employeeRoster = employeeRoster;
	}
}
