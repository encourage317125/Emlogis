package com.emlogis.model.contract;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;

import java.util.HashMap;
import java.util.Map;


public class ContractConfig {
	
	public static Map<Class, ElligibleContractLines> contractConfigMap = new HashMap<>();
		
	static {
		
		ElligibleContractLines siteContractLines = new ElligibleContractLines();
		
		siteContractLines.setContractLineType(ContractLineType.CONSECUTIVE_WORKING_DAYS, 1);
		siteContractLines.setContractLineType(ContractLineType.HOURS_BETWEEN_DAYS, 1);
		siteContractLines.setContractLineType(ContractLineType.HOURS_PER_DAY, 1);
		siteContractLines.setContractLineType(ContractLineType.HOURS_PER_WEEK, 1);
		siteContractLines.setContractLineType(ContractLineType.DAYS_PER_WEEK, 1);
		siteContractLines.setContractLineType(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, 1);
		siteContractLines.setContractLineType(ContractLineType.CUSTOM, 7);
		siteContractLines.setContractLineType(ContractLineType.DAILY_OVERTIME, 1);
		siteContractLines.setContractLineType(ContractLineType.WEEKLY_OVERTIME, 1);
		siteContractLines.setContractLineType(ContractLineType.TWO_WEEK_OVERTIME, 1);
		siteContractLines.setContractLineType(ContractLineType.COMPLETE_WEEKENDS, 1);
		
		ElligibleContractLines teamContractLines = new ElligibleContractLines();
		
		teamContractLines.setContractLineType(ContractLineType.CONSECUTIVE_WORKING_DAYS, 1);
		teamContractLines.setContractLineType(ContractLineType.HOURS_BETWEEN_DAYS, 1);
		teamContractLines.setContractLineType(ContractLineType.HOURS_PER_DAY, 1);
		teamContractLines.setContractLineType(ContractLineType.HOURS_PER_WEEK, 1);
		teamContractLines.setContractLineType(ContractLineType.DAYS_PER_WEEK, 1);
		teamContractLines.setContractLineType(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, 1);
		teamContractLines.setContractLineType(ContractLineType.CUSTOM, 7);
		teamContractLines.setContractLineType(ContractLineType.DAILY_OVERTIME, 1);
		teamContractLines.setContractLineType(ContractLineType.WEEKLY_OVERTIME, 1);
		teamContractLines.setContractLineType(ContractLineType.TWO_WEEK_OVERTIME, 1);
		teamContractLines.setContractLineType(ContractLineType.COMPLETE_WEEKENDS, 1);
		
		ElligibleContractLines employeeContractLines = new ElligibleContractLines();
		
		employeeContractLines.setContractLineType(ContractLineType.CONSECUTIVE_WORKING_DAYS, 1);
		employeeContractLines.setContractLineType(ContractLineType.HOURS_BETWEEN_DAYS, 1);
		employeeContractLines.setContractLineType(ContractLineType.HOURS_PER_DAY, 1);
		employeeContractLines.setContractLineType(ContractLineType.HOURS_PER_WEEK, 1);
		employeeContractLines.setContractLineType(ContractLineType.DAYS_PER_WEEK, 1);
		employeeContractLines.setContractLineType(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, 1);
		employeeContractLines.setContractLineType(ContractLineType.CUSTOM, 7);
		employeeContractLines.setContractLineType(ContractLineType.DAILY_OVERTIME, 1);
		employeeContractLines.setContractLineType(ContractLineType.WEEKLY_OVERTIME, 1);
		employeeContractLines.setContractLineType(ContractLineType.TWO_WEEK_OVERTIME, 1);
		employeeContractLines.setContractLineType(ContractLineType.COMPLETE_WEEKENDS, 1);
		
		contractConfigMap.put(SiteContract.class, siteContractLines);
		contractConfigMap.put(TeamContract.class, teamContractLines);
		contractConfigMap.put(EmployeeContract.class, employeeContractLines);
	}
	
	public static Integer getMaxForCLType(Contract contract, ContractLineType contractLineType) {
		Integer retVal = null;
		if(contract instanceof SiteContract) {
			retVal = contractConfigMap.get(SiteContract.class).getMaxForType(contractLineType);
		} else if (contract instanceof TeamContract) {
			retVal = contractConfigMap.get(TeamContract.class).getMaxForType(contractLineType);
		} else if (contract instanceof EmployeeContract) {
			retVal = contractConfigMap.get(EmployeeContract.class).getMaxForType(contractLineType);
		}
		
		return retVal; 
	}
}

class ElligibleContractLines {
	Map<ContractLineType, Integer> maxTypeMap = new HashMap<ContractLineType, Integer>();
	
	Integer getMaxForType(ContractLineType contractLineType) {
		return maxTypeMap.get(contractLineType); 
	}
	
	void setContractLineType(ContractLineType contractLineType, Integer max) {
		maxTypeMap.put(contractLineType, max);
	}
	
}
