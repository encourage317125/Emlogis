package com.emlogis.model.common;

import com.emlogis.model.PrimaryKey;

public interface PkEntity extends IBaseEntity {

    PrimaryKey getPrimaryKey();

    void setPrimaryKey(PrimaryKey primaryKey);

    String getTenantId();

    String getId();
}
