package com.emlogis.common.services.employee;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.emlogis.model.common.CacheConstants;
import com.emlogis.model.employee.*;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.tenant.RememberMe;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.server.services.cache.BasicCacheService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationDeliveryMethod;
import com.emlogis.common.notifications.NotificationType;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.contract.ContractLineService;
import com.emlogis.common.services.contract.ContractService;
import com.emlogis.common.services.notification.NotificationConfigInfo;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.BooleanCL;
import com.emlogis.model.contract.ContractLine;
import com.emlogis.model.contract.EmployeeContract;
import com.emlogis.model.contract.WeekdayRotationPatternCL;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;
import com.emlogis.model.employee.dto.AvailcalSimpleTimeFrame;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsAvailDto.AvailAction;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCDCopyDto.Repeat;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCIDaySelections;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsPrefDto.PrefAction;
import com.emlogis.model.employee.dto.AvailcalViewDto.AvailCDTimeFrame;
import com.emlogis.model.employee.dto.AvailcalViewDto.AvailCITimeFrame;
import com.emlogis.model.employee.dto.AvailcalViewDto.AvailType;
import com.emlogis.model.employee.dto.AvailcalViewDto.PrefCDTimeFrame;
import com.emlogis.model.employee.dto.AvailcalViewDto.PrefCITimeFrame;
import com.emlogis.model.employee.dto.AvailcalViewDto.PrefType;
import com.emlogis.model.employee.dto.AvailcalViewDto.PreviewType;
import com.emlogis.model.employee.dto.AvailcalViewDto.TimeFrameInstance;
import com.emlogis.model.employee.dto.AvailcalViewDto.WeekdayRotationValue;
import com.emlogis.model.employee.dto.EmployeeAvailabilityDto.EmployeeUnavailabilityDto;
import com.emlogis.model.employee.dto.EmployeeAvailabilityDto.OrgHolidayDto;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.structurelevel.Holiday;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class EmployeeService {

	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private SessionService sessionService;

	@EJB
	private SkillService skillService;

	@EJB
	private TeamService teamService;

	@EJB
	private OrganizationService organizationService;

	@EJB
	private TenantService tenantService;
	
	@EJB
	private UserAccountService userAccountService;
	
	@EJB
	private AccountUtilService accountUtilService;

	@EJB
	private NotificationService notificationService;
	
	@EJB
	private AbsenceTypeService absenceTypeService;
	
	@EJB
	private CDAvailabilityTimeFrameService cdAvailabilityTimeFrameService;

	@EJB
	private CIAvailabilityTimeFrameService ciAvailabilityTimeFrameService;
	
    @EJB
    private ContractService contractService;

    @EJB
    private ContractLineService contractLineService;

    @EJB
    private BasicCacheService cacheService;
	
	// Special key used to group CI availability/preference timeframes that fall on same days.
	class CIGroupKey {
		private DateTime startDateTime;
		private DateTime endDateTime;
		private DayOfWeek dayOfTheWeek;
		private AvailabilityType availabilityType;

		public DateTime getStartDateTime() {return startDateTime;}
		public void setStartDateTime(DateTime startDateTime) {this.startDateTime = startDateTime;}
		public DateTime getEndDateTime() {return endDateTime;}
		public void setEndDateTime(DateTime endDateTime) {this.endDateTime = endDateTime;}
		public DayOfWeek getDayOfTheWeek() {return dayOfTheWeek;}
		public void setDayOfTheWeek(DayOfWeek dayOfTheWeek) {this.dayOfTheWeek = dayOfTheWeek;}
		public AvailabilityType getAvailabilityType() {return availabilityType;}
		public void setAvailabilityType(AvailabilityType availabilityType) {this.availabilityType = availabilityType;}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((availabilityType == null) ? 0 : availabilityType
							.hashCode());
			result = prime
					* result
					+ ((dayOfTheWeek == null) ? 0 : dayOfTheWeek.hashCode());
			result = prime * result
					+ ((endDateTime == null) ? 0 : endDateTime.hashCode());
			result = prime
					* result
					+ ((startDateTime == null) ? 0 : startDateTime
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CIGroupKey other = (CIGroupKey) obj;
			if (availabilityType != other.availabilityType)
				return false;
			if (dayOfTheWeek != other.dayOfTheWeek)
				return false;
			if (endDateTime == null) {
				if (other.endDateTime != null)
					return false;
			} else if (!endDateTime.equals(other.endDateTime))
				return false;
			if (startDateTime == null) {
				if (other.startDateTime != null)
					return false;
			} else if (!startDateTime.equals(other.startDateTime))
				return false;
			return true;
		}
	}


	/**
	 * findEmployees() find a list of Employees matching criteria;
	 * @param sq
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public ResultSet<Employee> findEmployees(SimpleQuery sq) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException{
		sq.setEntityClass(Employee.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(sq);
        return sqh.executeSimpleQueryWithPaging(entityManager, sq);
    }

	/**
	 * Get employee 
	 * @param primaryKey
	 * @return
	 */
	public Employee getEmployee(PrimaryKey primaryKey) {
        return entityManager.find(Employee.class, primaryKey);
	}

    public void persistEmployee(Employee employee) {
		entityManager.persist(employee);
	}

    public void initEmployeeNotificationConfig(Employee employee) {
        String tenantId = employee.getTenantId();

    	// initialize notification types and delivery methods, all false by default
		Map<NotificationType, Boolean> notificationTypes = new HashMap<>();
		for (NotificationType notificationType : NotificationType.values()) {
			notificationTypes.put(notificationType, true);
		}
    	employee.setNotificationTypes(notificationTypes);

    	NotificationConfig nc = new NotificationConfig(new PrimaryKey(tenantId), true,
                NotificationDeliveryMethod.SMS, NotificationDeliveryFormat.SMS_TEXT);
    	entityManager.persist(nc);
    	employee.addNotificationConfig(nc);

    	nc = new NotificationConfig(new PrimaryKey(tenantId), true , NotificationDeliveryMethod.PersonalEmail,
                NotificationDeliveryFormat.PLAIN_TEXT);
    	entityManager.persist(nc);
    	employee.addNotificationConfig(nc);

    	nc = new NotificationConfig(new PrimaryKey(tenantId), true, NotificationDeliveryMethod.CorporateEmail,
                NotificationDeliveryFormat.HTML);
    	entityManager.persist(nc);
    	employee.addNotificationConfig(nc);

		entityManager.merge(employee);
	}

    /**
     * Update employee
     * @param employee
     * @return
     */
    public Employee update(Employee employee) {
        employee.touch();
        Employee mergedEmployee = entityManager.merge(employee);
		return mergedEmployee;
    }
    
    /**
     * Delete an employee. Note that this is a 'soft delete' where employee is marked isDeleted = true and timestamp
     * strings are appended to email, employeeIdentifier, and loginName if applicable.  
     * 
     * @param employee
     */
    public void softDelete(Employee employee) {
    	String dateTimeString = new DateTime().toString();
    	
    	UserAccount userAccount = employee.getUserAccount();
    	if (userAccount != null) {
    		userAccount.setLogin(userAccount.getLogin() + dateTimeString);
    		// TODO - When UserAccount gets some sort of active/inactive field that 
    		//        indicates whether an employee can/can't login, then this
    		//        delete logic should set that appropriately as well.
    	}
    	
    	if (employee.getWorkEmail() != null) {
        	employee.setWorkEmail(employee.getWorkEmail() + dateTimeString);
    	}

    	employee.setEmployeeIdentifier(employee.getEmployeeIdentifier() + dateTimeString);
    	employee.setIsDeleted(true);
    	employee.setActivityType(EmployeeActivityType.Inactive);

        entityManager.merge(userAccount);
        entityManager.merge(employee);
    }

    public void mergeEmployeeProcessAutoApproval(EmployeeProcessAutoApproval processAutoApproval) {
        entityManager.merge(processAutoApproval);
    }

    public void persistEmployeeProcessAutoApproval(EmployeeProcessAutoApproval processAutoApproval) {
        entityManager.persist(processAutoApproval);
    }

    public void deleteEmployeeProcessAutoApproval(PrimaryKey processAutoApprovalPrimaryKey) {
        EmployeeProcessAutoApproval processAutoApproval = entityManager.find(EmployeeProcessAutoApproval.class,
                processAutoApprovalPrimaryKey);
        if (processAutoApproval != null) {
            entityManager.remove(processAutoApproval);
        }
    }

	/**
	 * Add employee skill
	 * @param employee
	 * @param skill
	 * @param employeeSkill
	 * @return
	 */
	public EmployeeSkill addEmployeeSkill(Employee employee, Skill skill, EmployeeSkill employeeSkill) {
	    if (employee != null) {	    	
	        if (skill != null) {
	        	if (employeeSkill != null) {
	        		
					employeeSkill.setSkill(skill);
					employee.addEmployeeSkill(employeeSkill);
					
					if (employeeSkill.getIsPrimarySkill()) {
						Set<EmployeeSkill> employeeSkills = employeeSkill.getEmployee().getEmployeeSkills();
						for (EmployeeSkill tmpEmployeeSkill : employeeSkills){
							if( tmpEmployeeSkill.getIsPrimarySkill() && !tmpEmployeeSkill.equals(employeeSkill)) {
								tmpEmployeeSkill.setIsPrimarySkill(false);
								entityManager.persist(tmpEmployeeSkill);
							}
						}
					}					

					employee.touch();
                    entityManager.persist(employeeSkill);
				}
	        }
	    }
	    return employeeSkill;
	}

	/**
	 * Get employee skills
	 * @param employeePrimaryKey
	 * @param simpleQuery
	 * @return
	 */
    public ResultSet<EmployeeSkill> getEmployeeSkills(PrimaryKey employeePrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(entityManager, simpleQuery, employeePrimaryKey,
                Employee.class, "employeeSkills");
    }

	/**
	 * Update employee skills
	 * @param employeeSkill
	 * @return
	 */
	public EmployeeSkill updateEmployeeSkill(EmployeeSkill employeeSkill) {
		if (employeeSkill.getIsPrimarySkill()) {
	    	// Making it primary skill, so ensure no other is designated primary...
			Set<EmployeeSkill> employeeSkills = employeeSkill.getEmployee().getEmployeeSkills();
			for (EmployeeSkill tmpEmployeeSkill : employeeSkills){
				if (tmpEmployeeSkill.getIsPrimarySkill() && !tmpEmployeeSkill.equals(employeeSkill)) {
					tmpEmployeeSkill.setIsPrimarySkill(false);
					entityManager.merge(tmpEmployeeSkill);
				}
			}
		}
		
        employeeSkill.getEmployee().touch();
        return entityManager.merge(employeeSkill);
	}

	/**
	 * Remove employee skills
	 * @param employee
	 * @param skill
	 */
	public void removeEmployeeSkill(Employee employee, Skill skill) {
		EmployeeSkill employeeSkill = findEmployeeSkill(employee, skill);
		if (employeeSkill != null) {
			employee.removeEmployeeSkill(employeeSkill);
			employee.touch();
            entityManager.persist(employee);
		}
	}

	/**
	 * Utility method to find a specific EmployeeSkill relationship
	 * @param employee
	 * @param skill
	 * @return
	 */
	public EmployeeSkill findEmployeeSkill(Employee employee, Skill skill) {
        if (employee == null || skill == null) {
            return null;
        }

        String sql = "SELECT es.* FROM EmployeeSkill es WHERE es.employeeId = :employeeId AND es.skillId = :skillId";

        Query query = entityManager.createNativeQuery(sql, EmployeeSkill.class);

        query.setParameter("employeeId", employee.getId());
        query.setParameter("skillId", skill.getId());

        List<EmployeeSkill> result = query.getResultList();
        if (result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
	}

    public boolean employeeTeamsAreEmpty(PrimaryKey employeePrimaryKey) {
        String sql = "SELECT id FROM EmployeeTeam WHERE employeeId = :employeeId AND tenantId = :tenantId LIMIT 1";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeePrimaryKey.getId());
        query.setParameter("tenantId", employeePrimaryKey.getTenantId());

        return query.getResultList().isEmpty();
    }

    public boolean checkEmployeeTeam(PrimaryKey employeePrimaryKey, PrimaryKey teamPrimaryKey) {
        String sql = "SELECT id FROM EmployeeTeam WHERE employeeId = :employeeId AND teamId = :teamId " +
                     "                              AND tenantId = :tenantId LIMIT 1";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeePrimaryKey.getId());
        query.setParameter("teamId", teamPrimaryKey.getId());
        query.setParameter("tenantId", employeePrimaryKey.getTenantId());

        return !query.getResultList().isEmpty();
    }

	/**
	 * Add employee team.  Note that since there must always be a home
	 * team, if this is the first team to be added then it will be made 
	 * the home team implicitly (regardless of provided DTO's isHomeTeam 
	 * setting).
	 * 	 
	 * Also, associate the Employee to the Site
	 * @param employee
	 * @param team
	 * @param employeeTeam
	 * @return
	 */
	public EmployeeTeam addEmployeeTeam(Employee employee, Team team, EmployeeTeam employeeTeam) {
	    if (employee != null && team != null && employeeTeam != null) {
            if (employeeTeamsAreEmpty(employee.getPrimaryKey())) {
                employeeTeam.setIsHomeTeam(true);
            } else {
                if (employeeTeam.getIsHomeTeam()) {
                    // Making it home team, so ensure no other is designated home...
                    // Iterating over entire collection isn't efficient, but should be small n
                    // and will ensure we are in proper state of having exactly 1 home team.
                    /*for (EmployeeTeam tmpEmployeeTeam : employeeTeams) {
                        if (tmpEmployeeTeam.getIsHomeTeam() && !tmpEmployeeTeam.equals(employeeTeam)) {
                            tmpEmployeeTeam.setIsHomeTeam(false);
                            entityManager.persist(tmpEmployeeTeam);
                        }
                    }*/
                    updateHomeTeam(employee.getPrimaryKey(), employeeTeam.getPrimaryKey());
                }
            }

            employee.addEmployeeTeam(employeeTeam);
            team.addEmployeeTeam(employeeTeam);
            // associate the employee to the Site (if not already done)
            String siteId = getSiteId(team.getPrimaryKey());
            addEmployeeSite(employee.getId(), siteId, employee.getTenantId());
            employee.touch();
            entityManager.persist(employeeTeam);
            entityManager.persist(employee);
            entityManager.persist(team);
        }
	    return employeeTeam;
	}

	/**
	 * Get employee teams
	 * @param employeePrimaryKey
	 * @param simpleQuery
	 * @return ResultSet<EmployeeTeam>
	 */
    public ResultSet<EmployeeTeam> getEmployeeTeams(PrimaryKey employeePrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery, QueryPattern.NOT_DELETED.val("team"));
        return queryHelper.executeGetAssociatedWithPaging(entityManager, simpleQuery, employeePrimaryKey,
                Employee.class, "employeeTeams");
    }
    
    public Site getEmployeeSite(Employee employee) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	Site site = null;

    	PrimaryKey employeePrimaryKey = employee.getPrimaryKey();
    	
    	SimpleQuery query = new SimpleQuery(employee.getTenantId()).addFilter("employee.primaryKey.id='"
                + employeePrimaryKey.getId() + "'");
    	query.setEntityClass(EmployeeTeam.class);
    	
    	ResultSet<EmployeeTeam> employeeTeams = getEmployeeTeams (employeePrimaryKey, query);
    	
    	if (employeeTeams != null && employeeTeams.getResult() != null && employeeTeams.getResult().size() > 0) {
            EmployeeTeam employeeTeam = employeeTeams.getResult().iterator().next();
    		
    		if (employeeTeam != null) {
                Team team = employeeTeam.getTeam();
    			if (team != null) {
    				site = teamService.getSite(team);
    			}
    		}    		    	
    	}    	
    	return site;
    }

    public ResultSet<Team> getUnassociatedEmployeeTeams(PrimaryKey employeePrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);
        return queryHelper.executeGetUnassociatedWithPaging(entityManager, simpleQuery, employeePrimaryKey,
                Employee.class, "employeeTeams", "team");
    }

    public ResultSet<Skill> getUnassociatedEmployeeSkills(PrimaryKey employeePrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetUnassociatedWithPaging(entityManager, simpleQuery, employeePrimaryKey,
                Employee.class, "employeeSkills", "skill");
    }

	/**
	 * Update employee team.  Note that since there must always be a
	 * home team, the update will not be allowed if it would have 
	 * resulted in there being no home team designation.
	 * 
	 * @param employeeTeam
	 * @return
	 */
	public EmployeeTeam updateEmployeeTeam(EmployeeTeam employeeTeam) {
		if (employeeTeam.getEmployee() != null && employeeTeam.getTeam() != null) {
			
			Set<EmployeeTeam> employeeTeams = employeeTeam.getEmployee().getEmployeeTeams();
			if (employeeTeam.getIsHomeTeam()) {
				// Since we're updating home team, let's make sure no other is set as home team...
				// Iterating over entire collection isn't efficient, but should be small n
				// and will ensure we are in proper state of having exactly 1 home team.
				for (EmployeeTeam tmpEmployeeTeam : employeeTeams){
					if (tmpEmployeeTeam.getIsHomeTeam()  && !tmpEmployeeTeam.equals(employeeTeam)){
						tmpEmployeeTeam.setIsHomeTeam(false);
						entityManager.merge(tmpEmployeeTeam);
						break;
					}
				}
			} else {
				boolean foundHomeTeam = false;
				// Iterating over entire collection isn't efficient, but should be small n
				// and will ensure we are in proper state of having exactly 1 home team.
				for (EmployeeTeam tmpEmployeeTeam : employeeTeams) {
					if (tmpEmployeeTeam.getIsHomeTeam()) {
						foundHomeTeam = true;
						break;
					}
				}
				if (!foundHomeTeam) {
					throw new ValidationException("Can't perform update. There must be a home team assignment.");
				}
			}
			
			employeeTeam.getEmployee().touch();
			return entityManager.merge(employeeTeam);
			
		} else {
			throw new ValidationException("Invalid EmployeeTeam. Can't update.");
		}
	}

	/**
	 * Remove employee team.  Note that since there must always be a
	 * home team, the removal will not be allowed if it would have 
	 * resulted in there being no home team designation.
	 * Also, associate the Employee to the Site
	 * 
	 * @param employee
	 * @param team
	 */
	public void removeEmployeeTeam(Employee employee, Team team) {
		Set<EmployeeTeam> employeeTeams = employee.getEmployeeTeams();
		if (employeeTeams.size() > 1) {
			EmployeeTeam employeeTeam = findEmployeeTeam(employee, team);
			if (employeeTeam != null && !employeeTeam.getIsHomeTeam()) {
				employee.removeEmployeeTeam(employeeTeam);
				team.removeEmployeeTeam(employeeTeam);
				if (employee.getEmployeeTeams().isEmpty()) {
                    Site site = teamService.getSite(employeeTeam.getTeam());
					// employee no longer part of any team, time to unlink Site
					site.removeEmployee(employee);
					employee.setSite(null);
                    entityManager.persist(site);
				}
				
				employee.touch();
                entityManager.persist(employee);
                entityManager.persist(team);
			} else {
				throw new ValidationException(sessionService.getMessage("validation.employeeteam.hometeam.error",
                        employee.getId(), team.getId()));
			}
		} else {
			throw new ValidationException(sessionService.getMessage("validation.employeeteam.hometeam.error",
                    employee.getId(), team.getId()));
		}
	}

	/**
	 * Utility method to find a specific EmployeeTeam relationship
	 * @param employee
	 * @param team
	 * @return
	 */
	public EmployeeTeam findEmployeeTeam(Employee employee, Team team) {
        if (employee == null || team == null) {
            return null;
        }

        String sql = "SELECT et.* FROM EmployeeTeam et WHERE et.employeeId = :employeeId AND et.teamId = :teamId";

        Query query = entityManager.createNativeQuery(sql, EmployeeTeam.class);

        query.setParameter("employeeId", employee.getId());
        query.setParameter("teamId", team.getId());

        List<EmployeeTeam> result = query.getResultList();
        if (result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
	}

    public List<Object> quickSearch(String tenantId, String searchValue, String searchFields, String returnedFields,
                                      int limit, String orderBy, String orderDir, AccountACL acl)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ResultSet<Team> teamResultSet = teamService.findTeams(new SimpleQuery(tenantId).setEntityClass(Team.class), acl);
        if (teamResultSet.getResult().size() > 0) {
            String teamIds = ModelUtils.commaSeparatedQuotedIds(teamResultSet.getResult());

            String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "e");

            String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "e");

            String sql =
                    "SELECT DISTINCT " + (returnedFieldsClause != null ? returnedFieldsClause : "e.* ") +
                    "  FROM Employee e, EmployeeTeam et, Team t " +
                    " WHERE e.tenantId = :tenantId AND et.tenantId = :tenantId AND t.tenantId = :tenantId " +
                    "   AND e.id = et.employeeId AND et.teamId = t.id " +
                    (searchFieldsClause != null ? " AND (" + searchFieldsClause + ")" : "") +
                            "   AND t.id IN (" + teamIds + ") " + " AND " + QueryPattern.NOT_DELETED.val("e");

            if (StringUtils.isNotBlank(orderBy)) {
                sql += " ORDER BY e." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
            }

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("tenantId", tenantId);
            if (limit > 0) {
                query.setMaxResults(limit);
            }

            return query.getResultList();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public ResultSet<Object[]> query(String tenantId, String teamIds, String searchValue, String searchFields,
                                     String skillFilter, String skillOwnershipFilter, String teamFilter,
                                     String teamMembershipFilter, String accountFilter, String activityTypeFilter,
                                     String employeeNameFilter, EmployeeTeamBelonging belonging, int offset, int limit,
                                     String orderBy, String orderDir, AccountACL acl) // belonging
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setEntityClass(Team.class);
        if (StringUtils.isNotBlank(teamIds)) {
            String[] teamIndices = teamIds.split(",");
            String idsString = ModelUtils.commaSeparatedQuotedValues(Arrays.asList(teamIndices));
            String filter = "primaryKey.id IN (" + idsString + ")";
            simpleQuery.setFilter(filter);
        }

        ResultSet<Team> teamResultSet = teamService.findTeams(simpleQuery, acl);

        if (teamResultSet.getResult().size() > 0) {
            String teamIdsClause = ModelUtils.commaSeparatedQuotedIds(teamResultSet.getResult());

            String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "e");

            String activityTypeClause = null;
            if (StringUtils.isNotBlank(activityTypeFilter)) {
                String types = ModelUtils.commaSeparatedQuotedValues(activityTypeFilter.split(","));
                activityTypeClause = " e.activityType IN (" + types + ") ";
            }

            String employeeNameClause = null;
            if (StringUtils.isNotBlank(employeeNameFilter)) {
                employeeNameClause = " e.firstName LIKE '%" + employeeNameFilter + "%' " +
                        " OR e.lastName LIKE '%" + employeeNameFilter + "%'" +
                        " OR CONCAT(e.firstName, ' ', e.lastName) LIKE '%" + employeeNameFilter + "%' ";
            }

            String belongingClause = "";
            switch (belonging) {
                case HomeTeam:
                    belongingClause = " et.isHomeTeam = true ";
                    break;
                case FloatTeam:
                    belongingClause = " et.isFloating = true ";
                    break;
            }

            String sql =
                "SELECT DISTINCT e.id, e.firstName, e.lastName, sp.name primarySkillName, e.activityType, " +
                "                shs.name homeSite, tht.name homeTeam, e.hireDate, ua.workEmail, e.mobilePhone " +
                "  FROM Employee e LEFT JOIN UserAccount ua ON ua.id = e.userAccountId " +
                "                                          AND ua.tenantId = e.userAccountTenantId " +
                "       LEFT JOIN EmployeeTeam etht ON e.id = etht.employeeId AND etht.isHomeTeam = true " +
                "       LEFT JOIN Team tht ON tht.id = etht.teamId " +
                "       LEFT JOIN AOMRelationship r ON r.dst_id = tht.id AND tht.tenantId = r.dst_tenantId " +
                "       LEFT JOIN Site shs ON r.src_id = shs.id AND shs.tenantId = r.src_tenantId " +
                "       LEFT JOIN EmployeeSkill esp ON e.id = esp.employeeId " +
                "             AND esp.employeeTenantId = e.tenantId AND esp.isPrimarySkill = true " +
                "       LEFT JOIN Skill sp ON sp.id = esp.skillId AND sp.tenantId = esp.skillTenantId " +
                "       LEFT JOIN EmployeeTeam et ON e.id = et.employeeId AND e.tenantId = et.employeeTenantId " +
                (StringUtils.isBlank(skillFilter) && StringUtils.isBlank(skillOwnershipFilter) ? ""
                        : ", EmployeeSkill es, Skill s ") +
                        " WHERE e.tenantId = :tenantId " + " AND " + QueryPattern.NOT_DELETED.val("e") +
                (StringUtils.isBlank(skillFilter) && StringUtils.isBlank(skillOwnershipFilter) ? ""
                        : " AND es.tenantId = :tenantId AND s.tenantId = :tenantId " +
                          " AND e.id = es.employeeId AND es.skillId = s.id ") +
                "   AND (" +
                (StringUtils.isEmpty(teamIds) ? " et.teamId IS NULL OR " : "") +
                "                                 et.teamId IN (" + teamIdsClause + ")) " +
                (StringUtils.isNotBlank(belongingClause) ? " AND (" + belongingClause + ")" : "") +
                (StringUtils.isNotBlank(searchFieldsClause) ? " AND (" + searchFieldsClause + ")" : "") +
                (StringUtils.isNotBlank(activityTypeClause) ? " AND (" + activityTypeClause + ")" : "") +
                (StringUtils.isNotBlank(employeeNameClause) ? " AND (" + employeeNameClause + ")" : "");

            if (StringUtils.isNotBlank(skillFilter)) {
                sql += " AND (" + SimpleQueryHelper.buildFilterClause(skillFilter, "s") + ") ";
            }
            if (StringUtils.isNotBlank(skillOwnershipFilter)) {
                sql += " AND (" + SimpleQueryHelper.buildFilterClause(skillOwnershipFilter, "es") + ") ";
            }
            if (StringUtils.isNotBlank(teamFilter)) {
                sql += " AND (" + SimpleQueryHelper.buildFilterClause(teamFilter, "t") + ") ";
            }
            if (StringUtils.isNotBlank(teamMembershipFilter)) {
                sql += " AND (" + SimpleQueryHelper.buildFilterClause(teamMembershipFilter, "et") + ") ";
            }
            if (StringUtils.isNotBlank(accountFilter)) {
                sql += " AND ua.id IS NOT NULL AND (" + SimpleQueryHelper.buildFilterClause(accountFilter, "ua") + ") ";
            }

            String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";

            if (StringUtils.isNotBlank(orderBy)) {
                if (orderBy.contains("primaryJobRole")) {
                    orderBy = " sp.name ";
                } else if (orderBy.contains("homeTeam")) {
                    orderBy = " tht.name ";
                } else if (orderBy.contains("homeSite")) {
                    orderBy = " shs.name ";
                } else if (orderBy.startsWith("Employee.")) {
                    orderBy = orderBy.replaceFirst("Employee.", "e.");
                } else if (orderBy.startsWith("EmployeeSkill.")) {
                    orderBy = orderBy.replaceFirst("EmployeeSkill.", "esp.");
                } else if (orderBy.startsWith("EmployeeTeam.")) {
                    orderBy = orderBy.replaceFirst("EmployeeTeam.", "et.");
                } else if (orderBy.startsWith("Skill.")) {
                    orderBy = orderBy.replaceFirst("Skill.", "sp.");
                } else if (orderBy.startsWith("Team.")) {
                    orderBy = orderBy.replaceFirst("Team.", "t.");
                } else if (orderBy.startsWith("UserAccount.")) {
                    orderBy = orderBy.replaceFirst("UserAccount.", "ua.");
                } else {
                    orderBy = "e." + orderBy;
                }
                sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
            }

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("tenantId", tenantId);
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            Query countQuery = entityManager.createNativeQuery(countSql);
            countQuery.setParameter("tenantId", tenantId);

            Collection<Object[]> employees = query.getResultList();
            BigInteger total = (BigInteger) countQuery.getSingleResult();
            return new ResultSet<>(employees, total.intValue());
        } else {
            return new ResultSet<>(Collections.EMPTY_LIST);
        }
    }

    public Collection<String> managers(PrimaryKey employeePrimaryKey, Permissions permission) {
        String sql =
            "SELECT DISTINCT e.id " +
            "  FROM UserAccount ua " +
            "    LEFT JOIN Employee e ON ua.id = e.userAccountId AND ua.tenantId = e.userAccountTenantId " +
            "    LEFT JOIN User_Group ug ON ua.id = ug.user_id AND ua.tenantId = ug.user_tenantId " +
            "    LEFT JOIN GroupAccount ga ON ug.group_id = ga.id AND ug.group_tenantId = ga.tenantId " +
            "    LEFT JOIN Role_Account ra ON ra.account_id = ua.id OR ra.account_id = ga.id " +
            "    LEFT JOIN Role r ON r.id = ra.role_id AND r.tenantId = ra.role_tenantId " +
            "    LEFT JOIN Role_ACE role_ace ON r.id = role_ace.role_id AND r.tenantId = role_ace.role_tenantId " +
            "    LEFT JOIN ACE ace ON ace.id = role_ace.ace_id AND ace.tenantId = role_ace.ace_tenantId " +
            "    LEFT JOIN Role_Permission arp ON r.id = arp.Role_id AND r.tenantId = arp.Role_tenantId " +
            "    LEFT JOIN Permission ap ON ap.id = arp.permissions_id " +
            (permission == null ? ""
                : " LEFT JOIN Role_Permission rp ON r.id = rp.Role_id AND r.tenantId = rp.Role_tenantId " +
                  " LEFT JOIN Permission p ON p.id = rp.permissions_id ") +
            "    LEFT JOIN EmployeeTeam et ON e.id = et.employeeId AND e.tenantId = et.employeeTenantId " +
            "    LEFT JOIN Team t ON t.id = et.teamId AND t.tenantId = et.teamTenantId " +
            "    LEFT JOIN AOMRelationship rel ON rel.dst_id = t.id AND t.tenantId = rel.dst_tenantId " +
            "    LEFT JOIN Site s ON rel.src_id = s.id AND s.tenantId = rel.src_tenantId " +
            "    LEFT JOIN EmployeeTeam ett ON t.id = ett.teamId AND t.tenantId = ett.teamTenantId " +
            " WHERE " +
            "      ua.tenantId = :tenantId AND t.tenantId = ua.tenantId " +
            "  AND e.id IS NOT NULL " +
            "  AND ett.employeeId = :employeeId " +
            "  AND ap.name IN ('OrganizationProfile_View', 'OrganizationProfile_Mgmt') " +
            "  AND (  ace.pattern IS NULL " +
            "      OR t.path IS NULL AND s.path IS NULL " +
            "      OR t.path IS NULL AND s.path REGEXP ace.pattern AND ace.entityClass = 'Site' " +
            "      OR t.path REGEXP ace.pattern AND ace.entityClass = 'Team' " +
            "      )" +
            (permission == null ? "" : " AND p.name = :permission ");
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeePrimaryKey.getId());
        query.setParameter("tenantId", employeePrimaryKey.getTenantId());
        if (permission != null) {
            query.setParameter("permission", permission.name());
        }
        return query.getResultList();
    }

    public Collection<String> managerAccountIds(PrimaryKey employeePrimaryKey, Permissions permission) {
        String sql =
            "SELECT DISTINCT ua.id " +
            " FROM UserAccount ua " +
            "  LEFT JOIN User_Group ug ON ua.id = ug.user_id AND ua.tenantId = ug.user_tenantId " +
            "  LEFT JOIN GroupAccount ga ON ug.group_id = ga.id AND ug.group_tenantId = ga.tenantId " +
            "  LEFT JOIN Role_Account ra ON ra.account_id = ua.id OR ra.account_id = ga.id " +
            "  LEFT JOIN Role r ON r.id = ra.role_id AND r.tenantId = ra.role_tenantId " +
            "  LEFT JOIN Role_ACE role_ace ON r.id = role_ace.role_id AND r.tenantId = role_ace.role_tenantId " +
            "  LEFT JOIN ACE ace ON ace.id = role_ace.ace_id AND ace.tenantId = role_ace.ace_tenantId " +
            "  LEFT JOIN Role_Permission arp ON r.id = arp.Role_id AND r.tenantId = arp.Role_tenantId " +
            "  LEFT JOIN Permission ap ON ap.id = arp.permissions_id " +
            (permission == null ? ""
                : " LEFT JOIN Role_Permission rp ON r.id = rp.Role_id AND r.tenantId = rp.Role_tenantId " +
                  " LEFT JOIN Permission p ON p.id = rp.permissions_id ") +
            "  , Team t " +
            "  LEFT JOIN AOMRelationship rel ON rel.dst_id = t.id AND t.tenantId = rel.dst_tenantId " +
            "  LEFT JOIN Site s ON rel.src_id = s.id AND s.tenantId = rel.src_tenantId " +
            "  LEFT JOIN EmployeeTeam ett ON t.id = ett.teamId AND t.tenantId = ett.teamTenantId " +
            "WHERE ua.tenantId = :tenantId AND t.tenantId = ua.tenantId " +
            "  AND ett.employeeId = :employeeId " +
            "  AND ap.name IN ('OrganizationProfile_View', 'OrganizationProfile_Mgmt') " +
            "  AND (   ace.pattern IS NULL " +
            "       OR t.path IS NULL AND s.path IS NULL " +
            "       OR t.path IS NULL AND s.path REGEXP ace.pattern AND ace.entityClass = 'Site' " +
            "       OR t.path REGEXP ace.pattern AND ace.entityClass = 'Team')" +
            (permission == null ? "" : " AND p.name = :permission ");

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeePrimaryKey.getId());
        query.setParameter("tenantId", employeePrimaryKey.getTenantId());
        if (permission != null) {
            query.setParameter("permission", permission.name());
        }
        return query.getResultList();
    }

	/**
     * Partially populates and returns an EmployeeAvailabilityInfoDto.
     * For convenience, the calling Facade layer is expected to use its BaseFacade.toDto()   
     * to set the EmployeeDto in the EmployeeAvailabilityInfoDto returned from this method.
     * 
     * @param employee
     * @return
     */
	public EmployeeInfoDto getEmployeeInfo(Employee employee) {
		EmployeeInfoDto employeeInfoDto = new EmployeeInfoDto();
		
		Site site = employee.getSite();
		if (site != null){
			employeeInfoDto.setSiteName( site.getName() );
			employeeInfoDto.setSiteId( site.getId() );
			employeeInfoDto.setSiteTz( site.getTimeZone() );
			employeeInfoDto.setSiteFirstDayOfweek( site.getFirstDayOfWeek() );
		}
		
		Set<EmployeeTeam> employeeTeams = employee.getEmployeeTeams();
		for (EmployeeTeam employeeTeam : employeeTeams){
			EmployeeInfoDto.TeamDto teamDto = new EmployeeInfoDto.TeamDto();
			teamDto.id = employeeTeam.getTeam().getId();
			teamDto.name = employeeTeam.getTeam().getName();
			teamDto.isHomeTeam = employeeTeam.getIsHomeTeam();
			employeeInfoDto.getTeams().add(teamDto);
		}

		Set<EmployeeSkill> employeeSkills = employee.getEmployeeSkills();
		for (EmployeeSkill employeeSkill : employeeSkills){
			EmployeeInfoDto.SkillDto skillDto = new EmployeeInfoDto.SkillDto();
			skillDto.id = employeeSkill.getSkill().getId();
			skillDto.abbreviation = employeeSkill.getSkill().getAbbreviation();
			skillDto.isPrimary = employeeSkill.getIsPrimarySkill();
			employeeInfoDto.getSkills().add(skillDto);
		}
		employeeInfoDto.setAccountId(employee.getUserAccount().getId());
		return employeeInfoDto;
	}

	/**
	 * Partially populates and returns an EmployeeAvailabilityInfoDto.
	 * For convenience, the calling Facade layer is expected to use its BaseFacade.toDto()   
	 * to set the EmployeeDto in the EmployeeAvailabilityInfoDto returned from this method.
	 * 
	 * @param employee
	 * @param scheduleStatus
	 * @param start
	 * @param end
	 * @return
	 */
	public EmployeeAvailabilityDto getEmployeeAvailability(Employee employee, ScheduleStatus scheduleStatus, Long start,
                                                           Long end) {
		EmployeeAvailabilityDto employeeAvailabilityDto = new EmployeeAvailabilityDto();

        Collection<Holiday> orgHolidays = organizationService.getHolidays(employee.getTenantId(), start, end);
        List<OrgHolidayDto> orgHolidayDtos = employeeAvailabilityDto.getOrgHolidays();
        for (Holiday holiday : orgHolidays) {
            orgHolidayDtos.add(toEmployeeAvailabilityOrgHolidayDto(holiday));
        }

        List<Schedule> schedules = getEmployeeSchedules(employee, scheduleStatus, start, end);
        for (Schedule schedule : schedules) {
            EmployeeAvailabilityDto.ScheduleDto scheduleDto = new EmployeeAvailabilityDto.ScheduleDto();
            scheduleDto.setId(schedule.getId());
            scheduleDto.setName(schedule.getName());
            scheduleDto.setStartDate(schedule.getStartDate());
            scheduleDto.setEndDate(schedule.getEndDate());
            employeeAvailabilityDto.getSchedules().add(scheduleDto);
        }

        String sql =
            "SELECT * FROM CDAvailabilityTimeFrame " +
            " WHERE tenantId = :tenantId AND employeeId = :employeeId " +
            "   AND availabilityType = '" + AvailabilityType.UnAvail.name() + "'" +
            (start == null ? "" : " AND ADDDATE(startDateTime, INTERVAL durationInMinutes MINUTE) >= :startDate ") +
            (end == null ? "" : " AND startDateTime <= :endDate ");

        Query query = entityManager.createNativeQuery(sql, CDAvailabilityTimeFrame.class);
        query.setParameter("tenantId", employee.getTenantId());
        query.setParameter("employeeId", employee.getId());
        if (start != null) {
            query.setParameter("startDate", new Timestamp(start));
        }
        if (end != null) {
            query.setParameter("endDate", new Timestamp(end));
        }

        List<CDAvailabilityTimeFrame> availabilityTimeFrames = query.getResultList();
        List<EmployeeUnavailabilityDto> employeeUnavailabilityDtos = employeeAvailabilityDto.getEmpUnavailabilities();

        for (CDAvailabilityTimeFrame cdUnavailability : availabilityTimeFrames) {
            DateTime startTime = cdUnavailability.getStartDateTime();
            DateTime endTime = startTime.plusMinutes(cdUnavailability.getDurationInMinutes().getMinutes());

            EmployeeUnavailabilityDto empUnavailDto = new EmployeeUnavailabilityDto();
            empUnavailDto.setStartDate(startTime.getMillis());
            empUnavailDto.setEndDate(endTime.getMillis());
            empUnavailDto.setPto(cdUnavailability.getIsPTO());
            employeeUnavailabilityDtos.add(empUnavailDto);
        }

		return employeeAvailabilityDto;
	}

	private EmployeeAvailabilityDto.OrgHolidayDto toEmployeeAvailabilityOrgHolidayDto(Holiday holiday) {
		EmployeeAvailabilityDto.OrgHolidayDto orgHolidayDto = new EmployeeAvailabilityDto.OrgHolidayDto();
		orgHolidayDto.setName(holiday.getName());
		orgHolidayDto.setStartDate(holiday.getEffectiveStartDate());
		orgHolidayDto.setEndDate(holiday.getEffectiveEndDate());
		return orgHolidayDto;
	}

	private List<Schedule> getEmployeeSchedules(Employee employee, ScheduleStatus scheduleStatus, Long dateRangeStart,
                                                Long dateRangeEnd) {
		String tenantId = employee.getTenantId();
		String employeeId = employee.getId();
		int scheduleStatusOrdinal = scheduleStatus.ordinal();
		
		DateTime newStartDate = null;
		if (dateRangeStart != null) {
			newStartDate = new DateTime(dateRangeStart);			
		}

		DateTime newEndDate = null;
		if (dateRangeEnd != null) {
			newEndDate = new DateTime(dateRangeEnd);
		}

		String sql = "SELECT DISTINCT  sc.* " +
				     "  FROM Schedule sc INNER JOIN Shift sh ON sc.id = sh.scheduleId " +
                     " WHERE sh.employeeId = :employeeId " +
                     "   AND sc.status = :scheduleStatus " +
                     "   AND sc.tenantId = :tenantId ";

		if (newStartDate != null && newEndDate != null) {  // include ONLY overlapping
			sql = sql.concat(
					"  AND ("
					+ "     (sh.startDateTime >= '" + newStartDate + "' AND sh.startDateTime <= '" + newEndDate + "') OR "
					+ "     (sh.endDateTime   >= '" + newStartDate + "' AND sh.endDateTime   <= '" + newEndDate + "') OR "
					+ "     (sh.startDateTime <  '" + newStartDate + "' AND sh.endDateTime   >  '" + newEndDate + "')"
					+ ")"  
					);
		} else if (newStartDate == null && newEndDate != null) {  // include EACH ending ON OR BEFORE end date
			sql = sql.concat(" AND sh.endDateTime <= '" + newEndDate + "'");
		} else if (newStartDate != null && newEndDate == null) {  // include EACH starting ON OR AFTER start date
			sql = sql.concat(" AND sh.startDateTime >= '" + newStartDate + "'");			
		}

		Query query = entityManager.createNativeQuery(sql, Schedule.class);
		query.setParameter("employeeId", employeeId);
		query.setParameter("scheduleStatus", scheduleStatusOrdinal);
		query.setParameter("tenantId", tenantId);
		
		return query.getResultList();
	}

    /**
     * Utility method for getting a collection of like-skilled teammates.  Returns ids instead of 
     * entities for convenience of callers with special JPA transaction management needs.
     * 
     * TODO: May want to reimplement with query instead of entity traversals.
     * 
     * @param teamPk
     * @param skillPk
     * @return list of team member employee ids
     */
	public List<String> getLikeSkilledTeammates(PrimaryKey teamPk, PrimaryKey skillPk) {
		List<String> retList = new ArrayList<>();
		Team team = teamService.getTeam(teamPk);
		Skill skill = skillService.getSkill(skillPk);
		Set<EmployeeTeam> employeeTeams = team.getEmployeeTeams();
		for (EmployeeTeam employeeTeam : employeeTeams){
			Employee employee = employeeTeam.getEmployee();
			if (!employee.getIsDeleted()) {
				Set<EmployeeSkill> employeeSkills = employee.getEmployeeSkills();
				for (EmployeeSkill employeeSkill : employeeSkills) {
					if (employeeSkill.getSkill().equals(skill)) {
						retList.add(employee.getId());
					}
				}
			}
		}
		return retList;
	}

    @SuppressWarnings("unchecked")
    public Collection<Employee> getEmployeesByIds(String tenantId, Collection<String> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        } else {
            String idsText = ModelUtils.commaSeparatedQuotedValues(ids);
            Query query = entityManager.createQuery(
                    "SELECT e FROM Employee e WHERE e.primaryKey.tenantId = :tenantId " +
                    "   AND e.primaryKey.id IN (" + idsText + ")");
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        }
    }

	/**
	 * creates an account login out the employee information. fist try to use the provide login, then  employee identifier, then first & last name, 
	 * then random id. in case login is not unique, fallsback to random id
	 * @param emp
	 * @param logins
	 * @param preferEmpId 
	 * @return
	 */
	public String createLogin(Employee emp, Set<String> logins, String firstName, String lastName, String login,
                              boolean preferEmpId) {
		// try the provided login if not null, or the employee identifier
		// note this test is far from perfect. need to check lengths as well
		if ((StringUtils.isEmpty(login) || login.length() < 3) && preferEmpId) {
			// try to use the id
			String id = emp.getEmployeeIdentifier();
			if (!StringUtils.isEmpty(id) && id.length() >= 3) {
				// TODO harden this validity check
				if (StringUtils.containsOnly(id, "abcdefeghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._")) {
					login = id;
				}
			}
		}

		// otherwise try to use first name/last name
		if (login == null &&  !StringUtils.isEmpty(firstName) && !StringUtils.isEmpty(lastName)) {
			login = firstName.toLowerCase();
			String lastname = lastName.toLowerCase();
			login += lastname.charAt(0);
			login += lastname.charAt(lastname.length()-1);
		}
		// remove some unwanted chars, in case we get invalid ones
		login = StringUtils.replaceChars(login, " `~!@#$%^&*()_-+=[]{}|<>?,./:;\"\\'", null);
		
		// check login is unique
		if (login != null) {
			if (logins != null) {  // via array of known logins if provided
				if (logins.contains(login)) {
					// login already exist, reset it to generate a new one
					login = null;
				}
			} else {				// via query otherwise
				if (userAccountService.getUserAccountBylogin(emp.getTenantId(), login) != null) {
					// login already exist, reset it to generate a new one
					login = null;
				}
			}
		}
		
		// last, generate a random login
		if (login == null) {
			login = new PrimaryKey(emp.getTenantId()).getId();
		}
		return login;
	}
	
	/**
	 * Provides all the data necessary to populate the Availability & Preference Display
	 * @param employee
	 * @param viewStartDateTimeInUTC
	 * @param viewEndDateTimeInUTC
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public AvailcalViewDto getAvailcalView(Employee employee, DateTime viewStartDateTimeInUTC,
                                           DateTime viewEndDateTimeInUTC) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		
		AvailcalViewDto availCalDto = new AvailcalViewDto(employee.getId(), viewStartDateTimeInUTC.getMillis(),
                viewEndDateTimeInUTC.getMillis());

        availCalDto.setStartDate(employee.getStartDate() == null ? 0 : employee.getStartDate().toDate().getTime());
        availCalDto.setEndDate(employee.getEndDate() == null ? 0 : employee.getEndDate().toDate().getTime());

        populateAvailCalDtoPreference(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC, availCalDto);
		
		populateAvailCalDtoAvailability(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC, availCalDto);

		// Prepare to look for relevant contract lines....
		Set<EmployeeContract> employeeContracts = employee.getEmployeeContracts();
        Set<ContractLine> contractLines = new HashSet<>();
        for (EmployeeContract contract : employeeContracts) {
            contractLines.addAll(contract.getContractLines());
        }
        
		// Set coupleWeekends if there is a ContractLine, else leave default false value.
    	ContractLineType coupleWeekendsClType = ContractLineType.COMPLETE_WEEKENDS;
    	for (ContractLine contractLine : contractLines) {
			if (contractLine.getContractLineType().equals(coupleWeekendsClType)) {
                BooleanCL coupleWeekendsCL = (BooleanCL) contractLine;
        		availCalDto.setCoupleWeekends(coupleWeekendsCL.getEnabled());
    			break;
    		}
    	}
    	
		// Set weekdayRotations where there are ContractLines, else leave default NONE (1:1) value.
    	for (ContractLine contractLine : contractLines) {
			if (contractLine instanceof WeekdayRotationPatternCL) {
                WeekdayRotationPatternCL weekdayRotationPatternCL = (WeekdayRotationPatternCL) contractLine;
        		if (weekdayRotationPatternCL.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            		if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 2) {
            			availCalDto.getWeekdayRotations().setSunday(WeekdayRotationValue.EVERY_OTHER);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 3) {
            			availCalDto.getWeekdayRotations().setSunday(WeekdayRotationValue.EVERY_THIRD);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 2
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 4) {
            			availCalDto.getWeekdayRotations().setSunday(WeekdayRotationValue.TWO_OF_EVERY_FOUR);            			
            		}        		        			
        		} else if (weekdayRotationPatternCL.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            		if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 2) {
            			availCalDto.getWeekdayRotations().setMonday(WeekdayRotationValue.EVERY_OTHER);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 3) {
            			availCalDto.getWeekdayRotations().setMonday(WeekdayRotationValue.EVERY_THIRD);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 2
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 4) {
            			availCalDto.getWeekdayRotations().setMonday(WeekdayRotationValue.TWO_OF_EVERY_FOUR);            			
            		}        		        			        			
        		} else if (weekdayRotationPatternCL.getDayOfWeek().equals(DayOfWeek.TUESDAY)) {
            		if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 2) {
            			availCalDto.getWeekdayRotations().setTuesday(WeekdayRotationValue.EVERY_OTHER);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 3) {
            			availCalDto.getWeekdayRotations().setTuesday(WeekdayRotationValue.EVERY_THIRD);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 2
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 4) {
            			availCalDto.getWeekdayRotations().setTuesday(WeekdayRotationValue.TWO_OF_EVERY_FOUR);            			
            		}        		        			
        		} else if (weekdayRotationPatternCL.getDayOfWeek().equals(DayOfWeek.WEDNESDAY)) {
            		if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 2) {
            			availCalDto.getWeekdayRotations().setWednesday(WeekdayRotationValue.EVERY_OTHER);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 3) {
            			availCalDto.getWeekdayRotations().setWednesday(WeekdayRotationValue.EVERY_THIRD);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 2
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 4) {
            			availCalDto.getWeekdayRotations().setWednesday(WeekdayRotationValue.TWO_OF_EVERY_FOUR);            			
            		}        		        			
        		} else if (weekdayRotationPatternCL.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            		if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 2) {
            			availCalDto.getWeekdayRotations().setThursday(WeekdayRotationValue.EVERY_OTHER);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 3) {
            			availCalDto.getWeekdayRotations().setThursday(WeekdayRotationValue.EVERY_THIRD);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 2
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 4) {
            			availCalDto.getWeekdayRotations().setThursday(WeekdayRotationValue.TWO_OF_EVERY_FOUR);            			
            		}        		        			
        		} else if (weekdayRotationPatternCL.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            		if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 2) {
            			availCalDto.getWeekdayRotations().setFriday(WeekdayRotationValue.EVERY_OTHER);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 3) {
            			availCalDto.getWeekdayRotations().setFriday(WeekdayRotationValue.EVERY_THIRD);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 2
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 4) {
            			availCalDto.getWeekdayRotations().setFriday(WeekdayRotationValue.TWO_OF_EVERY_FOUR);            			
            		}        		        			
        		} else if (weekdayRotationPatternCL.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            		if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 2) {
            			availCalDto.getWeekdayRotations().setSaturday(WeekdayRotationValue.EVERY_OTHER);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 1
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 3) {
            			availCalDto.getWeekdayRotations().setSaturday(WeekdayRotationValue.EVERY_THIRD);            			
            		} else if (weekdayRotationPatternCL.getNumberOfDays() == 2
                            && weekdayRotationPatternCL.getOutOfTotalDays() == 4) {
            			availCalDto.getWeekdayRotations().setSaturday(WeekdayRotationValue.TWO_OF_EVERY_FOUR);            			
            		}        		        			
        		}
    		}
    	}

    	return availCalDto;
	}

	public AvailcalViewDto getAvailcalPreviewCDAvail(Employee employee, DateTime viewStartDateTimeInUTC, 
			DateTime viewEndDateTimeInUTC, AvailcalUpdateParamsCDAvailDto params) throws InstantiationException, 
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String tenantId = employee.getTenantId();
		DateTimeZone timeZone = employee.getSite().getTimeZone();

		AvailAction actionParam = params.getAction();
		if (actionParam == null){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.missingaction"));
		}

		AvailcalViewDto availCalDto = null;
		if (viewStartDateTimeInUTC != null  &&  viewEndDateTimeInUTC != null){
			validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
			availCalDto = this.getAvailcalView(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		} else {
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.daterange"));
		}
		//System.out.println("AvailcalUpdateParamsCDAvailDto... " +  params.toString(timeZone));  // useful for debugging
		//System.out.println("BEFORE PREVIEW DATA... " + availCalDto.toString(timeZone));         // useful for debugging
		
		Collection<AvailCDTimeFrame> viewCDTimeFrames = availCalDto.getAvailCDTimeFrames();
		Collection<AvailCITimeFrame> viewCITimeFrames = availCalDto.getAvailCITimeFrames();
		List<AvailCDTimeFrame> addedAvailCDTimeFrames = new ArrayList<AvailCDTimeFrame>();
		
		for (Long selectedDateInUTC : params.getSelectedDates()){
			DateTime selectedDateTimeStartInTZ = new DateTime( selectedDateInUTC, timeZone);
			DateTime selectedDateTimeEndInTz = selectedDateTimeStartInTZ.plusDays(1);
			DateTime selectedDateTimeStartInUTC = new DateTime(selectedDateTimeStartInTZ.toInstant(), DateTimeZone.UTC);
			DateTime selectedDateTimeEndInUTC = new DateTime(selectedDateTimeEndInTz.toInstant(), DateTimeZone.UTC);
			Interval selectedDateInterval = new Interval(selectedDateTimeStartInUTC, selectedDateTimeEndInUTC);
			if ( !selectedDateTimeStartInTZ.equals(selectedDateTimeStartInTZ.withTimeAtStartOfDay())){
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
			}
			
			// Flag selected date's existing CD timeframes as REMOVED since they would get replaced...
			for (AvailCDTimeFrame viewCDTimeFrame : viewCDTimeFrames){
				Interval timeFrameInterval = 
						new Interval(viewCDTimeFrame.getStartDateTime(), viewCDTimeFrame.getEndDateTime());
				if (timeFrameInterval.overlaps(selectedDateInterval)){
					viewCDTimeFrame.setPreview(PreviewType.REMOVED);
				}
			}

			// Flag selected date's existing CI timeframe instances as REMOVED since they would get overridden...
			for (AvailCITimeFrame viewCITimeFrame : viewCITimeFrames){
				Collection<TimeFrameInstance> viewCITimeFrameInstances = viewCITimeFrame.getTimeFrameInstances();
				for (TimeFrameInstance viewCITimeFrameInstance : viewCITimeFrameInstances){
					if (viewCITimeFrameInstance.getStartDateTime().equals(selectedDateInterval.getStartMillis()) &&
							viewCITimeFrameInstance.getEndDateTime() == null){
						viewCITimeFrameInstance.setPreview(PreviewType.REMOVED);
					} else if (viewCITimeFrameInstance.getStartDateTime() != null &&
							viewCITimeFrameInstance.getEndDateTime() != null){
						Interval timeFrameInterval = 
								new Interval(viewCITimeFrameInstance.getStartDateTime(), viewCITimeFrameInstance.getEndDateTime());
						if (timeFrameInterval.overlaps(selectedDateInterval)){
							viewCITimeFrameInstance.setPreview(PreviewType.REMOVED);
						}						
					} 
				}
			}

			// Add the selected date's new CD timeframes flagged as ADDED...
			if (AvailAction.AVAILABLE_FOR_DAY.equals(actionParam)){
				// NOTHING MORE TO DO!
				// Existing replaced/overridden already flagged REMOVED.
				// Nothing else to display since implicitly available. 
			} else if (AvailAction.UNAVAILABLE_FOR_DAY.equals(actionParam)){
				// Using Joda to calculate duration, as it will take DST into account.
				org.joda.time.Minutes durationInMinutes = 
						org.joda.time.Minutes.minutesBetween(selectedDateTimeStartInTZ, selectedDateTimeEndInTz);
				long durationInMillis = (long) TimeUnit.MINUTES.toMillis(durationInMinutes.getMinutes());
				// Making reason empty string because Availcal UI dialog (and params) don't support it.
				AvailCDTimeFrame availCDTimeFrame = new AvailCDTimeFrame();
				availCDTimeFrame.setPreview(PreviewType.ADDED);
				availCDTimeFrame.setStartDateTime(selectedDateInUTC);
				availCDTimeFrame.setEndDateTime(selectedDateInUTC + durationInMillis);
				availCDTimeFrame.setAvailType(AvailType.DAY_OFF);
				availCDTimeFrame.setPTO(params.isPto());
				
				PrimaryKey absenceTypePk = new PrimaryKey(tenantId, params.getAbsenceTypeId());
				AbsenceType absenceType = absenceTypeService.getAbsenceType(absenceTypePk);
				if (absenceType != null) {
					availCDTimeFrame.setAbsenceTypeName(absenceType.getName());
				}
				
				availCalDto.getAvailCDTimeFrames().add(availCDTimeFrame);
			} else if (AvailAction.AVAILABLE_FOR_TIMEFRAMES.equals(actionParam)) {
				List<AvailcalSimpleTimeFrame> availTimeFrames = params.getTimeFrames();
				for (AvailcalSimpleTimeFrame timeFrame : availTimeFrames){			
					AvailCDTimeFrame availCDTimeFrame = new AvailCDTimeFrame();
					availCDTimeFrame.setPreview(PreviewType.ADDED);
					availCDTimeFrame.setStartDateTime(timeFrame.getStartTime());
					availCDTimeFrame.setEndDateTime(timeFrame.getEndTime());
					availCDTimeFrame.setAvailType(AvailType.AVAIL);
					availCalDto.getAvailCDTimeFrames().add(availCDTimeFrame);				
				}				
			} else {
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.invalidaction"));
			}						
		}				
		
		//System.out.println("AFTER PREVIEW DATA... " + availCalDto.toString(timeZone));  // useful for debugging
		return availCalDto;
	}

	public AvailcalViewDto getAvailcalPreviewCIAvail(Employee employee, DateTime viewStartDateTimeInUTC, 
			DateTime viewEndDateTimeInUTC, AvailcalUpdateParamsCIAvailDto params) throws InstantiationException, 
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		String tenantId = employee.getTenantId();
		DateTimeZone timeZone = employee.getSite().getTimeZone();

		AvailAction actionParam = params.getAction();
		if (actionParam == null){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.missingaction"));
		}

		AvailcalViewDto availCalDto = null;
		if (viewStartDateTimeInUTC != null  &&  viewEndDateTimeInUTC != null){
			validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
			availCalDto = this.getAvailcalView(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		} else {
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.daterange"));
		}
		//System.out.println("AvailcalUpdateParamsCIAvailDto... " +  params.toString(timeZone));  // useful for debugging
		//System.out.println("BEFORE PREVIEW DATA... " + availCalDto.toString(timeZone));         // useful for debugging

		Collection<AvailCDTimeFrame> viewCDTimeFrames = availCalDto.getAvailCDTimeFrames();
		Collection<AvailCITimeFrame> viewCITimeFrames = availCalDto.getAvailCITimeFrames();
		List<AvailCDTimeFrame> addedAvailCDTimeFrames = new ArrayList<AvailCDTimeFrame>();

		AvailcalUpdateParamsCIDaySelections selectedDays = params.getSelectedDays();
		List<DayOfWeek> selectedDaysOfWeek = new ArrayList<DayOfWeek>();
		if (selectedDays.isSunday())    {selectedDaysOfWeek.add(DayOfWeek.SUNDAY);}
		if (selectedDays.isMonday())    {selectedDaysOfWeek.add(DayOfWeek.MONDAY);}
		if (selectedDays.isTuesday())   {selectedDaysOfWeek.add(DayOfWeek.TUESDAY);}
		if (selectedDays.isWednesday()) {selectedDaysOfWeek.add(DayOfWeek.WEDNESDAY);}
		if (selectedDays.isThursday())  {selectedDaysOfWeek.add(DayOfWeek.THURSDAY);}
		if (selectedDays.isFriday())    {selectedDaysOfWeek.add(DayOfWeek.FRIDAY);}
		if (selectedDays.isSaturday())  {selectedDaysOfWeek.add(DayOfWeek.SATURDAY);}

		DateTime viewStartDateInUTC = new DateTime(viewStartDateTimeInUTC, DateTimeZone.UTC);
		DateTime viewEndDateInUTC = new DateTime(viewEndDateTimeInUTC, DateTimeZone.UTC);
		Interval viewDatesIntervalInUTC = new Interval(viewStartDateInUTC, viewEndDateInUTC);

		DateTime addedEffectiveStartDateInUTC = new DateTime(params.getEffectiveStartDate(), DateTimeZone.UTC);
		DateTime addedEffectiveEndDateInUTC = new DateTime(params.getEffectiveEndDate(), DateTimeZone.UTC);
		Interval addedEffectiveDatesIntervalInUTC = 
				new Interval(addedEffectiveStartDateInUTC, addedEffectiveEndDateInUTC);

		Interval timeframeInstanceRemovalInterval = new Interval(
				new DateTime(addedEffectiveStartDateInUTC, timeZone),
				new DateTime(addedEffectiveEndDateInUTC, timeZone).plusDays(1));
		timeframeInstanceRemovalInterval.getStart();

		String employeeId = employee.getId();
		SimpleQuery simpleQuery = new SimpleQuery(tenantId );			
        simpleQuery.addFilter("startDateTime >= '" + addedEffectiveStartDateInUTC + "'");
        simpleQuery.addFilter("startDateTime < '" + addedEffectiveEndDateInUTC.plusDays(1) + "'");
        simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
    	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
    	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
    	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvail + "'");
    	simpleQuery.setTotalCount(true);
		ResultSet<CDAvailabilityTimeFrame> persistedCDAvailTimeframes = 
				cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);

		for (DayOfWeek selectedDayOfWeek : selectedDaysOfWeek){
			// Flag selected DayOfWeek's existing CI timeframes/instances as REMOVED where applicable...
			for (AvailCITimeFrame viewCITimeFrame : viewCITimeFrames){				
				DateTime existingEffectiveStartDateInUTC = 
						new DateTime(viewCITimeFrame.getEffectiveDateRangeStart(), DateTimeZone.UTC);

				DateTime existingEffectiveEndDateInUTC = 
						new DateTime(viewCITimeFrame.getEffectiveDateRangeEnd(), DateTimeZone.UTC);

				Interval existingEffectiveDatesIntervalInUTC = 
						new Interval(existingEffectiveStartDateInUTC, existingEffectiveEndDateInUTC);

				if (addedEffectiveDatesIntervalInUTC.overlaps(existingEffectiveDatesIntervalInUTC) &&  
						selectedDayOfWeek.equals( viewCITimeFrame.getDayOfTheWeek() )){

					Collection<TimeFrameInstance> viewCITimeFrameInstances = viewCITimeFrame.getTimeFrameInstances();
					for (TimeFrameInstance viewCITimeFrameInstance : viewCITimeFrameInstances){
						if (timeframeInstanceRemovalInterval.contains(viewCITimeFrameInstance.getStartDateTime())){
							viewCITimeFrameInstance.setPreview(PreviewType.REMOVED);
						}
					}
					if (addedEffectiveDatesIntervalInUTC.contains(existingEffectiveDatesIntervalInUTC)){
						viewCITimeFrame.setPreview(PreviewType.REMOVED);
					}
				}				
			}

			// Add the selected date's new CI timeframes flagged as ADDED...
			List<DateTime> possibleInstanceDatesInTZ = new ArrayList<DateTime>();
			DateTime indexDateInTZ = new DateTime(viewStartDateTimeInUTC, timeZone);
			while (!indexDateInTZ.isAfter(viewEndDateTimeInUTC)){				
				if (indexDateInTZ.getDayOfWeek() == selectedDayOfWeek.getJodaValue()){
					possibleInstanceDatesInTZ.add(new DateTime(indexDateInTZ, timeZone));
				}
				indexDateInTZ = indexDateInTZ.plusDays(1);
			}
			if (AvailAction.AVAILABLE_FOR_TIMEFRAMES.equals(actionParam)){				
				for (AvailcalSimpleTimeFrame timeFrame : params.getTimeFrames()){
					AvailCITimeFrame addedCITimeFrame = new AvailCITimeFrame();

					// Populate initial timeframe instances...
					Collection<TimeFrameInstance> timeFrameInstances = new ArrayList<TimeFrameInstance>();
					for (DateTime possibleInstanceDateInTZ: possibleInstanceDatesInTZ){
						TimeFrameInstance timeFrameInstance = new TimeFrameInstance();
						timeFrameInstance.setStartDateTime(
								possibleInstanceDateInTZ.toInstant().getMillis() 
								+ timeFrame.getStartTime());
						timeFrameInstance.setEndDateTime(
								possibleInstanceDateInTZ.toInstant().getMillis() 
								+ timeFrame.getEndTime());
						timeFrameInstance.setPreview(PreviewType.ADDED);
						timeFrameInstances.add(timeFrameInstance);
					}
					
					// Remove timeframe instances overridden by viewable CD timeframes...
					Collection<TimeFrameInstance> timeFrameInstancesToBeRemoved = new ArrayList<TimeFrameInstance>();
					for (AvailCDTimeFrame viewCDTimeFrame : viewCDTimeFrames){
						for (TimeFrameInstance timeFrameInstance : timeFrameInstances){
							DateTime timeFrameInstanceStartInTZ = new DateTime(timeFrameInstance.getStartDateTime(), timeZone);
							Interval dayIntervalForTimeFrame = new Interval(timeFrameInstanceStartInTZ.withTimeAtStartOfDay(), 
									timeFrameInstanceStartInTZ.withTimeAtStartOfDay().plusDays(1));
							if (dayIntervalForTimeFrame.contains(viewCDTimeFrame.getStartDateTime())){
								timeFrameInstancesToBeRemoved.add(timeFrameInstance);
							}
						}
					}
					for (TimeFrameInstance timeFrameInstanceToBeRemoved : timeFrameInstancesToBeRemoved){
						timeFrameInstances.remove(timeFrameInstanceToBeRemoved);
					}

					// Remove timeframe instances overridden by non-viewable CD timeframes 
					//            (CDAvailabilityTimeFrames with AvailabilityType.UnAvail) ...
					timeFrameInstancesToBeRemoved = new ArrayList<TimeFrameInstance>();
					for (CDAvailabilityTimeFrame persistedCDAvailTimeframe : persistedCDAvailTimeframes.getResult()){
						for (TimeFrameInstance timeFrameInstance : timeFrameInstances){
							DateTime timeFrameInstanceStartInTZ = new DateTime(timeFrameInstance.getStartDateTime(), timeZone);
							Interval dayIntervalForTimeFrame = new Interval(timeFrameInstanceStartInTZ.withTimeAtStartOfDay(), 
									timeFrameInstanceStartInTZ.withTimeAtStartOfDay().plusDays(1));
							if (dayIntervalForTimeFrame.contains(persistedCDAvailTimeframe.getStartDateTime())){
								timeFrameInstancesToBeRemoved.add(timeFrameInstance);
							}
						}
					}
					for (TimeFrameInstance timeFrameInstanceToBeRemoved : timeFrameInstancesToBeRemoved){
						timeFrameInstances.remove(timeFrameInstanceToBeRemoved);
					}
					
					addedCITimeFrame.setTimeFrameInstances(timeFrameInstances);			
					addedCITimeFrame.setDayOfTheWeek(selectedDayOfWeek);
					addedCITimeFrame.setEffectiveDateRangeStart(params.getEffectiveStartDate());
					addedCITimeFrame.setEffectiveDateRangeEnd(params.getEffectiveEndDate());
					addedCITimeFrame.setStartTime(timeFrame.getStartTime());
					addedCITimeFrame.setEndTime(timeFrame.getEndTime());
					addedCITimeFrame.setAvailType(AvailType.AVAIL);
					addedCITimeFrame.setPreview(PreviewType.ADDED);
					availCalDto.getAvailCITimeFrames().add(addedCITimeFrame);
				}
			} else if (AvailAction.UNAVAILABLE_FOR_DAY.equals(actionParam)  ||
					AvailAction.AVAILABLE_FOR_DAY.equals(actionParam)){
				AvailCITimeFrame addedCITimeFrame = new AvailCITimeFrame();
				
				// Populate initial timeframe instances...
				Collection<TimeFrameInstance> timeFrameInstances = new ArrayList<TimeFrameInstance>();
				for (DateTime possibleInstanceDateInTZ: possibleInstanceDatesInTZ){
					TimeFrameInstance timeFrameInstance = new TimeFrameInstance();
					timeFrameInstance.setStartDateTime(
							possibleInstanceDateInTZ.toInstant().getMillis());
					timeFrameInstance.setEndDateTime(null);
					timeFrameInstance.setPreview(PreviewType.ADDED);
					timeFrameInstances.add(timeFrameInstance);
				}
				
				// Remove timeframe instances overridden by viewable CD timeframes...
				Collection<TimeFrameInstance> timeFrameInstancesToBeRemoved = new ArrayList<TimeFrameInstance>();
				for (AvailCDTimeFrame viewCDTimeFrame : viewCDTimeFrames){
					for (TimeFrameInstance timeFrameInstance : timeFrameInstances){
						DateTime timeFrameInstanceStartInTZ = new DateTime(timeFrameInstance.getStartDateTime(), timeZone);
						Interval dayIntervalForTimeFrame = new Interval(timeFrameInstanceStartInTZ.withTimeAtStartOfDay(), 
								timeFrameInstanceStartInTZ.withTimeAtStartOfDay().plusDays(1));
						if (dayIntervalForTimeFrame.contains(viewCDTimeFrame.getStartDateTime())){
							timeFrameInstancesToBeRemoved.add(timeFrameInstance);
						}
					}
				}
				for (TimeFrameInstance timeFrameInstanceToBeRemoved : timeFrameInstancesToBeRemoved){
					timeFrameInstances.remove(timeFrameInstanceToBeRemoved);
				}

				// Remove timeframe instances overridden by non-viewable CD timeframes 
				//            (CDAvailabilityTimeFrames with AvailabilityType.UnAvail) ...
				timeFrameInstancesToBeRemoved = new ArrayList<TimeFrameInstance>();
				for (CDAvailabilityTimeFrame persistedCDAvailTimeframe : persistedCDAvailTimeframes.getResult()){
					for (TimeFrameInstance timeFrameInstance : timeFrameInstances){
						DateTime timeFrameInstanceStartInTZ = new DateTime(timeFrameInstance.getStartDateTime(), timeZone);
						Interval dayIntervalForTimeFrame = new Interval(timeFrameInstanceStartInTZ.withTimeAtStartOfDay(), 
								timeFrameInstanceStartInTZ.withTimeAtStartOfDay().plusDays(1));
						if (dayIntervalForTimeFrame.contains(persistedCDAvailTimeframe.getStartDateTime())){
							timeFrameInstancesToBeRemoved.add(timeFrameInstance);
						}
					}
				}
				for (TimeFrameInstance timeFrameInstanceToBeRemoved : timeFrameInstancesToBeRemoved){
					timeFrameInstances.remove(timeFrameInstanceToBeRemoved);
				}

				if (AvailAction.UNAVAILABLE_FOR_DAY.equals(actionParam)){
					addedCITimeFrame.setAvailType(AvailType.DAY_OFF);
					addedCITimeFrame.setTimeFrameInstances(timeFrameInstances);			
					addedCITimeFrame.setDayOfTheWeek(selectedDayOfWeek);
					addedCITimeFrame.setEffectiveDateRangeStart(params.getEffectiveStartDate());
					addedCITimeFrame.setEffectiveDateRangeEnd(params.getEffectiveEndDate());
					addedCITimeFrame.setStartTime(null);
					addedCITimeFrame.setEndTime(null);
					addedCITimeFrame.setPreview(PreviewType.ADDED);
					availCalDto.getAvailCITimeFrames().add(addedCITimeFrame);					
				} else { // It must be AvailAction.AVAILABLE_FOR_DAY...
					// Nothing more to display.  Employee is implicitly available and we've  
					// already marked existing PreviewType.REMOVED where needed.
				}
			} else {
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.invalidaction"));
			}

		}				

		return availCalDto;
	}
	
	public AvailcalViewDto updateAvailcalCDAvail(Employee employee, AvailcalUpdateParamsCDAvailDto params,
			DateTime viewStartDateTimeInUTC, DateTime viewEndDateTimeInUTC) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTimeZone timeZone = employee.getSite().getTimeZone();
		
		AvailAction actionParam = params.getAction();
		if (actionParam == null){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.missingaction"));
		}

		for (Long selectedDateInUTC : params.getSelectedDates()){
			DateTime selectedDateTimeInTZ = new DateTime( selectedDateInUTC.longValue(), timeZone);
			if ( !selectedDateTimeInTZ.equals(selectedDateTimeInTZ.withTimeAtStartOfDay())){
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
			}
			DateTime endDateTimeInTz = selectedDateTimeInTZ.plusDays(1);
			String tenantId = employee.getTenantId();
			
			// Clear out any old CD avail or unavail already on selected day.
			String employeeId = employee.getId();
			DateTime startDateTimeInUTC = new DateTime(selectedDateTimeInTZ.toInstant().getMillis()); 
			DateTime endDateTimeInUTC = new DateTime(endDateTimeInTz.toInstant().getMillis()); 			
			SimpleQuery simpleQuery = new SimpleQuery(tenantId );			
	        simpleQuery.addFilter("startDateTime >= '" + startDateTimeInUTC + "'");
	        simpleQuery.addFilter("startDateTime < '" + endDateTimeInUTC + "'");
	        simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
	    	simpleQuery.setTotalCount(true);
			ResultSet<CDAvailabilityTimeFrame> oldCdAvailabilityTimeFrames = cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
			for (CDAvailabilityTimeFrame oldCdAvailabilityTimeFrame : oldCdAvailabilityTimeFrames.getResult()){
				cdAvailabilityTimeFrameService.delete(oldCdAvailabilityTimeFrame);				
			}

			if (AvailAction.AVAILABLE_FOR_TIMEFRAMES == actionParam) {
				List<AvailcalSimpleTimeFrame> availTimeFrames = params.getTimeFrames();
				List<AvailcalSimpleTimeFrame> sortedUnavailTimeFrames = getInverseTimeFrames(availTimeFrames);
				
				for (AvailcalSimpleTimeFrame timeFrame : sortedUnavailTimeFrames){			
					long durationInMillis = timeFrame.getEndTime() - timeFrame.getStartTime();
					int durationInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
					cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId, null), employee, 
							null, null,  org.joda.time.Minutes.minutes(durationInMinutes), AvailabilityType.UnAvail, 
							new DateTime(selectedDateInUTC + timeFrame.getStartTime(), DateTimeZone.UTC), false);
				}
			} else if (AvailAction.AVAILABLE_FOR_DAY == actionParam){
				// Using Joda to calculate duration, as it will take DST into account.
				org.joda.time.Minutes durationInMinutes = org.joda.time.Minutes.minutesBetween(selectedDateTimeInTZ, endDateTimeInTz);
				cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId, null), employee, 
						null, null,  durationInMinutes, AvailabilityType.Avail, 
						new DateTime(selectedDateInUTC, DateTimeZone.UTC), false);
			} else if (AvailAction.UNAVAILABLE_FOR_DAY == actionParam){
				// Using Joda to calculate duration, as it will take DST into account.
				org.joda.time.Minutes durationInMinutes = org.joda.time.Minutes.minutesBetween(selectedDateTimeInTZ, endDateTimeInTz);
				PrimaryKey absenceTypePk = new PrimaryKey(tenantId, params.getAbsenceTypeId());
				AbsenceType absenceType = absenceTypeService.getAbsenceType(absenceTypePk);
				// Making reason empty string because Availcal UI dialog (and params) don't support it.
				cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId, null), employee, 
						absenceType, "",  durationInMinutes, AvailabilityType.UnAvail, 
						new DateTime(selectedDateInUTC, DateTimeZone.UTC), params.isPto());
			} else {
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.invalidaction"));
			}						
		}
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, employee.getTenantId(), employee.getId());
		
		AvailcalViewDto availCalDto = null;
		if (viewStartDateTimeInUTC != null  &&  viewEndDateTimeInUTC != null){
			validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
			availCalDto = this.getAvailcalView(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		}
	    return availCalDto;
	}

	public AvailcalViewDto updateAvailcalCDPref(Employee employee, AvailcalUpdateParamsCDPrefDto params,
			DateTime viewStartDateTimeInUTC, DateTime viewEndDateTimeInUTC) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTimeZone timeZone = employee.getSite().getTimeZone();
		
		PrefAction actionParam = params.getAction();
		if (actionParam == null){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.missingaction"));
		}
		
		for (Long selectedDateInUTC : params.getSelectedDates()){
			DateTime selectedDateTimeInTZ = new DateTime( selectedDateInUTC.longValue(), timeZone);
			if ( !selectedDateTimeInTZ.equals(selectedDateTimeInTZ.withTimeAtStartOfDay())){
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
			}
			DateTime endDateTimeInTz = selectedDateTimeInTZ.plusDays(1);
			String tenantId = employee.getTenantId();
			
			// Clear out any old CD availpref or unavailpref already on selected day.
			String employeeId = employee.getId();
			DateTime startDateTimeInUTC = new DateTime(selectedDateTimeInTZ.toInstant().getMillis()); 
			DateTime endDateTimeInUTC = new DateTime(endDateTimeInTz.toInstant().getMillis()); 			
			SimpleQuery simpleQuery = new SimpleQuery(tenantId );			
	        simpleQuery.addFilter("startDateTime >= '" + startDateTimeInUTC + "'");
	        simpleQuery.addFilter("startDateTime < '" + endDateTimeInUTC + "'");
	        simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.Avail + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvail + "'");
	    	simpleQuery.setTotalCount(true);
			ResultSet<CDAvailabilityTimeFrame> oldCdAvailabilityTimeFrames = cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
			for (CDAvailabilityTimeFrame oldCdAvailabilityTimeFrame : oldCdAvailabilityTimeFrames.getResult()){
				cdAvailabilityTimeFrameService.delete(oldCdAvailabilityTimeFrame);				
			}

			if (PrefAction.TIMEFRAMES == actionParam) {
				List<AvailcalSimpleTimeFrame> timeFrames = new ArrayList<AvailcalSimpleTimeFrame>();
				timeFrames.addAll(params.getPreferTimeFrames());
				timeFrames.addAll(params.getAvoidTimeFrames());
				sortTimeFrames(timeFrames);

				// prefer time frames...
				for (AvailcalSimpleTimeFrame timeFrameParam : params.getPreferTimeFrames()){					
					long durationInMillis = timeFrameParam.getEndTime() - timeFrameParam.getStartTime();
					int durationInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
					cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId, null), employee, 
							null, null,  org.joda.time.Minutes.minutes(durationInMinutes), AvailabilityType.AvailPreference, 
							new DateTime(selectedDateInUTC + timeFrameParam.getStartTime(), DateTimeZone.UTC), false);
				}
				
				// avoid time frames...
				for (AvailcalSimpleTimeFrame timeFrameParam : params.getAvoidTimeFrames()){					
					long durationInMillis = timeFrameParam.getEndTime() - timeFrameParam.getStartTime();
					int durationInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
					cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId, null), employee, 
							null, null,  org.joda.time.Minutes.minutes(durationInMinutes), AvailabilityType.UnAvailPreference, 
							new DateTime(selectedDateInUTC + timeFrameParam.getStartTime(), DateTimeZone.UTC), false);
				}
			} else if (PrefAction.PREFER_DAY == actionParam){
					org.joda.time.Minutes durationInMinutes = org.joda.time.Minutes.minutesBetween(selectedDateTimeInTZ, endDateTimeInTz);
					cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId, null), employee, 
							null, null,  durationInMinutes, AvailabilityType.AvailPreference, 
							new DateTime(selectedDateInUTC, DateTimeZone.UTC), false);
			} else if (PrefAction.AVOID_DAY == actionParam){
					org.joda.time.Minutes durationInMinutes = org.joda.time.Minutes.minutesBetween(selectedDateTimeInTZ, endDateTimeInTz);
					cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId, null), employee, 
							null, null,  durationInMinutes, AvailabilityType.UnAvailPreference, 
							new DateTime(selectedDateInUTC, DateTimeZone.UTC), false);
			} else {
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.invalidaction"));
			}						
		}
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, employee.getTenantId(), employee.getId());
		
		AvailcalViewDto availCalDto = null;
		if (viewStartDateTimeInUTC != null  &&  viewEndDateTimeInUTC != null){
			validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
			availCalDto = this.getAvailcalView(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		}
	    return availCalDto;
	}

	public AvailcalViewDto updateAvailcalCIAvail(Employee employee, AvailcalUpdateParamsCIAvailDto params,
			DateTime viewStartDateTimeInUTC, DateTime viewEndDateTimeInUTC) throws InstantiationException, IllegalAccessException, 
			NoSuchMethodException, InvocationTargetException {
		DateTimeZone timeZone = employee.getSite().getTimeZone();
		String tenantId = employee.getTenantId();

		// effective end date handling...
		Long effectiveEndDateInUTC = params.getEffectiveEndDate();
		DateTime effectiveEndDateInTZ = null;
		DateTime effectiveEndDateTimeInTZ = null;
		DateTime effectiveEndDateTimeInUTC = null; 	
		if (effectiveEndDateInUTC != null){
			effectiveEndDateInTZ = new DateTime( effectiveEndDateInUTC.longValue(), timeZone);
			if ( !effectiveEndDateInTZ.equals(effectiveEndDateInTZ.withTimeAtStartOfDay())){
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
			}		

			// Since client API will have selected end date as midnight of that day, we need
			//  to increment it to the beginning of the following day for persistence model.
			effectiveEndDateTimeInTZ = effectiveEndDateInTZ.plusDays(1);
			effectiveEndDateTimeInUTC = new DateTime(effectiveEndDateTimeInTZ.toInstant().getMillis(), DateTimeZone.UTC); 	
		}

		
		// effective start date handling...
		Long effectiveStartDateInUTC = params.getEffectiveStartDate();
		DateTime effectiveStartDateInTZ;
		if (effectiveStartDateInUTC == null){
			// DateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, DateTimeZone zone)
			effectiveStartDateInTZ = new DateTime( 2015, 1, 1, 0, 0, timeZone);			
		} else {
			effectiveStartDateInTZ = new DateTime( effectiveStartDateInUTC, timeZone);
		}
		if ( !effectiveStartDateInTZ.equals(effectiveStartDateInTZ.withTimeAtStartOfDay())){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
		}
		DateTime effectiveStartDateTimeInTZ = effectiveStartDateInTZ; // for naming consistent w/effectiveEndDateTimeInTZ
		DateTime effectiveStartDateTimeInUTC = new DateTime(effectiveStartDateTimeInTZ.toInstant().getMillis(), DateTimeZone.UTC);

		Set<DayOfWeek> selectedDaysSet = toDaysList(params.getSelectedDays()); 
		for (DayOfWeek dayOfWeek : selectedDaysSet) {
			
			// Let's make room for our new CI timeframe(s) by deleting or adjusting any existing
			// CI timeframe(s) that would otherwise have overlapping effective date periods...
			String employeeId = employee.getId();	
			SimpleQuery simpleQuery = new SimpleQuery(tenantId );

			String filter = "employee.primaryKey.id=" + "'" + employeeId  + "' " + " AND dayOfTheWeek =com.emlogis.engine.domain.DayOfWeek." + dayOfWeek.name() + " "
					+ "AND  (startDateTime = NULL OR startDateTime < '"  + viewEndDateTimeInUTC + "') "
					+ "AND (endDateTime = NULL OR endDateTime > '" + viewStartDateTimeInUTC + "')";
			simpleQuery.setFilter(filter).setTotalCount(true);
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");

			ResultSet<CIAvailabilityTimeFrame> overlappedCiAvailabilityTimeFrames = 
					ciAvailabilityTimeFrameService.findCIAvailabilityTimeFrames(simpleQuery);

			
			long newCiStart;
			if (effectiveEndDateTimeInUTC != null) {
				newCiStart = effectiveEndDateTimeInUTC.toInstant().getMillis();
			} else {
				newCiStart = 0L;  // just for overlap testing purposes
			}

			long newCiEnd;
			if (effectiveEndDateTimeInUTC != null) {
				newCiEnd = effectiveEndDateTimeInUTC.toInstant().getMillis();
			} else {
				newCiEnd = Long.MAX_VALUE;  // just for overlap testing purposes
			}
			
			for (CIAvailabilityTimeFrame overlappedCiAvailabilityTimeFrame : overlappedCiAvailabilityTimeFrames.getResult()){
				DateTime overlappedCiStartDateTime = overlappedCiAvailabilityTimeFrame.getStartDateTime();
				long overlappedCiStart;
				if (overlappedCiStartDateTime != null) {
					overlappedCiStart = overlappedCiStartDateTime.toInstant().getMillis();
				} else {
					overlappedCiStart = 0L;  // just for overlap testing purposes
				}

				DateTime overlappedCiEndDateTime = overlappedCiAvailabilityTimeFrame.getEndDateTime();
				long overlappedCiEnd;
				if (overlappedCiEndDateTime != null) {
					overlappedCiEnd = overlappedCiEndDateTime.toInstant().getMillis();
				} else {
					overlappedCiEnd = Long.MAX_VALUE;  // just for overlap testing purposes
				}

				if ( overlappedCiStart >= newCiStart && overlappedCiEnd <= newCiEnd ){
					// The case where new CI completely overlaps existing CI...
					ciAvailabilityTimeFrameService.delete(overlappedCiAvailabilityTimeFrame);	
					
				} else if ( overlappedCiStart < newCiStart && overlappedCiEnd > newCiEnd ) {
					// The case where existing CI completely overlaps new CI...
					// So... we'll effectively split the overlapped one in two, on either side of new opening.
					// (But really deleting original and creating two new ones for the same effect.)
					
					ciAvailabilityTimeFrameService.delete(overlappedCiAvailabilityTimeFrame);				

					ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
							new PrimaryKey(tenantId, null), employee,
							overlappedCiAvailabilityTimeFrame.getAbsenceType(),
							overlappedCiAvailabilityTimeFrame.getReason(),
							overlappedCiAvailabilityTimeFrame.getStartTime(), 
							overlappedCiAvailabilityTimeFrame.getDurationInMinutes(),
							overlappedCiAvailabilityTimeFrame.getAvailabilityType(),
							dayOfWeek, overlappedCiAvailabilityTimeFrame.getStartDateTime(),
							effectiveStartDateTimeInUTC);
					
					ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
							new PrimaryKey(tenantId, null), employee,
							overlappedCiAvailabilityTimeFrame.getAbsenceType(),
							overlappedCiAvailabilityTimeFrame.getReason(),
							overlappedCiAvailabilityTimeFrame.getStartTime(), 
							overlappedCiAvailabilityTimeFrame.getDurationInMinutes(),
							overlappedCiAvailabilityTimeFrame.getAvailabilityType(),
							dayOfWeek, effectiveEndDateTimeInUTC, 
							overlappedCiAvailabilityTimeFrame.getEndDateTime());
					
				} else if ( overlappedCiEnd > newCiStart ){
					// The case where end of existing CI overlaps start of new CI...
					overlappedCiAvailabilityTimeFrame.setEndDateTime(effectiveStartDateTimeInUTC);
					entityManager.merge(overlappedCiAvailabilityTimeFrame);
					
				} else if ( overlappedCiStart < newCiEnd ){
					// The case where end of new CI overlaps start of existing CI...
					overlappedCiAvailabilityTimeFrame.setStartDateTime(effectiveEndDateTimeInUTC);
					entityManager.merge(overlappedCiAvailabilityTimeFrame);
					
				} else {
					// TODO - validation exception with i18n message 
					throw new ValidationException("PLACEHOLDER ERROR MESSAGE: Overlap detection error.  Should never get here!");

				}
			}

			// Now that we've made room, let's go ahead and persist our new CI Unavail timeframe(s)...			
			if (AvailAction.AVAILABLE_FOR_TIMEFRAMES == params.getAction()) {
				List<AvailcalSimpleTimeFrame> availTimeFrames = params.getTimeFrames();
				List<AvailcalSimpleTimeFrame> sortedUnavailTimeFrames = getInverseTimeFrames(availTimeFrames);

				for (AvailcalSimpleTimeFrame timeFrameParam : sortedUnavailTimeFrames){
					long durationInMillis = timeFrameParam.getEndTime() - timeFrameParam.getStartTime();
					int durationInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
					ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
							new PrimaryKey(tenantId, null), employee, null, null, 
							LocalTime.fromMillisOfDay(timeFrameParam.getStartTime()),
							org.joda.time.Minutes.minutes(durationInMinutes), AvailabilityType.UnAvail, dayOfWeek,
							effectiveStartDateTimeInUTC, effectiveEndDateTimeInUTC);
				}
			} else if (AvailAction.AVAILABLE_FOR_DAY == params.getAction()) {
				//  We've already made room for this effective date range by clearing out any that would
				//  overlap.  Nothing more needs to be done to support AVAILABLE_FOR_DAY because the 
				//  employee is already implicitly available (and we don't support persistence of
				//  CI Avail records for that reason).
			} else if (AvailAction.UNAVAILABLE_FOR_DAY == params.getAction()) {
				ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
						new PrimaryKey(tenantId, null), employee, null, null, LocalTime.fromMillisOfDay(0),
						org.joda.time.Minutes.minutes(1440), AvailabilityType.UnAvail, dayOfWeek,
						effectiveStartDateTimeInUTC, effectiveEndDateTimeInUTC);
			} else {
				// TODO - validation exception with i18n message 
				throw new ValidationException("PLACEHOLDER ERROR MESSAGE: Invalid AvailAction.");
			}
		}
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, employee.getTenantId(), employee.getId());

		AvailcalViewDto availCalDto = null;
		if (viewStartDateTimeInUTC != null  &&  viewEndDateTimeInUTC != null){
			validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
			availCalDto = this.getAvailcalView(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		}
	    return availCalDto;
	}

	public AvailcalViewDto updateAvailcalCIPref(Employee employee, AvailcalUpdateParamsCIPrefDto params,
                                                DateTime viewStartDateTimeInUTC, DateTime viewEndDateTimeInUTC)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTimeZone timeZone = employee.getSite().getTimeZone();
		String tenantId = employee.getTenantId();

		// effective end date handling...
		Long effectiveEndDateInUTC = params.getEffectiveEndDate();
		DateTime effectiveEndDateInTZ = null;
		DateTime effectiveEndDateTimeInTZ = null;
		DateTime effectiveEndDateTimeInUTC = null; 	
		if (effectiveEndDateInUTC != null){
			effectiveEndDateInTZ = new DateTime( effectiveEndDateInUTC.longValue(), timeZone);
			if ( !effectiveEndDateInTZ.equals(effectiveEndDateInTZ.withTimeAtStartOfDay())){
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
			}		

			// Since client API will have selected end date as midnight of that day, we need
			//  to increment it to the beginning of the following day for persistence model.
			effectiveEndDateTimeInTZ = effectiveEndDateInTZ.plusDays(1);
			effectiveEndDateTimeInUTC = new DateTime(effectiveEndDateTimeInTZ.toInstant().getMillis(), DateTimeZone.UTC); 	
		}

		// effective start date handling...
		Long effectiveStartDateInUTC = params.getEffectiveStartDate();
		DateTime effectiveStartDateInTZ;
		if (effectiveStartDateInUTC == null){
			// DateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, DateTimeZone zone)
			effectiveStartDateInTZ = new DateTime( 2015, 1, 1, 0, 0, timeZone);			
		} else {
			effectiveStartDateInTZ = new DateTime( effectiveStartDateInUTC, timeZone);
		}
		if ( !effectiveStartDateInTZ.equals(effectiveStartDateInTZ.withTimeAtStartOfDay())){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
		}
		DateTime effectiveStartDateTimeInTZ = effectiveStartDateInTZ; // for naming consistent w/effectiveEndDateTimeInTZ
		DateTime effectiveStartDateTimeInUTC = new DateTime(effectiveStartDateTimeInTZ.toInstant().getMillis(), DateTimeZone.UTC);

		Set<DayOfWeek> selectedDaysSet = toDaysList(params.getSelectedDays()); 
		for (DayOfWeek dayOfWeek : selectedDaysSet) {
			
			// Let's make room for our new CI timeframe(s) by deleting or adjusting any existing
			// CI timeframe(s) that would otherwise have overlapping effective date periods...
			String employeeId = employee.getId();	
			SimpleQuery simpleQuery = new SimpleQuery(tenantId );
			
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //	*******  TODO - Shouldn't timeframes be startDateTime inclusive and endDateTime exclusive?
    //	*******	 TODO - And if so, the same should be considered in ciAvailabilityTimeFrameService.validateTimeFrame.
    //	*******	 TODO - Also need to handle NULL effective date range values, right? (start is beginning of time, end is end of time)
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			String filter = "employee.primaryKey.id=" + "'" + employeeId  + "' " + " AND dayOfTheWeek =com.emlogis.engine.domain.DayOfWeek." + dayOfWeek.name() + " "
					+ "AND  (startDateTime = NULL OR startDateTime < '"  + viewEndDateTimeInUTC + "') "
					+ "AND (endDateTime = NULL OR endDateTime > '" + viewStartDateTimeInUTC + "')";
			simpleQuery.setFilter(filter).setTotalCount(true);
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.Avail + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvail + "'");

			ResultSet<CIAvailabilityTimeFrame> overlappedCiAvailabilityTimeFrames = 
					ciAvailabilityTimeFrameService.findCIAvailabilityTimeFrames(simpleQuery);

			long newCiStart;
			if (effectiveEndDateTimeInUTC != null) {
				newCiStart = effectiveEndDateTimeInUTC.toInstant().getMillis();
			} else {
				newCiStart = 0L;  // just for overlap testing purposes
			}

			long newCiEnd;
			if (effectiveEndDateTimeInUTC != null) {
				newCiEnd = effectiveEndDateTimeInUTC.toInstant().getMillis();
			} else {
				newCiEnd = Long.MAX_VALUE;  // just for overlap testing purposes
			}
			
			for (CIAvailabilityTimeFrame overlappedCiAvailabilityTimeFrame : overlappedCiAvailabilityTimeFrames.getResult()){
				DateTime overlappedCiStartDateTime = overlappedCiAvailabilityTimeFrame.getStartDateTime();
				long overlappedCiStart;
				if (overlappedCiStartDateTime != null) {
					overlappedCiStart = overlappedCiStartDateTime.toInstant().getMillis();
				} else {
					overlappedCiStart = 0L;  // just for overlap testing purposes
				}

				DateTime overlappedCiEndDateTime = overlappedCiAvailabilityTimeFrame.getEndDateTime();
				long overlappedCiEnd;
				if (overlappedCiEndDateTime != null) {
					overlappedCiEnd = overlappedCiEndDateTime.toInstant().getMillis();
				} else {
					overlappedCiEnd = Long.MAX_VALUE;  // just for overlap testing purposes
				}

				if ( overlappedCiStart >= newCiStart && overlappedCiEnd <= newCiEnd ){
					// The case where new CI completely overlaps existing CI...
					ciAvailabilityTimeFrameService.delete(overlappedCiAvailabilityTimeFrame);	
					
				} else if ( overlappedCiStart < newCiStart && overlappedCiEnd > newCiEnd ) {
					// The case where existing CI completely overlaps new CI...
					// So... we'll effectively split the overlapped one in two, on either side of new opening.
					// (But really deleting original and creating two new ones for the same effect.)
					
					ciAvailabilityTimeFrameService.delete(overlappedCiAvailabilityTimeFrame);				

					ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
							new PrimaryKey(tenantId, null), employee,
							overlappedCiAvailabilityTimeFrame.getAbsenceType(),
							overlappedCiAvailabilityTimeFrame.getReason(),
							overlappedCiAvailabilityTimeFrame.getStartTime(), 
							overlappedCiAvailabilityTimeFrame.getDurationInMinutes(),
							overlappedCiAvailabilityTimeFrame.getAvailabilityType(),
							dayOfWeek, overlappedCiAvailabilityTimeFrame.getStartDateTime(),
							effectiveStartDateTimeInUTC);
					
					ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
							new PrimaryKey(tenantId, null), employee,
							overlappedCiAvailabilityTimeFrame.getAbsenceType(),
							overlappedCiAvailabilityTimeFrame.getReason(),
							overlappedCiAvailabilityTimeFrame.getStartTime(), 
							overlappedCiAvailabilityTimeFrame.getDurationInMinutes(),
							overlappedCiAvailabilityTimeFrame.getAvailabilityType(),
							dayOfWeek, effectiveEndDateTimeInUTC, 
							overlappedCiAvailabilityTimeFrame.getEndDateTime());
					
				} else if ( overlappedCiEnd > newCiStart ){
					// The case where end of existing CI overlaps start of new CI...
					overlappedCiAvailabilityTimeFrame.setEndDateTime(effectiveStartDateTimeInUTC);
					entityManager.merge(overlappedCiAvailabilityTimeFrame);
					
				} else if ( overlappedCiStart < newCiEnd ){
					// The case where end of new CI overalps start of existing CI...
					overlappedCiAvailabilityTimeFrame.setStartDateTime(effectiveEndDateTimeInUTC);
					entityManager.merge(overlappedCiAvailabilityTimeFrame);
					
				} else {
					// TODO - validation exception with i18n message 
					throw new ValidationException("PLACEHOLDER ERROR MESSAGE: Overlap detection error.  Should never get here!");
				}
			}
			

			// Now that we've made room, let's go ahead and persist our new CI Unavail timeframe(s)...			
			if (PrefAction.TIMEFRAMES == params.getAction()) {
				List<AvailcalSimpleTimeFrame> timeFrames = new ArrayList<AvailcalSimpleTimeFrame>();
				timeFrames.addAll(params.getPreferTimeFrames());
				timeFrames.addAll(params.getAvoidTimeFrames());
				sortTimeFrames(timeFrames);

				// prefer time frames...
				for (AvailcalSimpleTimeFrame timeFrameParam : params.getPreferTimeFrames()){					
					long durationInMillis = timeFrameParam.getEndTime() - timeFrameParam.getStartTime();
					int durationInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
					ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
							new PrimaryKey(tenantId, null), employee, null, null, 
							LocalTime.fromMillisOfDay(timeFrameParam.getStartTime()),
							org.joda.time.Minutes.minutes(durationInMinutes), AvailabilityType.AvailPreference, dayOfWeek,
							effectiveStartDateTimeInUTC, effectiveEndDateTimeInUTC);
				}
				
				// avoid time frames...
				for (AvailcalSimpleTimeFrame timeFrameParam : params.getAvoidTimeFrames()){					
					long durationInMillis = timeFrameParam.getEndTime() - timeFrameParam.getStartTime();
					int durationInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
					ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
							new PrimaryKey(tenantId, null), employee, null, null, 
							LocalTime.fromMillisOfDay(timeFrameParam.getStartTime()),
							org.joda.time.Minutes.minutes(durationInMinutes), AvailabilityType.UnAvailPreference, dayOfWeek,
							effectiveStartDateTimeInUTC, effectiveEndDateTimeInUTC);
				}				
			} else if (PrefAction.PREFER_DAY == params.getAction()) {
				ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
						new PrimaryKey(tenantId, null), employee, null, null, LocalTime.fromMillisOfDay(0),
						org.joda.time.Minutes.minutes(1440), AvailabilityType.AvailPreference, dayOfWeek,
						effectiveStartDateTimeInUTC, effectiveEndDateTimeInUTC);
			} else if (PrefAction.AVOID_DAY == params.getAction()) {
				ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(
						new PrimaryKey(tenantId, null), employee, null, null, LocalTime.fromMillisOfDay(0),
						org.joda.time.Minutes.minutes(1440), AvailabilityType.UnAvailPreference, dayOfWeek,
						effectiveStartDateTimeInUTC, effectiveEndDateTimeInUTC);
			} else {
				// TODO - validation exception with i18n message 
				throw new ValidationException("PLACEHOLDER ERROR MESSAGE: Invalid PrefAction");
			}
		}
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, employee.getTenantId(), employee.getId());

		AvailcalViewDto availCalDto = null;
		if (viewStartDateTimeInUTC != null  &&  viewEndDateTimeInUTC != null){
			validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
			availCalDto = this.getAvailcalView(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		}
	    return availCalDto;
	}
	
	public AvailcalViewDto updateAvailcalCDCopy(Employee employee, AvailcalUpdateParamsCDCopyDto params,
			DateTime viewStartDateTimeInUTC, DateTime viewEndDateTimeInUTC) throws InstantiationException, IllegalAccessException, 
			NoSuchMethodException, InvocationTargetException {
		
		DateTimeZone timeZone = employee.getSite().getTimeZone();

		Integer numDaysToAdd = null;
		if (params.getRepeat().equals(Repeat.EVERY_WEEK)){
			numDaysToAdd = 7;
		} else if (params.getRepeat().equals(Repeat.EVERY_OTHER_WEEK)){
			numDaysToAdd = 7*2;
		} else if (params.getRepeat().equals(Repeat.EVERY_THIRD_WEEK)){
			numDaysToAdd = 7*3;
		} else {
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.cdcopy.invalidrepeatepattern"));
		}

		
		// VALIDATE params.getSelectedDates().  Each must be on TZ midnight boundary.
		SortedSet<DateTime> sortedSelectedDateTimesInUTC = new TreeSet<DateTime>();
		SortedSet<DateTime> sortedSelectedDateTimesInTZ = new TreeSet<DateTime>();
		for (Long selectedDateInUTC : params.getSelectedDates()){
			DateTime selectedDateTimeInTZ = new DateTime( selectedDateInUTC.longValue(), timeZone);
			if ( !selectedDateTimeInTZ.equals(selectedDateTimeInTZ.withTimeAtStartOfDay())){
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
			}
			sortedSelectedDateTimesInUTC.add(new DateTime(selectedDateInUTC, DateTimeZone.UTC));
			sortedSelectedDateTimesInTZ.add(new DateTime(selectedDateInUTC, timeZone));
		}
		Long earliestSelectedDate = sortedSelectedDateTimesInUTC.first().getMillis();
		
		
		// VALIDATE params.getEffectiveStartDate().  Must be on TZ midnight boundary.  Must not precede earliest selected date.
		DateTime effectiveStartDateInUTC = new DateTime( params.getEffectiveStartDate(), DateTimeZone.UTC);
		DateTime effectiveStartDateInTZ = new DateTime( params.getEffectiveStartDate(), timeZone);
		if ( !effectiveStartDateInTZ.equals(effectiveStartDateInTZ.withTimeAtStartOfDay())){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
		}
		if (params.getEffectiveStartDate() < earliestSelectedDate){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.cdcopy.invalideffectivestartdate"));  
		}

		
		// VALIDATE params.getEffectiveEndDate();  Must be on TZ midnight boundary.  Must not exceed 365 days after earliest selected date.
		DateTime effectiveEndDateInUTC = new DateTime( params.getEffectiveEndDate(), DateTimeZone.UTC);
		DateTime effectiveEndDateInTZ = new DateTime( params.getEffectiveEndDate(), timeZone);
		if ( !effectiveEndDateInTZ.equals(effectiveEndDateInTZ.withTimeAtStartOfDay())){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
		}			
		if ( new DateTime(earliestSelectedDate).plusYears(1).getMillis() < params.getEffectiveEndDate() ){
			throw new ValidationException(sessionService.getMessage("validation.employee.availcal.cdcopy.invalideffectiveenddate"));
		}
		
		
		// VALIDATE that no resulting timeframe copies from any selected date will fall on the same date as any
		// resulting timeframe copies from any other selected date...
		List<DateTime> dateTimesUsed = new ArrayList<DateTime>();
		for (DateTime selectedDateTimeInTZ : sortedSelectedDateTimesInTZ){
			DateTime proposedCopyDateTime = new DateTime(selectedDateTimeInTZ);
			do {
				proposedCopyDateTime = proposedCopyDateTime.plusDays(numDaysToAdd);  // incrementing for do-loop
				if (proposedCopyDateTime.getMillis() >= effectiveStartDateInTZ.getMillis()  &&
						proposedCopyDateTime.getMillis() > effectiveEndDateInTZ.plusDays(1).getMillis()){
					if (dateTimesUsed.contains(proposedCopyDateTime)){
						throw new ValidationException(sessionService.getMessage("validation.employee.availcal.cdcopy.conflictingselecteddates")); 
					} else {
						dateTimesUsed.add(proposedCopyDateTime);
					}
				}
			} while (proposedCopyDateTime.getMillis() <= params.getEffectiveEndDate());
		}

		// Now finally make the copies...
		String tenantId = employee.getTenantId();
		String employeeId = employee.getId();
		for (Long selectedDateInUTC : params.getSelectedDates()){
			DateTime selectedDateTimeInTZ = new DateTime( selectedDateInUTC.longValue(), timeZone);
			if ( !selectedDateTimeInTZ.equals(selectedDateTimeInTZ.withTimeAtStartOfDay())){
				throw new ValidationException(sessionService.getMessage("validation.employee.availcal.datemidnight"));
			}
			DateTime selectedDateTimeInUTC = new DateTime( selectedDateInUTC.longValue());

			SimpleQuery simpleQuery;		
			DateTime queryStartDateTimeInUTC = selectedDateTimeInUTC;
			DateTime queryEndDateTimeInUTC = selectedDateTimeInUTC.plusDays(1);
			
			Collection<CDAvailabilityTimeFrame> selectedDateCDTimeFrames = new ArrayList<CDAvailabilityTimeFrame>();				
			if (params.isAvailability()) {
				// Get CD Avail/UnAvail timeframes...
				simpleQuery = new SimpleQuery(tenantId );			
			    simpleQuery.addFilter("startDateTime >= '" + queryStartDateTimeInUTC + "'");
			    simpleQuery.addFilter("startDateTime < '" + queryEndDateTimeInUTC + "'");
			    simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
				simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
				simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
				simpleQuery.setTotalCount(true);
				ResultSet<CDAvailabilityTimeFrame> cdAvailResultSet = cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
				selectedDateCDTimeFrames.addAll(cdAvailResultSet.getResult());				
			}
			
			if (params.isPreference()) {
				// Get CD AvailPreference/UnAvailPreference timeframes...		
				simpleQuery = new SimpleQuery(tenantId );			
			    simpleQuery.addFilter("startDateTime >= '" + queryStartDateTimeInUTC + "'");
			    simpleQuery.addFilter("startDateTime < '" + queryEndDateTimeInUTC + "'");
			    simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
				simpleQuery.addFilter("availabilityType != '" + AvailabilityType.Avail + "'");
				simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvail + "'");
				simpleQuery.setTotalCount(true);
				ResultSet<CDAvailabilityTimeFrame> cdPrefResultSet = cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
				selectedDateCDTimeFrames.addAll(cdPrefResultSet.getResult());				
			}

			for (CDAvailabilityTimeFrame selectedDateCDTimeFrame : selectedDateCDTimeFrames){
				// Only copy forward and only persist what falls in effective date range
				DateTime proposedCopyDateTimeInTZ = new DateTime(selectedDateCDTimeFrame.getStartDateTime(), timeZone);
				do {
					proposedCopyDateTimeInTZ = proposedCopyDateTimeInTZ.plusDays(numDaysToAdd);  // incrementing for do-loop
					if (proposedCopyDateTimeInTZ.getMillis() >= effectiveStartDateInUTC.getMillis()  &&  
							proposedCopyDateTimeInTZ.getMillis() <= effectiveEndDateInUTC.getMillis()){
						queryStartDateTimeInUTC = new DateTime(proposedCopyDateTimeInTZ.toInstant(), DateTimeZone.UTC) ; 
						queryEndDateTimeInUTC = queryStartDateTimeInUTC.plusDays(1); 	
						
						// Clear out any old CD avail or unavail already on selected day.
						if (params.isAvailability()) {
							simpleQuery = new SimpleQuery(tenantId );			
					        simpleQuery.addFilter("startDateTime >= '" + queryStartDateTimeInUTC + "'");
					        simpleQuery.addFilter("startDateTime < '" + queryEndDateTimeInUTC + "'");
					        simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
				        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
				        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
					    	simpleQuery.setTotalCount(true);
							ResultSet<CDAvailabilityTimeFrame> oldCdAvailabilityTimeFrames =
                                    cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
							for (CDAvailabilityTimeFrame oldCdAvailabilityTimeFrame : oldCdAvailabilityTimeFrames.getResult()){
								cdAvailabilityTimeFrameService.delete(oldCdAvailabilityTimeFrame);				
							}							
						}
						
						// Clear out any old CD availPref or unavailPref already on selected day.
						if (params.isPreference()) {
							simpleQuery = new SimpleQuery(tenantId );			
					        simpleQuery.addFilter("startDateTime >= '" + queryStartDateTimeInUTC + "'");
					        simpleQuery.addFilter("startDateTime < '" + queryEndDateTimeInUTC + "'");
					        simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
				        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
				        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
					    	simpleQuery.setTotalCount(true);
							ResultSet<CDAvailabilityTimeFrame> oldCdAvailabilityTimeFrames =
                                    cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
							for (CDAvailabilityTimeFrame oldCdAvailabilityTimeFrame : oldCdAvailabilityTimeFrames.getResult()) {
								cdAvailabilityTimeFrameService.delete(oldCdAvailabilityTimeFrame);				
							}							
						}
						
						cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(
								new PrimaryKey(tenantId, null), 
								selectedDateCDTimeFrame.getEmployee(),
								selectedDateCDTimeFrame.getAbsenceType(), 
								selectedDateCDTimeFrame.getReason(), 
								selectedDateCDTimeFrame.getDurationInMinutes(), 
								selectedDateCDTimeFrame.getAvailabilityType(), 
								new DateTime(proposedCopyDateTimeInTZ.toInstant(), DateTimeZone.UTC), 
								selectedDateCDTimeFrame.getIsPTO());
						
					}
				} while (proposedCopyDateTimeInTZ.getMillis() <= params.getEffectiveEndDate());
			}
		}
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, employee.getTenantId(), employee.getId());


		AvailcalViewDto availCalDto = null;
		if (viewStartDateTimeInUTC != null  &&  viewEndDateTimeInUTC != null){
			validateWeekBoundaryDates(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
			availCalDto = this.getAvailcalView(employee, viewStartDateTimeInUTC, viewEndDateTimeInUTC);
		}
	    return availCalDto;
	}
	
	public void updateAvailcalWeekdayRotation(Employee employee, AvailcalUpdateParamsWeekdayRotationDto params) {
        Set<EmployeeContract> employeeContracts = employee.getEmployeeContracts();
        Set<ContractLine> contractLines = new HashSet<>();
        EmployeeContract employeeContract = null;
        for (EmployeeContract contract : employeeContracts) {
            if (employeeContract == null) {
                employeeContract = contract;
            }
            contractLines.addAll(contract.getContractLines());
        }

        String tenantId = employee.getTenantId();

        if (employeeContract == null) {
			employeeContract = contractService.createEmployeeContract(new PrimaryKey(tenantId));

			employeeContract.setEmployee(employee);
			contractService.updateContract(employeeContract);
        }

    	WeekdayRotationPatternCL weekdayRotationPatternCL = null;
    	for (ContractLine contractLine : contractLines) {
			if (contractLine instanceof WeekdayRotationPatternCL) {
        		weekdayRotationPatternCL = (WeekdayRotationPatternCL) contractLine;
        		if (weekdayRotationPatternCL.getDayOfWeek().equals(params.getDayOfWeek())) {
        			break;
        		} else {
        			weekdayRotationPatternCL = null;
        		}
    		}
    	}
        
        if (weekdayRotationPatternCL == null) {
        	if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.NONE)) {
        		return;  // Nothing needs to be done!
        	}
        	
        	weekdayRotationPatternCL = new WeekdayRotationPatternCL(new PrimaryKey(tenantId));
        	weekdayRotationPatternCL.setContractLineType(ContractLineType.CUSTOM);
        	weekdayRotationPatternCL.setDayOfWeek(params.getDayOfWeek());
        	weekdayRotationPatternCL.setWeight(-1);
        	weekdayRotationPatternCL.setRotationType(RotationPatternType.DAYS_OFF_PATTERN);
        	if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.EVERY_OTHER)) {
            	weekdayRotationPatternCL.setNumberOfDays(1);
            	weekdayRotationPatternCL.setOutOfTotalDays(14);        		
        	} else if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.EVERY_THIRD)) {
            	weekdayRotationPatternCL.setNumberOfDays(1);
            	weekdayRotationPatternCL.setOutOfTotalDays(21);        		
        	} else if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.TWO_OF_EVERY_FOUR)) {
            	weekdayRotationPatternCL.setNumberOfDays(2);
            	weekdayRotationPatternCL.setOutOfTotalDays(28);        		
        	}
        	weekdayRotationPatternCL.setContract(employeeContract);
        	
			contractLineService.createContractLine(weekdayRotationPatternCL);
			contractService.updateContract(employeeContract);
        } else {
        	if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.NONE)) {
                employeeContract.getContractLines().remove(weekdayRotationPatternCL);
				contractLineService.delete(weekdayRotationPatternCL);
                contractService.updateContract(employeeContract);
        	} else {
            	if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.EVERY_OTHER)) {
                	weekdayRotationPatternCL.setNumberOfDays(1);
                	weekdayRotationPatternCL.setOutOfTotalDays(14);        		
            	} else if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.EVERY_THIRD)) {
                	weekdayRotationPatternCL.setNumberOfDays(1);
                	weekdayRotationPatternCL.setOutOfTotalDays(21);        		
            	} else if (params.getWeekdayRotationValue().equals(WeekdayRotationValue.TWO_OF_EVERY_FOUR)) {
                	weekdayRotationPatternCL.setNumberOfDays(2);
                	weekdayRotationPatternCL.setOutOfTotalDays(28);        		
            	}

				contractLineService.updateContractLine(weekdayRotationPatternCL);
        	}
        }
	}

	public void updateAvailcalWeekendCoupling(Employee employee, boolean coupleWeekends) {
        Set<EmployeeContract> employeeContracts = employee.getEmployeeContracts();
        Set<ContractLine> contractLines = new HashSet<>();
        EmployeeContract employeeContract = null;
        for (EmployeeContract contract : employeeContracts) {
            if (employeeContract == null) {
                employeeContract = contract;
            }
            contractLines.addAll(contract.getContractLines());
        }

        String tenantId = employee.getTenantId();

        if (employeeContract == null) {
			employeeContract = contractService.createEmployeeContract(new PrimaryKey(tenantId));
			employeeContract.setEmployee(employee);
			contractService.updateContract(employeeContract);
        }

    	ContractLineType coupleWeekendsClType = ContractLineType.COMPLETE_WEEKENDS;
        BooleanCL coupleWeekendsCL = null;
    	for (ContractLine contractLine : contractLines) {
			if (contractLine.getContractLineType().equals(coupleWeekendsClType)) {
        		coupleWeekendsCL = (BooleanCL) contractLine;
    			break;
    		}
    	}
        
        if (coupleWeekendsCL == null) {
        	coupleWeekendsCL = new BooleanCL( new PrimaryKey(tenantId ));
        	coupleWeekendsCL.setContractLineType(coupleWeekendsClType);
        	coupleWeekendsCL.setEnabled(coupleWeekends);
        	coupleWeekendsCL.setContract(employeeContract);
        	coupleWeekendsCL.setWeight(-1);
			contractLineService.createContractLine(coupleWeekendsCL);
			contractService.updateContract(employeeContract);
        } else {
        	coupleWeekendsCL.setEnabled(coupleWeekends);
			contractLineService.updateContractLine(coupleWeekendsCL);
        }
	}

    public Collection<Object[]> getSubmittedOpenShiftsRequestInfo(PrimaryKey primaryKey, long startDate, long endDate) {
        String sql =
                "SELECT r.id, r.requestType, r.requestStatus, r.submitterShiftId, r.requestDate " +
                "  FROM WorkflowRequest r " +
                " WHERE r.fk_initiator_tenant_id = :tenantId " +
                "   AND r.submitterId = :employeeId " +
                "   AND r.requestDate BETWEEN :startDate AND :endDate ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", primaryKey.getTenantId());
        query.setParameter("employeeId", primaryKey.getId());
        query.setParameter("startDate", new Timestamp(startDate));
        query.setParameter("endDate", new Timestamp(endDate));

        return query.getResultList();
    }

    public Collection<Object[]> getPeerWipSwapRequestInfo(PrimaryKey primaryKey, long startDate, long endDate) {
        String sql =
                "SELECT p.fk_wfl_process_instance_id, p.peerStatus, p.peerShiftId, p.submitterShiftId, " +
                "       p.submitterShiftStartDateTime, p.submitterShiftEndDateTime, p.requestType " +
                "  FROM WorkflowRequestPeer p " +
                " WHERE p.fk_recipient_tenant_id = :tenantId " +
                "      AND p.fk_recipient_employee_id = :employeeId " +
                "      AND (   p.requestType = 'SHIFT_SWAP_REQUEST' " +
                "                   AND p.peerShiftStartDateTime BETWEEN :startDate AND :endDate " +
                "           OR p.requestType = 'WIP_REQUEST' " +
                "                   AND p.submitterShiftStartDateTime BETWEEN :startDate AND :endDate)";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", primaryKey.getTenantId());
        query.setParameter("employeeId", primaryKey.getId());
        query.setParameter("startDate", new Timestamp(startDate));
        query.setParameter("endDate", new Timestamp(endDate));

        return query.getResultList();
    }

    private void updateHomeTeam(PrimaryKey employeePrimaryKey, PrimaryKey homeEmployeeTeamPrimaryKey) {
        String sql = "UPDATE EmployeeTeam SET isHomeTeam = false WHERE tenantId = :tenantId " +
                     "   AND employeeId = :employeeId AND id <> :id";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", employeePrimaryKey.getTenantId());
        query.setParameter("employeeId", employeePrimaryKey.getId());
        query.setParameter("id", homeEmployeeTeamPrimaryKey.getId());

        query.executeUpdate();
    }

    private String getSiteId(PrimaryKey teamPrimaryKey) {
        String sql = "SELECT DISTINCT s.id FROM Team t " +
                     "  LEFT JOIN AOMRelationship r ON r.dst_id = t.id AND t.tenantId = r.dst_tenantId " +
                     "  LEFT JOIN Site s ON r.src_id = s.id AND s.tenantId = r.src_tenantId " +
                "      AND " + QueryPattern.NOT_DELETED.val("s") +
                " WHERE t.id = :teamId AND t.tenantId = :tenantId ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", teamPrimaryKey.getTenantId());
        query.setParameter("teamId", teamPrimaryKey.getId());

        return (String) query.getSingleResult();
    }

    private void addEmployeeSite(String employeeId, String siteId, String tenantId) {
        String sql =
                "SELECT count(*) FROM Site_Employee WHERE site_id = :siteId AND employee_id = :employeeId " +
                "                                     AND site_tenantId = :tenantId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setParameter("employeeId", employeeId);
        query.setParameter("siteId", siteId);

        int count = ((Number) query.getSingleResult()).intValue();
        if (count == 0) {
            String insertSql =
                    "INSERT INTO Site_Employee (site_id, site_tenantId, employee_id, employee_tenantId) " +
                    "                   VALUES (:siteId, :tenantId, :employeeId, :tenantId) ";
            Query insertQuery = entityManager.createNativeQuery(insertSql);
            insertQuery.setParameter("tenantId", tenantId);
            insertQuery.setParameter("employeeId", employeeId);
            insertQuery.setParameter("siteId", siteId);

            insertQuery.executeUpdate();
        }
    }


    /**
     * Validates that the calendar view being requested makes sense, which includes:
     *   - Ensure that the FIRST calendar date precedes the LAST calendar date
     *   - Ensure that LAST calendar date is the last day of a site-specific week 
     *   - Ensure that FIRST calendar date is the first day of a site-specific week
     *   - Ensure that the LAST calendar date is represented as start of day in site-specific TZ
     *   - Ensure that the FIRST calendar date is represented as start of day in site-specific TZ 
     *   
     * @param employee
     * @param firstCalendarDateInUTC  (the UTC instant for site TZ midnight of the FIRST calendar DATE for view)
     * @param lastCalendarDateInUTC  (the UTC instant for site TZ midnight of the LAST calendar DATE for view)
     */
    private void validateWeekBoundaryDates(Employee employee,
			DateTime firstCalendarDateInUTC, DateTime lastCalendarDateInUTC) {
		// Validate that view range falls on site TZ week boundaries (1wk, 2wks ... 5wks, etc)
		DateTimeZone timeZone = employee.getSite().getTimeZone();
			
		DayOfWeek siteWeekDay1 = employee.getSite().getFirstDayOfWeek();
		DayOfWeek siteWeekDay2 = siteWeekDay1.determineNextDayOfWeek();
		DayOfWeek siteWeekDay3 = siteWeekDay2.determineNextDayOfWeek();
		DayOfWeek siteWeekDay4 = siteWeekDay3.determineNextDayOfWeek();
		DayOfWeek siteWeekDay5 = siteWeekDay4.determineNextDayOfWeek();
		DayOfWeek siteWeekDay6 = siteWeekDay5.determineNextDayOfWeek();
		DayOfWeek siteWeekDay7 = siteWeekDay6.determineNextDayOfWeek();
				
		DateTime viewStartDateTimeInTz = new DateTime(firstCalendarDateInUTC.toInstant(), timeZone);
		DateTime viewEndDateTimeInTz = new DateTime(lastCalendarDateInUTC.toInstant(), timeZone);
		if (!viewStartDateTimeInTz.equals(viewStartDateTimeInTz.withTimeAtStartOfDay())  
				|| viewStartDateTimeInTz.getDayOfWeek() != siteWeekDay1.getJodaValue()) {
			// TODO - i18n ValidationException for starting at tz beginning (midnight) on site's first day of week
			throw new ValidationException("View range start datetime must be start of site's first day of week in site timezone.");
		}
		if (!viewEndDateTimeInTz.equals(viewEndDateTimeInTz.withTimeAtStartOfDay()) 
				|| viewEndDateTimeInTz.getDayOfWeek() != siteWeekDay7.getJodaValue()) {
			// TODO - i18n ValidationException for ending at tz beginning midnight on site's last day of week
			throw new ValidationException("View range end datetime must be start of site's last day of week in site timezone.");
		}
		if (!viewStartDateTimeInTz.isBefore(viewEndDateTimeInTz)) {
			// TODO - i18n ValidationException for start datetime preceding end datetime
			throw new ValidationException("View range start datetime must be before end datetime");			
		}
	}

	private void populateAvailCalDtoAvailability(Employee employee, DateTime viewStartDateTimeInUTC,
                                                 DateTime viewEndDateTimeInUTC, AvailcalViewDto availCalDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
		///////////////////////////////////////////////////////////////////////
		//  Prep
		///////////////////////////////////////////////////////////////////////
		String employeeId = employee.getId();
		String tenantId = employee.getTenantId();
		DateTimeZone timeZone = employee.getSite().getTimeZone();
		DateTime viewStartDateTimeInTZ = new DateTime(viewStartDateTimeInUTC.toInstant(), timeZone);
		DateTime viewEndDateTimeInTZ = new DateTime(viewEndDateTimeInUTC.toInstant(), timeZone);
		// Let's query the timeframes required to build out display data...
		DateTime queryStartDateTimeInUTC = viewStartDateTimeInUTC;
		DateTime queryEndDateTimeInUTC = viewEndDateTimeInUTC.plusDays(1);
		///////////////////////////////////////////////////////////////////////
		//  CD Availability
		///////////////////////////////////////////////////////////////////////
		
		// Get CD Avail/UnAvail timeframes...
        SimpleQuery simpleQuery = new SimpleQuery(tenantId );
	    simpleQuery.addFilter("startDateTime >= '" + queryStartDateTimeInUTC + "'");
	    simpleQuery.addFilter("startDateTime < '" + queryEndDateTimeInUTC + "'");
	    simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
		simpleQuery.setTotalCount(true);
		ResultSet<CDAvailabilityTimeFrame> cdAvailResultSet =
                cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
		Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames = cdAvailResultSet.getResult();

		// Group our CDAvails by date using a map
		Map<DateTime, List<CDAvailabilityTimeFrame>> cdAvailTimeFramesMap = new HashMap<>();
		for (CDAvailabilityTimeFrame cdAvailTimeFrame : cdAvailTimeFrames) {
			DateTime startDateTimeInTZ = new DateTime(cdAvailTimeFrame.getStartDateTime().toInstant(), timeZone);
			DateTime dayDateTimeInTZ = new DateTime(startDateTimeInTZ.withTimeAtStartOfDay());
	
			if (!cdAvailTimeFramesMap.containsKey(dayDateTimeInTZ)) {
				cdAvailTimeFramesMap.put(dayDateTimeInTZ, new ArrayList<CDAvailabilityTimeFrame>());
			}
			cdAvailTimeFramesMap.get(dayDateTimeInTZ).add(cdAvailTimeFrame);
		}

		// For each date in the map...
		for (DateTime dayDateTime : cdAvailTimeFramesMap.keySet()) {
			List<CDAvailabilityTimeFrame> timeFramesListForDate = cdAvailTimeFramesMap.get(dayDateTime);			
			List<AvailcalSimpleTimeFrame> unavailTimeFramesListForDate = new ArrayList<>();
			
			// Transform our Unavail CDAvailabilityTimeFrames into AvailcalSimpleTimeFrames..
			for (CDAvailabilityTimeFrame timeFrameForDate : timeFramesListForDate){
				DateTime startDateTimeInTZ = new DateTime( timeFrameForDate.getStartDateTime().toInstant(), timeZone);
				DateTime endDateTimeInTZ = startDateTimeInTZ.plusMinutes(
                        timeFrameForDate.getDurationInMinutes().getMinutes());
				if (endDateTimeInTZ.equals(startDateTimeInTZ.plusDays(1))) {
					// There can be only one and it must be for all day, so let's make sure.  And
					// if everything looks okay, let's just go ahead and add it to the view dto...
					if (startDateTimeInTZ.equals(startDateTimeInTZ.withTimeAtStartOfDay())
							&& endDateTimeInTZ.equals(startDateTimeInTZ.plusDays(1))
							&& timeFramesListForDate.size() == 1) {  // all day long
						AvailCDTimeFrame availCdTimeFrame = new AvailCDTimeFrame();
						availCdTimeFrame.setStartDateTime(startDateTimeInTZ.toInstant().getMillis());
						availCdTimeFrame.setEndDateTime(endDateTimeInTZ.toInstant().getMillis());

						if (timeFrameForDate.getAvailabilityType().equals(AvailabilityType.UnAvail)) {
							if (timeFrameForDate.getIsPTO()) {
								availCdTimeFrame.setPTO(true);								
							}
							if (timeFrameForDate.getAbsenceType() != null) {
								availCdTimeFrame.setAbsenceTypeName(timeFrameForDate.getAbsenceType().getName());								
							}
							availCdTimeFrame.setAvailType(AvailType.DAY_OFF);
							availCalDto.getAvailCDTimeFrames().add(availCdTimeFrame);
						} else {
							availCdTimeFrame.setAvailType(AvailType.AVAIL);
							
							// TODO - Consider whether to include CD Avails for display.
							//        Technically unnecessary since employee is implicitly available anyway!
							//        We'll exclude it for now.
//							availCalDto.getAvailCDTimeFrames().add(availCdTimeFrame);
						}
					} else {
						// TODO - i18n ValidationException for a bad data situation
						throw new ValidationException("PLACEHOLDER ERROR MESSAGE:  Bad data found!");
					}
				} else {  
					// Must have one or more unavailability timeframes, so let's transform it to 
					// an AvailcalSimpleTimeFrame for the list that will be transformed to the
					// inverse timeframes...
					AvailcalSimpleTimeFrame simpleTimeFrame = new AvailcalSimpleTimeFrame();
										
					int startMillisOfDay = startDateTimeInTZ.getMillisOfDay();
					int endMillisOfDay = startMillisOfDay + 
							(int) TimeUnit.MINUTES.toMillis(timeFrameForDate.getDurationInMinutes().getMinutes());
					
					simpleTimeFrame.setStartTime(startMillisOfDay);
					simpleTimeFrame.setEndTime(endMillisOfDay);
					unavailTimeFramesListForDate.add(simpleTimeFrame);					
				}				
			}
			
			List<AvailcalSimpleTimeFrame> availTimeFramesListForDate = getInverseTimeFrames(unavailTimeFramesListForDate);
			for (AvailcalSimpleTimeFrame availTimeFrame : availTimeFramesListForDate) {
				AvailCDTimeFrame availCdTimeFrame = new AvailCDTimeFrame();
								
				DateTime startDateTimeInTZ = dayDateTime.plusMillis((int) availTimeFrame.getStartTime());
				DateTime endDateTimeInTZ = dayDateTime.plusMillis((int) availTimeFrame.getEndTime());
				DateTime startDateTimeInUTC = new DateTime(startDateTimeInTZ.toInstant(), DateTimeZone.UTC);
				DateTime endDateTimeInUTC = new DateTime(endDateTimeInTZ.toInstant(), DateTimeZone.UTC);
				
				availCdTimeFrame.setStartDateTime(startDateTimeInUTC.getMillis());
				availCdTimeFrame.setEndDateTime(endDateTimeInUTC.getMillis());
				availCdTimeFrame.setAvailType(AvailType.AVAIL);
				availCalDto.getAvailCDTimeFrames().add(availCdTimeFrame);
			}
		}
		
		///////////////////////////////////////////////////////////////////////
		//  CI Availability
		///////////////////////////////////////////////////////////////////////
	
		// TODO - Add CI Avail/Unavail to view dto...
		// Get CI Avail/UnAvail timeframes...
		simpleQuery = new SimpleQuery(tenantId);
		String ciAvailfilter = "employee.primaryKey.id=" + "'" + employeeId  + "' " 
				+ "AND  (startDateTime = NULL OR startDateTime < '"  + queryEndDateTimeInUTC + "') "
				+ "AND (endDateTime = NULL OR endDateTime > '" + queryStartDateTimeInUTC + "')";
		simpleQuery.setFilter(ciAvailfilter).setTotalCount(true);
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
		ResultSet<CIAvailabilityTimeFrame> ciAvailResultSet = 
				ciAvailabilityTimeFrameService.findCIAvailabilityTimeFrames(simpleQuery);
		Collection<CIAvailabilityTimeFrame> ciAvailabilityTimeFrames = ciAvailResultSet.getResult();

		// Let's gather together all the CI Unavail that have same weekday and effective date range ...
		Map<CIGroupKey, List<CIAvailabilityTimeFrame>> ciUnavailTimeFramesMap = new HashMap<>();
		for (CIAvailabilityTimeFrame ciUnavailableTimeFrame : ciAvailabilityTimeFrames){
			CIGroupKey key = new CIGroupKey();
	
			if (ciUnavailableTimeFrame.getStartDateTime() != null) {
				key.setStartDateTime(ciUnavailableTimeFrame.getStartDateTime());
			} else {
				key.setStartDateTime(new DateTime(0L));
			}
	
			if (ciUnavailableTimeFrame.getEndDateTime() != null) {
				key.setEndDateTime(ciUnavailableTimeFrame.getEndDateTime());
			} else {
				key.setEndDateTime(new DateTime(Long.MAX_VALUE));
			}
	
			key.setDayOfTheWeek(ciUnavailableTimeFrame.getDayOfTheWeek());
			key.setAvailabilityType(ciUnavailableTimeFrame.getAvailabilityType());
	
			if (!ciUnavailTimeFramesMap.containsKey(key)){
				ciUnavailTimeFramesMap.put(key, new ArrayList<CIAvailabilityTimeFrame>());
			}
			ciUnavailTimeFramesMap.get(key).add(ciUnavailableTimeFrame);
		}

		// Now that everything is grouped, let's work our way through the groups ...
		Set<CIGroupKey> ciAvailabilityKeySet = ciUnavailTimeFramesMap.keySet();
		for (CIGroupKey key : ciAvailabilityKeySet){
			List<CIAvailabilityTimeFrame> unavailTimeFrames = ciUnavailTimeFramesMap.get(key);

			// First a little date indexing for later use with this group ...
			DateTime effectiveDateRangeStartInTZ;
			if (key.getStartDateTime() != null) {
				effectiveDateRangeStartInTZ = new DateTime(key.getStartDateTime().toInstant(), timeZone);
			} else {
				effectiveDateRangeStartInTZ = viewStartDateTimeInTZ;
			}
	
			DateTime effectiveDateRangeEndInTZ;
			if (key.getEndDateTime() != null) {
				effectiveDateRangeEndInTZ = new DateTime(key.getEndDateTime().toInstant(), timeZone) ;
			} else {
				effectiveDateRangeEndInTZ = viewEndDateTimeInTZ;
			}
	
			DateTime effectiveViewDateRangeStartInTZ;
			if (viewStartDateTimeInTZ.isBefore(effectiveDateRangeStartInTZ)){
				effectiveViewDateRangeStartInTZ = effectiveDateRangeStartInTZ;
			} else {
				effectiveViewDateRangeStartInTZ = viewStartDateTimeInTZ;
			}
	
			DateTime effectiveViewDateRangeEndInTZ;
			if (viewEndDateTimeInTZ.isBefore(effectiveDateRangeEndInTZ)){
				effectiveViewDateRangeEndInTZ = viewEndDateTimeInTZ;
			} else {
				effectiveViewDateRangeEndInTZ = effectiveDateRangeEndInTZ;
			}

			// Now we can actually work through the timeframe(s) for the group ...
			if (unavailTimeFrames.size() == 1 &&
					unavailTimeFrames.get(0).getStartTime().getMillisOfDay() == 0 &&
					unavailTimeFrames.get(0).getDurationInMinutes().getMinutes() == 1440) {
				// Just this one all-day timeframe, so it will become an AvailType.DAY_OFF AvailCITimeFrame ...
				CIAvailabilityTimeFrame unavailTimeFrame = unavailTimeFrames.get(0);
				AvailCITimeFrame availCiTimeFrame = new AvailCITimeFrame();
	
				int dayOfWeekJodaValue = key.getDayOfTheWeek().getJodaValue();
				DateTime indexDateTime = effectiveViewDateRangeStartInTZ.withTimeAtStartOfDay();
				while (!indexDateTime.isAfter(effectiveViewDateRangeEndInTZ)) {
					if (indexDateTime.getDayOfWeek() == dayOfWeekJodaValue
                            && cdAvailTimeFramesMap.get(indexDateTime) == null) {
						TimeFrameInstance timeFrameInstance = new TimeFrameInstance();
						timeFrameInstance.setStartDateTime(indexDateTime.toInstant().getMillis());
						availCiTimeFrame.getTimeFrameInstances().add(timeFrameInstance);
					}
					indexDateTime = indexDateTime.plusDays(1);
				}

				// Populate rest of the AvailCITimeFrame attributes ...
				availCiTimeFrame.setDayOfTheWeek(key.getDayOfTheWeek());
				availCiTimeFrame.setAvailType(AvailType.DAY_OFF);
				availCiTimeFrame.setStartTime(null);
				availCiTimeFrame.setEndTime(null);
				if (key.getStartDateTime().equals(new DateTime(0L))) {
					availCiTimeFrame.setEffectiveDateRangeStart(null);
				} else {
					availCiTimeFrame.setEffectiveDateRangeStart(key.getStartDateTime().getMillis());
				}
				if (key.getEndDateTime().equals( new DateTime(Long.MAX_VALUE))) {
					availCiTimeFrame.setEffectiveDateRangeEnd(null);
				} else {
					availCiTimeFrame.setEffectiveDateRangeEnd(key.getEndDateTime().getMillis());
				}

				// Add the AvailCITimeFrame to the view DTO...
				availCalDto.getAvailCITimeFrames().add(availCiTimeFrame);
			} else {
				// Apparently one or more partial-day UNAVAIL timeframes, so they will be used to
				// derive AvailType.AVAIL AvailCITimeFrames for display ... 
	
				// So first we need to transform them to a list of their (inverse) AVAIL timeframes...
				List<AvailcalSimpleTimeFrame> unavailSimpleTimeFramesList = new ArrayList<>();
				for (CIAvailabilityTimeFrame unavailTimeFrame : unavailTimeFrames){
					int startTime = unavailTimeFrame.getStartTime().getMillisOfDay();
					int endTime = startTime + (int) TimeUnit.MINUTES.toMillis(
                            unavailTimeFrame.getDurationInMinutes().getMinutes());
	
					AvailcalSimpleTimeFrame unavailSimpleTimeFrame = new AvailcalSimpleTimeFrame();
					unavailSimpleTimeFrame.setStartTime(startTime);
					unavailSimpleTimeFrame.setEndTime(endTime);
					unavailSimpleTimeFramesList.add(unavailSimpleTimeFrame);
				}
				List<AvailcalSimpleTimeFrame> availSimpleTimeFramesList =
                        this.getInverseTimeFrames(unavailSimpleTimeFramesList);

				// Now that we've transformed to AVAIL timeframes, each can be made into an AvailCITimeFrames with
				// it's requisite list of any applicable timeframe instances for display...
				for (AvailcalSimpleTimeFrame availSimpleTimeFrame : availSimpleTimeFramesList){
					AvailCITimeFrame availCiTimeFrame = new AvailCITimeFrame();
	
					int dayOfWeekJodaValue = key.getDayOfTheWeek().getJodaValue();
					DateTime indexDateTime = effectiveViewDateRangeStartInTZ.withTimeAtStartOfDay(); 
					while (!indexDateTime.isAfter(effectiveViewDateRangeEndInTZ)) {
						if (indexDateTime.getDayOfWeek() == dayOfWeekJodaValue
                                && cdAvailTimeFramesMap.get(indexDateTime) == null) {
							TimeFrameInstance timeFrameInstance = new TimeFrameInstance();
							timeFrameInstance.setStartDateTime(indexDateTime.toInstant().getMillis() +
                                    availSimpleTimeFrame.getStartTime());
							timeFrameInstance.setEndDateTime(indexDateTime.toInstant().getMillis() +
                                    availSimpleTimeFrame.getEndTime());
							availCiTimeFrame.getTimeFrameInstances().add(timeFrameInstance);
						}
						indexDateTime = indexDateTime.plusDays(1);
					}
	
					// Populate rest of the AvailCITimeFrame attributes ...
					availCiTimeFrame.setDayOfTheWeek(key.getDayOfTheWeek());
					availCiTimeFrame.setAvailType(AvailType.AVAIL);
					availCiTimeFrame.setStartTime(availSimpleTimeFrame.getStartTime());
					availCiTimeFrame.setEndTime(availSimpleTimeFrame.getEndTime());
					if (key.getStartDateTime().equals(new DateTime(0L))) {
						availCiTimeFrame.setEffectiveDateRangeStart(null);
					} else {
						availCiTimeFrame.setEffectiveDateRangeStart(key.getStartDateTime().getMillis());
					}
					if (key.getEndDateTime().equals( new DateTime(Long.MAX_VALUE))) {
						availCiTimeFrame.setEffectiveDateRangeEnd(null);
					} else {
						availCiTimeFrame.setEffectiveDateRangeEnd(key.getEndDateTime().getMillis());
					}

					// Add the AvailCITimeFrame to the view DTO...
					availCalDto.getAvailCITimeFrames().add(availCiTimeFrame);
				}
			}			
		}
	}

	private void populateAvailCalDtoPreference(Employee employee,
			DateTime viewStartDateTimeInUTC, DateTime viewEndDateTimeInUTC,
			AvailcalViewDto availCalDto) throws InstantiationException,
			IllegalAccessException, InvocationTargetException {
		///////////////////////////////////////////////////////////////////////
		//  Prep
		///////////////////////////////////////////////////////////////////////
		String employeeId = employee.getId();
		String tenantId = employee.getTenantId();
		DateTimeZone timeZone = employee.getSite().getTimeZone();
		DateTime viewStartDateTimeInTZ = new DateTime( viewStartDateTimeInUTC.toInstant(), timeZone);
		DateTime viewEndDateTimeInTZ = new DateTime( viewEndDateTimeInUTC.toInstant(), timeZone);
		// Let's query the timeframes required to build out display data...
		DateTime queryStartDateTimeInUTC = viewStartDateTimeInUTC;
		DateTime queryEndDateTimeInUTC = viewEndDateTimeInUTC.plusDays(1);
		SimpleQuery simpleQuery;			
	
	
		///////////////////////////////////////////////////////////////////////
		//  CD Preference
		///////////////////////////////////////////////////////////////////////
		
		// Get CD AvailPreference/UnAvailPreference timeframes...		
		simpleQuery = new SimpleQuery(tenantId );			
	    simpleQuery.addFilter("startDateTime >= '" + queryStartDateTimeInUTC + "'");
	    simpleQuery.addFilter("startDateTime < '" + queryEndDateTimeInUTC + "'");
	    simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.Avail + "'");
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvail + "'");
		simpleQuery.setTotalCount(true);
		ResultSet<CDAvailabilityTimeFrame> cdPrefResultSet = cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(simpleQuery);
		Collection<CDAvailabilityTimeFrame> cdPrefTimeFrames = cdPrefResultSet.getResult();		
		
		// Group our CDPrefs by date using a map (these are for later use determining whether CI Preference instances are overridden)
		Map<DateTime, List<CDAvailabilityTimeFrame>> cdPrefTimeFramesMap = new HashMap<DateTime, List<CDAvailabilityTimeFrame>>();
		for (CDAvailabilityTimeFrame cdAvailTimeFrame : cdPrefTimeFrames){
			DateTime startDateTimeInTZ = new DateTime( cdAvailTimeFrame.getStartDateTime().toInstant(), timeZone);
			DateTime dayDateTimeInTZ = new DateTime(startDateTimeInTZ.withTimeAtStartOfDay());
	
			if (!cdPrefTimeFramesMap.containsKey(dayDateTimeInTZ)){
				cdPrefTimeFramesMap.put(dayDateTimeInTZ, new ArrayList<CDAvailabilityTimeFrame>());
			}
			cdPrefTimeFramesMap.get(dayDateTimeInTZ).add(cdAvailTimeFrame);
		}
	
		// Put together set of days that have CD prefs
		Set<DateTime> datesWithCdPrefs = new HashSet<DateTime>();
		for (CDAvailabilityTimeFrame cdPrefTimeFrame : cdPrefTimeFrames){
			DateTime startDateTimeInTZ = new DateTime(cdPrefTimeFrame.getStartDateTime().toInstant(), timeZone);
			datesWithCdPrefs.add(startDateTimeInTZ.withTimeAtStartOfDay());
		}
		
		
		// Populate AvailcalViewDto with PrefCDTimeFrames...
		for (CDAvailabilityTimeFrame cdPrefTimeFrame : cdPrefTimeFrames){
			DateTime startDateTimeInTZ = new DateTime( cdPrefTimeFrame.getStartDateTime().toInstant(), timeZone);
			DateTime endDateTimeInTZ = startDateTimeInTZ.plusMinutes(cdPrefTimeFrame.getDurationInMinutes().getMinutes());
	
			PrefCDTimeFrame prefCdTimeFrame = new PrefCDTimeFrame();
			prefCdTimeFrame.setStartDateTime(startDateTimeInTZ.toInstant().getMillis());
			prefCdTimeFrame.setEndDateTime(endDateTimeInTZ.toInstant().getMillis());			
			
			if ( startDateTimeInTZ.equals(startDateTimeInTZ.withTimeAtStartOfDay()) 
					&& endDateTimeInTZ.equals(startDateTimeInTZ.plusDays(1))){  // all day long
				if (cdPrefTimeFrame.getAvailabilityType().equals(AvailabilityType.AvailPreference)){
					prefCdTimeFrame.setPrefType(PrefType.PREFER_DAY);					
				} else 	if (cdPrefTimeFrame.getAvailabilityType().equals(AvailabilityType.UnAvailPreference)){
					prefCdTimeFrame.setPrefType(PrefType.AVOID_DAY);
				} else {
					// TODO - i18n ValidationException for invalid AvailabilityType for a Preference CD timeframe
					throw new ValidationException("Invalid AvailabiliyType for a Preference CD timeframe");
				}
			} else {  // NOT all day long, so PrefType.
				if (cdPrefTimeFrame.getAvailabilityType().equals(AvailabilityType.AvailPreference)){
					prefCdTimeFrame.setPrefType(PrefType.PREFER_TIMEFRAME);					
				} else 	if (cdPrefTimeFrame.getAvailabilityType().equals(AvailabilityType.UnAvailPreference)){
					prefCdTimeFrame.setPrefType(PrefType.AVOID_TIMEFRAME);
				} else {
					// TODO - i18n ValidationException for invalid AvailabilityType for a Preference CD timeframe
					throw new ValidationException("Invalid AvailabiliyType for a Preference CD timeframe");
				}
			}
			availCalDto.getPrefCDTimeFrames().add(prefCdTimeFrame);
		}

		///////////////////////////////////////////////////////////////////////
		//  CI Preference
		///////////////////////////////////////////////////////////////////////		
	
		// Get CI AvailPreference/UnAvailPreference timeframes...
		simpleQuery = new SimpleQuery(tenantId );			
		String ciPreffilter = "employee.primaryKey.id=" + "'" + employeeId  + "' " 
				+ "AND  (startDateTime = NULL OR startDateTime < '"  + queryEndDateTimeInUTC + "') "
				+ "AND (endDateTime = NULL OR endDateTime > '" + queryStartDateTimeInUTC + "')";
		simpleQuery.setFilter(ciPreffilter).setTotalCount(true);
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.Avail + "'");
		simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvail + "'");
		ResultSet<CIAvailabilityTimeFrame> ciPrefResultSet = 
				ciAvailabilityTimeFrameService.findCIAvailabilityTimeFrames(simpleQuery);
		Collection<CIAvailabilityTimeFrame> ciPreferenceTimeFrames = ciPrefResultSet.getResult();
	
	
		// Let's gather together all the CI Pref that have same weekday and effective date range ...
		Map<CIGroupKey, List<CIAvailabilityTimeFrame>> ciPreferenceTimeFramesMap = new HashMap<CIGroupKey, List<CIAvailabilityTimeFrame>>();
		for (CIAvailabilityTimeFrame ciPreferenceTimeFrame : ciPreferenceTimeFrames){
			CIGroupKey key = new CIGroupKey();
	
			if (ciPreferenceTimeFrame.getStartDateTime() != null) {
				key.setStartDateTime(ciPreferenceTimeFrame.getStartDateTime());
			} else {
				key.setStartDateTime(new DateTime(0L));
			}
	
			if (ciPreferenceTimeFrame.getEndDateTime() != null) {
				key.setEndDateTime(ciPreferenceTimeFrame.getEndDateTime());
			} else {
				key.setEndDateTime( new DateTime(Long.MAX_VALUE) );
			}
	
			key.setDayOfTheWeek(ciPreferenceTimeFrame.getDayOfTheWeek());
			key.setAvailabilityType(ciPreferenceTimeFrame.getAvailabilityType());
	
			if (!ciPreferenceTimeFramesMap.containsKey(key)){
				ciPreferenceTimeFramesMap.put(key, new ArrayList<CIAvailabilityTimeFrame>());
			}
			ciPreferenceTimeFramesMap.get(key).add(ciPreferenceTimeFrame);
		}
	
	
		// Now that everything is grouped, let's work our way through the groups ...
		Set<CIGroupKey> ciPreferenceKeySet = ciPreferenceTimeFramesMap.keySet();
		for (CIGroupKey key : ciPreferenceKeySet){
			List<CIAvailabilityTimeFrame> preferenceTimeFrames = ciPreferenceTimeFramesMap.get(key);
	
	
			// First a little date indexing for later use with this group ...
			DateTime effectiveDateRangeStartInTZ;
			if (key.getStartDateTime() != null) {
				effectiveDateRangeStartInTZ = new DateTime( key.getStartDateTime().toInstant(), timeZone );
			} else {
				effectiveDateRangeStartInTZ = viewStartDateTimeInTZ;
			}
	
			DateTime effectiveDateRangeEndInTZ;
			if (key.getEndDateTime() != null) {
				effectiveDateRangeEndInTZ = new DateTime( key.getEndDateTime().toInstant(), timeZone) ;	
			} else {
				effectiveDateRangeEndInTZ = viewEndDateTimeInTZ;
			}
	
			DateTime effectiveViewDateRangeStartInTZ;
			if (viewStartDateTimeInTZ.isBefore(effectiveDateRangeStartInTZ)){
				effectiveViewDateRangeStartInTZ = effectiveDateRangeStartInTZ;
			} else {
				effectiveViewDateRangeStartInTZ = viewStartDateTimeInTZ;
			}
	
			DateTime effectiveViewDateRangeEndInTZ;
			if (viewEndDateTimeInTZ.isBefore(effectiveDateRangeEndInTZ)){
				effectiveViewDateRangeEndInTZ = viewEndDateTimeInTZ;
			} else {
				effectiveViewDateRangeEndInTZ = effectiveDateRangeEndInTZ;
			}
	
	
	
	
			// Now we can actually work through the timeframe(s) for the group ...
	
			if (preferenceTimeFrames.size() == 1  &&
					preferenceTimeFrames.get(0).getStartTime().getMillisOfDay() == 0 &&
					preferenceTimeFrames.get(0).getDurationInMinutes().getMinutes() == 1440){
	
				// Just this one all-day timeframe, so it will become a PrefCITimeFrame ...
				//				CIAvailabilityTimeFrame preferenceTimeFrame = preferenceTimeFrames.get(0);
				PrefCITimeFrame preferenceCiTimeFrame = new PrefCITimeFrame();
	
				int dayOfWeekJodaValue = key.getDayOfTheWeek().getJodaValue();
				//				List<AvailcalSimpleTimeFrame> simpleTimeFramesList = new ArrayList<AvailcalSimpleTimeFrame>();
				DateTime indexDateTime = effectiveViewDateRangeStartInTZ.withTimeAtStartOfDay(); 
				while (!indexDateTime.isAfter(effectiveViewDateRangeEndInTZ)){
					if (indexDateTime.getDayOfWeek() == dayOfWeekJodaValue  &&  cdPrefTimeFramesMap.get(indexDateTime) == null){
						TimeFrameInstance timeFrameInstance = new TimeFrameInstance();
						timeFrameInstance.setStartDateTime( indexDateTime.toInstant().getMillis() );
						preferenceCiTimeFrame.getTimeFrameInstances().add(timeFrameInstance);
					}
					indexDateTime = indexDateTime.plusDays(1);
				}
	
	
				// Populate rest of the PrefCITimeFrame attributes ...
				preferenceCiTimeFrame.setDayOfTheWeek(key.getDayOfTheWeek());
				if (key.getAvailabilityType().equals(AvailabilityType.AvailPreference)) {
					preferenceCiTimeFrame.setPrefType(PrefType.PREFER_DAY);
				} else {
					preferenceCiTimeFrame.setPrefType(PrefType.AVOID_DAY);						
				}
				preferenceCiTimeFrame.setStartTime(null);
				preferenceCiTimeFrame.setEndTime(null);
				if (key.getStartDateTime().equals(new DateTime(0L))) {
					preferenceCiTimeFrame.setEffectiveDateRangeStart(null);
				} else {
					preferenceCiTimeFrame.setEffectiveDateRangeStart(key.getStartDateTime().getMillis());
				}
				if (key.getEndDateTime().equals( new DateTime(Long.MAX_VALUE))) {
					preferenceCiTimeFrame.setEffectiveDateRangeEnd(null);
				} else {
					preferenceCiTimeFrame.setEffectiveDateRangeEnd(key.getEndDateTime().getMillis());
				}
	
	
				// Add the PrefCITimeFrame to the view DTO...
				availCalDto.getPrefCITimeFrames().add(preferenceCiTimeFrame);
	
	
			} else { 
	
				// Apparently one or more partial-day timeframes, so they will be used to derive PrefCITimeFrames for display ... 
	
				// 
				List<AvailcalSimpleTimeFrame> simpleTimeFramesList = new ArrayList<AvailcalSimpleTimeFrame>();
				for (CIAvailabilityTimeFrame preferenceTimeFrame : preferenceTimeFrames){
					int startTime = preferenceTimeFrame.getStartTime().getMillisOfDay();
					int endTime = startTime + (int) TimeUnit.MINUTES.toMillis(preferenceTimeFrame.getDurationInMinutes().getMinutes());
	
					AvailcalSimpleTimeFrame simpleTimeFrame = new AvailcalSimpleTimeFrame();
					simpleTimeFrame.setStartTime(startTime);
					simpleTimeFrame.setEndTime(endTime);
					simpleTimeFramesList.add(simpleTimeFrame);
				}
	
	
				// Now each can be made into a PrefCITimeFrame with it's requisite list of any applicable timeframe instances for display...
				for (AvailcalSimpleTimeFrame simpleTimeFrame : simpleTimeFramesList){
					PrefCITimeFrame preferenceCiTimeFrame = new PrefCITimeFrame();
	
					int dayOfWeekJodaValue = key.getDayOfTheWeek().getJodaValue();
					DateTime indexDateTime = effectiveViewDateRangeStartInTZ.withTimeAtStartOfDay(); 
					while (!indexDateTime.isAfter(effectiveViewDateRangeEndInTZ)){
						if (indexDateTime.getDayOfWeek() == dayOfWeekJodaValue  &&  cdPrefTimeFramesMap.get(indexDateTime) == null){
							TimeFrameInstance timeFrameInstance = new TimeFrameInstance();
							timeFrameInstance.setStartDateTime( indexDateTime.toInstant().getMillis() + simpleTimeFrame.getStartTime() );
							timeFrameInstance.setEndDateTime( indexDateTime.toInstant().getMillis() + simpleTimeFrame.getEndTime() );
							preferenceCiTimeFrame.getTimeFrameInstances().add(timeFrameInstance);
						}
						indexDateTime = indexDateTime.plusDays(1);
					}
	
	
					// Populate rest of the PrefCITimeFrame attributes ...
					preferenceCiTimeFrame.setDayOfTheWeek(key.getDayOfTheWeek());
					preferenceCiTimeFrame.setStartTime(simpleTimeFrame.getStartTime());
					preferenceCiTimeFrame.setEndTime(simpleTimeFrame.getEndTime());
					if (key.getAvailabilityType().equals(AvailabilityType.AvailPreference)) {
						preferenceCiTimeFrame.setPrefType(PrefType.PREFER_TIMEFRAME);
					} else {
						preferenceCiTimeFrame.setPrefType(PrefType.AVOID_TIMEFRAME);						
					}
					if (key.getStartDateTime().equals(new DateTime(0L))) {
						preferenceCiTimeFrame.setEffectiveDateRangeStart(null);
					} else {
						preferenceCiTimeFrame.setEffectiveDateRangeStart(key.getStartDateTime().getMillis());
					}
					if (key.getEndDateTime().equals( new DateTime(Long.MAX_VALUE))) {
						preferenceCiTimeFrame.setEffectiveDateRangeEnd(null);
					} else {
						preferenceCiTimeFrame.setEffectiveDateRangeEnd(key.getEndDateTime().getMillis());
					}
	
	
					// Add the PrefCITimeFrame to the view DTO...
					availCalDto.getPrefCITimeFrames().add(preferenceCiTimeFrame);
				}
			}			
		}
	}

	/**
     * Returns DayOfWeek list of only selected days.
     * @param selectedDays
     * @return
     */
	private Set<DayOfWeek> toDaysList(AvailcalUpdateParamsCIDaySelections selectedDays) {
		Set<DayOfWeek> selectedDaysList = new HashSet<DayOfWeek>();
		if (selectedDays.isSunday()){
			selectedDaysList.add(DayOfWeek.SUNDAY);
		} 

		if (selectedDays.isMonday()){
			selectedDaysList.add(DayOfWeek.MONDAY);			
		} 

		if (selectedDays.isTuesday()){
			selectedDaysList.add(DayOfWeek.TUESDAY);			
		} 

		if (selectedDays.isWednesday()){
			selectedDaysList.add(DayOfWeek.WEDNESDAY);			
		} 

		if (selectedDays.isThursday()){
			selectedDaysList.add(DayOfWeek.THURSDAY);			
		} 

		if (selectedDays.isFriday()){
			selectedDaysList.add(DayOfWeek.FRIDAY);			
		} 

		if (selectedDays.isSaturday()){
			selectedDaysList.add(DayOfWeek.SATURDAY);			
		}
		
		return selectedDaysList;
	}

	
	/**
	 * Validates list of timeframes and returns them as a sorted list.
	 * @param timeFrames
	 * @return
	 */
	private List<AvailcalSimpleTimeFrame> sortTimeFrames( List<AvailcalSimpleTimeFrame> timeFrames ) {
		
		final long millisInADay = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
		final long millisInAnHour = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
	
		// First let's get them into ascending order by start time...
		// TODO - Quick and dirty solution.... may need to revisit for efficiency...
		SortedMap<Long, AvailcalSimpleTimeFrame> sortedTimeFramesMap =  new TreeMap<Long, AvailcalSimpleTimeFrame>();
		for (AvailcalSimpleTimeFrame availTimeFrame : timeFrames){
			
			//endtime=0 --> endtime=24hr
			if (availTimeFrame.getEndTime()==0){
				availTimeFrame.setEndTime(millisInADay);  // note this assumes no DST time change
			}
			
			sortedTimeFramesMap.put(availTimeFrame.getStartTime(), availTimeFrame);
		}
		
		// Second, let's validate timeframe (end times later than start times, none overlap, etc.) ...
		AvailcalSimpleTimeFrame[] sortedTimeFrameArray = sortedTimeFramesMap.values().toArray(new AvailcalSimpleTimeFrame[0]);
		for (int i = 0;  i < sortedTimeFrameArray.length;  i++){
			AvailcalSimpleTimeFrame thisTimeFrame = null;
			AvailcalSimpleTimeFrame nextTimeFrame = null;
			
			thisTimeFrame = sortedTimeFrameArray[i];
			if (i != sortedTimeFrameArray.length - 1){
				nextTimeFrame = sortedTimeFrameArray[i+1];
			}
			
			if ( thisTimeFrame.getStartTime() < 0 ){ 
				throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
			if ( thisTimeFrame.getEndTime()   < 0 ){ 
				throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
			if ( thisTimeFrame.getStartTime() > millisInADay +  millisInAnHour){ 
				throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
			if ( thisTimeFrame.getEndTime()   > millisInADay + millisInAnHour){ 
				throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
			if ( thisTimeFrame.getStartTime() >= thisTimeFrame.getEndTime() ){ 
				throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
	
			if (nextTimeFrame != null){
				if ( nextTimeFrame.getStartTime() < 0 ){ 
					throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
				if ( nextTimeFrame.getEndTime()   < 0 ){ 
					throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
				if ( nextTimeFrame.getStartTime() > millisInADay + millisInAnHour){ 
					throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
				if ( nextTimeFrame.getEndTime()   > millisInADay + millisInAnHour){ 
					throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
				if ( nextTimeFrame.getStartTime() >= nextTimeFrame.getEndTime() ){ 
					throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }
				if ( thisTimeFrame.getEndTime() >= nextTimeFrame.getStartTime() ){ 
					throw new ValidationException("Invalid time frame(s)!"); /*TODO - Make i18n*/ }						
			}					
		}
		return Arrays.asList(sortedTimeFrameArray);
	}


	/**
	 * Returns sorted list of inverse timeframes for a day.
	 * So for instance, if the input list represented 7am-1pm & 4pm-8pm 
	 * then the returned list would represent 12am-7am, 1pm-4pm, and 8pm-12am (day's end).
	 * 
	 * NOTE that these calculations are based upon a 24 hour day, so caller may need to 
	 * adjust returned results for Daylight Savings Time (DST) transition days that end 
	 * up being either 23 or 25 hours long.
	 * 
	 * @param timeFrames
	 * @return
	 */
	private List<AvailcalSimpleTimeFrame> getInverseTimeFrames(List<AvailcalSimpleTimeFrame> timeFrames) {
		// Let's make sure they are valid and sorted first...
		timeFrames = this.sortTimeFrames(timeFrames);

		final long millisInADay = TimeUnit.DAYS.toMillis(1);

		AvailcalSimpleTimeFrame[] sortedTimeFramesArray = (AvailcalSimpleTimeFrame[]) timeFrames.toArray();
		ArrayList<AvailcalSimpleTimeFrame> newTimeFrames = new ArrayList<AvailcalSimpleTimeFrame>();
		
		long invTfStart = 0;
		for (AvailcalSimpleTimeFrame tf: sortedTimeFramesArray){
			if (tf.getStartTime() > invTfStart){
				AvailcalSimpleTimeFrame invTF =  new AvailcalSimpleTimeFrame();
				invTF.setStartTime(invTfStart);
				invTF.setEndTime(tf.getStartTime());
				
				newTimeFrames.add(invTF);
			}
			invTfStart = tf.getEndTime();
		}
		if (invTfStart!=0 && invTfStart < millisInADay){
			AvailcalSimpleTimeFrame lastInvTF =  new AvailcalSimpleTimeFrame();
			lastInvTF.setStartTime(invTfStart);
			lastInvTF.setEndTime(millisInADay);
			
			newTimeFrames.add(lastInvTF);

		}

		return newTimeFrames;
	}

	public NotificationConfigInfo checkNotificationEnabled(Employee employee, MsgDeliveryType deliveryType) {
        return notificationService.userHasNotificationEnabled(employee.getUserAccount(), deliveryType);
	}

    public Collection<Object[]> employeeICalendarInfo(PrimaryKey employeePrimaryKey, long startDate) {
        String sql =
            "SELECT id, startDateTime, endDateTime, CONCAT(teamName, ' - ', skillName) " +
            "  FROM Shift " +
            " WHERE employeeId = :employeeId AND tenantId = :tenantId AND startDateTime > :startDate " +
            "UNION " +
            "SELECT id, startDateTime, DATE_ADD(startDateTime, INTERVAL durationInMinutes MINUTE), reason " +
            "  FROM CDAvailabilityTimeFrame " +
            " WHERE employeeId = :employeeId AND tenantId = :tenantId AND isPTO AND startDateTime > :startDate " +
            "UNION " +
            "SELECT id, startDateTime, endDateTime, " +
            "       CONCAT('Open Shift: ', teamName, ' - ', skillName, '; Deadline: ', deadline) " +
            "  FROM PostedOpenShift " +
            " WHERE employeeId = :employeeId AND tenantId = :tenantId AND startDateTime > :startDate " +
            "UNION " +
            "SELECT id, effectiveStartDate, effectiveEndDate, description " +
            "  FROM Holiday " +
            " WHERE tenantId = :tenantId AND effectiveStartDate > :startDate";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeePrimaryKey.getId());
        query.setParameter("tenantId", employeePrimaryKey.getTenantId());
        query.setParameter("startDate", new Timestamp(startDate));

        return query.getResultList();
    }

    public Employee findEmployeeByHash(String hash) {
        String sql = "SELECT e FROM Employee e " +
                " WHERE e.calendarSyncId = :hash " +
                "   AND " + QueryPattern.NOT_DELETED.val("e");
        Query query = entityManager.createQuery(sql);
        query.setParameter("hash", hash);

        List<Employee> employees = query.getResultList();

        return employees.isEmpty() ? null : employees.get(0);
    }

}
