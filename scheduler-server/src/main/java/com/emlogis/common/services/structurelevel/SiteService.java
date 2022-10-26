package com.emlogis.common.services.structurelevel;

import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.shiftpattern.ShiftLengthService;
import com.emlogis.common.services.shiftpattern.ShiftTypeService;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.aom.AOMRelationshipDef;
import com.emlogis.model.contract.ContractLine;
import com.emlogis.model.contract.IntMinMaxCL;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.structurelevel.*;
import com.emlogis.model.tenant.settings.SchedulingSettings;
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
import java.util.*;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SiteService extends StructureLevelService<Site> {

	 @EJB
	 private ShiftTypeService shiftTypeService;

	 @EJB
	 private ShiftLengthService shiftLengthService;

	 @EJB
	 private SkillService skillService;

	 @EJB
	 private TeamService teamService;

	 @EJB
	 private SessionService sessionService;

	 /**
	 * findSites() find a list of Sites matching criteria;
	 * @param simpleQuery
	 * @param acl
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<Site> findSites(SimpleQuery simpleQuery, AccountACL acl) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		simpleQuery.setEntityClass(Site.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		simpleQuery.setAcl(acl);
        return sqh.executeSimpleQueryWithPaging(getEntityManager(), simpleQuery);
    }
	
	public Site getSite(PrimaryKey primaryKey) {
		return getEntityManager().find(Site.class, primaryKey);
	}
		
	public Site createSite(PrimaryKey pk) {
		Site site = new Site(pk);
		
        SchedulingSettings schedulingSettings = new SchedulingSettings(new PrimaryKey(pk.getTenantId()));
		insert(site);
        site.setSchedulingSettings(schedulingSettings);
        insertSchedulingSettings(schedulingSettings);
        createPostOverrides(site, "default", null);
        site.setShiftDropReasons(createShiftDropReasonDefaultSet(site));
		return site;
	}

    public void softDelete(Site site) {
    	List<Team> teams = getTeams(site);
    	if (teams == null || teams.size() > 0) {
        	throw new ValidationException(sessionService.getMessage("site.constraint.violation"));	
    	} else {
        	String dateTimeString = new DateTime().toString();
        	site.setName(site.getName() + dateTimeString);
        	site.setIsDeleted(true);
        	
        	Set<Skill> skills = site.getSkills();
        	for (Skill skill : skills){
        		site.removeSkill(skill);
        	}
        	
    		this.update(site);    		
    	}
    }

    public void hardDelete(Site site) {
    	deleteSchedulingSettings(site);
        deleteEntities(site.getEmployees());

        List<Team> teams = getTeams(site);
        for (Team team : teams) {
            teamService.hardDelete(team);
        }

        deleteEntities(site.getAbsenceTypes());
        deleteEntities(site.getSiteContracts());

        deleteEntity(site);
    }

    /* (non-Javadoc)
	 * @see com.emlogis.common.services.structurelevel.StructureLevelService#delete(com.emlogis.model.structurelevel.StructureLevel)
	 */
    @Deprecated
	@Override
	public void delete(Site site) {
    	// Only soft deletes are supported for Sites, 
    	// so deprecated and delegated to softDelete
		softDelete(site);
	}

	/**
     * Get collection of Skills for Site specified by PrimaryKey
     * @param sitePrimaryKey
     * @param simpleQuery
     * @return ResultSet
     */
    public ResultSet<Skill> getSkills(PrimaryKey sitePrimaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, sitePrimaryKey, Site.class,
                "skills");
	}
    
    /**
     * Add Skill specified by id to Site specified by PrimaryKey
     * @param sitePrimaryKey
     * @param skillId
     */
	public void addSkill(PrimaryKey sitePrimaryKey, String skillId) {
		Site site = getSite(sitePrimaryKey);
        if (site != null) {
            PrimaryKey skillPrimaryKey = new PrimaryKey(sitePrimaryKey.getTenantId(), skillId);
            Skill skill = skillService.getSkill(skillPrimaryKey);

            if (skill != null) {
            	site.addSkill(skill);
            	site.touch();
            	getEntityManager().persist(site);
            }
        }
    }

	/**
	 * Remove Skill specified by id to Site specified by PrimaryKey
	 * @param sitePrimaryKey
	 * @param skillId
	 */
	public void removeSkill(PrimaryKey sitePrimaryKey, String skillId) {
		Site site = getSite(sitePrimaryKey);
		if (site != null) {
			PrimaryKey skillPrimaryKey = new PrimaryKey(sitePrimaryKey.getTenantId(), skillId);
			Skill skill = skillService.getSkill(skillPrimaryKey);

			// As in Aspen, a Skill can't be removed from a parent Site until it has
			// first been removed from any/all Teams under the Site. This isn't done
			// automatically, so the end user would have to perform those removals 
			// first before proceeding to remove from the parent Site.
			List<Team> teams = getTeams(site);
			for (Team team : teams) {
				if (team.getSkills().contains(skill)) {
					throw new ValidationException(sessionService.getMessage("validation.skill.remove.error"));
				}
			}

			if (skill != null && site.getSkills().contains(skill)) {
				site.removeSkill(skill);
				site.touch();
				getEntityManager().persist(site);
			}
		}
	}    
    
    /**
     * Returns a List of Teams associated with this Site
     * @param site
     * @return
     */
    public List<Team> getTeams(Site site) {
    	List<Team> result = new ArrayList<>();

        List<StructureLevel> levels = getAssociatedObjects(site, AOMRelationshipDef.SITE_TEAM_REL,
                TraversalDirection.OUT);
        for (StructureLevel team : levels){
            Team castedTeam  = (Team) team;
            if(!castedTeam.getIsDeleted()) {
                result.add(castedTeam);
            }
    	}
    	
    	return result;
    }

    public ResultSet<Team> getTeams(PrimaryKey primaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, primaryKey, Site.class,
                "dstRels", "dst");
    }

    public ResultSet<Skill> getUnassociatedSkills(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetUnassociatedWithPaging(getEntityManager(), simpleQuery, primaryKey, Site.class,
                "skills");
    }

    public List<Object[]> siteTeamSkills(
            String tenantId,
            String searchValue,
            String searchFields,
            String siteFilter,
            String teamFilter,
            String skillFilter,
            AccountACL acl) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String aceIds = acl == null ? null : ModelUtils.commaSeparatedQuotedValues(acl.getAceIds());
        String sql =
                "SELECT st.id siteId, st.name siteName, t.id teamId, t.name teamName, s.id skillId, s.name skillName " +
                "  FROM Site st " +
                "    LEFT JOIN AOMRelationship ar ON st.id = ar.src_id AND st.tenantId = ar.tenantId " +
                        "       AND ar.type = 'Site_Team' " +
                        "    LEFT JOIN Team t ON ar.dst_id = t.id AND ar.tenantId = t.tenantId " +
                        "       AND " + QueryPattern.NOT_DELETED.val("t") +
                "    LEFT JOIN Team_Skill ts ON t.id = ts.teams_id AND t.tenantId = ts.teams_tenantId " +
                "    LEFT JOIN Skill s ON ts.skills_id = s.id AND ts.skills_tenantId = s.tenantId " +
                (StringUtils.isEmpty(aceIds) ? "" : ", ACE ace ") +
                " WHERE st.tenantId = :tenantId " +
                (StringUtils.isEmpty(aceIds) ? "" :
                "   AND ace.id IN (" + aceIds + ") " +
                "   AND (t.path IS NULL AND st.path REGEXP ace.pattern AND ace.entityClass = 'Site' " +
                        "     OR t.path REGEXP ace.pattern AND ace.entityClass = 'Team') ") +
                        "   AND " + QueryPattern.NOT_DELETED.val("st");

        if (StringUtils.isNotBlank(siteFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(siteFilter, "st") + ") ";
        }
        if (StringUtils.isNotBlank(siteFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(teamFilter, "t") + ") ";
        }
        if (StringUtils.isNotBlank(skillFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(skillFilter, "s") + ") ";
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);

        return query.getResultList();
    }

    public SchedulingSettings createSchedulingSettings(Site site) {
        SchedulingSettings schedulingSettings = new SchedulingSettings(new PrimaryKey(site.getTenantId()));
        site.setSchedulingSettings(schedulingSettings);
    	getEntityManager().persist(schedulingSettings);
    	return schedulingSettings;
    }

    public SchedulingSettings updateSchedulingSettings(SchedulingSettings schedulingSettings) {
        return getEntityManager().merge(schedulingSettings);
    }

    public void deleteSchedulingSettings(Site site) {
    	if (site.getSchedulingSettings() != null) {
    		getEntityManager().remove(site.getSchedulingSettings());
    		site.setSchedulingSettings(null);
    	}
    }

    public List<Object[]> siteTeams(String tenantId, AccountACL acl) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String aceIds = acl == null ? null : ModelUtils.commaSeparatedQuotedValues(acl.getAceIds());

        String sql =
            "SELECT DISTINCT t.id teamId, t.name teamName, s.id siteId, s.name siteName " +
            "  FROM Site s LEFT JOIN AOMRelationship r ON r.src_id = s.id AND s.tenantId = r.src_tenantId " +
            "              LEFT JOIN Team t ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            (StringUtils.isEmpty(aceIds) ? "" : ", ACE ace ") +
            " WHERE s.tenantId = :tenantId " +
            (StringUtils.isEmpty(aceIds) ? "" :
            "   AND ace.id IN (" + aceIds + ") " +
            "   AND (t.path IS NULL AND s.path REGEXP ace.pattern AND ace.entityClass = 'Site' " +
                    "     OR t.path REGEXP ace.pattern AND ace.entityClass = 'Team') ") +
                    "   AND " + QueryPattern.NOT_DELETED.val("t");

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);

        return query.getResultList();
    }

    public List<Object[]> siteSchedules(String tenantId, Long startDate, AccountACL acl) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String aceIds = acl == null ? null : ModelUtils.commaSeparatedQuotedValues(acl.getAceIds());

        String sql =
            "SELECT DISTINCT sc.id scheduleId, sc.name scheduleName, s.id siteId, s.name siteName " +
            "  FROM Site s LEFT JOIN AOMRelationship r ON r.src_id = s.id AND s.tenantId = r.src_tenantId " +
            "              LEFT JOIN Team t ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "              LEFT JOIN Team_Schedule ts ON t.id = ts.Team_id AND t.tenantId = ts.Team_tenantId " +
            "              LEFT JOIN Schedule sc ON sc.id = ts.schedules_id AND sc.tenantId = ts.schedules_tenantId " +
            "                    AND DATE(sc.startDate) = DATE(:startDate) " +
            (StringUtils.isEmpty(aceIds) ? "" : ", ACE ace ") +
            " WHERE s.tenantId = :tenantId " +
            (StringUtils.isEmpty(aceIds) ? "" :
            "   AND ace.id IN (" + aceIds + ") " +
            "   AND (t.path IS NULL AND s.path REGEXP ace.pattern AND ace.entityClass = 'Site' " +
            "     OR t.path REGEXP ace.pattern AND ace.entityClass = 'Team') ");

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setParameter("startDate", new Timestamp(startDate == null ? 0 : startDate));

        return query.getResultList();
    }

    public List<Object[]> schedules(String siteId, Long startDate, AccountACL acl, String filter) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String aceIds = acl == null ? null : ModelUtils.commaSeparatedQuotedValues(acl.getAceIds());

        String sql =
            "SELECT DISTINCT sc.id scheduleId, sc.name scheduleName " +
            "  FROM Site s LEFT JOIN AOMRelationship r ON r.src_id = s.id AND s.tenantId = r.src_tenantId " +
            "              LEFT JOIN Team t ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "              LEFT JOIN Team_Schedule ts ON t.id = ts.Team_id AND t.tenantId = ts.Team_tenantId " +
            "              LEFT JOIN Schedule sc ON sc.id = ts.schedules_id AND sc.tenantId = ts.schedules_tenantId " +
            "                    AND DATE(sc.startDate) <= DATE(:startDate) " +
            "                    AND DATE_ADD(sc.startDate, INTERVAL sc.scheduleLengthInDays DAY ) > DATE(:startDate) " +
            (StringUtils.isEmpty(aceIds) ? "" : ", ACE ace ") +
            " WHERE s.id = :siteId " +
            (StringUtils.isEmpty(aceIds) ? "" :
            "   AND ace.id IN (" + aceIds + ") " +
            "   AND (t.path IS NULL AND s.path REGEXP ace.pattern AND ace.entityClass = 'Site' " +
            "     OR t.path REGEXP ace.pattern AND ace.entityClass = 'Team') ") +
                    (StringUtils.isNotBlank(filter) ? " AND " + SimpleQueryHelper.buildFilterClause(filter, "sc") : "");

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("siteId", siteId);
        query.setParameter("startDate", new Timestamp(startDate == null ? 0 : startDate));

        return query.getResultList();
    }

    public List<Object[]> siteOvertimesInfo(String siteId) {
        String sql =
            "SELECT  " +
            "    immcl.contractLineType " +
            "  , CASE  " +
            "          WHEN immcl.maximumEnabled THEN immcl.maximumValue " +
            "          WHEN immcl.minimumEnabled THEN immcl.minimumValue " +
            "          ELSE -1  " +
            "    END AS norma " +
            "  FROM SiteContract sc  " +
            "    LEFT JOIN IntMinMaxCL immcl ON sc.id = immcl.contractId AND sc.tenantId = immcl.contractTenantId " +
            " WHERE  " +
            "       immcl.contractLineType IN ('DAILY_OVERTIME', 'WEEKLY_OVERTIME', 'TWO_WEEK_OVERTIME') " +
            "   AND sc.id = :siteId ";
        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("siteId", siteId);

        return query.getResultList();
    }

    public ResultSet<Schedule> schedulesForSite(PrimaryKey sitePrimaryKey, String filter, Integer offset,
                                                Integer limit, String orderBy, String orderDir, AccountACL acl)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String aceIds = acl == null ? null : ModelUtils.commaSeparatedQuotedValues(acl.getAceIds());

        String sql =
            "SELECT DISTINCT sc.* " +
            "  FROM Site s LEFT JOIN AOMRelationship r ON r.src_id = s.id AND s.tenantId = r.src_tenantId " +
            "              LEFT JOIN Team t ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "              LEFT JOIN Team_Schedule ts ON t.id = ts.Team_id AND t.tenantId = ts.Team_tenantId " +
            "              LEFT JOIN Schedule sc ON sc.id = ts.schedules_id AND sc.tenantId = ts.schedules_tenantId " +
            (StringUtils.isEmpty(aceIds) ? "" : ", ACE ace ") +
            " WHERE s.id = :siteId AND s.tenantId = :tenantId AND sc.id IS NOT NULL " +
            (StringUtils.isEmpty(aceIds) ? "" :
            "   AND ace.id IN (" + aceIds + ") " +
            "   AND (t.path IS NULL AND s.path REGEXP ace.pattern AND ace.entityClass = 'Site' " +
            "     OR t.path REGEXP ace.pattern AND ace.entityClass = 'Team') ");

        String countSql = "SELECT count(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY sc." + orderBy + (StringUtils.isBlank(orderDir) ? "" : " " + orderDir);
        }

        Query query = getEntityManager().createNativeQuery(sql, Schedule.class);
        query.setParameter("siteId", sitePrimaryKey.getId());
        query.setParameter("tenantId", sitePrimaryKey.getTenantId());
        if (offset != null && offset >= 0) {
            query.setFirstResult(offset);
        }
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        Query countQuery = getEntityManager().createNativeQuery(countSql);
        countQuery.setParameter("siteId", sitePrimaryKey.getId());
        countQuery.setParameter("tenantId", sitePrimaryKey.getTenantId());
        BigInteger total = (BigInteger) countQuery.getSingleResult();

        ResultSet<Schedule> resultSet = new ResultSet<>();

        resultSet.setResult(query.getResultList());
        resultSet.setTotal(total.intValue());

        return resultSet;
    }

    public PostOverrides createPostOverrides(Site site, String name,
                                             Map<ConstraintOverrideType, Boolean> overrideOptions) {
    	PostOverrides postOverrides = getPostOverrides(site, name);
    	if (postOverrides != null) {
        	throw new ValidationException("site.postoverrides.alreadyexist");	
    	}
    	
    	postOverrides = new PostOverrides(new PrimaryKey(site.getTenantId()));
    	postOverrides.setName(name);
    	if (overrideOptions != null) {
    		postOverrides.setOverrideOptions(overrideOptions);
    	} else {
    		postOverrides.setOverrideOptions(PostOverrides.getDefaultOverrides());    		
    	}
    	postOverrides.setSite(site);
        if (site.getPostOverrides() == null) {
            site.setPostOverrides(new HashSet<PostOverrides>());
        }
    	site.getPostOverrides().add(postOverrides);
    	getEntityManager().persist(postOverrides);
    	return postOverrides;
    }
    
    public ResultSet<PostOverrides> getPostOverrides(Site site) {
    	SimpleQuery sq = new SimpleQuery(site.getTenantId());
		sq.setEntityClass(PostOverrides.class)
			.addFilter("site.primaryKey.id = '" + site.getId() + "'")
			.setOrderByField("name")
			.setOrderAscending(true);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeSimpleQueryWithPaging(getEntityManager(), sq);
    }

    public PostOverrides getPostOverrides(Site site, String name) {
    	SimpleQuery sq = new SimpleQuery(site.getTenantId());
		sq.setEntityClass(PostOverrides.class);
		sq.addFilter("site.primaryKey.id = '" + site.getId() + "'");
		sq.addFilter("name = '" + name + "'");
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		Collection<PostOverrides> postOverrides  = sqh.executeSimpleQuery(getEntityManager(), sq);
		switch (postOverrides.size()) {
            case 0:
                return null;
            case 1:
                return postOverrides.iterator().next();
            default:
                // SHOULD BE 1 0r 0
                // TODO throw exception if more than 1
                return null;
		}
    }

    public PostOverrides updatePostOverrides(Site site, String name,
                                             Map<ConstraintOverrideType, Boolean> overrideOptions) {
    	PostOverrides postOverrides = getPostOverrides(site, name);
    	if (postOverrides == null) {
    		// no PostOverrides found with that name, let's create a new one
    		return createPostOverrides(site, name,  overrideOptions);
    	} else {
	    	if (overrideOptions != null) {
	    		postOverrides.setOverrideOptions(overrideOptions);
	    	} else {
	    		postOverrides.setOverrideOptions(PostOverrides.getDefaultOverrides());    		
	    	}
            return getEntityManager().merge(postOverrides);
    	}
    }

    public void deletePostOverrides(Site site, String name) {
    	PostOverrides postOverrides = getPostOverrides(site, name);
    	if (postOverrides != null) {
    		postOverrides.setSite(null);
        	site.getPostOverrides().remove(postOverrides);
    		getEntityManager().remove(postOverrides);
    	}
    }

    public int countLengthOfShiftLengthForSite(int length, PrimaryKey sitePrimaryKey,
                                               PrimaryKey exceptShiftLengthPrimaryKey) {
        String sql = "SELECT count(*) FROM ShiftLength " +
                     " WHERE lengthInMin = :length AND siteId = :siteId AND siteTenantId = :tenantId ";
        if (exceptShiftLengthPrimaryKey != null) {
            sql += " AND id <> :shiftLengthId ";
        }

        Query query = getEntityManager().createNativeQuery(sql);

        query.setParameter("length", length);
        query.setParameter("siteId", sitePrimaryKey.getId());
        query.setParameter("tenantId", sitePrimaryKey.getTenantId());
        if (exceptShiftLengthPrimaryKey != null) {
            query.setParameter("shiftLengthId", exceptShiftLengthPrimaryKey.getId());
        }

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public ShiftLength getShiftLengthByLength(Site site, int length) {
        Set<ShiftLength> shiftLengths = site.getShiftLengths();
        for (ShiftLength shiftLength : shiftLengths) {
            if (shiftLength.getLengthInMin() == length) {
                return shiftLength;
            }
        }
        return null;
    }

    public Collection<IntMinMaxCL> getSiteIntMinMaxCLs(PrimaryKey teamPrimaryKey) {
        String sql =
            "SELECT imm.* FROM Team t " +
            "   LEFT JOIN AOMRelationship r ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "   LEFT JOIN Site s ON r.src_id = s.id AND r.src_tenantId = s.tenantId " +
            "   LEFT JOIN SiteContract cs ON s.id = cs.siteId AND s.tenantId = cs.siteTenantId " +
            "   LEFT JOIN IntMinMaxCL imm ON imm.contractId = cs.id AND imm.contractTenantId = cs.tenantId " +
            " WHERE t.id = :teamId AND t.tenantId = :tenantId " + " AND " + QueryPattern.NOT_DELETED.val("s");

        Query query = getEntityManager().createNativeQuery(sql, IntMinMaxCL.class);
        query.setParameter("teamId", teamPrimaryKey.getId());
        query.setParameter("tenantId", teamPrimaryKey.getTenantId());

        return query.getResultList();
    }

    public ResultSet<Object[]> getDropShiftReasonsAbsenceTypes(PrimaryKey sitePrimaryKey, String filter, int offset,
                                                               int limit, String orderBy, String orderDir) {
        String sql =
            "SELECT * FROM (" +
            "  SELECT id, name, description, timeToDeductInMin, siteId, " +
            "         CASE isActive " +
            "           WHEN 1 THEN TRUE " +
            "           WHEN 0 THEN FALSE " +
            "         END, " +
            "         'AbsenceType' " +
            "    FROM AbsenceType " +
            "  UNION " +
            "  SELECT id, reasonCode, description, -1, site_id, TRUE, 'ShiftDropReason' " +
            "    FROM ShiftDropReason " +
            ") x " +
            " WHERE siteId = :siteId " +
            (StringUtils.isEmpty(filter) ? "" : " AND " + filter);

        String countSql = "SELECT count(*) FROM (" + sql + ") xx";

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY " + orderBy + (StringUtils.isBlank(orderDir) ? "" : " " + orderDir);
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("siteId", sitePrimaryKey.getId());
        if (offset >= 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        Query countQuery = getEntityManager().createNativeQuery(countSql);
        countQuery.setParameter("siteId", sitePrimaryKey.getId());
        BigInteger total = (BigInteger) countQuery.getSingleResult();

        ResultSet<Object[]> result = new ResultSet<>();

        result.setResult(query.getResultList());
        result.setTotal(total.intValue());

        return result;
    }

    private void insertSchedulingSettings(SchedulingSettings schedulingSettings) {
        getEntityManager().persist(schedulingSettings);
    }

    private Set<ShiftDropReason> createShiftDropReasonDefaultSet(Site site) {
        Set<ShiftDropReason> result = new HashSet<>();

        result.add(createShiftDropReason(1, "No Show", site));
        result.add(createShiftDropReason(2, "Illness", site));
        result.add(createShiftDropReason(3, "Family Emergency", site));
        result.add(createShiftDropReason(4, "Car Problems", site));
        result.add(createShiftDropReason(5, "Weather", site));
        result.add(createShiftDropReason(6, "Reassigned", site));
        result.add(createShiftDropReason(7, "Other", site));

        return result;
    }

    private ShiftDropReason createShiftDropReason(int reasonCode, String description, Site site) {
        ShiftDropReason result = new ShiftDropReason(new PrimaryKey(site.getTenantId()));

        result.setReasonCode(reasonCode);
        result.setDescription(description);
        result.setSite(site);

        getEntityManager().persist(result);

        return result;
    }

}
