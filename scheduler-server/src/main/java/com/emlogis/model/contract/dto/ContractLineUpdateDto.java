package com.emlogis.model.contract.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;
import com.emlogis.model.dto.UpdateDto;

import java.io.Serializable;

public class ContractLineUpdateDto extends UpdateDto implements Serializable {
	
	public final static String CATEGORY = "category";
	public final static String NAME = "name";
	public final static String CONTRACTLINETYPE = "contractLineType";
	public final static String CONTRACTID = "contractId";
	public final static String MINIMUMENABLED = "minimumEnabled";
	public final static String MINIMUMVALUE = "minimumValue";
	public final static String MINIMUMWEIGHT = "minimumWeight";
	public final static String MAXIMUMENABLED = "maximumEnabled";
	public final static String MAXIMUMVALUE = "maximumValue";
	public final static String MAXIMUMWEIGHT = "maximumWeight";
	
	public final static String ENABLED = "enabled";
	public final static String WEIGHT = "weight";
	
	public final static String DAY_OF_WEEK = "dayOfWeek";
	public final static String NUMBER_OF_DAYS = "numberOfDays";
	public final static String OUT_OF_TOTAL_DAYS = "outOfTotalDays";
	public final static String ROTATION_TYPE = "rotationType";
	
	public final static String DAYS_OFF_AFTER = "daysOffAfter";
	public final static String DAYS_OFF_BEFORE = "daysOffBefore";
	
	private String category;
	private String name;
	private ContractLineType contractLineType;
	private String contractId;
	
	// IntMinMaxCL
	private boolean minimumEnabled;
	private int minimumValue;
	private int minimumWeight;
	private boolean maximumEnabled;
	private int maximumValue;
	private int maximumWeight;

	// BooleanCL
	private boolean enabled;
	private int weight;
	
	// WeekdayRotationPatternCL
	private DayOfWeek dayOfWeek;
	private int numberOfDays;
	private int outOfTotalDays;
	private RotationPatternType rotationType;
	
	// WeekendWorkPatternCL
	private String daysOffAfter;
	private String daysOffBefore;
	
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getContractId() {
		return contractId;
	}

	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	public boolean isMinimumEnabled() {
		return minimumEnabled;
	}

	public void setMinimumEnabled(boolean minimumEnabled) {
		this.minimumEnabled = minimumEnabled;
	}

	public int getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(int minimumValue) {
		this.minimumValue = minimumValue;
	}

	public int getMinimumWeight() {
		return minimumWeight;
	}

	public void setMinimumWeight(int minimumWeight) {
		this.minimumWeight = minimumWeight;
	}

	public boolean isMaximumEnabled() {
		return maximumEnabled;
	}

	public void setMaximumEnabled(boolean maximumEnabled) {
		this.maximumEnabled = maximumEnabled;
	}

	public int getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(int maximumValue) {
		this.maximumValue = maximumValue;
	}

	public int getMaximumWeight() {
		return maximumWeight;
	}

	public void setMaximumWeight(int maximumWeight) {
		this.maximumWeight = maximumWeight;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
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
