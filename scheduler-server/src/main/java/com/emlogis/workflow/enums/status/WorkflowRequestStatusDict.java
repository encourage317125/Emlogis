package com.emlogis.workflow.enums.status;

/**
 * Created by lucas on 01.06.2015.
 */
public enum WorkflowRequestStatusDict {
    UNKNOWN(false, false, 0l),
	PEER_PENDING(false, true, 1l),
    ADMIN_PENDING(false, true, 2l),
    WITHDRAWN(true, false, 3l),
    PEER_DECLINED(true, false, 4l),
    PEER_APPROVED(false, false, 5l),
    DELETED(true, false, 6l),
    DECLINED(true, false, 7l),
    APPROVED(true, false, 8l),
    EXPIRED(true, false, 9l);

    private final Boolean isFinalState;
    private final Boolean isPending;
    private final Long weight;

    WorkflowRequestStatusDict(
            Boolean isFinalState,
            Boolean isPending,
            Long weight
    ) {
        this.isFinalState = isFinalState;
        this.isPending = isPending;
        this.weight = weight;
    }

    public Boolean isFinalState() {
        return isFinalState;
    }

    public Boolean isPending(){
        return this.isPending;
    }

    public Long weight() {
        return weight;
    }
}


