/*
 * Package: com.emlogis.common.services.workflow
 *
 * File: AbstractWorkflowRepositoryService.java
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
package com.emlogis.common.services.common;

import com.emlogis.model.common.SimpleKeyBaseEntity;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * Represents functionality abstraction for repository level DAO access.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 *          reviewed by
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public abstract class SimpleKeyJPARepositoryServiceImpl<T extends SimpleKeyBaseEntity>
        extends GeneralJPARepositoryImpl<T, String> implements GeneralJPARepository<T, String> {
}
