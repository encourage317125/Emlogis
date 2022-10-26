package com.emlogis.workflow.controller.dto;

import java.io.Serializable;

/**
 * Created by user on 02.10.15.
 */
public class SuccessSubmitDto implements Serializable {

    private String requestId;

    private String requestStatus;

    private String requestType;

    private Long requestDate;

    private String shiftId;

    private Integer numberOfPeers;

    private Integer numberOfManagers;

    public SuccessSubmitDto() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Long getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Long requestDate) {
        this.requestDate = requestDate;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public Integer getNumberOfPeers() {
        return numberOfPeers;
    }

    public void setNumberOfPeers(Integer numberOfPeers) {
        this.numberOfPeers = numberOfPeers;
    }

    public Integer getNumberOfManagers() {
        return numberOfManagers;
    }

    public void setNumberOfManagers(Integer numberOfManagers) {
        this.numberOfManagers = numberOfManagers;
    }
}
