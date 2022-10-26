package com.emlogis.common.facade.workflow.dashboard.details.builder.peer;

import com.emlogis.common.facade.workflow.dashboard.details.builder.AbstractRequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.commentary.RequestCommentary;
import com.emlogis.model.workflow.dto.details.peer.DetailedPeerRequestDetailsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

import javax.ejb.Stateless;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.*;

/**
 * Created by user on 21.08.15.
 */
@Stateless
public abstract class AbstractPeerRequestDetailsBuilder<ReturnType extends DetailedPeerRequestDetailsDto>
        extends AbstractRequestDetailsBuilder<ReturnType>
        implements RequestDetailsBuilder<ReturnType> {

    protected DetailedPeerRequestDetailsDto detailedBaseTaskInfo(
            WorkflowRequest request,
            Employee employee
    ) throws WorkflowServerException {
       // String desc = description(fromJsonString(request.getDescription(), RequestDescriptionDto.class));
        Boolean isRead = workflowRequestPeerService.isRead(request.getId(), employee);
        DetailedPeerRequestDetailsDto detailedManagerTaskDto = new DetailedPeerRequestDetailsDto(
                baseTaskInfo(request, WorkflowRoleDict.PEER, employee),
                peerCanAct(request, employee), getLastActionDate(request), getLastComment(request),
                request.getPeerDescription(), fromJsonString(request.commentary(), RequestCommentary.class), isRead);

        return detailedManagerTaskDto;
    }
}
