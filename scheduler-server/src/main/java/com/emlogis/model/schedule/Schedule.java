package com.emlogis.model.schedule;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.engine.domain.communication.ScheduleCompletion;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.shiftpattern.PatternElt;
import com.emlogis.model.structurelevel.Team;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "Schedule")
@Table(indexes={
		@Index(name="SCHEDULE_STARTDATE_INDEX", unique=false, columnList="startDate") ,
		@Index(name="SCHEDULE_ENDDATE_INDEX", unique=false, columnList="endDate"),
		@Index(name="SCHEDULE_STATUS_INDEX", unique=false, columnList="status"),
		@Index(name="SCHEDULE_STATE_INDEX", unique=false, columnList="state")
})
public class Schedule extends BaseEntity implements Cloneable {
	
	private	ScheduleType scheduleType = ScheduleType.ShiftStructureBased;	// Schedule type: ShiftPattern or ShiftStructure Based
	
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime startDate;                // planning window start date

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime endDate;                // planning window end date

    private int scheduleLengthInDays = 7;    // period in Days covered by schedule (like 1, 2, 3 4 weeks, ..)

    private int maxComputationTime = 60;        // max computation time in secs. 60 = 1min;
    private int maximumUnimprovedSecondsSpent = 100;

    private String name;

    private String description;

    private boolean posted = false;

    private ScheduleStatus status = ScheduleStatus.Simulation;
    
    private boolean	preservePreAssignedShifts = false;	// flag to preserve the pre-assigned shifts on shift re-generation & schedule duplication
    													// pre-assigned shifts are shifts that have been assigned prior to an engine execution
    													// Ex: generate shifts, pre-assign, execute, (1) re-genenerate shifts or (2) re-execute
    													// on 1 and 2, if flag is true, system will not delete pre-assigned shifts.
    
    private boolean	preserveEngineAssignedShifts = false;	// flag to preserve the shifts automatically assigned by engine generation
														// on shift re-generation & schedule duplication

    private boolean	preservePostAssignedShifts = false;	// flag to preserve the shifts manually assigned POST shift generation
    													// on e-generation & schedule duplication

    @Column(unique = true, length = 1024)
    private String ruleWeightMultipliers; 			// json serialized map of weight multiplier for each rule (k=rule, v=multiplier)

    private TaskState state = TaskState.Idle;        // operational state

    private ScheduleCompletion completion;          // completion code (OK, Error, Abort)

    private String completionInfo;					// short user friendly indication about completion

    private int hardScore = 0;					// engine execution hard score result computed from engineScore
    private int softScore = 0;					// engine execution soft score result computed from engineScore
    
    private String engineId;                    // Id of engine that did the last schedule execution

    private String engineLabel;                 // label of engine that did the last schedule execution
    
    private	long shiftGenerationDuration = -1;	//
    private	long employeeGenerationDuration = -1;	//
    private	long requestGenerationDuration = -1;	//
    private	long responseProcessingDuration = -1;	//
    private	long returnedOpenShifts = -1;			//	nb of open shifts returned by engine (can be != actual open shifts due to manual edits)
    private	long returnedAssignedShifts = -1;		//  nb of generated shifts returned by engine (can be != actual assigned shifts due to manual edits)

    private int scheduledTeamCount = -1;
    private int scheduledEmployeeCount = -1;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime executionStartDate = new DateTime(0);        // date/time the last execution has been fired (date/time UTC)

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime requestSentDate = new DateTime(0);            // date/time the last request has been sent to Engine (date/time UTC)

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime executionAckDate = new DateTime(0);        // date/time the last execution has been acknowledged by Engine (date/time UTC)

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime responseReceivedDate = new DateTime(0);        // date/time the last response has been received (successfully or not) by engine (date/time UTC)

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime executionEndDate = new DateTime(0);        // date/time the last execution has been completed (successfully or not) by engine (date/time UTC)

    @ManyToMany
    @JoinTable(name = "Team_Schedule",
            joinColumns = {@JoinColumn(name = "schedules_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "schedules_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "Team_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "Team_id", referencedColumnName = "id")})
    private Set<Team> teams;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ShiftStructure_Schedule",
            joinColumns = {@JoinColumn(name = "schedules_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "schedules_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "ShiftStructure_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "ShiftStructure_id", referencedColumnName = "id")})
    private Set<ShiftStructure> shiftStructures;

    private String requestId;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "optionsId", referencedColumnName = "id"),
            @JoinColumn(name = "optionsTenantId", referencedColumnName = "tenantId")
    })
    private SchedulingOptions schedulingOptions;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "reportId", referencedColumnName = "id"),
            @JoinColumn(name = "reportTenantId", referencedColumnName = "tenantId")
    })
    private ScheduleReport scheduleReport;

    private String scheduleGroupId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PatternElt> patternElts;

    public Schedule() {
    	setupDefaultRuleWeightMultipliers();
    }

	public Schedule(PrimaryKey primaryKey) {
        super(primaryKey);
        setupDefaultRuleWeightMultipliers();
    }

    @PrePersist
    public void prePersist() {
    	super.prePersist();
    	
    	// Now synchronize the endDate if we can calculate it from the startDate...
    	if (startDate != null) {
        	endDate = startDate.plusDays(scheduleLengthInDays);
    	}
    }

    private void setupDefaultRuleWeightMultipliers() {
    	Map<RuleName, Integer> ruleWeights = new HashMap<>();
		ruleWeights.put(RuleName.CD_TIME_OFF_CONSTRAINT , 1);
		ruleWeights.put(RuleName.CI_TIME_OFF_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MAX_CONSECUTIVE_DAYS_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, 1);
		ruleWeights.put(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MIN_HOURS_PER_WEEK_CONSTRAINT, 1);
		ruleWeights.put(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, 1);
		ruleWeights.put(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE, 1);
		ruleWeights.put(RuleName.SKILL_MATCH_RULE, 40);
		ruleWeights.put(RuleName.TEAM_ASSOCIATION_CONSTRAINT, 10);
		ruleWeights.put(RuleName.TEAM_ASSOCIATION_CONSTRAINT_FLOAT, 10);
		ruleWeights.put(RuleName.WEEKDAY_ROTATION_PATTERN_RULE, 1);
		ruleWeights.put(RuleName.COUPLED_WEEKEND_RULE, 1);
		ruleWeights.put(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE, 1);
		ruleWeights.put(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE, 1);
		ruleWeights.put(RuleName.AVOID_DAILY_OVERTIME_RULE, 1);
		ruleWeights.put(RuleName.AVOID_WEEKLY_OVERTIME_RULE, 1);
		ruleWeights.put(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE, 1);
		ruleWeights.put(RuleName.OVERLAPPING_SHIFTS_RULE, 10);	
		this.setRuleWeightMultipliers(ruleWeights);
	}
    
    public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(ScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}

	public long getStartDate() {
        return startDate == null ? 0 : startDate.toDate().getTime();
    }

    public void setStartDate(long startDate) {
        this.startDate = new DateTime(startDate);
        this.endDate = this.startDate.plusDays(scheduleLengthInDays);
    }

    public long getEndDate() {
        if (startDate != null) {
            return startDate.plusDays(scheduleLengthInDays).getMillis();
        } else {
            return 0;
        }
//    	return endDate == null ? 0 : endDate.getMillis();
    }

    public int getScheduleLengthInDays() {
        return scheduleLengthInDays;
    }

    public void setScheduleLengthInDays(int scheduleLengthInDays) {
        this.scheduleLengthInDays = scheduleLengthInDays;
        this.endDate = new DateTime(getStartDate()).plusDays(scheduleLengthInDays);
    }

    public boolean isPreservePreAssignedShifts() {
		return preservePreAssignedShifts;
	}

	public void setPreservePreAssignedShifts(boolean preservePreAssignedShifts) {
		this.preservePreAssignedShifts = preservePreAssignedShifts;
	}

	public boolean isPreservePostAssignedShifts() {
		return preservePostAssignedShifts;
	}

	public void setPreservePostAssignedShifts(boolean preservePostAssignedShifts) {
		this.preservePostAssignedShifts = preservePostAssignedShifts;
	}

    public boolean isPreserveEngineAssignedShifts() {
        return preserveEngineAssignedShifts;
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

    @SuppressWarnings("unchecked")
    public Map<RuleName, Integer> getRuleWeightMultipliers() {
    	if (StringUtils.isEmpty(ruleWeightMultipliers)) {
    		return new HashMap<>();
    	} else {
    		return (Map<RuleName, Integer>) EmlogisUtils.fromJsonString(ruleWeightMultipliers);
    	}
	}
    
	public void setRuleWeightMultipliers(Map<RuleName, Integer> ruleWeights) {
		ruleWeightMultipliers = EmlogisUtils.toJsonString(ruleWeights);
	}

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
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
		return 0;
	}

	public void setMediumScore(int mediumScore) {
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

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }

    public Set<ShiftStructure> getShiftStructures() {
        return shiftStructures;
    }

    public void setShiftStructures(Set<ShiftStructure> shiftStructures) {
        this.shiftStructures = shiftStructures;
    }

    public SchedulingOptions getSchedulingOptions() {
        return schedulingOptions;
    }

    public void setSchedulingOptions(SchedulingOptions schedulingOptions) {
        this.schedulingOptions = schedulingOptions;
    }

    public ScheduleReport getScheduleReport() {
        return scheduleReport;
    }

    public void setScheduleReport(ScheduleReport scheduleReport) {
        this.scheduleReport = scheduleReport;
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

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
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

	public long getExecutionStartDate() {
        return executionStartDate.getMillis();
    }

    public void setExecutionStartDate(long executionStartDate) {
        this.executionStartDate = new DateTime(executionStartDate);
    }

    public long getRequestSentDate() {
        return requestSentDate.getMillis();
    }

    public void setRequestSentDate(long requestSentDate) {
        this.requestSentDate = new DateTime(requestSentDate);
    }

    public long getResponseReceivedDate() {
        return responseReceivedDate.getMillis();
    }

    public void setResponseReceivedDate(long responseReceivedDate) {
        this.responseReceivedDate = new DateTime(responseReceivedDate);
    }

    public long getExecutionAckDate() {
        return executionAckDate.getMillis();
    }

    public void setExecutionAckDate(long executionAckDate) {
        this.executionAckDate = new DateTime(executionAckDate);
    }

    public long getExecutionEndDate() {
        return executionEndDate.getMillis();
    }

    public void setExecutionEndDate(long executionEndDate) {
        this.executionEndDate = new DateTime(executionEndDate);
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

    public Set<PatternElt> getPatternElts() {
        return patternElts;
    }

    public void setPatternElts(Set<PatternElt> patternElts) {
        this.patternElts = patternElts;
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

    @Override
    public Schedule clone() throws CloneNotSupportedException {
        Schedule result = (Schedule) super.clone();
        result.setName(result.getName() + " Copy");
        Set<Team> teams = new HashSet<>();
        teams.addAll(getTeams());
        result.setTeams(teams);

        Set<ShiftStructure> shiftStructures = new HashSet<>();
        shiftStructures.addAll(getShiftStructures());
        result.setShiftStructures(shiftStructures);

        Set<PatternElt> patternElts = new HashSet<>();
        patternElts.addAll(getPatternElts());
        result.setPatternElts(patternElts);

        SchedulingOptions schedulingOptions = getSchedulingOptions();
        result.setSchedulingOptions(schedulingOptions);

        return result;
    }
}
