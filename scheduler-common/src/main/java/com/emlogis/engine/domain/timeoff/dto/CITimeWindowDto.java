package com.emlogis.engine.domain.timeoff.dto;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.timeoff.CDOverrideAvailDate;

public abstract class CITimeWindowDto extends TimeWindowDto {
	protected DayOfWeek dayOfWeek;
	
	protected DateTime effectiveStart;
	protected DateTime effectiveEnd;

	protected List<CDOverrideAvailDate> cdOverrides; // List of objects which may override this CI time window
	
	public CITimeWindowDto(){
		super();
	}
	
	public CITimeWindowDto(CITimeWindowDto dto){
		super(dto);
		this.dayOfWeek = dto.dayOfWeek;
	}
	
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
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

	public List<CDOverrideAvailDate> getCdOverrides() {
		return cdOverrides;
	}

	public void setCdOverrides(List<CDOverrideAvailDate> cdOverrides) {
		this.cdOverrides = cdOverrides;
	}

	public void addCDOverride(CDOverrideAvailDate cdOverride){
		if (cdOverrides==null){
			cdOverrides = new ArrayList<CDOverrideAvailDate>();
		}
		cdOverrides.add(cdOverride);
	}
	
	
	@Override
	public String toString() {
		return "CITimeWindowDto [dayOfWeek=" + dayOfWeek + ", effectiveStart="
				+ effectiveStart + ", effectiveEnd=" + effectiveEnd + "]";
	}

}
