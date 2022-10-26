package com.emlogis.engine.solver.drools;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.emlogis.engine.domain.Employee;

public class EmployeeWeekendSequence implements Comparable<EmployeeWeekendSequence>, Serializable {

    private Employee employee;
    private int firstSundayIndex;
    private int lastSundayIndex;

    public EmployeeWeekendSequence(Employee employee, int firstSundayIndex, int lastSundayIndex) {
        this.employee = employee;
        this.firstSundayIndex = firstSundayIndex;
        this.lastSundayIndex = lastSundayIndex;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getFirstSundayIndex() {
        return firstSundayIndex;
    }

    public void setFirstSundayIndex(int firstSundayIndex) {
        this.firstSundayIndex = firstSundayIndex;
    }

    public int getLastSundayIndex() {
        return lastSundayIndex;
    }

    public void setLastSundayIndex(int lastSundayIndex) {
        this.lastSundayIndex = lastSundayIndex;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof EmployeeWeekendSequence) {
            EmployeeWeekendSequence other = (EmployeeWeekendSequence) o;
            return new EqualsBuilder()
                    .append(employee, other.employee)
                    .append(firstSundayIndex, other.firstSundayIndex)
                    .append(lastSundayIndex, other.lastSundayIndex)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(employee)
                .append(firstSundayIndex)
                .append(lastSundayIndex)
                .toHashCode();
    }

    public int compareTo(EmployeeWeekendSequence other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(firstSundayIndex, other.firstSundayIndex)
                .append(lastSundayIndex, other.lastSundayIndex)
                .toComparison();
    }

    @Override
    public String toString() {
        return employee + " is working the weekend of " + firstSundayIndex + " - " + lastSundayIndex;
    }

    public int getWeekendLength() {
        return ((lastSundayIndex - firstSundayIndex) / 7) + 1;
    }

}
