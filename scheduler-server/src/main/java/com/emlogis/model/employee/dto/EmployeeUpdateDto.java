package com.emlogis.model.employee.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.emlogis.model.contract.dto.OvertimeDto;
import com.emlogis.model.employee.EmployeeActivityType;
import com.emlogis.model.employee.EmployeeType;
import com.emlogis.model.structurelevel.dto.AOMEntityUpdateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeUpdateDto extends AOMEntityUpdateDto implements Serializable {
	
	// public constants for validation annotations
	public static final	String EMPLOYEE_IDENTIFIER = "employeeIdentifier";
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
 	public static final String EC_RELATIONSHIP = "ecRelationship";
 	public static final String EC_PHONE_NUMBER = "ecPhoneNumber";
 	public static final String EMERGENCY_CONTACT = "emergencyContact";
 	public static final String GENDER = "gender"; 
 	public static final String HOME_PHONE = "homePhone";
 	public static final String HOME_EMAIL = "homeEmail";
 	public static final String PROFESSIONAL_LABEL = "professionalLabel";

    // required attributes (also in the create DTO)
	private	String firstName;
	private	String lastName;
	private	String employeeIdentifier;

	// optional attributes (not in the create DTO, but avail for use in create or update)
	private	String middleName;
    private String workEmail;
    private String workPhone;
    private String mobilePhone;

    private EmployeeActivityType activityType;
    private Boolean isNotificationEnabled;
 	private String address;
 	private String address2;
 	private String city;
 	private String state;
 	private String zip;
 	private String ecRelationship;
 	private String ecPhoneNumber;
 	private String emergencyContact;
 	private String gender; 
 	private String homePhone;
 	private String homeEmail;
 	private Integer primaryContactIndicator;
 	private String professionalLabel;
 	private Long hireDate;
 	private Long startDate;
 	private Long endDate;
 	private Float hourlyRate;
 	private Boolean isDeleted;
    private EmployeeType employeeType;

    private OvertimeDto overtimeDto;

    /**
     * Getter for firstName
     * @return
     */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Setter for firstName
	 * @param firstName
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Getter for middleName
	 * @return
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * Setter for middleName
	 * @param middleName
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * Getter for lastName
	 * @return
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Setter for lastName
	 * @param lastName
	 */
	public void setLastName(String lastName) {
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
		return workEmail;
	}

	/**
	 * Setter for workEmail
	 * @param workEmail
	 */
	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}

	/**
	 * @return the workPhone
	 */
	public String getWorkPhone() {
		return workPhone;
	}

	/**
	 * @param workPhone the workPhone to set
	 */
	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	/**
	 * Getter for notifiationSmsNumber
	 * @return
	 */
	public String getMobilePhone() {
		return mobilePhone;
	}

	/**
	 * Setter for mobilePhone
	 * @param notificationSmsNumber
	 */
	public void setMobilePhone(String notificationSmsNumber) {
		this.mobilePhone = notificationSmsNumber;
	}	

    public EmployeeActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(EmployeeActivityType activityType) {
		this.activityType = activityType;
	}

	public Boolean getIsNotificationEnabled() {
		return isNotificationEnabled;
	}

	public void setIsNotificationEnabled(boolean isNotificationEnabled) {
		this.isNotificationEnabled = isNotificationEnabled;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the address2
	 */
	public String getAddress2() {
		return address2;
	}

	/**
	 * @param address2 the address2 to set
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the zip
	 */
	public String getZip() {
		return zip;
	}

	/**
	 * @param zip the zip to set
	 */
	public void setZip(String zip) {
		this.zip = zip;
	}

	/**
	 * @return the ecRelationship
	 */
	public String getEcRelationship() {
		return ecRelationship;
	}

	/**
	 * @param ecRelationship the ecRelationship to set
	 */
	public void setEcRelationship(String ecRelationship) {
		this.ecRelationship = ecRelationship;
	}

	/**
	 * @return the ecPhoneNumber
	 */
	public String getEcPhoneNumber() {
		return ecPhoneNumber;
	}

	/**
	 * @param ecPhoneNumber the ecPhoneNumber to set
	 */
	public void setEcPhoneNumber(String ecPhoneNumber) {
		this.ecPhoneNumber = ecPhoneNumber;
	}

	/**
	 * @return the emergencyContact
	 */
	public String getEmergencyContact() {
		return emergencyContact;
	}

	/**
	 * @param emergencyContact the emergencyContact to set
	 */
	public void setEmergencyContact(String emergencyContact) {
		this.emergencyContact = emergencyContact;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the homePhone
	 */
	public String getHomePhone() {
		return homePhone;
	}

	/**
	 * @param homePhone the homePhone to set
	 */
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}

	/**
	 * @return the primaryContactIndicator
	 */
	public Integer getPrimaryContactIndicator() {
		return primaryContactIndicator;
	}

	/**
	 * @param primaryContactIndicator the primaryContactIndicator to set
	 */
	public void setPrimaryContactIndicator(Integer primaryContactIndicator) {
		this.primaryContactIndicator = primaryContactIndicator;
	}

	/**
	 * @return the homeEmail
	 */
	public String getHomeEmail() {
		return homeEmail;
	}

	/**
	 * @param homeEmail the homeEmail to set
	 */
	public void setHomeEmail(String homeEmail) {
		this.homeEmail = homeEmail;
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
	public Long getHireDate() {
		return hireDate;
	}

	/**
	 * @param hireDate the hireDate to set
	 */
	public void setHireDate(Long hireDate) {
		this.hireDate = hireDate;
	}

	/**
	 * @return the startDate
	 */
	public Long getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Long getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the hourlyRate
	 */
	public Float getHourlyRate() {
		return hourlyRate;
	}

	/**
	 * @param hourlyRate the hourlyRate to set
	 */
	public void setHourlyRate(Float hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
    }

    public OvertimeDto getOvertimeDto() {
		return overtimeDto;
	}

	public void setOvertimeDto(OvertimeDto overtimeDto) {
		this.overtimeDto = overtimeDto;
	}
	
}