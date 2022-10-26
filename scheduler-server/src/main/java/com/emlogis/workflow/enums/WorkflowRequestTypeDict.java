package com.emlogis.workflow.enums;

import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.security.Permissions;

import static com.emlogis.common.security.Permissions.*;

/**
 * Created by Developer on 19.01.2015.
 */
public enum WorkflowRequestTypeDict {

    TIME_OFF_REQUEST(NotificationCategory.TIME_OFF, Availability_Request, Availability_RequestMgmt),
    OPEN_SHIFT_REQUEST(NotificationCategory.OPEN_SHIFTS, Shift_Request, Shift_RequestMgmt),
    SHIFT_SWAP_REQUEST(NotificationCategory.SHIFT_SWAP, Shift_Request, Shift_RequestMgmt),
    WIP_REQUEST(NotificationCategory.WORK_IN_PLACE, Shift_Request, Shift_RequestMgmt),
    AVAILABILITY_REQUEST(NotificationCategory.AVAILABILITY, Availability_Request, Availability_RequestMgmt);

    private final NotificationCategory notificationCategory;
    private final Permissions regularPermission;
    private final Permissions managementPermission;

    WorkflowRequestTypeDict(
            NotificationCategory notificationCategory, //human readable name
            Permissions regularPermission, //permission to create/reply
            Permissions managementpermission //permission to act manager's actions
    ) {
        this.notificationCategory = notificationCategory;
        this.regularPermission = regularPermission;
        this.managementPermission = managementpermission;
    }

    public Permissions getRegularPermission() {
        return regularPermission;
    }

    public Permissions getManagementPermission() {
        return managementPermission;
    }

    public NotificationCategory getNotificationCategory() {
        return notificationCategory;
    }
}
