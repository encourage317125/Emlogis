package com.emlogis.common.facade.workflow.validator.threadlocal;

import com.emlogis.common.facade.workflow.validator.records.RequestRecord;

import javax.ejb.*;

/**
 * Created by user on 21.08.15.
 */
@Stateful
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestContext {

    private RequestRecord record;

    private ThreadLocal<RequestRecord> storage = new ThreadLocal<>();

    public RequestContext() {
    }

    public void set(RequestRecord auditRecord) {
       // storage.set(auditRecord);
        record = auditRecord;
    }

    public RequestRecord get() {
       // return storage.get();
        return record;
    }
}
