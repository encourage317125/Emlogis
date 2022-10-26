package com.emlogis.common.facade.workflow.helper;

import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.dto.process.request.submit.*;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;

import javax.ejb.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.emlogis.workflow.WflUtil.*;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ServiceHelper {

    @EJB
    private ShiftService shiftService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private ScheduleService scheduleService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private TeamService teamService;

    public Employee employee(String tenantId, String generatedId) {
        return employeeService.getEmployee(new PrimaryKey(tenantId, generatedId));
    }

    public UserAccount account(String tenantId, String generatedId) {
        return userAccountService.getUserAccount(new PrimaryKey(tenantId, generatedId));
    }

    public Shift shift(String tenantId, String generatedId) {
        return shiftService.getShift(new PrimaryKey(tenantId, generatedId));
    }

    public com.emlogis.model.schedule.Schedule schedule(String tenantId, String generatedId) {
        return scheduleService.getSchedule(new PrimaryKey(tenantId, generatedId));
    }

    public Site site(Team team) {
        return teamService.getSite(team);
    }

    public Team team(String tenantId, String generatedId) {
        return teamService.getTeam(new PrimaryKey(tenantId, generatedId));
    }

    public UserAccount systemAccount(String tenantId){
        return userAccountService.getUserAccount(new PrimaryKey(tenantId, UserAccount.DEFAULT_ADMIN_ID));
    }

    public Set<TimeOffShiftDto> findTimeOffShifts(WorkflowRequest request) {
        Set<TimeOffShiftDto> resultSet = new HashSet<>();
        String fields = "id,startDateTime,endDateTime,skillId,skillName,skillAbbrev,shiftLength,excess,teamId,teamName";
        ResultSet<Object[]> shiftObjs = shiftService.getShifts(
                request.getInitiator().getId(),
                requestStartDateDayStart(request),
                requestStartDateDayEnd(request),
                request.getSubmitterTz().getID(),
                ScheduleStatus.Posted.ordinal(),
                fields,
                0, 25, "startDateTime", "ASC",
                // 25 shifts max should be enough as an employee generally has 1 or 2, rarely more shifts a day
                false);
        Iterator<Object[]> iterator = shiftObjs.getResult().iterator();
        while (iterator.hasNext()) {
            TimeOffShiftDto timeOffShiftDto = new TimeOffShiftDto(iterator.next(), request.getInitiator().getId(),
                    request.getInitiator().reportName());
            if (checkIfShiftInsideRequestDate(timeOffShiftDto, request.getSubmitterTz(), request.getRequestDate())) {
                resultSet.add(timeOffShiftDto);
            }
        }
        return resultSet;
    }

    public WorkflowRequestTypeDict type(SubmitDto dto) {
        if (dto instanceof AvailabilitySubmitDto) {
            return WorkflowRequestTypeDict.AVAILABILITY_REQUEST;
        }
        if (dto instanceof TimeOffSubmitDto) {
            return WorkflowRequestTypeDict.TIME_OFF_REQUEST;
        }
        if (dto instanceof ShiftSwapSubmitDto) {
            return WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST;
        }
        if (dto instanceof WorkInPlaceSubmitDto) {
            return WorkflowRequestTypeDict.WIP_REQUEST;
        }
        if (dto instanceof OpenShiftSubmitDto) {
            return WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST;
        }
        throw new RuntimeException("Unknown request type");
    }
}
