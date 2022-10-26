package com.emlogis.model.workflow.notification;

import com.emlogis.model.workflow.notification.common.MessageParametersShiftInfo;
import com.emlogis.model.workflow.notification.common.RequestMessageParameters;
import com.emlogis.model.workflow.notification.common.RequestNotification;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 23.07.15.
 */
public final class OpenShiftMessageParameters extends RequestNotification
        implements Serializable, RequestMessageParameters {

    private MessageParametersShiftInfo submitterShiftInfo;
    private WorkflowRequestStatusDict requestStatus;
    private String comment;
    private String declineReason;
    private String logCode;
    private String shiftStartDate;
    private String shiftStartTime;
    private String shiftEndTime;

    public OpenShiftMessageParameters(
            String requestId,
            String code,
            WorkflowRequestStatusDict requestStatus,
            Long submitDateTime,
            MessageParametersShiftInfo submitterShiftInfo,
            String comment,
            String declineReason,
            String receiverName,
            String logCode,
            String shiftStartDate,
            String shiftStartTime,
            String shiftEndTime
    ) {
        super(requestId, code, submitDateTime, submitterShiftInfo.getOwner(), receiverName);
        this.logCode = logCode;
        this.submitterShiftInfo = submitterShiftInfo;
        this.requestStatus = requestStatus;
        this.comment = comment;
        this.declineReason = declineReason;
        this.shiftStartDate = shiftStartDate;
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shiftEndTime;
    }

    public String getShiftStartDate() {
        return shiftStartDate;
    }

    public void setShiftStartDate(String shiftStartDate) {
        this.shiftStartDate = shiftStartDate;
    }

    public String getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(String shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public String getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(String shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public String getLogCode() {
        return logCode;
    }

    public void setLogCode(String logCode) {
        this.logCode = logCode;
    }

    public void setSubmitterShiftInfo(MessageParametersShiftInfo submitterShiftInfo) {
        this.submitterShiftInfo = submitterShiftInfo;
    }

    public void setRequestStatus(WorkflowRequestStatusDict requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public MessageParametersShiftInfo getSubmitterShiftInfo() {
        return submitterShiftInfo;
    }

    public WorkflowRequestStatusDict getRequestStatus() {
        return requestStatus;
    }

    public String getComment() {
        return comment;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    @Override
    public Map<String, String> post() {
        Map<String, String> result = new HashMap<>(super.post());
        result.put("logCode", getLogCode());
        result.put("shiftId", submitterShiftInfo.getShiftId());
        result.put("shiftStartDate", getShiftStartDate());
        result.put("shiftStartTime", getShiftStartTime());
        result.put("shiftEndTime", getShiftEndTime());
        //result.put("shiftEndDateTime", submitterShiftInfo.getShiftEndDateTime());
        result.put("shiftTeamId", submitterShiftInfo.getShiftTeamId());
        result.put("shiftTeamName", submitterShiftInfo.getShiftTeamName());
        result.put("shiftSkillId", submitterShiftInfo.getShiftSkillId());
        result.put("shiftSkillName", submitterShiftInfo.getShiftSkillName());
        result.put("requestStatus", getRequestStatus().name());
        result.put("comment", getComment());
        result.put("declineReason", getDeclineReason());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpenShiftMessageParameters) {
            OpenShiftMessageParameters other = (OpenShiftMessageParameters) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getComment(), other.getComment());
            builder.append(getDeclineReason(), other.getDeclineReason());
            builder.append(getRequestStatus(), other.getRequestStatus());
            builder.append(getSubmitterShiftInfo(), other.getSubmitterShiftInfo());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getComment());
        builder.append(getDeclineReason());
        builder.append(getRequestStatus());
        builder.append(getSubmitterShiftInfo());
        return builder.toHashCode();
    }
}
