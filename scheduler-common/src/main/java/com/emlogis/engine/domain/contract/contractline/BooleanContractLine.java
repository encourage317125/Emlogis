package com.emlogis.engine.domain.contract.contractline;


public class BooleanContractLine extends ContractLine {

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
