/*
 * Package: com.emlogis.common.services.workflow
 *
 * File: WFLProtoProcessService.java
 *
 * Created:February 6, 2015 1:57:06 PM
 *
 * Copyright (c) 2004-2012 EmLogis, Inc. 9800 Richmond Ave. Suite 235, Houston,
 * Texas, 77042, U.S.A. All rights reserved.
 *
 * This software is the confidential and proprietary information of EmLogis,
 * Inc. ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with EmLogis.
 *
 * Date: February 6, 2015
 *
 * Author: alex@borlis.net
 *
 * Changes: Created
 */
package com.emlogis.common.services.workflow.process.proto;


import com.emlogis.common.services.common.PrimaryKeyJPARepositoryServiceImpl;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Represents repository level access EJB for {@link WflProcess}.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 *          reviewed by
 */
@Stateless
@Local(value = WflProcessService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WflProcessServiceImpl
        extends PrimaryKeyJPARepositoryServiceImpl<WflProcess>
        implements WflProcessService {

    @Override
    public Class<WflProcess> getEntityClass() {
        return WflProcess.class;
    }

    @Override
    public WflProcess findByTypeAndOrganization(WorkflowRequestTypeDict type, String tenantId) {
        String queryStr = new String("" +
                " SELECT p.* FROM WflProcess p " +
                " JOIN WflProcessType pt on (pt.id=fk_wfl_process_type_id AND pt.type = '" + type.name() + "')" +
                " WHERE p.tenantId = '" + tenantId + "';");
        Query query = getEntityManager().createNativeQuery(queryStr, WflProcess.class);
        return (WflProcess) query.getSingleResult();
    }

    @Override
    public WflProcess merge(WflProcess process) {
        try {
            return findByTypeAndOrganization(process.getType().getType(), process.getTenantId());
        } catch (NoResultException nre) {
            return create(process);
        }
    }
}
