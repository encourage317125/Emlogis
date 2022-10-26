package com.emlogis.engine.solver.drools;

import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftDate;

import java.util.List;

public class EmployeeOvertimeTotal {
	private String employeeId;
	private ShiftDate OTDate; // Represent the day of OT or start of the week/two week period
	private int numOTMinutes;
	private OTType type; // Can be daily, weekly or two week
	
	private List<Shift> involvedShifts;
	
	public EmployeeOvertimeTotal(String employeeId, ShiftDate oTDate,
			int numOTMinutes, OTType type, List<Shift> involvedShifts) {
		super();
		this.employeeId = employeeId;
		OTDate = oTDate;
		this.numOTMinutes = numOTMinutes;
		this.type = type;
		this.involvedShifts = involvedShifts;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public ShiftDate getOTDate() {
		return OTDate;
	}

	public void setOTDate(ShiftDate oTDate) {
		OTDate = oTDate;
	}

	public int getNumOTMinutes() {
		return numOTMinutes;
	}

	public void setNumOTMinutes(int numOTMinutes) {
		this.numOTMinutes = numOTMinutes;
	}

	public OTType getType() {
		return type;
	}

	public void setType(OTType type) {
		this.type = type;
	}

	public List<Shift> getInvolvedShifts() {
		return involvedShifts;
	}

	public void setInvolvedShifts(List<Shift> involvedShifts) {
		this.involvedShifts = involvedShifts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((OTDate == null) ? 0 : OTDate.hashCode());
		result = prime * result
				+ ((employeeId == null) ? 0 : employeeId.hashCode());
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
		EmployeeOvertimeTotal other = (EmployeeOvertimeTotal) obj;
		if (OTDate == null) {
			if (other.OTDate != null)
				return false;
		} else if (!OTDate.equals(other.OTDate))
			return false;
		if (employeeId == null) {
			if (other.employeeId != null)
				return false;
		} else if (!employeeId.equals(other.employeeId))
			return false;
		return type == other.type;
	}

	public enum OTType {
		DAILY,
		WEEKLY,
		TWO_WEEK
	}
}
