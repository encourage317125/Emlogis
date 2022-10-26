package com.emlogis.common.services.schedule;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.model.schedule.PostMode;
import com.emlogis.model.schedule.PostedOpenShift;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class PostedOpenShiftService {
	
	private final Logger logger = LoggerFactory.getLogger(PostedOpenShiftService.class);


    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private SessionService sessionService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private ShiftService shiftService;

    @EJB
    private NotificationService notificationService;
    
    @EJB
    private AccountUtilService accountUtilService;

    public ResultSet<PostedOpenShift> findPostedOpenShifts(SimpleQuery simpleQuery) {
        simpleQuery.setEntityClass(PostedOpenShift.class);
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public Collection<PostedOpenShift> getPostedOpenShiftsOfEmployee(String tenantId, String employeeId,
                                                                     Long startDateTime, Long endDateTime) {
    	String sql = "SELECT os FROM PostedOpenShift os " +
                     " WHERE os.primaryKey.tenantId = :tenantId AND os.employeeId = :employeeId ";
    	
    	if (startDateTime != null) {
    		sql += " AND os.startDateTime >= :startDateTime";
    	}
    	if (endDateTime != null) {
    		sql += " AND os.startDateTime < :endDateTime ";
    	}

		sql += " AND os.deadline > :now ";

        Query query = entityManager.createQuery(sql);
		query.setParameter("tenantId", tenantId);
		query.setParameter("employeeId", employeeId);
        if (startDateTime != null) {
            query.setParameter("startDateTime", new DateTime(startDateTime));
        }
        if (endDateTime != null) {
            query.setParameter("endDateTime", new DateTime(endDateTime));
        }
		query.setParameter("now", new DateTime(System.currentTimeMillis()));
		return query.getResultList();
	}

    public Collection<PostedOpenShift> getPostedOpenShiftsOfEmployeeSchedule(PrimaryKey schedulePrimaryKey,
                                                                             String employeeId) {
    	String sql = "SELECT os FROM PostedOpenShift os " +
                     " WHERE os.primaryKey.tenantId = :tenantId " +
                     "AND os.scheduleId = :scheduleId " +
                     "AND os.employeeId = :employeeId";

        Query query = entityManager.createQuery(sql);
		query.setParameter("tenantId", schedulePrimaryKey.getTenantId());
		query.setParameter("scheduleId", schedulePrimaryKey.getId());
		query.setParameter("employeeId", employeeId);
		return query.getResultList();
	}

    public Collection<PostedOpenShift> getPostedOpenShiftsOfSchedule(
            PrimaryKey schedulePrimaryKey, // mandatory field, schedule primary key
            Long startDateTime, //nullable field
            Long endDateTime, //nullable field
            Boolean excess) {
        String sql = "SELECT os.* FROM PostedOpenShift os " +
                " WHERE os.tenantId = :tenantId AND os.scheduleId = :scheduleId ";

        if (startDateTime != null && startDateTime > Constants.DATE_2000_01_01) {
            sql += " AND os.startDateTime >= :startDateTime";
        }
        if (endDateTime != null && endDateTime > Constants.DATE_2000_01_01) {
            sql += " AND os.startDateTime < :endDateTime ";
        }
        if (excess != null) {
            sql += " AND os.excess = :excess ";
        }

        Query query = entityManager.createNativeQuery(sql, PostedOpenShift.class);
        query.setParameter("tenantId", schedulePrimaryKey.getTenantId());
        query.setParameter("scheduleId", schedulePrimaryKey.getId());
        if (startDateTime != null && startDateTime > Constants.DATE_2000_01_01) {
            query.setParameter("startDateTime", new Timestamp(startDateTime));
        }
        if (endDateTime != null && endDateTime > Constants.DATE_2000_01_01) {
            query.setParameter("endDateTime", new Timestamp(endDateTime));
        }
        if (excess != null) {
            query.setParameter("excess", excess);
        }

        return query.getResultList();
    }

    @Schedule(second = "00", minute = "00", hour = "00", persistent = false)
    public void cleanupPostedOpenShifts() {
        String deleteSql = "DELETE FROM PostedOpenShift WHERE endDateTime < NOW()";
        Query query = entityManager.createNativeQuery(deleteSql);
        query.executeUpdate();
    }

    public PostedOpenShift getPostedOpenShift(PrimaryKey primaryKey) {
        return entityManager.find(PostedOpenShift.class, primaryKey);
    }

    public PostedOpenShift getPostedOpenShiftByEmployeeAndShift(String tenantId, String employeeId, String shiftId) {
    	String sql = "SELECT os FROM PostedOpenShift os " +
                     " WHERE os.primaryKey.tenantId = :tenantId " +
                     "AND os.shiftId = :shiftId " +
                     "AND os.employeeId = :employeeId";

        Query query = entityManager.createQuery(sql);
		query.setParameter("tenantId", tenantId);
		query.setParameter("employeeId", employeeId);
		query.setParameter("shiftId", shiftId);
		List<Object[]> resultData = query.getResultList();
		switch (resultData.size()) {
		case 0:
			return null;
		case 1:
			return (PostedOpenShift)query.getResultList().get(0);
		default: 			// should not happen
			throw new RuntimeException("Getting PostedOS of: [" + tenantId + "]employee: " + employeeId + " shift: "
                    + shiftId + " retruns more than 0 or 1 result");
		}
	}
    
    public void markPostedOpenShiftAsRequested(String tenantId, String employeeId, String shiftId) {
    	PostedOpenShift openShift = getPostedOpenShiftByEmployeeAndShift(tenantId, employeeId, shiftId);
    	if (openShift != null) {
    		markPostedOpenShiftAsRequested(openShift);
    	}
	}
    
    public void markPostedOpenShiftAsRequested(PostedOpenShift openShift) {
    	openShift.setRequested(true);
    	openShift.setRequestedOn(System.currentTimeMillis());
    	update(openShift);
	}
       
    public void clearPostedOpenShiftAsRequested(String tenantId, String employeeId, String shiftId) {
    	PostedOpenShift openShift = getPostedOpenShiftByEmployeeAndShift(tenantId, employeeId, shiftId);
    	if (openShift != null) {
    		clearPostedOpenShiftAsRequested(openShift);
    	}
	}
  
    public void clearPostedOpenShiftAsRequested(PostedOpenShift openShift) {
    	openShift.setRequested(false);
    	openShift.setRequestedOn(0);
    	update(openShift);
	}
    
    public void insert(PostedOpenShift openShift) {
        entityManager.persist(openShift);
    }

    public PostedOpenShift update(PostedOpenShift openShift) {
        return entityManager.merge(openShift);
    }

    public void delete(PostedOpenShift openShift) {
        entityManager.remove(openShift);
    }

	/**
	 * Deletes all records associated to a Shift (ie all associations to employees this Shift has been posted to)
	 * @param shiftPk
	 */
	public void deletePostedOpenShiftsByShift(PrimaryKey shiftPk) {
		Query query = entityManager.createNativeQuery(
			"DELETE FROM PostedOpenShift WHERE tenantId = :tenantId AND shiftId = :shiftId ");
		query.setParameter("tenantId", shiftPk.getTenantId());
		query.setParameter("shiftId", shiftPk.getId());
        query.executeUpdate();
	}

    public Collection<PostedOpenShift> postOpenShifts(
            com.emlogis.model.schedule.Schedule schedule, //shifts schedule
            PostMode postMode, // Replace or Cumulative post open shift method
            Map<String, Collection<String>> openShiftEmpIdsMap, //post as available to employees
            Map<ConstraintOverrideType, Boolean> overrideOptions, //options
            long deadline, //apply UTC long expiration
            String comments, //commentary on post action
            String terms) {
        if (schedule.getStatus() != ScheduleStatus.Posted) {
            throw new ValidationException(sessionService.getMessage("validation.schedule.status.invalid",
                    schedule.getStatus()));
        }

        Map<Employee, List<PostedOpenShift>> newEmployeeNotificationMap = new HashMap<>();
        Map<Employee, List<PostedOpenShift>> updateEmployeeNotificationMap = new HashMap<>();

        String tenantId = schedule.getTenantId();
        Collection<PostedOpenShift> existingOpenShifts =
                getPostedOpenShiftsOfSchedule(schedule.getPrimaryKey(), null, null, null);

        Set<String> shiftIds = openShiftEmpIdsMap.keySet();

        Map<String, Employee> employeeMap = buildEmployeeMap(tenantId, openShiftEmpIdsMap);
        Map<String, Shift> shiftMap = buildShiftMap(tenantId, shiftIds);

        Collection<PostedOpenShift> result = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (String shiftId : shiftIds) {
            Collection<String> empIds = openShiftEmpIdsMap.get(shiftId);
            if (empIds != null && empIds.size() > 0) {
                // get shift
                Shift shift = shiftMap.get(shiftId);
                if (shift == null) {
                    throw new ValidationException(sessionService.getMessage("validation.shift.not.exist", shiftId));
                }
                if (!StringUtils.equals(schedule.getId(), shift.getScheduleId())) {
                    throw new ValidationException(
                            sessionService.getMessage("validation.schedule.postopenshift.shiftschedulemismatch",
                                    schedule.getStatus(), schedule.getId(), shift.getScheduleId()));
                }

                for (String employeeId : empIds) {
                    // get employee
                    Employee employee = employeeMap.get(employeeId);

                    if (employee == null) {
                        throw new ValidationException(sessionService.getMessage("validation.employee.not.exist",
                                employeeId));
                    }

                    boolean createNewPostedOpenShift = true;
                    PostedOpenShift existedOpenShift = findPostedOpenShift(existingOpenShifts, shiftId, employeeId);
                    if (existedOpenShift != null) {
                        createNewPostedOpenShift = !existedOpenShift.isRequested();
                        if (createNewPostedOpenShift) {
                            existingOpenShifts.remove(existedOpenShift);
                            delete(existedOpenShift);
                        }
                    }

                    if (createNewPostedOpenShift) {
                        PostedOpenShift postedOpenShift = createOpenShift(schedule, employee, shift, now);
                        postedOpenShift.setComments(comments);
                        postedOpenShift.setTerms(terms);
                        postedOpenShift.setOverrideOptions(overrideOptions);
                        postedOpenShift.setDeadline(deadline);

                        insert(postedOpenShift);

                        result.add(postedOpenShift);

                        if (existedOpenShift != null) {
                            updateNotificationMap(updateEmployeeNotificationMap, employee, postedOpenShift);
                        } else {
                            updateNotificationMap(newEmployeeNotificationMap, employee, postedOpenShift);
                        }

                        if (StringUtils.isNotEmpty(shift.getEmployeeId())) {
                            shift.dropShiftAssignment();
                            shiftService.update(shift);
                        }
                    }
                }
            }
        }

        // find PostedOpenShifts to delete
        Collection<PostedOpenShift> postedOpenShiftsToDelete = new ArrayList<>();
        if (PostMode.Replace.equals(postMode)) {
            for (PostedOpenShift postedOpenShift : existingOpenShifts) {
                String shiftId = postedOpenShift.getShiftId();
                if (shiftIds.contains(shiftId)) {
                    Collection<String> empIds = openShiftEmpIdsMap.get(shiftId);
                    if (!empIds.contains(postedOpenShift.getEmployeeId())) {
                        postedOpenShiftsToDelete.add(postedOpenShift);
                    }
                } else {
                    postedOpenShiftsToDelete.add(postedOpenShift);
                }
            }
        }

        deletePostedOpenShifts(postedOpenShiftsToDelete, tenantId, employeeMap);

        sendNotifications(newEmployeeNotificationMap, "New");
        sendNotifications(updateEmployeeNotificationMap, "Update");

        return result;
    }

	private void updateNotificationMap(Map<Employee, List<PostedOpenShift>> employeeNotificationMap,
			Employee employee, PostedOpenShift openShift) {
		List<PostedOpenShift> employeeOpenShiftList = employeeNotificationMap.get(employee);
		
		if (employeeOpenShiftList == null) {
			employeeOpenShiftList = new ArrayList<>();
			employeeNotificationMap.put(employee, employeeOpenShiftList);
		}
		
		employeeOpenShiftList.add(openShift);
	}

    public void cancelPosts(PrimaryKey schedulePrimaryKey, Map<String, Collection<String>> openShiftEmpIdsMap) {
        String tenantId = schedulePrimaryKey.getTenantId();

        Collection<PostedOpenShift> existingOpenShifts =
                getPostedOpenShiftsOfSchedule(schedulePrimaryKey, null, null, null);

        Collection<String> shiftIds = openShiftEmpIdsMap.keySet();

        // find PostedOpenShifts to delete
        Collection<PostedOpenShift> postedOpenShiftsToDelete = new ArrayList<>();
        for (PostedOpenShift postedOpenShift : existingOpenShifts) {
            String shiftId = postedOpenShift.getShiftId();
            if (shiftIds.contains(shiftId)) {
                Collection<String> empIds = openShiftEmpIdsMap.get(shiftId);
                if (empIds == null || empIds.size() == 0 || empIds.contains(postedOpenShift.getEmployeeId())) {
                    postedOpenShiftsToDelete.add(postedOpenShift);
                }
            } else {
                postedOpenShiftsToDelete.add(postedOpenShift);
            }
        }

        Map<String, Employee> employeeMap = buildEmployeeMap(tenantId, openShiftEmpIdsMap);

        deletePostedOpenShifts(postedOpenShiftsToDelete, tenantId, employeeMap);
    }

    // build a map of Shifts corresponding to the specified shiftIds
    private Map<String, Shift> buildShiftMap(String tenantId, Collection<String> shiftIds) {
        Collection<Shift> shiftCollection = shiftService.getShifts(tenantId, shiftIds);
        Map<String, Shift> shiftMap = new HashMap<>();
        for (Shift shift : shiftCollection) {
            shiftMap.put(shift.getId(), shift);
        }
        return shiftMap;
    }

    // build a map of target Employees corresponding to the specified employeeIds
    private Map<String, Employee> buildEmployeeMap(String tenantId,
                                                   Map<String, Collection<String>> openShiftEmpIdsMap) {
        Set<String> shiftIds = openShiftEmpIdsMap.keySet();
        Set<String> employeeIds = new HashSet<>();

        for (String shiftId : shiftIds) {
            Collection<String> empIds = openShiftEmpIdsMap.get(shiftId);
            if (empIds != null) {
                employeeIds.addAll(empIds);
            }
        }

        Map<String, Employee> employeeMap = new HashMap<>();
        Collection<Employee> employeeCollection = employeeService.getEmployeesByIds(tenantId, employeeIds);
        for (Employee employee : employeeCollection) {
            employeeMap.put(employee.getId(), employee);
        }
        return employeeMap;
    }

    private void deletePostedOpenShifts(Collection<PostedOpenShift> postedOpenShifts,
                                        String tenantId, Map<String, Employee> employeeMap) {
        Map<Employee, List<PostedOpenShift>> cancelEmployeeNotificationMap = new HashMap<>();

        for (PostedOpenShift postedOpenShift : postedOpenShifts) {
            if (!postedOpenShift.isRequested()) {
                Employee employee = null;
                if (employeeMap != null) {
                    employee = employeeMap.get(postedOpenShift.getEmployeeId());
                }
                if (employee == null) {
                    employee = employeeService.getEmployee(new PrimaryKey(tenantId, postedOpenShift.getEmployeeId()));
                }
                if (employee == null) {
                    throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noemployee",
                            postedOpenShift.getEmployeeId()));
                }
                List<PostedOpenShift> cancelledShifts = cancelEmployeeNotificationMap.get(employee);
                
                if (cancelledShifts == null) {
                	cancelledShifts = new ArrayList<>();
                } 
                cancelledShifts.add(postedOpenShift);

                delete(postedOpenShift);
            }
        }
        
        sendNotifications(cancelEmployeeNotificationMap, "Cancel");
    }

    private PostedOpenShift findPostedOpenShift(Collection<PostedOpenShift> postedOpenShifts, String shiftId,
                                                String employeeId) {
        for (PostedOpenShift postedOpenShift : postedOpenShifts) {
            if (StringUtils.equals(shiftId, postedOpenShift.getShiftId())
                    && StringUtils.equals(employeeId, postedOpenShift.getEmployeeId())) {
                return postedOpenShift;
            }
        }
        return null;
    }

    private void sendNotifications(Map<Employee, List<PostedOpenShift>> notificationMap, String type) {
    	// We aren't sending notifications for deleted shifts right now.
    	if (type.equalsIgnoreCase("cancel")) {
            return;
        }
    	
    	List<Map<String, String>> shiftList;
    	Map<String, String> shift;

        for (Map.Entry<Employee, List<PostedOpenShift>> entry : notificationMap.entrySet()) {
            Employee employee = entry.getKey();

            UserAccount userAccount = employee.getUserAccount();

            Map<String, String> messageAttributes = new HashMap<>();

            shiftList = new ArrayList<>();
            
            boolean shiftListOverflow = false;
            int overFlowCount = 0;
                        
            List<PostedOpenShift> employeeShiftlist = entry.getValue();

            for (PostedOpenShift openShift: employeeShiftlist) {
            	if(overFlowCount++ > Constants.NOTIFICATION_OPEN_SHIFT_MAX) {
            		shiftListOverflow = true;
            		break;
            	}
            	
            	shift = new HashMap<>();

                String shiftDateString = accountUtilService.getTimeZoneAdjustedDateString(
                        new DateTime(openShift.getStartDateTime()), userAccount, Constants.NOTIF_DATE_FORMATTER);

                String startTimeSting = accountUtilService.getTimeZoneAdjustedDateString(
                        new DateTime(openShift.getStartDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);

                String endTimeString = accountUtilService.getTimeZoneAdjustedDateString(
                        new DateTime(openShift.getEndDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);
            	
            	shift.put("skillName", openShift.getSkillName());
            	shift.put("team", openShift.getTeamName());
            	shift.put("shiftDate", shiftDateString);
            	shift.put("shiftStartTime", startTimeSting);
            	shift.put("shiftEndTime", endTimeString);
            	
            	shiftList.add(shift);
            }
            
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            try {
				objectMapper.writeValue(writer, shiftList);
			} catch (Exception e) {
				logger.error("Error getting openshift template value for employee: " + employee.getId(), e);
			}
            String openShiftListString = writer.toString();
            
            messageAttributes.put("shiftListString", openShiftListString);
            messageAttributes.put("shiftListOverflow", Boolean.toString(shiftListOverflow) );
            
            NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
            notificationMessageDTO.setNotificationOperation(NotificationOperation.POST);
            notificationMessageDTO.setNotificationCategory(NotificationCategory.OPEN_SHIFTS);

            notificationMessageDTO.setTenantId(employee.getTenantId());
            notificationMessageDTO.setReceiverUserId(employee.getUserAccount().getId());
            notificationMessageDTO.setMessageAttributes(messageAttributes);
            
            // This probably needs to be set since SMS message size is below 4096l
            notificationMessageDTO.setEmailOnly(true);

            notificationService.sendNotification(notificationMessageDTO);
        }
    }

    private PostedOpenShift createOpenShift(com.emlogis.model.schedule.Schedule schedule, Employee employee,
                                            Shift shift, long timestamp) {
        PostedOpenShift result = new PostedOpenShift(new PrimaryKey(schedule.getTenantId()));
        result.setCreated(timestamp);
        result.setPostId(timestamp);

        result.setShiftId(shift.getId());
        result.setScheduleId(schedule.getId());
        result.setScheduleName(schedule.getName());
        result.setSiteName(shift.getSiteName());
        result.setTeamId(shift.getTeamId());
        result.setTeamName(shift.getTeamName());

        result.setEmployeeId(employee.getId());
        result.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());

        result.setStartDateTime(shift.getStartDateTime());
        result.setEndDateTime(shift.getEndDateTime());
        result.setShiftLength(shift.getShiftLength());
        result.setExcess(shift.isExcess());

        result.setSkillId(shift.getSkillId());
        result.setSkillName(shift.getSkillName());
        result.setSkillAbbrev(shift.getSkillAbbrev());

        return result;
    }

    public Set<String> getSchedules(String tenantId, String employeeId) {
		
		String sql = "SELECT os.scheduleId FROM PostedOpenShift os " +
				" WHERE os.tenantId = :tenantId AND os.employeeId = :employeeId ";
		
		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("tenantId", tenantId);
		query.setParameter("employeeId", employeeId);
		List<Object[]> resultData = query.getResultList();
		Set<String> result = new HashSet<>();
		for (Object obj : resultData) {
			result.add((String) obj);
		}
		return result;
	}

    public class ShiftPostedPair {
        private final Shift shift;
        private final PostedOpenShift postedOpenShift;

        public ShiftPostedPair(Shift shift, PostedOpenShift postedOpenShift) {
            this.shift = shift;
            this.postedOpenShift = postedOpenShift;
        }

        public Shift getShift() {
            return shift;
        }

        public PostedOpenShift getPostedOpenShift() {
            return postedOpenShift;
        }
    }

    public ShiftPostedPair checkShift(PrimaryKey employeePk, String shiftId) {
        Shift shift = null;
        try {
            shift = shiftService.getShift(new PrimaryKey(employeePk.getTenantId(), shiftId));
            PostedOpenShift postedOpenShift = getPostedOpenShiftByEmployeeAndShift(
                    employeePk.getTenantId(), employeePk.getId(), shift.getId());
            if (postedOpenShift == null) {
                PostedOpenShift altPostedOpenShift = findAlternativePostedOpenShift(
                        employeePk, shift.getId(), shift.getStartDateTime(),
                        shift.getEndDateTime(), shift.getSkillId(), shift.getTeamId());
                if (altPostedOpenShift == null) {
                    return null;
                }
                return new ShiftPostedPair(shift, altPostedOpenShift);
            } else {
                return new ShiftPostedPair(shift, postedOpenShift);
            }
        } catch (Exception exception) {
            //no found
        }
        return null;
    }

    public PostedOpenShift findAlternativePostedOpenShift(
            PrimaryKey employeePk,
            String shiftId,
            Long startDateTime,
            Long endDateTime,
            String skillId,
            String teamId
    ) {
        String queryStr = "SELECT pos.* FROM PostedOpenShift pos " +
                          " WHERE pos.shiftId = '" + shiftId + "' " +
                          "   AND pos.endDateTime = '" + new Timestamp(startDateTime) + "' " +
                          "   AND pos.endDateTime = '" + new Timestamp(endDateTime) + "' " +
                          "   AND pos.employeeId = '" + employeePk.getId() + "' " +
                          "   AND pos.skillId = '" + skillId + "' " +
                          "   AND pos.teamId = '" + teamId + "';";
        Query query = entityManager.createNativeQuery(queryStr, PostedOpenShift.class);
        return (PostedOpenShift) query.getSingleResult();
    }
}

