package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.convenience.ACEConfigurationAllSitesDto;

import java.util.List;

public class RoleViewDto extends RoleDto {

    private List<GroupDto> groups;
    private List<MemberDto> members;
    private List<PermissionDto> permissions;
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

    public static class GroupDto {

        private String groupId;
        private String name;
        private String description;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
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

    public static class PermissionDto {

        private String permissionId;
        private String name;
        private String description;

        public String getPermissionId() {
            return permissionId;
        }

        public void setPermissionId(String permissionId) {
            this.permissionId = permissionId;
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

    public List<GroupDto> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDto> groups) {
        this.groups = groups;
    }

    public List<MemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<MemberDto> members) {
        this.members = members;
    }

    public List<PermissionDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDto> permissions) {
        this.permissions = permissions;
    }

    public ACEConfigurationAllSitesDto getAceConfigurationAllSitesDto() {
        return aceConfigurationAllSitesDto;
    }

    public void setAceConfigurationAllSitesDto(ACEConfigurationAllSitesDto aceConfigurationAllSitesDto) {
        this.aceConfigurationAllSitesDto = aceConfigurationAllSitesDto;
    }
}
