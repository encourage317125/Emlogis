package com.emlogis.common.facade.workflow.reports;

import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.proto.WflProcessService;
import com.emlogis.common.services.workflow.type.WflProcessTypeService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.Organization;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.dto.reports.base.*;
import com.emlogis.model.workflow.dto.reports.requests.*;
import com.emlogis.model.workflow.entities.WflProcessType;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import java.util.*;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.exception.ExceptionCode.NOT_SUPPORTED_WORKFLOW_REPORT_TYPE;

/**
 * Created by alex on 2/27/15.
 */
@Stateless
@Local(value = WorkflowReportFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkflowReportFacadeImpl implements WorkflowReportFacade {

    @EJB
    private WorkflowRequestService processInstanceService;

    @EJB
    private WflProcessService protoProcessService;

    @EJB
    private TeamService teamService;

    @EJB
    private ShiftService shiftService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private WflProcessTypeService typeService;

    @EJB
    private AbsenceTypeService absenceTypeService;

    @EJB
    private OrganizationService organizationService;

    @EJB
    private UserAccountService userAccountService;

    @Override
    public Collection<String> findSites() throws WorkflowServerException {
        Collection<Organization> organizationResultSet =
                organizationService.findOrganizations(new SimpleQuery()).getResult();
        Set<String> sites = new HashSet<>();
        for (Organization organization : organizationResultSet) {
            sites.add(organization.getTenantId());
        }
        return sites;
    }

    @Override
    public Collection<WflReportTeamDto> findTeams(String tenantId) throws WorkflowServerException {
        Set<WflReportTeamDto> resultSet = new HashSet<>();
        List<WorkflowRequest> instances = new ArrayList<>();
        if (tenantId == null || StringUtils.isEmpty(tenantId)) {
            instances.addAll(processInstanceService.findAll());
        } else {
            instances.addAll(processInstanceService.findAllByTenant(tenantId));
        }
        for (WorkflowRequest instance : instances) {
            Team team = teamService.getTeam(new PrimaryKey(instance.getInitiator().getTenantId(), instance.getSubmitterTeamId()));
            resultSet.add(new WflReportTeamDto(team.getName(), team.getId()));
        }
        return resultSet;
    }

    @Override
    public Collection<WflReportWorkflowTypeDto> findTypes() throws WorkflowServerException {
        Set<WflReportWorkflowTypeDto> resultSet = new HashSet<>();
        List<WflProcessType> types = typeService.findAll();
        for (WflProcessType type : types) {
            resultSet.add(new WflReportWorkflowTypeDto(type.getName(), type.getType().name()));
        }
        return resultSet;
    }

    @Override
    public WflReportDto report(
            String type, String site, List<String> teams, Long startDate, Long endDate
    ) throws WorkflowServerException {
        WflProcessType processType = typeService.findByName(type);
        if (!processType.getType().equals(WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST) &&
                !processType.getType().equals(WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST) &&
                !processType.getType().equals(WorkflowRequestTypeDict.WIP_REQUEST) &&
                !processType.getType().equals(WorkflowRequestTypeDict.TIME_OFF_REQUEST)) {
            throw new WorkflowServerException(NOT_SUPPORTED_WORKFLOW_REPORT_TYPE,
                    "No reports can be produced!");
        }
        List<WorkflowRequest> instances =
                processInstanceService.findAllByParameters(processType.getType().toString(), site, teams, startDate, endDate);
        WflReportDto result = new WflReportDto(currentDateStr(), type, site,
                reportDateInterval(startDate, endDate), teamsAsString(teams));
        for (WorkflowRequest instance : instances) {
            Shift shift = shift(instance.getInitiator().getTenantId(), instance.getSubmitterShiftId());
            WflReportActorDto originator = null;
            if(shift != null) {
                originator = new WflReportActorDto(instance.getInitiator().reportName(),
                        instance.getInitiator().getHomeTeam().getName(), shift.getId(), shift.getStartDateTime(),
                        shift.getEndDateTime(), shiftName(shift), shiftDate(shift), shift.getSkillName(), null);
            } else {
                originator = new WflReportActorDto(instance.getInitiator().reportName(),
                        instance.getInitiator().getHomeTeam().getName(), null);
            }

            switch (processType.getType()) {
                case SHIFT_SWAP_REQUEST: {
                    ShiftSwapWflReportDataDto shiftSwapWflReportDataDto = new ShiftSwapWflReportDataDto();
                    shiftSwapWflReportDataDto.setTeamName(originator.getTeamName());
                    shiftSwapWflReportDataDto.setOriginator(originator);
                    shiftSwapWflReportDataDto.setRequestDate(instance.getRequestDate());
                    shiftSwapWflReportDataDto.getRecipients().addAll(parseToRecipients(instance.getRecipients()));
                    result.getData().add(shiftSwapWflReportDataDto);
                    break;
                }
                case OPEN_SHIFT_REQUEST: {
                    OpenShiftWflReportDataDto openShiftWflReportDataDto = new OpenShiftWflReportDataDto();
                    openShiftWflReportDataDto.setEmployeeName(instance.getInitiator().reportName());

                    int position = result.getData().indexOf(openShiftWflReportDataDto);
                    if(position == -1) {
                        openShiftWflReportDataDto.setTeamName(instance.getInitiator().getHomeTeam().getName());
                        openShiftWflReportDataDto.setRequestDate(instance.getRequestDate());
                        result.getData().add(openShiftWflReportDataDto);
                    } else {
                        openShiftWflReportDataDto = (OpenShiftWflReportDataDto) result.getData().get(position);
                    }

                    Shift requestShift = shift(instance.getTenantId(), instance.getSubmitterShiftId());
                    OpenShiftRequestDataDto openShiftRequestDataDto =  new OpenShiftRequestDataDto(
                            requestShift.getStartDateTime(), requestShift.getEndDateTime(), requestShift.getSkillName(),
                            shiftDate(requestShift), instance.getRequestStatus().toString());
                    openShiftWflReportDataDto.getRequests().add(openShiftRequestDataDto);
                    break;
                }
                case WIP_REQUEST: {
                    WorkInPlaceWflReportDataDto workInPlaceWflReportDataDto = new WorkInPlaceWflReportDataDto();
                    workInPlaceWflReportDataDto.setTeamName(originator.getTeamName());
                    workInPlaceWflReportDataDto.setOriginator(originator);
                    workInPlaceWflReportDataDto.setRequestDate(instance.getRequestDate());
                    workInPlaceWflReportDataDto.getRecipients().addAll(parseToWipRecipients(instance));
                    result.getData().add(workInPlaceWflReportDataDto);
                    break;
                }
                case TIME_OFF_REQUEST: {
                    TimeOffRequestInfoDto timeOffRequestInfoDto = fromJsonString(instance.getData(), TimeOffRequestInfoDto.class);

                    TimeOffWflReportDataDto timeOffWflReportDataDto = new TimeOffWflReportDataDto();
                    timeOffWflReportDataDto.setTeamName(originator.getTeamName());
                    timeOffWflReportDataDto.setRequestDate(instance.getCreated().getMillis());
                    timeOffWflReportDataDto.setEmployeeName(instance.getInitiator().reportName());
                    timeOffWflReportDataDto.setDayOffDate(instance.getRequestDate());
                    timeOffWflReportDataDto.setAbsenceType(
                            absenceTypeService.getAbsenceType(
                                    new PrimaryKey(instance.getInitiator().getTenantId(), timeOffRequestInfoDto.getAbsenceTypeId())
                            ).getName());
                    timeOffWflReportDataDto.setStatus(instance.getRequestStatus().toString());
                    result.getData().add(timeOffWflReportDataDto);
                    break;
                }
            }

        }
        return result;
    }

    private Set<WflReportBaseActorDto> parseToWipRecipients(WorkflowRequest instance) {
        Set<WflReportBaseActorDto> resultSet = new HashSet<>();
        Set<WorkflowRequestPeer> recipients = instance.getRecipients();
        for (WorkflowRequestPeer recipient : recipients) {
            resultSet.add(new WflReportBaseActorDto(recipient.getRecipient().reportName(),
                    recipient.getRecipient().getHomeTeam().getName(), recipient.getPeerStatus().toString()));
        }
        return resultSet;
    }

    private List<WflReportActorDto> parseToRecipients(Set<WorkflowRequestPeer> recipients) {
        List<WflReportActorDto> resultList = new ArrayList<>();
        for (WorkflowRequestPeer recipient : recipients) {
            Shift shift = shift(recipient.getRecipient().getTenantId(), recipient.getPeerShiftId());
            resultList.add(new WflReportActorDto(recipient.getRecipient().reportName(),
                    recipient.getRecipient().getHomeTeam().getName(), shift.getId(),
                    shift.getStartDateTime(), shift.getEndDateTime(), shift.getShiftLengthName(), shiftDate(shift),
                    shift.getSkillName(), recipient.getPeerStatus().toString()));
        }
        return resultList;
    }

    private Shift shift(String tenantId, String id) {
        return shiftService.getShift(new PrimaryKey(tenantId, id));
    }

    private Employee employee(String tenantId, String id) {
        return employeeService.getEmployee(new PrimaryKey(tenantId, id));
    }
}
