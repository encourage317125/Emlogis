/*
 * Package: com.emlogis.common.services.workflow
 *
 * File: WorkflowAbstractRoleService.java
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
package com.emlogis.common.services.workflow.roles;


import com.emlogis.common.services.common.SimpleKeyJPARepositoryServiceImpl;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WflRole;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.criteria.Expression;
import java.util.Set;

import static com.emlogis.common.services.common.GeneralJPARepository.Operation.select;

/**
 * Represents repository level access EJB for {@link WflRole}.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 *          reviewed by
 */
@Stateless
@Local(value = WflRoleService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WflRoleServiceImpl
        extends SimpleKeyJPARepositoryServiceImpl<WflRole>
        implements WflRoleService {

    private final static Logger logger = Logger.getLogger(WflRoleServiceImpl.class);

    @Override
    public Class<WflRole> getEntityClass() {
        return WflRole.class;
    }

    @Override
    public WflRole findByRoleAndProcess(
            WorkflowRoleDict roleType, WflProcess abstractProcessEntity
    ) {
        Expression<Set<WflProcess>> rolesExp = getFrom(select).get("processes");
        return findBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get("roleType"), roleType),
                getBuilder().isMember(abstractProcessEntity, rolesExp)
        ));
    }

    @Override
    public WflRole merge(WflRole role) {
        try {
            return findByNameAndType(role.getName(), role.getRoleType());
        } catch (NoResultException nre) {
            return create(role);
        }
    }

    @Override
    public WflRole findByName(String name) {
        return findBy(getBuilder().equal(getFrom(select).get("name"), name));
    }


    @Override
    public WflRole merge(WorkflowRoleDict roleDict, WflProcess process) {
        try {
            return findByRoleAndProcess(roleDict, process);
        } catch (Throwable error) {
            return create(new WflRole(roleDict.name(), roleDict, process));
        }
    }

    private WflRole findByNameAndType(String name, WorkflowRoleDict roleType) {
        return findBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get("name"), name),
                getBuilder().equal(getFrom(select).get("roleType"), roleType)
        ));
    }
}
