package com.emlogis.model.tenant.dto;

import com.emlogis.model.tenant.AccountStatus;

public class UserAccountQueryDto extends AccountDto {

    private String login;
    private String email;
    private long inactivityPeriod;
    private String language;
    private AccountStatus status;
    private String employeeId;
    private String employeeFirstName;
    private String employeeLastName;
    private String roles;
    private int nbOfRoles;
    private String groups;
    private int nbOfGroups;

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
	}

	public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public int getNbOfRoles() {
        return nbOfRoles;
    }

    public void setNbOfRoles(int nbOfRoles) {
        this.nbOfRoles = nbOfRoles;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public int getNbOfGroups() {
        return nbOfGroups;
    }

    public void setNbOfGroups(int nbOfGroups) {
        this.nbOfGroups = nbOfGroups;
    }
}

