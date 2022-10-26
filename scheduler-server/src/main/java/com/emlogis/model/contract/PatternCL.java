package com.emlogis.model.contract;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public abstract class PatternCL extends ContractLine implements Serializable {
	
	@Column
	private int weight;
	
    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected PatternCL() {
		super();
	}

    /**
     * Required fields constructor.
     * 
     * @param primaryKey
     */
    public PatternCL(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	public PatternCL(PrimaryKey primaryKey, String name, String category,
			ContractLineType contractLineType, Contract contract,int weight) {
		super(primaryKey, name, category, contractLineType, contract);
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}

