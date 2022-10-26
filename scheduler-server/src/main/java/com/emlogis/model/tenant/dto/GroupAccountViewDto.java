package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.convenience.ACEConfigurationAllSitesDto;

import java.util.List;

public class GroupAccountViewDto extends GroupAccountDto {

    private List<MemberDto> members;
    private List<RoleDto> roles;
    private ACEConfigurationAllSitesDto aceConfigurationAllSitesDto;

    public static class MemberDto {

        private String accountId;
        private String employeeId;
        private String name;

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

    public static class RoleDto {

        private String roleId;
        private String name;
        private String description;

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public List<MemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<MemberDto> members) {
        this.members = members;
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }

    public ACEConfigurationAllSitesDto getAceConfigurationAllSitesDto() {
        return aceConfigurationAllSitesDto;
    }

    public void setAceConfigurationAllSitesDto(ACEConfigurationAllSitesDto aceConfigurationAllSitesDto) {
        this.aceConfigurationAllSitesDto = aceConfigurationAllSitesDto;
    }
}