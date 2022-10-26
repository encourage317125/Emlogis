package com.emlogis.model.workflow.entities;

/**
 * Created by user on 04.09.15.
 */
public enum RequestNotificationStatus {
    PENDING,
    QUEUED,
    PROCESSED,
    FAILED,
    NOT_QUALIFIED,
    DUPLICATION;

    public String identifier() {
        return "_" + this.name();
    }

}
