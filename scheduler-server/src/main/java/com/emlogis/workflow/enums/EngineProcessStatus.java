package com.emlogis.workflow.enums;

import java.util.HashMap;

/**
 * Created by alexborlis on 18.02.15.
 */
public class EngineProcessStatus {

    public static final HashMap<Integer, String> statuses = new HashMap<>();

    static {
        statuses.put(0, "STATE_PENDING");
        statuses.put(1, "STATE_ACTIVE");
        statuses.put(2, "STATE_COMPLETED");
        statuses.put(2, "STATE_ABORTED");
        statuses.put(3, "STATE_SUSPENDED");
    }

    public static String processStatus(Integer id) {
        return statuses.get(id);
    }

}
