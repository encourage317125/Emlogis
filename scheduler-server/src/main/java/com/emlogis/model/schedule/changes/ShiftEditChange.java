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
		@Index(name="SHECHG_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="SHECHG_EMPLOYEEAId_INDEX", unique=false, columnList="employeeAId") ,
		@Index(name="SHECHG_EMPLOYEEBId_INDEX", unique=false, columnList="employeeBId") ,
		@Index(name="SHECHG_TYPE_INDEX", unique=false, columnList="type")
})
public class ShiftEditChange extends BaseScheduleChange {

    private String shiftId;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String previousShiftCopy;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String newShiftCopy;

    private String changedAtributes;

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getPreviousShiftCopy() {
        return previousShiftCopy;
    }

    public void setPreviousShiftCopy(String previousShiftCopy) {
        this.previousShiftCopy = previousShiftCopy;
    }

    public String getNewShiftCopy() {
        return newShiftCopy;
    }

    public void setNewShiftCopy(String newShiftCopy) {
        this.newShiftCopy = newShiftCopy;
    }

    public String getChangedAtributes() {
        return changedAtributes;
    }

    public void setChangedAtributes(String changedAtributes) {
        this.changedAtributes = changedAtributes;
    }
}
