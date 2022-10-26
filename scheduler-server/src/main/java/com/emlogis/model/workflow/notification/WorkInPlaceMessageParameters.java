package com.emlogis.model.workflow.notification;

import com.emlogis.common.EmlogisUtils;
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
import java.util.*;

import static com.emlogis.workflow.WflUtil.messagePrmPeer;
import static com.emlogis.workflow.WflUtil.messagePrmPeers;
import static java.lang.String.valueOf;

/**
 * Created by user on 23.07.15.
 */
public final class WorkInPlaceMessageParameters extends RequestNotification
        implements Serializable, RequestMessageParameters {

    private MessageParametersShiftInfo submitterShiftInfo;
    private List<MessageEmployeeInfo> peers;
    private MessageEmployeeInfo receiver;
    private MessageEmployeeInfo choosen;
    private Long peerCount;
    private WorkflowRequestStatusDict receiverStatus;
    private WorkflowRequestStatusDict requestStatus;
    private String logCode;

    public WorkInPlaceMessageParameters(
            String requestId,
            String code,
            WorkflowRequestStatusDict requestStatus,
            Long submitDateTime,
            MessageParametersShiftInfo submitterShiftInfo,
            List<MessageEmployeeInfo> peers,
            MessageEmployeeInfo receiver,
            Long peerCount,
            String receiverName,
            WorkflowRequestStatusDict receiverStatus,
            MessageEmployeeInfo choosen,
            String logCode
    ) {
        super(requestId, code, submitDateTime, submitterShiftInfo.getOwner(), receiverName);
        this.logCode = logCode;
        this.submitterShiftInfo = submitterShiftInfo;
        this.peers = peers;
        this.peerCount = peerCount;
        this.requestStatus = requestStatus;
        this.receiver = receiver;
        this.receiverStatus = receiverStatus;
        this.choosen = choosen;
    }

    public String getLogCode() {
        return logCode;
    }

    public void setLogCode(String logCode) {
        this.logCode = logCode;
    }

    public MessageEmployeeInfo getReceiver() {
        return receiver;
    }

    public void setReceiver(MessageEmployeeInfo receiver) {
        this.receiver = receiver;
    }

    public MessageEmployeeInfo getChoosen() {
        return choosen;
    }

    public void setChoosen(MessageEmployeeInfo choosen) {
        this.choosen = choosen;
    }

    public WorkflowRequestStatusDict getReceiverStatus() {
        return receiverStatus;
    }

    public void setReceiverStatus(WorkflowRequestStatusDict receiverStatus) {
        this.receiverStatus = receiverStatus;
    }

    public MessageParametersShiftInfo getSubmitterShiftInfo() {
        return submitterShiftInfo;
    }

    public void setSubmitterShiftInfo(MessageParametersShiftInfo submitterShiftInfo) {
        this.submitterShiftInfo = submitterShiftInfo;
    }

    public List<MessageEmployeeInfo> getPeers() {
        if (peers == null) {
            peers = new ArrayList<>();
        }
        return peers;
    }

    public void setPeers(List<MessageEmployeeInfo> peers) {
        this.peers = peers;
    }

    public Long getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(Long peerCount) {
        this.peerCount = peerCount;
    }

    public WorkflowRequestStatusDict getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(WorkflowRequestStatusDict requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Map<String, String> post() {
        Map<String, String> result = new HashMap<>(super.post());
        result.put("logCode", getLogCode());
        result.put("requestStatus", getRequestStatus().name());
        result.put("submitterShiftId", getSubmitterShiftInfo().getShiftId());
        result.put("submitterShiftDate", getSubmitterShiftInfo().getShiftDate());
        result.put("submitterShiftStartTime", getSubmitterShiftInfo().getShiftStartTime());
        result.put("submitterShiftEndTime", getSubmitterShiftInfo().getShiftEndTime());
        result.put("submitterTeamId", getSubmitterShiftInfo().getShiftTeamId());
        result.put("submitterTeamName", getSubmitterShiftInfo().getShiftTeamName());
        result.put("submitterSkillId", getSubmitterShiftInfo().getShiftSkillId());
        result.put("submitterSkillName", getSubmitterShiftInfo().getShiftSkillName());
        result.put("peerShifts", messagePrmPeers(getPeers()));
        result.put("peerCount", valueOf(getPeerCount()));
        result.put("receiver", messagePrmPeer(getReceiver()));
        if(getChoosen() != null) {
            result.put("choosen", messagePrmPeer(getChoosen()));
        } else {
            result.put("choosen", EmlogisUtils.toJsonString(Collections.emptyList()));
        }
        result.put("receiverStatus", getReceiverStatus().name());
        return result;
    }


    public boolean equals(Object obj) {
        if (obj instanceof WorkInPlaceMessageParameters) {
            WorkInPlaceMessageParameters other = (WorkInPlaceMessageParameters) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getPeerCount(), other.getPeerCount());
            builder.append(getPeers(), other.getPeers());
            builder.append(getRequestStatus(), other.getRequestStatus());
            builder.append(getSubmitterShiftInfo(), other.getSubmitterShiftInfo());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getPeerCount());
        builder.append(getPeers());
        builder.append(getRequestStatus());
        builder.append(getSubmitterShiftInfo());
        return builder.toHashCode();
    }
}
