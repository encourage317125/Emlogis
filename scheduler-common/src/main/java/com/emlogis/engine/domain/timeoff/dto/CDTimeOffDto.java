package com.emlogis.engine.domain.timeoff.dto;


public class CDTimeOffDto extends CDTimeWindowDto {
	private boolean isPTO;
	
	public CDTimeOffDto() {
		super();
	}
	
	public CDTimeOffDto(CDTimeOffDto dto){
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
