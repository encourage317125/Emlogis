package com.emlogis.model.structurelevel.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.WeekendDefinition;
import com.emlogis.model.contract.dto.OvertimeDto;
import com.emlogis.model.dto.UpdateDto;

import java.util.Map;

public class SiteUpdateDto extends UpdateDto {

    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ABBREVIATION = "abbreviation";
    public static final String ADDRESS = "address";
    public static final String ADDRESS2 = "address2";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String COUNTRY = "country";
    public static final String ZIP = "zip";
    public static final String SHIFT_INCREMENTS = "shiftIncrements";
    public static final String SHIFT_OVERLAPS = "shiftOverlaps";
    public static final String MAX_CONSECUTIVE_SHIFTS = "maxConsecutiveShifts";
    public static final String TIME_OFF_BETWEEN_SHIFTS = "timeOffBetweenShifts";

    private	String aomEntityType;
    private Map<String,Object> properties;	// aom properties
    private String name;
    private String description;
    private WeekendDefinition weekendDefinition;
    private DayOfWeek firstDayOfWeek;
    private Boolean isDeleted;
    private Boolean isNotificationEnabled;

    // Value's examples: Pacific/Auckland, Etc/GMT+12 etc.
    // Possible values can be found by link http://joda-time.sourceforge.net/timezones.html (Canonical ID)
    private String timeZone;
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
    private Boolean enableWIPFragments;
    private long twoWeeksOvertimeStartDate;
    
	private OvertimeDto overtimeDto;

    public String getAomEntityType() {
        return aomEntityType;
    }

    public void setAomEntityType(String aomEntityType) {
        this.aomEntityType = aomEntityType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getIsNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setIsNotificationEnabled(Boolean isNotificationEnabled) {
        this.isNotificationEnabled = isNotificationEnabled;
    }

    public Boolean getEnableWIPFragments() {
        return enableWIPFragments;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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

    public Boolean isEnableWIPFragments() {
        return enableWIPFragments;
    }

    public void setEnableWIPFragments(Boolean enableWIPFragments) {
        this.enableWIPFragments = enableWIPFragments;
    }

    public long getTwoWeeksOvertimeStartDate() {
        return twoWeeksOvertimeStartDate;
    }

    public void setTwoWeeksOvertimeStartDate(long twoWeeksOvertimeStartDate) {
        this.twoWeeksOvertimeStartDate = twoWeeksOvertimeStartDate;
    }

	public OvertimeDto getOvertimeDto() {
		return overtimeDto;
	}

	public void setOvertimeDto(OvertimeDto overtimeDto) {
		this.overtimeDto = overtimeDto;
	}
}
