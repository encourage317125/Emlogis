package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.emlogis.engine.sqlserver.loader.domain.EmployeeContractLine.EmployeeContractPK;


/**
 * The persistent class for the EmployeeContractLines database table.
 * 
 */
@Entity
@Table(name="EmployeeContractLines")
@NamedQuery(name="EmployeeContractLine.findAll", query="SELECT e FROM EmployeeContractLine e")
@IdClass(value = EmployeeContractPK.class)
public class EmployeeContractLine implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(insertable=false, updatable=false)
	private long employeeId;

	@Column(name="MaxValue", insertable=false, updatable=false)
	private double maxValue;

	@Column(name="MinValue", insertable=false, updatable=false)
	private double minValue;

	@Id
	@Column(name="RestrictionName", insertable=false, updatable=false)
	private String restrictionName;

	@Column(name="RestrictionOrigin", insertable=false, updatable=false)
	private String restrictionOrigin;

	public EmployeeContractLine() {
	}

	public long getEmployeeId() {
		return this.employeeId;
	}

	public void setEmployeeId(long employeeId) {
		this.employeeId = employeeId;
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
	

	public static class EmployeeContractPK implements Serializable{
		private static final long serialVersionUID = 1L;

		public long employeeId;
		public String restrictionName;
		
		public EmployeeContractPK() {
		}
		
		public EmployeeContractPK(long employeeId, String restrictionName) {
			this.employeeId = employeeId;
			this.restrictionName = restrictionName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (employeeId ^ (employeeId >>> 32));
			result = prime
					* result
					+ ((restrictionName == null) ? 0 : restrictionName
							.hashCode());
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
			EmployeeContractPK other = (EmployeeContractPK) obj;
			if (employeeId != other.employeeId)
				return false;
			if (restrictionName == null) {
				if (other.restrictionName != null)
					return false;
			} else if (!restrictionName.equals(other.restrictionName))
				return false;
			return true;
		}
	}

}