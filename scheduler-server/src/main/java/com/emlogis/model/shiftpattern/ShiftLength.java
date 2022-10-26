package com.emlogis.model.shiftpattern;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Site;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ShiftLength extends BaseEntity {

    private String name;

    private String description;

    private int lengthInMin;

    private boolean active = true;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shiftLength")
    private Set<ShiftType> shiftTypes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "siteTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "siteId", referencedColumnName = "id")
    })
    private Site site;

    public ShiftLength() {}

    public ShiftLength(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLengthInMin() {
        return lengthInMin;
    }

    public void setLengthInMin(int lengthInMin) {
        this.lengthInMin = lengthInMin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<ShiftType> getShiftTypes() {
        return shiftTypes;
    }

    public void setShiftTypes(Set<ShiftType> shiftTypes) {
        this.shiftTypes = shiftTypes;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
