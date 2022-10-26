package com.emlogis.model.schedule;

import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenShiftEligibilityRequestTracker extends EngineRequestTracker implements Serializable {
    
    private Collection<ShiftQualificationDto> qualificationShifts = new ArrayList<ShiftQualificationDto>();

	public Collection<ShiftQualificationDto> getQualificationShifts() {
		return qualificationShifts;
	}

	public void setQualificationShifts(
			Collection<ShiftQualificationDto> qualificationShifts) {
		this.qualificationShifts = qualificationShifts;
	}
	
}
