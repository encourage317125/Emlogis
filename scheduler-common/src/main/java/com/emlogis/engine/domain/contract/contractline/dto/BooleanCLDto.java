package com.emlogis.engine.domain.contract.contractline.dto;

public class BooleanCLDto extends ContractLineDto {
	
    private boolean enabled;
    private int weight = -1;        //default = -1

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
