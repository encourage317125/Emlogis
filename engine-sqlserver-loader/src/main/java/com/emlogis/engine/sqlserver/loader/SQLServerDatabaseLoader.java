package com.emlogis.engine.sqlserver.loader;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.joda.time.LocalDate;

import com.emlogis.engine.sqlserver.loader.domain.EmployeeContractLine;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeIDToTeamID;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeSkill;
import com.emlogis.engine.sqlserver.loader.domain.ProductionShiftSummary;
import com.emlogis.engine.sqlserver.loader.domain.ShiftDemand;
import com.emlogis.engine.sqlserver.loader.domain.SiteContractLine;
import com.emlogis.engine.sqlserver.loader.domain.T_Employee;
import com.emlogis.engine.sqlserver.loader.domain.T_EmployeeCIRotation;
import com.emlogis.engine.sqlserver.loader.domain.T_EmployeeWeekend;
import com.emlogis.engine.sqlserver.loader.domain.T_Site;
import com.emlogis.engine.sqlserver.loader.domain.T_SiteResource;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCDAvailability;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCDPreference;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCIAvailability;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCIPreference;
import com.emlogis.engine.sqlserver.loader.exception.NullEntityManagerException;

public class SQLServerDatabaseLoader {
	// Database connection plumbing
	private EntityManager em;
	private boolean keepEntityManager=false;
	
	public boolean isKeepEntityManager() {
		return keepEntityManager;
	}

	public void setKeepEntityManager(boolean keepEntityManager) {
		this.keepEntityManager = keepEntityManager;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!keepEntityManager) {
			em.close();
		}
		super.finalize();
	}
	
	public List<T_SiteResource> loadEmployeeConstraintOverrides(long scheduleId) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_SiteResource> constraintOverrideQuery = em
				.createQuery("select rtoConstraint from T_SiteResource as rtoConstraint "
						+ " where SiteScheduleID = ?1 AND RTOBinaryTally != 0",
						T_SiteResource.class).setParameter(1, scheduleId);
		
		return constraintOverrideQuery.getResultList();
	
	}
	
	public List<T_Employee> loadEmployeesForTeam(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_Employee> employeeQuery = em
				.createQuery("select employee from T_Employee as employee "
						+ " where IsSchedulable = 1 AND IsActive = 1 AND employee.employeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?1))",
						T_Employee.class).setParameter(1, teamIds);
		
		return employeeQuery.getResultList();
	}
	
	public List<T_EmployeeCIAvailability> loadTeamCIAvailability(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_EmployeeCIAvailability> ciAvailabilityQuery = em
				.createQuery("select ciAvailability from T_EmployeeCIAvailability as ciAvailability "
						+ "WHERE EmployeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?1))",
						T_EmployeeCIAvailability.class).
						setParameter(1, teamIds); //TODO: Change to named parameters
		return ciAvailabilityQuery.getResultList();
	}
	
	public List<T_EmployeeCIPreference> loadTeamCIPreference(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_EmployeeCIPreference> ciPreferenceQuery = em
				.createQuery("select ciPreference from T_EmployeeCIPreference as ciPreference "
						+ "WHERE EmployeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?1))",
						T_EmployeeCIPreference.class).
						setParameter(1, teamIds); //TODO: Change to named parameters
		return ciPreferenceQuery.getResultList();
	}
	
	public List<T_EmployeeCDAvailability> loadTeamCDAvailability(List<Long> teamIds, LocalDate planningWindowStart, LocalDate planningWindowEnd) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_EmployeeCDAvailability> cdAvailabilityQuery = em
				.createQuery("select cdAvailability from T_EmployeeCDAvailability as cdAvailability "
						+ " where AvailabilityDate >= ?1 and AvailabilityDate <= ?2 AND"
						+ " EmployeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?3))",
						T_EmployeeCDAvailability.class).
						setParameter(1, planningWindowStart.toDate(), TemporalType.TIMESTAMP).
						setParameter(2, planningWindowEnd.toDate(),   TemporalType.TIMESTAMP).
						setParameter(3, teamIds);
		return cdAvailabilityQuery.getResultList();
	}
	
	public List<T_EmployeeCDPreference> loadTeamCDPreference(List<Long> teamIds, LocalDate planningWindowStart, LocalDate planningWindowEnd) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_EmployeeCDPreference> cdPreferenceQuery = em
				.createQuery("select cdPreference from T_EmployeeCDPreference as cdPreference "
						+ " where PreferenceDate >= ?1 and PreferenceDate <= ?2 AND"
						+ " EmployeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?3))",
						T_EmployeeCDPreference.class).
						setParameter(1, planningWindowStart.toDate(), TemporalType.TIMESTAMP).
						setParameter(2, planningWindowEnd.toDate(),   TemporalType.TIMESTAMP).
						setParameter(3, teamIds);
		return cdPreferenceQuery.getResultList();
	}
	

	public List<T_EmployeeCIRotation> loadTeamCIRotation(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_EmployeeCIRotation> ciRotationQuery = em
				.createQuery("select ciRotation from T_EmployeeCIRotation as ciRotation "
						+ " WHERE EmployeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?1))",
						T_EmployeeCIRotation.class).
						setParameter(1, teamIds);
		return ciRotationQuery.getResultList();
	}
	
	public List<T_EmployeeWeekend> loadTeamWeekendOptions(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_EmployeeWeekend> empWeekendOptions = em
				.createQuery("select weekendOptions from T_EmployeeWeekend as weekendOptions "
						+ " WHERE EmployeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?1))",
						T_EmployeeWeekend.class).
						setParameter(1, teamIds);
		return empWeekendOptions.getResultList();
	}
	
	public List<EmployeeContractLine> loadTeamEmployeeContractLines(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<EmployeeContractLine> employeeContractLineQuery = em
				.createQuery("select empContractLine from EmployeeContractLine as empContractLine "
						+ "WHERE employeeID in ( SELECT employeeID FROM EmployeeIDToTeamID"
						+ " WHERE TeamID IN (?1))",
						EmployeeContractLine.class).
						setParameter(1, teamIds); //TODO: Change to named parameters
		return employeeContractLineQuery.getResultList();
	}
	
	public List<SiteContractLine> loadSiteContractLines(int siteId) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<SiteContractLine> siteContractLineQuery = em
				.createQuery("select siteContractLine from SiteContractLine as siteContractLine "
						+ "WHERE SiteID = ?",
						SiteContractLine.class).
						setParameter(1, siteId); //TODO: Change to named parameters
		return siteContractLineQuery.getResultList();
	}
	
	public List<ShiftDemand> loadTeamScheduleShiftDemand(List<Long> teamIds, LocalDate planningWindowStart, LocalDate planningWindowEnd) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		
		TypedQuery<ShiftDemand> scheduleShiftDemand = em
				.createQuery("select shiftDemand from ShiftDemand as shiftDemand "
						+ " where Date >= ?1 and Date <= ?2 AND"
						+ " TeamID IN (?3))",
						ShiftDemand.class).
						setParameter(1, planningWindowStart.toDate(), TemporalType.TIMESTAMP).
						setParameter(2, planningWindowEnd.toDate(),   TemporalType.TIMESTAMP).
						setParameter(3, teamIds);
		return scheduleShiftDemand.getResultList();
	}

	public List<EmployeeSkill> loadTeamEmployeeSkills(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<EmployeeSkill> employeeSkillsQuery = em
				.createQuery("select employeeSkill from opta_EmployeeSkill as employeeSkill "
						+ "WHERE TeamID IN (?1)",
						EmployeeSkill.class).
						setParameter(1, teamIds); //TODO: Change to named parameters
		return employeeSkillsQuery.getResultList();
	}
	
	public List<EmployeeIDToTeamID> loadEmployeeToTeamRelationships(List<Long> teamIds) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<EmployeeIDToTeamID> employeeToTeamQuery = em
				.createQuery("select employeeToTeam from EmployeeIDToTeamID as employeeToTeam "
						+ "WHERE TeamID IN (?1)",
						EmployeeIDToTeamID.class).
						setParameter(1, teamIds); //TODO: Change to named parameters
		return employeeToTeamQuery.getResultList();
	}
	
	public T_Site loadSiteInformation(int siteId) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		TypedQuery<T_Site> employeeToTeamQuery = em
				.createQuery("select site from T_Site as site "
						+ "WHERE SiteID = ? ",
						T_Site.class).
						setParameter(1, siteId); //TODO: Change to named parameters
		return employeeToTeamQuery.getSingleResult();
	}
	
	public List<ProductionShiftSummary> loadEmployeePostedShiftAssignments(Long employeeID, LocalDate planningWindowStart, LocalDate planningWindowEnd) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		
		TypedQuery<ProductionShiftSummary> employeeProductionShiftSummary = em
				.createQuery("select productionShiftSummary from ProductionShiftSummary as productionShiftSummary "
						+ " where ShiftDate >= ? and ShiftDate <= ? AND"
						+ " EmployeeID = ?)",
						ProductionShiftSummary.class).
						setParameter(1, planningWindowStart.toDate(), TemporalType.TIMESTAMP).
						setParameter(2, planningWindowEnd.toDate(),   TemporalType.TIMESTAMP).
						setParameter(3, employeeID);
		return employeeProductionShiftSummary.getResultList();
	}
	
	public List<ProductionShiftSummary> loadEmployeePostedShiftAssignments(Long employeeID, List<Date> dateList) throws NullEntityManagerException{
		if(em == null){
			throw new NullEntityManagerException();
		}
		
		TypedQuery<ProductionShiftSummary> employeeProductionShiftSummary = em
				.createQuery("select productionShiftSummary from ProductionShiftSummary as productionShiftSummary "
						+ " where ShiftDate IN :dateList AND"
						+ " EmployeeID = :empID)",
						ProductionShiftSummary.class).
						setParameter("dateList", dateList).
						setParameter("empID", employeeID);
		return employeeProductionShiftSummary.getResultList();
	}
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
}
