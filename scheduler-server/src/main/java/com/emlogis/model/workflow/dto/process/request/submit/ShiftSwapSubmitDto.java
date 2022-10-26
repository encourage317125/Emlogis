package com.emlogis.model.workflow.dto.process.request.submit;

import com.emlogis.model.workflow.dto.process.request.WflRoleInstanceDto;
import com.emlogis.workflow.enums.WorkflowPeerWinnerStrategyDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucas on 28.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SHIFT_SWAP_REQUEST")
public class ShiftSwapSubmitDto extends SubmitDto implements Serializable {

    @JsonProperty(value = "applyStrategy", required = false)
    private WorkflowPeerWinnerStrategyDict applyStrategy = WorkflowPeerWinnerStrategyDict.MANUAL_SELECT;

    @JsonProperty(value = "submitterShiftId", required = true)
    private String submitterShiftId;

    @JsonProperty(value = "assignments", required = true)
    private List<WflRoleInstanceDto> assignments;

    public ShiftSwapSubmitDto() {
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

    public List<WflRoleInstanceDto> getAssignments() {
        if(assignments == null) {
            assignments = new ArrayList<>();
        }
        return assignments;
    }

    public void setAssignments(List<WflRoleInstanceDto> assignments) {
        this.assignments = assignments;
    }
}
