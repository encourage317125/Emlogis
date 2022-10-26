package com.emlogis.model.workflow.dto.decision;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 19.08.15.
 */
public class WorkflowDecisionResultListDto implements Serializable {

    private List<WorkflowDecisionResultInfoDto> resultList;

    private String requestId;

    private String requestStatus;

    private Integer count = 0;

    public WorkflowDecisionResultListDto() {
    }

    public WorkflowDecisionResultListDto(String requestId) {
        this.requestId = requestId;
    }

    public List<WorkflowDecisionResultInfoDto> getResultList() {
        if (resultList == null) {
            resultList = new ArrayList<>();
        }
        return resultList;
    }

    public void setResultList(List<WorkflowDecisionResultInfoDto> resultList) {
        this.resultList = resultList;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
}
