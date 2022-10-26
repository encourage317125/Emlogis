package com.emlogis.model.schedule.changes;

/**
 * Created by emlogis on 7/11/14.
 */

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
		@Index(name="SHASCHG_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="SHASCHG_EMPLOYEEAId_INDEX", unique=false, columnList="employeeAId") ,
		@Index(name="SHASCHG_EMPLOYEEBId_INDEX", unique=false, columnList="employeeBId") ,
		@Index(name="SHASCHG_TYPE_INDEX", unique=false, columnList="type")
})
public class ShiftAssignChange extends BaseScheduleChange {

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
