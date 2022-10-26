package com.emlogis.model.workflow.dto.details.manager;

import com.emlogis.model.workflow.dto.action.InstanceLog;
import com.emlogis.model.workflow.dto.commentary.RequestCommentary;
import com.emlogis.model.workflow.dto.details.abstracts.RequestDetailsInfo;

/**
 * Created by user on 21.08.15.
 */
public interface ManagerDetailsInfo extends RequestDetailsInfo {

    Boolean getManagerApprovalNeeded();

    void setManagerApprovalNeeded(Boolean managerApprovalNeeded);

    String getComment();

    void setComment(String comment);

    Long getDateOfAction();

    void setDateOfAction(Long dateOfAction);

    String getDescription();

    void setDescription(String description);

    RequestCommentary getCommentary();

    void setCommentary(RequestCommentary commentary);

    InstanceLog getHistory();

    void setHistory(InstanceLog history);

    Boolean getIsRead();

    void setIsRead(Boolean isRead);
}
