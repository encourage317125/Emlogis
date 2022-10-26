package com.emlogis.model.workflow.notification;

import com.emlogis.common.EmlogisUtils;
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

import static com.emlogis.workflow.WflUtil.messagePrmShift;
import static com.emlogis.workflow.WflUtil.messagePrmShifts;

/**
 * Created by user on 23.07.15.
 */
public final class ShiftSwapMessageParameters extends RequestNotification
        implements Serializable, RequestMessageParameters {

    private MessageParametersShiftInfo submitterShiftInfo;

    private List<MessageParametersShiftInfo> peerShifts;

    private List<MessageParametersShiftInfo> receiverShifts;

    private MessageParametersShiftInfo choosen;

    private Long peerShiftsCount;

    private Long receiverShiftsCount;

    private WorkflowRequestStatusDict receiverStatus;

    private WorkflowRequestStatusDict requestStatus;

    private String logCode;

    public ShiftSwapMessageParameters(
            String requestId,
            String code,
            WorkflowRequestStatusDict requestStatus,
            Long submitDateTime,
            MessageParametersShiftInfo submitterShiftInfo,
            List<MessageParametersShiftInfo> peerShifts,
            List<MessageParametersShiftInfo> receiverShifts,
            Long peerShiftsCount,
            Long receiverShiftsCount,
            String receiverName,
            WorkflowRequestStatusDict receiverStatus,
            MessageParametersShiftInfo choosen,
            String logCode
    ) {
        super(requestId, code, submitDateTime, submitterShiftInfo.getOwner(), receiverName);
        this.submitterShiftInfo = submitterShiftInfo;
        this.peerShifts = peerShifts;
        this.receiverShifts = receiverShifts;
        this.peerShiftsCount = peerShiftsCount;
        this.receiverShiftsCount = receiverShiftsCount;
        this.requestStatus = requestStatus;
        this.choosen = choosen;
        this.receiverStatus = receiverStatus;
        this.logCode = logCode;
    }

    public String getLogCode() {
        return logCode;
    }

    public void setLogCode(String logCode) {
        this.logCode = logCode;
    }

    public WorkflowRequestStatusDict getReceiverStatus() {
        return receiverStatus;
    }

    public void setReceiverStatus(WorkflowRequestStatusDict receiverStatus) {
        this.receiverStatus = receiverStatus;
    }

    public List<MessageParametersShiftInfo> getReceiverShifts() {
        if (receiverShifts == null) {
            receiverShifts = new ArrayList<>();
        }
        return receiverShifts;
    }

    public Long getReceiverShiftsCount() {
        return receiverShiftsCount;
    }

    public MessageParametersShiftInfo getSubmitterShiftInfo() {
        return submitterShiftInfo;
    }

    public List<MessageParametersShiftInfo> getPeerShifts() {
        return new ArrayList<>(peerShifts);
    }

    public Long getPeerShiftsCount() {
        return peerShiftsCount;
    }

    public WorkflowRequestStatusDict getRequestStatus() {
        return requestStatus;
    }

    public void setSubmitterShiftInfo(MessageParametersShiftInfo submitterShiftInfo) {
        this.submitterShiftInfo = submitterShiftInfo;
    }

    public void setPeerShifts(List<MessageParametersShiftInfo> peerShifts) {
        this.peerShifts = peerShifts;
    }

    public void setPeerShiftsCount(Long peerShiftsCount) {
        this.peerShiftsCount = peerShiftsCount;
    }

    public void setRequestStatus(WorkflowRequestStatusDict requestStatus) {
        this.requestStatus = requestStatus;
    }

    public MessageParametersShiftInfo getChoosen() {
        return choosen;
    }

    public void setChoosen(MessageParametersShiftInfo choosen) {
        this.choosen = choosen;
    }

    @Override
    public Map<String, String> post() {
        Map<String, String> result = new HashMap<>(super.post());
        try {
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
            result.put("peerShifts", messagePrmShifts(getPeerShifts()));
            result.put("peerCount", String.valueOf(getPeerShiftsCount()));
            if (!getReceiverShifts().isEmpty()) {
                result.put("receiverShifts", messagePrmShifts(getReceiverShifts()));
                result.put("receiverShiftsCount", String.valueOf(getReceiverShiftsCount()));
            }
            result.put("receiverStatus", getReceiverStatus().name());
            if (getChoosen() != null) {
                result.put("choosen", messagePrmShift(getChoosen()));
            } else {
                result.put("choosen", EmlogisUtils.toJsonString(Collections.emptyList()));
            }
        } catch (Exception error) {
            error.printStackTrace();
            throw new RuntimeException(error);
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ShiftSwapMessageParameters) {
            ShiftSwapMessageParameters other = (ShiftSwapMessageParameters) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getPeerShifts(), other.getPeerShifts());
            builder.append(getPeerShiftsCount(), other.getPeerShiftsCount());
            builder.append(getRequestStatus(), other.getRequestStatus());
            builder.append(getSubmitterShiftInfo(), other.getSubmitterShiftInfo());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getPeerShifts());
        builder.append(getPeerShiftsCount());
        builder.append(getRequestStatus());
        builder.append(getSubmitterShiftInfo());
        return builder.toHashCode();
    }
}
