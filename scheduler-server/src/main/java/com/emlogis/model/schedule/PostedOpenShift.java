package com.emlogis.model.schedule;

import java.util.HashMap;
import java.util.Map;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.core.type.TypeReference;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes={
//		@Index(name="OPENSHIFT_INDEX", /*unique=true,*/ columnList="employeeIdentifier"),  // unique=true prevents importing customer into several  
//		@Index(name="OS_SHIFTID_INDEX", unique=false, columnList="shiftId") ,
		@Index(name="OS_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="OS_STARTTIME_INDEX", unique=false, columnList="startDateTime") ,
		@Index(name="OS_ENDTIME_INDEX", unique=false, columnList="endDateTime"),
		@Index(name="OS_EMPID_INDEX", unique=false, columnList="employeeId"),
		@Index(name="OS_EMPIDSTARTTIME_INDEX", unique=false, columnList="employeeId,startDateTime"), 
		@Index(name="OS_EMPIDENDTIME_INDEX", unique=false, columnList="employeeId,endDateTime"),		
})
public class PostedOpenShift extends BaseEntity implements Cloneable {

	private String shiftId;
    private String scheduleId;
    private String scheduleName;

    private String siteName;
//    private String shiftPatternId;

    private String skillId;
    private String skillAbbrev;
    private String skillName;

    private String employeeId;
    private String employeeName;

    private String teamId;
    private String teamName;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime postId;		// unique post group Id, which also indicates the date of the post
    
    private boolean excess;
    private int shiftLength;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime startDateTime;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime endDateTime;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime deadline;		// date the Post will expire
    
    private String comments;
    
    private String terms;		// should be 'AutoApprove' or empty or null

    private boolean isRequested = false;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime requestedOn = null;		

    @Column(length = 1024)
    private String overrideOptions;
    
    public PostedOpenShift() {}

    public PostedOpenShift(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getShiftId() {
		return shiftId;
	}

	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}

	public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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
	
	public long getDatePosted() {
		return getPostId();
	}

	public long getPostId() {
        return postId == null ? 0 : postId.getMillis();
    }

    public void setPostId(DateTime postId) {
        this.postId = postId;
    }

    public void setPostId(long postId) {
        this.postId = new DateTime(postId);
    }
    
    public long getStartDateTime() {
        return startDateTime == null ? 0 : startDateTime.getMillis();
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = new DateTime(startDateTime);
    }

    public long getEndDateTime() {
        return endDateTime == null ? 0 : endDateTime.getMillis();
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

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
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

    public Long getDeadline() {
		return deadline == null ? getStartDateTime() : deadline.getMillis();
	}

	public void setDeadline(long deadline) {
		this.deadline = new DateTime(deadline);
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public boolean isRequested() {
		return isRequested;
	}

	public void setRequested(boolean isRequested) {
		this.isRequested = isRequested;
	}

	public long getRequestedOn() {
		return requestedOn == null ? 0 : requestedOn.getMillis();
	}

	public void setRequestedOn(DateTime requestedOn) {
		this.requestedOn = requestedOn;
	}
	
    public void setRequestedOn(long requestedOn) {
        this.requestedOn = new DateTime(requestedOn);
    }

    @SuppressWarnings("unchecked")
	public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
		if (overrideOptions == null) {
			return null;
        } else {
            return EmlogisUtils.fromJsonString(overrideOptions,
                    new TypeReference<Map<ConstraintOverrideType, Boolean>>() {});
		}
	}

	public void setOverrideOptions(Map<ConstraintOverrideType, Boolean> overrideOptions) {
		this.overrideOptions = EmlogisUtils.toJsonString(overrideOptions);
	}

	/**
	 * getOverrides() returns null if no override option is present, 
	 * or a ready to be transferred to engine, Map of <ConstraintOverrideType, OverrideOption>
	 * @return
	 */
	public Map<ConstraintOverrideType, OverrideOption> getOverrides() {
		if (overrideOptions == null) {
			return null;
		} else {
			// convert overrideOptions into a map of Map<ConstraintOverrideType, OverrideOption> that can be consumed by engine
			Map<ConstraintOverrideType, Boolean> posOverrideOptions = getOverrideOptions();
	        Map<ConstraintOverrideType, OverrideOption> map = new HashMap<>();
	        for (ConstraintOverrideType ot : ConstraintOverrideType.values()) {
	        	OverrideOption opt = new OverrideOption();
	        	opt.setScope(posOverrideOptions.get(ot) ? OverrideOptionScope.All : OverrideOptionScope.None);
	    		map.put(ot, opt);
	        }	
	    	return map; 	
		}	
	}	
	
	@Override
    public Shift clone() throws CloneNotSupportedException {
        return (Shift) super.clone();
    }

}
