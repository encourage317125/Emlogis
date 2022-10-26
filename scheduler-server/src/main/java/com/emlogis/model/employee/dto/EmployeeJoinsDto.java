package com.emlogis.model.employee.dto;

public class EmployeeJoinsDto {

    private String id;
    private String firstName;
    private String lastName;
    private String primaryJobRole;
    private String activityType;
    private String homeSite;
    private String homeTeam;
    private String primaryShift;
    private Long hireDate;
    private String workEmail;
    private String mobilePhone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getPrimaryJobRole() {
        return primaryJobRole;
    }

    public void setPrimaryJobRole(String primaryJobRole) {
        this.primaryJobRole = primaryJobRole;
    }

    public String getHomeSite() {
        return homeSite;
    }

    public void setHomeSite(String homeSite) {
        this.homeSite = homeSite;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getPrimaryShift() {
        return primaryShift;
    }

    public void setPrimaryShift(String primaryShift) {
        this.primaryShift = primaryShift;
    }

    public Long getHireDate() {
        return hireDate;
    }

    public void setHireDate(Long hireDate) {
        this.hireDate = hireDate;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
}
