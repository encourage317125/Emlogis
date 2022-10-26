package com.emlogis.model.common;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Developer on 16.01.2015.
 */
public interface IBaseEntity extends Serializable {

    String getOwnedBy();

    void setOwnedBy(String ownedBy);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);

    DateTime getCreated();

    void setCreated(DateTime created);

    DateTime getUpdated();

    void setUpdated(DateTime updated);

}
