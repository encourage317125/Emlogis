package com.emlogis.engine.solver.drools;


public class EmployeeCostTotal {
	private String employeeId;
	private long totalCost = 0; // Cost is expressed in dollars multiplied by 100 to avoid float arithmetic

	
	public EmployeeCostTotal(String employeeId, long totalCost) {
		this.employeeId = employeeId;
		this.totalCost = totalCost;
	}
	
	public String getEmployeeId() {
		return employeeId;
	}
	
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	
	public long getTotalCost() {
		return totalCost;
	}
	
	public void setTotalCost(long totalCost) {
		this.totalCost = totalCost;
	}
	
	public void addCost(long cost){
		totalCost += cost;
	}
	
}
