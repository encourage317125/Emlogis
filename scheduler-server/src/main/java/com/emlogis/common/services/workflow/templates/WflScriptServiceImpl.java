/*
 * Package: com.emlogis.common.services.workflow
 *
 * File: WFLTemplateService.java
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
package com.emlogis.common.services.workflow.templates;

import com.emlogis.common.services.common.SimpleKeyJPARepositoryServiceImpl;
import com.emlogis.model.workflow.entities.WflProcessType;
import com.emlogis.model.workflow.entities.WflSourceScript;
import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.persistence.NoResultException;
import java.util.List;

import static com.emlogis.common.services.common.GeneralJPARepository.Operation.select;

/**
 * Represents repository level access EJB for {@link WflSourceScript}.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 *          reviewed by
 */
@Stateless
@Local(value = WflScriptService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WflScriptServiceImpl extends SimpleKeyJPARepositoryServiceImpl<WflSourceScript> implements WflScriptService {

    private final static Logger logger = Logger.getLogger(WflScriptServiceImpl.class);

    @Override
    public Class<WflSourceScript> getEntityClass() {
        return WflSourceScript.class;
    }

    @Override
    public List<WflSourceScript> findAllByName(String name) {
        return findAllBy(getBuilder().equal(getFrom(select).get("name"), name));
    }

    @Override
    public WflSourceScript merge(WflSourceScript template) {
        try {
            return findByParameters(template.getName(), template.getType());
        } catch (NoResultException nre) {
            return create(template);
        }
    }

    private WflSourceScript findByParameters(String name, WflProcessType type) {
        return findBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get("name"), name),
                getBuilder().equal(getFrom(select).get("type"), type)
        ));
    }
}
