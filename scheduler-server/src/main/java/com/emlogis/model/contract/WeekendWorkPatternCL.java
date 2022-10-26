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
public class WeekendWorkPatternCL extends PatternCL implements Serializable {

	@Column
	private String daysOffAfter;
	@Column
	private String daysOffBefore;

	public WeekendWorkPatternCL() {
		super();
		this.setContractLineType(ContractLineType.COMPLETE_WEEKENDS);
	}

	public WeekendWorkPatternCL(PrimaryKey primaryKey, String name,
			String category, ContractLineType contractLineType,
			Contract contract, int weight, String daysOffAfter,
			String daysOffBefore) {
		super(primaryKey, name, category, contractLineType, contract, weight);

		this.daysOffAfter = daysOffAfter;
		this.daysOffBefore = daysOffBefore;
	}

	public WeekendWorkPatternCL(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	public String getDaysOffAfter() {
		return daysOffAfter;
	}

	public void setDaysOffAfter(String daysOffAfter) {
		this.daysOffAfter = daysOffAfter;
	}

	public String getDaysOffBefore() {
		return daysOffBefore;
	}

	public void setDaysOffBefore(String daysOffBefore) {
		this.daysOffBefore = daysOffBefore;
	}

}
