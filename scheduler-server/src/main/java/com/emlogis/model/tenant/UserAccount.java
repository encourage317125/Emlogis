package com.emlogis.model.tenant;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.notifications.NotificationType;
import com.emlogis.common.security.encoding.ShaPasswordEncoder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.NotificationConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "UserAccount")
public class UserAccount extends Account implements PersonalizedEntity {

    public static final String DEFAULT_ADMIN_ID = "admin";
    public static final String DEFAULT_SCHEDULECREATOR_ID = "schedulecreator";
    public static final String DEFAULT_SHIFTMANAGER_ID = "shiftmanager";
    public static final String DEFAULT_SUPPORTACCOUNT_ID = "svcsupport";
    public static final String DEFAULT_SERVICEADMIN_ID = "svcadmin";

    public static final String MIGRATIONACCOUNT_ID = "migration";

    private	String login;

    private	String password;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime pwdChanged = new DateTime(0);    //  date/time UTC password was last changed

    // TODO remove email user work/home email instead
    @Deprecated
    private	String email;

    private	long inactivityPeriod; // in minutes

    private String country;

    private String language = "en";

    private DateTimeZone timeZone = DateTimeZone.UTC;    // tenant default TimeZone.

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column
    private String middleName;
    
    @Column
 	private String gender; 

    @Column
    private String workEmail;

    @Column
    private String workPhone;

    @Column
    private String mobilePhone;
    
    @Column
 	private String homePhone;
    
    @Column
 	private String homeEmail;
    
    @Column
 	private String address;
    
    @Column
 	private String address2;
    
    @Column
 	private String city;
    
    @Column
 	private String state;
       
    @Column
 	private String zip;
    
    @Column
 	private String ecRelationship;
    
    @Column
 	private String ecPhoneNumber;
    
    @Column
 	private String emergencyContact;

    @Column
 	private Integer primaryContactIndicator;

    @OneToOne(mappedBy = "userAccount", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private AccountPicture userPicture;
   
    @Column
    private boolean isNotificationEnabled = true;

    @ElementCollection(fetch = FetchType.LAZY) // let JPA do the mapping table itself instead of using the complicated @CollectionTable
	private Map<NotificationType, Boolean> notificationTypes;

	@OneToMany(mappedBy = "userAccount", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<NotificationConfig> notificationConfigs = new HashSet<>();
    
    //todo:: temporary EAGER because of issues with latest USER/ACCOUNT - EMPLOYEE moves
    @OneToOne(mappedBy = "userAccount",fetch = FetchType.EAGER, orphanRemoval = true)
    private Employee employee;

    private	AccountStatus status = AccountStatus.Active;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastStateCheckedDate = new DateTime(0);

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime accountValidityDate = new DateTime(0);    //  date/time UTC until which account is valid

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "User_Group",
            joinColumns = {@JoinColumn(name = "user_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "group_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "group_id", referencedColumnName = "id")})
    private Set<GroupAccount> groupAccounts = new HashSet<>();

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime passwordValidUntil = new DateTime(0);

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastLogged = new DateTime(0);

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime accountSuspendedUntil = new DateTime(0);

    @Column(length = 2000)
    private String passwordHistory;

    @Column(length = 2000)
    private String unsuccessfulLoginHistory;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userAccount")
    private Set<RememberMe> rememberMeSet;
    
    @Column(length = 256)
    private String preferences;							// json serialized object capturing user preferences

    private String confirmationId;

    public UserAccount() {
    	setupDefaultPreferences();
    }

    public UserAccount(PrimaryKey primaryKey) {
        super(primaryKey);
        setupDefaultPreferences();
    }
    

	private void setupDefaultPreferences() {
		this.setPreferences(getDefaultPreferences());
	}
	
	private Preferences getDefaultPreferences() {
		Preferences prefs = new Preferences();
		// TODO initialize some prefs with default values
		return prefs;
	}
	
	public Preferences getPreferences() {
		if (StringUtils.isBlank(preferences)) {
			return getDefaultPreferences();
		} else {
			return  EmlogisUtils.fromJsonString(preferences, Preferences.class);
		}
	}
	
	public void setPreferences(Preferences preferences) {
		this.preferences = EmlogisUtils.toJsonString(preferences);
	}	    
	
	public void setPreferences(String preferences) {
		if (StringUtils.isNotBlank(preferences)) {
			// deserialize json to make sure it looks valid
			Preferences prefs = EmlogisUtils.fromJsonString(preferences, Preferences.class); 
			this.preferences = preferences;
		}
	}

    public Set<GroupAccount> getGroupAccounts() {
        return groupAccounts;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }
/*
    public void setPassword(String password) {
        this.password = password;
        setPwdChanged(System.currentTimeMillis());
    }
*/
    public void setPassword(String password, boolean isHashed) {
    	if (!isHashed) {
	        ShaPasswordEncoder passwordEncoder=new ShaPasswordEncoder();
	        password =  passwordEncoder.getEncryptedString(password + "." + this.getTenantId());
    	}
        this.password = password;
        setPwdChanged(System.currentTimeMillis());
    }
    
    public long getPwdChanged() {
		return pwdChanged.getMillis();
	}

	public void setPwdChanged(long pwdChanged) {
		this.pwdChanged = new DateTime(pwdChanged);
	}

	public String getEmail() {
        return this.getWorkEmail();
    }

    public void setEmail(String email) {
    }

    public long getInactivityPeriod() {
        return inactivityPeriod;
    }

    public void setInactivityPeriod(long inactivityPeriod) {
        this.inactivityPeriod = inactivityPeriod;
    }

    public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
		if (employee != null) {
			employee.doSetLanguage(language);
		}
	}

	public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public long getAccountValidityDate() {
		return accountValidityDate == null ? 0 : accountValidityDate.getMillis();
	}

	public void setAccountValidityDate(long accountValidityDate) {
		this.accountValidityDate = new DateTime(accountValidityDate);
	}

	public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getPasswordHistory() {
        return passwordHistory;
    }

    public void setPasswordHistory(String passwordHistory) {
        this.passwordHistory = passwordHistory;
    }

    public void setPwdChanged(DateTime pwdChanged) {
        this.pwdChanged = pwdChanged;
    }

    public void setAccountValidityDate(DateTime accountValidityDate) {
        this.accountValidityDate = accountValidityDate;
    }

    public void setGroupAccounts(Set<GroupAccount> groupAccounts) {
        this.groupAccounts = groupAccounts;
    }

    public DateTime getPasswordValidUntil() {
        return passwordValidUntil;
    }

    public void setPasswordValidUntil(DateTime passwordValidUntil) {
        this.passwordValidUntil = passwordValidUntil;
    }

    public DateTime getLastLogged() {
        return lastLogged;
    }

    public void setLastLogged(DateTime lastLogged) {
        this.lastLogged = lastLogged;
    }

    public DateTime getAccountSuspendedUntil() {
        return accountSuspendedUntil;
    }

    public void setAccountSuspendedUntil(DateTime accountSuspendedUntil) {
        this.accountSuspendedUntil = accountSuspendedUntil;
    }

    public String getUnsuccessfulLoginHistory() {
        return unsuccessfulLoginHistory;
    }

    public void setUnsuccessfulLoginHistory(String unsuccessfulLoginHistory) {
        this.unsuccessfulLoginHistory = unsuccessfulLoginHistory;
    }

    public DateTime getLastStateCheckedDate() {
        return lastStateCheckedDate;
    }

    public void setLastStateCheckedDate(DateTime lastStateCheckedDate) {
        this.lastStateCheckedDate = lastStateCheckedDate;
    }

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
		if (employee != null) {
			employee.doSetCountry(country);
		}
	}

	public DateTimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(DateTimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
		updateName();
		if (employee != null) {
			employee.doSetFirstName(firstName);
		}
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
		updateName();
		if (employee != null) {
			employee.doSetLastName(lastName);
		}
	}

	private void updateName() {
		setName(firstName + " " + lastName);
		setDescription(getName() + " Account");
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
		if (employee != null) {
			employee.doSetMiddleName(middleName);
		}
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
		if (employee != null) {
			employee.doSetGender(gender);
		}
	}

	public String getWorkEmail() {
		return workEmail;
	}

	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
		if (employee != null) {
			employee.doSetWorkEmail(workEmail);
		}
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
		if (employee != null) {
			employee.doSetWorkPhone(workPhone);
		}
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
		if (employee != null) {
			employee.doSetMobilePhone(mobilePhone);
		}
	}

	public String getHomePhone() {
		return homePhone;
	}

	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
		if (employee != null) {
			employee.doSetHomePhone(homePhone);
		}
	}

	public String getHomeEmail() {
		return homeEmail;
	}

	public void setHomeEmail(String homeEmail) {
		this.homeEmail = homeEmail;
		if (employee != null) {
			employee.doSetHomeEmail(homeEmail);
		}
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
		if (employee != null) {
			employee.doSetAddress(address);
		}
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
		if (employee != null) {
			employee.doSetAddress2(address2);
		}
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
		if (employee != null) {
			employee.doSetCity(city);
		}
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
		if (employee != null) {
			employee.doSetState(state);
		}
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
		if (employee != null) {
			employee.doSetZip(zip);
		}
	}

	public boolean isNotificationEnabled() {
		return isNotificationEnabled;
	}

	public void setNotificationEnabled(boolean isNotificationEnabled) {
		this.isNotificationEnabled = isNotificationEnabled;
		if (employee != null) {
			employee.doSetIsNotificationEnabled(isNotificationEnabled);
		}
	}

	public Map<NotificationType, Boolean> getNotificationTypes() {
		return notificationTypes;
	}

	public void setNotificationTypes(
			Map<NotificationType, Boolean> notificationTypes) {
		this.notificationTypes = notificationTypes;
	}

	public Set<NotificationConfig> getNotificationConfigs() {
		return notificationConfigs;
	}

	public void setNotificationConfigs(Set<NotificationConfig> notificationConfigs) {
		this.notificationConfigs = notificationConfigs;
	}
	
	public void addNotificationConfig(NotificationConfig notificationConfig) {
		this.notificationConfigs.add(notificationConfig);
		notificationConfig.setUserAccount(this);
	}
	
	public void removeNotificationConfig(NotificationConfig notificationConfig) {
		this.notificationConfigs.remove(notificationConfig);
		notificationConfig.setUserAccount(null);
	}

	public String getEmployeeId() {
		return employee == null ? null : employee.getId();
	}

	public String getEcRelationship() {
		return ecRelationship;
	}

	public void setEcRelationship(String ecRelationship) {
		this.ecRelationship = ecRelationship;
		if (employee != null) {
			employee.doSetEcRelationship(ecRelationship);
		}
	}

	public String getEcPhoneNumber() {
		return ecPhoneNumber;
	}

	public void setEcPhoneNumber(String ecPhoneNumber) {
		this.ecPhoneNumber = ecPhoneNumber;
		if (employee != null) {
			employee.doSetEcPhoneNumber(ecPhoneNumber);
		}
	}

	public String getEmergencyContact() {
		return emergencyContact;
	}

	public void setEmergencyContact(String emergencyContact) {
		this.emergencyContact = emergencyContact;
		if (employee != null) {
			employee.doSetEmergencyContact(emergencyContact);
		}
	}

	public Integer getPrimaryContactIndicator() {
		return primaryContactIndicator;
	}

	public void setPrimaryContactIndicator(Integer primaryContactIndicator) {
		this.primaryContactIndicator = primaryContactIndicator;
		if (employee != null) {
			employee.doSetPrimaryContactIndicator(primaryContactIndicator);
		}
	}

	public AccountPicture getUserPicture() {
		return userPicture;
	}

	public void setUserPicture(AccountPicture userPicture) {
		this.userPicture = userPicture;
	}

	public Set<RememberMe> getRememberMeSet() {
		return rememberMeSet;
	}

	public void setRememberMeSet(Set<RememberMe> rememberMeSet) {
		this.rememberMeSet = rememberMeSet;
	}

	@Override
	public String reportName() {
		return new String(getFirstName() + " " + getLastName());
	}

	public void addRememberMe(RememberMe rmEntity) {
		rememberMeSet.add(rmEntity);
	}

	public void removeRememberMe(RememberMe rmEntity) {
		rememberMeSet.remove(rmEntity);
	}

    public String getConfirmationId() {
        return confirmationId;
    }

    public void setConfirmationId(String confirmationId) {
        this.confirmationId = confirmationId;
    }
}
