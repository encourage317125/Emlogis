package com.emlogis.common.facade.workflow.dashboard.query;

/**
 * Created by user on 21.08.15.
 */

import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.manager.WorkflowRequestManagerService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.search.WflProcessSearchService;
import com.emlogis.common.services.workflow.process.update.RequestActionService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.emlogis.model.workflow.dto.filter.ManagerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.PeerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.SubmitterRequestsFilterDto;
import com.emlogis.model.workflow.dto.process.response.WflOriginatorInstanceBriefInfoDto;
import com.emlogis.model.workflow.dto.task.ManagerRequestDetailsInfoDto;
import com.emlogis.model.workflow.dto.task.RequestDetailsBriefInfoDto;
import com.emlogis.model.workflow.dto.task.RequestDetailsBriefInfoOld;
import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestManager;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.workflow.WflUtil;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST;
import static com.emlogis.workflow.enums.status.WorkflowRequestStatusDict.PEER_PENDING;
import static com.emlogis.workflow.exception.ExceptionCode.MANAGER_REQUEST_QUERY_FAIL;
import static edu.emory.mathcs.backport.java.util.Arrays.asList;

@Stateful
@Local(RequestQueryFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestQueryFacadeImpl implements RequestQueryFacade {

    private final static Logger logger = Logger.getLogger(RequestQueryFacadeImpl.class);

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private WorkflowRequestPeerService workflowRequestPeerService;

    @EJB
    private WorkflowRequestService workflowRequestService;

    @EJB
    private RequestActionService requestActionService;

    @EJB
    private WflProcessSearchService wflProcessSearchService;

    @EJB
    private WorkflowRequestManagerService workflowRequestManagerService;

    @EJB
    private RequestRoleProxy requestRoleProxy;

    @Inject
    private Instance<DescriptionBuilder> descriptionBuilder;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<? extends AbstractRequestDetailsInfoDto> getEmployeeTasks(
            Employee employee, PeerRequestsFilterDto filterDto) throws WorkflowServerException {

        ResultSet<Object[]> peerProcessResultSet = wflProcessSearchService.peerProcessSearch(filterDto, employee);

        List<RequestDetailsBriefInfoDto> tasks = new ArrayList<>();

        for (Object[] row : peerProcessResultSet.getResult()) {
            WorkflowRequestTypeDict requestTypeDict = WorkflowRequestTypeDict.valueOf((String) row[1]);
            String description = null;
            //todo:: enhance
            if (requestTypeDict.equals(SHIFT_SWAP_REQUEST)) {
                description = (String) row[13];
            } else {
                description = (String) row[13];
            }

            Set<String> peerStatusesSet = new HashSet<>();
            String peerStatusesStr = (String) row[25];
            if (!peerStatusesStr.isEmpty()) {
                peerStatusesSet.addAll(asList((peerStatusesStr).split("@")));
            }
            String status = getPeerAggregatedRequestStatus(peerStatusesSet, employee).name();

            RequestDetailsBriefInfoDto taskBriefInfoDto = new RequestDetailsBriefInfoDto(
                    row[0] == null ? null : ((String) row[0]),
                    (String) row[1],
                    row[2] == null ? null : ((Timestamp) row[2]).getTime(),
                    (row[3] == null ? null : ((Timestamp) row[3]).getTime()),
                    (row[4] == null ? null : ((Timestamp) row[4]).getTime()),
                    status,
                    new TaskShiftBriefInfoDto(
                            (String) row[20],
                            (row[21] == null ? null : ((BigInteger) row[21]).longValue()),
                            (row[22] == null ? null : ((BigInteger) row[22]).longValue()),
                            (String) row[9],
                            (String) row[8],
                            (String) row[23],
                            (String) row[24]),
                    (String) row[7],
                    (String) row[6],
                    WorkflowRequestStatusDict.valueOf((String) row[5]).equals(PEER_PENDING),
                    (String) row[8],
                    (String) row[9],
                    (String) row[10],
                    (String) row[11],
                    description
            );
            taskBriefInfoDto.setIsRead(parseQueryConcatenatedIsRead((byte[]) row[26], employee.getId()));
            tasks.add(taskBriefInfoDto);
        }

        ResultSet<RequestDetailsBriefInfoDto> result = new ResultSet<>();
        result.setResult(tasks);
        result.setTotal(peerProcessResultSet.getTotal());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<? extends AbstractRequestDetailsInfoDto> getEmployeeTasksOld(
            Employee employee, PeerRequestsFilterDto filterDto) throws WorkflowServerException {

        ResultSet<Object[]> peerProcessResultSet = wflProcessSearchService.peerProcessSearchOld(filterDto, employee);

        List<RequestDetailsBriefInfoOld> tasks = new ArrayList<>();

        for (Object[] row : peerProcessResultSet.getResult()) {
            RequestDetailsBriefInfoOld taskBriefInfoDto = new RequestDetailsBriefInfoOld(
                    row[0] == null ? null : ((String) row[0]),
                    (String) row[1],
                    row[2] == null ? null : ((Timestamp) row[2]).getTime(),
                    (row[3] == null ? null : ((Timestamp) row[3]).getTime()),
                    (row[4] == null ? null : ((Timestamp) row[4]).getTime()),
                    (String) row[5],
                    new TaskShiftBriefInfoDto(
                            (String) row[14],
                            (row[15] == null ? null : ((Timestamp) row[15]).getTime()),
                            (row[16] == null ? null : ((Timestamp) row[16]).getTime()),
                            (String) row[17],
                            (String) row[18],
                            (String) row[19],
                            (String) row[20]
                    ),
                    (String) row[9],
                    (String) row[8],
                    new TaskShiftBriefInfoDto(
                            (String) row[21],
                            (row[22] == null ? null : ((Timestamp) row[22]).getTime()),
                            (row[23] == null ? null : ((Timestamp) row[23]).getTime()),
                            (String) row[24],
                            (String) row[25],
                            (String) row[26],
                            (String) row[27]
                    ),
                    RequestTechnicalStatusDict.PROCESS_INITIATED.equals(row[28]),
                    (String) row[10],
                    (String) row[11],
                    (String) row[12],
                    (String) row[13],
                    (row[29] == null ? false : (Boolean) row[29]),
                    (String) row[6]
            );
            tasks.add(taskBriefInfoDto);
        }

        ResultSet<RequestDetailsBriefInfoOld> result = new ResultSet<>();
        result.setResult(tasks);
        result.setTotal(peerProcessResultSet.getTotal());

        return result;
    }


    @Override
    public ResultSet<ManagerRequestDetailsInfoDto> getManagerTasks(
            UserAccount account, ManagerRequestsFilterDto filterDto, AccountACL acl
    ) throws WorkflowServerException {
        try {
            ResultSet<Object[]> managerResultSet = wflProcessSearchService.managerProcessSearch(filterDto, account, acl);

            List<ManagerRequestDetailsInfoDto> managerTaskInfoDtos = new ArrayList<>();

            for (Object[] row : managerResultSet.getResult()) {
                TaskShiftBriefInfoDto taskShiftBriefInfoDto;
                try {
                    taskShiftBriefInfoDto = fromJsonString((String) row[14], TaskShiftBriefInfoDto.class);
                } catch (Exception e) {
                    taskShiftBriefInfoDto = null;
                }

                WorkflowRequestStatusDict status = WorkflowRequestStatusDict.valueOf((String) row[5]);
                ManagerRequestDetailsInfoDto managerTaskInfoDto = new ManagerRequestDetailsInfoDto(
                        row[0] == null ? null : ((String) row[0]),
                        (String) row[1],
                        row[2] == null ? null : ((Timestamp) row[2]).getTime(),
                        (row[3] == null ? null : ((Timestamp) row[3]).getTime()),
                        (row[4] == null ? null : ((Timestamp) row[4]).getTime()),
                        status.name(),
                        taskShiftBriefInfoDto,
                        (String) row[7],
                        (String) row[6],
                        (String) row[13],
                        (String) row[8],
                        (String) row[9],
                        (String) row[10],
                        (String) row[11],
                        (row[15] == null ? 0 : ((Number) row[15]).intValue()),
                        (row[16] == null ? 0 : ((Number) row[16]).intValue()),
                        (String) row[17],
                        (String) row[18],
                        (row[19] == null ? null : ((Timestamp) row[19]).getTime())
                );
                managerTaskInfoDto.setEmployeeStartDate(row[21] == null ? null : ((Date) row[21]).getTime());

                Boolean isRead;
                if (status.isFinalState()) {
                    isRead = true;
                } else {
                    Object irData = row[20];
                    if (irData instanceof Integer) {
                        isRead = !irData.equals(0);
                    } else {
                        isRead = (Boolean) row[20];
                    }
                    if (isRead == null) {
                        addWorkflowRequestManager(((String) row[0]), account);
                        isRead = false;
                    }
                }
                managerTaskInfoDto.setIsRead(isRead);

                managerTaskInfoDtos.add(managerTaskInfoDto);
            }

            ResultSet<ManagerRequestDetailsInfoDto> result = new ResultSet<>();
            result.setResult(managerTaskInfoDtos);
            result.setTotal(managerResultSet.getTotal());

            return result;
        } catch (Exception error) {
            TranslationParam[] params = {new TranslationParam("filter", filterDto.toString()),
                    new TranslationParam("error", error.getMessage())};
            String message = translator.getMessage(locale(account), "request.error.manager.query", params);
            logger.error(message, error);
            throw new WorkflowServerException(MANAGER_REQUEST_QUERY_FAIL,
                    message, error);
        }
    }

    private void addWorkflowRequestManager(String requestId, UserAccount manager) {
        WorkflowRequest workflowRequest = workflowRequestService.find(new PrimaryKey(manager.getTenantId(), requestId));
        Boolean isManager = requestRoleProxy.validateIsManager(workflowRequest.getRequestType(), manager,
                workflowRequest.getInitiator());
        if (isManager) {
            WorkflowRequestManager workflowRequestManager = new WorkflowRequestManager(workflowRequest, manager);
            workflowRequestManager.setIsRead(false);
            workflowRequestManager.setRequestStatus(WflUtil.getRequestStatus(workflowRequest));
            workflowRequest.getManagers().add(workflowRequestManager);
            List<UserAccount> managers = requestRoleProxy.findManagers(workflowRequest.getRequestType(),
                    workflowRequest.getInitiator());
            workflowRequest.setManagerIds(managerIds(managers));
            workflowRequest.setManagerNames(managerNames(managers));
            workflowRequestService.update(workflowRequest);
        }
    }

    private String aggregatedPeerStatus(
            String requestId,
            String requestStatus,
            Employee requestEmployee
    ) {
        WorkflowRequestStatusDict result = null;
        Set<WorkflowRequestPeer> requestPeerSet = new HashSet<>(
                workflowRequestPeerService.findPeers(new PrimaryKey(requestEmployee.getTenantId(), requestId), requestEmployee));
        for (WorkflowRequestPeer peer : requestPeerSet) {
            if (result == null) {
                result = peer.getPeerStatus();
            } else if (result.weight() < peer.getPeerStatus().weight()) {
                result = peer.getPeerStatus();
            }
        }
        return requestStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<WflOriginatorInstanceBriefInfoDto> getAllSubmittedRequests(
            Employee employee,
            SubmitterRequestsFilterDto filterDto
    ) throws WorkflowServerException {

        ResultSet<Object[]> submittedResultSet = wflProcessSearchService.submittedProcessSearch(filterDto, employee);

        List<WflOriginatorInstanceBriefInfoDto> submittedTaskInfoDtos = new ArrayList<>();

        for (Object[] row : submittedResultSet.getResult()) {

            WflOriginatorInstanceBriefInfoDto taskInfoDto = new WflOriginatorInstanceBriefInfoDto(
                    row[0] == null ? null : ((String) row[0]),
                    (String) row[1],
                    row[2] == null ? null : ((Timestamp) row[2]).getTime(),
                    (row[4] == null ? null : ((Timestamp) row[4]).getTime()),
                    (String) row[7]);

            taskInfoDto.setEventDate(row[3] == null ? null : ((Timestamp) row[3]).getTime());
            taskInfoDto.setStatus((String) row[5]);
            PrimaryKey requestPk = new PrimaryKey(employee.getTenantId(), (String) row[0]);
            taskInfoDto.setIsReadByManager(workflowRequestManagerService.readAtLeastByOneManager(
                    workflowRequestService.find(requestPk)));
            taskInfoDto.setIsReadByPeer(workflowRequestPeerService.readAtLeastByOnePeer(((String) row[0])));
            TaskShiftBriefInfoDto taskShiftBriefInfoDto;
            try {
                taskShiftBriefInfoDto = fromJsonString((String) row[6], TaskShiftBriefInfoDto.class);
            } catch (Exception e) {
                taskShiftBriefInfoDto = null;
            }
            taskInfoDto.setOriginatorShift(taskShiftBriefInfoDto);

            submittedTaskInfoDtos.add(taskInfoDto);
        }

        ResultSet<WflOriginatorInstanceBriefInfoDto> result = new ResultSet<>();
        result.setResult(submittedTaskInfoDtos);
        result.setTotal(submittedResultSet.getTotal());

        return result;
    }
}
