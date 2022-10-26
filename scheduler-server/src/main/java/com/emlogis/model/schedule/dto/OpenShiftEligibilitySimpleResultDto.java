package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.communication.ScheduleCompletion;
import com.emlogis.model.dto.Dto;
import com.emlogis.model.schedule.TaskState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** QualificationRequestTracker DTO for event service use.
 * @author emlogis
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenShiftEligibilitySimpleResultDto extends Dto {
	
	public static class OpenShiftDto implements Serializable {
		private String 	id;
		private long 	startDateTime;
		private long 	endDateTime;
		private	int		shiftLength;
		private String 	skillId;
		private String 	skillName;
		private	String	teamId;
		private	String	teamName;
		private	long	posted;			// date the shift has been  Posted (if posted)
		private	boolean	isRequested;	// (aggregation of the isRequested of all employees associated to that Shift, if Posted
		private	int		reqCount;
		private	int		empCount;

		// other Shift attribute to be considered...
		// created
		// updated
		// skillAbbrev
		// excess
		// postId
		// deadline
		// dateAvailable

		private Set<EligibleEmployeeDto> employees = new HashSet<>();
				
		public OpenShiftDto() {
			super();
		}
		public OpenShiftDto(String id) {
			super();
			this.id = id;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public long getStartDateTime() {
			return startDateTime;
		}
		public void setStartDateTime(long startDateTime) {
			this.startDateTime = startDateTime;
		}
		public long getEndDateTime() {
			return endDateTime;
		}
		public void setEndDateTime(long endDateTime) {
			this.endDateTime = endDateTime;
		}
		public int getShiftLength() {
			return shiftLength;
		}
		public void setShiftLength(int shiftLength) {
			this.shiftLength = shiftLength;
		}
		public String getSkillId() {
			return skillId;
		}
		public void setSkillId(String skillId) {
			this.skillId = skillId;
		}
		public String getSkillName() {
			return skillName;
		}
		public void setSkillName(String skillName) {
			this.skillName = skillName;
		}
		public String getTeamId() {
			return teamId;
		}
		public void setTeamId(String teamId) {
			this.teamId = teamId;
		}
		public String getTeamName() {
			return teamName;
		}
		public void setTeamName(String teamName) {
			this.teamName = teamName;
		}
		public long getPosted() {
			return posted;
		}
		public void setPosted(long posted) {
			this.posted = posted;
		}
		public boolean isRequested() {
			return isRequested;
		}
		public void setRequested(boolean isRequested) {
			this.isRequested = isRequested;
		}
		public int getReqCount() {
			return reqCount;
		}
		public void setReqCount(int reqCount) {
			this.reqCount = reqCount;
		}
		public int	addRequested() {
			isRequested = true;
			reqCount++;
			return reqCount;
		}
		public int getEmpCount() {
			return empCount;
		}
		public void setEmpCount(int empCount) {
			this.empCount = empCount;
		}
		public int	addEmployee() {
			empCount++;
			return empCount;
		}
		public Set<EligibleEmployeeDto> getEmployees() {
			return employees;
		}
		public void setEmployees(Set<EligibleEmployeeDto> employees) {
			this.employees = employees;
		}
		public void addEmployee(EligibleEmployeeDto empDto) {
			employees.add(empDto);
		}	
	}

	public static class EligibleEmployeeDto implements Serializable {
		private String id;
		private String name;
		private String homeTeamName;
		private String primarySkillName;
		private String primarySkillAbbreviation;
		
		public EligibleEmployeeDto() {
			super();
		}
		public EligibleEmployeeDto(String id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getHomeTeamName() {
			return homeTeamName;
		}
		public void setHomeTeamName(String homeTeamName) {
			this.homeTeamName = homeTeamName;
		}
		public String getPrimarySkillName() {
			return primarySkillName;
		}
		public void setPrimarySkillName(String primarySkillName) {
			this.primarySkillName = primarySkillName;
		}
		public String getPrimarySkillAbbreviation() {
			return primarySkillAbbreviation;
		}
		public void setPrimarySkillAbbreviation(String primarySkillAbbreviation) {
			this.primarySkillAbbreviation = primarySkillAbbreviation;
		}
	}
	
    private String name;
	private	String scheduleId;
	private	String requestId;
	private	String tenantId;

    private long startDate;              // planning window start date

    private long endDate;                // planning window end date

    private int scheduleLengthInDays = 7;    // period in Days covered by schedule (like 1, 2, 3 4 weeks, ..)


    private TaskState state = TaskState.Idle;       // operational state

    private ScheduleCompletion completion;          // completion code (OK, Error, Abort)

    private String completionInfo;					// short user friendly indication about completion

    
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


    private	int shiftCount = 0;
    private Collection<OpenShiftDto> openShifts = new ArrayList<>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
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
	public DateTime getExecutionStartDate() {
		return executionStartDate;
	}
	public void setExecutionStartDate(DateTime executionStartDate) {
		this.executionStartDate = executionStartDate;
	}
	public DateTime getRequestSentDate() {
		return requestSentDate;
	}
	public void setRequestSentDate(DateTime requestSentDate) {
		this.requestSentDate = requestSentDate;
	}
	public DateTime getExecutionAckDate() {
		return executionAckDate;
	}
	public void setExecutionAckDate(DateTime executionAckDate) {
		this.executionAckDate = executionAckDate;
	}
	public DateTime getResponseReceivedDate() {
		return responseReceivedDate;
	}
	public void setResponseReceivedDate(DateTime responseReceivedDate) {
		this.responseReceivedDate = responseReceivedDate;
	}
	public DateTime getExecutionEndDate() {
		return executionEndDate;
	}
	public void setExecutionEndDate(DateTime executionEndDate) {
		this.executionEndDate = executionEndDate;
	}
	public int getShiftCount() {
		return shiftCount;
	}
	public void setShiftCount(int shiftCount) {
		this.shiftCount = shiftCount;
	}
	public Collection<OpenShiftDto> getOpenShifts() {
		return openShifts;
	}
	public void setOpenShifts(Collection<OpenShiftDto> openShifts) {
		this.openShifts = openShifts;
	}
	public void addOpenShifts(OpenShiftDto osDto) {
		openShifts.add(osDto);
	}

}
