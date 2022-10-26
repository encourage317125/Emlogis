package com.emlogis.engine.domain.timeoff;

public class CIPreference extends CITimeWindow{
	private PreferenceType type;

	public PreferenceType getType() {
		return type;
	}

	public void setType(PreferenceType type) {
		this.type = type;
	}
}
