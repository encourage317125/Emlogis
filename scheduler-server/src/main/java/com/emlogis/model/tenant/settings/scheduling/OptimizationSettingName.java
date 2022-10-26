package com.emlogis.model.tenant.settings.scheduling;

public enum OptimizationSettingName {
	
	CoupleWeekends("CoupleWeekends"),
	MinimizeNbOfOpenShifts("MinimizeNbOfOpenShifts"),	
	DistributeOpenShifts("DistributeOpenShifts"),
	MinimizeNbOfExcessShifts("MinimizeNbOfExcessShifts"),
	DistributeExcessShifts("DistributeExcessShifts"),
	TeamScattering("TeamScattering"),
	ClusterHorizontally("ClusterHorizontally"),
	ClusterVertically("ClusterVertically"),
	DoubleUp("DoubleUp"),
	DistributeWorkedWeekends("DistributeWorkedWeekends"),
	PrimarySkillValue("PrimarySkillValue"),
	ManageOpenShiftPlacement("ManageOpenShiftPlacement"),
	ManageExcessShiftPlacement("ManageExcessShiftPlacement"),
	OptimizationPreference("OptimizationPreference"),
	HomeTeamValue("HomeTeamValue");
	
	private String value;
	
	private OptimizationSettingName(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
