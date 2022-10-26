package com.emlogis.model.contract.dto;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.model.dto.ReadDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class ContractLineDTO extends ReadDto implements Serializable {
	
	private String id;
	private String category;
	private String name;
	private ContractLineType contractLineType;
	private String contractId;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ContractLineType getContractLineType() {
		return contractLineType;
	}
	public void setContractLineType(ContractLineType contractLineType) {
		this.contractLineType = contractLineType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getContractId() {
		return contractId;
	}
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}	
}
