package com.emlogis.common.services.systeminfo;

import com.emlogis.common.Constants;
import com.emlogis.common.services.BaseService;
import com.emlogis.model.ACE;
import com.emlogis.model.contract.*;
import com.emlogis.model.employee.*;
import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.model.schedule.PostedOpenShift;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.schedule.changes.*;
import com.emlogis.model.shiftpattern.*;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.GroupAccount;
import com.emlogis.model.tenant.Organization;
import com.emlogis.model.tenant.Role;
import com.emlogis.model.tenant.UserAccount;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SystemInfoService extends BaseService {

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    private Class[] entities = {
            Site.class,
            Team.class,
            Employee.class,
            UserAccount.class,
            GroupAccount.class,
            Role.class,
            ACE.class,
            Skill.class,
            AvailabilityTimeFrame.class,
            Contract.class,
            ContractLine.class,
            ShiftLength.class,
            ShiftType.class,
            Schedule.class,
            Shift.class,
            ScheduleChange.class,
            ShiftPattern.class,
            ShiftReq.class,
            PatternElt.class,
            PostedOpenShift.class,
            SendNotification.class,
            ReceiveNotification.class
    };

    /**
     * Getting count of records in table
     *
     * @param entity   table name
     * @param filter   additional filter
     * @param tenantId organization filter
     * @return
     */
    public int entityCount(String entity, String filter, String tenantId) {
        String where = "";
        if (StringUtils.isNotBlank(filter)) {
            where += (where == "") ? " WHERE " : " AND ";
            where += filter;
        }
        if (StringUtils.isNotBlank(tenantId)) {
            where += (where == "") ? " WHERE " : " AND ";
            where += "tenantId = '" + tenantId + "'";
        }
        String sql = "SELECT COUNT(*) FROM " + entity + where;
        Query query = entityManager.createNativeQuery(sql);
        return ((BigInteger) query.getSingleResult()).intValue();
    }

    /**
     * Basic method for getting dbInfo
     *
     * @param tenantId if blank - return summary dbInfo,
     *                 else - per tenant
     * @return map of entities counts
     */
    public Map<String, Object> dbInfo(String tenantId) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Class aClass : entities) {
            if (!StringUtils.isNotBlank(tenantId)) {
                map.put(Organization.class.getSimpleName(), entityCount(Organization.class.getSimpleName(), "", ""));
            }
            if (aClass == Schedule.class) {
                HashMap<String, Object> scheduleMap = new HashMap<>();
                scheduleMap.put("total", entityCount(aClass.getSimpleName(), "", tenantId));
                scheduleMap.put("simulation", entityCount(aClass.getSimpleName(), "status = 0", tenantId));
                scheduleMap.put("production", entityCount(aClass.getSimpleName(), "status = 1", tenantId));
                scheduleMap.put("posted", entityCount(aClass.getSimpleName(), "status = 2", tenantId));
                map.put(aClass.getSimpleName(), scheduleMap);

            } else if (aClass == AvailabilityTimeFrame.class) {
                Class[] classes = {
                        CIAvailabilityTimeFrame.class,
                        CDAvailabilityTimeFrame.class
                };
                map.put(aClass.getSimpleName(), specifyDbInfo(classes, tenantId));

            } else if (aClass == ContractLine.class) {
                Class[] classes = {
                        BooleanCL.class,
                        IntMinMaxCL.class,
                        WeekdayRotationPatternCL.class,
                        WeekendWorkPatternCL.class
                };
                map.put(aClass.getSimpleName(), specifyDbInfo(classes, tenantId));

            } else if (aClass == Contract.class) {
                Class[] classes = {
                        SiteContract.class,
                        TeamContract.class,
                        EmployeeContract.class
                };
                map.put(aClass.getSimpleName(), specifyDbInfo(classes, tenantId));

            } else if (aClass == ScheduleChange.class) {
                Class[] classes = {
                        ScheduleChange.class,
                        ShiftAddChange.class,
                        ShiftAssignChange.class,
                        ShiftDeleteChange.class,
                        ShiftDropChange.class,
                        ShiftEditChange.class,
                        ShiftSwapChange.class,
                        ShiftWipChange.class,
                };
                map.put(aClass.getSimpleName(), specifyDbInfo(classes, tenantId));

            } else if (aClass == PostedOpenShift.class) {
                HashMap<String, Object> postedShiftsMap = new HashMap<>();
                postedShiftsMap.put("total", entityCount(aClass.getSimpleName(), "", tenantId));
                postedShiftsMap.put("current", entityCount(aClass.getSimpleName(), "startDateTime > NOW()", tenantId));
                postedShiftsMap.put("passed", entityCount(aClass.getSimpleName(), "startDateTime <= NOW()", tenantId));
                map.put(aClass.getSimpleName(), postedShiftsMap);

            } else {
                map.put(aClass.getSimpleName(), entityCount(aClass.getSimpleName(), "", tenantId));
            }
        }
        return map;
    }

    private Map specifyDbInfo(Class[] classes, String tenantId) {
        HashMap<String, Object> detailMap = new HashMap<>();

        int sum = 0;
        for (Class aClass : classes) {
            int count = entityCount(aClass.getSimpleName(), "", tenantId);
            detailMap.put(aClass.getSimpleName(), count);
            sum += count;
        }
        detailMap.put("total", sum);
        return detailMap;
    }

    public List<Map> dbPerCustomer() {
        ArrayList<Map> list = new ArrayList<>();
        String sql = "SELECT o.tenantId, o.name FROM Organization o ";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> orgs = query.getResultList();
        for (Object[] org : orgs) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", org[0]);
            map.put("name", org[1]);
            map.put("dbInfo", dbInfo(org[0].toString()));
            list.add(map);
        }
        return list;
    }
}
