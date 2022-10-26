package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.emlogis.engine.sqlserver.loader.domain.SiteContractLine.SiteContractLinePK;


/**
 * The persistent class for the SiteContractLines database table.
 * 
 */
@Entity
@Table(name="SiteContractLines")
@NamedQuery(name="SiteContractLine.findAll", query="SELECT s FROM SiteContractLine s")
@IdClass(value = SiteContractLinePK.class)
public class SiteContractLine implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="MaxValue", insertable=false, updatable=false)
	private double maxValue;

	@Column(name="MinValue", insertable=false, updatable=false)
	private double minValue;

	@Id
	@Column(name="RestrictionName", insertable=false, updatable=false)
	private String restrictionName;

	@Column(name="RestrictionOrigin", insertable=false, updatable=false)
	private String restrictionOrigin;

	@Id
	@Column(name="SiteID", insertable=false, updatable=false)
	private long siteID;

	public SiteContractLine() {
	}

	public double getMaxValue() {
		return this.maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getMinValue() {
		return this.minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public String getRestrictionName() {
		return this.restrictionName;
	}

	public void setRestrictionName(String restrictionName) {
		this.restrictionName = restrictionName;
	}

	public String getRestrictionOrigin() {
		return this.restrictionOrigin;
	}

	public void setRestrictionOrigin(String restrictionOrigin) {
		this.restrictionOrigin = restrictionOrigin;
	}

	public long getSiteID() {
		return this.siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}
	
	public static class SiteContractLinePK implements Serializable{
		private static final long serialVersionUID = 1L;

		public long siteID;
		public String restrictionName;
		
		public SiteContractLinePK() {
		}
		
		public SiteContractLinePK(long siteID, String restrictionName) {
			this.siteID = siteID;
			this.restrictionName = restrictionName;
		}
		
		public long getSiteID() {
			return siteID;
		}
		public void setSiteID(long siteID) {
			this.siteID = siteID;
		}
		public String getRestrictionName() {
			return restrictionName;
		}
		public void setRestrictionName(String restrictionName) {
			this.restrictionName = restrictionName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((restrictionName == null) ? 0 : restrictionName
							.hashCode());
			result = prime * result + (int) (siteID ^ (siteID >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SiteContractLinePK other = (SiteContractLinePK) obj;
			if (restrictionName == null) {
				if (other.restrictionName != null)
					return false;
			} else if (!restrictionName.equals(other.restrictionName))
				return false;
			if (siteID != other.siteID)
				return false;
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((restrictionName == null) ? 0 : restrictionName.hashCode());
		result = prime * result + (int) (siteID ^ (siteID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiteContractLine other = (SiteContractLine) obj;
		if (restrictionName == null) {
			if (other.restrictionName != null)
				return false;
		} else if (!restrictionName.equals(other.restrictionName))
			return false;
		if (siteID != other.siteID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SiteContractLine [maxValue=");
		builder.append(maxValue);
		builder.append(", minValue=");
		builder.append(minValue);
		builder.append(", restrictionName=");
		builder.append(restrictionName);
		builder.append(", restrictionOrigin=");
		builder.append(restrictionOrigin);
		builder.append(", siteID=");
		builder.append(siteID);
		builder.append("]");
		return builder.toString();
	}
	

}