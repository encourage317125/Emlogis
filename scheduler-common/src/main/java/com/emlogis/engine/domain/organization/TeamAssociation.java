package com.emlogis.engine.domain.organization;

import com.emlogis.engine.domain.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class TeamAssociation {
	private String teamId;
	private Employee employee;
	private TeamAssociationType type;
	private boolean isHomeTeam;
	
	@JsonIgnore
	public String getEmployeeId(){
		return employee.getEmployeeId();
	}
	
	public Employee getEmployee() {
		return employee;
	}
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
	public TeamAssociationType getType() {
		return type;
	}
	public void setType(TeamAssociationType type) {
		this.type = type;
	}
	public boolean isHomeTeam() {
		return isHomeTeam;
	}
	public void setHomeTeam(boolean isHomeTeam) {
		this.isHomeTeam = isHomeTeam;
	}
	public String getTeamId() {
		return teamId;
	}
	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((employee == null) ? 0 : employee.hashCode());
		result = prime * result + ((teamId == null) ? 0 : teamId.hashCode());
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
		TeamAssociation other = (TeamAssociation) obj;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
			return false;
		if (teamId == null) {
			if (other.teamId != null)
				return false;
		} else if (!teamId.equals(other.teamId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TeamAssociation [teamId=");
		builder.append(teamId);
		builder.append(", employee=");
		builder.append(employee);
		builder.append(", type=");
		builder.append(type);
		builder.append(", isHomeTeam=");
		builder.append(isHomeTeam);
		builder.append("]");
		return builder.toString();
	}
	

	
}
