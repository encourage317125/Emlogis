package com.emlogis.engine.solver.drools;

import org.joda.time.Weeks;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftDate;

public class EmployeeWeekdayRotationSequence {
	
	private Employee employee;

	private ShiftDate firstDay;
	private ShiftDate lastDay;

	public EmployeeWeekdayRotationSequence(Employee employee,
			ShiftDate firstDay, ShiftDate lastDay) {
		this.employee = employee;
		this.firstDay = firstDay;
		this.lastDay = lastDay;
	}
	
	public boolean startsAfterLastDayOf(EmployeeWeekdayRotationSequence seq){
		return firstDay.isAfter(seq.getLastDay());
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public ShiftDate getFirstDay() {
		return firstDay;
	}

	public void setFirstDay(ShiftDate firstDay) {
		this.firstDay = firstDay;
	}

	public ShiftDate getLastDay() {
		return lastDay;
	}

	public void setLastDay(ShiftDate lastDay) {
		this.lastDay = lastDay;
	}

	public DayOfWeek getDayOfWeek() {
		return firstDay.getDayOfWeek();
	}

	public int getConsecutiveWeekdays() {
		return Weeks.weeksBetween(firstDay.getDate(), lastDay.getDate()).getWeeks() + 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((employee == null) ? 0 : employee.hashCode());
		result = prime * result
				+ ((firstDay == null) ? 0 : firstDay.hashCode());
		result = prime * result + ((lastDay == null) ? 0 : lastDay.hashCode());
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
		EmployeeWeekdayRotationSequence other = (EmployeeWeekdayRotationSequence) obj;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
			return false;
		if (firstDay == null) {
			if (other.firstDay != null)
				return false;
		} else if (!firstDay.equals(other.firstDay))
			return false;
		if (lastDay == null) {
			if (other.lastDay != null)
				return false;
		} else if (!lastDay.equals(other.lastDay))
			return false;
		return true;
	}


}
