package com.emlogis.workflow;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.EmployeeProcessAutoApproval;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.commentary.RequestCommentDto;
import com.emlogis.model.workflow.dto.commentary.RequestCommentary;
import com.emlogis.model.workflow.dto.decision.ShiftDecisionAction;
import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.dto.process.request.submit.AvailabilitySubmitDto;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.model.workflow.notification.common.MessageEmployeeInfo;
import com.emlogis.model.workflow.notification.common.MessageParametersShiftInfo;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowShiftManagerActionDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.emlogis.common.EmlogisUtils.equals;
import static com.emlogis.common.EmlogisUtils.toJsonString;
import static com.emlogis.workflow.enums.WorkflowActionDict.*;
import static com.emlogis.workflow.enums.WorkflowRequestTypeDict.*;
import static com.emlogis.workflow.exception.ExceptionCode.REQUEST_STATUS_CALCULATION_ERROR;
import static edu.emory.mathcs.backport.java.util.Arrays.asList;

/**
 * Created by alexborlis on 18.02.15.
 */
public class WflUtil {

    public static final String UTF_8 = "UTF-8";
    private static WorkflowStatusChangeMatrix matrix = new WorkflowStatusChangeMatrix();

    public static boolean employeeHasAutoApprovalFlag(WflProcess protoProcess, Employee requestEmployee) {
        for (EmployeeProcessAutoApproval autoApproval : requestEmployee.getEmployeeProcessAutoApprovals()) {
            if (autoApproval.getWflProcessType().getType().equals(protoProcess.getType().getType())) {
                return autoApproval.isAutoApproval();
            }
        }
        return false;
    }

    public static String messagePrm(Long lng, DateTimeZone dtz, Locale lcl) {
        return new String(dateStr(dtz, lng, lcl) + " " + timeStr(dtz, lng, lcl));
    }

    public static String messagePrmOnlyDate(Long lng, DateTimeZone dtz, Locale lcl) {
        return new String(dateStr(dtz, lng, lcl) + " ");
    }


    public static String timeStr(DateTimeZone dtz, Long timeUTC, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormat.forStyle("-S").withLocale(locale);
        String output = formatter.print(dtz.convertUTCToLocal(timeUTC));
        return output;
    }

    public static String dateStr(DateTimeZone dtz, Long timeUTC, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        String formattedDate = df.format(dtz.convertUTCToLocal(timeUTC));
        return formattedDate;
    }

    public static String messagePrmShifts(List<MessageParametersShiftInfo> list) {
        return toJsonString(list);
    }

    public static String messagePrmShift(MessageParametersShiftInfo choosen) {
        return toJsonString(choosen);
    }

    public static String messagePrmPeers(List<MessageEmployeeInfo> list) {
        return toJsonString(list);
    }

    public static String messagePrmPeer(MessageEmployeeInfo receiver) {
        return toJsonString(receiver);
    }

    public static Boolean checkIfShiftInsideRequestDate(
            TimeOffShiftDto timeOffShiftDto,
            DateTimeZone submitterTz,
            Long requestDate) {
        //first calculate shifts start and end date time in Site timezone
        Long startDateInSiteTime = submitterTz.convertUTCToLocal(timeOffShiftDto.getStartDateTime());
        Long endDateInSiteTime = submitterTz.convertUTCToLocal(timeOffShiftDto.getEndDateTime());

        //then calculate UTC request date in Site timezone
        Long requestDateInSiteTime = submitterTz.convertUTCToLocal(requestDate);

        //end find that day start and end
        Long requestDateStartPeriod = providedDateStartPeriod(requestDateInSiteTime);
        Long requestDateEndPeriod = providedDateEndPeriod(requestDateInSiteTime);

        //identify if in Site timezone
        Boolean startDateLaysBetween = (startDateInSiteTime >= requestDateStartPeriod) && (startDateInSiteTime <= requestDateEndPeriod);
        Boolean endDateLaysBetween = (endDateInSiteTime >= requestDateStartPeriod) && (endDateInSiteTime <= requestDateEndPeriod);
        return startDateLaysBetween || endDateLaysBetween;
    }

    public static Boolean peerCanAct(WorkflowRequest request, Employee employee) {
        return getPeerAggregatedRequestStatus(request, employee).equals(WorkflowRequestStatusDict.PEER_PENDING);
    }

    public static Minutes minutesInDay() {
        return Minutes.minutes(60 * 24);
    }

    public static DateTime startOfDateInUTC(Long requestDate) {
        return new DateTime(new DateTime(requestDate).toDate());
    }

    public static List<WorkflowRequestPeer> findAllEligiblePeersThatApproved(
            WorkflowRequest request,
            Employee requestEmployee
    ) {
        List<WorkflowRequestPeer> resultList = new ArrayList<>();
        for (WorkflowRequestPeer peer : request.getRecipients()) {
            Boolean sameEmployee = peer.getRecipient().getId().equals(requestEmployee.getId());
            if (sameEmployee) {
                if (employeeApprovedAsPeer(request, peer)) {
                    resultList.add(peer);
                }
            }
        }
        return resultList;
    }

    public static WorkflowRequestPeer identifyPeer(
            WorkflowRequest request,
            PrimaryKey shiftPk,
            UserAccount userAccount
    ) {
        for (WorkflowRequestPeer peer : request.getRecipients()) {
            if (userAccount.getEmployee().getId().equals(peer.getRecipient().getId())) {
                if (request.getRequestType().equals(SHIFT_SWAP_REQUEST)) {
                    if (peer.getPeerShiftId().equals(shiftPk.getId())) {
                        return peer;
                    }
                } else {
                    return peer;
                }
            }
        }
        throw new RuntimeException("Account " + userAccount.getId() + " is not a peer to request " + request.getId());
    }

    public static String findLatestAppropriateComment(
            WorkflowRequest request,
            WorkflowRequestTranslator translator
    ) {
        if (request.getRequestStatus().isFinalState()) {
            switch (request.getRequestStatus()) {
                case PEER_DECLINED: {
                    return latestCommentByAction(request, PEER_DECLINE);
                }
                case WITHDRAWN: {
                    return translator.getMessage(request.locale(), "request.decline.reason.withdrawn.default", null);
                }
                case DELETED: {
                    String comment = latestCommentByAction(request, PROCESS_TERMINATED);
                    if (StringUtils.isEmpty(comment)) {
                        comment = latestCommentByAction(request, PEER_CANCELLED);
                    }
                    return comment;
                }
                case DECLINED: {
                    String comment = latestCommentByAction(request, ERROR_IN_ACTION);
                    if (StringUtils.isEmpty(comment)) {
                        comment = latestCommentByAction(request, MANAGER_DECLINE);
                    }
                    return comment;
                }
                case APPROVED: {
                    String comment = latestCommentByAction(request, ACTION_SUCCESS);
                    if (StringUtils.isEmpty(comment)) {
                        comment = latestCommentByAction(request, MANAGER_APPROVE);
                    }
                    return comment;
                }
                case EXPIRED: {
                    return translator.getMessage(request.locale(), "request.decline.reason.expired.default", null);
                }
                default: {
                    return "";
                }
            }
        } else {
            return translator.getMessage(request.locale(), "request.decline.reason.pending.default", null);
        }
    }

    private static String latestCommentByAction(
            WorkflowRequest request,
            WorkflowActionDict actionDict
    ) {
        for (WorkflowRequestLog action : request.getActions()) {
            if (action.getAction().equals(actionDict)) {
                return action.getComment();
            }
        }
        return "";
    }

    public static boolean checkNumberOfAssignOptionsMoreThenOne(List<ShiftDecisionAction> decisionActionList) {
        int count = 0;
        for (ShiftDecisionAction action : decisionActionList) {
            if (count > 1) {
                return true;
            }
            if (action.getAction().equals(WorkflowShiftManagerActionDict.ASSIGN_SHIFT) ||
                    action.getAction().equals(WorkflowShiftManagerActionDict.POST_AS_OPEN_SHIFT)) {
                count++;
            }
        }
        if (count > 1) {
            return true;
        } else {
            return false;
        }
    }

    public static Long chooseEarliestDate(Collection<Long> selectedDates) {
        Long earliest = null;
        for (Long dateTime : selectedDates) {
            if (earliest == null) {
                earliest = dateTime;
            } else {
                if (earliest >= dateTime) {
                    earliest = dateTime;
                }
            }
        }
        return earliest;
    }

    public static Long chooseLatestDate(Collection<Long> selectedDates) {
        Long latest = null;
        for (Long dateTime : selectedDates) {
            if (latest == null) {
                latest = dateTime;
            } else {
                if (latest <= dateTime) {
                    latest = dateTime;
                }
            }
        }
        return latest;
    }

    /**
     * Method used
     * @param dateTime
     * @param dtx
     * @param locale
     * @return
     */
    public static String dayoftheWeek(
            Long dateTime,
            DateTimeZone dtx,
            Locale locale
    ){
        return new DateTime(dtx.convertUTCToLocal(dateTime)).dayOfWeek().getName();
    }

    public static AvailcalSimpleTimeFrame chooseEarliestTF(List<AvailcalSimpleTimeFrame> timeFrames) {
        AvailcalSimpleTimeFrame earliest = null;
        for (AvailcalSimpleTimeFrame tf : timeFrames) {
            if (earliest == null) {
                earliest = tf;
            } else {
                if (earliest.getStartTime() >= tf.getStartTime()) {
                    earliest = tf;
                }
            }
        }
        return earliest;
    }

    public static AvailcalSimpleTimeFrame chooseLatestTF(List<AvailcalSimpleTimeFrame> timeFrames) {
        AvailcalSimpleTimeFrame latest = null;
        for (AvailcalSimpleTimeFrame tf : timeFrames) {
            if (latest == null) {
                latest = tf;
            } else {
                if (latest.getStartTime() <= tf.getStartTime()) {
                    latest = tf;
                }
            }
        }
        return latest;
    }


    private enum StatusView {
        PRIVILEGED_STATUS,
        PEER_REQUEST_STATUS,
        PEER_TO_PRIVILEGED_STATUS,
    }

    private static final ConcurrentHashMap<WorkflowRequestTypeDict, java.lang.String> initialResourceMap;

    static {
        initialResourceMap = new ConcurrentHashMap<>();
        initialResourceMap.put(TIME_OFF_REQUEST, "pto_notification.ftl");
        initialResourceMap.put(OPEN_SHIFT_REQUEST, "open_shift_notification.ftl");
        initialResourceMap.put(SHIFT_SWAP_REQUEST, "shift_swap_notification.ftl");
        initialResourceMap.put(WIP_REQUEST, "work_in_place_notification.ftl");
        initialResourceMap.put(AVAILABILITY_REQUEST, "availability_request_notification.ftl");
    }

    /**
     * {@inheritDoc}
     */
    public static WorkflowRequestStatusDict getRequestStatus(
            WorkflowRequest instance
    ) throws WorkflowServerException {
        return matrix.getStatus(StatusView.PRIVILEGED_STATUS, instance, null);
    }

    /**
     * {@inheritDoc}
     */
    public static WorkflowRequestStatusDict getPeerAggregatedRequestStatus(
            WorkflowRequest request, WorkflowRequestPeer peer
    ) throws WorkflowServerException {
        return matrix.getStatus(StatusView.PEER_REQUEST_STATUS, request, peer);
    }

    /**
     * {@inheritDoc}
     */
    public static WorkflowRequestStatusDict getPeerAggregatedRequestStatus(
            WorkflowRequest request, Employee employee
    ) throws WorkflowServerException {
        WorkflowRequestStatusDict aggregatedStatus = null;
        for (WorkflowRequestPeer peer : request.getRecipients()) {
            Boolean sameEmployee = peer.getRecipient().getId().equals(employee.getId());
            Boolean sametenant = peer.getRecipient().getTenantId().equals(employee.getTenantId());
            if (sameEmployee && sametenant) {
                WorkflowRequestStatusDict status = matrix.getStatus(StatusView.PEER_REQUEST_STATUS, request, peer);
                if (aggregatedStatus == null) {
                    aggregatedStatus = status;
                } else if (aggregatedStatus.weight() < status.weight()) {
                    aggregatedStatus = status;
                }
            }
        }
        return aggregatedStatus;
    }

    /**
     * {@inheritDoc}
     */
    public static WorkflowRequestStatusDict getPeerAggregatedRequestStatus(
            Set<String> peersStr, Employee employee
    ) throws WorkflowServerException {
        WorkflowRequestStatusDict aggregatedStatus = null;
        for (String peerStr : peersStr) {
            String[] pInfo = peerStr.split(":");
            Boolean sameEmployee = (pInfo[1]).equals(employee.getId());
            Boolean sametenant = (pInfo[0]).equals(employee.getTenantId());
            if (sameEmployee && sametenant) {
                WorkflowRequestStatusDict status = WorkflowRequestStatusDict.valueOf(pInfo[2]);
                if (aggregatedStatus == null) {
                    aggregatedStatus = status;
                } else if (aggregatedStatus.weight() < status.weight()) {
                    aggregatedStatus = status;
                }
            }
        }
        return aggregatedStatus;
    }

    /**
     * {@inheritDoc}
     */
    public static WorkflowRequestStatusDict getPeerToPrivilegedStatus(
            WorkflowRequest instance, WorkflowRequestPeer peer
    ) throws WorkflowServerException {
        return matrix.getStatus(StatusView.PEER_TO_PRIVILEGED_STATUS, instance, peer);
    }

    public static boolean isUUID(String string) {
        try {
            UUID.fromString(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static Long shiftDate(Shift shift) {
        return new Date(shift.getStartDateTime()).getTime();
    }

    public static String shiftTime(Shift shift) {
        return shift.getStartDateTime() + " - " + shift.getEndDateTime();
    }

    public static String shiftName(Shift shift) {
        return shift.getShiftLengthName();
    }

    public static String reportDateInterval(Long startDate, Long endDate) {
        return new Date(startDate).toString() + "/" + new Date(endDate).toString();
    }

    /**
     * make sure its updating each time
     *
     * @param instance
     * @return
     * @throws WorkflowServerException
     */
    public static WorkflowRequest recalculatePeersStatuses(WorkflowRequest instance) throws WorkflowServerException {
        int peerApprovedCount = 0;
        for (WorkflowRequestPeer peer : instance.getRecipients()) {
            peer.setPeerStatus(getPeerAggregatedRequestStatus(instance, peer));
            if (peer.getPeerStatus() == WorkflowRequestStatusDict.PEER_APPROVED) {
                peerApprovedCount++;
            }
        }
        instance.setPeerApprovedCount(peerApprovedCount);
        return instance;
    }

    public static String teamsAsString(List<String> teams) {
        StringBuilder builder = new StringBuilder();
        Integer size = teams.size();
        Integer counter = 0;
        for (java.lang.String team : teams) {
            builder.append(team);
            if (counter != size - 1) {
                builder.append(", ");
            }
            counter++;
        }
        return builder.toString();
    }

//    public static Boolean isFirstAppliedStrategyProcess(WorkflowRequest instance) {
//        return instance.getApplyStrategy().equals(FIRST_APPLIED);
//    }

    public static Boolean isLastPeerAttender(WorkflowRequest instance, WorkflowActionDict role) {
        return (getActionsByType(instance, WorkflowActionDict.PEER_DECLINE).size() + getActionsByType(instance, WorkflowActionDict.PEER_APPROVE).size())
                == instance.getRecipients().size();
    }

//    public static Boolean isFirstAppliedProcessAlreadyApplied(WorkflowRequest request) {
//        return recipientsApproved(request).size() > 0 && request.getApplyStrategy().equals(FIRST_APPLIED);
//    }

    public static WorkflowRequest addCommentary(WorkflowRequest instance, Long dateTime, String employeeName, String comment) {
        RequestCommentary rc = EmlogisUtils.fromJsonString(instance.commentary(), RequestCommentary.class);
        rc.getCommentary().add(new RequestCommentDto(dateTime, employeeName, comment));
        instance.setComment(toJsonString(rc));
        return instance;
    }

    public static Boolean employeeAlreadyMadeDecision(WorkflowRequest instance, String employeeId) {
        Set<WorkflowRequestLog> actions = instance.getActions();
        boolean acted = false;
        for (WorkflowRequestLog action : actions) {
            if (action.getActorId().equals(employeeId)) {
                acted = true;
                break;
            }
        }
        return acted;
    }

    public static String managerIds(List<UserAccount> managers) {
        StringBuilder builder = new StringBuilder();
        for (UserAccount manager : managers) {
            builder.append(manager.getId() + ", ");
        }
        return builder.toString();
    }

    public static String managerNames(List<UserAccount> managers) {
        StringBuilder builder = new StringBuilder();
        for (UserAccount manager : managers) {
            builder.append(manager.reportName() + ", ");
        }
        return builder.toString();
    }

    public static Long currentDateTime() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    }

    public static Boolean parseQueryConcatenatedIsRead(byte[] byteArray, String employeeId) {
        Boolean isRead = false;
        String sourceString = (new String(byteArray));
        List<String> cumulativeIsReadList = asList((sourceString).split("@"));
        isRead = sourceString.contains(employeeId);
        if (isRead) {
            Boolean localIsRead = false;
            for (String str : cumulativeIsReadList) {
                if (str.contains(employeeId)) {
                    List<String> employeeValue = asList(str.split(":"));
                    localIsRead = Integer.valueOf(employeeValue.get(1).toCharArray()[0]) == 1;
                    if (localIsRead) {
                        break;
                    }
                }

            }
            isRead = localIsRead;
        }
        return isRead;
    }

    public static Date currentDate() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
    }

    public static String currentDateStr() {
        return new DateTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime()).toString();
    }

    public static Timestamp asCurrentTime(DateTimeZone dtz) {
        Long localInUTC = dtz.convertLocalToUTC(currentDateTime(), true);
        return new Timestamp(localInUTC);
    }

    public static Long requestStartDateDayStart(WorkflowRequest instance) {
        return providedDateStartPeriod(instance.getRequestDate());
    }

    public static Long requestStartDateDayEnd(WorkflowRequest instance) {
        return providedDateEndPeriod(instance.getRequestDate());
    }

    public static Long providedDateStartPeriod(Long providedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(providedDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static Long providedDateEndPeriod(Long providedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(providedDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static Long identifyAvailabilityRequestDate(DateTimeZone dtz, AvailabilitySubmitDto submitDto) {
        Long resultDate = null;
        AvailabilityWorkflowRequest availabilityWorkflowRequest = submitDto.getAvailUpdate();
        switch (availabilityWorkflowRequest.getType()) {
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto dto = (AvailcalUpdateParamsCIPrefDto) submitDto.getAvailUpdate();
                resultDate = dto.getEffectiveStartDate();
                break;
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto dto = (AvailcalUpdateParamsCIAvailDto) submitDto.getAvailUpdate();
                resultDate = dto.getEffectiveStartDate();
                break;
            }
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto dto = (AvailcalUpdateParamsCDPrefDto) submitDto.getAvailUpdate();
                Collection<Long> allDates = dto.getSelectedDates();
                Long minimum = null;
                for (Long cand : allDates) {
                    if (minimum == null) {
                        minimum = cand;
                    } else if (cand < minimum) {
                        minimum = cand;
                    }
                }
                resultDate = minimum;
                break;
            }
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto dto = (AvailcalUpdateParamsCDAvailDto) submitDto.getAvailUpdate();
                Collection<Long> allDates = dto.getSelectedDates();
                Long minimum = null;
                for (Long cand : allDates) {
                    if (minimum == null) {
                        minimum = cand;
                    } else if (cand < minimum) {
                        minimum = cand;
                    }
                }
                resultDate = minimum;
                break;
            }
        }
        return resultDate;
    }

    public static Long identifyTimeOffRequestDate(DateTimeZone dtz, TimeOffRequestInfoDto submitDto) {
        return submitDto.getDate();
    }

    public static Long identifyShiftRequestDate(DateTimeZone dtz, Shift submitterShift) {
        return submitterShift.getStartDateTime();
    }

    public static DateTime identifyShiftRequestDateTime(DateTimeZone dtz, Shift submitterShift, Long datetime) {
        return new DateTime(datetime);
    }

//    public static Long identifyShiftRequestDateTimeLongUTC(DateTimeZone dtz, Long datetime) {
//        return dtz.convertLocalToUTC(datetime, t);
//    }

    public static Set<WorkflowRequestLog> getActionsByType(WorkflowRequest instance, WorkflowActionDict type) {
        Set<WorkflowRequestLog> resultSet = new HashSet<>();
        for (WorkflowRequestLog action : instance.getActions()) {
            if (action.getAction().equals(type)) {
                resultSet.add(action);
            }
        }
        return resultSet;
    }

    public static Boolean managerCanAct(WorkflowRequest instance) {
        if (instance.getStatus().equals(RequestTechnicalStatusDict.READY_FOR_ADMIN)) {
            return true;
        }
        if (getActionsByType(instance, PEER_APPROVE).isEmpty() && getActionsByType(instance, PEER_DECLINE).isEmpty()) {
            return false;
        } else if (getActionsByType(instance, PEER_APPROVE).isEmpty() && !getActionsByType(instance, PEER_DECLINE).isEmpty()) {
            return false;
        } else if (!getActionsByType(instance, PEER_APPROVE).isEmpty() && getActionsByType(instance, PEER_DECLINE).isEmpty()) {
            return true;
        }
        return false;
    }

    public static List<WorkflowRequestPeer> recipientsDeclined(WorkflowRequest instance) {
        List<WorkflowRequestPeer> recipientsDeclined = new ArrayList<>();
        Set<WorkflowRequestPeer> recipients = instance.getRecipients();
        Set<WorkflowRequestLog> actions = getActionsByType(instance, PEER_DECLINE);
        for (WorkflowRequestLog action : actions) {
            for (WorkflowRequestPeer role : recipients) {
                if (action.getActorId().equals(role.getRecipient().getId())) {
                    recipientsDeclined.add(role);
                }
            }
        }
        return recipientsDeclined;
    }

    public static List<WorkflowRequestPeer> recipientsApproved(WorkflowRequest instance) {
        List<WorkflowRequestPeer> recipientsApproved = new ArrayList<>();
        Set<WorkflowRequestPeer> recipients = instance.getRecipients();
        Set<WorkflowRequestLog> actions = getActionsByType(instance, PEER_APPROVE);
        for (WorkflowRequestLog action : actions) {
            for (WorkflowRequestPeer role : recipients) {
                if (action.getProcessInstance().getRequestType().equals(WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST)) {
                    if (action.getActorId().equals(role.getRecipient().getId()) && role.getPeerShiftId().equals(action.getShiftId())) {
                        recipientsApproved.add(role);
                    }
                } else {
                    if (action.getActorId().equals(role.getRecipient().getId())) {
                        recipientsApproved.add(role);
                    }
                }
            }
        }
        return recipientsApproved;
    }

//    public static WflOriginatorInstanceBriefInfoDto transport(WorkflowRequest instance) {
//        return new WflOriginatorInstanceBriefInfoDto(
//                instance.getId(), instance.getApplyStrategy(), instance.getProtoProcess().getType().getType().name(),
//                instance.getCreated().getMillis(), instance.getExpiration(), instance.getDescription());
//    }

    public static Boolean isSwapOrWip(WorkflowRequest instance) {
        WorkflowRequestTypeDict type = instance.getProtoProcess().getType().getType();
        return type.equals(WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST) || type.equals(WorkflowRequestTypeDict.WIP_REQUEST);
    }

    public static Boolean isProvidedEmployeeChosen(WorkflowRequest instance, WorkflowRequestPeer peer) {
        if (instance.getChosenPeerId() == null) {
            return false;
        }
        return instance.getChosenPeerId().equals(peer.getId());
    }

    public static Boolean employeeApprovedAsPeer(WorkflowRequest instance, WorkflowRequestPeer peer) {
        List<WorkflowRequestPeer> approved = recipientsApproved(instance);
        for (WorkflowRequestPeer peerInstance : approved) {
            if (peerInstance.equals(peer)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean employeeApprovedRequest(WorkflowRequest instance, Employee employee) {
        List<WorkflowRequestPeer> approved = recipientsApproved(instance);
        for (WorkflowRequestPeer peerInstance : approved) {
            if (peerInstance.getRecipient().getId().equals(employee.getId()) &&
                    peerInstance.getRecipient().getTenantId().equals(employee.getTenantId())) {
                return true;
            }
        }
        return false;
    }

    public static Boolean employeeDeclinedAsPeer(WorkflowRequest instance, WorkflowRequestPeer peer) {
        List<WorkflowRequestPeer> declined = recipientsDeclined(instance);
        for (WorkflowRequestPeer peerInstance : declined) {
            if (peerInstance.equals(peer)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean peerDidNotActYet(WorkflowRequest instance, WorkflowRequestPeer peer) {
        Map<WorkflowActionDict, Set<WorkflowRequestLog>> mapped = actionsMapped(instance);
        Iterator<Map.Entry<WorkflowActionDict, Set<WorkflowRequestLog>>> iterator = mapped.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WorkflowActionDict, Set<WorkflowRequestLog>> entry = iterator.next();
            Boolean actionBellongsToPeer = entry.getKey().equals(WorkflowActionDict.PEER_APPROVE) || entry.getKey().equals(WorkflowActionDict.PEER_DECLINE);
            if (actionBellongsToPeer) {
                for (WorkflowRequestLog actionLog : entry.getValue()) {
                    Boolean sameShift = actionLog.getShiftId().equals(peer.getPeerShiftId());
                    Boolean sameEmployee = actionLog.getActorId().equals(peer.getRecipient().getId());
                    if (sameShift && sameEmployee) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static Map<WorkflowActionDict, Set<WorkflowRequestLog>> actionsMapped(WorkflowRequest instance) {
        Map<WorkflowActionDict, Set<WorkflowRequestLog>> map = new HashMap<>();
        for (WorkflowRequestLog actionLog : instance.getActions()) {
            if (map.containsKey(actionLog.getAction())) {
                map.get(actionLog.getAction()).add(actionLog);
            } else {
                Set<WorkflowRequestLog> actionLogSet = new HashSet<>();
                actionLogSet.add(actionLog);
                map.put(actionLog.getAction(), actionLogSet);
            }
        }
        return map;
    }

    public static Boolean isShiftSwap(WorkflowRequest instance) {
        return instance.getProtoProcess().getType().getType().equals(WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST);
    }

    public static Long getActorLastActionDate(WorkflowRequest instance, WorkflowRequestPeer peerInstance) {
        Set<WorkflowRequestLog> actionLogs = instance.getActions();
        WorkflowRequestLog latest = null;
        for (WorkflowRequestLog action : actionLogs) {
            if (action.getActorId().equals(peerInstance.getRecipient().getId())) {
                if (latest == null) {
                    latest = action;
                } else if (latest.getCreated().getMillis() < action.getCreated().getMillis()) {
                    latest = action;
                }
            }
        }
        return latest != null ? latest.getCreated().getMillis() : null;
    }

    public static String getActorLastCommentIfExists(WorkflowRequest instance, WorkflowRequestPeer peerInstance) {
        Set<WorkflowRequestLog> actionLogs = instance.getActions();
        WorkflowRequestLog latest = null;
        for (WorkflowRequestLog action : actionLogs) {
            if (action.getActorId().equals(peerInstance.getRecipient().getId())) {
                if (latest == null) {
                    latest = action;
                } else if (latest.getCreated().getMillis() < action.getCreated().getMillis()) {
                    latest = action;
                }
            }
        }
        return latest != null ? latest.getComment() : null;
    }

    public static Long getLastActionDate(WorkflowRequest instance) {
        Long latest = null;
        for (WorkflowRequestLog actionLog : instance.getActions()) {
            if (latest == null) {
                latest = actionLog.getCreated().getMillis();
            } else if (latest < actionLog.getCreated().getMillis()) {
                latest = actionLog.getCreated().getMillis();
            }
        }
        return latest;
    }

    public static String getLastComment(WorkflowRequest instance) {
        WorkflowRequestLog latest = null;
        for (WorkflowRequestLog actionLog : instance.getActions()) {
            if (latest == null) {
                latest = actionLog;
            } else if (latest.getCreated().getMillis() < actionLog.getCreated().getMillis()) {
                latest = actionLog;
            }
        }
        return latest != null ? latest.getComment() : " ";
    }

    public static Boolean declinedByManager(WorkflowRequest instance, WorkflowRequestPeer peer) {
        for (WorkflowRequestPeer peerInstance : instance.getRecipients()) {
            if (peerInstance.equals(peer)) {
                return peerInstance.getDeclinedByManager();
            }
        }
        return false;
    }

    public static void switchApprovalForDecline(WorkflowRequest instance, WorkflowRequestPeer peer) {
        for (WorkflowRequestLog actionLog : instance.getActions()) {
            if (actionLog.getAction().equals(WorkflowActionDict.PEER_APPROVE) &&
                    actionLog.getActorId().equals(peer.getRecipient().getId()) &&
                    actionLog.getShiftId().equals(peer.getPeerShiftId())) {
                actionLog.setAction(WorkflowActionDict.PEER_DECLINE);
            }
        }
    }

    public static WorkflowRequestLog getLastAction(WorkflowRequest instance) {
        WorkflowRequestLog latest = null;
        for (WorkflowRequestLog actionLog : instance.getActions()) {
            if (latest == null) {
                latest = actionLog;
            } else if (actionLog.getCreated().getMillis() > latest.getCreated().getMillis()) {
                latest = actionLog;
            }
        }
        return latest;
    }

    public static Boolean doesNotContainErrors(WorkflowRequest instance) {
        WorkflowRequestLog latest = null;
        for (WorkflowRequestLog actionLog : instance.getActions()) {
            if (actionLog.getAction().equals(WorkflowActionDict.ERROR_IN_ACTION)) {
                return false;
            }
        }
        return true;
    }

    public static TranslationParam[] errorPrm(Throwable throwable) {
        TranslationParam[] params = {new TranslationParam("error", throwable.getMessage())};
        return params;
    }

    public static TranslationParam[] errorPrm(Class cls) {
        TranslationParam[] params = {new TranslationParam("type", cls.getSimpleName())};
        return params;
    }

    public static Locale locale(Employee employee) {
        String lang = employee.getLanguage() != null ? employee.getLanguage() : "en";
        String country = employee.getCountry() != null ? employee.getCountry() : "US";
        return new Locale(lang, country);
    }

    public static Locale locale(UserAccount account) {
        String lang = account.getLanguage() != null ? account.getLanguage() : "en";
        String country = account.getCountry() != null ? account.getCountry() : "US";
        return new Locale(lang, country);
    }

    private static class WorkflowStatusChangeMatrix {

        //todo:: to be refactored after description approval

        public WorkflowRequestStatusDict getStatus(
                StatusView statusView, WorkflowRequest instance, WorkflowRequestPeer peer
        ) throws WorkflowServerException {
            Boolean requestHasPeers = isSwapOrWip(instance);
            Boolean peerDeclinedByManager = false;
            Boolean peerApprovedButDeclinedByManager = false;
            Boolean peerDidNotMakeDecisionYet = false;
            Boolean peerDeclinedRequest = false;
            Boolean peerApprovedRequest = false;
            Boolean peerApprovedAndChosen = false;

            if (peer != null) {
                peerApprovedRequest = employeeApprovedAsPeer(instance, peer);
                peerDeclinedByManager = declinedByManager(instance, peer);
                peerApprovedButDeclinedByManager = peerApprovedRequest && !isProvidedEmployeeChosen(instance, peer);
                peerDidNotMakeDecisionYet = peerDidNotActYet(instance, peer);
                peerDeclinedRequest = employeeDeclinedAsPeer(instance, peer);
                peerApprovedAndChosen = isProvidedEmployeeChosen(instance, peer) && peerApprovedRequest;
            }

            switch (instance.getStatus()) {
                case TERMINATED:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_REQUEST_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerApprovedRequest) {
                                return WorkflowRequestStatusDict.PEER_APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else {
                                return WorkflowRequestStatusDict.PEER_PENDING;
                            }
                    }
                case PROCESS_INITIATED:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            if (requestHasPeers) {
                                if (recipientsApproved(instance).size() > 0) {
                                    return WorkflowRequestStatusDict.ADMIN_PENDING;
                                } else {
                                    return WorkflowRequestStatusDict.PEER_PENDING;
                                }
                            } else {
                                return WorkflowRequestStatusDict.ADMIN_PENDING;
                            }
                        case PEER_REQUEST_STATUS:
                            if (requestHasPeers) {
                                if (peerDeclinedByManager) {
                                    return WorkflowRequestStatusDict.DECLINED;
                                } else if (peerApprovedRequest) {
                                    return WorkflowRequestStatusDict.PEER_APPROVED;
                                } else if (peerDidNotMakeDecisionYet) {
                                    return WorkflowRequestStatusDict.PEER_PENDING;
                                } else if (peerDeclinedRequest) {
                                    return WorkflowRequestStatusDict.PEER_DECLINED;
                                }
                            } else {
                                return WorkflowRequestStatusDict.DECLINED;
                            }
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (requestHasPeers) {
                                if (peerApprovedRequest) {
                                    return WorkflowRequestStatusDict.PEER_APPROVED;
                                } else if (peerDeclinedRequest) {
                                    return WorkflowRequestStatusDict.PEER_DECLINED;
                                } else {
                                    return WorkflowRequestStatusDict.PEER_PENDING;
                                }
                            } else {
                                return WorkflowRequestStatusDict.ADMIN_PENDING;
                            }
                    }
                case APPROVED:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.APPROVED;
                        case PEER_REQUEST_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerApprovedAndChosen) {
                                return WorkflowRequestStatusDict.APPROVED;
                            } else if (peerApprovedButDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.WITHDRAWN;
                            }
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerApprovedAndChosen) {
                                return WorkflowRequestStatusDict.APPROVED;
                            } else if (peerApprovedButDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.WITHDRAWN;
                            }
                    }
                case REMOVED:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.DELETED;
                        case PEER_REQUEST_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.WITHDRAWN;
                            } else if (peerApprovedRequest) {
                                return WorkflowRequestStatusDict.PEER_APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            }
                    }
                case FAILED_TO_SUBMIT:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_REQUEST_STATUS:
                            //todo:: review it
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_TO_PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                    }
                case DECLINED_BY_PEERS:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.PEER_DECLINED;
                        case PEER_REQUEST_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerApprovedRequest) {
                                return WorkflowRequestStatusDict.PEER_APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else {
                                return WorkflowRequestStatusDict.PEER_PENDING;
                            }
                    }
                case DECLINED_BY_MANAGERS:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_REQUEST_STATUS:
                            return WorkflowRequestStatusDict.DECLINED;
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerApprovedRequest) {
                                return WorkflowRequestStatusDict.PEER_APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else {
                                return WorkflowRequestStatusDict.PEER_PENDING;
                            }
                    }
                case READY_FOR_ACTION:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.APPROVED;
                        case PEER_REQUEST_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.WITHDRAWN;
                            } else if (peerApprovedAndChosen) {
                                return WorkflowRequestStatusDict.APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerApprovedButDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            }
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.WITHDRAWN;
                            } else if (peerApprovedAndChosen) {
                                return WorkflowRequestStatusDict.APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerApprovedButDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            }
                    }
                case READY_FOR_ADMIN:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.ADMIN_PENDING;
                        case PEER_REQUEST_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerApprovedRequest) {
                                return WorkflowRequestStatusDict.PEER_APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.PEER_PENDING;
                            }
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerApprovedRequest) {
                                return WorkflowRequestStatusDict.PEER_APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.PEER_PENDING;
                            }
                    }
                case ACTION_IN_PROGRESS:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.APPROVED;
                        case PEER_REQUEST_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.WITHDRAWN;
                            } else if (peerApprovedAndChosen) {
                                return WorkflowRequestStatusDict.APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerApprovedButDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            }
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (peerDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            } else if (peerDidNotMakeDecisionYet) {
                                return WorkflowRequestStatusDict.WITHDRAWN;
                            } else if (peerApprovedAndChosen) {
                                return WorkflowRequestStatusDict.APPROVED;
                            } else if (peerDeclinedRequest) {
                                return WorkflowRequestStatusDict.PEER_DECLINED;
                            } else if (peerApprovedButDeclinedByManager) {
                                return WorkflowRequestStatusDict.DECLINED;
                            }
                    }
                case ACTION_COMPLETE_SUCCESS:
                    switch (statusView) {
                        case PRIVILEGED_STATUS:
                            return WorkflowRequestStatusDict.APPROVED;
                        case PEER_REQUEST_STATUS:
                            if (requestHasPeers) {
                                if (peerDeclinedByManager) {
                                    return WorkflowRequestStatusDict.DECLINED;
                                } else if (peerApprovedAndChosen) {
                                    return WorkflowRequestStatusDict.APPROVED;
                                } else if (peerApprovedButDeclinedByManager) {
                                    return WorkflowRequestStatusDict.DECLINED;
                                } else if (peerDeclinedRequest) {
                                    return WorkflowRequestStatusDict.PEER_DECLINED;
                                } else if (peerDidNotMakeDecisionYet) {
                                    return WorkflowRequestStatusDict.WITHDRAWN;
                                }
                            } else {
                                return WorkflowRequestStatusDict.DECLINED;
                            }
                        case PEER_TO_PRIVILEGED_STATUS:
                            if (requestHasPeers) {
                                if (peerDeclinedByManager) {
                                    return WorkflowRequestStatusDict.DECLINED;
                                } else if (peerApprovedAndChosen) {
                                    return WorkflowRequestStatusDict.APPROVED;
                                } else if (peerApprovedButDeclinedByManager) {
                                    return WorkflowRequestStatusDict.DECLINED;
                                } else if (peerDeclinedRequest) {
                                    return WorkflowRequestStatusDict.PEER_DECLINED;
                                } else if (peerDidNotMakeDecisionYet) {
                                    return WorkflowRequestStatusDict.WITHDRAWN;
                                }
                            } else {
                                return WorkflowRequestStatusDict.ADMIN_PENDING;
                            }
                    }
                case EXPIRED: {
                    return WorkflowRequestStatusDict.EXPIRED;
                }
                case ACTION_COMPLETE_WITH_ERRORS: {
                    return WorkflowRequestStatusDict.DECLINED;
                }
            }
            throw new WorkflowServerException(REQUEST_STATUS_CALCULATION_ERROR, "No Such status exception");
        }
    }
}
