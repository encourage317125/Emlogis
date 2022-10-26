package com.emlogis.rest.auditing;

public enum ApiCallCategory {
	
	Session("Session"),
	Unclassified("Unclassified"),
	AccountManagement("AccountManagement"),
	OrganizationManagement("OrganizationManagement"),
	EmployeeManagement("EmployeeManagement"),
	ShiftBidding("ShiftBidding"), 
	DemandManagement("DemandManagement"), 
	CalendarSync("CalendarSync"), 
	OpenShiftEligibility("OpenShiftEligibility"), 
	ShiftSwapEligibility("ShiftSwapEligibility"), 
	Qualification("Qualification"), 
	Requests("Requests"), 
	

	CustomerManagement("CustomerManagement"), 		// customer management (service provider only)
	SystemManagement("SystemManagement");			// general system configuration  (service provider only)
	
	private String value;
	
	private ApiCallCategory(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
