package com.emlogis.model.aom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AOMIntegerPropertyDef extends AOMPropertyDef  {

	private	boolean hasMin	= false;
	
	private int minValue;

	private	boolean hasMax	= false;

	private int maxValue;

	private int defaultValue;
	
	public AOMIntegerPropertyDef() {}

	public AOMIntegerPropertyDef(String tenantId) {
		super(tenantId);
		setType(AOMPropertyType.Integer);
	}

	public boolean isHasMin() {
		return hasMin;
	}

	public void setHasMin(boolean hasMin) {
		this.hasMin = hasMin;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public boolean isHasMax() {
		return hasMax;
	}

	public void setHasMax(boolean hasMax) {
		this.hasMax = hasMax;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(int defaultValue) {
		this.defaultValue = defaultValue;
	}

    
}
