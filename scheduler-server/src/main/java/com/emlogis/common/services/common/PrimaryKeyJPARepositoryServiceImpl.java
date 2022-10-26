package com.emlogis.common.services.common;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.PkEntity;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * Created by user on 14.07.15.
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public abstract class PrimaryKeyJPARepositoryServiceImpl<T extends PkEntity>
        extends GeneralJPARepositoryImpl<T, PrimaryKey> implements GeneralJPARepository<T, PrimaryKey> {
}
