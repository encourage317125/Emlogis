package com.emlogis.common.facade.workflow.dashboard.details.builder.submitter;

import com.emlogis.common.facade.workflow.dashboard.details.builder.AbstractRequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.action.InstanceLog;
import com.emlogis.model.workflow.dto.commentary.RequestCommentary;
import com.emlogis.model.workflow.dto.details.submitter.DetailedSubmitterRequestDetailsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

import javax.ejb.Stateless;

import static com.emlogis.common.EmlogisUtils.deserializeObject;
import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by user on 21.08.15.
 */
@Stateless
public abstract class AbstractSubmitterRequestDetailsBuilder<ReturnType extends DetailedSubmitterRequestDetailsDto>
        extends AbstractRequestDetailsBuilder<ReturnType>
        implements RequestDetailsBuilder<ReturnType> {



    protected DetailedSubmitterRequestDetailsDto detailedBaseTaskInfo(WorkflowRequest request, Employee employee) throws WorkflowServerException {
       // String desc = description(fromJsonString(request.getDescription(), RequestDescriptionDto.class));
        Boolean isRead;
        if (request.getRequestStatus().isFinalState()) {
            isRead = true;
        } else {
            isRead = workflowRequestManagerService.isReadForSubmitter(request, employee);
        }

        String reason = null;
        if (request.getDeclineReason() == null || isEmpty(request.getDeclineReason())) {
            reason = findLatestAppropriateComment(request, translator);
        } else {
            reason = request.getDeclineReason();
        }

        DetailedSubmitterRequestDetailsDto detailedManagerTaskDto = new DetailedSubmitterRequestDetailsDto(
                baseTaskInfo(request, WorkflowRoleDict.ORIGINATOR, employee),
                managerCanAct(request), getLastActionDate(request), getLastComment(request),
                request.getSubmitterDescription(), fromJsonString(request.commentary(), RequestCommentary.class),
                (InstanceLog) deserializeObject(request.getHistory()), isRead, reason);

        return detailedManagerTaskDto;
    }
}
