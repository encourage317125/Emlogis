package com.emlogis.engine.domain.timeoff.dto;

import com.emlogis.engine.domain.timeoff.PreferenceType;

public class CDPreferenceDto extends CDTimeWindowDto {
	private PreferenceType type;

	public CDPreferenceDto(){
		super();
	}
	
	public CDPreferenceDto(CDPreferenceDto dto){
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
