package com.emlogis.model.structurelevel;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.TeamContract;
import com.emlogis.model.employee.EmployeeTeam;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.ShiftStructure;
import com.emlogis.model.shiftpattern.ShiftPattern;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
//@Table(indexes={
//		  @Index(name="TEAM_INDEX", unique=true, columnList="tenantId, id") 
//})
public class Team extends StructureLevel {

    public final static String AOM_ENTITY_TYPE = "Team";
    
    private	String	abbreviation;
    
    private	boolean	isActive = true;
    
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime startDate = new DateTime(0);     

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime endDate = new DateTime(0);     


    // bidirectional
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "team", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    private Set<ShiftStructure> shiftStructures = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Team_Schedule",
            joinColumns = {@JoinColumn(name = "Team_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "Team_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "schedules_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "schedules_id", referencedColumnName = "id")})
    private Set<Schedule> schedules;

    //bi-directional one-to-many association to EmployeeTeam
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EmployeeTeam> employeeTeams = new HashSet<>();
    
    //bi-directional one-to-many association to contract
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TeamContract> teamContracts = new HashSet<>();

    // bidirectional
    @OneToMany(mappedBy = "team", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ShiftPattern> shiftPatterns = new HashSet<>();
    
    @Column
    private boolean isDeleted = false;

    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected Team() {
        super();
    }

    public Team(PrimaryKey primaryKey) {
        super(primaryKey);
        setAomEntityType(AOM_ENTITY_TYPE);	// optional as by default AOM entity type = this. simple class name
    }

    /**
     * Setter for skills field
     * @param skills
     *
     * Protected to satisfy JPA but otherwise discourage use by developers, who
     * should only add/remove skills through the add/remove methods that will
     * correctly manage the bidirectional many-to-many relationship.
     */
    protected void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    /**
     * Getter for skills field
     * @return
     *
     * It is recommended that developers use this for read-only activities, as
     * adding/removing skills is best handled through the add/remove methods
     * that will correctly manage the bidirectional many-to-many relationship.
     */
    public Set<Skill> getSkills() {
        return skills;
    }

    /**
     * Add skill to this team
     * @param skill
     */
    public void addSkill(Skill skill) {
        if (!skills.contains(skill)) {
            this.skills.add(skill);
        }

        // Bidirectional many-to-many relationship, so handle the inverse as well...
        if (!skill.getTeams().contains(this)) {
            skill.getTeams().add(this);
        }
    }

    /**
     * Remove skill from this team
     * @param skill
     */
    public void removeSkill(Skill skill) {
        if (skills.contains(skill)){
            this.skills.remove(skill);
        }

        // Bidirectional many-to-many relationship, so handle the inverse as well...
        if (skill.getTeams().contains(this)){
            skill.getTeams().remove(this);
        }
    }

    public Set<ShiftStructure> getShiftStructures() {
        return shiftStructures;
    }

    public void setShiftStructures(Set<ShiftStructure> shiftStructures) {
        this.shiftStructures = shiftStructures;
    }

    public void addShiftStructure(ShiftStructure shiftStructure) {
        shiftStructures.add(shiftStructure);
    }

    public void removeShiftStructure(ShiftStructure shiftStructure) {
        shiftStructures.remove(shiftStructure);
    }

    public Set<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(Set<Schedule> schedules) {
        this.schedules = schedules;
    }

    /**
     * @return the employeeTeams
     */
    public Set<EmployeeTeam> getEmployeeTeams() {
        return employeeTeams;
    }

    /**
     * @param employeeTeams the employeeTeams to set
     */
    public void setEmployeeTeams(Set<EmployeeTeam> employeeTeams) {
        this.employeeTeams = employeeTeams;
    }

    public Set<ShiftPattern> getShiftPatterns() {
        return shiftPatterns;
    }

    public void setShiftPatterns(Set<ShiftPattern> shiftPatterns) {
        this.shiftPatterns = shiftPatterns;
    }

    /**
     * Add EmployeeTeam to this Team
     * @param employeeTeam
     */
    public void addEmployeeTeam(EmployeeTeam employeeTeam) {
        if (!employeeTeams.contains(employeeTeam)){
            this.employeeTeams.add(employeeTeam);
            employeeTeam.setTeam(this);
        }
    }

    /**
     * Remove EmployeeTeam from this Team
     * @param employeeTeam
     */
    public void removeEmployeeTeam(EmployeeTeam employeeTeam) {
        if (employeeTeams.contains(employeeTeam)) {
            employeeTeams.remove(employeeTeam);
        }
    }

	public Set<TeamContract> getTeamContracts() {
		return teamContracts;
	}

	public void setTeamContracts(Set<TeamContract> teamContracts) {
		this.teamContracts = teamContracts;
	}

	/**
	 * @return the isDeleted
	 */
	public boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

    public long getStartDate() {
		return startDate  == null ? 0 : startDate.toDate().getTime();
    }

    public void setStartDate(long startDate) {
		this.startDate = new DateTime(startDate);
    }

    public long getEndDate() {
		return endDate  == null ? 0 : endDate.toDate().getTime();
    }

    public void setEndDate(long endDate) {
		this.endDate = new DateTime(endDate);
    }

}
