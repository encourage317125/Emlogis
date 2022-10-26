package com.emlogis.model.employee;

import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(indexes={
		@Index(name="CDAVAIL__AVAILTYPE_INDEX", unique=false, columnList="availabilityType") ,
		@Index(name="CDAVAIL_STARTDATETIME_INDEX", unique=false, columnList="startDateTime") ,
		@Index(name="CDAVAIL_ISPTO_INDEX", unique=false, columnList="isPTO")
})
public class CDAvailabilityTimeFrame extends AvailabilityTimeFrame {

	@Column(nullable = false)
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	DateTime startDateTime;
	
	private boolean isPTO = false;
	
	public CDAvailabilityTimeFrame() {}

	public CDAvailabilityTimeFrame(PrimaryKey primaryKey, Employee employee,
			AbsenceType absenceType, String reason,
			Minutes duration, AvailabilityType availabilityType, DateTime startDateTime, boolean isPTO) {
		super(primaryKey, employee, absenceType, reason, duration,
				availabilityType);
		
		this.startDateTime = startDateTime;
		this.isPTO = isPTO;
	}

	/**
	 * @return the isPTO
	 */
	public boolean getIsPTO() {
		return isPTO;
	}

	/**
	 * @param isPTO the isPTO to set
	 */
	public void setIsPTO(boolean isPTO) {
		this.isPTO = isPTO;
	}

	public DateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(DateTime startDateTime) {
		this.startDateTime = startDateTime;
	}
	
}
