package com.emlogis.engine.domain.communication.constraints;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.dto.WeekdayRotationPatternCLDto;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;

public class WeekdayRotationConstraintDto extends ShiftConstraintDto {
	private WeekdayRotationPatternCLDto pattern;

	public WeekdayRotationConstraintDto(WeekdayRotationPattern info) {
		pattern = new WeekdayRotationPatternCLDto();
		pattern.setWeight(info.getWeight());
		pattern.setDayOfWeek(info.getDayOfWeek());
		pattern.setNumberOfDays(info.getNumberOfDays());
		pattern.setOutOfTotalDays(info.getOutOfTotalDays());
		pattern.setRotationType(info.getRotationType());
		pattern.setContractLineType(ContractLineType.CUSTOM);
	}
	
	public WeekdayRotationConstraintDto() {
	}
	
	public WeekdayRotationPatternCLDto getPattern() {
		return pattern;
	}

	public void setPattern(WeekdayRotationPatternCLDto pattern) {
		this.pattern = pattern;
	}
	
}
