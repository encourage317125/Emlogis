package com.emlogis.workflow.enums;

import com.emlogis.common.notifications.NotificationRole;

/**
 * Created by alexborlis on 05.02.15.
 */
public enum WorkflowRoleDict {
    MANAGER(NotificationRole.MANAGER, "Request administrator"),
    PEER(NotificationRole.PEER, "Request peer(SWAP/WIP)"),
    ORIGINATOR(NotificationRole.SUBMITTER, "Request submitter");

    private final NotificationRole notificationRole;
    private final String readableName;

    WorkflowRoleDict(NotificationRole notificationRole, String readableName) {
        this.notificationRole = notificationRole;
        this.readableName = readableName;
    }

    public NotificationRole notificationRole() {
        return notificationRole;
    }

    public String readableName() {
        return readableName;
    }
}
