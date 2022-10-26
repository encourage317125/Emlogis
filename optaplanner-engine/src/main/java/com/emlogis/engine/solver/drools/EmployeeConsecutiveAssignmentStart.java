package com.emlogis.engine.solver.drools;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftDate;

public class EmployeeConsecutiveAssignmentStart implements Comparable<EmployeeConsecutiveAssignmentStart>,
        Serializable {

    private Employee employee;
    private ShiftDate shiftDate;

    public EmployeeConsecutiveAssignmentStart(Employee employee, ShiftDate shiftDate) {
        this.employee = employee;
        this.shiftDate = shiftDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ShiftDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(ShiftDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof EmployeeConsecutiveAssignmentStart) {
            EmployeeConsecutiveAssignmentStart other = (EmployeeConsecutiveAssignmentStart) o;
            return new EqualsBuilder()
                    .append(employee, other.employee)
                    .append(shiftDate, other.shiftDate)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(employee)
                .append(shiftDate)
                .toHashCode();
    }

    public int compareTo(EmployeeConsecutiveAssignmentStart other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(shiftDate, other.shiftDate)
                .toComparison();
    }

    @Override
    public String toString() {
        return employee + " " + shiftDate + " - ...";
    }

    public int getShiftDateDayIndex() {
        return shiftDate.getDayIndex();
    }

}
