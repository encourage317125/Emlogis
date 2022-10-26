package com.emlogis.common.services.workflow.process.intance;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.description.annotations.RequestDescriptionBuilderQualifierImpl;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.EmployeeTeam;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;
import com.emlogis.model.workflow.entities.*;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.workflow.WflUtil;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.AvailabilityRequestSubtype.NONE;
import static com.emlogis.workflow.enums.WorkflowRoleDict.*;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.PROCESS_INITIATED;

/**
 * Created by bbox on 22.10.15.
 */
@Stateless
@Local(value = WorkflowRequestInitialDataSearchService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WorkflowRequestInitialDataSearchServiceImpl implements WorkflowRequestInitialDataSearchService {

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager em;

    @EJB
    private WorkflowRequestService workflowRequestService;

    @EJB
    WorkflowRequestPeerService peerService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private TeamService teamService;

    @EJB
    private RequestRoleProxy requestRoleProxy;

    @Inject
    private Instance<DescriptionBuilder> descriptionBuilder;

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    @EJB
    private ShiftService shiftService;

    @EJB
    private AccountUtilService accountUtilService;

    @Override
    public WorkflowRequest initialize(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            String shiftId,
            Long expiration,
            String data,
            Integer recipientsSize,
            Map<String, List<String>> assignments
    ) {
        WorkflowRequest request = null;
        switch (abstractRequest.getType().getType()) {
            case SHIFT_SWAP_REQUEST: {
                request = initSwapRequest(abstractRequest, userAccount, employee, shiftId, null, expiration, data, recipientsSize);
                break;
            }
            case WIP_REQUEST: {
                request = initWipRequest(abstractRequest, userAccount, employee, shiftId, null, expiration, data, recipientsSize);
                break;
            }
        }
        List<UserAccount> managers = requestRoleProxy.findManagers(request.getRequestType(), request.getInitiator());
        for (UserAccount manager : managers) {
            request.getManagers().add(new WorkflowRequestManager(request, manager));
        }

        request.setManagerIds(managerIds(managers));
        request.setManagerNames(managerNames(managers));

        Iterator<Map.Entry<String, List<String>>> iterator = assignments.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> item = iterator.next();
            Employee peerEmployee = employeeService.getEmployee(new PrimaryKey(userAccount.getTenantId(), item.getKey()));
            for (String peerShiftId : item.getValue()) {
                Team team = null;
                Shift shift = null;
                Site site = null;
                if (peerShiftId.equals("EMPTY")) {
                    team = findHomeTeam(userAccount, employee);
                } else {
                    shift = shiftService.getShift(new PrimaryKey(userAccount.getTenantId(), peerShiftId));
                    team = teamService.getTeam(new PrimaryKey(userAccount.getTenantId(), shift.getTeamId()));
                }
                site = teamService.getSite(team);
                WorkflowRequestPeer peer = new WorkflowRequestPeer(request, userAccount, peerEmployee,
                        team, site, shift);
                request.getRecipients().add(peer);
            }
        }

        request = workflowRequestService.merge(request);
        request = workflowRequestService.update(descriptions(request));
        if (request.getRequestStatus().isFinalState()) {
            request = rebornRequest(request);
        }
        return workflowRequestService.update(request);
    }


    @Override
    public WorkflowRequest initialize(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            Long requestDate,
            String shiftId,
            AvailabilityRequestSubtype subtype,
            Long expiration,
            String data
    ) {
        WorkflowRequest request = null;
        switch (abstractRequest.getType().getType()) {
            case AVAILABILITY_REQUEST: {
                request = initAvailabilityRequest(abstractRequest, userAccount, employee, subtype, requestDate, expiration, data, 0);
                break;
            }
            case TIME_OFF_REQUEST: {
                request = initTimeOffRequest(abstractRequest, userAccount, employee, requestDate, expiration, data, 0);
                break;
            }
            case OPEN_SHIFT_REQUEST: {
                request = initOpenShiftRequest(abstractRequest, userAccount, employee, shiftId, requestDate, expiration);
                break;
            }
        }
        List<UserAccount> managers = requestRoleProxy.findManagers(request.getRequestType(), request.getInitiator());
        for (UserAccount manager : managers) {
            request.getManagers().add(new WorkflowRequestManager(request, manager));
        }

        request.setManagerIds(managerIds(managers));
        request.setManagerNames(managerNames(managers));

        request = workflowRequestService.merge(request);
        request = workflowRequestService.update(descriptions(request));
        if (request.getRequestStatus().isFinalState()) {
            request = rebornRequest(request);
        }
        return workflowRequestService.update(request);
    }

    private WorkflowRequest descriptions(WorkflowRequest request) {
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

    private WorkflowRequest rebornRequest(
            WorkflowRequest request
    ) {
        request.setStatus(PROCESS_INITIATED);
        request.setActions(new HashSet<WorkflowRequestLog>());
        request.setRequestStatus(WflUtil.getRequestStatus(request));
        List<UserAccount> managers = requestRoleProxy.findManagers(request.getRequestType(), request.getInitiator());
        for (UserAccount manager : managers) {
            request.getManagers().add(new WorkflowRequestManager(request, manager));
        }

        request.setManagerIds(managerIds(managers));
        request.setManagerNames(managerNames(managers));

        request = workflowRequestService.update(request);

        for (WorkflowRequestPeer peer : request.getRecipients()) {
            peer.setPeerStatus(WflUtil.getPeerAggregatedRequestStatus(request, peer));
            peerService.update(peer);
        }
        request.getActions().clear();
        return workflowRequestService.update(request);
    }

    private Object[] shiftData(String tenantId, String shiftId, String employeeId, String accountId) {
        String sql = " " +
                "SELECT " +
                "  s.startDateTime, " + //0
                "  s.endDateTime, " + //1
                "  s.skillId, " + //2
                "  s.skillName, " + //3
                "  s.teamId, " + //4
                "  s.teamName, " + //5
                "  s.siteName, " + //6
                "  st.id," + //7
                "  st.language as siteLanguage, " + //8
                "  st.country as siteCountry, " + //9
                "  st.timeZone as siteTimeZone, " + //10
                "  emp.language as emplLanguage, " + //11
                "  emp.country as emplCountry, " + //12
                "  ua.timeZone as accountTimeZone" + //13
                " FROM Shift s " +
                "  JOIN Site st on (st.name = s.siteName AND st.tenantId =:tenantId) " +
                "  JOIN Employee emp on (emp.tenantId =:tenantId AND emp.id =:employeeId) " +
                "  JOIN UserAccount ua on (ua.tenantId =:tenantId AND ua.id =:accountId) " +
                " WHERE s.id =:shiftId AND s.tenantId =:tenantId ";
        Query query = em.createNativeQuery(sql);
        query.setParameter("shiftId", shiftId);
        query.setParameter("tenantId", tenantId);
        query.setParameter("employeeId", employeeId);
        query.setParameter("accountId", accountId);
        return (Object[]) query.getSingleResult();
    }


    public DateTimeZone read(byte[] asBytes) {
        final String tz = new ByteBufferInput(asBytes).readString();
        if ("".equals(tz)) {
            return DateTimeZone.getDefault();
        }
        return DateTimeZone.forID(tz);
    }


    private WorkflowRequest initWipRequest(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            String shiftId,
            Long requestDate,
            Long expiration,
            String data,
            Integer recipientsSize
    ) {
        Object[] initSql = shiftData(userAccount.getTenantId(), shiftId, employee.getId(), userAccount.getId());
        WorkflowRequest workflowRequest = new WorkflowRequest(
                abstractRequest,
                PROCESS_INITIATED,
                employee,
                employee,
                expiration,
                shiftId,
                ((java.sql.Timestamp) initSql[0]).getTime(), //shift start date time
                ((java.sql.Timestamp) initSql[1]).getTime(), //shift end date time
                (String) initSql[2], //shift skill id
                (String) initSql[3], //shift skill name
                (String) initSql[4], //shift team id
                (String) initSql[5], //shift team name
                (String) initSql[7], //shift site id
                (String) initSql[6], //shift site name
                accountUtilService.getActualTimeZone(userAccount), //submitter time zone
                (initSql[12] != null ? (String) initSql[12] : (String) initSql[9]), //country string
                (initSql[11] != null ? (String) initSql[11] : (String) initSql[8]), //language string
                requestDate,
                NONE,
                data,
                recipientsSize
        );
        return workflowRequest;
    }

    private WorkflowRequest initSwapRequest(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            String shiftId,
            Long requestDate,
            Long expiration,
            String data,
            Integer recipientsSize
    ) {
        Object[] initSql = shiftData(userAccount.getTenantId(), shiftId, employee.getId(), userAccount.getId());
        WorkflowRequest workflowRequest = new WorkflowRequest(
                abstractRequest,
                PROCESS_INITIATED,
                employee,
                employee,
                expiration,
                shiftId,
                ((java.sql.Timestamp) initSql[0]).getTime(), //shift start date time
                ((java.sql.Timestamp) initSql[1]).getTime(), //shift end date time
                (String) initSql[2], //shift skill id
                (String) initSql[3], //shift skill name
                (String) initSql[4], //shift team id
                (String) initSql[5], //shift team name
                (String) initSql[7], //shift site id
                (String) initSql[6], //shift site name
                accountUtilService.getActualTimeZone(userAccount), //submitter time zone
                (initSql[12] != null ? (String) initSql[12] : (String) initSql[9]), //country string
                (initSql[11] != null ? (String) initSql[11] : (String) initSql[8]), //language string
                requestDate,
                NONE,
                data,
                recipientsSize
        );
        return workflowRequest;
    }

    private WorkflowRequest initOpenShiftRequest(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            String shiftId,
            Long requestDate,
            Long expiration
    ) {

        Object[] initSql = shiftData(userAccount.getTenantId(), shiftId, employee.getId(), userAccount.getId());

        TaskShiftBriefInfoDto data = new TaskShiftBriefInfoDto(
                shiftId,
                ((java.sql.Timestamp) initSql[0]).getTime(),
                ((java.sql.Timestamp) initSql[1]).getTime(),
                (String) initSql[4],
                (String) initSql[5],
                (String) initSql[2],
                (String) initSql[3]);
        WorkflowRequest request = new WorkflowRequest(
                abstractRequest,
                PROCESS_INITIATED,
                employee,
                employee,
                expiration,
                shiftId,
                ((java.sql.Timestamp) initSql[0]).getTime(), //shift start date time
                ((java.sql.Timestamp) initSql[1]).getTime(), //shift end date time
                (String) initSql[2], //shift skill id
                (String) initSql[3], //shift skill name
                (String) initSql[4], //shift team id
                (String) initSql[5], //shift team name
                (String) initSql[7], //shift site id
                (String) initSql[6], //shift site name
                accountUtilService.getActualTimeZone(userAccount), //submitter time zone
                (initSql[12] != null ? (String) initSql[12] : (String) initSql[9]), //country string
                (initSql[11] != null ? (String) initSql[11] : (String) initSql[8]), //language string
                requestDate,
                NONE,
                EmlogisUtils.toJsonString(data),
                0
        );
        postedOpenShiftService.markPostedOpenShiftAsRequested(request.getInitiator().getTenantId(),
                request.getInitiator().getId(), request.getSubmitterShiftId());
        return request;
    }

    private WorkflowRequest initTimeOffRequest(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            Long requestDate,
            Long expiration,
            String data,
            Integer recipientsSize
    ) {
        Team team = findHomeTeam(userAccount, employee);
        Site site = teamService.getSite(team);
        WorkflowRequest workflowRequest = new WorkflowRequest(
                abstractRequest,
                PROCESS_INITIATED,
                employee,
                employee,
                expiration,
                null, // shift id
                null, //shift start date time
                null, //shift end date time
                null, //shift skill id
                null, //shift skill name
                team.getId(), //shift team id
                team.getName(), //shift team name
                site.getId(), //shift site id
                site.getName(), //shift site name
                site.getTimeZone(), //submitter time zone
                site.getCountry(), //country string
                site.getLanguage(), //language string
                requestDate,
                NONE,
                data,
                recipientsSize
        );
        return workflowRequest;

    }

    private WorkflowRequest initAvailabilityRequest(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            AvailabilityRequestSubtype subtype,
            Long requestDate,
            Long expiration,
            String data,
            Integer recipientsSize
    ) {
        Team team = findHomeTeam(userAccount, employee);
        Site site = teamService.getSite(team);
        WorkflowRequest workflowRequest = new WorkflowRequest(
                abstractRequest,
                PROCESS_INITIATED,
                employee,
                employee,
                expiration,
                null, // shift id
                null, //shift start date time
                null, //shift end date time
                null, //shift skill id
                null, //shift skill name
                team.getId(), //shift team id
                team.getName(), //shift team name
                site.getId(), //shift site id
                site.getName(), //shift site name
                site.getTimeZone(), //submitter time zone
                site.getCountry(), //country string
                site.getLanguage(), //language string
                requestDate,
                subtype,
                data,
                recipientsSize
        );
        return workflowRequest;
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

}
