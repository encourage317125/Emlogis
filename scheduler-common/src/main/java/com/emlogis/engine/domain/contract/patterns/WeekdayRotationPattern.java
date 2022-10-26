package com.emlogis.engine.domain.contract.patterns;

import com.emlogis.engine.domain.DayOfWeek;

public class WeekdayRotationPattern extends Pattern {
	private DayOfWeek dayOfWeek;
	private int numberOfDays;
	private int outOfTotalDays;
	
	private RotationPatternType rotationType;
	
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getNumberOfDays() {
		return numberOfDays;
	}

	public void setNumberOfDays(int numberOfDays) {
		this.numberOfDays = numberOfDays;
	}

	public int getOutOfTotalDays() {
		return outOfTotalDays;
	}

	public void setOutOfTotalDays(int outOfTotalDays) {
		this.outOfTotalDays = outOfTotalDays;
	}

	public RotationPatternType getRotationType() {
		return rotationType;
	}

	public void setRotationType(RotationPatternType rotationType) {
		this.rotationType = rotationType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dayOfWeek == null) ? 0 : dayOfWeek.hashCode());
		result = prime * result + numberOfDays;
		result = prime * result + outOfTotalDays;
		result = prime * result
				+ ((rotationType == null) ? 0 : rotationType.hashCode());
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
		WeekdayRotationPattern other = (WeekdayRotationPattern) obj;
		if (dayOfWeek != other.dayOfWeek)
			return false;
		if (numberOfDays != other.numberOfDays)
			return false;
		if (outOfTotalDays != other.outOfTotalDays)
			return false;
		if (rotationType != other.rotationType)
			return false;
		return true;
	}

	public enum RotationPatternType {
		DAYS_OFF_PATTERN,
		DAYS_ON_PATTERN	
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WeekdayRotationPattern [dayOfWeek=");
		builder.append(dayOfWeek);
		builder.append(", numberOfDays=");
		builder.append(numberOfDays);
		builder.append(", outOfTotalDays=");
		builder.append(outOfTotalDays);
		builder.append(", rotationType=");
		builder.append(rotationType);
		builder.append("]");
		return builder.toString();
	}
}
