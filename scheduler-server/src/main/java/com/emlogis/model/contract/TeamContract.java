package com.emlogis.model.contract;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Team;

import javax.persistence.*;

@Entity
public class TeamContract extends Contract {
	
    //bi-directional many-to-one association to Team
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="teamTenantId", referencedColumnName="tenantId"),
        @JoinColumn(name="teamId", referencedColumnName="id")
    })
    private Team team;

	public TeamContract() {
		super();
	}

	public TeamContract(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	} 
	
	public String getTeamId() {
		return team.getId();
	}
}
