package com.emlogis.model.structurelevel.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.WeekendDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteDto extends StructureLevelDto {

    private WeekendDefinition weekendDefinition;
    private DayOfWeek firstDayOfWeek;
    private boolean isDeleted;
    private boolean isNotificationEnabled;

    // Value's examples: Pacific/Auckland, Etc/GMT+12 etc.
    // Possible values can be found by link http://joda-time.sourceforge.net/timezones.html (Canonical ID)
    private String timeZone;
    private int timeZoneOffset;
 	private String abbreviation;
 	private String address;
 	private String address2;
 	private String city; 
 	private String state;
 	private String country;
 	private String zip;
    
    // Site Scheduling configuration options
 	private int shiftIncrements;
 	private int shiftOverlaps;
 	private int maxConsecutiveShifts;
 	private int timeOffBetweenShifts;
 	private boolean enableWIPFragments;
    private long twoWeeksOvertimeStartDate;         
    
    
    public WeekendDefinition getWeekendDefinition() {
        return weekendDefinition;
    }

    public void setWeekendDefinition(WeekendDefinition weekendDefinition) {
        this.weekendDefinition = weekendDefinition;
    }

    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

	public int getTimeZoneOffset() {
		return timeZoneOffset;
	}

	public void setTimeZoneOffset(int timeZoneOffset) {
		this.timeZoneOffset = timeZoneOffset;
	}

	public boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean getIsNotificationEnabled() {
		return isNotificationEnabled;
	}

	public void setIsNotificationEnabled(boolean isNotificationEnabled) {
		this.isNotificationEnabled = isNotificationEnabled;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public int getShiftIncrements() {
		return shiftIncrements;
	}

	public void setShiftIncrements(int shiftIncrements) {
		this.shiftIncrements = shiftIncrements;
	}

	public int getShiftOverlaps() {
		return shiftOverlaps;
	}

	public void setShiftOverlaps(int shiftOverlaps) {
		this.shiftOverlaps = shiftOverlaps;
	}

	public int getMaxConsecutiveShifts() {
		return maxConsecutiveShifts;
	}

	public void setMaxConsecutiveShifts(int maxConsecutiveShifts) {
		this.maxConsecutiveShifts = maxConsecutiveShifts;
	}

	public int getTimeOffBetweenShifts() {
		return timeOffBetweenShifts;
	}

	public void setTimeOffBetweenShifts(int timeOffBetweenShifts) {
		this.timeOffBetweenShifts = timeOffBetweenShifts;
	}

	public boolean isEnableWIPFragments() {
		return enableWIPFragments;
	}

	public void setEnableWIPFragments(boolean enableWIPFragments) {
		this.enableWIPFragments = enableWIPFragments;
	}

	public long getTwoWeeksOvertimeStartDate() {
		return twoWeeksOvertimeStartDate;
	}

	public void setTwoWeeksOvertimeStartDate(long twoWeeksOvertimeStartDate) {
		this.twoWeeksOvertimeStartDate = twoWeeksOvertimeStartDate;
	}

}