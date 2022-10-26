package com.emlogis.model.tenant.dto;

import java.util.List;

public class UserAccountViewDto extends UserAccountDto {

    private List<GroupDto> groups;
    private List<RoleDto> roles;
    private List<RoleDto> inheritedRoles;
    private String tenantId;

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

    public List<GroupDto> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDto> groups) {
        this.groups = groups;
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }

    public List<RoleDto> getInheritedRoles() {
        return inheritedRoles;
    }

    public void setInheritedRoles(List<RoleDto> inheritedRoles) {
        this.inheritedRoles = inheritedRoles;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
