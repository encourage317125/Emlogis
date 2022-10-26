package com.emlogis.model.employee.dto;

import com.emlogis.model.employee.EmployeeType;

import java.io.Serializable;

public class EmployeeViewDto implements Serializable {

    private String employeeId;
    private String firstName;
    private String lastName;
    private EmployeeType employeeType;
    private Long hireDate;
    private String primarySkillName;
    private String primarySkillId;
    private String homeTeamName;
    private String homeTeamId;
    private Boolean isFloating;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
    }

    public Long getHireDate() {
        return hireDate;
    }

    public void setHireDate(Long hireDate) {
        this.hireDate = hireDate;
    }

    public String getPrimarySkillName() {
        return primarySkillName;
    }

    public void setPrimarySkillName(String primarySkillName) {
        this.primarySkillName = primarySkillName;
    }

    public String getPrimarySkillId() {
        return primarySkillId;
    }

    public void setPrimarySkillId(String primarySkillId) {
        this.primarySkillId = primarySkillId;
    }

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public void setHomeTeamName(String homeTeamName) {
        this.homeTeamName = homeTeamName;
    }

    public String getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(String homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }
}
