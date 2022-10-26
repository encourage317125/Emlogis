package com.emlogis.engine.domain.contract.contractline;

import com.emlogis.engine.domain.contract.Contract;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MinMaxContractLine extends ContractLine {

    private boolean minimumEnabled;
    private int minimumValue;
    private int minimumWeight;
	private boolean maximumEnabled;
    private int maximumValue;
    private int maximumWeight;

    public static MinMaxContractLine createMaxContractLine(Contract contract, ContractLineType type, boolean isEnabled, int value, int weight) {
		MinMaxContractLine contractLine = new MinMaxContractLine();
		contractLine.setContract(contract);
		contractLine.setContractLineType(type);
		contractLine.setMaximumEnabled(isEnabled);
		contractLine.setMaximumValue(value);
		contractLine.setMaximumWeight(weight);
		return contractLine;
	}
    
    public boolean isMinimumEnabled() {
        return minimumEnabled;
    }

    public void setMinimumEnabled(boolean minimumEnabled) {
        this.minimumEnabled = minimumEnabled;
    }

    public int getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(int minimumValue) {
        this.minimumValue = minimumValue;
    }

    public int getMinimumWeight() {
        return minimumWeight;
    }

    public void setMinimumWeight(int minimumWeight) {
        this.minimumWeight = minimumWeight;
    }

    public boolean isMaximumEnabled() {
        return maximumEnabled;
    }

    public void setMaximumEnabled(boolean maximumEnabled) {
        this.maximumEnabled = maximumEnabled;
    }

    public int getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(int maximumValue) {
        this.maximumValue = maximumValue;
    }

    public int getMaximumWeight() {
        return maximumWeight;
    }

    public void setMaximumWeight(int maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

    @JsonIgnore
    public boolean isEnabled() {
        return minimumEnabled || maximumEnabled;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (maximumEnabled ? 1231 : 1237);
		result = prime * result + maximumValue;
		result = prime * result + maximumWeight;
		result = prime * result + (minimumEnabled ? 1231 : 1237);
		result = prime * result + minimumValue;
		result = prime * result + minimumWeight;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinMaxContractLine other = (MinMaxContractLine) obj;
		if (maximumEnabled != other.maximumEnabled)
			return false;
		if (maximumValue != other.maximumValue)
			return false;
		if (maximumWeight != other.maximumWeight)
			return false;
		if (minimumEnabled != other.minimumEnabled)
			return false;
		if (minimumValue != other.minimumValue)
			return false;
		if (minimumWeight != other.minimumWeight)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MinMaxContractLine [minimumEnabled=");
		builder.append(minimumEnabled);
		builder.append(", minimumValue=");
		builder.append(minimumValue);
		builder.append(", minimumWeight=");
		builder.append(minimumWeight);
		builder.append(", maximumEnabled=");
		builder.append(maximumEnabled);
		builder.append(", maximumValue=");
		builder.append(maximumValue);
		builder.append(", maximumWeight=");
		builder.append(maximumWeight);
		builder.append(", getContract()=");
		builder.append(getContract());
		builder.append(", getContractLineType()=");
		builder.append(getContractLineType());
		builder.append("]");
		return builder.toString();
	}

}
