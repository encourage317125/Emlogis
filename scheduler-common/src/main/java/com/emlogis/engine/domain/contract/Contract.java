package com.emlogis.engine.domain.contract;

import com.emlogis.engine.domain.contract.contractline.ContractScope;


public class Contract {

	// References either an employeeID, 
	// TeamId or SiteId
	private String contractRefId;
	private ContractScope scope;

    public Contract(){
    }
    
	public String getContractRefId() {
		return contractRefId;
	}

	public void setContractRefId(String contractRefId) {
		this.contractRefId = contractRefId;
	}

	public ContractScope getScope() {
		return scope;
	}

	public void setScope(ContractScope scope) {
		this.scope = scope;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contractRefId == null) ? 0 : contractRefId.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
		Contract other = (Contract) obj;
		if (contractRefId == null) {
			if (other.contractRefId != null)
				return false;
		} else if (!contractRefId.equals(other.contractRefId))
			return false;
		if (scope != other.scope)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Contract [contractRefId=");
		builder.append(contractRefId);
		builder.append(", scope=");
		builder.append(scope);
		builder.append("]");
		return builder.toString();
	}


}	