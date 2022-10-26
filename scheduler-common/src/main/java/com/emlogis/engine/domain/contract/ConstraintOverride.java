package com.emlogis.engine.domain.contract;

import com.emlogis.engine.domain.Employee;

public class ConstraintOverride {
	Employee 				employee;
	ConstraintOverrideType 	type;
	
	public ConstraintOverride() {

	}
	
	public ConstraintOverride(Employee employee, ConstraintOverrideType type) {
		this.employee = employee;
		this.type = type;
	}
	
	public Employee getEmployee() {
		return employee;
	}
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
	public ConstraintOverrideType getType() {
		return type;
	}
	public void setType(ConstraintOverrideType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((employee == null) ? 0 : employee.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ConstraintOverride other = (ConstraintOverride) obj;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConstraintOverride [employee=");
		builder.append(employee);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

}
