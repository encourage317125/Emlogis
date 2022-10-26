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
public class BooleanCL extends ContractLine implements Serializable {
	
	@Column
	private boolean enabled;
	
	@Column
	private int weight;
	
    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected BooleanCL() {
		super();
	}
    
    /**
     * Required fields constructor.
     * 
     * @param primaryKey
     */
    public BooleanCL(PrimaryKey primaryKey) {
		super(primaryKey);
	}



	public BooleanCL(PrimaryKey primaryKey, String name, String category,
			ContractLineType contractLineType, Contract contract, boolean enabled, int weight) {
		super(primaryKey, name, category, contractLineType, contract);
		this.enabled = enabled;
		this.weight = weight;
	}

	/**
	 * @return the enabled
	 */
	public boolean getEnabled() {
		return enabled;
	}



	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}



	/**
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}

