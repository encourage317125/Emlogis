package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.communication.ScheduleCompletion;
import com.emlogis.model.dto.BaseEntityDto;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.ScheduleType;
import com.emlogis.model.schedule.TaskState;

import java.util.Map;

public class ScheduleDto extends BaseEntityDto {

    public final static String START_DATE = "startDate";
    
    private ScheduleType scheduleType;

    private long startDate;

    private long endDate;

    private int	scheduleLengthInDays = 7;
    
    private boolean	preservePreAssignedShifts;
    private boolean	preservePostAssignedShifts;
    private boolean	preserveEngineAssignedShifts;


    private int maxComputationTime;		// max computation time in secs. -1 = infinite;
    private int maximumUnimprovedSecondsSpent;

    private String name;

    private String description;
    
    private Map<String, Integer> ruleWeightMultipliers; 	

    private boolean posted = false;

    private ScheduleStatus status;

    private TaskState state;

    private ScheduleCompletion completion;

    private String completionInfo;

    private int hardScore = 0;
    private int mediumScore = 0;
    private int softScore = 0;

    private String engineId;
    private String engineLabel;

    private long executionStartDate;
    
    private long requestSentDate;

    private long executionAckDate;

    private long responseReceivedDate;

    private long executionEndDate;

    private String requestId;
    
    private String scheduleGroupId;
    
    private	long shiftGenerationDuration = -1;	//
    private	long employeeGenerationDuration = -1;	//
    private	long requestGenerationDuration = -1;	//
    private	long responseProcessingDuration = -1;	//

    private	long returnedOpenShifts = -1;			//	nb of open shifts returned by engine (can be != actual open shifts due to manual edits)
    private	long returnedAssignedShifts = -1;		//  nb of generated shifts returned by engine (can be != actual assigned shifts due to manual edits)

    private int scheduledTeamCount = -1;
    private int scheduledEmployeeCount = -1;


    public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(ScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}
	
    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
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

	public Map<String, Integer> getRuleWeightMultipliers() {
		return ruleWeightMultipliers;
	}

	public void setRuleWeightMultipliers(Map<String, Integer> ruleWeightMultipliers) {
		this.ruleWeightMultipliers = ruleWeightMultipliers;
	}

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

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public ScheduleStatus getStatus() {
		return status;
	}

	public void setStatus(ScheduleStatus status) {
		this.status = status;
	}

	public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public ScheduleCompletion getCompletion() {
        return completion;
    }

    public void setCompletion(ScheduleCompletion completion) {
        this.completion = completion;
    }

    public String getCompletionInfo() {
        return completionInfo;
    }

    public void setCompletionInfo(String completionInfo) {
        this.completionInfo = completionInfo;
    }

	public int getHardScore() {
		return hardScore;
	}

	public void setHardScore(int hardScore) {
		this.hardScore = hardScore;
	}

	public int getMediumScore() {
		return mediumScore;
	}

	public void setMediumScore(int mediumScore) {
		this.mediumScore = mediumScore;
	}

	public int getSoftScore() {
		return softScore;
	}

	public void setSoftScore(int softScore) {
		this.softScore = softScore;
	}

	public String getEngineId() {
		return engineId;
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}

	public String getEngineLabel() {
		return engineLabel;
	}

	public void setEngineLabel(String engineLabel) {
		this.engineLabel = engineLabel;
	}

	public long getExecutionStartDate() {
        return executionStartDate;
    }

    public void setExecutionStartDate(long executionStartDate) {
        this.executionStartDate = executionStartDate;
    }

    public long getExecutionAckDate() {
        return executionAckDate;
    }

    public void setExecutionAckDate(long executionAckDate) {
        this.executionAckDate = executionAckDate;
    }

    public long getExecutionEndDate() {
        return executionEndDate;
    }

    public void setExecutionEndDate(long executionEndDate) {
        this.executionEndDate = executionEndDate;
    }

    public long getRequestSentDate() {
		return requestSentDate;
	}

	public void setRequestSentDate(long requestSentDate) {
		this.requestSentDate = requestSentDate;
	}

	public long getResponseReceivedDate() {
		return responseReceivedDate;
	}

	public void setResponseReceivedDate(long responseReceivedDate) {
		this.responseReceivedDate = responseReceivedDate;
	}

	public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

	public String getScheduleGroupId() {
		return scheduleGroupId;
	}

	public void setScheduleGroupId(String scheduleGroupId) {
		this.scheduleGroupId = scheduleGroupId;
	}

	public long getShiftGenerationDuration() {
		return shiftGenerationDuration;
	}

	public void setShiftGenerationDuration(long shiftGenerationDuration) {
		this.shiftGenerationDuration = shiftGenerationDuration;
	}

	public long getEmployeeGenerationDuration() {
		return employeeGenerationDuration;
	}

	public void setEmployeeGenerationDuration(long employeeGenerationDuration) {
		this.employeeGenerationDuration = employeeGenerationDuration;
	}

	public long getRequestGenerationDuration() {
		return requestGenerationDuration;
	}

	public void setRequestGenerationDuration(long requestGenerationDuration) {
		this.requestGenerationDuration = requestGenerationDuration;
	}

	public long getResponseProcessingDuration() {
		return responseProcessingDuration;
	}

	public void setResponseProcessingDuration(long responseProcessingDuration) {
		this.responseProcessingDuration = responseProcessingDuration;
	}

	public long getReturnedOpenShifts() {
		return returnedOpenShifts;
	}

	public void setReturnedOpenShifts(long returnedOpenShifts) {
		this.returnedOpenShifts = returnedOpenShifts;
	}

	public long getReturnedAssignedShifts() {
		return returnedAssignedShifts;
	}

	public void setReturnedAssignedShifts(long returnedAssignedShifts) {
		this.returnedAssignedShifts = returnedAssignedShifts;
	}

	public int getScheduledTeamCount() {
		return scheduledTeamCount;
	}

	public void setScheduledTeamCount(int scheduledTeamCount) {
		this.scheduledTeamCount = scheduledTeamCount;
	}

	public int getScheduledEmployeeCount() {
		return scheduledEmployeeCount;
	}

	public void setScheduledEmployeeCount(int scheduledEmployeeCount) {
		this.scheduledEmployeeCount = scheduledEmployeeCount;
	}

}
