package com.emlogis.model.schedule.changes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(indexes={  // because of table mapping strategy, these indexes have to be redefined into each subclass 
		@Index(name="SHACHG_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="SHACHG_EMPLOYEEAId_INDEX", unique=false, columnList="employeeAId") ,
		@Index(name="SHACHG_EMPLOYEEBId_INDEX", unique=false, columnList="employeeBId") ,
		@Index(name="SHACHG_TYPE_INDEX", unique=false, columnList="type")
})
public class ShiftAddChange extends BaseScheduleChange {

    private String shiftId;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String shiftCopy;

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftCopy() {
        return shiftCopy;
    }

    public void setShiftCopy(String shiftCopy) {
        this.shiftCopy = shiftCopy;
    }
}
