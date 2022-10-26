package com.emlogis.model.tenant.dto;

import java.io.Serializable;

import org.joda.time.DateTimeZone;


public class UserAccountInfoDto implements Serializable {
		
	private UserAccountDto accountDto;
	private DateTimeZone   actualTimeZone ;
	
	public UserAccountDto getAccountDto() {
		return accountDto;
	}
	public void setAccountDto(UserAccountDto accountDto) {
		this.accountDto = accountDto;
	}
	public DateTimeZone getActualTimeZone() {
		return actualTimeZone;
	}
	public void setActualTimeZone(DateTimeZone actualTimeZone) {
		this.actualTimeZone = actualTimeZone;
	}

}