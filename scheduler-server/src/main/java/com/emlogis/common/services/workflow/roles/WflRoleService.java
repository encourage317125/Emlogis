package com.emlogis.common.services.workflow.roles;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WflRole;
import com.emlogis.workflow.enums.WorkflowRoleDict;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface WflRoleService extends GeneralJPARepository<WflRole, String> {

    WflRole findByRoleAndProcess(WorkflowRoleDict roleType, WflProcess abstractProcessEntity);

    WflRole merge(WflRole role);

    WflRole findByName(String name);

    WflRole merge(WorkflowRoleDict roleDict, WflProcess process);
}
