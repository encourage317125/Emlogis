package com.emlogis.model.employee.dto;

import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailcalUpdateParamsCDPrefDto extends AvailcalUpdateParamsPrefDto implements AvailabilityWorkflowRequest, Serializable {

	private Collection<Long> selectedDates = new ArrayList<Long>();
	
	public Collection<Long> getSelectedDates() {return selectedDates;}
	public void setSelectedDates(Collection<Long> selectedDates) {this.selectedDates = selectedDates;}

	@Override
	@JsonIgnore
	public String getActionStr() {
		return getAction().name();
	}

	@Override
	@JsonIgnore
	public AvailabilityRequestSubtype getType() {
		return AvailabilityRequestSubtype.AvailcalUpdateParamsCDPrefDto;
	}

}
