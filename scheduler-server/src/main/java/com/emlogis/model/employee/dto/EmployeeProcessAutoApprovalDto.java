package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class EmployeeProcessAutoApprovalDto extends BaseEntityDto {

    private boolean autoApproval;
    private String employeeId;
    private String wflProcessTypeId;
    private String wflProcessTypeName;

    public boolean isAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getWflProcessTypeId() {
        return wflProcessTypeId;
    }

    public void setWflProcessTypeId(String wflProcessTypeId) {
        this.wflProcessTypeId = wflProcessTypeId;
    }

    public String getWflProcessTypeName() {
        return wflProcessTypeName;
    }

    public void setWflProcessTypeName(String wflProcessTypeName) {
        this.wflProcessTypeName = wflProcessTypeName;
    }
}
