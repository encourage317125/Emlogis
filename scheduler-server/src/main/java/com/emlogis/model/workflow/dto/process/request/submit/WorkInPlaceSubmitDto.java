package com.emlogis.model.workflow.dto.process.request.submit;

import com.emlogis.workflow.enums.WorkflowPeerWinnerStrategyDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucas on 28.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("WIP_REQUEST")
public class WorkInPlaceSubmitDto extends SubmitDto implements Serializable {

    private WorkflowPeerWinnerStrategyDict applyStrategy = WorkflowPeerWinnerStrategyDict.MANUAL_SELECT;
    private String submitterShiftId;
    private List<String> recipientIds;

    public WorkInPlaceSubmitDto() {
    }

    public WorkflowPeerWinnerStrategyDict getApplyStrategy() {
        return applyStrategy;
    }

    public void setApplyStrategy(WorkflowPeerWinnerStrategyDict applyStrategy) {
        this.applyStrategy = applyStrategy;
    }

    public String getSubmitterShiftId() {
        return submitterShiftId;
    }

    public void setSubmitterShiftId(String submitterShiftId) {
        this.submitterShiftId = submitterShiftId;
    }

    public List<String> getRecipientIds() {
        if (recipientIds == null) {
            recipientIds = new ArrayList<>();
        }
        return recipientIds;
    }

    public void setRecipientIds(List<String> recipientIds) {
        this.recipientIds = recipientIds;
    }
}
