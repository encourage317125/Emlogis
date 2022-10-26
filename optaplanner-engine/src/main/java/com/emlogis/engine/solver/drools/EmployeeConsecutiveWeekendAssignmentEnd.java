package com.emlogis.engine.solver.drools;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.contract.Contract;

public class EmployeeConsecutiveWeekendAssignmentEnd implements Comparable<EmployeeConsecutiveWeekendAssignmentEnd>, Serializable {

    private Employee employee;
    private int sundayIndex;

    public EmployeeConsecutiveWeekendAssignmentEnd(Employee employee, int sundayIndex) {
        this.employee = employee;
        this.sundayIndex = sundayIndex;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getSundayIndex() {
        return sundayIndex;
    }

    public void setSundayIndex(int sundayIndex) {
        this.sundayIndex = sundayIndex;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof EmployeeConsecutiveWeekendAssignmentEnd) {
            EmployeeConsecutiveWeekendAssignmentEnd other = (EmployeeConsecutiveWeekendAssignmentEnd) o;
            return new EqualsBuilder()
                    .append(employee, other.employee)
                    .append(sundayIndex, other.sundayIndex)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(employee)
                .append(sundayIndex)
                .toHashCode();
    }

    public int compareTo(EmployeeConsecutiveWeekendAssignmentEnd other) {
        return new CompareToBuilder()
                .append(employee, other.employee)
                .append(sundayIndex, other.sundayIndex)
                .toComparison();
    }

    @Override
    public String toString() {
        return employee + " weekend ... - " + sundayIndex;
    }

}
