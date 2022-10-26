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
		@Index(name="SHWICHG_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="SHWICHG_EMPLOYEEAId_INDEX", unique=false, columnList="employeeAId") ,
		@Index(name="SHWICHG_EMPLOYEEBId_INDEX", unique=false, columnList="employeeBId") ,
		@Index(name="SHWICHG_TYPE_INDEX", unique=false, columnList="type")
})
public class ShiftWipChange extends BaseScheduleChange{
	
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
