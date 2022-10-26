package com.emlogis.model.workflow.notification;

import com.emlogis.model.workflow.notification.common.MessageEmployeeInfo;
import com.emlogis.model.workflow.notification.common.RequestMessageParameters;
import com.emlogis.model.workflow.notification.common.RequestNotification;
import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.emlogis.workflow.WflUtil.messagePrmOnlyDate;

/**
 * Created by user on 23.07.15.
 */
public class AvailabilityMessageParameters extends RequestNotification
        implements Serializable, RequestMessageParameters {

    private AvailabilityRequestSubtype subtype;
    private String effectiveStartDate;
    private WorkflowRequestStatusDict requestStatus;
    private String logCode;
    private String weekDay;
    private String startTime;
    private String endTime;
    private String effectiveUntilDate;

    public AvailabilityMessageParameters(
            String requestId,
            String code,
            Long submitDateTime,
            MessageEmployeeInfo submitter,
            AvailabilityRequestSubtype subtype,
            String effectiveStartDate,
            WorkflowRequestStatusDict requestStatus,
            String receiverName,
            String logCode,
            String weekDay,
            String startTime,
            String endTime,
            String effectiveUntilDate
    ) {
        super(requestId, code, submitDateTime, submitter, receiverName);
        this.logCode = logCode;
        this.subtype = subtype;
        this.effectiveStartDate = effectiveStartDate;
        this.requestStatus = requestStatus;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.effectiveUntilDate = effectiveUntilDate;
    }

    public String getLogCode() {
        return logCode;
    }

    public void setLogCode(String logCode) {
        this.logCode = logCode;
    }

    public AvailabilityRequestSubtype getSubtype() {
        return subtype;
    }

    public void setSubtype(AvailabilityRequestSubtype subtype) {
        this.subtype = subtype;
    }

    public String getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(String effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public WorkflowRequestStatusDict getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(WorkflowRequestStatusDict requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEffectiveUntilDate() {
        return effectiveUntilDate;
    }

    public void setEffectiveUntilDate(String effectiveUntilDate) {
        this.effectiveUntilDate = effectiveUntilDate;
    }

    @Override
    public Map<String, String> post() {
        Map<String, String> result = new HashMap<>(super.post());
        result.put("logCode", getLogCode());
        result.put("subtype", getSubtype().name());
        result.put("type", getSubtype().name());
        result.put("effectiveStartDate",getEffectiveStartDate());
        result.put("requestStatus", getRequestStatus().name());
        result.put("weekDay", getWeekDay());
        result.put("startTime", getStartTime());
        result.put("endTime", getEndTime());
        result.put("effectiveUntilDate", getEffectiveUntilDate());
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof AvailabilityMessageParameters) {
            AvailabilityMessageParameters other = (AvailabilityMessageParameters) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getSubtype(), other.getSubtype());
            builder.append(getEffectiveStartDate(), other.getEffectiveStartDate());
            builder.append(getRequestStatus(), other.getRequestStatus());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSubtype());
        builder.append(getEffectiveStartDate());
        builder.append(getRequestStatus());
        return builder.toHashCode();
    }
}
