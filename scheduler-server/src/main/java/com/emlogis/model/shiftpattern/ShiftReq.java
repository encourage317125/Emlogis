package com.emlogis.model.shiftpattern;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ShiftReq extends BaseEntity {

    private int employeeCount;

    private int excessCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "shiftPatternTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "shiftPatternId", referencedColumnName = "id")
    })
    private ShiftPattern shiftPattern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "shiftTypeTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "shiftTypeId", referencedColumnName = "id")
    })
    private ShiftType shiftType;

    public ShiftReq() {}

    public ShiftReq(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getShiftLengthId() {
        return shiftType == null ? null : shiftType.getShiftLengthId();
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public int getExcessCount() {
        return excessCount;
    }

    public void setExcessCount(int excessCount) {
        this.excessCount = excessCount;
    }

    public ShiftPattern getShiftPattern() {
        return shiftPattern;
    }

    public void setShiftPattern(ShiftPattern shiftPattern) {
        this.shiftPattern = shiftPattern;
    }

    public String getShiftTypeId() {
        return getShiftType() == null ? null : shiftType.getId();
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

}
