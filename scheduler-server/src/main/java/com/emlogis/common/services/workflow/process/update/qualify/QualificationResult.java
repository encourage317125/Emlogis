package com.emlogis.common.services.workflow.process.update.qualify;

import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.model.workflow.entities.WorkflowRequest;

/**
 * Created by user on 27.08.15.
 */
public class QualificationResult {
    private WorkflowRequest request;
    private Boolean result;
    private PostedOpenShiftService.ShiftPostedPair shiftPostedPair;

    public QualificationResult(WorkflowRequest request) {
        this.request = request;
    }

    public void setRequest(WorkflowRequest request) {
        this.request = request;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public void setShiftPostedPair(PostedOpenShiftService.ShiftPostedPair shiftPostedPair) {
        this.shiftPostedPair = shiftPostedPair;
    }

    public WorkflowRequest getRequest() {
        return request;
    }

    public Boolean getResult() {
        return result;
    }

    public PostedOpenShiftService.ShiftPostedPair getShiftPostedPair() {
        return shiftPostedPair;
    }
}
