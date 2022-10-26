package com.emlogis.engine.domain.timeoff.dto;

import com.emlogis.engine.domain.timeoff.PreferenceType;

public class CIPreferenceDto extends CITimeWindowDto{
	private PreferenceType type;

	public CIPreferenceDto(){
		super();
	}
	
	
	public CIPreferenceDto(CIPreferenceDto dto){
		super(dto);
		this.type = dto.type;
	}
	
	public PreferenceType getType() {
		return type;
	}

	public void setType(PreferenceType type) {
		this.type = type;
	}
}
