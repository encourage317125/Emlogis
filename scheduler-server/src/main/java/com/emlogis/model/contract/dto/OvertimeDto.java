package com.emlogis.model.contract.dto;

import com.emlogis.model.dto.Dto;

public class OvertimeDto extends Dto {
	
	private int dailyOvertimeMins = -1;    // daily overtime, in minutes, -1 = unspecified
	private int weeklyOvertimeMins = -1;   // weekly overtime, in minutes, -1 = unspecified
	private int biweeklyOvertimeMins = -1; // biweekly overtime, in minutes, -1 = unspecified
	
	public int getDailyOvertimeMins() {
		return dailyOvertimeMins;
	}

	public void setDailyOvertimeMins(int dailyOvertimeMins) {
		this.dailyOvertimeMins = dailyOvertimeMins;
	}

    public int getWeeklyOvertimeMins() {
		return weeklyOvertimeMins;
	}

    public void setWeeklyOvertimeMins(int weeklyOvertimeMins) {
		this.weeklyOvertimeMins = weeklyOvertimeMins;
	}

    public int getBiweeklyOvertimeMins() {
		return biweeklyOvertimeMins;
	}

    public void setBiweeklyOvertimeMins(int biweeklyOvertimeMins) {
		this.biweeklyOvertimeMins = biweeklyOvertimeMins;
	}

}
