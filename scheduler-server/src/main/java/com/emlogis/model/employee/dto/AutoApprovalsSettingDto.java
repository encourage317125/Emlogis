package com.emlogis.model.employee.dto;

public class AutoApprovalsSettingDto {

    private Boolean availAutoApprove = false;
    private Boolean swapAutoApprove = false;
    private Boolean wipAutoApprove = false;

    public Boolean getAvailAutoApprove() {
        return availAutoApprove;
    }

    public void setAvailAutoApprove(Boolean availAutoApprove) {
        this.availAutoApprove = availAutoApprove;
    }

    public Boolean getSwapAutoApprove() {
        return swapAutoApprove;
    }

    public void setSwapAutoApprove(Boolean swapAutoApprove) {
        this.swapAutoApprove = swapAutoApprove;
    }

    public Boolean getWipAutoApprove() {
        return wipAutoApprove;
    }

    public void setWipAutoApprove(Boolean wipAutoApprove) {
        this.wipAutoApprove = wipAutoApprove;
    }
}
