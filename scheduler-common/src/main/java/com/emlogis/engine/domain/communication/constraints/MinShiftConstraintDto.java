package com.emlogis.engine.domain.communication.constraints;

public class MinShiftConstraintDto extends ShiftConstraintDto {
	
	private int minValue;
	private int actualValue;
	
	/**
	 * @return the minValue
	 */
	public int getMinValue() {
		return minValue;
	}
	
	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(int minValue) {
		this.minValue = minValue;
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
