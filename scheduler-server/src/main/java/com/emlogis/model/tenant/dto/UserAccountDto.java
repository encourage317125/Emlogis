package com.emlogis.model.tenant.dto;

import org.joda.time.DateTimeZone;

import com.emlogis.model.tenant.AccountStatus;

public class UserAccountDto extends AccountDto {

    public static final String LOGIN = "login";
    public static final String INACTIVITY_PERIOD = "inactivityPeriod";
    public static final	String FIRST_NAME = "firstName";
    public static final	String LAST_NAME = "lastName";
    public static final	String MIDDLE_NAME = "middleName";
    public static final String WORK_EMAIL = "workEmail";
    public static final String WORK_PHONE = "workPhone";
    public static final String MOBILE_PHONE = "mobilePhone";
    public static final String ADDRESS = "address";
    public static final String ADDRESS2 = "address2";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String ZIP = "zip";
    public static final String GENDER = "gender";
    public static final String HOME_PHONE = "homePhone";
    public static final String HOME_EMAIL = "homeEmail";

    private String login;

    @Deprecated
    private String email;

    private Long inactivityPeriod = 0L;
    
    private AccountStatus status;
    
    private String firstName;
    private String lastName;
    private String middleName;
    private String workEmail;
    private String workPhone;
    private String mobilePhone;
 	private String address;
 	private String address2;
 	private String city;
 	private String state;
 	private String country;
 	private String zip;
    private String language;
 	private String gender; 
 	private String homePhone;
 	private String homeEmail;
 	private String employeeId;
    private String timeZone;


 	private boolean isNotificationEnabled = true;

 	private long lastLogged;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public Long getInactivityPeriod() {
		return inactivityPeriod;
	}

	public void setInactivityPeriod(Long inactivityPeriod) {
		this.inactivityPeriod = inactivityPeriod;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getWorkEmail() {
		return workEmail;
	}

	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}

	public String getHomeEmail() {
		return homeEmail;
	}

	public void setHomeEmail(String homeEmail) {
		this.homeEmail = homeEmail;
	}

	public boolean isNotificationEnabled() {
		return isNotificationEnabled;
	}

	public void setNotificationEnabled(boolean isNotificationEnabled) {
		this.isNotificationEnabled = isNotificationEnabled;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

    public long getLastLogged() {
        return lastLogged;
    }

    public void setLastLogged(long lastLogged) {
        this.lastLogged = lastLogged;
    }

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
}
