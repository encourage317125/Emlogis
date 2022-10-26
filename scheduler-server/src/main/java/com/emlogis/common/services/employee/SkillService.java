package com.emlogis.common.services.employee;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SkillService {

	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
    
	@EJB
	private SiteService siteService;

	@EJB
	private TeamService teamService;

	@EJB
	private SessionService sessionService;

	/**
	 * findSkills() find a list of Skills matching criteria;
	 * @param sq
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<Skill> findSkills(SimpleQuery sq) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException{
		sq.setEntityClass(Skill.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(entityManager, sq);
	}

	/**
	 * Get skill 
	 * @param primaryKey
	 * @return
	 */
	public Skill getSkill(PrimaryKey primaryKey) {
		return entityManager.find(Skill.class, primaryKey);
	}

	/**
	 * Create skill
	 * @param pk
	 * @param name
	 * @param abbreviation
	 * @param description
	 * @return
	 */
    public Skill createSkill(PrimaryKey pk, String name, String abbreviation, String description) {
		Skill skill = new Skill(pk, name, abbreviation, description);
		entityManager.persist(skill);
		return skill;
	}

    /**
     * Update skill
     * @param skill
     * @return
     */
    public Skill update(Skill skill) {
        skill.touch();
        return entityManager.merge(skill);
    }

    /**
     * Delete skill
     * @param skill
     */
    public void delete(Skill skill) {
        if (findEntitiesWithSkill(skill, "ShiftPattern").size() > 0
                || findEntitiesWithSkill(skill, "EmployeeSkill").size() > 0
                || findEntitiesWithSkill(skill, "ShiftPattern").size() > 0
                || findEntitiesWithSkill(skill, "ShiftReqOld").size() > 0
                || findShiftsWithSkill(skill).size() > 0) {
            throw new ValidationException(sessionService.getMessage("entity.constraint.violation"));
        } else {
            entityManager.remove(skill);
        }
    }

    public List<Object[]> getSiteTeamAssociations(PrimaryKey primaryKey) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String sql =
                "SELECT t.id teamId, t.name teamName, t.description, s.id siteId, s.name siteName, " +
                "  CASE " +
                "    WHEN ts.teams_id IS NULL THEN 'false' " +
                "    ELSE 'true' " +
                "  END teamHasSkill " +
                " FROM Team t " +
                "    LEFT JOIN AOMRelationship r ON r.dst_id = t.id AND t.tenantId = r.dst_tenantId " +
                "    LEFT JOIN Site s ON r.src_id = s.id AND s.tenantId = r.src_tenantId " +
                        "       AND " + QueryPattern.NOT_DELETED.val("s") +
                "    LEFT JOIN Team_Skill ts ON t.id = ts.teams_id AND t.tenantId = ts.teams_tenantId " +
                "                               AND ts.skills_id = :skillId " +
                "    LEFT JOIN Skill sk ON sk.id = ts.skills_id AND sk.tenantId = ts.skills_tenantId " +
                        " WHERE t.tenantId = :tenantId "
                        + " AND " + QueryPattern.NOT_DELETED.val("t");

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("skillId", primaryKey.getId());
        query.setParameter("tenantId", primaryKey.getTenantId());

        return query.getResultList();
    }

    public List<Object[]> getTeamAssociations(PrimaryKey primaryKey) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String sql =
                "SELECT t.id teamId, t.name teamName, s.id siteId, s.name siteName " +
                "  FROM Team_Skill ts " +
                "          LEFT JOIN Team t ON t.id = ts.teams_id AND t.tenantId = ts.teams_tenantId " +
                "          LEFT JOIN AOMRelationship r ON r.dst_id = t.id AND t.tenantId = r.dst_tenantId " +
                "          LEFT JOIN Site s ON r.src_id = s.id AND s.tenantId = r.src_tenantId " +
                        " WHERE ts.skills_id = :skillId AND ts.skills_tenantId = :tenantId "
                        + " AND " + QueryPattern.NOT_DELETED.val("t")
                        + " AND " + QueryPattern.NOT_DELETED.val("s");

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("skillId", primaryKey.getId());
        query.setParameter("tenantId", primaryKey.getTenantId());

        return query.getResultList();
    }

    /**
     * Get sites associated with skill
     * @param skillPrimaryKey
     * @return
     */
    @Deprecated  // Not part of originally prescribed API, but leaving in case it proves useful.
    public Collection<Site> getSites(PrimaryKey skillPrimaryKey) {
    	Skill skill = entityManager.find(Skill.class, skillPrimaryKey);
        
        if (skill != null) {
        	return skill.getSites();
        } else {
        	return null;
        }
    }

    /**
     * Get teams associated with skill
     * @param skillPrimaryKey
     * @return
     */
    @Deprecated  // Not part of originally prescribed API, but leaving in case it proves useful.
    public Collection<Team> getTeams(PrimaryKey skillPrimaryKey) {
    	Skill skill = entityManager.find(Skill.class, skillPrimaryKey);
        
        if (skill != null) {
        	return skill.getTeams();
        } else {
        	return null;
        }
    }

    private Collection<Shift> findShiftsWithSkill(Skill skill) {
        SimpleQuery shiftSimpleQuery = new SimpleQuery(skill.getTenantId());
        shiftSimpleQuery.setEntityClass(Shift.class);
        shiftSimpleQuery.addFilter("skillId = '" + skill.getId() + "'");

        return new SimpleQueryHelper().executeSimpleQuery(entityManager, shiftSimpleQuery);
    }

    private Collection findEntitiesWithSkill(Skill skill, String name) {
        Query query = entityManager.createQuery("SELECT e FROM " + name + " e WHERE e.skill = :skill");
        query.setParameter("skill", skill);
        return query.getResultList();
    }
}

