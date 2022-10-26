package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.model.dto.UpdateDto;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.ScheduleType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleUpdateDto extends UpdateDto implements Serializable {

    public static final String NAME = "name";

	private String name;
	private String description;
    private Long startDate;
	private ScheduleStatus status;
    private int	scheduleLengthInDays = -1;		// -1 means ignore value on update
	private boolean	preservePreAssignedShifts;
    private boolean	preservePostAssignedShifts;
    private boolean	preserveEngineAssignedShifts;
    private int maxComputationTime = 60;				// max computation time in secs. = 1 min;
    private int maximumUnimprovedSecondsSpent;

    private Map<RuleName, Integer> ruleWeightMultipliers; 	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public ScheduleStatus getStatus() {
		return status;
	}

	public void setStatus(ScheduleStatus status) {
		this.status = status;
	}

	public int getScheduleLengthInDays() {
		return scheduleLengthInDays;
	}

	public void setScheduleLengthInDays(int scheduleLengthInDays) {
		this.scheduleLengthInDays = scheduleLengthInDays;
	}

	public boolean getPreservePreAssignedShifts() {
		return preservePreAssignedShifts;
	}

	public void setPreservePreAssignedShifts(boolean preservePreassignedShifts) {
		this.preservePreAssignedShifts = preservePreassignedShifts;
	}

	public boolean getPreservePostAssignedShifts() {
		return preservePostAssignedShifts;
	}

	public void setPreservePostAssignedShifts(boolean preservePostAssignedShifts) {
		this.preservePostAssignedShifts = preservePostAssignedShifts;
	}

	public boolean getPreserveEngineAssignedShifts() {
		return preserveEngineAssignedShifts;
	}

	public void setPreserveEngineAssignedShifts(boolean preserveEngineAssignedShifts) {
		this.preserveEngineAssignedShifts = preserveEngineAssignedShifts;
	}

	public int getMaxComputationTime() {
		return maxComputationTime;
	}

	public void setMaxComputationTime(int maxComputationTime) {
		this.maxComputationTime = maxComputationTime;
	}

	public int getMaximumUnimprovedSecondsSpent() {
		return maximumUnimprovedSecondsSpent;
	}

	public void setMaximumUnimprovedSecondsSpent(int maximumUnimprovedSecondsSpent) {
		this.maximumUnimprovedSecondsSpent = maximumUnimprovedSecondsSpent;
	}

	public Map<RuleName, Integer> getRuleWeightMultipliers() {
		return ruleWeightMultipliers;
	}

	public void setRuleWeightMultipliers(Map<RuleName, Integer> ruleWeightMultipliers) {
		this.ruleWeightMultipliers = ruleWeightMultipliers;
	}

}
