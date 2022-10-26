package com.emlogis.model.schedule.changes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by emlogis on 7/11/14.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(indexes={  // because of table mapping strategy, these indexes have to be redefined into each subclass 
		@Index(name="SHDRCHG_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="SHDRCHG_EMPLOYEEAId_INDEX", unique=false, columnList="employeeAId") ,
		@Index(name="SHDRCHG_EMPLOYEEBId_INDEX", unique=false, columnList="employeeBId") ,
		@Index(name="SHDRCHG_TYPE_INDEX", unique=false, columnList="type")
})
public class ShiftDropChange extends BaseScheduleChange {

    private String droppedShiftId;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String droppedShiftCopy;

    public String getDroppedShiftId() {
        return droppedShiftId;
    }

    public void setDroppedShiftId(String droppedShiftId) {
        this.droppedShiftId = droppedShiftId;
    }

    public String getDroppedShiftCopy() {
        return droppedShiftCopy;
    }

    public void setDroppedShiftCopy(String droppedShiftCopy) {
        this.droppedShiftCopy = droppedShiftCopy;
    }
}
