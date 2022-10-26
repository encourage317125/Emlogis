package com.emlogis.model.schedule;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes={
		@Index(name="SHIFT_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="SHIFT_SCHEDULESTATUS_INDEX", unique=false, columnList="scheduleStatus") ,
		@Index(name="SHIFT_STARTTIME_INDEX", unique=false, columnList="startDateTime") ,
		@Index(name="SHIFT_ENDTIME_INDEX", unique=false, columnList="endDateTime"),
		@Index(name="SHIFT_EMPID_INDEX", unique=false, columnList="employeeId"),
		@Index(name="SHIFT_ASSIGNMENTTYPE_INDEX", unique=false, columnList="assignmentType")
})
public class Shift extends BaseEntity implements Cloneable {

    private String scheduleId;

    private ScheduleStatus scheduleStatus = ScheduleStatus.Simulation;

    private String teamId;

    private String teamName;

    private String shiftStructureId;

    private String shiftPatternId;

    private boolean locked;

    private int paidTime;

    private String skillId;

    private String skillAbbrev;

    private String skillName;

    private String siteName;

    private int skillProficiencyLevel;

    private String employeeId;

    private String employeeName;

    private AssignmentType assignmentType;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime assigned;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime startDateTime;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime endDateTime;

    private boolean excess;

    private String shiftLengthId;

    private int shiftLength;

    private String shiftLengthName;

//    private String shiftReqId;

    private int employeeIndex;
   
    // attributes to keep information about last manual or workflow change
    private ShiftChangeType chgType;	// WIP/SWAP/ASSIGN, etc

    private String chgEmployeeName;		// employee which Shift changed
    
    private String chgManagerName;		// Manager who did the  Shift change

    private String chgInfo;	// chgInfo, free from string, with for instance employee name of WIP/SWAP peer  ?
    
    private String chgRequestId;		// link back to Workflow request that did the change
    

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime changed;	// date the
    
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime requested;	// date of the request that triggered the change

    private String comment;	// comment, manually set by managers
    

    public Shift() {}

    public Shift(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public ScheduleStatus getScheduleStatus() {
        return scheduleStatus;
    }

    public String getShiftPatternId() {
        return shiftPatternId;
    }

    public void setShiftPatternId(String shiftPatternId) {
        this.shiftPatternId = shiftPatternId;
    }

    public void setScheduleStatus(ScheduleStatus scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getShiftStructureId() {
        return shiftStructureId;
    }

    public void setShiftStructureId(String shiftStructureId) {
        this.shiftStructureId = shiftStructureId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getShiftLengthId() {
        return shiftLengthId;
    }

    public void setShiftLengthId(String shiftLengthId) {
        this.shiftLengthId = shiftLengthId;
    }

    public int getShiftLength() {
        return shiftLength;
    }

    public void setShiftLength(int shiftLength) {
        this.shiftLength = shiftLength;
    }

    public int getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(int paidTime) {
        this.paidTime = paidTime;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public int getSkillProficiencyLevel() {
        return skillProficiencyLevel;
    }

    public void setSkillProficiencyLevel(int skillProficiencyLevel) {
        this.skillProficiencyLevel = skillProficiencyLevel;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * Setter for employeeId field.  
     * Deprecated to discourage direct use by developers, who should instead call makeShfitAssignment 
     * or dropShiftAssignment to ensure all related variables are being set in unison.
     * @param employeeId
     */
    @Deprecated
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    /**
     * Setter for employeeName field.
     * Deprecated to discourage direct use by developers, who should instead call makeShfitAssignment 
     * or dropShiftAssignment to ensure all related variables are being set in unison.
     * @param employeeName
     */
    @Deprecated
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    /**
     * Setter for assignmentType field.
     * Deprecated to discourage direct use by developers, who should instead call makeShfitAssignment 
     * or dropShiftAssignment to ensure all related variables are being set in unison.
     * @param assignmentType
     */
    @Deprecated
    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public DateTime getAssigned() {
        return assigned;
    }

    /**
     * Setter for assigned field.
     * Deprecated to discourage direct use by developers, who should instead call makeShfitAssignment 
     * or dropShiftAssignment to ensure all related variables are being set in unison.
     * @param assigned
     */
    @Deprecated
    public void setAssigned(DateTime assigned) {
        this.assigned = assigned;
    }

    public Long getStartDateTime() {
        return startDateTime == null ? null : startDateTime.getMillis();
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = new DateTime(startDateTime);
    }

    public Long getEndDateTime() {
        return endDateTime == null ? null : endDateTime.getMillis();
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = new DateTime(endDateTime);
    }

    public boolean isExcess() {
        return excess;
    }

    public void setExcess(boolean excess) {
        this.excess = excess;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getShiftLengthName() {
        return shiftLengthName;
    }

    public void setShiftLengthName(String shiftLengthName) {
        this.shiftLengthName = shiftLengthName;
    }

    public int getEmployeeIndex() {
        return employeeIndex;
    }

    public void setEmployeeIndex(int employeeIndex) {
        this.employeeIndex = employeeIndex;
    }

    public String getSkillAbbrev() {
        return skillAbbrev;
    }

    public void setSkillAbbrev(String skillAbbrev) {
        this.skillAbbrev = skillAbbrev;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public ShiftChangeType getChgType() {
		return chgType;
	}

	public void setChgType(ShiftChangeType chgType) {
		this.chgType = chgType;
	}

	public String getChgInfo() {
		return chgInfo;
	}

	public void setChgInfo(String chgInfo) {
		this.chgInfo = chgInfo;
	}

	public String getChgRequestId() {
		return chgRequestId;
	}

	public void setChgRequestId(String chgRequestId) {
		this.chgRequestId = chgRequestId;
	}

	public Long getChanged() {
        return changed == null ? null : changed.getMillis();
	}

	public void setChanged(DateTime changed) {
		this.changed = changed;
	}

    public void setChanged(long changed) {
        this.changed = new DateTime(changed);
    }

    public DateTime getRequested() {
        return requested;
    }

    public void setRequested(DateTime requested) {
        this.requested = requested;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

//    public String getShiftReqId() {
//        return shiftReqId;
//    }
//
//    public void setShiftReqId(String shiftReqId) {
//        this.shiftReqId = shiftReqId;
//    }

    /**
     * Drops the shifts employee assignment by nulling all the appropriate variables in unison.
     * Generally this should be used instead of direct calls to the deprecated setters for
     * the fields employeeId, employeeName, assignmentType, and/or assigned.
     */
    public void dropShiftAssignment() {
    	this.employeeId = null;
    	this.employeeName = null;
    	this.assignmentType = null;
    	this.assigned = null;
    }

    /**
     * Assigns an employee to a shift by setting all the appropriate variables in unison.
     * Generally this should be used instead of direct calls to the deprecated setters for
     * the fields employeeId, employeeName, assignmentType, and/or assigned.
     * @param employeeId
     * @param employeeName
     * @param assignmentType
     */
    public void makeShiftAssignment(String employeeId, String employeeName, AssignmentType assignmentType) {
    	this.employeeId = employeeId;
    	this.employeeName = employeeName;
    	this.assignmentType = assignmentType;
    	this.assigned = new DateTime();
    }

    /**
     * Assigns an employee to a shift by setting all the appropriate variables in unison.
     * Generally this should be used instead of direct calls to the deprecated setters for
     * the fields employeeId, employeeName, assignmentType, and/or assigned.
     * @param employeeId
     * @param employeeName
     * @param assignmentType
     */
    public void makeShiftAssignment(String employeeId, String employeeName, 
    		AssignmentType assignmentType, DateTime assigned) {
    	this.employeeId = employeeId;
    	this.employeeName = employeeName;
    	this.assignmentType = assignmentType;
    	this.assigned = assigned;
    }
    

    public String getChgEmployeeName() {
		return chgEmployeeName;
	}

	public void setChgEmployeeName(String chgEmployeeName) {
		this.chgEmployeeName = chgEmployeeName;
	}

	public String getChgManagerName() {
		return chgManagerName;
	}

	public void setChgManagerName(String chgManagerName) {
		this.chgManagerName = chgManagerName;
	}

	@Override
    public Shift clone() throws CloneNotSupportedException {
        return (Shift) super.clone();
    }

    @Override
    public String toString() {
        return "Shift{" +
                "scheduleId='" + scheduleId + '\'' +
                ", scheduleStatus=" + scheduleStatus +
                ", teamId='" + teamId + '\'' +
                ", teamName='" + teamName + '\'' +
                ", shiftStructureId='" + shiftStructureId + '\'' +
                ", shiftPatternId='" + shiftPatternId + '\'' +
                ", locked=" + locked +
                ", paidTime=" + paidTime +
                ", skillId='" + skillId + '\'' +
                ", skillAbbrev='" + skillAbbrev + '\'' +
                ", skillName='" + skillName + '\'' +
                ", siteName='" + siteName + '\'' +
                ", skillProficiencyLevel=" + skillProficiencyLevel +
                ", employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", assignmentType=" + assignmentType +
                ", assigned=" + assigned +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", excess=" + excess +
                ", shiftLengthId='" + shiftLengthId + '\'' +
                ", shiftLength=" + shiftLength +
                ", shiftLengthName='" + shiftLengthName + '\'' +
                ", employeeIndex=" + employeeIndex +
                '}';
    }
/*    
    public class ShiftChangeInfo {
        private ShiftChangeType chgType;	// WIP/SWAP/ASSIGN, etc
        private String chgRequestId;		// Id of associated  changeRequest (if change done by workflow)
        private String chgManagerName;		// Manager who did the Shift change
        private String chgEmployeeName;		// employee which Shift changed
        private String chgInfo;				// chgInfo, free form string, with for instance employee name of WIP/SWAP peer  ?
        private long   changed;				// date of the change
        
		public ShiftChangeInfo() {
			super();
			// TODO Auto-generated constructor stub
		}
		public String getChgRequestId() {
			return chgRequestId;
		}
		public void setChgRequestId(String chgRequestId) {
			this.chgRequestId = chgRequestId;
		}
		public ShiftChangeType getChgType() {
			return chgType;
		}
		public void setChgType(ShiftChangeType chgType) {
			this.chgType = chgType;
		}
		public String getChgEmployeeName() {
			return chgEmployeeName;
		}
		public void setChgEmployeeName(String chgEmployeeName) {
			this.chgEmployeeName = chgEmployeeName;
		}
		public String getChgManagerName() {
			return chgManagerName;
		}
		public void setChgManagerName(String chgManagerName) {
			this.chgManagerName = chgManagerName;
		}
		public String getChgInfo() {
			return chgInfo;
		}
		public void setChgInfo(String chgInfo) {
			this.chgInfo = chgInfo;
		}
		public long getChanged() {
			return changed;
		}
		public void setChanged(long changed) {
			this.changed = changed;
		}
        
    }
*/
}
