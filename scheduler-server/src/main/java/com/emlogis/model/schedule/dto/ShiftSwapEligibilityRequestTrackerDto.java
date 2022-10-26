package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.communication.ScheduleCompletion;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.model.dto.Dto;
import com.emlogis.model.schedule.TaskState;
import com.emlogis.scheduler.engine.communication.request.RequestType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** QualificationRequestTracker DTO for event service use.
 * @author emlogis
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftSwapEligibilityRequestTrackerDto extends Dto {
	
    private String name;
	private	String scheduleId;
	private	String requestId;
	private	String tenantId;

	private RequestType requestType;         // Qualification or Eligibility (determines how qualificationShifts should be interpreted)
	
    private DateTime startDate;              // planning window start date

    private DateTime endDate;                // planning window end date

    private int scheduleLengthInDays = 7;    // period in Days covered by schedule (like 1, 2, 3 4 weeks, ..)

    private int maxComputationTime = -1;     // max computation time in secs. -1 = infinite;
    private int maximumUnimprovedSecondsSpent = 100;


    private TaskState state = TaskState.Idle;       // operational state

    private ScheduleCompletion completion;          // completion code (OK, Error, Abort)

    private String completionInfo;					// short user friendly indication about completion

    private int hardScore = 0;					// engine execution hard score result

    private int softScore = 0;					// engine execution soft score result
    
    private String engineId;                    // Id of engine that did the last schedule execution

    private String engineLabel;                 // label of engine that did the last schedule execution
    
    private	long shiftGenerationDuration = -1;		//
    private	long employeeGenerationDuration = -1;	//
    private	long requestGenerationDuration = -1;	//
    private	long responseProcessingDuration = -1;	//
    private	long returnedOpenShifts = -1;			//	nb of open shifts returned by engine (can be != actual open shifts due to manual edits)
    private	long returnedAssignedShifts = -1;		//  nb of generated shifts returned by engine (can be != actual assigned shifts due to manual edits)

    private int scheduledTeamCount = -1;
    private int scheduledEmployeeCount = -1;

    private DateTime executionStartDate = new DateTime(0);      // date/time the last execution has been fired (date/time UTC)

    private DateTime requestSentDate = new DateTime(0);         // date/time the last request has been sent to Engine (date/time UTC)

    private DateTime executionAckDate = new DateTime(0);        // date/time the last execution has been acknowledged by Engine (date/time UTC)

    private DateTime responseReceivedDate = new DateTime(0);    // date/time the last response has been received (successfully or not) by engine (date/time UTC)

    private DateTime executionEndDate = new DateTime(0);        // date/time the last execution has been completed (successfully or not) by engine (date/time UTC)

    private String completionReport; 							// engine execution report data for providing feedback to user.
    
    private Map<String, Collection<ShiftQualificationDto>> qualificationShiftsMap = new HashMap<String, Collection<ShiftQualificationDto>>();



    
	public long getStartDate() {
        return startDate == null ? 0 : startDate.toDate().getTime();
    }

    public void setStartDate(long startDate) {
        this.startDate = new DateTime(startDate);
    }

    public long getEndDate() {
        return endDate == null ? 0 : endDate.getMillis();
    }

    public void setEndDate(long endDate) {
        this.endDate = new DateTime(endDate);
    }

    public int getScheduleLengthInDays() {
        return scheduleLengthInDays;
    }

	public void setScheduleLengthInDays(int scheduleLengthInDays) {
        this.scheduleLengthInDays = scheduleLengthInDays;
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

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getCompletionReport() {
		return completionReport;
	}

	public void setCompletionReport(String completionReport) {
		this.completionReport = completionReport;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Map<String, Collection<ShiftQualificationDto>> getQualificationShiftsMap() {
		return qualificationShiftsMap;
	}

	public void setQualificationShiftsMap(Map<String, Collection<ShiftQualificationDto>> qualificationShiftsMap) {
		this.qualificationShiftsMap = qualificationShiftsMap;
	}

	
}
