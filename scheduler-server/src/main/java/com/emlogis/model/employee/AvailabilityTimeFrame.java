package com.emlogis.model.employee;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.Minutes;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity

public abstract class AvailabilityTimeFrame extends BaseEntity {
	
	public enum AvailabilityType {
		Avail,					// actual Availability
		UnAvail,				// actual UnAvailability
		AvailPreference,		// prefered Availability
		UnAvailPreference		// prefered UnAvailability
	}
	
    //bi-directional many-to-one association to Employee
	// You must have a employee associated Employee Availability timeFrames
    @ManyToOne(optional = false)
    @JoinColumns({
        @JoinColumn(name="employeeTenantId", referencedColumnName="tenantId"),
        @JoinColumn(name="employeeId", referencedColumnName="id")
    })
    private Employee employee;
	
	@OneToOne
    @JoinColumns({
        @JoinColumn(name="AbsenceTenantId", referencedColumnName="tenantId"),
        @JoinColumn(name="AbsenceTypeId", referencedColumnName="id")
    })
	private AbsenceType absenceType;
	
	@Column
	String reason;		
	
	@Column(nullable = false)
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentMinutes")
	Minutes durationInMinutes;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	AvailabilityType availabilityType;
	
	public AvailabilityTimeFrame() {}
	
	public AvailabilityTimeFrame(PrimaryKey primaryKey, Employee employee,
			AbsenceType absenceType, String reason, Minutes duration, 
			AvailabilityType availabilityType) {
		super(primaryKey);
		this.employee = employee;
		this.absenceType = absenceType;
		this.reason = reason;
		this.durationInMinutes = duration;
		this.availabilityType = availabilityType;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
		
		// since relationship is bidirectional...
		if (!employee.getAvailabilityTimeFrames().contains(this)){
			employee.addAvailabilityTimeFrame(this);			
		}
	}
	
	public String getEmployeeId() {
		return employee.getId();
	}

	public AbsenceType getAbsenceType() {
		return absenceType;
	}

	public void setAbsenceType(AbsenceType absenceType) {
		this.absenceType = absenceType;
	}
	
	public String getAbsenceTypeId() {
		return  (absenceType != null ? absenceType.getId() : null);	
	}	

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public Minutes getDurationInMinutes() {
		return durationInMinutes;
	}

	public void setDurationInMinutes(Minutes durationInMinutes) {
		this.durationInMinutes = durationInMinutes;
	}
	
	public AvailabilityType getAvailabilityType() {
		return availabilityType;
	}

	public void setAvailabilityType(AvailabilityType availabilityType) {
		this.availabilityType = availabilityType;
	}
}
