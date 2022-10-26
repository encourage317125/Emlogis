package com.emlogis.workflow.persistence;

import org.drools.persistence.TransactionManager;
import org.drools.persistence.TransactionSynchronization;
import org.drools.persistence.TransactionSynchronizationRegistryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * Created by alexborlis on 12.02.15.
 */
@Singleton
@LocalBean
public class EmlogisWFLTransactionManager implements TransactionManager {

    private static Logger logger = LoggerFactory.getLogger(EmlogisWFLTransactionManager.class);
    protected TransactionSynchronizationRegistry txSyncRegistry;

    public EmlogisWFLTransactionManager() {
        this.txSyncRegistry = (TransactionSynchronizationRegistry) findTransactionSynchronizationRegistry();
    }

    public boolean begin() {
        // do nothing
        return false;
    }

    public void commit(boolean arg0) {
        // do nothing

    }

    public int getStatus() {

        return TransactionManager.STATUS_ACTIVE;
    }

    public void registerTransactionSynchronization(TransactionSynchronization arg0) {

        if (txSyncRegistry != null) {
            TransactionSynchronizationRegistryHelper.registerTransactionSynchronization( txSyncRegistry, arg0 );
        }

    }

    public void rollback(boolean arg0) {
        // do nothing

    }

    protected Object findTransactionSynchronizationRegistry() {

        //String jndiName = JtaTransactionManager.DEFAULT_TRANSACTION_SYNCHRONIZATION_REGISTRY_NAME;
        try {
            InitialContext context = new InitialContext();
            Object tsrObject = context.lookup("java:jboss/TransactionSynchronizationRegistry");
            return tsrObject;
        } catch (NamingException ex) {
            logger.warn("Error when getting TransactionSynchronizationRegistry from JNDI ", ex);
            String customJndiLocation = System.getProperty("jbpm.tsr.jndi.lookup", "java:jboss/TransactionSynchronizationRegistry");
            try {

                Object tsrObject =  InitialContext.doLookup(customJndiLocation);
                logger.debug( "JTA TransactionSynchronizationRegistry found at default JNDI location [{}]",
                        customJndiLocation );

                return tsrObject;
            } catch (Exception e1) {
                logger.debug( "No JTA TransactionSynchronizationRegistry found at default JNDI location [{}]",
                        customJndiLocation,
                        ex );
            }
        }

        return null;
    }

    @Override
    public void putResource(Object key, Object resource) {
        TransactionSynchronizationRegistryHelper.putResource(this.txSyncRegistry, key, resource);
    }

    @Override
    public Object getResource(Object key) {
        return TransactionSynchronizationRegistryHelper.getResource(this.txSyncRegistry, key);
    }
}
