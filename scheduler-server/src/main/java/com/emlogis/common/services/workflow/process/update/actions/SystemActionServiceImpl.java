package com.emlogis.common.services.workflow.process.update.actions;

import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.callables.*;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.*;
import com.emlogis.common.services.workflow.process.update.postprocess.ActionPostProcessor;
import com.emlogis.common.services.workflow.process.update.qualify.QualificationResult;
import com.emlogis.common.services.workflow.process.update.qualify.RequestActionValidateManager;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.workflow.dto.decision.ShiftDecisionAction;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static com.emlogis.common.Constants.WORKFLOW_ACTION_EXECUTION_TIMEOUT;
import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.findLatestAppropriateComment;
import static java.lang.System.getProperty;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Created by user on 19.08.15.
 */
@Stateless
@Local(SystemActionService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SystemActionServiceImpl implements SystemActionService {

    private final static Logger logger = Logger.getLogger(SystemActionServiceImpl.class);

    //proxies
    @EJB
    private ShiftSwapActionProxy shiftSwapActionProxy;
    @EJB
    private OpenShiftActionProxy openShiftActionProxy;
    @EJB
    private WipActionProxy wipActionProxy;
    @EJB
    private TimeOffAssignActionProxy timeOffAssignActionProxy;
    @EJB
    private DropShiftActionProxy dropShiftActionProxy;
    @EJB
    private PostOpenShiftsActionProxy postOpenShiftsActionProxy;
    @EJB
    private AvailabilityActionProxy availabilityActionProxy;
    @EJB
    private TimeOffNoActionProxy timeOffNoActionProxy;

    @EJB
    private RequestActionValidateManager actionValidateService;
    @EJB
    private ActionPostProcessor postProcessService;
    @EJB
    private WorkflowRequestService wrService;
    @EJB
    private EmployeeService employeeService;
    @EJB
    private ShiftService shiftService;
    @EJB
    private ScheduleService scheduleService;
    @EJB
    private AbsenceTypeService absenceTypeService;

    @EJB
    private WorkflowRequestTranslator translator;

    @Override
    public WorkflowRequest openShiftAction(
            Boolean isAutoApproval,
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            ExecutorService executorService = newSingleThreadExecutor();
            PrimaryKey schedulePk = new PrimaryKey(request.getTenantId(),
                    qualify.getShiftPostedPair().getShift().getScheduleId());
            Callable<ResultPair> thread = new OpenShiftActionThread( isAutoApproval,
                    openShiftActionProxy, schedulePk,
                    qualify.getShiftPostedPair().getShift().getPrimaryKey(),
                    request.getInitiator().getPrimaryKey(), false, actorName,
                    findLatestAppropriateComment(request, translator), request.getPrimaryKey(), actorPk);
            Future<ResultPair> future = executorService.submit(thread);
            postProcessService.processActionStatusCommentaryCleanupNotification(request,
                    processFutureCallback(future, executorService), actorPk, actorName, comment, true);
        }
        return request;
    }

    @Override
    public WorkflowRequest shiftSwapAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    ) {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            Shift shiftA = shift(request.getInitiator().getTenantId(), request.getSubmitterShiftId());
            Shift shiftB = shift(request.getTenantId(), peer.getPeerShiftId());
            PrimaryKey schedulePk = new PrimaryKey(request.getTenantId(), shiftA.getScheduleId());
            ExecutorService executorService = newSingleThreadExecutor();
            Callable<ResultPair> thread = new ShiftSwapActionThread(
                    shiftSwapActionProxy, schedulePk, shiftA.getPrimaryKey(), shiftB.getPrimaryKey(), false,
                    findLatestAppropriateComment(request, translator), request.getPrimaryKey(), actorPk);
            Future<ResultPair> future = executorService.submit(thread);
            return postProcessService.processActionStatusCommentaryCleanupNotification(request,
                    processFutureCallback(future, executorService), actorPk, actorName, comment, true);
        }
        return request;
    }

    @Override
    public WorkflowRequest wipAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    ) {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            Shift shift = shift(request.getInitiator().getTenantId(), request.getSubmitterShiftId());
            PrimaryKey schedulePk = new PrimaryKey(request.getTenantId(), shift.getScheduleId());
            ExecutorService executorService = newSingleThreadExecutor();
            Callable<ResultPair> thread = new WipActionThread(wipActionProxy, schedulePk, shift.getPrimaryKey(),
                    peer.getRecipient().getPrimaryKey(), false, findLatestAppropriateComment(request, translator),
                    request.getPrimaryKey(), actorPk);
            Future<ResultPair> future = executorService.submit(thread);
            return postProcessService.processActionStatusCommentaryCleanupNotification(request,
                    processFutureCallback(future, executorService),
                    actorPk, actorName, comment, true);
        }
        return request;
    }

    @Override
    public WorkflowRequest availabilityAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            ExecutorService executorService = newSingleThreadExecutor();
            Callable<ResultPair> thread = new AvailabilityActionThread(availabilityActionProxy, request);
            Future<ResultPair> future = executorService.submit(thread);
            return postProcessService.processActionStatusCommentaryCleanupNotification(request,
                    processFutureCallback(future, executorService),
                    actorPk, actorName, comment, true);
        }
        return request;
    }

    private ResultPair processFutureCallback(Future<ResultPair> future, ExecutorService executorService) {
        ResultPair resultPair = new ResultPair(false, "unknown error");
        try {
            Long timeout;
            String timeoutStr = getProperty(WORKFLOW_ACTION_EXECUTION_TIMEOUT);
            if (timeoutStr != null) {
                timeout = Long.valueOf(timeoutStr);
            } else {
                timeout = 100l;
            }
            resultPair = future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            try {
                executorService.shutdown();
            } catch (Exception error) {
                //todo:: okay
                logger.info("Execution interrupted");
            }
            resultPair = new ResultPair(false, "Timeout fail");
        } finally {
            return resultPair;
        }
    }

    private AbsenceType absenceType(WorkflowRequest request) {
        TimeOffRequestInfoDto timeOffRequestInfoDto = fromJsonString(request.getData(), TimeOffRequestInfoDto.class);
        AbsenceType absenceType = absenceTypeService.getAbsenceType(new PrimaryKey(request.getTenantId(),
                timeOffRequestInfoDto.getAbsenceTypeId()));
        return absenceType;
    }

    @Override
    public WorkflowRequest dropShift(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            Shift shift = shift(request.getInitiator().getTenantId(), decisionAction.getShiftId());
            ExecutorService executorService = newSingleThreadExecutor();
            PrimaryKey schedulePk = new PrimaryKey(shift.getTenantId(), shift.getScheduleId());
            Callable<ResultPair> thread = new DropShiftActionThread(dropShiftActionProxy,
                    schedulePk, shift.getPrimaryKey(), request.getInitiator(), absenceType(request),
                    request.getRequestDate(), findLatestAppropriateComment(request, translator), request.getPrimaryKey(), actorPk);
            Future<ResultPair> future = executorService.submit(thread);
            ResultPair resultPair = processFutureCallback(future, executorService);
            return postProcessService.processActionStatusCommentaryCleanupNotification(request, resultPair, actorPk, actorName, comment, true);
        }
        return request;
    }

    @Override
    public WorkflowRequest assignShiftTo(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            Employee assignee = employeeService.getEmployee(new PrimaryKey(request.getTenantId(), decisionAction.getEmployeeIds().get(0)));
            Shift shift = shift(request.getInitiator().getTenantId(), decisionAction.getShiftId());
            ExecutorService executorService = newSingleThreadExecutor();
            PrimaryKey schedulePk = new PrimaryKey(request.getTenantId(), shift.getScheduleId());
            Callable<ResultPair> thread = new TimeOffAssignActionThread(timeOffAssignActionProxy, schedulePk, shift.getPrimaryKey(),
                    assignee.getPrimaryKey(), false, request.getInitiator(), absenceType(request), request.getRequestDate(),
                    findLatestAppropriateComment(request, translator), request.getPrimaryKey(), actorPk);
            Future<ResultPair> future = executorService.submit(thread);
            ResultPair resultPair = processFutureCallback(future, executorService);
            return postProcessService.processActionStatusCommentaryCleanupNotification(request, resultPair, actorPk, actorName, comment, true);
        }
        return request;
    }

    @Override
    public WorkflowRequest postOpenShifts(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            wrService.declineOtherRequestsOnThatOpenShift(request, actorPk);
            Shift shift = shift(request.getInitiator().getTenantId(), decisionAction.getShiftId());
            ExecutorService executorService = newSingleThreadExecutor();
            com.emlogis.model.schedule.Schedule schedule = schedule(request.getTenantId(), shift.getScheduleId());
            Callable<ResultPair> thread = new PostOpenShiftsActionThread(postOpenShiftsActionProxy, schedule,
                    parseDecisionIntoMap(decisionAction), shift.getStartDateTime(), comment, request.getInitiator(),
                    absenceType(request), request.getRequestDate(), request.getInitiatorComment());
            Future<ResultPair> future = executorService.submit(thread);
            ResultPair resultPair = processFutureCallback(future, executorService);
            return postProcessService.processActionStatusCommentaryCleanupNotification(request, resultPair, actorPk, actorName, comment, true);
        }
        return request;
    }

    @Override
    public WorkflowRequest timeOffWithoutAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) {
        QualificationResult qualify = actionValidateService.qualify(request);
        if (qualify.getResult()) {
            request = qualify.getRequest();
            ExecutorService executorService = newSingleThreadExecutor();
            Callable<ResultPair> thread = new TimeOffNoActionThread(request.getInitiator(), absenceType(request),
                    request.getRequestDate(), request.getInitiatorComment(), timeOffNoActionProxy);
            Future<ResultPair> future = executorService.submit(thread);
            ResultPair resultPair = processFutureCallback(future, executorService);
            return postProcessService.processActionStatusCommentaryCleanupNotification(request, resultPair,
                    actorPk, actorName, comment, true);
        }
        return request;
    }

    private Map<String, Collection<String>> parseDecisionIntoMap(
            ShiftDecisionAction decisionAction
    ) {
        Map<String, Collection<String>> result = new HashMap<>();
        result.put(decisionAction.getShiftId(), decisionAction.getEmployeeIds());
        return result;
    }

    private Shift shift(String tenantId, String id) {
        return shiftService.getShift(new PrimaryKey(tenantId, id));
    }

    private com.emlogis.model.schedule.Schedule schedule(String tenantId, String id) {
        return scheduleService.getSchedule(new PrimaryKey(tenantId, id));
    }
}
