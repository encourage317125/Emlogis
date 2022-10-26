package com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.AbstractSubmitterRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.AvailabilityWorkflowRequest;
import com.emlogis.model.workflow.dto.details.submitter.DetailedSubmitterRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.submitter.impl.AvailabilitySubmitterRequestDetailsDto;
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
@RequestDetailsBuilderQualifier(value = "AVAILABILITY_REQUEST_ORIGINATOR_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AvailabilitySubmitterDetailsBuilder<AT extends AvailabilityWorkflowRequest>
        extends AbstractSubmitterRequestDetailsBuilder<AvailabilitySubmitterRequestDetailsDto>
        implements RequestDetailsBuilder<AvailabilitySubmitterRequestDetailsDto> {


        @Override
        public AvailabilitySubmitterRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
                Employee employee = employee(requesterPk);
                DetailedSubmitterRequestDetailsDto detailedManagerTaskDto = detailedBaseTaskInfo(request, employee);
                AT availBean = (AT) fromJsonString(request.getData(), request.getAvailabilityRequestSubtype().getClazz());
                AvailabilitySubmitterRequestDetailsDto result = new AvailabilitySubmitterRequestDetailsDto(
                        detailedManagerTaskDto, availBean);
                return result;
        }

        @Override
        public WorkflowRequestTypeDict requestType() {
                return WorkflowRequestTypeDict.AVAILABILITY_REQUEST;
        }

        @Override
        public WorkflowRoleDict role() {
                return WorkflowRoleDict.ORIGINATOR;
        }
}
