package com.emlogis.common.services.workflow.process.update.qualify;

import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;

import javax.ejb.*;

import static com.emlogis.workflow.WflUtil.*;

/**
 * Created by user on 19.08.15.
 */
@Stateless
@Local(RequestActionValidateManager.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestActionValidateManagerImpl implements RequestActionValidateManager {

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    @EJB
    private WorkflowRequestService wrService;

    @EJB
    private ScheduleService scheduleService;

    @EJB
    private ShiftService shiftService;

    /**
     * {@inheritDoc}
     */
    public QualificationResult qualify(
            WorkflowRequest request
    ) {
        QualificationResult result = new QualificationResult(request);
        switch (request.getRequestType()) {
            case TIME_OFF_REQUEST: {
                result.setResult(Boolean.TRUE);
                break;
            }
            case AVAILABILITY_REQUEST: {
                result.setResult(Boolean.TRUE);
                break;
            }
            case SHIFT_SWAP_REQUEST: {
                result.setResult(Boolean.TRUE);
                break;
            }
            case WIP_REQUEST: {
                result.setResult(Boolean.TRUE);
                break;
            }
            case OPEN_SHIFT_REQUEST: {
                PostedOpenShiftService.ShiftPostedPair spp = preQualifyOpenShifts(request);
                result.setResult(spp != null);
                result.setShiftPostedPair(spp);
                break;
            }
        }
        if (result.getResult()) {
            result.setRequest(startActionProcess(request));
        }
        return result;
    }

    private WorkflowRequest startActionProcess(WorkflowRequest request) {
        request.setStatus(RequestTechnicalStatusDict.ACTION_IN_PROGRESS);
        request.setRequestStatus(getRequestStatus(request));
        request = recalculatePeersStatuses(request);
        request = wrService.update(request);
        return request;
    }

    private PostedOpenShiftService.ShiftPostedPair preQualifyOpenShifts(
            WorkflowRequest request
    ) {
        PostedOpenShiftService.ShiftPostedPair spp = postedOpenShiftService.checkShift(
                request.getInitiator().getPrimaryKey(), request.getSubmitterShiftId());
        if (spp == null) {
            request.setStatus(RequestTechnicalStatusDict.TERMINATED);
            request.setRequestStatus(getRequestStatus(request));
            for (WorkflowRequestPeer peer : request.getRecipients()) {
                peer.setPeerStatus(getPeerAggregatedRequestStatus(request, peer));
            }
            request.setDeclineReason("Shift used in " + request.getRequestType().name());
            request = wrService.update(request);
            WorkflowRequestLog logItem = new WorkflowRequestLog(request,
                    WorkflowActionDict.PROCESS_TERMINATED, WorkflowRoleDict.MANAGER, request.getInitiator().getId(),
                    request.hasShift() ? request.getSubmitterShiftId() : null,
                    "Shift used in " + request.getRequestType().name());
            request.getActions().add(logItem);
            request = wrService.update(request);
            return null;
        }
//        else {
//            return spp;
////            PrimaryKey schedulePk = new PrimaryKey(request.getTenantId(), spp.getShift().getScheduleId());
////            Schedule submitterSchedule = scheduleService.getSchedule(schedulePk);
////            Collection<Shift> shifts = shiftService.getEmployeeScheduleShifts(submitterSchedule, request.getInitiator());
////            if (shifts.isEmpty()) {
////                request.setStatus(RequestTechnicalStatusDict.TERMINATED);
////                request.setRequestStatus(getRequestStatus(request));
////                for (WorkflowRequestPeer peer : request.getRecipients()) {
////                    peer.setPeerStatus(getPeerAggregatedRequestStatus(request, peer));
////                }
////                request = wrService.update(request);
////                WorkflowRequestLog logItem = new WorkflowRequestLog(request,
////                        WorkflowActionDict.PROCESS_TERMINATED, WorkflowRoleDict.MANAGER, request.getInitiator().getId(),
////                        request.hasShift() ? request.getSubmitterShiftId() : null,
////                        "Shift used in " + request.getRequestType().name());
////                request.getActions().add(logItem);
////                request = wrService.update(request);
////                return null;
////            }
//        }
        return spp;
    }

}
