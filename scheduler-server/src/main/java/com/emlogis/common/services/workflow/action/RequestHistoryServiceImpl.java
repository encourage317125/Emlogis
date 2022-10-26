package com.emlogis.common.services.workflow.action;

import com.emlogis.model.workflow.dto.action.InstanceLog;
import com.emlogis.model.workflow.dto.action.InstanceLogItem;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;

import javax.ejb.Local;
import javax.ejb.Stateless;

import static com.emlogis.common.EmlogisUtils.deserializeObject;
import static com.emlogis.common.EmlogisUtils.serializeObject;

/**
 * Created by user on 13.07.15.
 */
@Stateless
@Local(RequestHistoryService.class)
public class RequestHistoryServiceImpl implements RequestHistoryService {

    @Override
    public byte[] log(WorkflowRequestLog action) {
        byte[] oldHistory = action.getProcessInstance().getHistory();
        InstanceLog instanceLog = null;
        if (oldHistory != null) {
            instanceLog = (InstanceLog) deserializeObject(oldHistory);
        } else {
            instanceLog = new InstanceLog();
        }
        InstanceLogItem instanceLogItem = new InstanceLogItem(action);
        instanceLog.add(instanceLogItem);
        return serializeObject(instanceLog);
    }

}
