package com.emlogis.common.services.contract;

import com.emlogis.common.Constants;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.Contract;
import com.emlogis.model.contract.EmployeeContract;
import com.emlogis.model.contract.SiteContract;
import com.emlogis.model.contract.TeamContract;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationTargetException;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ContractService {
	
	@PersistenceContext(unitName= Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
	

	/**
	 * findContracts() find a list of contracts matching criteria;
	 * @param sq
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<Contract> findContracts(SimpleQuery sq) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException{
		sq.setEntityClass(Contract.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(entityManager, sq);
	}	
	
	/**
	 * Get contract 
	 * @param primaryKey
	 * @return
	 */
	public Contract getContract(PrimaryKey primaryKey) {
		return entityManager.find(Contract.class, primaryKey);
	}	
	
	/**
	 * Create Employee Contract 
	 * @param primaryKey
	 * @return
	 */
	
	public EmployeeContract createEmployeeContract(PrimaryKey primaryKey){
		EmployeeContract employeeContract = new EmployeeContract(primaryKey);
		entityManager.persist(employeeContract);
		return employeeContract;
	}
	
	public SiteContract createSiteContract(PrimaryKey primaryKey){
		SiteContract siteContract = new SiteContract(primaryKey);
		entityManager.persist(siteContract);
		return siteContract;
	}
	
	public TeamContract createTeamContract(PrimaryKey primaryKey){
		TeamContract teamContract = new TeamContract(primaryKey);
		entityManager.persist(teamContract);
		return teamContract;
	}
	
	/**
	 * Update Contract 
	 * @param contract
	 * @return
	 */
	public Contract updateContract(Contract contract) {
		return entityManager.merge(contract);
	}
	
	public void persistContract(Contract contract) {
		entityManager.persist(contract);
	}

}
