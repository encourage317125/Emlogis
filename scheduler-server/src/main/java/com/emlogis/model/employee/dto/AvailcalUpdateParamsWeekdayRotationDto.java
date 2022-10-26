package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.DateTime;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.employee.dto.AvailcalViewDto.WeekdayRotationValue;

public class AvailcalUpdateParamsWeekdayRotationDto extends AvailcalUpdateParamsDto implements Serializable {

	private DayOfWeek dayOfWeek;
	private WeekdayRotationValue weekdayRotationValue;
	
	public DayOfWeek getDayOfWeek() {return dayOfWeek;}
	public void setDayOfWeek(DayOfWeek dayOfWeek) {this.dayOfWeek = dayOfWeek;}
	public WeekdayRotationValue getWeekdayRotationValue() {return weekdayRotationValue;}
	public void setWeekdayRotationValue(WeekdayRotationValue weekdayRotationValue) {this.weekdayRotationValue = weekdayRotationValue;}
	
}
