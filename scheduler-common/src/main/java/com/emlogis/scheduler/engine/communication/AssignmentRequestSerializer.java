package com.emlogis.scheduler.engine.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.EmployeeRosterInfo;
import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.ShiftSkillRequirement;
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.Skill;
import com.emlogis.engine.domain.SkillProficiency;
import com.emlogis.engine.domain.communication.ShiftAssignmentDto;
import com.emlogis.engine.domain.contract.ConstraintOverride;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.BooleanContractLine;
import com.emlogis.engine.domain.contract.contractline.ContractLine;
import com.emlogis.engine.domain.contract.contractline.MinMaxContractLine;
import com.emlogis.engine.domain.contract.contractline.PatternContractLine;
import com.emlogis.engine.domain.contract.contractline.dto.BooleanCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.ContractLineDto;
import com.emlogis.engine.domain.contract.contractline.dto.IntMinMaxCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.WeekdayRotationPatternCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.WeekendWorkPatternCLDto;
import com.emlogis.engine.domain.contract.dto.ConstraintOverrideDto;
import com.emlogis.engine.domain.contract.dto.ContractDto;
import com.emlogis.engine.domain.contract.patterns.CompleteWeekendWorkPattern;
import com.emlogis.engine.domain.contract.patterns.Pattern;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;
import com.emlogis.engine.domain.dto.AssignmentRequestDto;
import com.emlogis.engine.domain.dto.EmployeeDto;
import com.emlogis.engine.domain.dto.EmployeeRosterDto;
import com.emlogis.engine.domain.dto.EmployeeSkillDto;
import com.emlogis.engine.domain.dto.EmployeeTeamDto;
import com.emlogis.engine.domain.dto.ShiftDto;
import com.emlogis.engine.domain.dto.SkillDto;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.timeoff.CDPreference;
import com.emlogis.engine.domain.timeoff.CDTimeOff;
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.domain.timeoff.CIPreference;
import com.emlogis.engine.domain.timeoff.CITimeOff;
import com.emlogis.engine.domain.timeoff.CITimeWindow;
import com.emlogis.engine.domain.timeoff.TimeWindow;
import com.emlogis.engine.domain.timeoff.dto.CDPreferenceDto;
import com.emlogis.engine.domain.timeoff.dto.CDTimeOffDto;
import com.emlogis.engine.domain.timeoff.dto.CDTimeWindowDto;
import com.emlogis.engine.domain.timeoff.dto.CIPreferenceDto;
import com.emlogis.engine.domain.timeoff.dto.CITimeOffDto;
import com.emlogis.engine.domain.timeoff.dto.CITimeWindowDto;
import com.emlogis.engine.domain.timeoff.dto.TimeWindowDto;

public class AssignmentRequestSerializer {

	private DateTimeZone scheduleTimeZone;
	
	private class ContractInfo {
		List<Contract> contracts = new ArrayList<>();
		List<ContractLine> contractLines = new ArrayList<>();
		List<Pattern> patterns = new ArrayList<>();
		List<PatternContractLine> patternContractLines = new ArrayList<>();
	}

	private List<TimeWindow> getEmployeeTimeOffs(List<EmployeeDto> employeeDtos) {
		List<TimeWindow> result = new ArrayList<>();

		for (EmployeeDto employeeDto : employeeDtos) {
			List<TimeWindowDto> timeOffDtos = employeeDto
					.getEmployeeTimeOffDtos();

			if (timeOffDtos != null) {
				for (TimeWindowDto timeOffDto : timeOffDtos) {
					TimeWindow timeOff = null;
					if (timeOffDto instanceof CITimeWindowDto) {
						if (timeOffDto instanceof CITimeOffDto) {
							timeOff = new CITimeOff();
						} else if (timeOffDto instanceof CIPreferenceDto) {
							timeOff = new CIPreference();
						} else {
							continue; // Unrecognized time off class
						}

						((CITimeWindow) timeOff)
								.setDayOfWeek(((CITimeWindowDto) timeOffDto)
										.getDayOfWeek());
						((CITimeWindow) timeOff).setCdOverrides(((CITimeWindowDto) timeOffDto).getCdOverrides());
						((CITimeWindow) timeOff).setEffectiveStart(((CITimeWindowDto) timeOffDto).getEffectiveStart());
						((CITimeWindow) timeOff).setEffectiveEnd(((CITimeWindowDto) timeOffDto).getEffectiveEnd());

					} else if (timeOffDto instanceof CDTimeWindowDto) {
						if (timeOffDto instanceof CDTimeOffDto) {
							timeOff = new CDTimeOff();
						} else if (timeOffDto instanceof CDPreferenceDto) {
							timeOff = new CDPreference();
						} else {
							continue; // Unrecognized time offclass
						}

						DateTime startDate = ((CDTimeWindowDto) timeOffDto)
								.getDayOffStart().withZone(scheduleTimeZone);
						((CDTimeWindow) timeOff).setDayOffStart(startDate);

						DateTime endDate = ((CDTimeWindowDto) timeOffDto)
								.getDayOffEnd().withZone(scheduleTimeZone);
						if (endDate == null) { // If end date is not specified
												// it ends on startDate
							((CDTimeWindow) timeOff).setDayOffEnd(startDate);
						} else {
							((CDTimeWindow) timeOff).setDayOffEnd(endDate);
						}
					}
					if (timeOff != null) {
						timeOff.setEmployeeId(timeOffDto.getEmployeeId());
						timeOff.setAllDay(timeOffDto.isAllDay());
						timeOff.setPTO(timeOffDto.isPTO());
						timeOff.setWeight(timeOffDto.getWeight());
						
						LocalTime startTime = timeOffDto.getStartTime();
						LocalTime endTime = timeOffDto.getEndTime();
						
						// Set up exception for CI Time Window
						// If EndTime == 00:00:00 set it to TimeWindow.END_OF_DAY_TIME
						if(timeOffDto instanceof CITimeWindowDto && endTime.equals(LocalTime.MIDNIGHT)){
							endTime = TimeWindow.END_OF_DAY_TIME;
						}
						timeOff.setTimeWindow(startTime, endTime);
						
						result.add(timeOff);
					}
				}
			}
		}

		return result;
	}

	private List<ConstraintOverride> getConstraintOverrides(List<ConstraintOverrideDto> constraintOverrideDtos,
                                                            List<Employee> employees) {
        List<ConstraintOverride> result = new ArrayList<>();

        for (ConstraintOverrideDto constraintOverrideDto : constraintOverrideDtos) {
            ConstraintOverride constraintOverride = new ConstraintOverride();
            constraintOverride.setType(constraintOverrideDto.getType());
            for (Employee employee : employees) {
                if (constraintOverrideDto.getEmployeeId().equals(employee.getEmployeeId())) {
                    constraintOverride.setEmployee(employee);
                    break;
                }
            }
            result.add(constraintOverride);
        }

        return result;
    }

    public EmployeeSchedule mapToEmployeeSchedule(AssignmentRequestDto assignmentRequestDto) {
        EmployeeSchedule result = new EmployeeSchedule();

        result.setMaxComputationTime(assignmentRequestDto.getMaxComputationTime());
        result.setMaximumUnimprovedSecondsSpent(assignmentRequestDto.getMaximumUnimprovedSecondsSpent());
        result.setEmployeeRosterInfo(createEmployeeRosterInfo(assignmentRequestDto.getEmployeeRosterDto()));
        result.setShiftList(getShifts(assignmentRequestDto.getShiftDtos()));
        result.setEmployeeList(getEmployees(assignmentRequestDto.getEmployeeDtos()));
        result.setSkillList(getSkills(assignmentRequestDto.getSkillDtos()));

        result.setShiftTypeSkillRequirementList(getShiftSkillRequirement(result.getShiftList(), result.getSkillList()));
      
        Map<String, Integer> numEmployeesPerTeam = new HashMap<String, Integer>();
        result.setTeamAssoctiations(getTeamAssociations(assignmentRequestDto.getEmployeeTeamDtos(),
                result.getEmployeeList(), numEmployeesPerTeam));
        result.setNumEmployeesPerTeam(numEmployeesPerTeam);
        
        Map<String, Integer> numEmployeesPerSkill = new HashMap<String, Integer>();
        result.setSkillProficiencyList(getSkillProficiencies(assignmentRequestDto.getEmployeeSkillDtos(),
                result.getEmployeeList(), result.getSkillList(), numEmployeesPerSkill));
        result.setNumEmployeesPerSkill(numEmployeesPerSkill);
        
        result.setShiftDateList(getShiftDates(result.getShiftList()));
        result.setEmployeeTimeOffs(getEmployeeTimeOffs(assignmentRequestDto.getEmployeeDtos()));
        result.setConstraintOverrides(getConstraintOverrides(assignmentRequestDto.getConstraintOverrideDtos(),
                result.getEmployeeList()));
        result.setShiftAssignmentList(getShiftAssignments(assignmentRequestDto.getShiftAssignmentDtos(),
                result.getShiftList(), result.getEmployeeList()));

        ContractInfo contractInfo = getContractInfo(assignmentRequestDto.getContractDtos());

        result.setContractList(contractInfo.contracts);
        result.setContractLineList(contractInfo.contractLines);
        result.setPatternList(contractInfo.patterns);
        result.setPatternContractLineList(contractInfo.patternContractLines);

        return result;
    }

    private EmployeeRosterInfo createEmployeeRosterInfo(EmployeeRosterDto employeeRosterDto) {
        EmployeeRosterInfo result = new EmployeeRosterInfo();
        
        scheduleTimeZone = DateTimeZone.forID(employeeRosterDto.getTimeZone());

        result.setFirstDayOfWeek(employeeRosterDto.getFirstDayOfWeek());
        result.setWeekendDefinition(employeeRosterDto.getWeekendDefinition());
        result.setFirstShiftDate(employeeRosterDto.getFirstShiftDate());
        result.setLastShiftDate(employeeRosterDto.getLastShiftDate());
        result.setPlanningWindowStart(employeeRosterDto.getPlanningWindowStart());
        result.setTwoWeekOvertimeStartDate(employeeRosterDto.getTwoWeekOvertimeStartDate());
        result.setRuleWeightMultipliers(employeeRosterDto.getRuleWeightMultipliers());
        result.setScoringRulesToScoreLevelMap(employeeRosterDto.getScoringRulesToScoreLevelMap());
        result.setForceCompletionEnabled(employeeRosterDto.isForceCompletionEnabled());

        return result;
    }

    private List<Shift> getShifts(List<ShiftDto> shiftDtos) {
        List<Shift> result = new ArrayList<>();

        for (ShiftDto shiftDto : shiftDtos) {
            Shift shift = new Shift();
            shift.setRequiredEmployeeSize(shiftDto.getRequiredEmployeeSize());
            shift.setTeamId(shiftDto.getTeamId());
            shift.setSkillId(shiftDto.getSkillId());
            shift.setId(shiftDto.getId());
            
            shift.setExcessShift(shiftDto.isExcessShift());
    		shift.setStartDateTime(shiftDto.getStartDateTime().withZone(scheduleTimeZone));
    		shift.setEndDateTime(shiftDto.getEndDateTime().withZone(scheduleTimeZone));
    	  
    		shift.setBeingQualified(shiftDto.getBeingQualified());
    		shift.setBeingSwapped(shiftDto.isBeingSwapped());
            
    		result.add(shift);
        }

        return result;
    }

    private List<ShiftSkillRequirement> getShiftSkillRequirement(List<Shift> shifts, List<Skill> skills) {
        List<ShiftSkillRequirement> result = new ArrayList<>();

        for (Shift shift : shifts) {
            for (Skill skill : skills) {
                if (skill.getCode().equals(shift.getSkillId())) {
                    ShiftSkillRequirement shiftSkillRequirement = new ShiftSkillRequirement(shift, skill);
                    result.add(shiftSkillRequirement);
                    break;
                }
            }
        }

        return result;
    }

	private List<Employee> getEmployees(List<EmployeeDto> employeeDtos) {
		List<Employee> result = new ArrayList<>();

		for (EmployeeDto employeeDto : employeeDtos) {
			Employee employee = new Employee();
			employee.setEmployeeId(employeeDto.getId());
			employee.setFirstName(employeeDto.getFirstName());
			employee.setLastName(employeeDto.getLastName());
			employee.setScheduleable(employeeDto.isScheduleable());
			employee.setSeniority(employeeDto.getSeniority());
			employee.setStartDate(employeeDto.getStartDate());
			employee.setStopDate(employeeDto.getStopDate());
			
			employee.setHourlyRate(employeeDto.getHourlyRate());

			employee.setNumberOfDaysOffInPlanningPeriod(employeeDto.getEmployeeTimeOffDtos().size());

			result.add(employee);
		}

		return result;
	}

    private List<Skill> getSkills(List<SkillDto> skillDtos) {
        List<Skill> result = new ArrayList<>();

        for (SkillDto skillDto : skillDtos) {
            Skill skill = new Skill();
            skill.setName(skillDto.getName());
            skill.setCode(skillDto.getId());

            result.add(skill);
        }

        return result;
    }

    private List<TeamAssociation> getTeamAssociations(List<EmployeeTeamDto> employeeTeamDtos,
                                                      List<Employee> employees, Map<String, Integer> numEmployeesPerTeam) {
        List<TeamAssociation> result = new ArrayList<>();

        for (EmployeeTeamDto employeeTeamDto : employeeTeamDtos) {
        	String teamId = employeeTeamDto.getTeamId();
            Employee employee = findEmployee(employees, employeeTeamDto.getEmployeeId());

            if(employee == null) continue;
        	
            TeamAssociation teamAssociation = new TeamAssociation();
            
            teamAssociation.setEmployee(employee);
            teamAssociation.setTeamId(teamId);
            teamAssociation.setHomeTeam(employeeTeamDto.isHomeTeam());
            teamAssociation.setType(employeeTeamDto.getType());

            if(!numEmployeesPerTeam.containsKey(teamId)){
            	numEmployeesPerTeam.put(teamId, 1);
            } else {
            	numEmployeesPerTeam.put(teamId, numEmployeesPerTeam.get(teamId) + 1);
            }
            
            employee.getTeamIds().add(teamId);
            
            result.add(teamAssociation);
        }

        return result;
    }

    private Employee findEmployee(List<Employee> employees, String id) {
        for (Employee employee : employees) {
            if (StringUtils.equals(employee.getEmployeeId(), id)) {
                return employee;
            }
        }

        return null;
    }

    private ShiftType findShiftType(List<ShiftType> shiftTypes, String shiftId) {
        for (ShiftType shiftType : shiftTypes) {
            if (StringUtils.equals(shiftType.getShiftId(), shiftId)) {
                return shiftType;
            }
        }

        return null;
    }

    private Skill findSkill(List<Skill> skills, String id) {
        for (Skill skill : skills) {
            if (StringUtils.equals(skill.getCode(), id)) {
                return skill;
            }
        }

        return null;
    }

    private Shift findShift(List<Shift> shifts, String id) {
        for (Shift shift : shifts) {
            if (StringUtils.equals(shift.getId(), id)) {
                return shift;
            }
        }

        return null;
    }

    private List<SkillProficiency> getSkillProficiencies(List<EmployeeSkillDto> employeeSkillDtos,
                                                         List<Employee> employees, List<Skill> skills, Map<String, Integer> numEmployeesPerSkill) {
        List<SkillProficiency> result = new ArrayList<>();

        for (EmployeeSkillDto employeeSkillDto : employeeSkillDtos) {
        	String skillId = employeeSkillDto.getSkillId();
        	Employee employee = findEmployee(employees, employeeSkillDto.getEmployeeId());
        	
        	if(employee == null) continue; 
        	
            SkillProficiency skillProficiency = new SkillProficiency();
            
            skillProficiency.setPrimarySkill(employeeSkillDto.isPrimarySkill());
            skillProficiency.setSkillLevel(Integer.valueOf(employeeSkillDto.getSkillLevel()));
            skillProficiency.setEmployee(employee);
            skillProficiency.setSkill(findSkill(skills, skillId));

            if(!numEmployeesPerSkill.containsKey(skillId)){
            	numEmployeesPerSkill.put(skillId, 1);
            } else {
            	numEmployeesPerSkill.put(skillId, numEmployeesPerSkill.get(skillId) + 1);
            }
            
            employee.getSkillIds().add(skillId);
            
            result.add(skillProficiency);
        }

        return result;
    }

    private List<ShiftDate> getShiftDates(List<Shift> shifts) {
        Set<ShiftDate> shiftDates = new HashSet<>();

        for (Shift shift : shifts) {
            ShiftDate shiftDate = new ShiftDate();
            shiftDate.setDate(shift.getStartShiftDate().getDate());
            shiftDate.setDayIndex(shift.getStartShiftDate().getDayIndex());

            shiftDates.add(shiftDate);
        }

        List<ShiftDate> result = new ArrayList<>();
        result.addAll(shiftDates);
        return result;
    }

    private List<ShiftAssignment> getShiftAssignments(List<ShiftAssignmentDto> shiftAssignmentDtos,
                                                      List<Shift> shifts, List<Employee> employees) {
        List<ShiftAssignment> result = new ArrayList<>();

        for (ShiftAssignmentDto shiftAssignmentDto : shiftAssignmentDtos) {
            Shift shift = findShift(shifts, shiftAssignmentDto.getShiftId());

            ShiftAssignment shiftAssignment = new ShiftAssignment();
            shiftAssignment.setEmployee(findEmployee(employees, shiftAssignmentDto.getEmployeeId()));
            shiftAssignment.setShift(shift);
            shiftAssignment.setLocked(shiftAssignmentDto.isLocked());
            shiftAssignment.setIndexInShift(shift.getIndex());

            result.add(shiftAssignment);
        }

        return result;
    }

    private ContractInfo getContractInfo(List<ContractDto> contractDtos) {
        ContractInfo result = new ContractInfo();

        for (ContractDto contractDto : contractDtos) {
            Contract contract = new Contract();
            contract.setContractRefId(contractDto.getId());
            contract.setScope(contractDto.getScope());

            result.contracts.add(contract);

            if (contractDto.getContractLineDtos() != null) {
                for (ContractLineDto contractLineDto : contractDto.getContractLineDtos()) {
                    if (contractLineDto instanceof BooleanCLDto) {
                        BooleanContractLine booleanContractLine = new BooleanContractLine();
                        booleanContractLine.setEnabled(((BooleanCLDto) contractLineDto).isEnabled());
                        booleanContractLine.setWeight(((BooleanCLDto) contractLineDto).getWeight());
                        booleanContractLine.setContract(contract);
                        booleanContractLine.setContractLineType(contractLineDto.getContractLineType());

                        result.contractLines.add(booleanContractLine);
                    } else if (contractLineDto instanceof IntMinMaxCLDto) {
                        MinMaxContractLine minMaxContractLine = new MinMaxContractLine();
                        minMaxContractLine.setMaximumEnabled(((IntMinMaxCLDto) contractLineDto).isMaximumEnabled());
                        minMaxContractLine.setMinimumEnabled(((IntMinMaxCLDto) contractLineDto).isMinimumEnabled());
                        minMaxContractLine.setMaximumValue(((IntMinMaxCLDto) contractLineDto).getMaximumValue());
                        minMaxContractLine.setMinimumValue(((IntMinMaxCLDto) contractLineDto).getMinimumValue());
                        minMaxContractLine.setMaximumWeight(((IntMinMaxCLDto) contractLineDto).getMaximumWeight());
                        minMaxContractLine.setMinimumWeight(((IntMinMaxCLDto) contractLineDto).getMinimumWeight());
                        minMaxContractLine.setContract(contract);
                        minMaxContractLine.setContractLineType(contractLineDto.getContractLineType());


                        result.contractLines.add(minMaxContractLine);
                    } else if (contractLineDto instanceof WeekdayRotationPatternCLDto) {
                        WeekdayRotationPatternCLDto rotationPatternCLDto = (WeekdayRotationPatternCLDto) contractLineDto;

                        WeekdayRotationPattern weekdayRotationPattern = new WeekdayRotationPattern();
                        weekdayRotationPattern.setWeight(rotationPatternCLDto.getWeight());
                        weekdayRotationPattern.setDayOfWeek(rotationPatternCLDto.getDayOfWeek());
                        weekdayRotationPattern.setNumberOfDays(rotationPatternCLDto.getNumberOfDays());
                        weekdayRotationPattern.setOutOfTotalDays(rotationPatternCLDto.getOutOfTotalDays());
                        weekdayRotationPattern.setRotationType(rotationPatternCLDto.getRotationType());

                        result.patterns.add(weekdayRotationPattern);

                        result.patternContractLines.add(createPatternContractLine(contract, weekdayRotationPattern));
                    } else if (contractLineDto instanceof WeekendWorkPatternCLDto) {
                        WeekendWorkPatternCLDto workPatternCL = (WeekendWorkPatternCLDto) contractLineDto;

                        CompleteWeekendWorkPattern completeWeekendWorkPattern = new CompleteWeekendWorkPattern();
                        completeWeekendWorkPattern.setWeight(workPatternCL.getWeight());
                        completeWeekendWorkPattern.setDaysOffAfter(toDayOfWeekCollection(workPatternCL.getDaysOffAfter()));
                        completeWeekendWorkPattern.setDaysOffBefore(toDayOfWeekCollection(workPatternCL.getDaysOffBefore()));

                        result.patterns.add(completeWeekendWorkPattern);

                        result.patternContractLines.add(createPatternContractLine(contract, completeWeekendWorkPattern));
                    }
                }
            }
        }

        return result;
    }

    private PatternContractLine createPatternContractLine(Contract contract, Pattern pattern) {
        PatternContractLine result = new PatternContractLine();
        result.setContract(contract);
        result.setPattern(pattern);
        return result;
    }

    private Collection<DayOfWeek> toDayOfWeekCollection(String daysStr) {
        if (StringUtils.isEmpty(daysStr)) {
            return null;
        }

        String[] days = StringUtils.split(daysStr, ",");

        List<DayOfWeek> result = new ArrayList<>();
        for (String day : days) {
            result.add(DayOfWeek.valueOfDayName(day));
        }
        return result;
    }


}
