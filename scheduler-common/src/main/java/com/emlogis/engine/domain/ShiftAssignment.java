package com.emlogis.engine.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.emlogis.engine.domain.solver.EmployeeStrengthComparator;
import com.emlogis.engine.domain.solver.ShiftAssignmentDifficultyFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@PlanningEntity(difficultyWeightFactoryClass = ShiftAssignmentDifficultyFactory.class)
@JsonInclude(Include.NON_NULL)
public class ShiftAssignment {

	private Shift shift;
	private int indexInShift;
	private boolean isLocked = false;

	public String getShiftId(){
		return shift.getId();
	}
	
	// Planning variables: changes during planning, between score calculations.
	@JsonInclude(Include.NON_NULL)
	private Employee employee;
	
	@JsonIgnore
	public boolean isExcessShift() {
		return shift.isExcessShift();
	}
	
	@JsonIgnore
	public int getShiftDurationSeconds() {
		return shift.getShiftDurationSeconds();
	}
	
	@JsonIgnore
	public int getShiftDurationHours() {
		return shift.getShiftDurationHours();
	}
	
	@JsonIgnore
	public int getShiftDurationMinutes() {
		return shift.getShiftDurationMinutes();
	}

	public Shift getShift() {
		return shift;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public int getIndexInShift() {
		return indexInShift;
	}
	
	@JsonIgnore
	public LocalTime getShiftStartTime(){
		return shift.getShiftStartTime();
	}
	
	@JsonIgnore
	public LocalTime getShiftEndTime(){
		return shift.getShiftEndTime();
	}
	

	public void setIndexInShift(int indexInShift) {
		this.indexInShift = indexInShift;
	}

	@PlanningVariable(valueRangeProviderRefs = { "employeeRange" }, nullable = true, 
					strengthComparatorClass = EmployeeStrengthComparator.class)
	public Employee getEmployee() {
		return employee;
	}
	
	@JsonIgnore
	public String getEmployeeId() {
		return employee.getEmployeeId();
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	// ************************************************************************
	// Complex methods
	// ************************************************************************

	public boolean startsOnOrAfter(ShiftDate shiftDate) {
		return shift.startsOnOrAfter(shiftDate);
	}
	
	public boolean startsOnOrBefore(ShiftDate shiftDate) {
		return shift.startsOnOrBefore(shiftDate);
	}

	public boolean startsBefore(ShiftDate shiftDate) {
		return shift.startsBefore(shiftDate);
	}

	public boolean startsAfter(ShiftDate shiftDate) {
		return shift.startsAfter(shiftDate);
	}

	public boolean startsBefore(ShiftAssignment shiftAssignment) {
		return shift.startsBefore(shiftAssignment.getShift());
	}
	
	public boolean startsOnOrAfterShiftStarts(ShiftAssignment shiftAssignment) {
		return shift.startsAfterShiftStarts(shiftAssignment.getShift())
				|| shift.getShiftStartDateTime().equals(shiftAssignment.getShift().getShiftStartDateTime());
	}
	
	public boolean startsOnOrAfterShiftEnds(ShiftAssignment shiftAssignment) {
		return shift.startsAfterShiftEnds(shiftAssignment.getShift())
				|| shift.getShiftStartDateTime().equals(shiftAssignment.getShift().getShiftEndDateTime());
	}
	
	public boolean startsAfterShiftEnds(ShiftAssignment shiftAssignment) {
		return shift.startsAfterShiftEnds(shiftAssignment.getShift());
	}
	
	public boolean startsAfter(DateTime shiftDateTime){
		return getShiftStartDateTime().isAfter(shiftDateTime);
	}
	
	public boolean startsBefore(DateTime shiftDateTime){
		return getShiftStartDateTime().isBefore(shiftDateTime);
	}
	
	public boolean startsOnOrAfter(DateTime shiftDateTime){
		return getShiftStartDateTime().isAfter(shiftDateTime) || getShiftStartDateTime().equals(shiftDateTime);
	}
	
	public boolean startsOnOrBefore(DateTime shiftDateTime){
		return getShiftStartDateTime().isBefore(shiftDateTime) || getShiftStartDateTime().equals(shiftDateTime);
	}
	
	public boolean endsOnOrAfter(DateTime shiftDateTime){
		return getShiftEndDateTime().isAfter(shiftDateTime) || getShiftEndDateTime().equals(shiftDateTime);
	}
	
	public boolean endsOnOrBefore(DateTime shiftDateTime){
		return getShiftEndDateTime().isBefore(shiftDateTime) || getShiftEndDateTime().equals(shiftDateTime);
	}
	
	public boolean endsOnOrBefore(ShiftDate shiftDateTime){
		return endsOnOrBefore(shiftDateTime.getDate().toDateTimeAtStartOfDay());
	}
	
	public boolean endsAfter(DateTime shiftDateTime){
		return getShiftEndDateTime().isAfter(shiftDateTime);
	}
	
	public boolean endsBefore(DateTime shiftDateTime){
		return getShiftEndDateTime().isBefore(shiftDateTime);
	}
	
	public boolean startsAfter(LocalTime shiftTime){
		return getShiftStartTime().isAfter(shiftTime);
	}
	
	public boolean startsOnOrAfter(LocalTime shiftTime){
		return getShiftStartTime().isAfter(shiftTime) || getShiftStartTime().equals(shiftTime);
	}
	
	public boolean startsOnOrBefore(LocalTime shiftTime){
		return getShiftStartTime().isBefore(shiftTime) || getShiftStartTime().equals(shiftTime);
	}
	
	public boolean startsBefore(LocalTime shiftTime){
		return getShiftStartTime().isBefore(shiftTime);
	}
	
	public boolean endsOnOrAfter(LocalTime shiftTime){
		return getShiftEndTime().isAfter(shiftTime) || getShiftEndTime().equals(shiftTime);
	}
	
	public boolean endsAfter(LocalTime shiftTime){
		return getShiftEndTime().isAfter(shiftTime);
	}
	
	public boolean endsBefore(LocalTime shiftTime){
		return getShiftEndTime().isBefore(shiftTime);
	}

	@JsonIgnore
	public DateTime getShiftStartDateTime() {
		return shift.getShiftStartDateTime();
	}

	@JsonIgnore
	public DateTime getShiftEndDateTime() {
		return shift.getShiftEndDateTime();
	}
	
	@JsonIgnore
	public ShiftDate getShiftDate() {
		return shift.getStartShiftDate();
	}
	
	@JsonIgnore
	public LocalDate getStartDate() {
		return shift.getStartShiftDate().getDate();
	}

	@JsonIgnore
	public ShiftType getShiftType() {
		return shift.getShiftType();
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	@JsonIgnore
	public int getShiftDateDayIndex() {
		return shift.getStartShiftDate().getDayIndex();
	}

	@JsonIgnore
	public DayOfWeek getShiftDateDayOfWeek() {
		return shift.getStartShiftDate().getDayOfWeek();
	}

	@JsonIgnore
	public DayOfWeek getDayOfWeek() {
		return shift.getStartShiftDate().getDayOfWeek();
	}

	@JsonIgnore
	public boolean isWeekendShift(WeekendDefinition weekendDefinition) {
		return weekendDefinition.isWeekend(getDayOfWeek());
	}
	
	@JsonIgnore
	public boolean isSameWeekend(ShiftAssignment shiftAssignment) {
		if (getDayOfWeek() == DayOfWeek.SATURDAY) {
			return getShiftDate().plusDays(1).equals(shiftAssignment.getShiftDate());
		} else if (getDayOfWeek() == DayOfWeek.SUNDAY) {
			return getShiftDate().minusDays(1).equals(shiftAssignment.getShiftDate());
		}
		return false;
	}

	@JsonIgnore
	public boolean isInScheduleWindow(EmployeeRosterInfo info) {
		if(shift == null){
			return false;
		}
		return shift.isInScheduleWindow(info);
	}
	
	@JsonIgnore
	public boolean isInPlanningWindow(EmployeeRosterInfo info) {
		if(shift == null){
			return false;
		}
		return shift.isInPlanningWindow(info);
	}

	@JsonIgnore
	public String getTeamId(){
		return shift.getTeamId();
	}
	
	@JsonIgnore
	public String getSkillId(){
		return shift.getSkillId();
	}
	
	@JsonIgnore
	public boolean isBeingQualified(){
		return shift.isBeingQualified();
	}
	
	@JsonIgnore
	public boolean isBeingSwapped(){
		return shift.isBeingSwapped();
	}
	
	/**
	 * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot
	 * be used because the rule engine already requires them (for performance in
	 * their original state).
	 * 
	 * @see #solutionHashCode()
	 */
	public boolean solutionEquals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof ShiftAssignment) {
			ShiftAssignment other = (ShiftAssignment) o;
			return new EqualsBuilder().append(shift, other.shift).append(employee, other.employee).isEquals();
		} else {
			return false;
		}
	}

	/**
	 * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot
	 * be used because the rule engine already requires them (for performance in
	 * their original state).
	 * 
	 * @see #solutionEquals(Object)
	 */
	public int solutionHashCode() {
		return new HashCodeBuilder().append(shift).append(employee).toHashCode();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + indexInShift;
		result = prime * result + ((shift == null) ? 0 : shift.hashCode());
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
		ShiftAssignment other = (ShiftAssignment) obj;
		if (indexInShift != other.indexInShift)
			return false;
		if (shift == null) {
			if (other.shift != null)
				return false;
		} else if (!shift.equals(other.shift))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ShiftAssignment [shift=" + shift + ", employee=" + employee + "]";
	}


}
