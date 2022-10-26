package com.emlogis.model.workflow.dto.process.response;

import java.io.Serializable;

/**
 * Created by user on 21.07.15.
 */
public class SuccessSubmitResultDto extends SubmitResultDto implements Serializable {

    private String identifier;

    private String declineReason;

    private String requestId;

    private String requestStatus;

    private String requestType;

    private Long requestDate;

    private String shiftId;

    private Integer numberOfPeers;

    private Integer numberOfManagers;

	boolean autoApprove = false;

    public SuccessSubmitResultDto() {
    }

    public SuccessSubmitResultDto(
            Boolean isSuccess,
            String requestId,
            String requestStatus,
            String requestType,
            Long requestDate,
            String shiftId,
            Integer numberOfPeers,
            Integer numberOfManagers,
            String identifier,
            String declineReason) {
        super(isSuccess);
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.shiftId = shiftId;
        this.numberOfPeers = numberOfPeers;
        this.numberOfManagers = numberOfManagers;
        this.identifier = identifier;
        this.declineReason = declineReason;
    }

    public SuccessSubmitResultDto(
            Boolean isSuccess,
            String requestId,
            String requestStatus,
            String requestType,
            Long requestDate,
            String shiftId,
            Integer numberOfPeers,
            Integer numberOfManagers,
            String identifier,
            String declineReason,
            Boolean autoApprove) {
        super(isSuccess);
        this.autoApprove = autoApprove;
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.shiftId = shiftId;
        this.numberOfPeers = numberOfPeers;
        this.numberOfManagers = numberOfManagers;
        this.identifier = identifier;
        this.declineReason = declineReason;
    }

    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
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
