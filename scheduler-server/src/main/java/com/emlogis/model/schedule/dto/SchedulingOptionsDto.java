package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.dto.BaseEntityDto;
import com.emlogis.model.schedule.OverrideOption;
import com.emlogis.model.tenant.settings.scheduling.OptimPreferenceOptimizationSetting;

import java.util.Map;

public class SchedulingOptionsDto extends BaseEntityDto {
	
    private String optimizationPreferenceSetting;
    private boolean	overrideOptimizationPreference;

    private Map<ConstraintOverrideType, OverrideOption> overrideOptions;
    
    public String getOptimizationPreferenceSetting() {
		return optimizationPreferenceSetting;
	}

	public void setOptimizationPreferenceSetting(
			String optimizationPreferenceSetting) {
		this.optimizationPreferenceSetting = optimizationPreferenceSetting;
	}

	public boolean isOverrideOptimizationPreference() {
		return overrideOptimizationPreference;
	}

	public void setOverrideOptimizationPreference(
			boolean overrideOptimizationPreference) {
		this.overrideOptimizationPreference = overrideOptimizationPreference;
	}

	public Map<ConstraintOverrideType, OverrideOption> getOverrideOptions() {
        return overrideOptions;
    }

    public void setOverrideOptions(Map<ConstraintOverrideType, OverrideOption> overrideOptions) {
        this.overrideOptions = overrideOptions;
    }
}
