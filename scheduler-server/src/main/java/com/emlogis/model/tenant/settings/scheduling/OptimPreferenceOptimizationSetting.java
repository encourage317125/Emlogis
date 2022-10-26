package com.emlogis.model.tenant.settings.scheduling;

import org.apache.commons.lang3.StringUtils;

public class OptimPreferenceOptimizationSetting extends StringOptimizationSetting {
	
	private static String DEFAULT_SETTING = "COP";

	public OptimPreferenceOptimizationSetting() {
		super();
		setValue(DEFAULT_SETTING);
	}

	public OptimPreferenceOptimizationSetting(OptimizationSettingName name) {
		super(name);
		setValue(DEFAULT_SETTING);
	}

	public OptimPreferenceOptimizationSetting(OptimizationSettingName name, String value) {
		super(name,value);
	}

	public void setValue(String value) {  // Cost Overtime Preference
		String values[] = {"None", "COP", "CPO", "OCP", "OPC", "PCO", "POC"};
		for (String val : values) {
			if (StringUtils.equals(val, value)) {
				super.setValue(value);
				return;
			}
		}
	}
	
}
