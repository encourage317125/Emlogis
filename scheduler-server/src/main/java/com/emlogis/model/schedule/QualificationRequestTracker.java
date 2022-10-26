package com.emlogis.model.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;

import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class QualificationRequestTracker extends EngineRequestTracker implements Serializable {
    
    private Collection<ShiftQualificationDto> qualificationShifts = new ArrayList<>();

	public Collection<ShiftQualificationDto> getQualificationShifts() {
		return qualificationShifts;
	}

	public void setQualificationShifts(
			Collection<ShiftQualificationDto> qualificationShifts) {
		this.qualificationShifts = qualificationShifts;
	}
	
}
