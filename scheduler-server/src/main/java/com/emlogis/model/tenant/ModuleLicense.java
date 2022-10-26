package com.emlogis.model.tenant;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ModuleLicense {
	
	private	String			moduleName;

	private	ModuleStatus	moduleStatus;

	@Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime 		moduleExpirationDate = new DateTime();    // expiration date days. 0 = unspecified

	public ModuleLicense() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ModuleLicense(String moduleName, ModuleStatus moduleStatus, long moduleExpirationDate) {
		super();
		this.moduleName = moduleName;
		this.moduleStatus = moduleStatus;
		this.moduleExpirationDate = new DateTime(moduleExpirationDate);
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
		return moduleExpirationDate.getMillis();
	}

	public void setModuleExpirationDate(DateTime moduleExpirationDate) {
		this.moduleExpirationDate = moduleExpirationDate;
	}

	public void setModuleExpirationDate(long moduleExpirationDate) {
		this.moduleExpirationDate = new DateTime(moduleExpirationDate);
	}


}
