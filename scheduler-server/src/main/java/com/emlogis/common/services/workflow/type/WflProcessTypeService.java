package com.emlogis.common.services.workflow.type;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.workflow.entities.WflProcessType;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface WflProcessTypeService extends GeneralJPARepository<WflProcessType, String> {

    WflProcessType findByDictionary(WorkflowRequestTypeDict dictionary);

    WflProcessType merge(WflProcessType pto);

    WflProcessType findByName(String name);
}
