package com.emlogis.engine.domain.dto;

import com.emlogis.engine.domain.communication.ShiftAssignmentDto;
import com.emlogis.engine.domain.contract.dto.ConstraintOverrideDto;
import com.emlogis.engine.domain.contract.dto.ContractDto;

import java.io.Serializable;
import java.util.List;

public class AssignmentRequestDto implements Serializable {

    private int maxComputationTime = -1;
    private int maximumUnimprovedSecondsSpent = 0;
    private EmployeeRosterDto employeeRosterDto;
    private List<ConstraintOverrideDto> constraintOverrideDtos;
    private List<SkillDto> skillDtos;
    private List<EmployeeDto> employeeDtos;
    private List<EmployeeSkillDto> employeeSkillDtos;
    private List<ContractDto> contractDtos;
    private List<EmployeeTeamDto> employeeTeamDtos;
    private List<ShiftDto> shiftDtos;
    private List<ShiftAssignmentDto> shiftAssignmentDtos;

    public int getMaxComputationTime() {
        return maxComputationTime;
    }

    public void setMaxComputationTime(int maxComputationTime) {
        this.maxComputationTime = maxComputationTime;
    }

    public int getMaximumUnimprovedSecondsSpent() {
		return maximumUnimprovedSecondsSpent;
	}

	public void setMaximumUnimprovedSecondsSpent(int maximumUnimprovedSecondsSpent) {
		this.maximumUnimprovedSecondsSpent = maximumUnimprovedSecondsSpent;
	}

	public EmployeeRosterDto getEmployeeRosterDto() {
        return employeeRosterDto;
    }

    public void setEmployeeRosterDto(EmployeeRosterDto employeeRosterDto) {
        this.employeeRosterDto = employeeRosterDto;
    }

    public List<ConstraintOverrideDto> getConstraintOverrideDtos() {
        return constraintOverrideDtos;
    }

    public void setConstraintOverrideDtos(List<ConstraintOverrideDto> constraintOverrideDtos) {
        this.constraintOverrideDtos = constraintOverrideDtos;
    }

    public List<SkillDto> getSkillDtos() {
        return skillDtos;
    }

    public void setSkillDtos(List<SkillDto> skillDtos) {
        this.skillDtos = skillDtos;
    }

    public List<EmployeeDto> getEmployeeDtos() {
        return employeeDtos;
    }

    public void setEmployeeDtos(List<EmployeeDto> employeeDtos) {
        this.employeeDtos = employeeDtos;
    }

    public List<EmployeeSkillDto> getEmployeeSkillDtos() {
        return employeeSkillDtos;
    }

    public void setEmployeeSkillDtos(List<EmployeeSkillDto> employeeSkillDtos) {
        this.employeeSkillDtos = employeeSkillDtos;
    }

    public List<ContractDto> getContractDtos() {
        return contractDtos;
    }

    public void setContractDtos(List<ContractDto> contractDtos) {
        this.contractDtos = contractDtos;
    }

    public List<EmployeeTeamDto> getEmployeeTeamDtos() {
        return employeeTeamDtos;
    }

    public void setEmployeeTeamDtos(List<EmployeeTeamDto> employeeTeamDtos) {
        this.employeeTeamDtos = employeeTeamDtos;
    }

    public List<ShiftDto> getShiftDtos() {
        return shiftDtos;
    }

    public void setShiftDtos(List<ShiftDto> shiftDtos) {
        this.shiftDtos = shiftDtos;
    }

    public List<ShiftAssignmentDto> getShiftAssignmentDtos() {
        return shiftAssignmentDtos;
    }

    public void setShiftAssignmentDtos(List<ShiftAssignmentDto> shiftAssignmentDtos) {
        this.shiftAssignmentDtos = shiftAssignmentDtos;
    }

}
