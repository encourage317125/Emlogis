package com.emlogis.model.workflow.notification;

import com.emlogis.model.workflow.notification.common.MessageEmployeeInfo;
import com.emlogis.model.workflow.notification.common.MessageParametersShiftInfo;
import com.emlogis.model.workflow.notification.common.RequestMessageParameters;
import com.emlogis.model.workflow.notification.common.RequestNotification;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emlogis.workflow.WflUtil.messagePrm;
import static com.emlogis.workflow.WflUtil.messagePrmShifts;

/**
 * Created by user on 23.07.15.
 */
public final class TimeOffMessageParameters extends RequestNotification
        implements Serializable, RequestMessageParameters {

    private String absenceTypeName;
    private String absenceDate;
    private String reason;
    private String requestStatus;
    private List<MessageParametersShiftInfo> submitterShifts;
    private Long submitterShiftsCount;
    private String logCode;

    public TimeOffMessageParameters(
            String requestId,
            String code,
            WorkflowRequestStatusDict requestStatus,
            Long submitDateTime,
            MessageEmployeeInfo submitter,
            String absenceTypeName,
            String reason,
            List<MessageParametersShiftInfo> submitterShifts,
            Long submitterShiftsCount,
            String receiverName,
            String logCode,
            String absenceDate
    ) {
        super(requestId, code, submitDateTime, submitter, receiverName);
        this.logCode = logCode;
        this.absenceDate = absenceDate;
        this.absenceTypeName = absenceTypeName;
        this.reason = reason;
        this.requestStatus = requestStatus.name();
        this.submitterShifts = submitterShifts;
        this.submitterShiftsCount = submitterShiftsCount;
    }

    public String getLogCode() {
        return logCode;
    }

    public void setLogCode(String logCode) {
        this.logCode = logCode;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getAbsenceDate() {
        return absenceDate;
    }

    public void setAbsenceDate(String absenceDate) {
        this.absenceDate = absenceDate;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public String getAbsenceTypeName() {
        return absenceTypeName;
    }

    public void setAbsenceTypeName(String absenceTypeName) {
        this.absenceTypeName = absenceTypeName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<MessageParametersShiftInfo> getSubmitterShifts() {
        if (submitterShifts == null) {
            submitterShifts = new ArrayList<>();
        }
        return submitterShifts;
    }

    public void setSubmitterShifts(List<MessageParametersShiftInfo> submitterShifts) {
        this.submitterShifts = submitterShifts;
    }

    public Long getSubmitterShiftsCount() {
        return submitterShiftsCount;
    }

    public void setSubmitterShiftsCount(Long submitterShiftsCount) {
        this.submitterShiftsCount = submitterShiftsCount;
    }

    @Override
    public Map<String, String> post() {
        Map<String, String> result = new HashMap<>(super.post());
        result.put("logCode", getLogCode());
        result.put("absenceDate", getAbsenceDate());
        result.put("absenceTypeName", getAbsenceTypeName());
        result.put("reason", getReason());
        result.put("requestStatus", getRequestStatus());
        result.put("submitterShifts", messagePrmShifts(getSubmitterShifts()));
        result.put("submitterShiftsCount", String.valueOf(getSubmitterShiftsCount()));
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TimeOffMessageParameters) {
            TimeOffMessageParameters other = (TimeOffMessageParameters) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getAbsenceDate(), other.getAbsenceDate());
            builder.append(getAbsenceTypeName(), other.getAbsenceTypeName());
            builder.append(getReason(), other.getReason());
            builder.append(getRequestStatus(), other.getRequestStatus());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getAbsenceDate());
        builder.append(getAbsenceTypeName());
        builder.append(getReason());
        builder.append(getRequestStatus());
        return builder.toHashCode();
    }
}
