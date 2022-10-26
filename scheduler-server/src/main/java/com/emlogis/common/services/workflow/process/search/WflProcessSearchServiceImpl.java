package com.emlogis.common.services.workflow.process.search;

import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.workflow.process.proto.WflProcessService;
import com.emlogis.common.services.workflow.type.WflProcessTypeService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.CacheConstants;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.filter.ManagerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.PeerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.SubmitterRequestsFilterDto;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.cache.BasicCacheService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import static com.emlogis.common.Constants.EMLOGIS_PERSISTENCE_UNIT_NAME;
import static com.emlogis.common.ModelUtils.commaSeparatedQuotedValues;

/**
 * Created by user on 05.06.15.
 */
@Stateless
@Local(WflProcessSearchService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WflProcessSearchServiceImpl implements WflProcessSearchService {

    @PersistenceContext(unitName = EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    enum orderDir {
        ASC,
        DESC
    }

    enum orderBy {
        TEAM("r.submitterTeamName"),
        EMPLOYEE("r.submitterName"),
        REQ_TYPE("r.requestType"),
        SUBMITTED("r.created"),
        REQ_DATE("r.requestDate"),
        EXPIRES("r.expiration"),
        REQ_STATUS("r.requestStatus"),
        DESCRIPTION("r.description"),
        SHIFT_DATE("p.submitterShiftStartDateTime"),
        EMPLOYEE_START_DATE("e.startDate");

        private final String query;

        orderBy(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }
    }

    @EJB
    private WflProcessTypeService processTypeService;

    @EJB
    private WflProcessService protoProcessService;

    @EJB
    private SiteService siteService;

    @EJB
    private TeamService teamService;

    @EJB
    private BasicCacheService cacheService;

    @EJB
    private SessionService sessionService;

    @Override
    public ResultSet<Object[]> peerProcessSearch(PeerRequestsFilterDto filter, Employee requester) {
        boolean textSearch = StringUtils.isNotBlank(filter.getFullTextSearch());

        String sql =
            "SELECT " +
            "       r.id as id," + // 0
            "       r.requestType," + // 1
            "       r.created," + // 2
            "       r.requestDate," + // 3
            "       r.expiration," + // 4
            "       r.requestStatus," + // 5
            "       r.submitterId," + // 6
            "       r.submitterName," + // 7
            "       r.submitterTeamName," + // 8
            "       r.submitterTeamId," + // 9
            "       r.submitterSiteName," + // 10
            "       r.submitterSiteId," + // 11
            "       r.comment," + // 12
            "       r.peerDescription," + // 13
            "       r.data," + // 14
            "       r.peerCount," + // 15
            "       r.peerApprovedCount," + // 16
            "       r.reviewerId," + // 17
            "       r.reviewerName," + // 18
            "       r.reviewed, " + // 19
            "       r.submitterShiftId, " + // 20
            "       r.submitterShiftStartDateTime, " + // 21
            "       r.submitterShiftEndDateTime, " + // 22
            "       r.submitterShiftSkillId, " + // 23
            "       r.submitterShiftSkillName, " + // 24
            "       GROUP_CONCAT(p.fk_recipient_tenant_id, ':', p.fk_recipient_employee_id, ':', p.peerStatus SEPARATOR '@'), " + // 25
            "       GROUP_CONCAT(p.peerId, ':', p.isRead SEPARATOR '@') " + // 26
            "  FROM WorkflowRequestPeer p" +
            "       JOIN WorkflowRequest r ON (p.fk_wfl_process_instance_id = r.id and r.fk_initiator_tenant_id = :tenantId)" +
            " WHERE p.fk_recipient_employee_id = :employeeId " +
            (filter.getTypes().isEmpty() ? ""
                    : "  AND r.requestType IN (" + commaSeparatedQuotedValues(filter.getTypes()) + ") ") +
            (filter.getStatuses().isEmpty() ? ""
                    : "  AND p.peerStatus IN (" + commaSeparatedQuotedValues(filter.getStatuses()) + ") ") +
            (textSearch
                    ? " AND (r.submitterName LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR r.submitterTeamName LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR r.description LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR r.comment LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR p.peerName LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR p.peerTeamName LIKE '%" + filter.getFullTextSearch() + "%') "
                    : "");

        if (filter.getDateFrom() != null) {
            if (filter.getDateTo() != null) {
                sql += " AND r.created BETWEEN '" + new Timestamp(filter.getDateFrom()) + "' " +
                        "AND '" + new Timestamp(filter.getDateTo()) + "' ";
            } else {
                sql += " AND r.created BETWEEN '" + toZonedTimeStamp(filter.getDateFrom()) + "' " +
                        "AND '" + toZonedTimeStamp(null) + "' ";
            }
        }

        sql += " GROUP BY r.id ";

        Query countQuery = entityManager.createNativeQuery(
                "SELECT count(*) FROM (" + "SELECT * FROM (" + sql + ") as result GROUP BY result.id) x");
        countQuery.setParameter("employeeId", requester.getId());
        countQuery.setParameter("tenantId", requester.getTenantId());


        if (StringUtils.isNotBlank(filter.getOrderBy())) {
            orderBy ob = orderBy.valueOf(filter.getOrderBy());
            if (ob.equals(orderBy.DESCRIPTION)) {
                sql += " ORDER BY r.peerDescription";
            } else {
                sql += " ORDER BY " + orderBy.valueOf(filter.getOrderBy()).getQuery();
            }
            if (StringUtils.isNotBlank(filter.getOrderDir())) {
                sql += " " + orderDir.valueOf(filter.getOrderDir()) + " ";
            }
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", requester.getId());
        query.setParameter("tenantId", requester.getTenantId());
        query.setFirstResult(filter.getOffset());
        query.setMaxResults(filter.getLimit());

        ResultSet<Object[]> result = new ResultSet<>();
        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    @Override
    public ResultSet<Object[]> peerProcessSearchOld(PeerRequestsFilterDto filter, Employee requester) {
        boolean textSearch = StringUtils.isNotBlank(filter.getFullTextSearch());

        String sql =
            "SELECT r.id, r.requestType, r.created, r.requestDate, r.expiration, p.peerStatus, r.peerDescription, " + // 0 - 6
            "       r.comment, r.submitterId, r.submitterName, r.submitterTeamName, r.submitterTeamId, " + // 7 - 11
            "       r.submitterSiteName, r.submitterSiteId, p.submitterShiftId, p.submitterShiftStartDateTime, " + // 12 - 15
            "       p.submitterShiftEndDateTime, p.submitterShiftTeamId, p.submitterShiftTeamName, " + // 16 - 18
            "       p.submitterShiftSkillId, p.submitterShiftSkillName, p.peerShiftId, p.peerShiftStartDateTime, " + // 19 - 22
            "       p.peerShiftEndDateTime, p.peerShiftTeamId, p.peerShiftTeamName, p.peerShiftSkillId, " + // 23 - 26
            "       p.peerShiftSkillName, r.status, p.isRead " + // 27 - 29
            "  FROM WorkflowRequest r " +
            "       LEFT JOIN WorkflowRequestPeer p ON p.fk_wfl_process_instance_id = r.id " +
            " WHERE " +
            "       r.fk_initiator_tenant_id = :tenantId " +
            "   AND p.fk_recipient_employee_id = :employeeId " +
            (filter.getTypes().isEmpty() ? ""
                    : "  AND r.requestType IN (" + commaSeparatedQuotedValues(filter.getTypes()) + ") ") +
            (filter.getStatuses().isEmpty() ? ""
                    : "  AND p.peerStatus IN (" + commaSeparatedQuotedValues(filter.getStatuses()) + ") ") +
            (textSearch
                    ? " AND (r.submitterName LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR r.submitterTeamName LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR r.peerDescription LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR r.comment LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR p.peerName LIKE '%" + filter.getFullTextSearch() + "%' " +
                    "  OR p.peerTeamName LIKE '%" + filter.getFullTextSearch() + "%') "
                    : "");

        if (filter.getDateFrom() != null) {
            if (filter.getDateTo() != null) {
                sql += " AND r.created BETWEEN '" + new Timestamp(filter.getDateFrom()) + "' " +
                        "AND '" + new Timestamp(filter.getDateTo()) + "' ";
            } else {
                sql += " AND r.created BETWEEN '" + toZonedTimeStamp(filter.getDateFrom()) + "' " +
                        "AND '" + toZonedTimeStamp(null) + "' ";
            }
        }

        Query countQuery = entityManager.createNativeQuery("SELECT count(*) FROM (" + sql + ") x");
        countQuery.setParameter("employeeId", requester.getId());
        countQuery.setParameter("tenantId", requester.getTenantId());

        if (StringUtils.isNotBlank(filter.getOrderBy())) {
            orderBy ob = orderBy.valueOf(filter.getOrderBy());
            if (ob.equals(orderBy.DESCRIPTION)) {
                sql += " ORDER BY r.peerDescription";
            } else {
                sql += " ORDER BY " + orderBy.valueOf(filter.getOrderBy()).getQuery();
            }
            if (StringUtils.isNotBlank(filter.getOrderDir())) {
                sql += " " + orderDir.valueOf(filter.getOrderDir()) + " ";
            }
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", requester.getId());
        query.setParameter("tenantId", requester.getTenantId());
        query.setFirstResult(filter.getOffset());
        query.setMaxResults(filter.getLimit());

        ResultSet<Object[]> result = new ResultSet<>();
        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    @Override
    public ResultSet<Object[]> managerProcessSearch(
            ManagerRequestsFilterDto filter,
            UserAccount account,
            AccountACL acl) {
        boolean textSearch = StringUtils.isNotBlank(filter.getFullTextSearch());

        Collection<String> teamIds = getAclTeamIds(acl, account);
        if (teamIds == null) {
            throw new ValidationException(sessionService.getMessage("validation.acl.no.teams"));
        }

        String sql =
            "SELECT " + (textSearch ? " DISTINCT " : "") +
            "       r.id," + // 0
            "       r.requestType," + // 1
            "       r.created," + // 2
            "       r.requestDate," + // 3
            "       r.expiration," + // 4
            "       r.requestStatus," + // 5
            "       r.submitterId," + // 6
            "       r.submitterName," + // 7
            "       r.submitterTeamName," + // 8
            "       r.submitterTeamId," + // 9
            "       r.submitterSiteName," + // 10
            "       r.submitterSiteId," + // 11
            "       r.comment," + // 12
            "       r.managerDescription," + // 13
            "       r.data," + // 14
            "       r.peerCount," + // 15
            "       r.peerApprovedCount," + // 16
            "       r.reviewerId," + // 17
            "       r.reviewerName," + // 18
            "       r.reviewed, " + // 19
            "       (SELECT DISTINCT(isRead) FROM WorkflowRequestManager " +
            "         WHERE r.id = fk_wfl_process_instance_id " +
            "           AND fk_manager_account_id = :accountId) AS isRead, " + // 20
            "       e.startDate " + // 21
            "  FROM WorkflowRequest r LEFT JOIN Employee e ON e.id = r.submitterId AND e.tenantId = r.tenantId " +
            (textSearch ? " LEFT JOIN WorkflowRequestPeer p ON r.id = p.fk_wfl_process_instance_id " : "") +
            " WHERE r.fk_initiator_tenant_id = :tenantId " +
            (filter.getTeams().isEmpty() ? ""
                    : "  AND r.submitterTeamId IN (" + commaSeparatedQuotedValues(filter.getTeams()) + ") ") +
            (teamIds == null ? "" : teamIds.isEmpty() ? " AND r.submitterTeamId IS NULL " // it is means that forbidden all teams by ACL
                    : "  AND r.submitterTeamId IN (" + commaSeparatedQuotedValues(teamIds) + ") ") +
            (filter.getSites().isEmpty() ? ""
                    : "  AND r.submitterSiteId IN (" + commaSeparatedQuotedValues(filter.getSites()) + ") ") +
            (filter.getTypes().isEmpty() ? ""
                    : "  AND r.requestType IN (" + commaSeparatedQuotedValues(filter.getTypes()) + ") ") +
            (filter.getStatuses().isEmpty() ? ""
                    : "  AND r.requestStatus IN (" + commaSeparatedQuotedValues(filter.getStatuses()) + ") ") +
            (textSearch ?
                    "  AND (r.submitterName LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR r.submitterTeamName LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR r.managerDescription LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR r.comment LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR p.peerName LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR p.peerTeamName LIKE '%" + filter.getFullTextSearch() + "%')"
                    : "") + " AND " + QueryPattern.NOT_DELETED.val("e");

        if (filter.getDateFrom() != null) {
            if (filter.getDateTo() != null) {
                sql += " AND r.created BETWEEN '" + new Timestamp(filter.getDateFrom()) + "' " +
                        "AND '" + new Timestamp(filter.getDateTo()) + "' ";
            } else {
                sql += " AND r.created BETWEEN '" + toZonedTimeStamp(filter.getDateFrom()) + "' " +
                        "AND '" + toZonedTimeStamp(null) + "' ";
            }
        }

        Query countQuery = entityManager.createNativeQuery("SELECT count(*) FROM (" + sql + ") x");
        countQuery.setParameter("tenantId", account.getTenantId());
        countQuery.setParameter("accountId", account.getId());

        if (StringUtils.isNotBlank(filter.getOrderBy())) {
            orderBy ob = orderBy.valueOf(filter.getOrderBy());
            if (ob.equals(orderBy.DESCRIPTION)) {
                sql += " ORDER BY r.managerDescription";
            } else {
                sql += " ORDER BY " + orderBy.valueOf(filter.getOrderBy()).getQuery();
            }
            if (StringUtils.isNotBlank(filter.getOrderDir())) {
                sql += " " + orderDir.valueOf(filter.getOrderDir()) + " ";
            }
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", account.getTenantId());
        query.setParameter("accountId", account.getId());
        query.setFirstResult(filter.getOffset());
        query.setMaxResults(filter.getLimit());

        ResultSet<Object[]> result = new ResultSet<>();
        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    @Override
    public ResultSet<Object[]> submittedProcessSearch(SubmitterRequestsFilterDto filter, Employee requester) {
        boolean textSearch = StringUtils.isNotBlank(filter.getFullTextSearch());

        String sql =
            "SELECT " + (textSearch ? " DISTINCT " : "") +
            "       r.id," + // 0
            "       r.requestType," + // 1
            "       r.created," + // 2
            "       r.requestDate, " + // 3
            "       r.expiration, " + // 4
            "       r.requestStatus," + // 5
            "       r.data, " + // 6
            "       r.submitterDescription " + // 7
            "  FROM WorkflowRequest r " +
            (textSearch ? " LEFT JOIN WorkflowRequestPeer p ON r.id = p.fk_wfl_process_instance_id " : "") +
            " WHERE r.fk_initiator_tenant_id = :tenantId AND r.submitterId = :employeeId " +
            (filter.getTypes().isEmpty() ? ""
                    : "  AND r.requestType IN (" + commaSeparatedQuotedValues(filter.getTypes()) + ") ") +
            (filter.getStatuses().isEmpty() ? ""
                    : "  AND r.requestStatus IN (" + commaSeparatedQuotedValues(filter.getStatuses()) + ") ") +
            (textSearch
                    ? "  AND (r.submitterName LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR r.submitterTeamName LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR r.submitterDescription LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR r.comment LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR p.peerName LIKE '%" + filter.getFullTextSearch() + "%'" +
                    "   OR p.peerTeamName LIKE '%" + filter.getFullTextSearch() + "%')"
                    : "");

        if (filter.getDateFrom() != null) {
            if (filter.getDateTo() != null) {
                sql += " AND r.created BETWEEN '" + new Timestamp(filter.getDateFrom()) + "' " +
                        "AND '" + new Timestamp(filter.getDateTo()) + "' ";
            } else {
                sql += " AND r.created BETWEEN '" + toZonedTimeStamp(filter.getDateFrom()) + "' " +
                        "AND '" + toZonedTimeStamp(null) + "' ";
            }
        }

        Query countQuery = entityManager.createNativeQuery("SELECT count(*) FROM (" + sql + ") x");
        countQuery.setParameter("employeeId", requester.getId());
        countQuery.setParameter("tenantId", requester.getTenantId());

        if (StringUtils.isNotBlank(filter.getOrderBy())) {
            orderBy ob = orderBy.valueOf(filter.getOrderBy());
            if (ob.equals(orderBy.DESCRIPTION)) {
                sql += " ORDER BY r.submitterDescription";
            } else {
                sql += " ORDER BY " + orderBy.valueOf(filter.getOrderBy()).getQuery();
            }
            if (StringUtils.isNotBlank(filter.getOrderDir())) {
                sql += " " + orderDir.valueOf(filter.getOrderDir()) + " ";
            }
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", requester.getId());
        query.setParameter("tenantId", requester.getTenantId());
        query.setFirstResult(filter.getOffset());
        query.setMaxResults(filter.getLimit());

        ResultSet<Object[]> result = new ResultSet<>();
        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    @Override
    public Object[] managerPendingAndNewRequestCounts(boolean teamRequests, PrimaryKey userAccountPk, PrimaryKey employeePk) {
        String sql =
            "SELECT " +
            "  (SELECT count(DISTINCT fk_wfl_process_instance_id) FROM WorkflowRequestManager " +
            "    WHERE fk_manager_account_id = :accountId " +
            "      AND requestStatus IN ('ADMIN_PENDING')) AS pendingManagerRequests, " +
            "  (SELECT count(DISTINCT r.id) FROM WorkflowRequestManager m LEFT JOIN WorkflowRequest r " +
            "                     ON r.id = m.fk_wfl_process_instance_id AND r.tenantId = m.fk_wfl_process_tenant_id " +
            "    WHERE m.isRead = FALSE AND m.fk_manager_account_id = :accountId) AS newManagerRequests, " +
            (!teamRequests ? "" :
            "  (SELECT count(DISTINCT fk_wfl_process_instance_id) FROM WorkflowRequestPeer " +
            "    WHERE fk_recipient_employee_id = :employeeId " +
            "      AND peerStatus IN ('PEER_PENDING')) AS pendingTeamRequests, " +
            "  (SELECT count(DISTINCT fk_wfl_process_instance_id) FROM WorkflowRequestPeer p " +
            "     LEFT JOIN WorkflowRequest r ON r.id = p.fk_wfl_process_instance_id " +
            "                                AND r.tenantId = p.fk_wfl_process_tenant_id " +
            "    WHERE p.isRead = FALSE AND p.fk_recipient_employee_id = :employeeId) AS newTeamRequests ") +
            " FROM dual ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("accountId", userAccountPk.getId());
        query.setParameter("employeeId", employeePk.getId());
        return (Object[]) query.getResultList().get(0);
    }

    @Override
    public Object[] teamPendingAndNewRequestCounts(PrimaryKey employeePrimaryKey) {
        String sql =
            "SELECT " +
            "  (SELECT count(DISTINCT fk_wfl_process_instance_id) FROM WorkflowRequestPeer " +
            "    WHERE fk_recipient_employee_id = :employeeId " +
            "      AND peerStatus IN ('PEER_PENDING')) AS pendingTeamRequests, " +
            "  (SELECT count(DISTINCT fk_wfl_process_instance_id) FROM WorkflowRequestPeer p " +
            "     LEFT JOIN WorkflowRequest r ON r.id = p.fk_wfl_process_instance_id " +
            "                                AND r.tenantId = p.fk_wfl_process_tenant_id " +
            "    WHERE p.isRead = FALSE AND p.fk_recipient_employee_id = :employeeId) AS newTeamRequests " +
            " FROM dual ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeePrimaryKey.getId());

        return (Object[]) query.getResultList().get(0);
    }

    private Collection<String> getAclTeamIds(AccountACL acl, Employee requester) {
        String tenantId = requester.getTenantId();
        String employeeId = requester.getId();

        String teamIds = (String) cacheService.getEntry(CacheConstants.ALLOWED_BY_ACL_TEAM_IDS_CACHE, tenantId,
                employeeId);
        if (teamIds == null) {
            ResultSet<Team> teamResultSet = teamService.findTeams(new SimpleQuery(tenantId), acl);

            teamIds = ModelUtils.commaSeparatedIds(teamResultSet.getResult(), null);
            if (teamIds == null) {
                teamIds = "";
            }

            cacheService.putEntry(CacheConstants.ALLOWED_BY_ACL_TEAM_IDS_CACHE, tenantId, employeeId, teamIds);
        }

        if (StringUtils.isBlank(teamIds)) {
            return null;
        } else {
            String[] teamIdArray = teamIds.split(",");
            return Arrays.asList(teamIdArray);
        }
    }

    private Collection<String> getAclTeamIds(AccountACL acl, UserAccount account) {
        String tenantId = account.getTenantId();
        String accountId = account.getId();
        // String employeeId = requester.getId();

        String teamIds = (String) cacheService.getEntry(CacheConstants.ALLOWED_BY_ACL_TEAM_IDS_CACHE, tenantId,
                accountId);
        if (teamIds == null) {
            ResultSet<Team> teamResultSet = teamService.findTeams(new SimpleQuery(tenantId), acl);

            teamIds = ModelUtils.commaSeparatedIds(teamResultSet.getResult(), null);
            if (teamIds == null) {
                teamIds = "";
            }

            cacheService.putEntry(CacheConstants.ALLOWED_BY_ACL_TEAM_IDS_CACHE, tenantId, accountId, teamIds);
        }

        if (StringUtils.isBlank(teamIds)) {
            return null;
        } else {
            String[] teamIdArray = teamIds.split(",");
            return Arrays.asList(teamIdArray);
        }
    }

    private Timestamp toZonedTimeStamp(Long date) {
        if (date == null) {
            return new Timestamp(Calendar.getInstance(DateTimeZone.UTC.toTimeZone()).getTimeInMillis());
        } else {
            return new Timestamp(date);
        }
    }

}
