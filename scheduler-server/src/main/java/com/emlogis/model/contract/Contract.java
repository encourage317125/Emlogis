package com.emlogis.model.contract;

import com.emlogis.common.Constants;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public abstract class Contract extends BaseEntity implements Serializable {
	
	// TODO Need to create nullable OneToOne relationships to Contract
	// for Organization, StructureLevel (Site but not Team?), and Employee.
	
	@OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<ContractLine> contractLines = new HashSet<>();
	
	@ElementCollection
	@MapKeyEnumerated
	@CollectionTable(name = "Contract_Line_Type_Map")
	@MapKeyColumn(name ="Contract_Line_Type")
	@Column(name = "Total")
	private Map<ContractLineType, Integer> clTypeMap = new HashMap<>();
	
	@Column(nullable = false)
	private String name = Constants.DEFAULT_CONTRACT_NAME;
	
	@Column(nullable = false)
	private boolean defaultContract = false;
	
    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected Contract() {
		super();
	}    
    
    /**
     * Required fields constructor.
     * @param primaryKey
     */
    public Contract(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	/**
	 * @return the contractLines
	 */
	public Set<ContractLine> getContractLines() {
		return contractLines;
	}

	/**
	 * @param contractLines the contractLines to set
	 */
	public void setContractLines(Set<ContractLine> contractLines) {
		this.contractLines = contractLines;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDefaultContract() {
		return defaultContract;
	}

	public void setDefaultContract(boolean isDefaultContract) {
		this.defaultContract = isDefaultContract;
	}			
	
	public void addContractLine(ContractLine contractLine) {
		contractLines.add(contractLine);
	}
	
	public void removeContractLine(ContractLine contractLine) {
		contractLines.remove(contractLine);		
	}
	
	public Integer getContractLineTypeCount(ContractLineType contractLineType) {
		
		Integer currentTypeCount = clTypeMap.get(contractLineType);
		
		if(currentTypeCount == null) {
			currentTypeCount =  new Integer(0);
			clTypeMap.put(contractLineType, currentTypeCount);
		}
		
		return currentTypeCount;
	}
	
	public void updateContractLineTypeCount(ContractLineType contractLineType, Integer newCount) {
		clTypeMap.put(contractLineType, newCount);
	}
}

