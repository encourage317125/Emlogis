package com.emlogis.model.tenant.settings;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.ProfileDayType;
import com.emlogis.model.tenant.settings.scheduling.BooleanOptimizationSetting;
import com.emlogis.model.tenant.settings.scheduling.OptimPreferenceOptimizationSetting;
import com.emlogis.model.tenant.settings.scheduling.OptimizationSetting;
import com.emlogis.model.tenant.settings.scheduling.OptimizationSettingList;
import com.emlogis.model.tenant.settings.scheduling.OptimizationSettingName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
public class SchedulingSettings extends BaseEntity implements Cloneable {
	
	private boolean	override = false;			// flag indicating if this instance is overriding the parent SchedulingSettings

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

    
    // Site optimization settings  + need to specify the order of these optimizations 
    //@JsonIgnore
    @Column(unique = true, length = 1024)
    private String optimizationSettings; 			// json serialized ordered list of optimization settings
/*
    // Couple Weekends:	Treat Saturday and Sunday as a single object.  If this Setting is selected, the Optimizer will ensure that the employee is scheduled, or not, in the same way on Saturday and Sunday
    // Minimize open shifts:	Ensure there are as few open shifts as possible
    // Distribute open shifts:	Distribute open shifts across the schedule period
    // Minimize excess shifts:	Ensure there are as few extra shifts as possible
    // Distribute excess shifts:	Distribute extra shifts across the schedule period
    // Team  - Scattering:	Randomly assign an employee to any of the teams he can regularly work on.   Disables the Team - Value optimization
    // Horizontal Clustering: Group an employee’s scheduled days together rather than distributing across the week
    // Vertical Clustering:	Ensure an employee’s shift start times on schedule days are as close as possible
    // Double up: Avoid changing an employee’s assign Skill and Team if the employee is scheduled on back to back shifts
    // Distribute worked weekends: Ensure that “worked weekends” are evenly distributed for those employees who are assigned a Weekend Rotation availability constraint 
    // Primary Skill Value:	Bias the employee’s scheduled hours to the Primary Skill
    // Team - Value: Bias an employee’s hours toward their Home Team  
*/
    
/*    
    Yes/No	Manage placement of open shifts	The Setting to specify which day/team/skill combination to avoid assigning open shifts
    Yes/No	Management placement of excess shifts	The Setting to specify which day/team/skill combination to avoid assigning extra shifts
    		
	// optimizationPreference 	Cost-Overtime-Preference (COP) Group	Optimize for the three values are specified by the user when a schedule is generated
*/
        
    public SchedulingSettings() { 
    	super();
    	setupDefaultOptimizationSettings();
    }

    public SchedulingSettings(PrimaryKey primaryKey) {
        super(primaryKey);
    	setupDefaultOptimizationSettings();
    }


    private void setupDefaultOptimizationSettings() {
		this.setOptimizationSettings(getDefaultOptimizationSettings());
	}

    private OptimizationSettingList getDefaultOptimizationSettings() {
    	
    	OptimizationSettingList settings = new OptimizationSettingList();
    	
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.CoupleWeekends,false));
//    	ManageOpenShiftPlacement("ManageOpenShiftPlacement")
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.MinimizeNbOfOpenShifts,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.DistributeOpenShifts,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.MinimizeNbOfExcessShifts,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.DistributeWorkedWeekends,false));
    	settings.add(new OptimPreferenceOptimizationSetting(OptimizationSettingName.OptimizationPreference,"COP"));
//			ManageExcessShiftPlacement("ManageExcessShiftPlacement"),
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.PrimarySkillValue,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.TeamScattering,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.DoubleUp,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.HomeTeamValue,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.ClusterHorizontally,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.ClusterVertically,false));
    	settings.add(new BooleanOptimizationSetting(OptimizationSettingName.DistributeExcessShifts,false));
    	

    /*
    	 to be added the following 3 optimization.... once we have determined their type
			ManageOpenShiftPlacement("ManageOpenShiftPlacement"),
			ManageExcessShiftPlacement("ManageExcessShiftPlacement"),
			OptimizationPreference("OptimizationPreference"),
	*/
		return settings;
	}
    
    public OptimizationSettingList getOptimizationSettings() {
    	if (StringUtils.isBlank(optimizationSettings)) {
    		return getDefaultOptimizationSettings();
    	} else {
    		return EmlogisUtils.fromJsonString(optimizationSettings, OptimizationSettingList.class);
    	}
	}
    
	public void setOptimizationSettings(OptimizationSettingList optimizationSettings) {
		this.optimizationSettings = EmlogisUtils.toJsonString(optimizationSettings);
	}	    
	
	public void setOptimizationSettings(String optimizationSettings) {
		if (StringUtils.isBlank(optimizationSettings)) {
			this.optimizationSettings = null;
		} else {
			// deserialize json to make sure it looks valid
			OptimizationSettingList settingsList = EmlogisUtils.fromJsonString(optimizationSettings, OptimizationSettingList.class); 
			this.optimizationSettings = optimizationSettings;
		}
	}    
	
	public String getCopSetting() {
		OptimizationSettingList settingList = getOptimizationSettings();
		for (OptimizationSetting setting : settingList) {
			if (setting instanceof OptimPreferenceOptimizationSetting) {
				return ((OptimPreferenceOptimizationSetting)setting).getValue();
			}	
		}
		return null;
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


	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	@Override
    public SchedulingSettings clone() throws CloneNotSupportedException {
        return (SchedulingSettings) super.clone();
    }
}

