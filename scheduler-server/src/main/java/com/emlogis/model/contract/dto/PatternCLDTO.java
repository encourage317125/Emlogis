package com.emlogis.model.contract.dto;

import java.io.Serializable;

public class PatternCLDTO extends ContractLineDTO implements Serializable {
	
	private int weight;

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}	
}
