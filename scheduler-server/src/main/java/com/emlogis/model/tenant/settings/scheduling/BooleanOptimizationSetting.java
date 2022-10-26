package com.emlogis.model.tenant.settings.scheduling;

public class BooleanOptimizationSetting extends OptimizationSetting {

	private boolean value = false;
	
	public BooleanOptimizationSetting() {
		super();
	}

	public BooleanOptimizationSetting(OptimizationSettingName name) {
		super(name);
	}

	public BooleanOptimizationSetting(OptimizationSettingName name, boolean value) {
		super(name);
		this.value = value;
	}

	public boolean isValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}
	
}
