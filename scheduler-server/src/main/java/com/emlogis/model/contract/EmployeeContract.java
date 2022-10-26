package com.emlogis.model.contract;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;

import javax.persistence.*;

@Entity
public class EmployeeContract extends Contract {
	
    //bi-directional many-to-one association to Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "employeeTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "employeeId", referencedColumnName = "id")
    })
    private Employee employee;

	public EmployeeContract(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	public EmployeeContract() {
		super();
	}
	
	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public String getEmployeId() {
		return employee.getId();
	}
}
