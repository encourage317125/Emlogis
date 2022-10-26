package com.emlogis.common.services.workflow.process.update.postprocess;

import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.action.RequestHistoryService;
import com.emlogis.common.services.workflow.action.RequestLogService;
import com.emlogis.common.services.workflow.manager.WorkflowRequestManagerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.cleanup.RequestCleanUpService;
import com.emlogis.common.services.workflow.process.update.description.RequestDescriptionProcessor;
import com.emlogis.common.services.workflow.process.update.logging.RequestLogManager;
import com.emlogis.common.services.workflow.process.update.logging.RequestLogResult;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.api.notification.WorkflowNotificationFacade;
import org.apache.log4j.Logger;

import javax.ejb.*;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowRoleDict.*;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.ACTION_COMPLETE_SUCCESS;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.ACTION_COMPLETE_WITH_ERRORS;

/**
 * Created by user on 19.08.15.
 */
@Stateless
@Local(ActionPostProcessor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ActionPostProcessorImpl implements ActionPostProcessor {

    private final static Logger logger = Logger.getLogger(ActionPostProcessorImpl.class);

    @EJB
    private WorkflowNotificationFacade workflowNotificationFacade;

    @EJB
    private RequestLogService requestLogService;

    @EJB
    private WorkflowRequestService wrService;

    @EJB
    private RequestHistoryService requestHistoryService;

    @EJB
    private WorkflowRequestManagerService workflowRequestManagerService;

    @EJB
    private RequestLogManager rlts;

    @EJB
    private RequestDescriptionProcessor requestDescriptionProcessor;

    @EJB
    private RequestCleanUpService cleanUpService;

    @EJB
    private WorkflowRequestTranslator translator;

    @Override
    public WorkflowRequest processActionStatusCommentaryCleanupNotification(
            WorkflowRequest request,
            ResultPair resultPair,
            PrimaryKey actorPk,
            String actorName,
            String commentaryStr,
            Boolean notify
    ) {
        if (!resultPair.getResult()) {
            TranslationParam[] params = {new TranslationParam("error", resultPair.getMessage())};
            String reason = translator.getMessage(locale(request.getInitiator()),
                    "request.action.system.error", params);
            request.setDeclineReason(reason);
        }
        String shiftId = request.hasShift() ? request.getSubmitterShiftId() : null;
        request = processActionResults(request, resultPair);
        request = updateStatuses(request, resultPair != null ? resultPair : request);
        request = addCommentary(request, currentDateTime(), actorName, commentaryStr);
        request = cleanUpService.cleanUp(request, actorPk);
        if(notify) {
            workflowNotificationFacade.notifyAll(request.getTenantId(), request, shiftId);
        }
        return processDescription(request);
    }

    private WorkflowRequest processDescription(WorkflowRequest request) {
        request.setManagerDescription(requestDescriptionProcessor.process(request, MANAGER));
        request.setSubmitterDescription(requestDescriptionProcessor.process(request, ORIGINATOR));
        if (isSwapOrWip(request)) {
            request.setPeerDescription(requestDescriptionProcessor.process(request, PEER));
        }
        return wrService.update(request);
    }

    @Override
    public WorkflowRequest processStatusCommentaryHistoryCleanupNotification(
            WorkflowRequestLog action,
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment,
            String actorName,
            PrimaryKey actorPk,
            Boolean notify
    ) {
        String shiftId = peer != null ? peer.getPeerShiftId() : (request.hasShift() ? request.getSubmitterShiftId() : null);
        request = updateStatuses(request);
        request = addCommentary(request, currentDateTime(), actorName, comment);
        request = historize(request, action);
        request = cleanUpService.cleanUp(request, actorPk);
        if(notify) {
            workflowNotificationFacade.notifyAll(request.getTenantId(), request, shiftId);
        }
        return processDescription(request);
    }

    @Override
    public WorkflowRequest processCommentaryHistoryCleanupNotification(
            WorkflowRequestLog action,
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String commentaryStr,
            String actorName,
            PrimaryKey actorPk,
            Boolean notify
    ) {
        String shiftId = peer != null ? peer.getPeerShiftId() : (request.hasShift() ? request.getSubmitterShiftId() : null);

        request = addCommentary(request, currentDateTime(), actorName, commentaryStr);
        request = historize(request, action);
        request = cleanUpService.cleanUp(request, actorPk);
        if(notify) {
            workflowNotificationFacade.notifyAll(request.getTenantId(), request, shiftId);
        }
        return processDescription(request);
    }

    @Override
    public WorkflowRequest processCommentaryHistoryCleanupNotification(
            WorkflowRequestLog action,
            WorkflowRequest request,
            String actorName,
            PrimaryKey actorPk,
            String comment,
            Boolean notify
    ) {
        request = addCommentary(request, currentDateTime(), actorName, comment);
        request = historize(request, action);
        request = cleanUpService.cleanUp(request, actorPk);
        if(notify) {
            workflowNotificationFacade.notifyAll(request.getTenantId(), request, request.getSubmitterShiftId());
        }
        return processDescription(request);
    }


    @Override
    public WorkflowRequest processActionResults(
            WorkflowRequest request,
            ResultPair resultPair
    ) {
        RequestLogResult lr = rlts.systemAction(request, resultPair);
        request = lr.getRequest();
        request = recalculatePeersStatuses(request);
        request.setStatus(resultPair.getResult() ? ACTION_COMPLETE_SUCCESS : ACTION_COMPLETE_WITH_ERRORS);
        request.setRequestStatus(getRequestStatus(request));
        request = historize(request, lr.getLog());
        request = processDescription(request);
        return cleanUpService.cleanUp(request, request.getInitiator().getPrimaryKey());
    }


    public WorkflowRequest updateStatuses(WorkflowRequest request, Object result) {
        if (result instanceof WorkflowRequest) {
            /**
             * case of peer approve/decline and originator proceed
             */
            WorkflowRequest instance = (WorkflowRequest) result;
            instance.setRequestStatus(getRequestStatus(instance));
            instance = recalculatePeersStatuses(instance);
            return wrService.update(instance);
        } else {
            /**
             * case of actions
             */
            request.setRequestStatus(getRequestStatus(request));
            return wrService.update(recalculatePeersStatuses(request));
        }
    }

    @Override
    public WorkflowRequest updateStatuses(WorkflowRequest request) {
        request.setRequestStatus(getRequestStatus(request));
        processDescription(request);
        return wrService.update(recalculatePeersStatuses(request));

    }

    /**
     * Method is enghancing request history record
     *
     * @param request
     * @param logItem
     * @return
     */
    private WorkflowRequest historize(WorkflowRequest request, WorkflowRequestLog logItem) {
        if (logItem != null) {
            request.setHistory(requestHistoryService.log(logItem));
        }
        return wrService.update(request);
    }

}
