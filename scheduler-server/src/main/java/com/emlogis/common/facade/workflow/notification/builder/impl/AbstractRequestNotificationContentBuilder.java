package com.emlogis.common.facade.workflow.notification.builder.impl;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.model.workflow.notification.common.MessageEmployeeInfo;
import com.emlogis.model.workflow.notification.common.MessageParametersShiftInfo;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.workflow.WflUtil;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import org.joda.time.DateTimeZone;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.*;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.*;

/**
 * Created by user on 25.08.15.
 */
@Stateless
public abstract class AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @EJB
    private TeamService teamService;

    @EJB
    private ShiftService shiftService;

    @EJB
    private WorkflowRequestPeerService workflowRequestPeerService;

    @EJB
    private ServiceHelper serviceHelper;

    @EJB
    private AccountUtilService accountUtilService;

    private Long effectiveStartDateTime(WorkflowRequest request) {
        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDAvailDto.class);
                return chooseEarliestDate(dto.getSelectedDates());
            }
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDPrefDto.class);
                return chooseEarliestDate(dto.getSelectedDates());
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIAvailDto.class);
                return dto.getEffectiveStartDate();
            }
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIPrefDto.class);
                return dto.getEffectiveStartDate();
            }
            default: {
                throw new ValidationException("Unrecognizable Availability request type!");
            }
        }
    }

    private Long availabilityEndDateTime(WorkflowRequest request) {
        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDAvailDto.class);
                return chooseLatestDate(dto.getSelectedDates());
            }
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDPrefDto.class);
                return chooseLatestDate(dto.getSelectedDates());
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIAvailDto.class);
                return dto.getEffectiveEndDate();
            }
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIPrefDto.class);
                return dto.getEffectiveEndDate();
            }
            default: {
                throw new ValidationException("Unrecognizable Availability request type!");
            }
        }
    }

    protected String availabilityEffectiveStartDate(WorkflowRequest request) {
        Long effectiveStartDate = effectiveStartDateTime(request);
        return dateStr(request.getSubmitterTz(), effectiveStartDate, locale(request.getInitiator()));
    }

    protected String availabilityEffectiveUntilDate(WorkflowRequest request) {
        Long effectiveEndDate = availabilityEndDateTime(request);
        return dateStr(request.getSubmitterTz(), effectiveEndDate, locale(request.getInitiator()));
    }

    protected String availabilityEndTime(WorkflowRequest request) {
        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDAvailDto.class);
                AvailcalSimpleTimeFrame earliestTF = chooseEarliestTF(dto.getTimeFrames());
                return timeStr(request.getSubmitterTz(), earliestTF.getStartTime(), locale(request.getInitiator()));
            }
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDPrefDto.class);
                AvailcalSimpleTimeFrame earliestTF = chooseEarliestTF(dto.getPreferTimeFrames());
                return timeStr(request.getSubmitterTz(), earliestTF.getStartTime(), locale(request.getInitiator()));
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIAvailDto.class);
                AvailcalSimpleTimeFrame earliestTF = chooseEarliestTF(dto.getTimeFrames());
                return timeStr(request.getSubmitterTz(), earliestTF.getStartTime(), locale(request.getInitiator()));
            }
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIPrefDto.class);
                AvailcalSimpleTimeFrame earliestTF = chooseEarliestTF(dto.getPreferTimeFrames());
                return timeStr(request.getSubmitterTz(), earliestTF.getStartTime(), locale(request.getInitiator()));
            }
            default: {
                throw new ValidationException("Unrecognizable Availability request type!");
            }
        }
    }

    protected String availabilityStartTime(WorkflowRequest request) {
        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDAvailDto.class);
                AvailcalSimpleTimeFrame latestTf = chooseLatestTF(dto.getTimeFrames());
                return timeStr(request.getSubmitterTz(), latestTf.getStartTime(), locale(request.getInitiator()));
            }
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDPrefDto.class);
                AvailcalSimpleTimeFrame latestTf = chooseLatestTF(dto.getPreferTimeFrames());
                return timeStr(request.getSubmitterTz(), latestTf.getStartTime(), locale(request.getInitiator()));
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIAvailDto.class);
                AvailcalSimpleTimeFrame latestTf = chooseLatestTF(dto.getTimeFrames());
                return timeStr(request.getSubmitterTz(), latestTf.getStartTime(), locale(request.getInitiator()));
            }
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIPrefDto.class);
                AvailcalSimpleTimeFrame latestTf = chooseLatestTF(dto.getPreferTimeFrames());
                return timeStr(request.getSubmitterTz(), latestTf.getStartTime(), locale(request.getInitiator()));
            }
            default: {
                throw new ValidationException("Unrecognizable Availability request type!");
            }
        }
    }

    protected String availabilityWeekDay(WorkflowRequest request) {
        Long latest = null;
        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDAvailDto.class);
                latest = chooseEarliestDate(dto.getSelectedDates());
                break;
            }
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDPrefDto.class);
                latest = chooseEarliestDate(dto.getSelectedDates());
                break;
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIAvailDto.class);
                latest = dto.getEffectiveStartDate();
                break;
            }
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIPrefDto.class);
                latest = dto.getEffectiveStartDate();
                break;
            }
            default: {
                throw new ValidationException("Unrecognizable Availability request type!");
            }
        }
        return dayoftheWeek(latest, request.getSubmitterTz(), locale(request.getInitiator()));
    }

    protected Employee employeeForPeer(
            String tenant,
            String peerId
    ) {
        PrimaryKey chosenPk = new PrimaryKey(tenant, peerId);
        WorkflowRequestPeer peer = workflowRequestPeerService.find(chosenPk);
        return serviceHelper.employee(tenant, peer.getRecipient().getId());
    }

    protected WorkflowRequestPeer findPeer(
            WorkflowRequest request,
            Employee employee,
            String shiftId
    ) {
        if (!WflUtil.isSwapOrWip(request)) {
            throw new RuntimeException("Request type has no peers");
        }
        for (WorkflowRequestPeer peer : request.getRecipients()) {
            if (request.getRequestType().equals(WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST)) {
                if (peer.getRecipient().getId().equals(employee.getId()) &&
                        peer.getRecipient().getTenantId().equals(employee.getTenantId()) &&
                        shiftId.equals(peer.getPeerShiftId())) {
                    return peer;
                }
            } else {
                if (peer.getRecipient().getId().equals(employee.getId()) &&
                        peer.getRecipient().getTenantId().equals(employee.getTenantId())) {
                    return peer;
                }
            }
        }
        throw new RuntimeException("Employee is not a peer!");
    }

    protected MessageParametersShiftInfo identifyChoosenPeerinfo(WorkflowRequest request) {
        if (request.getChosenPeerId() != null) {
            WorkflowRequestPeer peer = workflowRequestPeerService.find(
                    new PrimaryKey(request.getTenantId(), request.getChosenPeerId()));
            MessageParametersShiftInfo info = new MessageParametersShiftInfo(
                    parsePerson(peer.getRecipient()),
                    peer.getPeerShiftId(),
                    peer.getPeerShiftTeamId(),
                    peer.getPeerShiftTeamName(),
                    peer.getPeerShiftSkillId(),
                    peer.getPeerShiftSkillName(),
                    dateStr(request.getSubmitterTz(), peer.getPeerShiftStartDateTime(), request.locale()),
                    timeStr(request.getSubmitterTz(), peer.getPeerShiftStartDateTime(), request.locale()),
                    timeStr(request.getSubmitterTz(), peer.getPeerShiftEndDateTime(), request.locale()));
            return info;
        }
        return null;
    }


    protected List<MessageParametersShiftInfo> parseToPeerAppliedShiftInfoList(
            WorkflowRequest request,
            Employee employee
    ) {
        List<MessageParametersShiftInfo> result = new ArrayList<>();
        for (WorkflowRequestPeer peer : request.getRecipients()) {
            if (peer.getRecipient().getId().equals(employee.getId()) &&
                    peer.getRecipient().getTenantId().equals(employee.getTenantId())) {
                MessageParametersShiftInfo info = new MessageParametersShiftInfo(
                        parsePerson(peer.getRecipient()),
                        peer.getPeerShiftId(),
                        peer.getPeerShiftTeamId(),
                        peer.getPeerShiftTeamName(),
                        peer.getPeerShiftSkillId(),
                        peer.getPeerShiftSkillName(),
                        dateStr(request.getSubmitterTz(), peer.getPeerShiftStartDateTime(), request.locale()),
                        timeStr(request.getSubmitterTz(), peer.getPeerShiftStartDateTime(), request.locale()),
                        timeStr(request.getSubmitterTz(), peer.getPeerShiftEndDateTime(), request.locale()));
                result.add(info);
            }
        }
        return result;
    }


    protected List<TimeOffShiftDto> shifts(WorkflowRequest request) {
        List<TimeOffShiftDto> result = new ArrayList<>();
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
                result.add(timeOffShiftDto);
            }
        }
        return result;
    }

    protected List<MessageEmployeeInfo> parsePersons(Set<WorkflowRequestPeer> recipients) {
        List<MessageEmployeeInfo> result = new ArrayList<>();
        for (WorkflowRequestPeer requestPeer : recipients) {
            result.add(parsePerson(requestPeer.getRecipient()));
        }
        return result;
    }

    protected List<MessageParametersShiftInfo> parseShiftInfoList(
            List<TimeOffShiftDto> shifts,
            Employee employee
    ) {
        List<MessageParametersShiftInfo> result = new ArrayList<>();
        for (TimeOffShiftDto shift : shifts) {
            DateTimeZone dtz = accountUtilService.getActualTimeZone(employee);
            MessageParametersShiftInfo info = new MessageParametersShiftInfo(
                    parsePerson(employee),
                    shift.getId(),
                    shift.getTeamId(),
                    shift.getTeamName(),
                    shift.getSkillId(),
                    shift.getSkillName(),
                    dateStr(dtz, shift.getStartDateTime(), locale(employee)),
                    timeStr(dtz, shift.getStartDateTime(), locale(employee)),
                    timeStr(dtz, shift.getEndDateTime(), locale(employee)));
            result.add(info);
        }
        return result;
    }

    protected List<MessageParametersShiftInfo> parseToPeersSubmittedShiftInfoList(
            WorkflowRequest request
    ) {
        List<MessageParametersShiftInfo> result = new ArrayList<>();
        for (WorkflowRequestPeer peer : request.getRecipients()) {
            MessageParametersShiftInfo info = new MessageParametersShiftInfo(
                    parsePerson(peer.getRecipient()),
                    peer.getPeerShiftId(),
                    peer.getPeerShiftTeamId(),
                    peer.getPeerShiftTeamName(),
                    peer.getPeerShiftSkillId(),
                    peer.getPeerShiftSkillName(),
                    dateStr(request.getSubmitterTz(), peer.getPeerShiftStartDateTime(), request.locale()),
                    timeStr(request.getSubmitterTz(), peer.getPeerShiftStartDateTime(), request.locale()),
                    timeStr(request.getSubmitterTz(), peer.getPeerShiftEndDateTime(), request.locale()));
            result.add(info);
        }
        return result;
    }

    protected MessageParametersShiftInfo parseToSubmitterShiftInfo(WorkflowRequest request) {
        MessageParametersShiftInfo result = new MessageParametersShiftInfo(
                parsePerson(request.getInitiator()),
                request.getSubmitterShiftId(),
                request.getSubmitterTeamId(),
                request.getSubmitterTeamName(),
                request.getSubmitterShiftSkillId(),
                request.getSubmitterShiftSkillName(),
                dateStr(request.getSubmitterTz(), request.getSubmitterShiftStartDateTime(), request.locale()),
                timeStr(request.getSubmitterTz(), request.getSubmitterShiftStartDateTime(), request.locale()),
                timeStr(request.getSubmitterTz(), request.getSubmitterShiftEndDateTime(), request.locale()));
        return result;
    }

    protected MessageEmployeeInfo parsePerson(Employee employee) {
        DateTimeZone dtz = identifyEmployeeDateTimeZone(employee);
        Locale locale = identifyEmployeeLocale(employee);
        MessageEmployeeInfo employeeInfo = new MessageEmployeeInfo(employee.getId(), employee.getTenantId(),
                employee.reportName(), dtz, locale.getLanguage(), locale.getCountry());
        return employeeInfo;
    }

    protected MessageEmployeeInfo parsePerson(UserAccount account) {
        DateTimeZone dtz = identifyEmployeeDateTimeZone(account);
        Locale locale = locale(account);
        MessageEmployeeInfo employeeInfo = new MessageEmployeeInfo(account.getId(), account.getTenantId(),
                account.reportName(), dtz, locale.getLanguage(), locale.getCountry());
        return employeeInfo;
    }

    protected Locale identifyEmployeeLocale(Employee employee) {
        String lang = "en";
        String country = "US";
        Site employeeSite = teamService.getSite(employee.getHomeTeam());
        if (employee.getLanguage() != null) {
            lang = employee.getLanguage();
        } else if (employeeSite.getLanguage() != null) {
            lang = employeeSite.getLanguage();
        }
        if (employee.getCountry() != null) {
            country = employee.getCountry();
        } else if (employeeSite.getCountry() != null) {
            country = employeeSite.getCountry();
        }
        if (country == null && lang == null) {
            return new Locale("en", "US");
        } else if (country == null && lang != null) {
            return new Locale(lang);
        }
        return new Locale(lang, country);
    }

    protected WorkflowRequestStatusDict identifyStatus(
            WorkflowRequest request,
            List<WorkflowRequestPeer> peers
    ) {
        Map<WorkflowRequestPeer, WorkflowRequestStatusDict> statuses = new HashMap<>();
        for (WorkflowRequestPeer peer : peers) {
            statuses.put(peer, getPeerAggregatedRequestStatus(request, peer));
        }
        Set<Map.Entry<WorkflowRequestPeer, WorkflowRequestStatusDict>> entries = statuses.entrySet();
        WorkflowRequestStatusDict result = null;
        for (Map.Entry<WorkflowRequestPeer, WorkflowRequestStatusDict> entry : entries) {
            if (result == null) {
                result = entry.getValue();
            } else if (result.weight() <= entry.getValue().weight()) {
                result = entry.getValue();
            }
        }
        return result;
    }

    protected List<WorkflowRequestPeer> findPeers(WorkflowRequest request, Employee employee) {
        List<WorkflowRequestPeer> result = new ArrayList<>();
        for (WorkflowRequestPeer workflowRequestPeer : request.getRecipients()) {
            if (workflowRequestPeer.getRecipient().getId().equals(employee.getId())) {
                result.add(workflowRequestPeer);
            }
        }
        return result;
    }

    protected DateTimeZone identifyEmployeeDateTimeZone(Employee employee) {
        Site employeeSite = teamService.getSite(employee.getHomeTeam());
        if (employeeSite.getTimeZone() == null) {
            return DateTimeZone.UTC;
        }
        return employeeSite.getTimeZone();
    }

    protected DateTimeZone identifyEmployeeDateTimeZone(UserAccount account) {
        return account.getTimeZone() == null ? DateTimeZone.UTC : account.getTimeZone();
    }
}
