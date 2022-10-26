package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.ResultSetDto;

import java.util.Collection;

public class EmployeeAvailabilityAndShiftsDto extends EmployeeAvailabilityDto {

    private ResultSetDto<Object[]> shifts;

    private Collection<SubmittedWipSwapRequestInfo> submittedWipSwapRequestInfos;
    private Collection<PeerSwapRequestInfo> peerSwapRequestInfos;
    private Collection<PeerWipRequestInfo> peerWipRequestInfos;
    private Collection<SubmittedTimeOffRequestInfo> submittedTimeOffRequestInfos;
    private Collection<SubmittedOsRequestInfo> submittedOsRequestInfos;

    public static class SubmittedWipSwapRequestInfo {
        private String requestId;
        private String requestType;
        private String requestStatus;
        private String shiftId;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public String getRequestStatus() {
            return requestStatus;
        }

        public void setRequestStatus(String requestStatus) {
            this.requestStatus = requestStatus;
        }

        public String getShiftId() {
            return shiftId;
        }

        public void setShiftId(String shiftId) {
            this.shiftId = shiftId;
        }
    }

    public static class PeerSwapRequestInfo {
        private String requestId;
        private String peerStatus;
        private String peerShiftId;
        private String submitterShiftId;
        private long submitterShiftStartDateTime;
        private long submitterShiftEndDateTime;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getPeerStatus() {
            return peerStatus;
        }

        public void setPeerStatus(String peerStatus) {
            this.peerStatus = peerStatus;
        }

        public String getPeerShiftId() {
            return peerShiftId;
        }

        public void setPeerShiftId(String peerShiftId) {
            this.peerShiftId = peerShiftId;
        }

        public String getSubmitterShiftId() {
            return submitterShiftId;
        }

        public void setSubmitterShiftId(String submitterShiftId) {
            this.submitterShiftId = submitterShiftId;
        }

        public long getSubmitterShiftStartDateTime() {
            return submitterShiftStartDateTime;
        }

        public void setSubmitterShiftStartDateTime(long submitterShiftStartDateTime) {
            this.submitterShiftStartDateTime = submitterShiftStartDateTime;
        }

        public long getSubmitterShiftEndDateTime() {
            return submitterShiftEndDateTime;
        }

        public void setSubmitterShiftEndDateTime(long submitterShiftEndDateTime) {
            this.submitterShiftEndDateTime = submitterShiftEndDateTime;
        }
    }

    public static class PeerWipRequestInfo {
        private String requestId;
        private String peerStatus;
        private String submitterShiftId;
        private long submitterShiftStartDateTime;
        private long submitterShiftEndDateTime;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getPeerStatus() {
            return peerStatus;
        }

        public void setPeerStatus(String peerStatus) {
            this.peerStatus = peerStatus;
        }

        public String getSubmitterShiftId() {
            return submitterShiftId;
        }

        public void setSubmitterShiftId(String submitterShiftId) {
            this.submitterShiftId = submitterShiftId;
        }

        public long getSubmitterShiftStartDateTime() {
            return submitterShiftStartDateTime;
        }

        public void setSubmitterShiftStartDateTime(long submitterShiftStartDateTime) {
            this.submitterShiftStartDateTime = submitterShiftStartDateTime;
        }

        public long getSubmitterShiftEndDateTime() {
            return submitterShiftEndDateTime;
        }

        public void setSubmitterShiftEndDateTime(long submitterShiftEndDateTime) {
            this.submitterShiftEndDateTime = submitterShiftEndDateTime;
        }
    }

    public static class SubmittedTimeOffRequestInfo {
        private String requestId;
        private String requestType;
        private String requestStatus;
        private long requestDate;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public String getRequestStatus() {
            return requestStatus;
        }

        public void setRequestStatus(String requestStatus) {
            this.requestStatus = requestStatus;
        }

        public long getRequestDate() {
            return requestDate;
        }

        public void setRequestDate(long requestDate) {
            this.requestDate = requestDate;
        }
    }

    public static class SubmittedOsRequestInfo {
        private String requestId;
        private String requestType;
        private String requestStatus;
        private String shiftId;
        private long requestDate;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public String getRequestStatus() {
            return requestStatus;
        }

        public void setRequestStatus(String requestStatus) {
            this.requestStatus = requestStatus;
        }

        public String getShiftId() {
            return shiftId;
        }

        public void setShiftId(String shiftId) {
            this.shiftId = shiftId;
        }

        public long getRequestDate() {
            return requestDate;
        }

        public void setRequestDate(long requestDate) {
            this.requestDate = requestDate;
        }
    }

    public ResultSetDto<Object[]> getShifts() {
        return shifts;
    }

    public void setShifts(ResultSetDto<Object[]> shifts) {
        this.shifts = shifts;
    }

    public Collection<SubmittedWipSwapRequestInfo> getSubmittedWipSwapRequestInfos() {
        return submittedWipSwapRequestInfos;
    }

    public void setSubmittedWipSwapRequestInfos(Collection<SubmittedWipSwapRequestInfo> submittedWipSwapRequestInfos) {
        this.submittedWipSwapRequestInfos = submittedWipSwapRequestInfos;
    }

    public Collection<PeerSwapRequestInfo> getPeerSwapRequestInfos() {
        return peerSwapRequestInfos;
    }

    public void setPeerSwapRequestInfos(Collection<PeerSwapRequestInfo> peerSwapRequestInfos) {
        this.peerSwapRequestInfos = peerSwapRequestInfos;
    }

    public Collection<PeerWipRequestInfo> getPeerWipRequestInfos() {
        return peerWipRequestInfos;
    }

    public void setPeerWipRequestInfos(Collection<PeerWipRequestInfo> peerWipRequestInfos) {
        this.peerWipRequestInfos = peerWipRequestInfos;
    }

    public Collection<SubmittedTimeOffRequestInfo> getSubmittedTimeOffRequestInfos() {
        return submittedTimeOffRequestInfos;
    }

    public void setSubmittedTimeOffRequestInfos(Collection<SubmittedTimeOffRequestInfo> submittedTimeOffRequestInfos) {
        this.submittedTimeOffRequestInfos = submittedTimeOffRequestInfos;
    }

    public Collection<SubmittedOsRequestInfo> getSubmittedOsRequestInfos() {
        return submittedOsRequestInfos;
    }

    public void setSubmittedOsRequestInfos(Collection<SubmittedOsRequestInfo> submittedOsRequestInfos) {
        this.submittedOsRequestInfos = submittedOsRequestInfos;
    }
}
