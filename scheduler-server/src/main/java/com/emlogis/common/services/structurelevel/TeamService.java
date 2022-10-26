package com.emlogis.common.services.structurelevel;

import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.schedule.ShiftStructureService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.aom.AOMRelationshipDef;
import com.emlogis.model.employee.*;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.schedule.ShiftStructure;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.StructureLevel;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.ejb.*;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class TeamService extends StructureLevelService<Team> {

	@EJB
	private SkillService skillService;

	@EJB
	private ShiftStructureService shiftStructureService;

    @EJB
    private SessionService sessionService;

	 /**
	 * findTeams() find a list of Teams matching criteria;
	 * @param simpleQuery
	 * @param acl
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	
	public ResultSet<Team> findTeams(SimpleQuery simpleQuery, AccountACL acl) {
		simpleQuery.setEntityClass(Team.class);
		simpleQuery.setAcl(acl);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);
        return sqh.executeSimpleQueryWithPaging(getEntityManager(), simpleQuery);
    }
	
	public Team getTeam(PrimaryKey primaryKey) {
		return getEntityManager().find(Team.class, primaryKey);
	}

    @SuppressWarnings("unchecked")
	public Team createTeam(PrimaryKey pk) {
		Team team = new Team(pk);
		insert(team);
		return team;
	}

    public void softDelete(Team team) {
        if (findEntitiesWithTeam(team, "ShiftPattern").size() > 0
                || findEntitiesWithTeam(team, "ShiftStructure").size() > 0
                || findShiftsWithTeam(team).size() > 0) {
            throw new ValidationException(sessionService.getMessage("entity.constraint.violation"));
        } else if (findEntitiesWithTeam(team, "EmployeeTeam").size() > 0){
        	throw new ValidationException(sessionService.getMessage("team.constraint.violation"));
    	} else {
        	String dateTimeString = new DateTime().toString();
        	team.setName(team.getName() + dateTimeString);
        	team.setIsDeleted(true);
        	
        	Set<Skill> skills = team.getSkills();
        	for (Skill skill : skills){
        		team.removeSkill(skill);
        	}
        	
    		this.update(team);
    	}
    }

    public void hardDelete(Team team) {
        deleteEntities(team.getShiftStructures());
        deleteEntities(team.getShiftPatterns());
        deleteEntities(team.getEmployeeTeams());
        deleteEntities(team.getTeamContracts());

        deleteEntity(team);
    }

    /* (non-Javadoc)
	 * @see com.emlogis.common.services.structurelevel.StructureLevelService#delete(com.emlogis.model.structurelevel.StructureLevel)
	 */
    @Deprecated
	@Override
	public void delete(Team team) {
    	// Only soft deletes are supported for Teams, 
    	// so deprecated and delegated to softDelete
		softDelete(team);
	}

	/**
     * Get collection of Skills for Team specified by PrimaryKey
     * @param teamPrimaryKey
     * @return
     */
    public ResultSet<Skill> getSkills(PrimaryKey teamPrimaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, teamPrimaryKey, Team.class,
                "skills");
	}

    /**
     * Add Skill specified by id to Site specified by PrimaryKey
     * @param teamPrimaryKey
     * @param skillId
     */
	public void addSkill(PrimaryKey teamPrimaryKey, String skillId) {
		Team team = getTeam(teamPrimaryKey);
        if (team != null) {
            PrimaryKey skillPrimaryKey = new PrimaryKey(teamPrimaryKey.getTenantId(), skillId);
            Skill skill = skillService.getSkill(skillPrimaryKey);

            if (skill != null) {
            	team.addSkill(skill);
            	team.touch();
            	getEntityManager().persist(team);

        		// Adding a Skill to a Team also adds the same Skill to the 
            	// parent Site if it hasn't already been added.
            	Site site = getSite(team);
            	if (site != null){
            		site.addSkill(skill);
            		site.touch();
            		getEntityManager().persist(site);
            	}
            }
        }
    }

	/**
	 * Remove Skill specified by id to Team specified by PrimaryKey
	 * @param teamPrimaryKey
	 * @param skillId
	 */
    public void removeSkill(PrimaryKey teamPrimaryKey, String skillId) {
    	Team team = getTeam(teamPrimaryKey);
        if (team != null) {
            PrimaryKey skillPrimaryKey = new PrimaryKey(teamPrimaryKey.getTenantId(), skillId);
            Skill skill = skillService.getSkill(skillPrimaryKey);

            if (skill != null && team.getSkills().contains(skill)) {
            	team.removeSkill(skill);
                team.touch();
                getEntityManager().persist(team);
            }
        }
    }

    public void addShiftStructure(PrimaryKey teamPrimaryKey, String shiftStructureId) {
        Team team = getTeam(teamPrimaryKey);
        if (team != null) {
            PrimaryKey shiftStructurePrimaryKey = new PrimaryKey(teamPrimaryKey.getTenantId(), shiftStructureId);
            ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(shiftStructurePrimaryKey);

            Collection<ShiftStructure> shiftStructures = team.getShiftStructures();
            if (shiftStructure != null && !shiftStructures.contains(shiftStructure)) {
                shiftStructures.add(shiftStructure);
                team.touch();
                getEntityManager().persist(team);
            }
        }
    }

    public void removeShiftStructure(PrimaryKey teamPrimaryKey, String shiftStructureId) {
        Team team = getTeam(teamPrimaryKey);
        if (team != null) {
            PrimaryKey shiftStructurePrimaryKey = new PrimaryKey(teamPrimaryKey.getTenantId(), shiftStructureId);
            ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(shiftStructurePrimaryKey);

            Collection<ShiftStructure> shiftStructures = team.getShiftStructures();
            if (shiftStructure != null && shiftStructures.contains(shiftStructure)) {
                shiftStructures.remove(shiftStructure);
                team.touch();
                getEntityManager().persist(team);
            }
        }
    }

    public Site getSite(Team team) {
    	List<StructureLevel> levels = getAssociatedObjects(team, AOMRelationshipDef.SITE_TEAM_REL,
                TraversalDirection.IN);
    	return levels.size() == 1 ? (Site)levels.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    public Collection<Team> getTeams(String tenantId, Set<String> teamIds) {
        if (teamIds == null || teamIds.size() == 0) {
            return Collections.emptyList();
        }

        String idClause = "";
        for (String teamId : teamIds) {
            if (StringUtils.isEmpty(idClause)) {
                idClause = "'" + teamId + "'";
            } else {
                idClause += ", '" + teamId + "'";
            }
        }

        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setEntityClass(Team.class);
        if (StringUtils.isNotEmpty(idClause)) {
            simpleQuery.setFilter("primaryKey.id IN (" + idClause + ")");
        }
        return new SimpleQueryHelper().executeSimpleQuery(getEntityManager(), simpleQuery);
    }

    public int getEmployeeCount(Collection<Team> teams, String tenantId) {
        String ids = ModelUtils.commaSeparatedQuotedIds(teams);
        String sql =
                "SELECT count(*) FROM (SELECT DISTINCT e.* FROM EmployeeTeam et, Employee e " +
                "                       WHERE et.tenantId = :tenantId AND et.tenantId = e.tenantId " +
                "                         AND et.employeeId = e.id " +
                        "                         AND et.teamId IN (" + ids + ") " +
                        "                         AND " + QueryPattern.NOT_DELETED.val("e") + ") x ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public ResultSet<Object[]> queryEmployees(PrimaryKey teamPrimaryKey, int offset, int limit, String orderBy,
                                              String orderDir) {
        String sql =
                "SELECT DISTINCT e.id, e.firstName, e.lastName, e.employeeType, et.isHomeTeam, s.name " +
                "  FROM EmployeeTeam et LEFT JOIN Employee e ON et.tenantId = e.tenantId AND et.employeeId = e.id " +
                "                       LEFT JOIN EmployeeSkill es ON e.tenantId = es.employeeTenantId " +
                "                                                  AND e.id = es.employeeId " +
                "                       LEFT JOIN Skill s ON s.id = es.skillId AND s.tenantId = es.skillTenantId " +
                " WHERE et.teamId = :teamId AND et.tenantId = :tenantId " +
                        "   AND es.isPrimarySkill = true " + " AND " + QueryPattern.NOT_DELETED.val("e");

        Query countQuery = getEntityManager().createNativeQuery("SELECT count(*) FROM (" + sql + ") x");

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.startsWith("Employee.")) {
                orderBy = orderBy.replaceFirst("Employee.", "e.");
            } else if (orderBy.startsWith("Skill.")) {
                orderBy = orderBy.replaceFirst("Skill.", "s.");
            } else if (orderBy.startsWith("EmployeeTeam.")) {
                orderBy = orderBy.replaceFirst("EmployeeTeam.", "et.");
            } else {
                orderBy = "e." + orderBy;
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", teamPrimaryKey.getTenantId());
        query.setParameter("teamId", teamPrimaryKey.getId());
        if (offset > -1) {
            query.setFirstResult(offset);
        }
        if (limit > -1) {
            query.setMaxResults(limit);
        }

        countQuery.setParameter("tenantId", teamPrimaryKey.getTenantId());
        countQuery.setParameter("teamId", teamPrimaryKey.getId());

        ResultSet<Object[]> result = new ResultSet<>();

        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    public List<Object[]> dailyScheduleReport(String teamsIds, long startDate, long startTime, long endTime) {
        String sql =
            "SELECT sh.id, sh.employeeId, sh.employeeName, sh.skillId, sh.skillName, sh.startDateTime, sh.endDateTime, " +
            "  sh.requested, sh.comment, empls.professionalLabel, empls.teamId, empls.name " +
            "FROM (" +
            "       SELECT s.id, s.employeeId, s.employeeName, s.skillId, s.skillName, s.startDateTime, s.endDateTime, " +
            "           s.scheduleId, s.requested, s.comment" +
            "       FROM Shift s " +
            "       WHERE (s.startDateTime >= :startDate and s.startDateTime < :endDate) OR " +
            "           (s.endDateTime > :startDate and s.endDateTime < :endDate) OR " +
            "           (s.startDateTime <= :startDate and s.endDateTime >= :endDate)) sh " +
            "  JOIN ( " +
            "         SELECT DISTINCT e.id, e.professionalLabel, et.teamId, t.name, ts.schedules_id " +
            "         FROM EmployeeTeam et " +
            "           LEFT JOIN Employee e ON et.tenantId = e.tenantId AND et.employeeId = e.id " +
            "           LEFT JOIN Team_Schedule ts ON ts.Team_id = et.teamId AND ts.Team_tenantId = et.teamTenantId " +
            "           LEFT JOIN Team t ON ts.Team_id = t.id AND ts.Team_tenantId = t.tenantId " +
            "         WHERE et.teamId in (" + teamsIds + ") AND et.tenantId = :tenantId " +
                    "               AND " + QueryPattern.NOT_DELETED.val("e") +
            "       ) empls " +
            "    ON empls.id = sh.employeeId AND empls.schedules_id = sh.scheduleId " +
            "  ORDER BY sh.skillName, sh.startDateTime, sh.endDateTime " ;
        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", sessionService.getTenantId());
        query.setParameter("startDate", new Timestamp(startTime == -1 ? startDate : startDate + startTime));
        query.setParameter("endDate", new Timestamp(startDate + (startTime == -1 ? (24 * 60 * 60 * 1000) : endTime)));

        return query.getResultList();
    }

    public List<Object[]> getTeamsEmployees(List<String> teamIds) {
        String temIdsString = ModelUtils.separatedValues(teamIds, ',', '\'');

        String sql = "SELECT" +
                "        e.id as employeeId," +
                "        CONCAT(e.firstName, ' ', e.lastName) as employeeName," +
                "        e.professionalLabel," +
                "        et.teamId," +
                "        t.name as teamName," +
                "        et.isFloating," +
                "        e.employeeType" +
                "      FROM EmployeeTeam et" +
                "        INNER JOIN Employee e ON et.tenantId = e.tenantId AND et.employeeId = e.id" +
                "        INNER JOIN Team t ON t.id = et.teamId AND t.tenantId = et.tenantId" +
                "      WHERE et.teamId IN (" + temIdsString + ") AND et.tenantId = :tenantId AND et.isHomeTeam IS TRUE " +
                "           AND " + QueryPattern.NOT_DELETED.val("e");
        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", sessionService.getTenantId());

        return query.getResultList();
    }

    public List<Object[]> getTeamsEmployeesExtended(List<String> teamIds){
        String teamIdsString = ModelUtils.separatedValues(teamIds, ',', '\'');

        String sql = "SELECT " +
            "  e.id AS employeeId, " +
            "  CONCAT(e.firstName, ' ', e.lastName) AS employeeName, " +
            "  e.employeeType, " +
            "  e.activityType, " +
            "  e.startDate, " +
            "  e.endDate, " +
            "  u.login, " +
            "  e.hourlyRate, " +
            "  hpw.minimumValue AS minHoursWeek, " +
            "  hpw.maximumValue AS maxHoursWeek, " +
            "  hpd.minimumValue AS minHoursDay, " +
            "  hpd.maximumValue AS maxHoursDay, " +
            "  dpw.maximumValue AS maxDaysWeek, " +
            "  GROUP_CONCAT( " +
            "      DISTINCT " +
            "      s.abbreviation, ' (', " +
            "      if(es.isPrimarySkill = 1, psmh.minimumValue, 0), ') ', " +
            "      IFNULL(es.skillScore, '') " +
            "      SEPARATOR ' / ') AS skills, " +
            "  GROUP_CONCAT( " +
            "      DISTINCT " +
            "      t.name, " +
            "      if(et.isHomeTeam = 1, '(H)', ''), ' ', " +
            "      if(et.isFloating = 1, '(F)', ''), ' ' " +
            "      SEPARATOR ' / ') AS teams, " +
            "  GROUP_CONCAT( " +
            "      DISTINCT " +
            "      if(et.isHomeTeam = 1, t.id, ''), '' " +
            "      SEPARATOR '') AS homeTeamId " +
            "FROM Employee e " +
            "  INNER JOIN UserAccount u ON u.id = e.userAccountId AND u.tenantId = e.userAccountTenantId " +
            "  INNER JOIN EmployeeContract ec ON e.id = ec.employeeId AND e.tenantId = ec.employeeTenantId " +
            "  INNER JOIN IntMinMaxCL hpw " +
            "    ON hpw.contractId = ec.id AND hpw.contractTenantId = ec.tenantId AND hpw.contractLineType = 'HOURS_PER_WEEK' " +
            "  INNER JOIN IntMinMaxCL hpd " +
            "    ON hpd.contractId = ec.id AND hpd.contractTenantId = ec.tenantId AND hpd.contractLineType = 'HOURS_PER_DAY' " +
            "  INNER JOIN IntMinMaxCL dpw " +
            "    ON dpw.contractId = ec.id AND dpw.contractTenantId = ec.tenantId AND dpw.contractLineType = 'DAYS_PER_WEEK' " +
            "  LEFT JOIN IntMinMaxCL psmh " +
            "    ON psmh.contractId = ec.id AND psmh.contractTenantId = ec.tenantId AND " +
            "       psmh.contractLineType = 'HOURS_PER_WEEK_PRIME_SKILL' " +
            "  LEFT JOIN EmployeeSkill es " +
            "    ON e.id = es.employeeId AND e.tenantId = es.employeeTenantId " +
            "  LEFT JOIN Skill s " +
            "    ON s.id = es.skillId AND s.tenantId = es.skillTenantId " +
            "  LEFT JOIN EmployeeTeam et ON e.id = et.employeeId AND e.tenantId = et.employeeTenantId " +
            "  LEFT JOIN Team t ON t.id = et.teamId AND t.tenantId = et.teamTenantId " +
            "WHERE e.tenantId = :tenantId " +
            "GROUP BY e.id, e.tenantId " +
                "HAVING homeTeamId IN (" + teamIdsString + ") " + " AND " + QueryPattern.NOT_DELETED.val("e");
        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", sessionService.getTenantId());

        return query.getResultList();
    }

    private String getAvailabilityTypeSQLRestrictions(boolean isPreference) {
        if (isPreference) {
            return " AND availabilityType != '" + AvailabilityTimeFrame.AvailabilityType.Avail + "'"+
                    " AND availabilityType != '" + AvailabilityTimeFrame.AvailabilityType.UnAvail + "'";
        }
        return " AND availabilityType != '" + AvailabilityTimeFrame.AvailabilityType.AvailPreference + "'"+
                " AND availabilityType != '" + AvailabilityTimeFrame.AvailabilityType.UnAvailPreference + "'";
    }

    public List<CDAvailabilityTimeFrame> getCDAvailability(List<String> teamIds, DateTime startDateTime,
                                                           DateTime endDateTime, boolean isPreference){
        String teamIdsString = ModelUtils.separatedValues(teamIds, ',', '\'');

        String sql = "SELECT cda.*" +
                " FROM CDAvailabilityTimeFrame cda" +
                "  LEFT JOIN AbsenceType at ON at.id = cda.AbsenceTypeId" +
                "  INNER JOIN Employee e ON cda.employeeId = e.id AND e.tenantId = cda.tenantId" +
                "  INNER JOIN EmployeeTeam et ON et.employeeId = e.id AND et.tenantId = cda.tenantId" +
                "  INNER JOIN Team t ON et.teamId = t.id AND t.tenantId = cda.tenantId" +
                " WHERE cda.tenantId = :tenantId " +
                "  AND cda.startDateTime >= :startDate " +
                "  AND cda.startDateTime < :endDate " +
                "  AND t.id IN ( " + teamIdsString + " )" +
                getAvailabilityTypeSQLRestrictions(isPreference) +
                " AND " + QueryPattern.NOT_DELETED.val("e");
        Query query = getEntityManager().createNativeQuery(sql, CDAvailabilityTimeFrame.class);
        query.setParameter("tenantId", sessionService.getTenantId());
        query.setParameter("startDate", new Timestamp(startDateTime.getMillis()));
        query.setParameter("endDate", new Timestamp(endDateTime.getMillis()));

        return query.getResultList();
    }

    public List<CIAvailabilityTimeFrame> getCIAvailability(List<String> teamIds, DateTime startDateTime,
                                                           DateTime endDateTime, boolean isPreference){
        String teamIdsString = ModelUtils.separatedValues(teamIds, ',', '\'');

        String sql = "SELECT cia.* " +
                "FROM CIAvailabilityTimeFrame cia" +
                "  INNER JOIN Employee e ON cia.employeeId = e.id AND e.tenantId = cia.tenantId" +
                "  INNER JOIN EmployeeTeam et ON et.employeeId = e.id AND et.tenantId = cia.tenantId" +
                "  INNER JOIN Team t ON et.teamId = t.id AND t.tenantId = cia.tenantId " +
                "WHERE cia.tenantId = :tenantId " +
                "  AND (cia.startDateTime BETWEEN :startDate AND :endDate " +
                "           OR cia.endDateTime BETWEEN :startDate AND :endDate) " +
                "  AND t.id IN ( " + teamIdsString + " )" +
                getAvailabilityTypeSQLRestrictions(isPreference) +
                " AND " + QueryPattern.NOT_DELETED.val("e");
        Query query = getEntityManager().createNativeQuery(sql, CIAvailabilityTimeFrame.class);
        query.setParameter("tenantId", sessionService.getTenantId());
        query.setParameter("startDate", new Timestamp(startDateTime.getMillis()));
        query.setParameter("endDate", new Timestamp(endDateTime.getMillis()));

        return query.getResultList();
    }

    public List<Object[]> getTeamsSkills(List<String> teamIds){
        String teamIdsString = ModelUtils.separatedValues(teamIds, ',', '\'');

        String sql = "SELECT ts.teams_id, ts.skills_id, s.name FROM Team_Skill ts" +
                "  INNER JOIN Skill s ON ts.skills_id = s.id AND s.tenantId = ts.skills_tenantId " +
                "where ts.teams_id IN ( " + teamIdsString + " ) " +
                "AND ts.teams_tenantId = :tenantId and ts.skills_tenantId = :tenantId";
        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", sessionService.getTenantId());

        return query.getResultList();
    }

    public List<Object[]> getShiftDemands(List<String> teamIds, DateTime dateTime){
        String teamIdsString = ModelUtils.separatedValues(teamIds, ',', '\'');

        String sql = "SELECT sp.id as shiftPatternId, sp.teamId, " +
                "   sp.skillId, sp.dayOfWeek, sp.cdDate, sp.maxEmployeeCount, " +
                "   sd.id as demandId, sd.startTime, sd.lengthInMin, sd.employeeCount  " +
                "FROM ShiftDemand sd " +
                "INNER JOIN ShiftPattern sp ON sp.id = sd.shiftPatternId " +
                "WHERE sp.tenantId = :tenantId " +
                "   AND sp.teamId IN (" + teamIdsString + ") " +
                "   AND (sp.cdDate IS NOT NULL OR sp.dayOfWeek IS NOT NULL)" +
                "   AND (sp.cdDate IS NULL OR DATE(sp.cdDate) = :cdDate) " +
                "   AND (sp.dayOfWeek IS NULL OR sp.dayOfWeek = :dayOfWeek)";
        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", sessionService.getTenantId());
        query.setParameter("cdDate", new java.sql.Date(dateTime.getMillis()));
        query.setParameter("dayOfWeek", toServerDayOfWeek(dateTime.dayOfWeek().get()));

        return query.getResultList();
    }

    private int toServerDayOfWeek(int dateTimeDayOfWeek) {
        return dateTimeDayOfWeek % 7;
    }

    public List<Object[]> getExtendedShiftRequirements(DateTime startDate, DateTime endDate, List<String> teams) {
        Query q = getEntityManager().createNativeQuery(
                "SELECT sr.id, sp.cdDate, sp.dayOfWeek, st.startTime, sl.lengthInMin, " +
                        "            sr.employeeCount, sr.excessCount, sp.teamId, skill.id as skillId from EGS.ShiftPattern sp" +
                        "  JOIN EGS.Skill skill ON skill.id = sp.skillId AND skill.tenantId = sp.skillTenantId" +
                        "  JOIN EGS.ShiftReq sr ON sr.shiftPatternId = sp.id AND sr.tenantId = sp.tenantId" +
                        "  JOIN EGS.ShiftType st ON st.id = sr.shiftTypeId AND st.tenantId = sp.tenantId" +
                        "  JOIN EGS.ShiftLength sl ON sl.id = st.shiftLengthId AND sl.tenantId = sp.tenantId " +
                        "WHERE sp.tenantId = :tenantId " +
                        "      AND (sp.cdDate IS NULL OR sp.cdDate >= :startDate AND sp.cdDate <= :endDate)" +
                        "      AND sp.teamId IN (:teamIds);");
        q.setParameter("tenantId", sessionService.getTenantId());
        q.setParameter("startDate", new Timestamp(startDate.getMillis()));
        q.setParameter("endDate", new Timestamp(endDate.getMillis()));
        q.setParameter("teamIds", teams);
        List<Object[]> result = q.getResultList();
        return result;
    }

    public ResultSet<ShiftStructure> getShiftStructures(PrimaryKey teamPrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, teamPrimaryKey, Team.class,
                "shiftStructures");
    }

    public ResultSet<Object[]> getEmployees(PrimaryKey teamPrimaryKey, String filter, int offset, int limit,
                                            String orderBy, String orderDir) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String sql =
            "SELECT DISTINCT e.id employeeId, e.firstName, e.lastName, e.employeeType, e.hireDate, s.name skillName, " +
            "                s.id skillId, ht.name primaryTeamName, ht.id primaryTeamId, et.isFloating " +
            "  FROM EmployeeTeam et " +
            "       LEFT JOIN Employee e ON et.tenantId = e.tenantId AND et.employeeId = e.id " +
            "       LEFT JOIN EmployeeSkill es ON e.tenantId = es.employeeTenantId AND e.id = es.employeeId " +
            "                                 AND es.isPrimarySkill = true " +
            "       LEFT JOIN Skill s ON s.id = es.skillId AND s.tenantId = es.skillTenantId " +
            "       LEFT JOIN EmployeeTeam het ON e.id = het.employeeId AND e.tenantId = het.employeeTenantId " +
            "                                 AND het.isHomeTeam = true " +
            "       LEFT JOIN Team ht ON ht.id = het.teamId AND ht.tenantId = het.teamTenantId " +
                    " WHERE et.teamId = :teamId AND et.tenantId = :tenantId " + " AND " + QueryPattern.NOT_DELETED.val("e");

        if (StringUtils.isNotBlank(filter)) {
            String[] filters = filter.split(";");
            for (String filterItem : filters) {
                if (filterItem.startsWith("EmployeeTeam.")) {
                    filterItem = filterItem.replaceFirst("EmployeeTeam.", "het.");
                } else if (filterItem.startsWith("Employee.")) {
                    filterItem = filterItem.replaceFirst("Employee.", "e.");
                } else if (filterItem.startsWith("Skill.")) {
                    filterItem = filterItem.replaceFirst("Skill.", "s.");
                } else if (filterItem.startsWith("Team.")) {
                    filterItem = filterItem.replaceFirst("Team.", "ht.");
                } else {
                    filterItem = "e." + filterItem;
                }
                sql += " AND " + filterItem + " ";
            }
        }

        Query countQuery = getEntityManager().createNativeQuery("SELECT count(*) FROM (" + sql + ") x");

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.startsWith("EmployeeTeam.")) {
                orderBy = orderBy.replaceFirst("EmployeeTeam.", "het.");
            } else if (orderBy.startsWith("Employee.")) {
                orderBy = orderBy.replaceFirst("Employee.", "e.");
            } else if (orderBy.startsWith("Skill.")) {
                orderBy = orderBy.replaceFirst("Skill.", "s.");
            } else if (orderBy.startsWith("Team.")) {
                orderBy = orderBy.replaceFirst("Team.", "ht.");
            } else {
                orderBy = "e." + orderBy;
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", teamPrimaryKey.getTenantId());
        query.setParameter("teamId", teamPrimaryKey.getId());
        if (offset > -1) {
            query.setFirstResult(offset);
        }
        if (limit > -1) {
            query.setMaxResults(limit);
        }

        countQuery.setParameter("tenantId", teamPrimaryKey.getTenantId());
        countQuery.setParameter("teamId", teamPrimaryKey.getId());

        ResultSet<Object[]> result = new ResultSet<>();

        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    public ResultSet<EmployeeTeam> getTeamMembership(PrimaryKey teamPrimaryKey, String filter, int offset, int limit,
                                                     String orderBy, String orderDir) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String sql =
            "SELECT DISTINCT et.* " +
            "  FROM EmployeeTeam et " +
            "       LEFT JOIN Employee e ON et.tenantId = e.tenantId AND et.employeeId = e.id " +
            "       LEFT JOIN Team t ON t.id = et.teamId AND t.tenantId = et.teamTenantId " +
                    " WHERE et.teamId = :teamId AND et.tenantId = :tenantId " +
                    " AND " + QueryPattern.NOT_DELETED.val("e");

        if (StringUtils.isNotBlank(filter)) {
            String[] filters = filter.split(";");
            for (String filterItem : filters) {
                if (filterItem.startsWith("EmployeeTeam.")) {
                    filterItem = filterItem.replaceFirst("EmployeeTeam.", "et.");
                } else if (filterItem.startsWith("Employee.")) {
                    filterItem = filterItem.replaceFirst("Employee.", "e.");
                } else if (filterItem.startsWith("Team.")) {
                    filterItem = filterItem.replaceFirst("Team.", "t.");
                } else {
                    filterItem = "e." + filterItem;
                }
                sql += " AND " + filterItem + " ";
            }
        }

        Query countQuery = getEntityManager().createNativeQuery("SELECT count(*) FROM (" + sql + ") x");

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.startsWith("Employee.")) {
                orderBy = orderBy.replaceFirst("Employee.", "e.");
            } else if (orderBy.startsWith("EmployeeTeam.")) {
                orderBy = orderBy.replaceFirst("EmployeeTeam.", "et.");
            } else if (orderBy.startsWith("Team.")) {
                orderBy = orderBy.replaceFirst("Team.", "t.");
            } else {
                orderBy = "e." + orderBy;
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql, EmployeeTeam.class);
        query.setParameter("tenantId", teamPrimaryKey.getTenantId());
        query.setParameter("teamId", teamPrimaryKey.getId());
        if (offset > -1) {
            query.setFirstResult(offset);
        }
        if (limit > -1) {
            query.setMaxResults(limit);
        }

        countQuery.setParameter("tenantId", teamPrimaryKey.getTenantId());
        countQuery.setParameter("teamId", teamPrimaryKey.getId());

        ResultSet<EmployeeTeam> result = new ResultSet<>();

        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    public ResultSet<Object[]> getUnassociatedEmployees(PrimaryKey teamPrimaryKey, String filter, int offset, int limit,
                                                        String orderBy, String orderDir) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String sql =
            "SELECT DISTINCT e.id employeeId, e.firstName, e.lastName, e.employeeType, e.hireDate, " +
            "                s.name skillName, s.id skillId, ht.name primaryTeamName, ht.id primaryTeamId, " +
            "                et.isFloating " +
            "  FROM EmployeeTeam et " +
            "       LEFT JOIN Employee e ON et.tenantId = e.tenantId AND et.employeeId = e.id " +
            "       LEFT JOIN EmployeeSkill es ON e.tenantId = es.employeeTenantId AND e.id = es.employeeId " +
            "                                 AND es.isPrimarySkill = true " +
            "       LEFT JOIN Skill s ON s.id = es.skillId AND s.tenantId = es.skillTenantId " +
            "       LEFT JOIN EmployeeTeam het ON e.id = het.employeeId AND e.tenantId = het.employeeTenantId " +
            "                                 AND het.isHomeTeam = true " +
            "       LEFT JOIN Team ht ON ht.id = het.teamId AND ht.tenantId = het.teamTenantId " +
            " WHERE et.tenantId = :tenantId " +
                    "   AND e.id NOT IN (SELECT employeeId FROM EmployeeTeam WHERE teamId = :teamId) " +
                    "   AND " + QueryPattern.NOT_DELETED.val("e");

        if (StringUtils.isNotBlank(filter)) {
            String[] filters = filter.split(";");
            for (String filterItem : filters) {
                if (filterItem.startsWith("EmployeeTeam.")) {
                    filterItem = filterItem.replaceFirst("EmployeeTeam.", "het.");
                } else if (filterItem.startsWith("Employee.")) {
                    filterItem = filterItem.replaceFirst("Employee.", "e.");
                } else if (filterItem.startsWith("EmployeeSkill.")) {
                    filterItem = filterItem.replaceFirst("EmployeeSkill.", "es.");
                } else if (filterItem.startsWith("Skill.")) {
                    filterItem = filterItem.replaceFirst("Skill.", "s.");
                } else if (filterItem.startsWith("Team.")) {
                    filterItem = filterItem.replaceFirst("Team.", "ht.");
                } else {
                    filterItem = "e." + filterItem;
                }
                sql += " AND " + filterItem + " ";
            }
        }

        Query countQuery = getEntityManager().createNativeQuery("SELECT count(*) FROM (" + sql + ") x");

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.startsWith("Employee.")) {
                orderBy = orderBy.replaceFirst("Employee.", "e.");
            } else if (orderBy.startsWith("Skill.")) {
                orderBy = orderBy.replaceFirst("Skill.", "s.");
            } else if (orderBy.startsWith("Team.")) {
                orderBy = orderBy.replaceFirst("Team.", "ht.");
            } else {
                orderBy = "e." + orderBy;
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", teamPrimaryKey.getTenantId());
        query.setParameter("teamId", teamPrimaryKey.getId());
        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        countQuery.setParameter("tenantId", teamPrimaryKey.getTenantId());
        countQuery.setParameter("teamId", teamPrimaryKey.getId());

        ResultSet<Object[]> result = new ResultSet<>();

        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

    public ResultSet<EmployeeTeam> getEmployeeTeams(PrimaryKey teamPrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery, QueryPattern.NOT_DELETED.val("employee"));
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, teamPrimaryKey, Team.class,
                "employeeTeams");
    }

    public ResultSet<Employee> getUnassociatedTeamEmployees(PrimaryKey teamPrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);
        return queryHelper.executeGetUnassociatedWithPaging(getEntityManager(), simpleQuery, teamPrimaryKey, Team.class,
                "employeeTeams", "employee");
    }

    public ResultSet<Schedule> getSchedules(PrimaryKey primaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, primaryKey, Team.class, "schedules");
    }

    public ResultSet<Skill> getUnassociatedSkills(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetUnassociatedWithPaging(getEntityManager(), simpleQuery, primaryKey, Team.class,
                "skills");
    }

    public ResultSet<Site> getUnassociatedSites(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);
        return queryHelper.executeGetUnassociatedWithPaging(getEntityManager(), simpleQuery, primaryKey, Team.class,
                "srcRels", "src");
    }

    public void checkTeamNameUnicityOnSite(PrimaryKey sitePrimaryKey, String teamName, String teamId) {
        String sql =
            "SELECT t.id FROM Team t " +
            "  JOIN AOMRelationship r ON r.dst_id = t.id AND t.tenantId = r.src_tenantId " +
            "  JOIN Site s ON s.id = r.src_id AND s.tenantId = r.src_tenantId " +
            " WHERE s.tenantId = :tenantId AND s.id = :siteId AND t.name = :teamName " +
                    (StringUtils.isEmpty(teamId) ? "" : " AND t.id != :teamId ")
                    + " AND " + QueryPattern.NOT_DELETED.val("t") +
            " LIMIT 1 ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", sitePrimaryKey.getTenantId());
        query.setParameter("siteId", sitePrimaryKey.getId());
        query.setParameter("teamName", teamName);
        if (StringUtils.isNotEmpty(teamId)) {
            query.setParameter("teamId", teamId);
        }

        if (!query.getResultList().isEmpty()) {
            throw new ValidationException(sessionService.getMessage("validation.team.name.notunique", teamName,
                    sitePrimaryKey.getId()));
        }
    }

    private Collection<Shift> findShiftsWithTeam(Team team) {
        SimpleQuery shiftSimpleQuery = new SimpleQuery(team.getTenantId());
        shiftSimpleQuery.setEntityClass(Shift.class);
        shiftSimpleQuery.addFilter("teamId = '" + team.getId() + "'");

        return new SimpleQueryHelper().executeSimpleQuery(getEntityManager(), shiftSimpleQuery);
    }

    private Collection findEntitiesWithTeam(Team team, String name) {
        Query query = getEntityManager().createQuery("SELECT e FROM " + name + " e WHERE e.team = :team");
        query.setParameter("team", team);
        return query.getResultList();
    }
}
