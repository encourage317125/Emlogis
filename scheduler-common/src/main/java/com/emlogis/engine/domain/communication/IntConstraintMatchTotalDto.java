package com.emlogis.engine.domain.communication;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.optaplanner.core.api.score.constraint.primint.IntConstraintMatch;
import org.optaplanner.core.api.score.constraint.primint.IntConstraintMatchTotal;

public class IntConstraintMatchTotalDto implements Serializable {

    protected Set<IntConstraintMatchDto> constraintMatchSet;
    protected int weightTotal;
    protected String constraintPackage;
    protected String constraintName;

    public IntConstraintMatchTotalDto() {
    	this.constraintPackage = "";
		this.constraintName = "";
		this.constraintMatchSet = new HashSet<>();
		this.weightTotal = 0;
	}
    
    public IntConstraintMatchTotalDto(IntConstraintMatchTotal icm){
    	this.constraintPackage = icm.getConstraintPackage();
		this.constraintName = icm.getConstraintName();
		this.constraintMatchSet = convertConstraintMatches(icm.getConstraintMatchSet());
		this.weightTotal = icm.getWeightTotal();
    }
    
    public Set<IntConstraintMatchDto> convertConstraintMatches(Set<IntConstraintMatch> constraintMatches){
    	Set<IntConstraintMatchDto> dtoSet = new HashSet<>();
    	for(IntConstraintMatch originalConstraint : constraintMatches){
    		IntConstraintMatchDto dtoMatch = new IntConstraintMatchDto(originalConstraint);
    		dtoSet.add(dtoMatch);
    	}
    	return dtoSet;
    }

    public IntConstraintMatchTotalDto(String constraintPackage, String constraintName, int scoreLevel) {
		this.constraintPackage = constraintPackage;
		this.constraintName = constraintName;
		this.constraintMatchSet = new HashSet<IntConstraintMatchDto>();
		this.weightTotal = 0;
    }

	public int getWeightTotal() {
		return weightTotal;
	}

	public void setWeightTotal(int weightTotal) {
		this.weightTotal = weightTotal;
	}

	public Set<IntConstraintMatchDto> getConstraintMatchSet() {
		return constraintMatchSet;
	}

	public String getConstraintPackage() {
		return constraintPackage;
	}

	public String getConstraintName() {
		return constraintName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IntConstraintMatchTotalDTO [constraintPackage=");
		builder.append(constraintPackage);
		builder.append(", constraintName=");
		builder.append(constraintName);
		builder.append(", weightTotal=");
		builder.append(weightTotal);
		builder.append(", constraintMatchSet=");
		builder.append(constraintMatchSet);
		builder.append("]");
		return builder.toString();
	}

}
