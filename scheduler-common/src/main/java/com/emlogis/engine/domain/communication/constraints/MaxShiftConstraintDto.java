package com.emlogis.engine.domain.communication.constraints;

public class MaxShiftConstraintDto extends ShiftConstraintDto {
	
	private int maxValue;
	private int actualValue;
	
	/**
	 * @return the maxValue
	 */
	public int getMaxValue() {
		return maxValue;
	}
	
	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the actualValue
	 */
	public int getActualValue() {
		return actualValue;
	}

	/**
	 * @param actualValue the actualValue to set
	 */
	public void setActualValue(int actualValue) {
		this.actualValue = actualValue;
	}
	
}
