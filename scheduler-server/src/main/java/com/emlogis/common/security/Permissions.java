package com.emlogis.common.security;

public enum Permissions {
	
	OrganizationProfile_Mgmt("OrganizationProfile_Mgmt"),
	OrganizationProfile_View("OrganizationProfile_View"),
	
	SystemConfiguration_Mgmt("SystemConfiguration_Mgmt"),	

	Impersonate_ViewOnly("Impersonate_ViewOnly"),
    Impersonate_ReadWrite("Impersonate_ReadWrite"),
	
	Employee_View("Employee_View"),
	Employee_Mgmt("Employee_Mgmt"),
	EmployeeProfile_Update("EmployeeProfile_Update"),
	EmployeeWages_Mgmt("EmployeeWages_Mgmt"),
	
	Demand_Mgmt("Demand_Mgmt"),
	Demand_View("Demand_View"),

	Schedule_View("Schedule_View"),
	Schedule_Mgmt("Schedule_Mgmt"),  		// create / delete / edits 
	Schedule_Update("Schedule_Update"), 	// edit shifts only
	Schedule_AdvancedMgmt("Schedule_AdvancedMgmt"),
		
	Availability_Request("Availability_Request"),			// permission to submit Availability and TimeOff requests
	Availability_RequestMgmt("Availability_RequestMgmt"),	// permission to approve/decline Availability and TimeOff requests
	Shift_Request("Shift_Request"),							// permission to submit OS, WIP, SWAP requests
	Shift_RequestMgmt("Shift_RequestMgmt"),					// permission to approve/decline OS, WIP, SWAP requests
	Notification_Recipient("Notification_Recipient"),		// permission to receive request/workflow notifications
	
	Shift_Mgmt("Shift_Mgmt"),								// permission to manage shifts, ie assign/drop/delete/create/edit/ etc
//	ShiftBidding_Request("ShiftBidding.Request"),
//	ShiftBidding_RequestMgmt("ShiftBidding.Mgmt"),

	
	Account_View("Account_View"),
	Account_Mgmt("Account_Mgmt"),
	AccountProfile_Update("AccountProfile_Update"),		// Not really used at this point
	
	Role_View("Role_View"),
	Role_Mgmt("Role_Mgmt"),
	Tenant_View("Tenant_View"),
	Tenant_Mgmt("Tenant_Mgmt"),
	
	Reports_Mgmt("Reports_Mgmt"),
	Reports_View("Reports_View"),
	Reports_Exe("Reports_Exe"),

	Support("Support");

	private String value;
	
	private Permissions(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}

	 
