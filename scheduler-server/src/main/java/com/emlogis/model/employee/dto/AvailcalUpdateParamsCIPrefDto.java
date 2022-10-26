package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailcalUpdateParamsCIPrefDto extends AvailcalUpdateParamsPrefDto implements AvailabilityWorkflowRequest, Serializable {

	@Override
	@JsonIgnore
	public String getActionStr() {
		return getAction().name();
	}

	@Override
	@JsonIgnore
	public AvailabilityRequestSubtype getType() {
		return AvailabilityRequestSubtype.AvailcalUpdateParamsCIPrefDto;
	}

	AvailcalUpdateParamsCIDaySelections selectedDays = new AvailcalUpdateParamsCIDaySelections();
	Long effectiveStartDate = new DateTime( 2015, 1, 1, 0, 0).getMillis();
	Long effectiveEndDate = null;
	
	public AvailcalUpdateParamsCIDaySelections getSelectedDays() {return selectedDays;}
	public void setSelectedDays(AvailcalUpdateParamsCIDaySelections selectedDays) {this.selectedDays = selectedDays;}
	public Long getEffectiveStartDate() {return effectiveStartDate;}
	public void setEffectiveStartDate(Long effectiveStartDate) {this.effectiveStartDate = effectiveStartDate;}
	public Long getEffectiveEndDate() {return effectiveEndDate;}
	public void setEffectiveEndDate(Long effectiveEndDate) {this.effectiveEndDate = effectiveEndDate;}
}
