package com.emlogis.engine.domain;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ShiftDate implements Serializable {

    private static final LocalTime endOfDayTime = new LocalTime(11, 59, 59);

    private int dayIndex;
	private LocalDate date;

	public ShiftDate(LocalDate date) {
		this.date = date;
	}

	public ShiftDate(Date date) {
		this.date = new LocalDate(date);
	}

	public ShiftDate() {
		date = LocalDate.now();
	}

	public ShiftDate plusDays(int days) {
		return new ShiftDate(date.plusDays(days));
	}

	public ShiftDate minusDays(int days) {
		return new ShiftDate(date.minusDays(days));
	}

	/**
	 * Different database conventions and different libraries define the
	 * numerical value of the day of the week differently.
	 *
	 * Therefore the translation to DayOfWeek should be left up to specific
	 * interfaces.
	 *
	 * @return
	 */
	@JsonIgnore
	public DayOfWeek getDayOfWeek() {
		switch (date.getDayOfWeek()) {
		case 1:
			return DayOfWeek.MONDAY;
		case 2:
			return DayOfWeek.TUESDAY;
		case 3:
			return DayOfWeek.WEDNESDAY;
		case 4:
			return DayOfWeek.THURSDAY;
		case 5:
			return DayOfWeek.FRIDAY;
		case 6:
			return DayOfWeek.SATURDAY;
		case 7:
			return DayOfWeek.SUNDAY;
		default:
			throw new IllegalArgumentException("The calendarDayInWeek ("
					+ date.getDayOfWeek() + ") is not supported.");
		}
	}

	public int getDayIndex() {
		return dayIndex;
	}

	public void setDayIndex(int dayIndex) {
		this.dayIndex = dayIndex;
	}

	@JsonIgnore
	public String getDateString() {
		return date.toString();
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public boolean isBefore(ShiftDate date) {
		return this.date.isBefore(date.getDate());
	}

	public boolean isBeforeOrEquals(ShiftDate date) {
		return this.date.isBefore(date.getDate()) || this.date.equals(date.getDate());
	}

	public boolean isBeforeOrEquals(DateTime date) {
		DateTime endOfday = this.date.toDateTime(endOfDayTime);
		return endOfday.isBefore(date) || endOfday.equals(date);
	}

	public boolean isAfter(ShiftDate date) {
		return this.date.isAfter(date.getDate());
	}

	public boolean isAfter(DateTime date) {
		DateTime startOfDay = this.date.toDateTimeAtStartOfDay();
		return startOfDay.isAfter(date);
	}

	public boolean isAfterOrEquals(ShiftDate date) {
		return this.date.isAfter(date.getDate()) || this.date.equals(date.getDate());
	}

	public boolean isAfterOrEquals(DateTime date) {
		DateTime startOfDay = this.date.toDateTimeAtStartOfDay();
		return startOfDay.isAfter(date) || startOfDay.equals(date);
	}

	@JsonIgnore
	public ShiftDate getDateOfFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
		int numDaysBack = getDayOfWeek().getDistanceToPrevious(firstDayOfWeek);
		LocalDate firstDayOfWeekDate = date.minusDays(numDaysBack);
		ShiftDate firstDayOfWeekShiftDate = new ShiftDate(firstDayOfWeekDate);
		return firstDayOfWeekShiftDate;
	}

	public boolean isInPlanningWindow(EmployeeRosterInfo info){
		return info.isInPlanningWindow(this);
	}

	public boolean isInScheduleWindow(EmployeeRosterInfo info){
		return info.isInScheduleWindow(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShiftDate [date=");
		builder.append(date.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((date == null) ? 0 : date.hashCode());
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
		ShiftDate other = (ShiftDate) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}

}
