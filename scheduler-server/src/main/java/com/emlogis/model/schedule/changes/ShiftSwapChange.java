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
		@Index(name="SHWSCHG_SCHEDULEID_INDEX", unique=false, columnList="scheduleId") ,
		@Index(name="SHWSCHG_EMPLOYEEAId_INDEX", unique=false, columnList="employeeAId") ,
		@Index(name="SHWSCHG_EMPLOYEEBId_INDEX", unique=false, columnList="employeeBId") ,
		@Index(name="SHWSCHG_TYPE_INDEX", unique=false, columnList="type")
})
public class ShiftSwapChange extends BaseScheduleChange{

    private String employeeAnewShiftId;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String employeeAnewShiftCopy;

    private String employeeBnewShiftId;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String employeeBnewShiftCopy;

    public String getEmployeeAnewShiftId() {
        return employeeAnewShiftId;
    }

    public void setEmployeeAnewShiftId(String employeeAnewShiftId) {
        this.employeeAnewShiftId = employeeAnewShiftId;
    }

    public String getEmployeeAnewShiftCopy() {
        return employeeAnewShiftCopy;
    }

    public void setEmployeeAnewShiftCopy(String employeeAnewShiftCopy) {
        this.employeeAnewShiftCopy = employeeAnewShiftCopy;
    }

    public String getEmployeeBnewShiftId() {
        return employeeBnewShiftId;
    }

    public void setEmployeeBnewShiftId(String employeeBnewShiftId) {
        this.employeeBnewShiftId = employeeBnewShiftId;
    }

    public String getEmployeeBnewShiftCopy() {
        return employeeBnewShiftCopy;
    }

    public void setEmployeeBnewShiftCopy(String employeeBnewShiftCopy) {
        this.employeeBnewShiftCopy = employeeBnewShiftCopy;
    }
}
