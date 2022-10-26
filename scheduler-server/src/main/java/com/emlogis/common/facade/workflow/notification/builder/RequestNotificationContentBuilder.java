package com.emlogis.common.facade.workflow.notification.builder;

import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import java.util.Map;

/**
 * Created by user on 25.08.15.
 */
public interface RequestNotificationContentBuilder {

    Map<String, String> build(WorkflowRequest instance, PersonalizedEntity personalizedEntity, String logCode, String shiftId);

    WorkflowRequestTypeDict type();

    WorkflowRoleDict role();
}
