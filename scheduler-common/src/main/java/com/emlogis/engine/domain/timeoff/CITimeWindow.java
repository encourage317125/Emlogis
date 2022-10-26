package com.emlogis.engine.domain.timeoff;

import java.util.List;

import org.joda.time.DateTime;

import com.emlogis.engine.domain.DayOfWeek;

public abstract class CITimeWindow extends TimeWindow {
	private DayOfWeek dayOfWeek;
	
	protected DateTime effectiveStart;
	protected DateTime effectiveEnd;

	protected List<CDOverrideAvailDate> cdOverrides; // List of objects which may override this CI time window

	public List<CDOverrideAvailDate> getCdOverrides() {
		return cdOverrides;
	}

	public void setCdOverrides(List<CDOverrideAvailDate> cdOverrides) {
		this.cdOverrides = cdOverrides;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}
	
	public DateTime getEffectiveStart() {
		return effectiveStart;
	}

	public void setEffectiveStart(DateTime effectiveStart) {
		this.effectiveStart = effectiveStart;
	}

	public DateTime getEffectiveEnd() {
		return effectiveEnd;
	}

	public void setEffectiveEnd(DateTime effectiveEnd) {
		this.effectiveEnd = effectiveEnd;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CITimeOff [dayOfWeek=");
		builder.append(dayOfWeek);
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}
}
