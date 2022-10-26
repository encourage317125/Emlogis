package com.emlogis.workflow.enums;

/**
 * Created by alexborlis on 09.02.15.
 */
public enum WorkflowPeerWinnerStrategyDict {
    FIRST_APPLIED("FIRST_APPLIED"),
    MANUAL_SELECT("MANUAL_SELECT");

    private final String name;

    WorkflowPeerWinnerStrategyDict(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
