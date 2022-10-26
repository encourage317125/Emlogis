package com.emlogis.model.contract.dto;

import java.io.Serializable;

public class BooleanCLDTO extends ContractLineDTO implements Serializable {
	
	private boolean enabled;
	private int weight;
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
}
