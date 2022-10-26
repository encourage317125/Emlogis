package com.emlogis.model.employee;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
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
		@Index(name="CIAVAIL__AVAILTYPE_INDEX", unique=false, columnList="availabilityType") ,
		@Index(name="CIAVAIL_STARTDATETIME_INDEX", unique=false, columnList="startDateTime") ,
		@Index(name="CIAVAIL_ENDDATETIME_INDEX", unique=false, columnList="endDateTime")
})
public class CIAvailabilityTimeFrame extends AvailabilityTimeFrame {	
	
    @Column(nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
	private LocalTime startTime;
	
    @Column(nullable = false)
	private DayOfWeek dayOfTheWeek;
	
    @Column(nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime startDateTime;
	
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime endDateTime;

	public CIAvailabilityTimeFrame() {}

	public CIAvailabilityTimeFrame(PrimaryKey primaryKey, Employee employee,
			AbsenceType absenceType, String reason, LocalTime starTime,
			Minutes duration, AvailabilityType availabilityType, DayOfWeek dayOfTheWeek,
			DateTime startDateTime, DateTime endDateTime) {
		super(primaryKey, employee, absenceType, reason, duration,
				availabilityType);
		
		this.startTime = starTime;
		this.dayOfTheWeek = dayOfTheWeek;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}
	
	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}	
	
	public DateTime getStartDateTime() {
		return startDateTime;
	}
	
	public void setStartDateTime(DateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public DateTime getEndDateTime() {
		return endDateTime;
	}
	
	public void setEndDateTime(DateTime endDateTime) {
		this.endDateTime = endDateTime;
	}


	public DayOfWeek getDayOfTheWeek() {
		return dayOfTheWeek;
	}


	public void setDayOfTheWeek(DayOfWeek dayOfTheWeek) {
		this.dayOfTheWeek = dayOfTheWeek;
	}	
	
}
