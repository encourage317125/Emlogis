package com.emlogis.model.structurelevel;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class ShiftDropReason extends BaseEntity {

    private int reasonCode;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Site site;

    public ShiftDropReason() {}

    public ShiftDropReason(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
