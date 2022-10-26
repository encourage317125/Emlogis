package com.emlogis.common.facade.workflow.dashboard.details.builder.manager.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.manager.AbstractManagerRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.dto.AvailabilityWorkflowRequest;
import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.manager.impl.AvailabilityManagerRequestDetailsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import javax.enterprise.inject.Default;

import static com.emlogis.common.EmlogisUtils.fromJsonString;

/**
 * Created by user on 21.08.15.
 */
@Stateless
@Local
@Default
@RequestDetailsBuilderQualifier(value = "AVAILABILITY_REQUEST_MANAGER_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AvailabilityManagerDetailsBuilder<AT extends AvailabilityWorkflowRequest>
        extends AbstractManagerRequestDetailsBuilder<AvailabilityManagerRequestDetailsDto>
        implements RequestDetailsBuilder<AvailabilityManagerRequestDetailsDto> {


    @Override
    public AvailabilityManagerRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
        DetailedManagerRequestDetailsDto detailedManagerTaskDto = detailedBaseTaskInfo(request, account(requesterPk));
        AT availBean = (AT) fromJsonString(request.getData(), request.getAvailabilityRequestSubtype().getClazz());
        AvailabilityManagerRequestDetailsDto result = new AvailabilityManagerRequestDetailsDto(
                detailedManagerTaskDto, availBean);
        return result;
    }

    @Override
    public WorkflowRequestTypeDict requestType() {
        return WorkflowRequestTypeDict.AVAILABILITY_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return WorkflowRoleDict.MANAGER;
    }
}
