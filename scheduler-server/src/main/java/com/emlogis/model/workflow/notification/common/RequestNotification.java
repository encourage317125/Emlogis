package com.emlogis.model.workflow.notification.common;

import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.emlogis.workflow.WflUtil.messagePrm;

/**
 * Created by user on 23.07.15.
 */
public abstract class RequestNotification implements Serializable {

    private String requestId;

    private String code;

    private String submitDateTime;

    private MessageEmployeeInfo submitter;

    private String receiverName;

    public RequestNotification(
            String requestId,
            String code,
            Long submitDateTime,
            MessageEmployeeInfo owner,
            String receiverName
    ) {
        this.requestId = requestId;
        this.code = code;
        this.submitter = owner;
        this.receiverName = receiverName;
        this.submitDateTime = messagePrm(submitDateTime, owner.getTimeZone(), owner.locale());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setSubmitDateTime(String submitDateTime) {
        this.submitDateTime = submitDateTime;
    }

    public void setSubmitter(MessageEmployeeInfo submitter) {
        this.submitter = submitter;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSubmitDateTime() {
        return submitDateTime;
    }

    public MessageEmployeeInfo getSubmitter() {
        return submitter;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Map<String, String> post() {
        Map<String, String> result = new HashMap<>();
        result.put("requestId", getRequestId());
        result.put("code", getCode());
        result.put("submitDateTime", getSubmitDateTime());
        result.put("submitterId", getSubmitter().getId());
        result.put("submitterName", getSubmitter().getName());
        result.put("submitterLang", getSubmitter().getLang());
        result.put("submitterCountry", getSubmitter().getCountry());
        result.put("submitterTz", getSubmitter().getTimeZone().getID());
        result.put("receiverName", getReceiverName());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RequestNotification) {
            RequestNotification other = (RequestNotification) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getRequestId(), other.getRequestId());
            builder.append(getSubmitDateTime(), other.getSubmitDateTime());
            builder.append(getSubmitter(), other.getSubmitter());
            builder.append(getReceiverName(), other.getReceiverName());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getRequestId());
        builder.append(getSubmitDateTime());
        builder.append(getSubmitter());
        builder.append(getReceiverName());
        return builder.toHashCode();
    }

}
