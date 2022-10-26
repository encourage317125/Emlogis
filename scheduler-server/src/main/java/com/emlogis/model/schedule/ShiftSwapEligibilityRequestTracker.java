package com.emlogis.model.schedule;

import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftSwapEligibilityRequestTracker extends EngineRequestTracker implements Serializable {
    
	private Map<String, Collection<ShiftQualificationDto>> qualificationShifts = new HashMap<>();

	public Map<String, Collection<ShiftQualificationDto>> getQualificationShifts() {
		return qualificationShifts;
	}

	public void setQualificationShifts(Map<String, Collection<ShiftQualificationDto>> qualificationShifts) {
		this.qualificationShifts = qualificationShifts;
	}
	
}
