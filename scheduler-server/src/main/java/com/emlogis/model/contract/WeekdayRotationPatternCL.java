package com.emlogis.model.contract;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WeekdayRotationPatternCL extends PatternCL implements Serializable {
	 
	@Column
	private DayOfWeek dayOfWeek;
	@Column
	private int numberOfDays;
	@Column
	private int outOfTotalDays;
	@Column
	private RotationPatternType rotationType;
		
	public WeekdayRotationPatternCL() {
		super();
		this.setContractLineType(ContractLineType.CUSTOM);
	}
		
	public WeekdayRotationPatternCL(PrimaryKey primaryKey, String name,
			String category, ContractLineType contractLineType,
			Contract contract, int weight, DayOfWeek dayOfWeek, int numberOfDays, int outOfTotalDays,
            RotationPatternType rotationType) {
		super(primaryKey, name, category, contractLineType, contract, weight);

		this.dayOfWeek = dayOfWeek;
		this.numberOfDays = numberOfDays;
		this.outOfTotalDays = outOfTotalDays;
		this.rotationType = rotationType;
	}
	public WeekdayRotationPatternCL(PrimaryKey primaryKey) {
		super(primaryKey);
	}
	
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public int getNumberOfDays() {
		return numberOfDays;
	}
	public void setNumberOfDays(int numberOfDays) {
		this.numberOfDays = numberOfDays;
	}
	public int getOutOfTotalDays() {
		return outOfTotalDays;
	}
	public void setOutOfTotalDays(int outOfTotalDays) {
		this.outOfTotalDays = outOfTotalDays;
	}
	public RotationPatternType getRotationType() {
		return rotationType;
	}
	public void setRotationType(RotationPatternType rotationType) {
		this.rotationType = rotationType;
	}
}
