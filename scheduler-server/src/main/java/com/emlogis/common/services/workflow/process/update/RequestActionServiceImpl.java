package com.emlogis.common.services.workflow.process.update;

import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.actions.EmployeeActionService;
import com.emlogis.common.services.workflow.process.update.actions.SystemActionService;
import com.emlogis.common.services.workflow.process.update.cleanup.RequestCleanUpService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.decision.ShiftDecisionAction;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowRequestDecision;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowRequestDecision.APPROVE;
import static com.emlogis.workflow.enums.WorkflowRoleDict.MANAGER;
import static com.emlogis.workflow.enums.WorkflowRoleDict.PEER;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionManagementType.CONTAINER;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by user on 09.07.15.
 */
@Stateless
@Local(value = RequestActionService.class)
@TransactionAttribute(REQUIRED)
@TransactionManagement(CONTAINER)
public class RequestActionServiceImpl implements RequestActionService {

    private final static Logger logger = Logger.getLogger(RequestActionServiceImpl.class);
    private static final Long SECONDS_PER_ACTION = 3l;

    @EJB
    private EmployeeActionService eas;
    @EJB
    private SystemActionService sas;
    @EJB
    private RequestCleanUpService cleanUpService;
    @EJB
    private HazelcastClientService hazelcastClientService;
    @EJB
    private WorkflowRequestService workflowRequestService;
    @EJB
    private UserAccountService userAccountService;
    @EJB
    private WorkflowRequestTranslator translator;

    private Lock lock(LockKey key) {
        java.util.concurrent.locks.Lock executeLock = hazelcastClientService.getLock(key.toString());
        if (executeLock == null) {
            executeLock = new ReentrantLock();
            hazelcastClientService.putLock(executeLock, key.toString());
        }

        executeLock.lock();
        return executeLock;
    }

    private void release(LockKey key) {
        Lock executeLock = hazelcastClientService.getLock(key.toString());
        if (executeLock != null) {
            executeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Boolean emailAction(
            WorkflowRoleDict role,
            WorkflowRequestDecision decision,
            PrimaryKey requestPk,
            PrimaryKey shiftPk,
            String commentaryStr,
            PrimaryKey userAccountPk
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        UserAccount userAccount = userAccountService.getUserAccount(userAccountPk);
        try {
            if (!isSwapOrWip(request) && role.equals(PEER)) {
                throw new RuntimeException("No peer's actions for " + request.getRequestType().name());
            }
            if (role.equals(PEER)) {
                if (decision.equals(APPROVE)) {
                    WorkflowRequestPeer peer = identifyPeer(request, shiftPk, userAccount);
                    processPeerApprove(request, peer, commentaryStr);
                } else {
                    processPeerDecline(request, identifyPeer(request, shiftPk, userAccount), commentaryStr);
                }
            } else if (role.equals(MANAGER)) {
                if (decision.equals(APPROVE)) {
                    managerApprove(request, commentaryStr, identifyPeer(request, shiftPk, userAccount),
                            userAccount.reportName(), userAccountPk);
                } else {
                    managerDecline(request, commentaryStr, userAccount.reportName(), userAccountPk);
                }
            }
            return Boolean.TRUE;
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest initiatorProceed(
            WorkflowRequest request,
            String comment
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return eas.initiatorProceed(request, comment, true);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: initiatorProceed", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest processPeerApprove(
            WorkflowRequest request, //instance to make action
            WorkflowRequestPeer peer, //peer that makes action
            String comment
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return eas.processPeerApprove(request, peer, comment);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: processPeerApprove", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest managerApprove(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return eas.managerApprove(request, actorName, actorPk, comment, peer);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: managerApprove", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest processPeerDecline(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return eas.processPeerDecline(request, peer, comment);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: processPeerDecline", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest managerDecline(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            TranslationParam[] params = {new TranslationParam("comment", isEmpty(comment) ? "" : comment)};
            String reason = translator.getMessage(locale(request.getInitiator()),
                    "request.manager.decline.reason", params);
            request.setDeclineReason(reason);
            return eas.managerDecline(request, actorName, actorPk, comment);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: managerDecline", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionTerminated(
            String shiftId,
            String reason,
            PrimaryKey actorPk
    ) {
        cleanUpService.cleanUp(shiftId, reason, actorPk);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest openShiftAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.openShiftAction(false, request, comment, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: openShiftAction", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest shiftSwapAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    ) {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.shiftSwapAction(request, comment, peer, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: shiftSwapAction", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest wipAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    ) {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.wipAction(request, comment, peer, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: wipAction", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest availabilityAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.availabilityAction(request, comment, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: availabilityAction", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest dropShift(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.dropShift(request, decisionAction, comment, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: dropShift", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest assignShiftTo(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.assignShiftTo(request, decisionAction, comment, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: assignShiftTo", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest postOpenShifts(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.postOpenShifts(request, decisionAction, comment, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: postOpenShifts", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    @Override
    public WorkflowRequest timeOffWithoutAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) {
        Lock lock = lock(new LockKey(request));
        try {
            lock.tryLock(SECONDS_PER_ACTION, SECONDS);
            return sas.timeOffWithoutAction(request, comment, actorName, actorPk);
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error(" Error in WORKFLOW: timeOffWithoutAction", error);
            throw new RuntimeException(error);
        } finally {
            release(new LockKey(request));
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public WorkflowRequest forcedOpenShiftAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) {
        try {
            request = eas.initiatorProceed(request, comment, false);
            request = sas.openShiftAction(true, request, comment, actorName, actorPk);
            return request;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    private final class LockKey {
        private final String requestId;

        public LockKey(WorkflowRequest request) {
            this.requestId = request.getCode();
        }


        public String getRequestId() {
            return requestId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LockKey) {
                LockKey other = (LockKey) obj;
                EqualsBuilder builder = new EqualsBuilder();
                builder.append(getRequestId(), other.getRequestId());
                return builder.isEquals();
            }
            return false;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(getRequestId());
            return builder.toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).
                    append("requestId", getRequestId()).
                    toString();
        }
    }

}
