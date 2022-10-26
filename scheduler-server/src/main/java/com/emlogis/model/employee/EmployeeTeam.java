package com.emlogis.model.employee;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Team;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "employeeTenantId", "teamId", "teamTenantId"}))
public class EmployeeTeam extends BaseEntity implements Serializable{

	@Column(nullable = false)
	private boolean isFloating;

	// TODO Consider making Home Team designation a separate non-nullable one-to-one 
	//      relationship between the Employee and exactly 1 EmployeeSkill from  
	//      the Employee's employeeSkills Set.
	@Column(nullable = false)
	private boolean isHomeTeam;
	
	@Column(nullable = false)
	private boolean isSchedulable;

    //bi-directional many-to-one association to Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "employeeTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "employeeId", referencedColumnName = "id")
    })
    private Employee employee;
	
    //bi-directional many-to-one association to Team
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "teamTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "teamId", referencedColumnName = "id")
    })
    private Team team;

    /**
     * Default constructor
     */
	protected EmployeeTeam() {
		super();
		isSchedulable = false;
		isHomeTeam = false;
		isFloating = false;
	}

    /**
     * Primary key constructor
     */
	public EmployeeTeam(PrimaryKey primaryKey) {
		super(primaryKey);
		isSchedulable = false;
		isHomeTeam = false;
		isFloating = false;
	}

	/**
	 * @return the isFloating
	 */
	public boolean getIsFloating() {
		return isFloating;
	}

	/**
	 * @param isFloating the isFloating to set
	 */
	public void setIsFloating(boolean isFloating) {
		this.isFloating = isFloating;
	}

	/**
	 * @return the isHomeTeam
	 */
	public boolean getIsHomeTeam() {
		return isHomeTeam;
	}

	/**
	 * @param isHomeTeam the isHomeTeam to set
	 */
	public void setIsHomeTeam(boolean isHomeTeam) {
		this.isHomeTeam = isHomeTeam;
	}

	/**
	 * @return the isSchedulable
	 */
	public boolean getIsSchedulable() {
		return isSchedulable;
	}

	/**
	 * @param isSchedulable the isSchedulable to set
	 */
	public void setIsSchedulable(boolean isSchedulable) {
		this.isSchedulable = isSchedulable;
	}

	/**
	 * @return the employee
	 */
	public Employee getEmployee() {
		return employee;
	}

	/**
	 * @param employee the employee to set
	 */
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	/**
	 * @return the team
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * @param team the team to set
	 */
	public void setTeam(Team team) {
		this.team = team;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EmployeeTeam [isHomeTeam=" + isHomeTeam + ", team=" + team + "]";
	}

}
