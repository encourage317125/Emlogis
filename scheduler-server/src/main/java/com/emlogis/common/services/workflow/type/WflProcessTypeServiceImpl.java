/*
 * Package: com.emlogis.common.services.workflow
 *
 * File: WFLProcessTypeService.java
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
package com.emlogis.common.services.workflow.type;

import com.emlogis.common.services.common.SimpleKeyJPARepositoryServiceImpl;
import com.emlogis.model.workflow.entities.WflProcessType;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.persistence.NoResultException;

import static com.emlogis.common.services.common.GeneralJPARepository.Operation.select;

/**
 * Represents repository level access EJB for {@link WflProcessType}.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 *          reviewed by
 */
@Stateless
@Local(value = WflProcessTypeService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WflProcessTypeServiceImpl
        extends SimpleKeyJPARepositoryServiceImpl<WflProcessType>
        implements WflProcessTypeService {

    private final static Logger logger = Logger.getLogger(WflProcessTypeServiceImpl.class);

    @Override
    public Class<WflProcessType> getEntityClass() {
        return WflProcessType.class;
    }

    @Override
    public WflProcessType findByDictionary(WorkflowRequestTypeDict dictionary) {
        return findBy(getBuilder().equal(getFrom(select).get("type"), dictionary));
    }

    @Override
    public WflProcessType merge(WflProcessType pto) {
        try {
            return findByNameAndType(pto.getName(), pto.getType());
        } catch (NoResultException nre) {
            return create(pto);
        }
    }

    @Override
    public WflProcessType findByName(String name) {
        return findBy(getBuilder().equal(getFrom(select).get("name"), name));
    }

    private WflProcessType findByNameAndType(String name, WorkflowRequestTypeDict type) {
        return findBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get("name"), name),
                getBuilder().equal(getFrom(select).get("type"), type)
        ));
    }
}
