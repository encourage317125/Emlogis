package com.emlogis.engine.domain.contract.patterns;

import java.util.Collection;

import com.emlogis.engine.domain.DayOfWeek;


public class CompleteWeekendWorkPattern extends Pattern {
	private Collection<DayOfWeek> daysOffAfter;
	private Collection<DayOfWeek> daysOffBefore;
	
	public Collection<DayOfWeek> getDaysOffAfter() {
		return daysOffAfter;
	}
	public void setDaysOffAfter(Collection<DayOfWeek> daysOffAfter) {
		this.daysOffAfter = daysOffAfter;
	}
	public Collection<DayOfWeek> getDaysOffBefore() {
		return daysOffBefore;
	}
	public void setDaysOffBefore(Collection<DayOfWeek> daysOffBefore) {
		this.daysOffBefore = daysOffBefore;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WeekendWorkPattern [daysOffAfter=");
		builder.append(daysOffAfter);
		builder.append(", daysOffBefore=");
		builder.append(daysOffBefore);
		builder.append("]");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((daysOffAfter == null) ? 0 : daysOffAfter.hashCode());
		result = prime * result + ((daysOffBefore == null) ? 0 : daysOffBefore.hashCode());
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
		CompleteWeekendWorkPattern other = (CompleteWeekendWorkPattern) obj;
		if (daysOffAfter == null) {
			if (other.daysOffAfter != null)
				return false;
		} else if (!daysOffAfter.equals(other.daysOffAfter))
			return false;
		if (daysOffBefore == null) {
			if (other.daysOffBefore != null)
				return false;
		} else if (!daysOffBefore.equals(other.daysOffBefore))
			return false;
		return true;
	}
}
