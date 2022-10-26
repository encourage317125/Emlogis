package com.emlogis.common.facade.workflow.dashboard.details;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.details.WorkflowAvailabilitySettingsDto;
import com.emlogis.model.workflow.dto.details.abstracts.RequestDetailsInfo;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 21.08.15.
 */
public interface RequestDetailsFacade {


    /**
     * @param requesterPk
     * @param request
     * @param role
     * @return
     * @throws WorkflowServerException
     */
    RequestDetailsInfo getRequestDetails(
            PrimaryKey requesterPk,
            WorkflowRequest request,
            WorkflowRoleDict role
    ) throws WorkflowServerException;

    /**
     * Method returns DTo data about existing availability request
     *
     * @param tenantId  - tenant identifier
     * @param requestId - workflow request identifier
     * @return - {@link WorkflowAvailabilitySettingsDto} DTo with data
     */
    WorkflowAvailabilitySettingsDto getAvailabilityRequestSettings(
            String tenantId,
            String requestId
    );
}
