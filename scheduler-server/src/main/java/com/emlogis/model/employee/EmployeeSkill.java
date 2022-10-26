package com.emlogis.model.employee;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint( columnNames = {"employeeId", "employeeTenantId", "skillId", "skillTenantId"}))
public class EmployeeSkill extends BaseEntity implements Serializable{

	// TODO Consider making Primary Skill designation a separate nullable one-to-one 
	//      relationship between the Employee and either 0 or 1 EmployeeSkill from  
	//      the Employee's employeeSkills Set.
	@Column(nullable = false)
	private boolean isPrimarySkill;

	@Column(nullable = true)
	private int skillScore = 1;  // -1 or 1..5

	
    //bi-directional many-to-one association to Employee
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="employeeTenantId", referencedColumnName="tenantId"),
        @JoinColumn(name="employeeId", referencedColumnName="id")
    })
    private Employee employee;
	

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="skillTenantId", referencedColumnName="tenantId"),
        @JoinColumn(name="skillId", referencedColumnName="id")
    })
    private Skill skill;

    
    
    /**
     * Default constructor
     */
	protected EmployeeSkill() {
		super();
		isPrimarySkill = false;
	}

	
	
    /**
     * Primary key constructor
     */
	public EmployeeSkill(PrimaryKey primaryKey) {
		super(primaryKey);
		isPrimarySkill = false;
	}

	
	
	/**
	 * Getter for isPrimarySkill
	 * @return
	 */
	public boolean getIsPrimarySkill() {
		return isPrimarySkill;
	}

	
	
	/**
	 * Setter for isPrimarySkill
	 * @param isPrimary
	 */
	public void setIsPrimarySkill(boolean isPrimary) {
		this.isPrimarySkill = isPrimary;
	}

	
	
	/**
	 * Getter for skillScore
	 * @return
	 */
	public int getSkillScore() {
		return skillScore;
	}

	
	
	/**
	 * Setter for skillScore
	 * @param skillScore
	 */
	public void setSkillScore(int skillScore) {
		this.skillScore = skillScore;
	}

	
	
	/**
	 * Getter for employee
	 * @return
	 */
	public Employee getEmployee() {
		return employee;
	}

	
	
	/**
	 * Setter for employee
	 * @param employee
	 */
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	
	
	/**
	 * Getter for skill
	 * @return
	 */
	public Skill getSkill() {
		return skill;
	}

	
	
	/**
	 * Setter for skill
	 * @param skill
	 */
	public void setSkill(Skill skill) {
		this.skill = skill;
	}
      
}
