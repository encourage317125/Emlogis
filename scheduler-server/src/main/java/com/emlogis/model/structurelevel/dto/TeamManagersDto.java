package com.emlogis.model.structurelevel.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class TeamManagersDto implements Serializable {

    private String teamId;
    private String teamName;
    private Collection<ManagerDto> managers = new ArrayList<>();

    public static class ManagerDto implements Serializable {
        private String accountId;
        private String name;
        private String employeeId;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
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

    public Collection<ManagerDto> getManagers() {
        return managers;
    }

    public void setManagers(Collection<ManagerDto> managers) {
        this.managers = managers;
    }
}
