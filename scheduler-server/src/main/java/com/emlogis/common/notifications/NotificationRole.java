package com.emlogis.common.notifications;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.workflow.enums.WorkflowRoleDict;

/**
 * Created by user on 28.07.15.
 */
public enum NotificationRole {
    NONE(false),
    MANAGER(true),
    SUBMITTER(true),
    PEER(true);

    private final Boolean isWorkflowRole;

    NotificationRole(
            Boolean isWorkflowEvent
    ) {
        this.isWorkflowRole = isWorkflowEvent;
    }

    public Boolean isWorkflowRole() {
        return isWorkflowRole;
    }
    
    public String getFilePart() {
		return this.name().toLowerCase();
    }

}
