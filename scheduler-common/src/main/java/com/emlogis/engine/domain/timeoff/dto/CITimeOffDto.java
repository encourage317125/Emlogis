package com.emlogis.engine.domain.timeoff.dto;

public class CITimeOffDto extends CITimeWindowDto {
	private boolean isPTO;
	
	public CITimeOffDto() {
		super();
	}
	
	public CITimeOffDto(CITimeOffDto dto){
		super(dto);
		this.isPTO = dto.isPTO;
	}

	public boolean isPTO() {
		return isPTO;
	}

	public void setPTO(boolean isPTO) {
		this.isPTO = isPTO;
	}
}
