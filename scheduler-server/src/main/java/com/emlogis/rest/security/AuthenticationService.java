package com.emlogis.rest.security;

import com.emlogis.common.Constants;
import com.emlogis.model.tenant.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;


@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AuthenticationService {
	
	private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
		
    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager em;

    public UserAccount authenticate(String tenantId, String login, String password) {
    	logger.debug( "authenticate()");

        Query query = em.createQuery(
                "SELECT accounts FROM UserAccount accounts WHERE (accounts.primaryKey.tenantId = :tenantId) " +
                        "AND accounts.login = :login AND accounts.password = :password");
    	query.setParameter("tenantId", tenantId);
    	query.setParameter("login", login);
    	query.setParameter("password", password);
    	List<UserAccount> userAccounts = query.getResultList();
    	switch (userAccounts.size()) {
    	case 0:
    		return null;
    	case 1:
    		return userAccounts.get(0);
    	default:
    		// we have a pb, several accounst matching the login informatin.
    		// TODO throw appropraite exception
    		return null;
    	}


    }

}

