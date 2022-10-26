package com.emlogis.engine.domain.contract.dto;

import javax.xml.bind.annotation.XmlRootElement;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;

import java.io.Serializable;

@XmlRootElement
public class ConstraintOverrideDto implements Serializable {
	
	private String employeeId;
    private ConstraintOverrideType type;

    public ConstraintOverrideDto() {}

    public ConstraintOverrideDto(String employeeId, ConstraintOverrideType type) {
		super();
		this.employeeId = employeeId;
		this.type = type;
	}
	
	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public ConstraintOverrideType getType() {
		return type;
	}

	public void setType(ConstraintOverrideType type) {
		this.type = type;
	}


}
