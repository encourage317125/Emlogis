package com.emlogis.engine.domain.communication;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.optaplanner.core.api.score.constraint.primint.IntConstraintMatch;

import com.emlogis.engine.domain.Employee;

public class IntConstraintMatchDto {
	protected String constraintName;
	protected String employeeId;
	protected int weight;
	
	public IntConstraintMatchDto() {
		this.constraintName = "";
		this.weight = 0; 
		this.employeeId = "";
	}

 	public IntConstraintMatchDto(IntConstraintMatch constraintMatch) {
		this.constraintName = constraintMatch.getConstraintName();
		this.weight = constraintMatch.getWeight(); 
		this.employeeId = getEmployeeInConflict(constraintMatch);
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IntConstraintMatchDto [constraintName=");
		builder.append(constraintName);
		builder.append(", employeeId=");
		builder.append(employeeId);
		builder.append(", weight=");
		builder.append(weight);
		builder.append("]");
		return builder.toString();
	}

	//Worker Methods//
	private String getEmployeeInConflict(IntConstraintMatch constraintMatch){
		Employee emp = null;
		emp = (Employee) CollectionUtils.find(constraintMatch.getJustificationList(), new Predicate<Object>() {

			@Override
			public boolean evaluate(Object object) {
				if(object instanceof Employee){
					return true;
				}
				return false;
			}
		});
		if(emp != null){
			return emp.getEmployeeId();
		} else {
			return ""; //TODO: Need to return ShiftId and/or EmployeeId
		}
	}

}
