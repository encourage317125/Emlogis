package com.emlogis.common.facade.workflow.process.submition.builder.request;

import com.emlogis.common.facade.workflow.process.submition.builder.request.impl.*;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.submit.*;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.entities.WflProcess;

import javax.ejb.*;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkflowRequestProducer<Type extends SubmitDto> {

    @EJB
    private ShiftSwapRequestBuilder shiftSwapRequestBuilder;

    @EJB
    private WorkInPlaceRequestBuilder workInPlaceRequestBuilder;

    @EJB
    private OpenShiftsRequestBuilder openShiftsRequestBuilder;

    @EJB
    private AvailabilityRequestBuilder availabilityRequestBuilder;

    @EJB
    private TimeOffRequestBuilder timeOffRequestBuilder;

    public SubmitRequestResultDto produce(Type req, WflProcess parent, UserAccount userAccount) {
        switch (parent.getType().getType()) {
            case AVAILABILITY_REQUEST: {
                return availabilityRequestBuilder.build((AvailabilitySubmitDto) req, parent, userAccount);
            }

            case TIME_OFF_REQUEST: {
                return timeOffRequestBuilder.build((TimeOffSubmitDto) req, parent, userAccount);
            }

            case SHIFT_SWAP_REQUEST: {
                return shiftSwapRequestBuilder.build((ShiftSwapSubmitDto) req, parent, userAccount);
            }

            case OPEN_SHIFT_REQUEST: {
                return openShiftsRequestBuilder.build((OpenShiftSubmitDto) req, parent, userAccount);
            }

            case WIP_REQUEST: {
                return workInPlaceRequestBuilder.build((WorkInPlaceSubmitDto) req, parent, userAccount);
            }
        }
        throw new RuntimeException("Unknown request type");
    }

}
