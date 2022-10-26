package com.emlogis.model.tenant.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionRoleGroupMappingDto implements Serializable {

    private Map<PermissionDto, Map<RoleDto, Set<GroupDto>>> mappings = new HashMap<>();

    private static class MappingInsideDto implements Serializable {
        private String id;
        private String name;

        public MappingInsideDto(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o) return false;
            if (o.getClass() != this.getClass()) return false;

            MappingInsideDto that = (MappingInsideDto) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "{id='" + id + '\'' + ", name='" + name + '\'' + '}';
        }
    }

    public static class PermissionDto extends MappingInsideDto {
        public PermissionDto(String id, String name) {
            super(id, name);
        }
    }

    public static class RoleDto extends MappingInsideDto {
        public RoleDto(String id, String name) {
            super(id, name);
        }
    }

    public static class GroupDto extends MappingInsideDto {
        public GroupDto(String id, String name) {
            super(id, name);
        }
    }

    public Map<PermissionDto, Map<RoleDto, Set<GroupDto>>> getMappings() {
        return mappings;
    }

    public void setMappings(Map<PermissionDto, Map<RoleDto, Set<GroupDto>>> mappings) {
        this.mappings = mappings;
    }

    public void add(PermissionDto permissionDto, RoleDto roleDto) {
        Map<RoleDto, Set<GroupDto>> roleGroupValue = mappings.get(permissionDto);
        if (roleGroupValue == null) {
            roleGroupValue = new HashMap<>();
            mappings.put(permissionDto, roleGroupValue);
        }
        roleGroupValue.put(roleDto, null);
    }

    public void add(PermissionDto permissionDto, RoleDto roleDto, GroupDto groupDto) {
        Map<RoleDto, Set<GroupDto>> roleGroupValue = mappings.get(permissionDto);
        if (roleGroupValue == null) {
            roleGroupValue = new HashMap<>();
            mappings.put(permissionDto, roleGroupValue);
        }
        Set<GroupDto> groupDtos = roleGroupValue.get(roleDto);
        if (groupDtos == null) {
            groupDtos = new HashSet<>();
            roleGroupValue.put(roleDto, groupDtos);
        }
        groupDtos.add(groupDto);
    }
}
