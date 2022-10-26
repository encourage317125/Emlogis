package com.emlogis.engine.domain.contract.contractline;

import com.emlogis.engine.domain.contract.Contract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "type")  
@JsonSubTypes({  
    @Type(value = MinMaxContractLine.class, name = "minMaxContractLine"), 
    @Type(value = PatternContractLine.class, name = "patternContractLine"),  
    @Type(value = BooleanContractLine.class, name = "boolContractLine")})
public abstract class ContractLine {

    private Contract contract;
    private ContractLineType contractLineType;

    @JsonIgnore
    public String getEmployeeId(){
    	if(contract == null && contract.getScope() == ContractScope.EmployeeContract) {
    		return contract.getContractRefId();
    	}
    	return null;
    }
    
    @JsonIgnore
    public boolean isTeamContract(){
    	return contract.getScope() == ContractScope.TeamContract;
    }
    
    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ContractLineType getContractLineType() {
        return contractLineType;
    } 

    public void setContractLineType(ContractLineType contractLineType) {
        this.contractLineType = contractLineType;
    }

    public abstract boolean isEnabled();

    @Override
    public String toString() {
        return contract + "-" + contractLineType;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contract == null) ? 0 : contract.hashCode());
		result = prime
				* result
				+ ((contractLineType == null) ? 0 : contractLineType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContractLine other = (ContractLine) obj;
		if (contract == null) {
			if (other.contract != null)
				return false;
		} else if (!contract.equals(other.contract))
			return false;
		if (contractLineType != other.contractLineType)
			return false;
		return true;
	}

	
}
