package com.emlogis.engine.domain.communication;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.emlogis.engine.domain.communication.constraints.ScoreLevelResultDto;

public abstract class ScheduleResultDto implements Serializable {

	// The score constraints are rated by importance
	// Index 0 = most important, Index size-1 = Least Important
	protected List<ScoreLevelResultDto> hardScores;
	protected List<ScoreLevelResultDto> softScores;
	
	protected String id; // TODO remove latter. It has been done just for ability to be serialized

	public ScheduleResultDto(){
		hardScores = Collections.EMPTY_LIST; // Set to empty to prevent NPE
		softScores = Collections.EMPTY_LIST;
	}
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public List<ScoreLevelResultDto> getHardScores() {
		return hardScores;
	}

	public void setHardScores(List<ScoreLevelResultDto> hardScores) {
		this.hardScores = hardScores;
	}

	public List<ScoreLevelResultDto> getSoftScores() {
		return softScores;
	}

	public void setSoftScores(List<ScoreLevelResultDto> softScores) {
		this.softScores = softScores;
	}
    
}
