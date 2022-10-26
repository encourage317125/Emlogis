package com.emlogis.model.employee;

import com.emlogis.common.notifications.NotificationType;
import com.emlogis.common.security.encoding.ShaPasswordEncoder;
import com.emlogis.model.AOMEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.EmployeeContract;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.AccountStatus;
import com.emlogis.model.tenant.AccountPicture;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.tenant.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(indexes = {
        @Index(name = "EMPIDENTIFIER_INDEX", /*unique=true,*/ columnList = "employeeIdentifier"),  // unique=true prevents importing customer into several
        @Index(name = "TENANTEMPIDENTIFIER_INDEX", unique = true, columnList = "tenantId, employeeIdentifier")
})
public class Employee extends AOMEntity implements Serializable, PersonalizedEntity {

	@Deprecated // will move to UserAccount
    @Column
    private String language;        // used to override the Tenant & Site level langage (null means use Site or Tenant langage)

	@Deprecated // will move to UserAccount
    @Column(nullable = false)
    private String firstName = "default value";

	@Deprecated // will move to UserAccount
    @Column(nullable = false)
    private String lastName = "default value";

    @Column(nullable = false)
    private String employeeIdentifier;

    @Column
    private String calendarSyncId;

    // TODO - Since these are null-able and not required for construction, could/should any be AOM attributes?
	@Deprecated // will move to UserAccount
    @Column
    private String middleName;

	@Deprecated // will move to UserAccount
    @Column
    private String workEmail;

	@Deprecated // will move to UserAccount
    @Column
    private String workPhone;

	@Deprecated // will move to UserAccount
    @Column
    private String mobilePhone;

    // NOTE: isEngineSchedulable/isManuallySchedulable are deprecated in favor of activityType
    // NOTE: The following two attributes, engineSchedulable and manuallySchedulable, provide
    // what is hopefully a clearer way than in Hickory/Aspen to represent an employee's status...
    //   Status 'Inactive' effectively means  engineSchedulable == false  &&  manuallySchedulable == false
    //   Status 'Active'   effectively means  engineSchedulable == true   &&  manuallySchedulable == true
    //   Status 'Pooled'   effectively means  engineSchedulable == false  &&  manuallySchedulable == true
    
    @Column
    private EmployeeActivityType activityType = EmployeeActivityType.Active;

	//todo:: temporary EAGER because of issues with latest USER/ACCOUNT - EMPLOYEE moves
    @OneToOne(fetch = FetchType.EAGER)
	@JoinColumns({
			@JoinColumn(name = "userAccountId", referencedColumnName = "id"),
			@JoinColumn(name = "userAccountTenantId", referencedColumnName = "tenantId") })
	private UserAccount userAccount;
    
    @OneToMany(mappedBy = "employee",fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EmployeeContract> employeeContracts = new HashSet<>();
    
    @OneToMany(mappedBy = "employee",cascade = {CascadeType.ALL},fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<EmployeeTeam> employeeTeams = new HashSet<>();
	    
    @OneToMany(mappedBy = "employee",cascade = {CascadeType.ALL},fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<EmployeeSkill> employeeSkills = new HashSet<>();

    @OneToMany(mappedBy = "employee",cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<AvailabilityTimeFrame> availabilityTimeFrames = new HashSet<>();

    @OneToMany(mappedBy = "employee",cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<AvailabilityTimeFrame> preferedAvailabilityTimeFrames = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Site_Employee",
            joinColumns = {	@JoinColumn(name = "employee_tenantId", referencedColumnName = "tenantId"),
                    		@JoinColumn(name = "employee_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "site_tenantId", referencedColumnName = "tenantId"),
                    			  @JoinColumn(name = "site_id", referencedColumnName = "id")})
    private Set<Site> sites = new HashSet<>();

    @OneToMany(mappedBy = "employee",cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<EmployeeProcessAutoApproval> employeeProcessAutoApprovals = new HashSet<>();

    @Column
    private boolean isDeleted = false;

	@Deprecated // will move to UserAccount
    @Column
 	private String address;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String address2;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String city;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String state;
       
	@Deprecated // will move to UserAccount
    @Column
 	private String country;

	@Deprecated // will move to UserAccount
    @Column
 	private String zip;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String ecRelationship;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String ecPhoneNumber;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String emergencyContact;

	@Deprecated // will move to UserAccount
    @Column
 	private int primaryContactIndicator;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String gender; 
    
	@Deprecated // will move to UserAccount
    @Column
 	private String homePhone;
    
	@Deprecated // will move to UserAccount
    @Column
 	private String homeEmail;
        
    @Column
 	private String professionalLabel;
    
    @Column
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
 	private LocalDate hireDate;
    
    @Column
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
 	private LocalDate startDate;
    
    @Column
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
 	private LocalDate endDate;
    
    @Column
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
 	private DateTime inactiveDate;

    @Column
 	private float hourlyRate;
    
    @Column
    private EmployeeType employeeType = EmployeeType.FullTime;

	@Deprecated // will move to UserAccount
    @Column
    private boolean isNotificationEnabled = true;

     /**
     * Default no-arg constructor
     * NOTE: Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected Employee() {
		super();
		employeeIdentifier = "default value";
	}

    /**
     * Required fields constructor.
     * @param primaryKey
     * @param employeeIdentifier
     */
    public Employee(PrimaryKey primaryKey, String employeeIdentifier) {
		super(primaryKey);
		this.employeeIdentifier = employeeIdentifier;
	}

    public String getLanguage() {
		return userAccount.getLanguage();
	}

	public void setLanguage(String language) {
		doSetLanguage(language);
		userAccount.setLanguage(language);
	}

	// temporary call
	public void doSetLanguage(String language) {
		this.language = language;
	}

	/**
     * Getter for firstName
     * @return
     */
	public String getFirstName() {
		return userAccount.getFirstName();
	}

	/**
	 * Setter for firstName
	 * @param firstName
	 */
	public void setFirstName(String firstName) {
		doSetFirstName(firstName);
		userAccount.setFirstName(firstName);
	}

	// temporary call
	public void doSetFirstName(String firstName) { 
		this.firstName = firstName;
	}
	
	/**
	 * Getter for middleName
	 * @return
	 */
	public String getMiddleName() {
		return userAccount.getMiddleName();
	}

	/**
	 * Setter for middleName
	 * @param middleName
	 */
	public void setMiddleName(String middleName) {
		doSetMiddleName(middleName);
		userAccount.setMiddleName(middleName);
	}
	// temporary call
	public void doSetMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * Getter for lastName
	 * @return
	 */
	public String getLastName() {
		return userAccount.getLastName();
	}

	/**
	 * Setter for lastName
	 * @param lastName
	 */
	public void setLastName(String lastName) {
		doSetLastName(lastName);
		userAccount.setLastName(lastName);
	}
	// temporary call
	public void doSetLastName(String lastName) {
		this.lastName = lastName;
	}


	/**
	 * Getter for employeeIdentifier
	 * @return
	 */
	public String getEmployeeIdentifier() {
		return employeeIdentifier;
	}

	/**
	 * Setter for employeeIdentifier
	 * @param employeeIdentifier
	 */
	public void setEmployeeIdentifier(String employeeIdentifier) {
		this.employeeIdentifier = employeeIdentifier;
	}

	/**
	 * Getter for workEmail
	 * @return
	 */
	public String getWorkEmail() {
		return userAccount.getWorkEmail();
	}

	/**
	 * Setter for workEmail
	 * @param workEmail
	 */
	public void setWorkEmail(String workEmail) {
		doSetWorkEmail(workEmail);
		userAccount.setWorkEmail(workEmail);
	}
	// temporary call
	public void doSetWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}


	/**
	 * @return the workPhone
	 */
	public String getWorkPhone() {
		return userAccount.getWorkPhone();
	}

	/**
	 * @param workPhone the workPhone to set
	 */
	public void setWorkPhone(String workPhone) {
		doSetWorkPhone(workPhone);
		userAccount.setWorkPhone(workPhone);
	}

	// temporary call
	public void doSetWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	/**
	 * Getter for mobilePhone
	 * @return
	 */
	public String getMobilePhone() {
		return userAccount.getMobilePhone();
	}

	/**
	 * Setter for mobilePhone
	 * @param notificationSmsNumber
	 */
	public void setMobilePhone(String notificationSmsNumber) {
		doSetMobilePhone(notificationSmsNumber);
		userAccount.setMobilePhone(notificationSmsNumber);
	}
	// temporary call
	public void doSetMobilePhone(String notificationSmsNumber) {
		this.mobilePhone = notificationSmsNumber;
	}

	/**
	 * Getter for userAccount
	 * @return the userAccount
	 */
	public UserAccount getUserAccount() {
		return userAccount;
	}

	/**
	 * Setter for userAccount
	 * @param userAccount the userAccount to set
	 */
	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	/**
	 * Getter for employeeTeams
	 * @return
	 */
	public Set<EmployeeTeam> getEmployeeTeams() {
		return employeeTeams;
	}

	/**
	 * Setter for employeeTeams
	 * @param employeeTeams
	 * NOTE: Protected to satisfy JPA but discourage develpoer use
	 */
	protected void setEmployeeTeams(Set<EmployeeTeam> employeeTeams) {
		this.employeeTeams = employeeTeams;
	}

	/**
	 * Add EmployeeTeam to this Employee
	 * @param employeeTeam
	 */
	public void addEmployeeTeam(EmployeeTeam employeeTeam) {
		if (!employeeTeams.contains(employeeTeam)) {
			this.employeeTeams.add(employeeTeam);
			employeeTeam.setEmployee(this);
		}
	}
	
	/**
	 * Remove EmployeeTeam from this Employee
	 * @param employeeTeam
	 */
	public void removeEmployeeTeam(EmployeeTeam employeeTeam) {
		if (employeeTeams.contains(employeeTeam)) {
			employeeTeams.remove(employeeTeam);
		}
	}

	public Set<Site> getSites() {
		return sites;
	}

	public void setSites(Set<Site> sites) {
		this.sites = sites;
	}

	public Site getSite() {
		return (getSites().size() == 1 ? sites.iterator().next() : null);
	}

	public void removeSite(Site site) {
		sites.remove(site);
	}

	public void setSite(Site site) {		// Should be addSite() but  we are actually using this relationship as a many to one relationship
		if (site == null) {
			// clear sites
			sites = new HashSet<>();
			return;
		}
		if (getSites().size() > 0) {
			if (sites.contains(site)) {
				return;		// no modification if emp already associated to Site
			}
		}
		sites.add(site);
	}

	/**
	 * Getter for employeeSkills
	 * @return
	 */
	public Set<EmployeeSkill> getEmployeeSkills() {
		return employeeSkills;
	}

	/**
	 * Setter for employeeSkills
	 * @param employeeSkills
	 * NOTE: Protected to satisfy JPA but discourage developer use
	 */
	protected void setEmployeeSkills(Set<EmployeeSkill> employeeSkills) {
		this.employeeSkills = employeeSkills;
	}

	/**
	 * Add EmployeeSkill to this Employee
	 * @param employeeSkill
	 */
	public void addEmployeeSkill(EmployeeSkill employeeSkill) {
		if (!employeeSkills.contains(employeeSkill)) {
			this.employeeSkills.add(employeeSkill);
			employeeSkill.setEmployee(this);
		}
	}

	/**
	 * Remove EmployeeSkill from this Employee
	 * @param employeeSkill
	 */
	public void removeEmployeeSkill(EmployeeSkill employeeSkill) {
		if (employeeSkills.contains(employeeSkill)) {
			employeeSkills.remove(employeeSkill);
		}
	}

    /**
	 * @return the availabilityTimeFrames
	 */
	public Set<AvailabilityTimeFrame> getAvailabilityTimeFrames() {
		return availabilityTimeFrames;
	}

	/**
	 * @param availabilityTimeFrames the availabilityTimeFrames to set
	 */
	public void setAvailabilityTimeFrames(Set<AvailabilityTimeFrame> availabilityTimeFrames) {
		this.availabilityTimeFrames = availabilityTimeFrames;
	}

	/**
	 * Add AvailabilityTimeFrame to this Employee
	 * @param availabilityTimeFrame
	 */
	public void addAvailabilityTimeFrame(AvailabilityTimeFrame availabilityTimeFrame) {
		if (!availabilityTimeFrames.contains(availabilityTimeFrame)) {
			this.availabilityTimeFrames.add(availabilityTimeFrame);
			availabilityTimeFrame.setEmployee(this);
		}
	}

	/**
	 * Remove AvailabilityTimeFrame from this Employee
	 * @param availabilityTimeFrame
	 */
	public void removeAvailabilityTimeFrame(AvailabilityTimeFrame availabilityTimeFrame) {
		if (availabilityTimeFrames.contains(availabilityTimeFrame)) {
			availabilityTimeFrames.remove(availabilityTimeFrame);
		}
	}

    public boolean getIsNotificationEnabled() {
		return userAccount.isNotificationEnabled();
	}

	public EmployeeActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(EmployeeActivityType activityType) {
		this.activityType = activityType;
		if (activityType == EmployeeActivityType.Inactive) {
            inactiveDate = new DateTime();
		} else {
            inactiveDate = null;
        }
	}

	public void setIsNotificationEnabled(boolean isNotificationEnabled) {
		doSetIsNotificationEnabled(isNotificationEnabled);
		userAccount.setNotificationEnabled(isNotificationEnabled);
	}
	// temporary call
	public void doSetIsNotificationEnabled(boolean isNotificationEnabled) {
		this.isNotificationEnabled = isNotificationEnabled;
	}

	public Set<EmployeeContract> getEmployeeContracts() {
		return employeeContracts;
	}

	public void setEmployeeContracts(Set<EmployeeContract> employeeContracts) {
		this.employeeContracts = employeeContracts;
	}

	/**
	 * @return the isDeleted
	 */
	public boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
        if (isDeleted) {
            userAccount.setStatus(AccountStatus.Revoked);
        }
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return userAccount.getAddress();
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		doSetAddress(address);
		userAccount.setAddress(address);
	}
	// temporary call
	public void doSetAddress(String address) {
		this.address = address;
	}


	/**
	 * @return the address2
	 */
	public String getAddress2() {
		return userAccount.getAddress2();
	}

	/**
	 * @param address2 the address2 to set
	 */
	public void setAddress2(String address2) {
		doSetAddress2(address2);
		userAccount.setAddress2(address2);
	}
	// temporary call
	public void doSetAddress2(String address2) {
		this.address2 = address2;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return userAccount.getCity();
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		doSetCity(city);
		userAccount.setCity(city);
	}
	// temporary call
	public void doSetCity(String city) {
		this.city = city;
	}	

	/**
	 * @return the state
	 */
	public String getState() {
		return userAccount.getState();
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		doSetState(state);
		userAccount.setState(state);
	}
	// temporary call
	public void doSetState(String state) {
		this.state = state;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return userAccount.getCountry();
	}

	public void setCountry(String country) {
		doSetCountry(country);
		userAccount.setCountry(country);
	}
	// temporary call
	public void doSetCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the zip
	 */
	public String getZip() {
		return userAccount.getZip();
	}


	/**
	 * @param zip the zip to set
	 */
	public void setZip(String zip) {
		doSetZip(zip);
		userAccount.setZip(zip);
	}
	// temporary call
	public void doSetZip(String zip) {
		this.zip = zip;
	}

	/**
	 * @return the ecRelationship
	 */
	public String getEcRelationship() {
		return userAccount.getEcRelationship();
	}

	/**
	 * @param ecRelationship the ecRelationship to set
	 */
	public void setEcRelationship(String ecRelationship) {
		doSetEcRelationship(ecRelationship);
		userAccount.setEcRelationship(ecRelationship);
	}
	public void doSetEcRelationship(String ecRelationship) {
		this.ecRelationship = ecRelationship;
	}

	/**
	 * @return the ecPhoneNumber
	 */
	public String getEcPhoneNumber() {
		return userAccount.getEcPhoneNumber();
	}

	/**
	 * @param ecPhoneNumber the ecPhoneNumber to set
	 */
	public void setEcPhoneNumber(String ecPhoneNumber) {
		doSetEcPhoneNumber(ecPhoneNumber);
		userAccount.setEcPhoneNumber(ecPhoneNumber);
	}
	public void doSetEcPhoneNumber(String ecPhoneNumber) {
		this.ecPhoneNumber = ecPhoneNumber;
	}

	/**
	 * @return the emergencyContact
	 */
	public String getEmergencyContact() {
		return userAccount.getEmergencyContact();
	}

	/**
	 * @param emergencyContact the emergencyContact to set
	 */
	public void setEmergencyContact(String emergencyContact) {
		doSetEmergencyContact(emergencyContact);
		userAccount.setEmergencyContact(emergencyContact);
	}
	public void doSetEmergencyContact(String emergencyContact) {
		this.emergencyContact = emergencyContact;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return userAccount.getGender();
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		doSetGender(gender);
		userAccount.setGender(gender);
	}
	// temporary call
	public void doSetGender(String gender) {
		this.gender = gender;
	}


	/**
	 * @return the homePhone
	 */
	public String getHomePhone() {
		return userAccount.getHomePhone();
	}

	/**
	 * @param homePhone the homePhone to set
	 */
	public void setHomePhone(String homePhone) {
		doSetHomePhone(homePhone);
		userAccount.setHomePhone(homePhone);
	}
	// temporary call
	public void doSetHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}

	/**
	 * @return the homeEmail
	 */
	public String getHomeEmail() {
		return userAccount.getHomeEmail();
	}

	/**
	 * @param homeEmail the homeEmail to set
	 */
	public void setHomeEmail(String homeEmail) {
		doSetHomeEmail(homeEmail);
		userAccount.setHomeEmail(homeEmail);
	}
	// temporary call
	public void doSetHomeEmail(String homeEmail) {
		this.homeEmail = homeEmail;
	}

	/**
	 * @return the primaryContactIndicator
	 */
	public int getPrimaryContactIndicator() {
		// ugly workaround here, as we can get null from userAccount.getPrimaryContactIndicator(), which
		// cannot be casted to the primitive type int.
		Integer val  = userAccount.getPrimaryContactIndicator();
		return val != null ? val : 0;
	}

	/**
	 * @param primaryContactIndicator the primaryContactIndicator to set
	 */
	public void setPrimaryContactIndicator(int primaryContactIndicator) {
		doSetPrimaryContactIndicator(primaryContactIndicator);
		userAccount.setPrimaryContactIndicator(primaryContactIndicator);
	}
	public void doSetPrimaryContactIndicator(int primaryContactIndicator) {
		this.primaryContactIndicator = primaryContactIndicator;
	}

	/**
	 * @return the professionalLabel
	 */
	public String getProfessionalLabel() {
		return professionalLabel;
	}

	/**
	 * @param professionalLabel the professionalLabel to set
	 */
	public void setProfessionalLabel(String professionalLabel) {
		this.professionalLabel = professionalLabel;
	}

	/**
	 * @return the hireDate
	 */
	public LocalDate getHireDate() {
		return hireDate;
	}

	/**
	 * @param hireDate the hireDate to set
	 */
	public void setHireDate(LocalDate hireDate) {
		this.hireDate = hireDate;
	}

	/**
	 * @return the startDate
	 */
	public LocalDate getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public LocalDate getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the hourlyRate
	 */
	public float getHourlyRate() {
		return hourlyRate;
	}

	/**
	 * @param hourlyRate the hourlyRate to set
	 */
	public void setHourlyRate(float hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	/**
	 * @return the employeeType
	 */
	public EmployeeType getEmployeeType() {
		return employeeType;
	}

	/**
	 * @param employeeType the employeeType to set
	 */
	public void setEmployeeType(EmployeeType employeeType) {
		this.employeeType = employeeType;
	}

	public Team getHomeTeam() {
        if (getEmployeeTeams() != null) {
            for (EmployeeTeam employeeTeam : getEmployeeTeams()) {
                if (employeeTeam.getIsHomeTeam()) {
                    return employeeTeam.getTeam();
                }
            }
        }
        return null;
    }

    public Skill getPrimarySkill() {
        if (getEmployeeSkills() != null) {
            for (EmployeeSkill employeeSkill : getEmployeeSkills()) {
                if (employeeSkill.getIsPrimarySkill()) {
                    return employeeSkill.getSkill();
                }
            }
        }
        return null;
    }

	public Map<NotificationType, Boolean> getNotificationTypes() {
		return userAccount.getNotificationTypes();
	}

	public void setNotificationTypes(Map<NotificationType, Boolean> notificationTypes) {
		userAccount.setNotificationTypes(notificationTypes);
	}

	public Set<NotificationConfig> getNotificationConfigs() {
		return userAccount.getNotificationConfigs();
	}

	public void setNotificationConfigs(Set<NotificationConfig> notificationConfigs) {
		userAccount.setNotificationConfigs(notificationConfigs);
	}
	
	public void addNotificationConfig(NotificationConfig notificationConfig) {
		notificationConfig.setEmployee(this);
		userAccount.addNotificationConfig(notificationConfig);
	}
	
	public void removeNotificationConfig(NotificationConfig notificationConfig) {
		notificationConfig.setEmployee(null);
		userAccount.removeNotificationConfig(notificationConfig);
	}

    public Set<AvailabilityTimeFrame> getPreferedAvailabilityTimeFrames() {
        return preferedAvailabilityTimeFrames;
    }

    public void setPreferedAvailabilityTimeFrames(Set<AvailabilityTimeFrame> preferedAvailabilityTimeFrames) {
        this.preferedAvailabilityTimeFrames = preferedAvailabilityTimeFrames;
    }

    public Set<EmployeeProcessAutoApproval> getEmployeeProcessAutoApprovals() {
        return employeeProcessAutoApprovals;
    }

    public void setEmployeeProcessAutoApprovals(Set<EmployeeProcessAutoApproval> employeeProcessAutoApprovals) {
        this.employeeProcessAutoApprovals = employeeProcessAutoApprovals;
    }

    public AccountPicture getEmployeePicture() {
        return userAccount.getUserPicture();
    }

    public void setEmployeePicture(AccountPicture employeePicture) {
    	userAccount.setUserPicture(employeePicture);
    }

    public String getCalendarSyncId() {
        if (StringUtils.isEmpty(calendarSyncId)) {
            createCalendarSyncId();
        }
        return calendarSyncId;
    }

    public void setCalendarSyncId(String calendarSyncId) {
        this.calendarSyncId = calendarSyncId;
    }

    public DateTime getInactiveDate() {
        return inactiveDate;
    }

    public void setInactiveDate(DateTime inactiveDate) {
        this.inactiveDate = inactiveDate;
    }

    public String reportName() {
        return new String(userAccount.getFirstName() + " " + userAccount.getLastName());
    }

    @PrePersist
    private void createCalendarSyncId() {
        ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();
        calendarSyncId = passwordEncoder.getEncryptedString(getTenantId() + "." + getId());
    }

}
