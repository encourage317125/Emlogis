package com.emlogis.model.employee;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.shiftpattern.ShiftPattern;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Skill extends BaseEntity implements Serializable {
	
    @Column(nullable = false)
	private	String name;
	    
    @Column(nullable = false)
	private	String abbreviation;
	    
    @Column()
	private	String description;
    
    @Column()
    private boolean	isActive = true;
  
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime startDate = new DateTime(0);     

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime endDate = new DateTime(0);  
    
    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
    private Set<Site> sites = new HashSet<>();
    
    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
    private Set<Team> teams = new HashSet<>();

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ShiftPattern> shiftPatterns = new HashSet<>();

    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected Skill() {
		super();
		abbreviation =  "no abbreviation set";
		name = "no name set";
		description = "no description set";
	}

    /**
     * Required fields constructor.
     * 
     * @param primaryKey
     * @param name
     * @param abbreviation
     * @param description
     */
    public Skill(PrimaryKey primaryKey, String name, String abbreviation, String description) {
		super(primaryKey);
		this.name = name;
		this.abbreviation = abbreviation;
		this.description = description;
	}

    /**
     * Getter for abbreviation field
     * @return abbreviation
     */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * Setter for abbreviation field
	 * @param abbreviation
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	/**
	 * Getter for name field
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for name field
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for description field
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter for description field
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
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
    
	/**
	 * Setter for sites field
	 * @param sites
	 * 
	 * Protected to satisfy JPA but otherwise discourage use by developers, who
	 * should only add/remove sites through the add/remove methods that will
	 * correctly manage the bidirectional many-to-many relationship.
	 */
	protected void setSites(Set<Site> sites) {
		this.sites = sites;
	}

	/**
	 * Getter for sites field
	 * @return sites
	 * 
	 * It is recommended that developers use this for read-only activities, as 
	 * adding/removing sites is best handled through the add/remove methods
	 * that will correctly manage the bidirectional many-to-many relationship.
	 */
	public Set<Site> getSites() {
		return sites;
	}

	/**
	 * Setter for teams field
	 * @param teams
	 * 
	 * Protected to satisfy JPA but otherwise discourage use by developers, who
	 * should only add/remove teams through the add/remove methods that will
	 * correctly manage the bidirectional many-to-many relationship.
	 */
	protected void setTeams(Set<Team> teams) {
		this.teams = teams;
	}

	/**
	 * Getter for teams field
	 * @return teams
	 * 
	 * It is recommended that developers use this for read-only activities, as 
	 * adding/removing teams is best handled through the add/remove methods
	 * that will correctly manage the bidirectional many-to-many relationship.
	 */
	public Set<Team> getTeams() {
		return teams;
	}

    public Set<ShiftPattern> getShiftPatterns() {
        return shiftPatterns;
    }

    public void setShiftPatterns(Set<ShiftPattern> shiftPatterns) {
        this.shiftPatterns = shiftPatterns;
    }

    /**
	 * Add sites to this skill
	 * @param site
	 */
	public void addSite(Site site) {
		if (!sites.contains(site)){
			this.sites.add(site);
		}
		
		// Bidirectional many-to-many relationship, so handle the inverse as well...
		if (!site.getSkills().contains(this)){
			site.getSkills().add(this);
		}
	}

	/**
	 * Remove site from this skill
	 * @param site
	 */
	public void removeSite(Site site) {
		if (sites.contains(site)) {
			this.sites.remove(site);
		}

		// Bidirectional many-to-many relationship, so handle the inverse as well...
		if (site.getSkills().contains(this)) {
			site.getSkills().remove(this);
		}
	}

	/**
	 * Add team to this skill
	 * @param team
	 */
	public void addTeam(Team team) {
		if (!teams.contains(team)){
			this.teams.add(team);
		}

		// Bidirectional many-to-many relationship, so handle the inverse as well...
		if (!team.getSkills().contains(this)){
			team.getSkills().add(this);
		}
	}

	/**
	 * Remove team from this skill
	 * @param team
	 */
	public void removeTeam(Team team) {
		if (teams.contains(team)){
			this.teams.remove(team);
		}
		
		// Bidirectional many-to-many relationship, so handle the inverse as well...
		if (team.getSkills().contains(this)){
			team.getSkills().remove(this);
		}
	}

}

