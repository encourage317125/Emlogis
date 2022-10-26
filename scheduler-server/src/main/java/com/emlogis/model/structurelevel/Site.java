package com.emlogis.model.structurelevel;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.WeekendDefinition;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.SiteContract;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.shiftpattern.ShiftType;
import com.emlogis.model.tenant.settings.SchedulingSettings;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Site extends StructureLevel {
	
	public final static String AOM_ENTITY_TYPE = "Site";

    private WeekendDefinition weekendDefinition = WeekendDefinition.SATURDAY_SUNDAY;

    private DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

    private String language;		// used to override the Tenant level langage (null means use tenant langage)

    private DateTimeZone timeZone = DateTimeZone.UTC;

    @Transient
	private int timezoneOffset;    // using for mapping to dto

    // bidirectional
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable
	private Set<Skill> skills = new HashSet<>();

    //bi-directional one-to-many association to contract
    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SiteContract> siteContracts = new HashSet<>();
    
    // bidirectional one to many association to absence type
    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AbsenceType> absenceTypes = new HashSet<>();

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ShiftDropReason> shiftDropReasons = new HashSet<>();

    // bidirectional many to many association to employees (actually used as one to many for now)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Site_Employee",
    		joinColumns = {@JoinColumn(name = "site_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "site_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "employee_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "employee_id", referencedColumnName = "id")})
    private Set<Employee> employees = new HashSet<>();

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
    private Set<ShiftType> shiftTypes;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
    private Set<ShiftLength> shiftLengths;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<PostOverrides> postOverrides;

    private boolean isDeleted = false;
    
    private boolean isNotificationEnabled = true;
    
    // scheduling settings overriding the settings at org level. this attribute can be null, indicating that org level settings must be used.
    @JsonIgnore
    @OneToOne(targetEntity = SchedulingSettings.class, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumns( {
		@JoinColumn(name = "siteschedulingsettings_tenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "siteschedulingsettings_id", referencedColumnName = "id")
    })
    private SchedulingSettings schedulingSettings;

 	private String abbreviation; 	

 	private String address;

 	private String address2;

 	private String city; 

 	private String state;

 	private String country;

 	private String zip;
    
    // Site Scheduling configuration options
 	private int shiftIncrements = 30;
 	private int shiftOverlaps;
 	private int maxConsecutiveShifts;
 	private int TimeOffBetweenShifts;
 	private boolean enableWIPFragments;
 
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime twoWeeksOvertimeStartDate = new DateTime(0);     
    
	
    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
	protected Site() {}

	public Site(PrimaryKey primaryKey) {
		super(primaryKey);
		setAomEntityType(AOM_ENTITY_TYPE);		// optional as by default AOM entity type = this. simple class name 
	}

    public WeekendDefinition getWeekendDefinition() {
        return weekendDefinition;
    }

    public void setWeekendDefinition(WeekendDefinition weekendDefinition) {
        this.weekendDefinition = weekendDefinition;
    }


    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

	public int getTimeZoneOffset() {
		return timeZone.toTimeZone().getRawOffset() + timeZone.toTimeZone().getDSTSavings();
	}

    public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Setter for skills field
	 * @param skills
	 * 
	 * Protected to satisfy JPA but otherwise discourage use by developers, who
	 * should only add/remove skills through the add/remove methods that will
	 * correctly manage the bidirectional many-to-many relationship.
	 */
	protected void setSkills(Set<Skill> skills) {
		this.skills = skills;
	}

	/**
	 * Getter for skills field
	 * @return
	 * 
	 * It is recommended that developers use this for read-only activities, as 
	 * adding/removing skills is best handled through the add/remove methods
	 * that will correctly manage the bidirectional many-to-many relationship.
	 */
	public Set<Skill> getSkills() {
		return skills;
	}

	/**
	 * Add skill to this site
	 * @param skill
	 */
	public void addSkill(Skill skill) {
		if (!skills.contains(skill)){
			skills.add(skill);
		}

		// Bidirectional many-to-many relationship, so handle the inverse as well...
		if (!skill.getSites().contains(this)){
			skill.getSites().add(this);
		}
	}

	/**
	 * Remove skill from this site
	 * @param skill
	 */
	public void removeSkill(Skill skill) {
		if (skills.contains(skill)){
			skills.remove(skill);
		}
		
		// Bidirectional many-to-many relationship, so handle the inverse as well...
		if (skill.getSites().contains(this)){
			skill.getSites().remove(this);
		}
	}

	public Set<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

    public Set<ShiftType> getShiftTypes() {
        return shiftTypes;
    }

    public void setShiftTypes(Set<ShiftType> shiftTypes) {
        this.shiftTypes = shiftTypes;
    }

    public Set<PostOverrides> getPostOverrides() {
		return postOverrides;
	}

	public void setPostOverrides(Set<PostOverrides> postOverrides) {
		this.postOverrides = postOverrides;
	}

	/**
	 * Add employee to this site
	 * @param employee
	 */
	public void addEmployee(Employee employee) {
		if (!employees.contains(employee)) {
			employees.add(employee);
		}
	}

	/**
	 * Remove employee from this site
	 * @param employee
	 */
	public void removeEmployee(Employee employee) {
		if (employees.contains(employee)){
			employees.remove(employee);
		}
	}

	public Set<SiteContract> getSiteContracts() {
		return siteContracts;
	}

	public void setSiteContracts(Set<SiteContract> siteContracts) {
		this.siteContracts = siteContracts;
	}

	public Set<AbsenceType> getAbsenceTypes() {
		return absenceTypes;
	}

	public void setAbsenceTypes(Set<AbsenceType> absenceTypes) {
		this.absenceTypes = absenceTypes;
	}

	
	/**
	 * @return the isDeleted
	 */
	public boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @param isNotificationEnabled the isDeleted to set
	 */
	public void setIsNotificationEnabled(boolean isNotificationEnabled) {
		this.isNotificationEnabled = isNotificationEnabled;
	}
	
	/**
	 * @return the isDeleted
	 */
	public boolean getIsNotificationEnabled() {
		return isNotificationEnabled;
	}

	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void addAbsenceType(AbsenceType absenceType) {
		if (!absenceTypes.contains(absenceType)) {
			absenceTypes.add(absenceType);
		}
	}
	
	public void removeAbsenceType(AbsenceType absenceType) {
		if (absenceTypes.contains(absenceType)) {
			absenceTypes.remove(absenceType);
		}
	}

	public SchedulingSettings getSchedulingSettings() {
		return schedulingSettings;
	}

	public void setSchedulingSettings(SchedulingSettings schedulingSettings) {
		this.schedulingSettings = schedulingSettings;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public int getShiftIncrements() {
		return shiftIncrements;
	}

	public void setShiftIncrements(int shiftIncrements) {
		this.shiftIncrements = shiftIncrements;
	}

	public int getShiftOverlaps() {
		return shiftOverlaps;
	}

	public void setShiftOverlaps(int shiftOverlaps) {
		this.shiftOverlaps = shiftOverlaps;
	}

	public int getMaxConsecutiveShifts() {
		return maxConsecutiveShifts;
	}

	public void setMaxConsecutiveShifts(int maxConsecutiveShifts) {
		this.maxConsecutiveShifts = maxConsecutiveShifts;
	}

	public int getTimeOffBetweenShifts() {
		return TimeOffBetweenShifts;
	}

	public void setTimeOffBetweenShifts(int timeOffBetweenShifts) {
		TimeOffBetweenShifts = timeOffBetweenShifts;
	}

	public boolean isEnableWIPFragments() {
		return enableWIPFragments;
	}

	public void setEnableWIPFragments(boolean enableWIPFragments) {
		this.enableWIPFragments = enableWIPFragments;
	}

	public long getTwoWeeksOvertimeStartDate() {
		return twoWeeksOvertimeStartDate  == null ? 0 : twoWeeksOvertimeStartDate.toDate().getTime();
	}
	
	public void setTwoWeeksOvertimeStartDate(long twoWeeksOvertimeStartDate) {
		this.twoWeeksOvertimeStartDate = new DateTime(twoWeeksOvertimeStartDate);
	}

	public void setNotificationEnabled(boolean isNotificationEnabled) {
		this.isNotificationEnabled = isNotificationEnabled;
	}

    public Set<ShiftLength> getShiftLengths() {
        return shiftLengths;
    }

    public void setShiftLengths(Set<ShiftLength> shiftLengths) {
        this.shiftLengths = shiftLengths;
    }

    public Set<ShiftDropReason> getShiftDropReasons() {
        return shiftDropReasons;
    }

    public void setShiftDropReasons(Set<ShiftDropReason> shiftDropReasons) {
        this.shiftDropReasons = shiftDropReasons;
    }
}
