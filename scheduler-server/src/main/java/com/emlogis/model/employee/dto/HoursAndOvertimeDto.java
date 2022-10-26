package com.emlogis.model.employee.dto;

public class HoursAndOvertimeDto {

    private int minHoursPerDay = -1;
    private int maxHoursPerDay = -1;
    private int minHoursPerWeek = -1;
    private int maxHoursPerWeek = -1;
    private int daysPerWeek;
    private int consecutiveDays = -1;
    private int primarySkillHours = -1; // in minutes
    private OvertimeDto overtimeDto;

    public static class OvertimeDto {

        private int dailyOvertimeMins = -1; // daily overtime, in minutes
        private int weeklyOvertimeMins = -1; // weekly overtime, in minutes
        private int biweeklyOvertimeMins = -1; // biweekly overtime, in minutes

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

    public int getMinHoursPerDay() {
        return minHoursPerDay;
    }

    public void setMinHoursPerDay(int minHoursPerDay) {
        this.minHoursPerDay = minHoursPerDay;
    }

    public int getMaxHoursPerDay() {
        return maxHoursPerDay;
    }

    public void setMaxHoursPerDay(int maxHoursPerDay) {
        this.maxHoursPerDay = maxHoursPerDay;
    }

    public int getMinHoursPerWeek() {
        return minHoursPerWeek;
    }

    public void setMinHoursPerWeek(int minHoursPerWeek) {
        this.minHoursPerWeek = minHoursPerWeek;
    }

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public void setMaxHoursPerWeek(int maxHoursPerWeek) {
        this.maxHoursPerWeek = maxHoursPerWeek;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public void setDaysPerWeek(int daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public int getPrimarySkillHours() {
        return primarySkillHours;
    }

    public void setPrimarySkillHours(int primarySkillHours) {
        this.primarySkillHours = primarySkillHours;
    }

    public OvertimeDto getOvertimeDto() {
        if (overtimeDto == null) {
            overtimeDto = new OvertimeDto();
        }
        return overtimeDto;
    }

    public void setOvertimeDto(OvertimeDto overtimeDto) {
        this.overtimeDto = overtimeDto;
    }
}
