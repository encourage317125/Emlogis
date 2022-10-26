package com.emlogis.workflow.enums.status;

/**
 * Created by Developer on 19.01.2015.
 */
public enum RequestTechnicalStatusDict {
    PROCESS_INITIATED("Initiated"),
    APPROVED("Manager approved"),
    REMOVED("Removed by originator"),
    FAILED_TO_SUBMIT("Failed"),
    EXPIRED("Expired"),
    DECLINED_BY_PEERS("Peers declined"),
    DECLINED_BY_MANAGERS("Manager declined"),
    READY_FOR_ACTION("Action pending"),
    READY_FOR_ADMIN("Needs manager"),
    ACTION_IN_PROGRESS("Action in progress"),
    ACTION_COMPLETE_SUCCESS("Complete"),
    ACTION_COMPLETE_WITH_ERRORS("Complete with error"),
    TERMINATED("Request execution terminated");

    private final String description;

    RequestTechnicalStatusDict(
            String description
    ) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
