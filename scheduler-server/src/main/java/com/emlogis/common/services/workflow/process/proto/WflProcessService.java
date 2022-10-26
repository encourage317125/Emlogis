package com.emlogis.common.services.workflow.process.proto;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface WflProcessService extends GeneralJPARepository<WflProcess, PrimaryKey> {

    WflProcess findByTypeAndOrganization(WorkflowRequestTypeDict type, String tenantId);

    WflProcess merge(WflProcess process);
}
