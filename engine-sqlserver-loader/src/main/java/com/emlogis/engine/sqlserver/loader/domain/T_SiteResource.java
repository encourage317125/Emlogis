package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: T_SiteResource
 *
 */
@Entity
@NamedQuery(name="T_SiteResource.findAll", query="SELECT t FROM T_SiteResource t")
public class T_SiteResource implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="SiteResourceID")
	private long siteResourceId;
	
	@Column(name="SiteScheduleID")
	private long scheduleId;

	@Column(name="SiteEmployeeID")
	private long employeeId;

	@Column(name="RTOBinaryTally")
	private long rtoBinaryTally;

	public T_SiteResource() {
		super();
	}

	public long getSiteResourceId() {
		return siteResourceId;
	}

	public void setSiteResourceId(long siteResourceId) {
		this.siteResourceId = siteResourceId;
	}

	public long getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(long scheduleId) {
		this.scheduleId = scheduleId;
	}

	public long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(long employeeId) {
		this.employeeId = employeeId;
	}

	public long getRtoBinaryTally() {
		return rtoBinaryTally;
	}

	public void setRtoBinaryTally(long rtoBinaryTally) {
		this.rtoBinaryTally = rtoBinaryTally;
	}
   
}
