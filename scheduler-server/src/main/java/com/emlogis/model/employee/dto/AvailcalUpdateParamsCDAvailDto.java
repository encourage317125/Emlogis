package com.emlogis.model.employee.dto;

import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailcalUpdateParamsCDAvailDto extends AvailcalUpdateParamsAvailDto implements AvailabilityWorkflowRequest, Serializable {

	private Collection<Long> selectedDates = new ArrayList<Long>();
	private boolean pto = false;
	private String absenceTypeId;
	
	public Collection<Long> getSelectedDates() {return selectedDates;}
	public void setSelectedDates(Collection<Long> selectedDates) {this.selectedDates = selectedDates;}
	public boolean isPto() {return pto;}
	public void setPto(boolean pto) {this.pto = pto;}
	public String getAbsenceTypeId() {return absenceTypeId;}
	public void setAbsenceTypeId(String absenceTypeId) {this.absenceTypeId = absenceTypeId;}

	@Override
	@JsonIgnore
	public String getActionStr() {
		return getAction().name();
	}

	@Override
	@JsonIgnore
	public AvailabilityRequestSubtype getType() {
		return AvailabilityRequestSubtype.AvailcalUpdateParamsCDAvailDto;
	}
	
	public String toString(DateTimeZone timeZone) {
		if (timeZone == null){timeZone = DateTimeZone.UTC;}

		StringBuilder ret = new StringBuilder();
		ret.append("\n\nAvailcalUpdateParamsCDAvailDto ["
				+ "getAction()=" + getAction() + ", ");
		
		
		ret.append("getSelectedDates()=[");
		for (Long selectedDate: selectedDates){
			ret.append(selectedDates + " (" + new DateTime(selectedDate, timeZone) + "), ");
		}
		ret.append(", ");

		ret.append("getTimeFrames()=" + getTimeFrames() + ", "
				+ "isPto()=" + isPto() + ", "
				+ "getAbsenceTypeId()=" + getAbsenceTypeId() + "]\n\n");
		return ret.toString();
	}
}
