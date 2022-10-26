package com.emlogis.engine.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import com.emlogis.engine.domain.contract.ConstraintOverride;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLine;
import com.emlogis.engine.domain.contract.contractline.PatternContractLine;
import com.emlogis.engine.domain.contract.patterns.Pattern;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.timeoff.TimeWindow;
import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningSolution
public class EmployeeSchedule implements Solution<BendableScore> {

    private String code;

    private int maxComputationTime = -1;						// max computation time in secs. -1 = infinite;
    private int maxUnimprovedSecondsSpent = 0;     //Max number of allowed unimproving steps, 0 means use XML config
    private EmployeeRosterInfo employeeRosterInfo;
    private List<Skill> skillList;
    private List<ShiftType> shiftTypeList;
    private List<ShiftSkillRequirement> shiftTypeSkillRequirementList;
    private List<Pattern> patternList;
    private List<Contract> contractList;
    private List<ContractLine> contractLineList;
    private List<PatternContractLine> patternContractLineList;
    private List<Employee> employeeList;
    private List<SkillProficiency> skillProficiencyList;
    private List<ShiftDate> shiftDateList;
    private List<Shift> shiftList;
    private List<TimeWindow> employeeTimeOffs;

    private List<TeamAssociation> teamAssociations;
    private List<ConstraintOverride> constraintOverrides;
    private List<ShiftAssignment> shiftAssignmentList;
   
    // Performance increasing shift characteristics 
    private Map<String, Integer> numEmployeesPerSkill;
    private Map<String, Integer> numEmployeesPerTeam;

    @JsonIgnore
    private BendableScore score;
    
    public EmployeeSchedule(){
    	skillList = new ArrayList<Skill>();
    	shiftTypeList = new ArrayList<ShiftType>();
    	shiftTypeSkillRequirementList = new ArrayList<ShiftSkillRequirement>();
    	patternList = new ArrayList<Pattern>();
    	contractList = new ArrayList<Contract>();
    	contractLineList = new ArrayList<ContractLine>();
    	patternContractLineList = new ArrayList<PatternContractLine>();
    	employeeList = new ArrayList<Employee>();
    	skillProficiencyList = new ArrayList<SkillProficiency>();
    	shiftDateList = new ArrayList<ShiftDate>();
    	shiftList = new ArrayList<Shift>();
    	employeeTimeOffs = new ArrayList<TimeWindow>();
    	shiftAssignmentList = new ArrayList<ShiftAssignment>();
    	teamAssociations = new ArrayList<TeamAssociation>();
    	constraintOverrides = new ArrayList<ConstraintOverride>();
    }
    
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getMaxComputationTime() {
		return maxComputationTime;
	}

	public void setMaxComputationTime(int maxComputationTime) {
		this.maxComputationTime = maxComputationTime;
	}

	public EmployeeRosterInfo getEmployeeRosterInfo() {
        return employeeRosterInfo;
    }

    public void setEmployeeRosterInfo(EmployeeRosterInfo employeeRosterInfo) {
        this.employeeRosterInfo = employeeRosterInfo;
    }

    public List<Skill> getSkillList() {
        return skillList;
    }

    public void setSkillList(List<Skill> skillList) {
        this.skillList = skillList;
    }

    public List<ShiftType> getShiftTypeList() {
        return shiftTypeList;
    }

    public void setShiftTypeList(List<ShiftType> shiftTypeList) {
        this.shiftTypeList = shiftTypeList;
    }

    public List<ShiftSkillRequirement> getShiftTypeSkillRequirementList() {
        return shiftTypeSkillRequirementList;
    }

    public void setShiftTypeSkillRequirementList(List<ShiftSkillRequirement> shiftTypeSkillRequirementList) {
        this.shiftTypeSkillRequirementList = shiftTypeSkillRequirementList;
    }

    public List<Pattern> getPatternList() {
        return patternList;
    }

    public void setPatternList(List<Pattern> patternList) {
        this.patternList = patternList;
    }

    public List<Contract> getContractList() {
        return contractList;
    }

    public void setContractList(List<Contract> contractList) {
        this.contractList = contractList;
    }

    public List<ContractLine> getContractLineList() {
        return contractLineList;
    }

    public void setContractLineList(List<ContractLine> contractLineList) {
        this.contractLineList = contractLineList;
    }

    public List<PatternContractLine> getPatternContractLineList() {
        return patternContractLineList;
    }

    public void setPatternContractLineList(List<PatternContractLine> patternContractLineList) {
        this.patternContractLineList = patternContractLineList;
    }

    @ValueRangeProvider(id = "employeeRange")
    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<SkillProficiency> getSkillProficiencyList() {
        return skillProficiencyList;
    }

    public void setSkillProficiencyList(List<SkillProficiency> skillProficiencyList) {
        this.skillProficiencyList = skillProficiencyList;
    }

    public List<ShiftDate> getShiftDateList() {
        return shiftDateList;
    }

    public void setShiftDateList(List<ShiftDate> shiftDateList) {
        this.shiftDateList = shiftDateList;
    }

    public List<Shift> getShiftList() {
        return shiftList;
    }

    public void setShiftList(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    @PlanningEntityCollectionProperty
    public List<ShiftAssignment> getShiftAssignmentList() {
        return shiftAssignmentList;
    }

    public void setShiftAssignmentList(List<ShiftAssignment> shiftAssignmentList) {
        this.shiftAssignmentList = shiftAssignmentList;
    }

    public List<TeamAssociation> getTeamAssoctiations() {
		return teamAssociations;
	}


	public void setTeamAssoctiations(List<TeamAssociation> teamAssoctiations) {
		this.teamAssociations = teamAssoctiations;
	}


	public List<ConstraintOverride> getConstraintOverrides() {
		return constraintOverrides;
	}


	public void setConstraintOverrides(List<ConstraintOverride> constraintOverrides) {
		this.constraintOverrides = constraintOverrides;
	}


	public BendableScore getScore() {
        return score;
    }

    public void setScore(BendableScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int getMaximumUnimprovedSecondsSpent() {
		return maxUnimprovedSecondsSpent;
	}


	public void setMaximumUnimprovedSecondsSpent(int maxUnimprovedSecondsSpent) {
		this.maxUnimprovedSecondsSpent = maxUnimprovedSecondsSpent;
	}


	public Map<String, Integer> getNumEmployeesPerSkill() {
		return numEmployeesPerSkill;
	}


	public void setNumEmployeesPerSkill(Map<String, Integer> numEmployeesPerSkill) {
		this.numEmployeesPerSkill = numEmployeesPerSkill;
	}


	@JsonIgnore
    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.add(employeeRosterInfo);
        facts.addAll(skillList);
        facts.addAll(shiftTypeSkillRequirementList);
        facts.addAll(patternList);
        facts.addAll(contractList);
        facts.addAll(contractLineList);
        facts.addAll(patternContractLineList);
        facts.addAll(employeeList);
        facts.addAll(skillProficiencyList);
        facts.addAll(shiftDateList);
        facts.addAll(shiftList);
        facts.addAll(employeeTimeOffs);
        facts.addAll(teamAssociations);
        facts.addAll(constraintOverrides);
        // Do not add the planning entity's (shiftAssignmentList) because that will be done automatically
        return facts;
    }

    public Map<String, Integer> getNumEmployeesPerTeam() {
		return numEmployeesPerTeam;
	}


	public void setNumEmployeesPerTeam(Map<String, Integer> numEmployeesPerTeam) {
		this.numEmployeesPerTeam = numEmployeesPerTeam;
	}


	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmployeeSchedule)) {
            return false;
        } else {
            EmployeeSchedule other = (EmployeeSchedule) o;
            if (shiftAssignmentList.size() != other.shiftAssignmentList.size()) {
                return false;
            }
            for (Iterator<ShiftAssignment> it = shiftAssignmentList.iterator(), otherIt = other.shiftAssignmentList.iterator(); it.hasNext();) {
                ShiftAssignment shiftAssignment = it.next();
                ShiftAssignment otherShiftAssignment = otherIt.next();
                // Notice: we don't use equals()
                if (!shiftAssignment.solutionEquals(otherShiftAssignment)) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (ShiftAssignment shiftAssignment : shiftAssignmentList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(shiftAssignment.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }

	public List<TimeWindow> getEmployeeTimeOffs() {
		return employeeTimeOffs;
	}

	public void setEmployeeTimeOffs(List<TimeWindow> employeeTimeOff) {
		this.employeeTimeOffs = employeeTimeOff;
	}

}
