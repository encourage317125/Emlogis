package com.emlogis.model.workflow.dto.action;

import java.util.ArrayList;

/**
 * Created by user on 13.07.15.
 */
public class InstanceLog extends ArrayList<InstanceLogItem> {

    @Override
    public boolean add(InstanceLogItem instanceLogItem) {
        if (!this.contains(instanceLogItem)) {
            return super.add(instanceLogItem);
        }
        return true;
    }
}
