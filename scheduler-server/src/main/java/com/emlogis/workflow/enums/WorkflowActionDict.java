package com.emlogis.workflow.enums;

/**
 * Created by alexborlis on 15.02.15.
 */
public enum WorkflowActionDict {
    ACTION_IN_PROGRESS,
    ORIGINATOR_PROCEED,
    ORIGINATOR_REMOVED,
    PEER_APPROVE,
    PEER_DECLINE,
    MANAGER_APPROVE,
    MANAGER_DECLINE,
    ERROR_IN_ACTION,
    ACTION_SUCCESS,
    PROCESS_TERMINATED,
    PEER_CANCELLED
}
