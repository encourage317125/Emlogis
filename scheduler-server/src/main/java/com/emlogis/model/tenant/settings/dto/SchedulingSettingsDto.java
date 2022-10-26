package com.emlogis.model.tenant.settings.dto;

import com.emlogis.model.dto.BaseEntityDto;
import com.emlogis.model.tenant.ProfileDayType;
import com.emlogis.model.tenant.settings.scheduling.OptimizationSettingList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class SchedulingSettingsDto extends BaseEntityDto {
	
	private boolean	override;	
	
    // Site configuration settings   
    private boolean breakShiftAtMidnightForDisplay;
    private boolean breakShiftAtMidnightForHours;
    private boolean reduceMaximumHoursForPTO;
    private boolean allowChainingAccrossTeams;
    private boolean allowChainingAccrossSkills;
    private boolean allowChainingAccrossMidnight;
    private boolean forceCompletion;
    private int consecutiveLimitOf12hoursDays = 4;
    private ProfileDayType profileDayType = ProfileDayType.DayShiftStarts;

    
    // Site optimization settings  
    private OptimizationSettingList optimizationSettings;
	
	public SchedulingSettingsDto() {}

	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public boolean isBreakShiftAtMidnightForDisplay() {
		return breakShiftAtMidnightForDisplay;
	}

	public void setBreakShiftAtMidnightForDisplay(
			boolean breakShiftAtMidnightForDisplay) {
		this.breakShiftAtMidnightForDisplay = breakShiftAtMidnightForDisplay;
	}

	public boolean isBreakShiftAtMidnightForHours() {
		return breakShiftAtMidnightForHours;
	}

	public void setBreakShiftAtMidnightForHours(boolean breakShiftAtMidnightForHours) {
		this.breakShiftAtMidnightForHours = breakShiftAtMidnightForHours;
	}

	public boolean isReduceMaximumHoursForPTO() {
		return reduceMaximumHoursForPTO;
	}

	public void setReduceMaximumHoursForPTO(boolean reduceMaximumHoursForPTO) {
		this.reduceMaximumHoursForPTO = reduceMaximumHoursForPTO;
	}

	public boolean isAllowChainingAccrossTeams() {
		return allowChainingAccrossTeams;
	}

	public void setAllowChainingAccrossTeams(boolean allowChainingAccrossTeams) {
		this.allowChainingAccrossTeams = allowChainingAccrossTeams;
	}

	public boolean isAllowChainingAccrossSkills() {
		return allowChainingAccrossSkills;
	}

	public void setAllowChainingAccrossSkills(boolean allowChainingAccrossSkills) {
		this.allowChainingAccrossSkills = allowChainingAccrossSkills;
	}

	public boolean isAllowChainingAccrossMidnight() {
		return allowChainingAccrossMidnight;
	}

	public void setAllowChainingAccrossMidnight(boolean allowChainingAccrossMidnight) {
		this.allowChainingAccrossMidnight = allowChainingAccrossMidnight;
	}

	public boolean isForceCompletion() {
		return forceCompletion;
	}

	public void setForceCompletion(boolean forceCompletion) {
		this.forceCompletion = forceCompletion;
	}

	public int getConsecutiveLimitOf12hoursDays() {
		return consecutiveLimitOf12hoursDays;
	}

	public void setConsecutiveLimitOf12hoursDays(int consecutiveLimitOf12hoursDays) {
		this.consecutiveLimitOf12hoursDays = consecutiveLimitOf12hoursDays;
	}

	public ProfileDayType getProfileDayType() {
		return profileDayType;
	}

	public void setProfileDayType(ProfileDayType profileDayType) {
		this.profileDayType = profileDayType;
	}

	public OptimizationSettingList getOptimizationSettings() {
		return optimizationSettings;
	}

	public void setOptimizationSettings(OptimizationSettingList optimizationSettings) {
		this.optimizationSettings = optimizationSettings;
	}

}
