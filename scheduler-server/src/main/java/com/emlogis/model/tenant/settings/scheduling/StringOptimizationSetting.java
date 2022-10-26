package com.emlogis.model.tenant.settings.scheduling;

public class StringOptimizationSetting extends OptimizationSetting {

	private String value = "";
	
	public StringOptimizationSetting() {
		super();
	}

	public StringOptimizationSetting(OptimizationSettingName name) {
		super(name);
	}

	public StringOptimizationSetting(OptimizationSettingName name, String value) {
		super(name);
		setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
