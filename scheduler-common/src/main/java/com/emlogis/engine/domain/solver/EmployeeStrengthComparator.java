package com.emlogis.engine.domain.solver;

import com.emlogis.engine.domain.Employee;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

/**
 * The more skills the employee has and the more teams he/she belongs to
 * the stronger that employee is as a variable. The more days of they have 
 * the weaker they are
 * 
 * @author emlogis
 *
 */
public class EmployeeStrengthComparator implements Comparator<Employee> {

	@Override
	public int compare(Employee o1, Employee o2) {
		if(o1 == null) return 1;
		if(o2 == null) return -1;
		return new CompareToBuilder()
				.append(o1.getSkillIds().size(), o2.getSkillIds().size())
				.append(o1.getTeamIds().size(), o2.getTeamIds().size())
				.append(o2.getNumberOfDaysOffInPlanningPeriod(), o1.getNumberOfDaysOffInPlanningPeriod())
				.toComparison();
	}

}
