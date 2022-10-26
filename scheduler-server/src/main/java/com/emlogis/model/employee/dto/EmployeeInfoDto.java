package com.emlogis.model.employee.dto;

import com.emlogis.engine.domain.DayOfWeek;
import org.joda.time.DateTimeZone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmployeeInfoDto implements Serializable {
	
	public static class TeamDto implements Serializable {
		public String id;
		public String name;
		public Boolean isHomeTeam;
	}
	
	public static class SkillDto implements Serializable {
		public String id;
		public String abbreviation;
		public Boolean isPrimary;
	}
	
	private EmployeeDto employeeDto;
	private String calendarSyncUrl;
	private String accountId;
	private DateTimeZone siteTz;
	private String siteName;
	private String siteId;
	private DayOfWeek siteFirstDayOfweek;
	private List<TeamDto> teams = new ArrayList<>();
	private List<SkillDto> skills = new ArrayList<>();

	public EmployeeDto getEmployeeDto() {
		return employeeDto;
	}

	public void setEmployeeDto(EmployeeDto employeeDto) {
		this.employeeDto = employeeDto;
	}

	public DateTimeZone getSiteTz() {
		return siteTz;
	}

	public void setSiteTz(DateTimeZone siteTz) {
		this.siteTz = siteTz;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public List<TeamDto> getTeams() {
		return teams;
	}

	public void setTeams(List<TeamDto> teams) {
		this.teams = teams;
	}

	public List<SkillDto> getSkills() {
		return skills;
	}

	public void setSkills(List<SkillDto> skills) {
		this.skills = skills;
	}

	public DayOfWeek getSiteFirstDayOfweek() {
		return siteFirstDayOfweek;
	}

	public void setSiteFirstDayOfweek(DayOfWeek siteFirstDayOfweek) {
		this.siteFirstDayOfweek = siteFirstDayOfweek;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

    public String getCalendarSyncUrl() {
        return calendarSyncUrl;
    }

    public void setCalendarSyncUrl(String calendarSyncUrl) {
        this.calendarSyncUrl = calendarSyncUrl;
    }
}
