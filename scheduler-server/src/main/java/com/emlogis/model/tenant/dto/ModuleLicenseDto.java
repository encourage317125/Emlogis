package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.ReadDto;
import com.emlogis.model.tenant.ModuleStatus;

public class ModuleLicenseDto  extends ReadDto {

	private	String			moduleName;
	private	ModuleStatus	moduleStatus;
    private long 			moduleExpirationDate;    // expiration date days. 0 = unspecified
	
	public ModuleLicenseDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ModuleLicenseDto(String moduleName, ModuleStatus moduleStatus,
			long moduleExpirationDate) {
		super();
		this.moduleName = moduleName;
		this.moduleStatus = moduleStatus;
		this.moduleExpirationDate = moduleExpirationDate;
	}

	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public ModuleStatus getModuleStatus() {
		return moduleStatus;
	}
	public void setModuleStatus(ModuleStatus moduleStatus) {
		this.moduleStatus = moduleStatus;
	}
	public long getModuleExpirationDate() {
		return moduleExpirationDate;
	}
	public void setModuleExpirationDate(long moduleExpirationDate) {
		this.moduleExpirationDate = moduleExpirationDate;
	}
	
	public String getClName() {
		return this.getClass().getSimpleName();
	}
    
}
