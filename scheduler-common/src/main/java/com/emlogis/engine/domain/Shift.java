package com.emlogis.engine.domain;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Shift implements Serializable {
	private String id;

	private ShiftType shiftType;
	private int index;

	private int requiredEmployeeSize;
	private String skillId;
	private String teamId;
	
	// True if this shift needs to be constraint checked 
	// Or if it needs to be checked for open shift eligibility
	private boolean beingQualified = false;
	
	// True if this shift needs to be checked against
	// shifts being qualified to determine if they are swappable
	private boolean beingSwapped = false;
	
	private DateTime startDateTime;
	private DateTime endDateTime; 
	private boolean isExcessShift;
	
	public static final String FORCE_COMPLETION_PREFIX = "FC-";
	
	public Shift(){
	    
	}
	
	public Shift(Shift shift){
	    id = shift.id;
	    index = shift.index;
	    requiredEmployeeSize = shift.requiredEmployeeSize;
	    skillId = shift.skillId;
	    teamId = shift.teamId;
	    beingQualified = shift.beingQualified;
	    beingSwapped = shift.beingSwapped;
	    startDateTime = new DateTime(shift.startDateTime);
	    endDateTime = new DateTime(shift.endDateTime);
	    isExcessShift = shift.isExcessShift;
	}

	@JsonIgnore
	public int getShiftDurationSeconds() {
		return Seconds.secondsBetween(startDateTime, endDateTime).getSeconds();
	}

	@JsonIgnore
	public int getShiftDurationHours() {
		return Hours.hoursBetween(startDateTime, endDateTime).getHours();
	}
	
	@JsonIgnore
	public int getShiftDurationMinutes() {
		return Minutes.minutesBetween(startDateTime, endDateTime).getMinutes();
	}

	public ShiftDate getStartShiftDate() {
		return new ShiftDate(startDateTime.toLocalDate());
	}

	public ShiftType getShiftType() {
		return shiftType;
	}

	public void setShiftType(ShiftType shiftType) {
		this.shiftType = shiftType;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getRequiredEmployeeSize() {
		return requiredEmployeeSize;
	}

	public void setRequiredEmployeeSize(int requiredEmployeeSize) {
		this.requiredEmployeeSize = requiredEmployeeSize;
	}

	public boolean isExcessShift() {
		return isExcessShift;
	}
	
	public void setExcessShift(boolean excessShift){
		this.isExcessShift = excessShift;
	}

	public boolean startsBefore(Shift shift){
		return startDateTime.isBefore(shift.getShiftStartDateTime());
	}

	public boolean startsAfterShiftEnds(Shift shift){
		return startDateTime.isAfter(shift.getShiftEndDateTime());
	}

	public boolean startsAfterShiftStarts(Shift shift){
		return startDateTime.isAfter(shift.getShiftStartDateTime());
	}

	public boolean endsBefore(Shift shift){
		return endDateTime.isBefore(shift.getShiftEndDateTime());
	}

	public boolean endsAfter(Shift shift){
		return endDateTime.isAfter(shift.getShiftEndDateTime());
	}

	public boolean startsAfter(DateTime shiftDateTime){
		return startDateTime.isAfter(shiftDateTime);
	}

	public boolean startsBefore(DateTime shiftDateTime){
		return startDateTime.isBefore(shiftDateTime);
	}

	public boolean endsAfter(DateTime shiftDateTime){
		return endDateTime.isAfter(shiftDateTime);
	}

	public boolean endsBefore(DateTime shiftDateTime){
		return endDateTime.isBefore(shiftDateTime);
	}

	public boolean startsAfter(ShiftDate shiftDate){
		LocalTime endOfDay = new LocalTime(23, 59, 59);
		return startDateTime.isAfter(shiftDate.getDate().toDateTime(endOfDay));
	}

	public boolean startsOnOrAfter(ShiftDate shiftDate){
		DateTime startOfDay = shiftDate.getDate().toDateTimeAtStartOfDay();
		return startDateTime.isAfter(startOfDay) || getShiftStartDateTime().equals(startOfDay);
	}

	public boolean startsOnOrBefore(ShiftDate shiftDate){
		DateTime startOfDay = shiftDate.getDate().toDateTimeAtStartOfDay();
		return startDateTime.isBefore(startOfDay) || getShiftStartDateTime().equals(startOfDay);
	}

	public boolean startsBefore(ShiftDate shiftDate){
		LocalTime startOfDay = new LocalTime(0, 0, 0);
		return startDateTime.isBefore(shiftDate.getDate().toDateTime(startOfDay));
	}

	@JsonIgnore
	public DateTime getShiftStartDateTime(){
		return startDateTime;
	}

	@JsonIgnore
	public DateTime getShiftEndDateTime(){
		return endDateTime;
	}

	@JsonIgnore
	public LocalTime getShiftStartTime(){
		return startDateTime.toLocalTime();
	}

	@JsonIgnore
	public LocalTime getShiftEndTime(){
		return endDateTime.toLocalTime();
	}

	public ShiftDate getEndShiftDate() {
		return new ShiftDate(endDateTime.toLocalDate());
	}

	// TODO: Fix this to support shift assignments that cross midnight
	public boolean isInScheduleWindow(EmployeeRosterInfo info) {
		if (startDateTime == null || info == null) {
			return false;
		}
		return info.isInScheduleWindow(getStartShiftDate());
	}

	public boolean isInPlanningWindow(EmployeeRosterInfo info) {
		if (startDateTime == null || info == null) {
			return false;
		}
		return info.isInPlanningWindow(getStartShiftDate());
	}

	public String getSkillId() {
		return skillId;
	}

	public void setSkillId(String skillId) {
		this.skillId = skillId;
	}

	public String getId() {
		return id;
	}

	public void setId(String siteRequirementId) {
		this.id = siteRequirementId;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public boolean isBeingQualified() {
		return beingQualified;
	}

	public void setBeingQualified(boolean beingQualified) {
		this.beingQualified = beingQualified;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shift other = (Shift) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public DateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(DateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public DateTime getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(DateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	public boolean isBeingSwapped() {
		return beingSwapped;
	}

	public void setBeingSwapped(boolean beingSwapped) {
		this.beingSwapped = beingSwapped;
	}

	@Override
	public String toString() {
		return "Shift [id=" + id + ", skillId=" + skillId + ", teamId=" + teamId + ", beingQualified=" + beingQualified
				+ ", startDateTime=" + startDateTime + ", endDateTime=" + endDateTime + ", isExcessShift="
				+ isExcessShift + "]";
	}


}
