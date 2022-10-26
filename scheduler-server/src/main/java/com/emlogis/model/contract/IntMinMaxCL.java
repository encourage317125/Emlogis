package com.emlogis.model.contract;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class IntMinMaxCL extends ContractLine implements Cloneable {
	
	@Column
	private boolean minimumEnabled;
	
	@Column
	private int minimumValue;
	
	@Column
	private int minimumWeight;

	@Column
	private boolean maximumEnabled;
	
	@Column
	private int maximumValue;
	
	@Column
	private int maximumWeight;
	
    /**
     * Default no-arg constructor
     * Protected to satisfy JPA but otherwise discourage no-arg construction by developers
     */
    protected IntMinMaxCL() {}

	public IntMinMaxCL(PrimaryKey primaryKey, String name, String category,
			ContractLineType contractLineType, Contract contract,
			boolean minimumEnabled, int minimumValue, int minimumWeight,
			boolean maximumEnabled, int maximumValue, int maximumWeight) {
		super(primaryKey, name, category, contractLineType, contract);
		this.minimumEnabled = minimumEnabled;
		this.minimumValue = minimumValue;
		this.minimumWeight = minimumWeight;
		this.maximumEnabled = maximumEnabled;
		this.maximumValue = maximumValue;
		this.maximumWeight = maximumWeight;
	}

    /**
     * Required fields constructor.
     * 
     * @param primaryKey
     */
    public IntMinMaxCL(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	/**
	 * @return the minimumEnabled
	 */
	public boolean getMinimumEnabled() {
		return minimumEnabled;
	}

	/**
	 * @param minimumEnabled the minimumEnabled to set
	 */
	public void setMinimumEnabled(boolean minimumEnabled) {
		this.minimumEnabled = minimumEnabled;
	}

	/**
	 * @return the minimumValue
	 */
	public int getMinimumValue() {
		return minimumValue;
	}

	/**
	 * @param minimumValue the minimumValue to set
	 */
	public void setMinimumValue(int minimumValue) {
		this.minimumValue = minimumValue;
	}

	/**
	 * @return the minimumWeight
	 */
	public int getMinimumWeight() {
		return minimumWeight;
	}

	/**
	 * @param minimumWeight the minimumWeight to set
	 */
	public void setMinimumWeight(int minimumWeight) {
		this.minimumWeight = minimumWeight;
	}

	/**
	 * @return the maximumEnabled
	 */
	public boolean getMaximumEnabled() {
		return maximumEnabled;
	}

	/**
	 * @param maximumEnabled the maximumEnabled to set
	 */
	public void setMaximumEnabled(boolean maximumEnabled) {
		this.maximumEnabled = maximumEnabled;
	}

	/**
	 * @return the maximumValue
	 */
	public int getMaximumValue() {
		return maximumValue;
	}

	/**
	 * @param maximumValue the maximumValue to set
	 */
	public void setMaximumValue(int maximumValue) {
		this.maximumValue = maximumValue;
	}

	/**
	 * @return the maximumWeight
	 */
	public int getMaximumWeight() {
		return maximumWeight;
	}

	/**
	 * @param maximumWeight the maximumWeight to set
	 */
	public void setMaximumWeight(int maximumWeight) {
		this.maximumWeight = maximumWeight;
	}

    @Override
    public IntMinMaxCL clone() throws CloneNotSupportedException {
        return (IntMinMaxCL) super.clone();
    }
}

