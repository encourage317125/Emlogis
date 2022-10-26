package com.emlogis.model.contract;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public abstract class ContractLine extends BaseEntity {
	
	@Column
	private String category;  // TODO What is category?
	
	@Column
	private String name;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ContractLineType contractLineType;
	
	//bi-directional many to one association to contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "contractTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "contractId", referencedColumnName = "id")
    })
	private Contract contract;
	
    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected ContractLine() {
		super();
	}

    /**
     * Required fields constructor.
     * @param primaryKey
     */
    public ContractLine(PrimaryKey primaryKey) {
		super(primaryKey);
	}
    
    /**
     * Required fields constructor.
     * @param primaryKey
     * @param name
     * @param category
     * @param contractLineType
     * @param contract
     */
    public ContractLine(PrimaryKey primaryKey, String name, String category, ContractLineType contractLineType,
                        Contract contract) {
		super(primaryKey);
		this.name = name;
		this.category = category;
		this.contractLineType = contractLineType;
		this.contract = contract;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public ContractLineType getContractLineType() {
		return contractLineType;
	}

	public void setContractLineType(ContractLineType contractLineType) {
		this.contractLineType = contractLineType;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}
	
	public String getContractId() {
		return contract.getId();
	}	
}

