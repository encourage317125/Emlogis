package com.emlogis.common.facade.workflow.process.submition.builder.request.impl;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.description.annotations.RequestDescriptionBuilderQualifierImpl;
import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.facade.workflow.process.submition.builder.request.RequestBuilder;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestInitialDataSearchService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.RequestActionService;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.EmployeeTeam;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.dto.process.request.submit.AvailabilitySubmitDto;
import com.emlogis.model.workflow.dto.process.request.submit.ShiftSwapSubmitDto;
import com.emlogis.model.workflow.dto.process.request.submit.SubmitDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.workflow.WflUtil;
import com.emlogis.workflow.api.identification.RequestRoleProxy;

import javax.ejb.*;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowRoleDict.*;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.PROCESS_INITIATED;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public abstract class AbstractRequestBuilder<T extends SubmitDto> implements RequestBuilder<T> {

    @EJB
    ServiceHelper serviceHelper;

    @EJB
    WorkflowRequestService service;

    @EJB
    WorkflowRequestPeerService peerService;

    @EJB
    RequestRoleProxy requestRoleProxy;

    @EJB
    RequestActionService requestActionService;

    @EJB
    WorkflowRequestTranslator translator;

    @Inject
    private Instance<DescriptionBuilder> descriptionBuilder;

    @EJB
    AccountUtilService accountUtilService;

    @EJB
    TeamService teamService;

    @EJB
    EmployeeService employeeService;

    @EJB
    WorkflowRequestInitialDataSearchService initialDataSearchService;

    protected Team getEmployeeTeam(UserAccount userAccount, Shift shift) {
        Employee employee = employee(userAccount);
        if (shift == null) {
            return findHomeTeam(userAccount, employee);
        } else {
            Team team = findShiftTeam(userAccount, shift);
            if (team == null) {
                return findHomeTeam(userAccount, employee);
            } else {
                return team;
            }
        }
    }

    private Team findShiftTeam(UserAccount userAccount, Shift shift) {
        try {
            return serviceHelper.team(userAccount.getTenantId(), shift.getTeamId());
        } catch (Exception e) {
            throw new RuntimeException("No team found for account " + userAccount.reportName());
        }
    }

    private Team findHomeTeam(UserAccount userAccount, Employee employee) {
        SimpleQuery simpleQuery = new SimpleQuery(employee.getPrimaryKey().getTenantId());
        simpleQuery.setEntityClass(EmployeeTeam.class);
        ResultSet<EmployeeTeam> teamResultSet = null;
        try {
            teamResultSet = employeeService.getEmployeeTeams(employee.getPrimaryKey(), simpleQuery);
            for (EmployeeTeam employeeTeam : teamResultSet.getResult()) {
                if (employeeTeam.getIsHomeTeam()) {
                    return employeeTeam.getTeam();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        throw new RuntimeException("No team found for account " + userAccount.reportName());
    }

    protected Employee employee(UserAccount userAccount) {
        if (userAccount.getEmployee() == null) {
            throw new ValidationException("Can not find employee for account " + userAccount.reportName());
        } else {
            return userAccount.getEmployee();
        }
    }


    protected WorkflowRequest descriptions(WorkflowRequest request) {
        DescriptionBuilder serviceManager = descriptionBuilder.select(new RequestDescriptionBuilderQualifierImpl(request.getRequestType(), MANAGER)).get();
        request.setManagerDescription(serviceManager.build(request, request.getSubmitterTz(), locale(request.getInitiator())));
        DescriptionBuilder serviceSubmitter = descriptionBuilder.select(new RequestDescriptionBuilderQualifierImpl(request.getRequestType(), ORIGINATOR)).get();
        request.setSubmitterDescription(serviceSubmitter.build(request, request.getSubmitterTz(), locale(request.getInitiator())));
        if (WflUtil.isSwapOrWip(request)) {
            DescriptionBuilder servicePeer = descriptionBuilder.select(new RequestDescriptionBuilderQualifierImpl(request.getRequestType(), PEER)).get();
            request.setPeerDescription(servicePeer.build(request, request.getSubmitterTz(), locale(request.getInitiator())));
        }
        return request;
    }

    protected WorkflowRequest rebornRequest(
            WorkflowRequest workflowRequest
    ) {
        workflowRequest.setStatus(PROCESS_INITIATED);
        workflowRequest.setActions(new HashSet<WorkflowRequestLog>());
        workflowRequest.setRequestStatus(WflUtil.getRequestStatus(workflowRequest));
        workflowRequest = service.update(workflowRequest);
        for (WorkflowRequestPeer peer : workflowRequest.getRecipients()) {
            peer.setPeerStatus(WflUtil.getPeerAggregatedRequestStatus(workflowRequest, peer));
            peerService.update(peer);
        }
        workflowRequest.getActions().clear();
        return service.update(workflowRequest);
    }

    protected void initPeerInstance(
            WorkflowRequest request,
            WorkflowRequestPeer peerInstance,
            Employee peerEmployee,
            Shift peerShift,
            Site peerSite,
            Team peerTeam
    ) {

        peerInstance.setRequestType(request.getRequestType());

        peerInstance.setPeerId(peerEmployee.getId());
        peerInstance.setPeerName(peerEmployee.getFirstName() + " " + peerEmployee.getLastName());
        if (peerTeam != null) {
            peerInstance.setPeerTeamId(peerTeam.getId());
            peerInstance.setPeerTeamName(peerTeam.getName());
        }
        if (peerSite != null) {
            peerInstance.setPeerSiteId(peerSite.getId());
            peerInstance.setPeerSiteName(peerSite.getName());
            peerInstance.setPeerTz(peerSite.getTimeZone());
            peerInstance.setPeerCountry(peerSite.getCountry());
        }
        peerInstance.setPeerLang(peerSite.getLanguage());

        if (peerShift != null) {
            peerInstance.setPeerShiftId(peerShift.getId());
            peerInstance.setPeerShiftStartDateTime(peerShift.getStartDateTime());
            peerInstance.setPeerShiftEndDateTime(peerShift.getEndDateTime());
            peerInstance.setPeerShiftTeamId(peerShift.getTeamId());
            peerInstance.setPeerShiftTeamName(peerShift.getTeamName());
            peerInstance.setPeerShiftSkillId(peerShift.getSkillId());
            peerInstance.setPeerShiftSkillName(peerShift.getSkillName());
        }

        if (request.getSubmitterShiftId() != null) {
            peerInstance.setSubmitterShiftId(request.getSubmitterShiftId());
            peerInstance.setSubmitterShiftStartDateTime(request.getSubmitterShiftStartDateTime());
            peerInstance.setSubmitterShiftEndDateTime(request.getSubmitterShiftEndDateTime());
            peerInstance.setSubmitterShiftTeamId(request.getSubmitterTeamId());
            peerInstance.setSubmitterShiftTeamName(request.getSubmitterTeamName());
            peerInstance.setSubmitterShiftSkillId(request.getSubmitterShiftSkillId());
            peerInstance.setSubmitterShiftSkillName(request.getSubmitterShiftSkillName());
        }

    }

    protected class InitialDataBuilder {
        private final Shift submitterShift;
        private final Team submitterTeam;
        private final Site submittedSite;
        private final Long requestDateTime;
        private final Long currentDateTime;

        public InitialDataBuilder(
                AvailabilitySubmitDto dto,
                UserAccount userAccount
        ) {
            submitterShift = null;
            submitterTeam = getEmployeeTeam(userAccount, null);
            submittedSite = (submitterTeam != null ? serviceHelper.site(submitterTeam) : null);
            requestDateTime = identifyAvailabilityRequestDate(submittedSite.getTimeZone(), dto);
            currentDateTime = WflUtil.currentDateTime();
        }

        public InitialDataBuilder(UserAccount userAccount, Shift shift) {
            submitterShift = shift;
            submitterTeam = getEmployeeTeam(userAccount, shift);
            submittedSite = (submitterTeam != null ? serviceHelper.site(submitterTeam) : null);
            requestDateTime = identifyShiftRequestDate(submittedSite.getTimeZone(), submitterShift);
            currentDateTime = WflUtil.currentDateTime();
        }

        public InitialDataBuilder(UserAccount userAccount, String shiftId) {
            submitterShift = serviceHelper.shift(userAccount.getTenantId(), shiftId);
            submitterTeam = getEmployeeTeam(userAccount, serviceHelper.shift(userAccount.getTenantId(), shiftId));
            submittedSite = (submitterTeam != null ? serviceHelper.site(submitterTeam) : null);
            requestDateTime = identifyShiftRequestDate(submittedSite.getTimeZone(), submitterShift);
            currentDateTime = WflUtil.currentDateTime();
        }

        public InitialDataBuilder(ShiftSwapSubmitDto dto, UserAccount userAccount) {
            submitterShift = serviceHelper.shift(userAccount.getTenantId(), dto.getSubmitterShiftId());
            submitterTeam = getEmployeeTeam(userAccount, submitterShift);
            submittedSite = (submitterTeam != null ? serviceHelper.site(submitterTeam) : null);
            requestDateTime = identifyShiftRequestDate(submittedSite.getTimeZone(), submitterShift);
            currentDateTime = WflUtil.currentDateTime();
        }

        public InitialDataBuilder(
                UserAccount userAccount,
                TimeOffRequestInfoDto timeOffRequestInfoDto
        ) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
            submitterShift = null;
            submitterTeam = getEmployeeTeam(userAccount, null);
            submittedSite = accountUtilService.getUserSite(userAccount);
            requestDateTime = identifyTimeOffRequestDate(submittedSite.getTimeZone(), timeOffRequestInfoDto);
            currentDateTime = WflUtil.currentDateTime();
        }

        public Shift shift() {
            return submitterShift;
        }

        public Team team() {
            return submitterTeam;
        }

        public Site site() {
            return submittedSite;
        }

        public Long requestDateTime() {
            return requestDateTime;
        }

        public Long currentDateTime() {
            return currentDateTime;
        }
    }

    class ReqData {
        private final Boolean autoApprove;
        private final String identifier;

        public ReqData(Boolean autoApprove, String identifier) {
            this.autoApprove = autoApprove;
            this.identifier = identifier;
        }

        public Boolean getAutoApprove() {
            return autoApprove;
        }

        public String getIdentifier() {
            return identifier;
        }
    }
}
