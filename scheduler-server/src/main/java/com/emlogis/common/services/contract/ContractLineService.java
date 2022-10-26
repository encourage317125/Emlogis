package com.emlogis.common.services.contract;

import com.emlogis.common.Constants;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.*;
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
public class ContractLineService {
	
	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
	
	/**
	 * findContractLines() find a list of contract lines matching criteria;
	 * @param sq
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<ContractLine> findContractLines(SimpleQuery sq) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		sq.setEntityClass(ContractLine.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(entityManager, sq);
	}
	
	/**
	 * @param primaryKey
	 * @return ContractLine
	 */
	public ContractLine getContractLine(PrimaryKey primaryKey) {
		return entityManager.find(ContractLine.class, primaryKey);
	}
	
	public void createContractLine(ContractLine contractLine) {
		entityManager.persist(contractLine);
	}

	public ContractLine updateContractLine(ContractLine contractLine) {
		return entityManager.merge(contractLine);
	}
	
	public void delete(ContractLine contractLine) {
		entityManager.remove(contractLine);
	}

}
