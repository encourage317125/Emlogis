package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.DateTime;

public class AvailcalUpdateParamsCDCopyDto implements Serializable {
	
	public enum Repeat implements Serializable {
		EVERY_WEEK,
		EVERY_OTHER_WEEK,
		EVERY_THIRD_WEEK		
	}

	private Collection<Long> selectedDates = new ArrayList<Long>();
	private long effectiveStartDate = new DateTime( 2015, 1, 1, 0, 0).getMillis();
	private long effectiveEndDate;
	private boolean availability;
	private boolean preference;
	private Repeat repeat;
	
	public Collection<Long> getSelectedDates() {return selectedDates;}
	public void setSelectedDates(Collection<Long> selectedDates) {this.selectedDates = selectedDates;}
	public long getEffectiveStartDate() {return effectiveStartDate;}
	public void setEffectiveStartDate(long effectiveStartDate) {this.effectiveStartDate = effectiveStartDate;}
	public Long getEffectiveEndDate() {return effectiveEndDate;}
	public void setEffectiveEndDate(Long effectiveEndDate) {this.effectiveEndDate = effectiveEndDate;}
	public boolean isAvailability() {return availability;}
	public void setAvailability(boolean availability) {this.availability = availability;}
	public boolean isPreference() {return preference;}
	public void setPreference(boolean preference) {this.preference = preference;}
	public Repeat getRepeat() {return repeat;}
	public void setRepeat(Repeat repeat) {this.repeat = repeat;}
}
