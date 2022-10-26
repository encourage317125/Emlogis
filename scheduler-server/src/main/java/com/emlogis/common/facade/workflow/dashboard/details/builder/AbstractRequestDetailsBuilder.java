package com.emlogis.common.facade.workflow.dashboard.details.builder;

import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.facade.workflow.roles.WorkflowProcessWinnerFacade;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.manager.WorkflowRequestManagerService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.emlogis.model.workflow.dto.task.TaskRecipientBriefInfoDto;
import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.rest.security.SessionService;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

import javax.ejb.EJB;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Set;

import static com.emlogis.workflow.WflUtil.getPeerAggregatedRequestStatus;
import static com.emlogis.workflow.WflUtil.getPeerToPrivilegedStatus;

/**
 * Created by user on 21.08.15.
 */
public abstract class AbstractRequestDetailsBuilder<Type extends AbstractRequestDetailsInfoDto>
        implements RequestDetailsBuilder<Type> {

    @EJB
    ResourcesBundle resourcesBundle;

    @EJB
    SessionService sessionService;

    @EJB
    UserAccountService userAccountService;

    @EJB
    ServiceHelper serviceHelper;

    @EJB
    protected
    WorkflowRequestManagerService workflowRequestManagerService;

    @EJB
    protected
    WorkflowRequestPeerService workflowRequestPeerService;

    @EJB
    protected
    RequestRoleProxy requestRoleProxy;

    @EJB
    protected
    WorkflowRequestTranslator translator;

    @EJB
    protected WorkflowRequestService service;

    @Inject
    private Instance<DescriptionBuilder> descriptionBuilder;

    @EJB
    WorkflowProcessWinnerFacade workflowProcessWinnerFacade;

    @EJB
    ShiftService shiftService;

    @EJB
    EmployeeService employeeService;

    protected Employee employee(PrimaryKey pk) {
        return employeeService.getEmployee(pk);
    }

//    protected String description(
//            RequestDescriptionDto dto,
//            Employee employee
//    ) {
//        RequestDescriptionBuilderQualifierImpl qualifier = new RequestDescriptionBuilderQualifierImpl(
//                requestType(), role());
//        DescriptionBuilder service = descriptionBuilder.select(qualifier).get();
//        return service.build(dto, employee.getSite().getTimeZone(), locale(employee));
//    }

    protected AbstractRequestDetailsInfoDto baseTaskInfo(
            WorkflowRequest request,
            WorkflowRoleDict role,
            Employee employee) throws WorkflowServerException {
        String status;
        if (role.equals(WorkflowRoleDict.MANAGER) | role.equals(WorkflowRoleDict.ORIGINATOR)) {
            status = request.getRequestStatus().name();
        } else {
            status = getPeerAggregatedRequestStatus(request, employee).name();
        }
        AbstractRequestDetailsInfoDto result = new AbstractRequestDetailsInfoDto(
                request.getId(),
                request.getProtoProcess().getType().getType().name(),
                request.getCreated().getMillis(),
                request.getRequestDate(),
                request.getExpiration(),
                shiftInfo(request),
                request.getInitiator().getId(),
                request.getInitiator().reportName(),
                request.getSubmitterTeamName(), request.getSubmitterTeamId(),
                request.getSubmitterSiteName(), request.getSubmitterSiteId(),
                status);
        if (employee != null) {
            if (employee.getStartDate() != null) {
                result.setEmployeeStartDate(employee.getStartDate().toDate().getTime());
            }
        }

        return result;
    }

    public TaskShiftBriefInfoDto shiftInfo(WorkflowRequest instance) {
        return new TaskShiftBriefInfoDto(
                instance.getSubmitterShiftId(),
                instance.getSubmitterShiftStartDateTime(),
                instance.getSubmitterShiftEndDateTime(),
                instance.getSubmitterTeamId(),
                instance.getSubmitterTeamName(),
                instance.getSubmitterShiftSkillId(),
                instance.getSubmitterShiftSkillName());
    }

    public TaskShiftBriefInfoDto shiftInfo(WorkflowRequestPeer peerInstance) {
        return new TaskShiftBriefInfoDto(
                peerInstance.getPeerShiftId(),
                peerInstance.getPeerShiftStartDateTime(),
                peerInstance.getPeerShiftEndDateTime(),
                peerInstance.getPeerShiftTeamId(),
                peerInstance.getPeerTeamName(),
                peerInstance.getPeerShiftSkillId(),
                peerInstance.getPeerShiftSkillName());
    }

    protected TaskRecipientBriefInfoDto peerBaseInfo(WorkflowRequestPeer peerInstance) throws WorkflowServerException {
        TaskRecipientBriefInfoDto result = new TaskRecipientBriefInfoDto(
                peerInstance.getRecipient().reportName(),
                peerInstance.getRecipient().getId(),
                peerInstance.getPeerTeamName(), peerInstance.getPeerTeamId(),
                peerInstance.commentOnAction(),
                peerInstance.dateActed(),
                getPeerToPrivilegedStatus(peerInstance.getProcess(), peerInstance).name()
        );
        return result;
    }

    protected Set<TimeOffShiftDto> findTimeOffShifts(WorkflowRequest request) {
        return serviceHelper.findTimeOffShifts(request);
    }

}
