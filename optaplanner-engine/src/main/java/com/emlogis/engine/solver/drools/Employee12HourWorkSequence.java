package com.emlogis.engine.solver.drools;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.Days;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftDate;

public class Employee12HourWorkSequence implements Comparable<Employee12HourWorkSequence>, Serializable {

    private Employee employee;
    private ShiftDate firstDay;
    private ShiftDate lastDay;

    public Employee12HourWorkSequence(Employee employee, ShiftDate firstDay, ShiftDate lastDay) {
        this.employee = employee;
        this.firstDay = firstDay;
        this.lastDay = lastDay;
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

	public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ShiftDate getFirstDayIndex() {
        return firstDay;
    }

    public void setFirstDayIndex(ShiftDate firstDay) {
        this.firstDay = firstDay;
    }

    public ShiftDate getLastDayIndex() {
        return lastDay;
    }

    public void setLastDayIndex(ShiftDate lastDay) {
        this.lastDay = lastDay;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Employee12HourWorkSequence) {
            Employee12HourWorkSequence other = (Employee12HourWorkSequence) o;
            return new EqualsBuilder()
                    .append(employee, other.employee)
                    .append(firstDay, other.firstDay)
                    .append(lastDay, other.lastDay)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(employee)
                .append(firstDay)
                .append(lastDay)
                .toHashCode();
    }

    public int compareTo(Employee12HourWorkSequence other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(firstDay, other.firstDay)
                .append(lastDay, other.lastDay)
                .toComparison();
    }

    @Override
    public String toString() {
        return employee + " is working between " + firstDay + " - " + lastDay;
    }

    public int getDayLength() {
    	return Days.daysBetween( firstDay.getDate(), lastDay.getDate() ).getDays() + 1;
    }

}
