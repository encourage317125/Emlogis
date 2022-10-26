package com.emlogis.engine.domain.dto;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;

import com.emlogis.engine.domain.EmployeeRosterInfo;
import com.emlogis.engine.domain.communication.ShiftAssignmentDto;
import com.emlogis.engine.domain.contract.dto.ConstraintOverrideDto;
import com.emlogis.engine.domain.timeoff.dto.TimeWindowDto;

@PlanningSolution
public class EmployeeScheduleDto  {

    private String code;

    private EmployeeRosterInfo employeeRosterInfo;
    /*
    private List<SkillDto> 		skillDtoList;
//    private List<ShiftType> 	shiftTypeList;				// should no longer be required
//    private List<ShiftSkillRequirement> shiftTypeSkillRequirementList;	// idem
//    private List<PatternDto> 	patternDtos; ???? needed ? should be included in contractLineDtos
    private List<ContractDto> 	contractDtos;  
    private List<ContractLineDto> contractLineDtos;
//    private List<PatternContractLine> patternContractLineList; ???? needed ? should be rebuilt from patternDtos in contractLineDtos
    private List<EmployeeDto> 	employeeDtoList;
    private List<SkillProficiencyDto> skillProficiencyDtos;
    private List<ShiftDate> 	shiftDates;
    private List<ShiftDto> 		shiftDtos;
    private List<TimeOffDto> 	employeeTimeOffDtos;
    private List<TeamAssociationDto> teamAssociationDtos;
    */
    
    private List<TimeWindowDto> 			employeeTimeOffDtos;					

    private List<ConstraintOverrideDto> constraintOverrideDtos;		
    
    private List<ShiftAssignmentDto> 	shiftAssignments;
    
    
    public EmployeeScheduleDto(){
    	/*
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
    	employeeTimeOff = new ArrayList<TimeOff>();
    	shiftAssignmentList = new ArrayList<ShiftAssignment>();
    	teamAssociations = new ArrayList<TeamAssociation>();
    	constraintOverrides = new ArrayList<ConstraintOverride>();
    	*/
    }
    
    

}
