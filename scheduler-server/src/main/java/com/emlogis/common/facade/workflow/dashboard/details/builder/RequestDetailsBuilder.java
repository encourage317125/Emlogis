package com.emlogis.common.facade.workflow.dashboard.details.builder;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.details.abstracts.RequestDetailsInfo;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

/**
 * Created by user on 20.08.15.
 */
public interface RequestDetailsBuilder<Type extends RequestDetailsInfo> {
    
    Type build(WorkflowRequest request, PrimaryKey requesterPk);

    WorkflowRequestTypeDict requestType();

    WorkflowRoleDict role();
}
