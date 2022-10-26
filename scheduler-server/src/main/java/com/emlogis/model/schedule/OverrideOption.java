package com.emlogis.model.schedule;

import java.util.HashSet;
import java.util.Set;

public class OverrideOption {

    private OverrideOptionScope scope = OverrideOptionScope.None;
    private Set<String> employeeIds = new HashSet<>();                // list of employee Ids when scope = Select;

	public OverrideOptionScope getScope() {
		return scope;
	}

    public OverrideOption setScope(OverrideOptionScope scope) {
		this.scope = scope;
		return this;
	}

    public Set<String> getEmployeeIds() {
		return employeeIds;
	}

    public OverrideOption setEmployeeIds(Set<String> employeeIds) {
		this.employeeIds = employeeIds;
		return this;
	}

    public OverrideOption addEmployeeIds(String employeeId) {
        this.employeeIds.add(employeeId);
		return this;
	}

    public OverrideOption removeEmployeeIds(String employeeId) {
        this.employeeIds.remove(employeeId);
		return this;
	}
}
