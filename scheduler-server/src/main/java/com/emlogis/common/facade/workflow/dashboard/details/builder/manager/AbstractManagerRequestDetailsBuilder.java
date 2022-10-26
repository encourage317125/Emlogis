package com.emlogis.common.facade.workflow.dashboard.details.builder.manager;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.workflow.dashboard.details.builder.AbstractRequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.action.InstanceLog;
import com.emlogis.model.workflow.dto.commentary.RequestCommentary;
import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestManager;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

import static com.emlogis.common.EmlogisUtils.deserializeObject;
import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by user on 21.08.15.
 */
@Stateless
public abstract class AbstractManagerRequestDetailsBuilder<ReturnType extends DetailedManagerRequestDetailsDto>
        extends AbstractRequestDetailsBuilder<ReturnType>
        implements RequestDetailsBuilder<ReturnType> {

    @EJB
    private UserAccountService userAccountService;


    protected UserAccount account(PrimaryKey pk) {
        return userAccountService.getUserAccount(pk);
    }

    protected DetailedManagerRequestDetailsDto detailedBaseTaskInfo(
            WorkflowRequest request,
            UserAccount account
    ) throws WorkflowServerException {
        Boolean isRead = null;
        if (request.getRequestStatus().isFinalState()) {
            isRead = true;
        } else {
            isRead = workflowRequestManagerService.isReadForManager(request, account);
            if (isRead == null) {
                if (requestRoleProxy.validateIsManager(request.getRequestType(),
                        account, request.getInitiator())) {
                    WorkflowRequestManager workflowRequestManager =
                            new WorkflowRequestManager(request, account);
                    workflowRequestManager.setIsRead(false);
                    workflowRequestManager.setRequestStatus(getRequestStatus(request));
                    request.getManagers().add(workflowRequestManager);
                    List<UserAccount> managers = requestRoleProxy.findManagers(
                            request.getRequestType(),
                            request.getInitiator());
                    request.setManagerIds(managerIds(managers));
                    request.setManagerNames(managerNames(managers));
                    service.update(request);
                    isRead = false;
                } else {
                    String message = translator.getMessage(locale(account),
                            "request.employee.is.not.manager", null);
                    throw new ValidationException(message);
                }
            }
        }
        /**
         * if there was no appropriate exceptional situation while running asynch action API task
         * find out the latest history comment that belongs to its execution
         */
        String reason = null;
        if (request.getDeclineReason() == null || isEmpty(request.getDeclineReason())) {
            reason = findLatestAppropriateComment(request, translator);
        } else {
            reason = request.getDeclineReason();
        }
        DetailedManagerRequestDetailsDto detailedManagerTaskDto = new DetailedManagerRequestDetailsDto(
                baseTaskInfo(request, WorkflowRoleDict.MANAGER, null),
                managerCanAct(request), getLastActionDate(request), getLastComment(request),
                request.getManagerDescription(), fromJsonString(request.commentary(), RequestCommentary.class),
                (InstanceLog) deserializeObject(request.getHistory()), isRead, reason);

        return detailedManagerTaskDto;
    }


}