package com.emlogis.common.services.workflow.process.update.description;

import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.description.annotations.RequestDescriptionBuilderQualifierImpl;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by user on 03.09.15.
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestDescriptionProcessor {

    @Inject
    private Instance<DescriptionBuilder> descriptionBuilder;


    public String process(
            WorkflowRequest request,
            WorkflowRoleDict role
    ) {
        DescriptionBuilder service = descriptionBuilder.select(new RequestDescriptionBuilderQualifierImpl(
                request.getRequestType(), role)).get();
        return service.build(request, request.getSubmitterTz(), request.locale());
    }
}
