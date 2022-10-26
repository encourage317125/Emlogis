package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.DateTime;

public class AvailcalUpdateParamsCoupleWeekends implements Serializable {

	private boolean coupleWeekends;

	public boolean isCoupleWeekends() {return coupleWeekends;}
	public void setCoupleWeekends(boolean coupleWeekends) {this.coupleWeekends = coupleWeekends;}
}
