package com.emlogis.model.employee.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class AvailcalViewDto implements Serializable {

    private String employeeId;
    private long daterangestart;
    private long daterangeend;
    private Long startDate;
    private Long endDate;
    private boolean coupleWeekends;
    private WeekdayRotations weekdayRotations = new WeekdayRotations();
    private Collection<AvailCITimeFrame> availCITimeFrames = new ArrayList<>();
    private Collection<AvailCDTimeFrame> availCDTimeFrames = new ArrayList<>();
    private Collection<PrefCITimeFrame> prefCITimeFrames = new ArrayList<>();
    private Collection<PrefCDTimeFrame> prefCDTimeFrames = new ArrayList<>();

    public enum PreviewType implements Serializable {
		REMOVED,
		ADDED
	}
	
	public enum AvailType implements Serializable {
		AVAIL,
		DAY_OFF
	}
	
	public enum PrefType implements Serializable {
		AVOID_DAY,
		PREFER_DAY,
		AVOID_TIMEFRAME,
		PREFER_TIMEFRAME
	}
	
	public static class TimeFrameInstance implements Serializable {
		private Long startDateTime;
		private Long endDateTime;
		private PreviewType preview = null;
		
		public Long getStartDateTime() {return startDateTime;}
		public void setStartDateTime(Long startDateTime) {this.startDateTime = startDateTime;}
		public Long getEndDateTime() {return endDateTime;}
		public void setEndDateTime(Long endDateTime) {this.endDateTime = endDateTime;}
		public PreviewType getPreview() {return preview;}
		public void setPreview(PreviewType preview) {this.preview = preview;}

		public String toString(DateTimeZone timeZone) {
			if (timeZone == null){timeZone = DateTimeZone.UTC;}
			return "\n\t\t\t\tTimeFrameInstance ["
					+ "getStartDateTime() = " + getStartDateTime() + " (" + new DateTime(getStartDateTime(), timeZone) + "), "
					+ "getEndDateTime() = " + getEndDateTime() + " (" + new DateTime(getEndDateTime(), timeZone) + "), "
					+ "getPreview() = " + getPreview() + "]";
		}
	}

	public static abstract class CDTimeFrame implements Serializable {
		private Long startDateTime;
		private Long endDateTime;
		
		public Long getStartDateTime() {return startDateTime;}
		public void setStartDateTime(Long startDateTime) {this.startDateTime = startDateTime;}
		public Long getEndDateTime() {return endDateTime;}
		public void setEndDateTime(Long endDateTime) {this.endDateTime = endDateTime;}
	}

	public static abstract class CITimeFrame implements Serializable {
		private Long effectiveDateRangeStart;
		private Long effectiveDateRangeEnd;
		private Long startTime;
		private Long endTime;
		private DayOfWeek dayOfTheWeek;
		private Collection<TimeFrameInstance> timeFrameInstances = new ArrayList<TimeFrameInstance>();
		
		public Long getEffectiveDateRangeStart() {return effectiveDateRangeStart;}
		public void setEffectiveDateRangeStart(Long effectiveDateRangeStart) {this.effectiveDateRangeStart = effectiveDateRangeStart;}
		public Long getEffectiveDateRangeEnd() {return effectiveDateRangeEnd;}
		public void setEffectiveDateRangeEnd(Long effectiveDateRangeEnd) {this.effectiveDateRangeEnd = effectiveDateRangeEnd;}
		public Long getStartTime() {return startTime;}
		public void setStartTime(Long startTime) {this.startTime = startTime;}
		public Long getEndTime() {return endTime;}
		public void setEndTime(Long endTime) {this.endTime = endTime;}
		public Collection<TimeFrameInstance> getTimeFrameInstances() {return timeFrameInstances;}
		public void setTimeFrameInstances(Collection<TimeFrameInstance> timeFrameInstances) {this.timeFrameInstances = timeFrameInstances;}
		public DayOfWeek getDayOfTheWeek() {return dayOfTheWeek;}
		public void setDayOfTheWeek(DayOfWeek dayOfTheWeek) {this.dayOfTheWeek = dayOfTheWeek;}
	}
	
	public static class AvailCDTimeFrame extends CDTimeFrame implements Serializable {
		private AvailType availType;
		private boolean isPTO;
		private String reason;
		private String absenceTypeName;
		private PreviewType preview = null;
		
		public AvailType getAvailType() {return availType;}
		public void setAvailType(AvailType availType) {this.availType = availType;}
		public boolean isPTO() {return isPTO;}
		public void setPTO(boolean isPTO) {this.isPTO = isPTO;}
		public String getReason() {return reason;}
		public void setReason(String reason) {this.reason = reason;}
		public String getAbsenceTypeName() {return absenceTypeName;}
		public void setAbsenceTypeName(String absenceTypeName) {this.absenceTypeName = absenceTypeName;}
		public PreviewType getPreview() {return preview;}
		public void setPreview(PreviewType preview) {this.preview = preview;}

		public String toString(DateTimeZone timeZone) {
			if (timeZone == null){timeZone = DateTimeZone.UTC;}
			return "\n\t\tAvailCDTimeFrame [getAvailType() = " + getAvailType()
					+ ", isPTO() = " + isPTO() + ", getReason() = "
					+ getReason() + ", getAbsenceTypeName() = "
					+ getAbsenceTypeName() 
					+ ",\n\t\t\t"
					+ "getStartDateTime() = " + getStartDateTime() + " (" + new DateTime(getStartDateTime(), timeZone) + "), "
					+ "getEndDateTime() = " + getEndDateTime() + " (" + new DateTime(getEndDateTime(), timeZone) + "), "
					+ "getPreview() = " + getPreview() 
					+ "]";
		}
	}

	public static class AvailCITimeFrame extends CITimeFrame implements Serializable {
		private AvailType availType;
		private PreviewType preview = null;

		public AvailType getAvailType() {return availType;}
		public void setAvailType(AvailType availType) {this.availType = availType;}
		public PreviewType getPreview() {return preview;}
		public void setPreview(PreviewType preview) {this.preview = preview;}

		public String toString(DateTimeZone timeZone) {
			if (timeZone == null){timeZone = DateTimeZone.UTC;}
			StringBuilder ret = new StringBuilder();
			ret.append("\n\t\tAvailCITimeFrame ["
					+ "getEffectiveDateRangeStart() = " + getEffectiveDateRangeStart() + " (" + new DateTime(getEffectiveDateRangeStart(), timeZone) + "), "
					+ "getEffectiveDateRangeEnd() = " + getEffectiveDateRangeEnd() + " (" + new DateTime(getEffectiveDateRangeEnd(), timeZone) + "),\n\t\t\t"
					+ "getAvailType() = " + getAvailType() + ", "
					+ "getDayOfTheWeek() = " + getDayOfTheWeek());
					
			if (getStartTime() == null){
				ret.append(", getStartTime() = null");
			} else {
				ret.append(", getStartTime() = " + getStartTime() + " (" + DurationFormatUtils.formatDuration(getStartTime(), "HH:mm" + ")"));
			}

			if (getEndTime() == null){
				ret.append(", getEndTime() = null"); 
			} else {
				ret.append(", getEndTime() = " + getEndTime() + " (" + DurationFormatUtils.formatDuration(getEndTime(), "HH:mm" + ")"));
			}
			
			ret.append(", getPreview() = " + getPreview());

			ret.append(", getTimeFrameInstances() = ");
			if (!getTimeFrameInstances().isEmpty()) {
				for (TimeFrameInstance timeFrameInstance : getTimeFrameInstances()) {
					ret.append(timeFrameInstance.toString(timeZone));
				}
			} else {ret.append("[ ]");}
			
			ret.append("]");
			return ret.toString();
		}
		
	}
	
	public static class PrefCDTimeFrame extends CDTimeFrame implements Serializable {
		private PrefType prefType;

		public PrefType getPrefType() {return prefType;}
		public void setPrefType(PrefType prefType) {this.prefType = prefType;}

		public String toString(DateTimeZone timeZone) {
			if (timeZone == null){timeZone = DateTimeZone.UTC;}
			return "\n\t\tPrefCDTimeFrame [\ngetPrefType() = " + getPrefType() + ", "
					+ "getStartDateTime() = " + getStartDateTime() + " (" + new DateTime(getStartDateTime(), timeZone) + "), "
					+ "getEndDateTime() = " + getEndDateTime() + " (" + new DateTime(getEndDateTime(), timeZone) + ")"
					+ "]";
		}
	}
	
	public static class PrefCITimeFrame extends CITimeFrame implements Serializable {
		private PrefType prefType;

		public PrefType getPrefType() {return prefType;}
		public void setPrefType(PrefType prefType) {this.prefType = prefType;}

		public String toString(DateTimeZone timeZone) {
			if (timeZone == null){timeZone = DateTimeZone.UTC;}
			StringBuilder ret = new StringBuilder();
			ret.append("\n\t\tPrefCITimeFrame ["
					+ "getEffectiveDateRangeStart() = " + getEffectiveDateRangeStart() + " (" + new DateTime(getEffectiveDateRangeStart(), timeZone) + "), "
					+ "getEffectiveDateRangeEnd() = " + getEffectiveDateRangeEnd() + " (" + new DateTime(getEffectiveDateRangeEnd(), timeZone) + "),\n\t\t\t"
					+ "getPrefType() = " + getPrefType()
					+ ", getDayOfTheWeek() = " + getDayOfTheWeek());

			if (getStartTime() == null){
				ret.append(", getStartTime() = null");
			} else {
				ret.append(", getStartTime() = " + DurationFormatUtils.formatDuration(getStartTime(), "HH:mm"));
			}

			if (getEndTime() == null){
				ret.append(", getEndTime() = null"); 
			} else {
				ret.append(", getEndTime() = " + DurationFormatUtils.formatDuration(getEndTime(), "HH:mm"));
			}
			
			ret.append(", getTimeFrameInstances() = ");
			if (!getTimeFrameInstances().isEmpty()) {
				for (TimeFrameInstance timeFrameInstance : getTimeFrameInstances()) {
					ret.append(timeFrameInstance.toString(timeZone));
				}
			} else {ret.append("[ ]");}

			ret.append("\n\n]");
			ret.append("]");
			return ret.toString();
		}
	}

	public static class WeekdayRotations implements Serializable {
		private WeekdayRotationValue sunday     = WeekdayRotationValue.NONE;
		private WeekdayRotationValue monday     = WeekdayRotationValue.NONE;
		private WeekdayRotationValue tuesday    = WeekdayRotationValue.NONE;
		private WeekdayRotationValue wednesday  = WeekdayRotationValue.NONE;
		private WeekdayRotationValue thursday   = WeekdayRotationValue.NONE;
		private WeekdayRotationValue friday     = WeekdayRotationValue.NONE;
		private WeekdayRotationValue saturday   = WeekdayRotationValue.NONE;

		public WeekdayRotationValue getSunday() {return sunday;}
		public void setSunday(WeekdayRotationValue sunday) {this.sunday = sunday;}
		public WeekdayRotationValue getMonday() {return monday;}
		public void setMonday(WeekdayRotationValue monday) {this.monday = monday;}
		public WeekdayRotationValue getTuesday() {return tuesday;}
		public void setTuesday(WeekdayRotationValue tuesday) {this.tuesday = tuesday;}
		public WeekdayRotationValue getWednesday() {return wednesday;}
		public void setWednesday(WeekdayRotationValue wednesday) {this.wednesday = wednesday;}
		public WeekdayRotationValue getThursday() {return thursday;}
		public void setThursday(WeekdayRotationValue thursday) {this.thursday = thursday;}
		public WeekdayRotationValue getFriday() {return friday;}
		public void setFriday(WeekdayRotationValue friday) {this.friday = friday;}
		public WeekdayRotationValue getSaturday() {return saturday;}
		public void setSaturday(WeekdayRotationValue saturday) {this.saturday = saturday;}

		@Override
		public String toString() {
			return "WeekdayRotations [\n\t\t"
					+ "getSunday() = " + getSunday()+ ", "
					+ "getMonday() = " + getMonday() + ", "
					+ "getTuesday() = " + getTuesday() + ", "
					+ "getWednesday() = " + getWednesday() + ", "
					+ "getThursday() = " + getThursday() + ", "
					+ "getFriday() = " + getFriday() + ", "
					+ "getSaturday() = " + getSaturday() + "]";
		}
    }
	
	public enum WeekdayRotationValue {
		NONE,
		EVERY_OTHER,
		EVERY_THIRD,
		TWO_OF_EVERY_FOUR
	}

	public AvailcalViewDto(String employeeId, long daterangestart, long daterangeend) {
		super();
		this.employeeId = employeeId;
		this.daterangestart = daterangestart;
		this.daterangeend = daterangeend;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public long getDaterangestart() {
		return daterangestart;
	}

	public void setDaterangestart(long daterangestart) {
		this.daterangestart = daterangestart;
	}

	public long getDaterangeend() {
		return daterangeend;
	}

	public void setDaterangeend(long daterangeend) {
		this.daterangeend = daterangeend;
	}

	public boolean isCoupleWeekends() {
		return coupleWeekends;
	}

	public void setCoupleWeekends(boolean coupleWeekends) {
		this.coupleWeekends = coupleWeekends;
	}

	public WeekdayRotations getWeekdayRotations() {
		return weekdayRotations;
	}

	public void setWeekdayRotations(WeekdayRotations weekdayRotations) {
		this.weekdayRotations = weekdayRotations;
	}

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Collection<AvailCITimeFrame> getAvailCITimeFrames() {
		return availCITimeFrames;
	}

	public void setAvailCITimeFrames(Collection<AvailCITimeFrame> availCITimeFrames) {
		this.availCITimeFrames = availCITimeFrames;
	}

	public Collection<AvailCDTimeFrame> getAvailCDTimeFrames() {
		return availCDTimeFrames;
	}

    public void setAvailCDTimeFrames(Collection<AvailCDTimeFrame> availCDTimeFrames) {
		this.availCDTimeFrames = availCDTimeFrames;
	}

    public Collection<PrefCITimeFrame> getPrefCITimeFrames() {
		return prefCITimeFrames;
	}

    public void setPrefCITimeFrames(Collection<PrefCITimeFrame> prefCITimeFrames) {
		this.prefCITimeFrames = prefCITimeFrames;
	}

    public Collection<PrefCDTimeFrame> getPrefCDTimeFrames() {
		return prefCDTimeFrames;
	}

    public void setPrefCDTimeFrames(Collection<PrefCDTimeFrame> prefCDTimeFrames) {
		this.prefCDTimeFrames = prefCDTimeFrames;
	}

	public String toString(DateTimeZone timeZone) {
		if (timeZone == null){timeZone = DateTimeZone.UTC;}
		StringBuilder ret = new StringBuilder();
		ret.append("\n\nAvailcalViewDto ["
				+ "\n\tgetEmployeeId() = " + getEmployeeId()
				+ ", \n\tgetDaterangestart() = " + getDaterangestart() + " (" + new DateTime(getDaterangestart(), timeZone) + ")"
				+ ", \n\tgetDaterangeend() = " + getDaterangeend() + " (" + new DateTime(getDaterangeend(), timeZone) + ")"
				+ ", \n\tisCoupleWeekends() = " + isCoupleWeekends()
				+ ", \n\tgetWeekdayRotations() = " + getWeekdayRotations());
				
		ret.append(", \n\tgetAvailCITimeFrames() = ");
		if (!availCITimeFrames.isEmpty()) {
			for (AvailCITimeFrame availCITimeFrame : availCITimeFrames) {
				ret.append(availCITimeFrame.toString(timeZone));
			}
		} else {ret.append("[ ]");}

		ret.append(", \n\tgetAvailCDTimeFrames() = ");
		if (!availCDTimeFrames.isEmpty()) {
			for (AvailCDTimeFrame availCDTimeFrame : availCDTimeFrames) {
				ret.append(availCDTimeFrame.toString(timeZone));
			}
		} else {ret.append("[ ]");}
		
		ret.append(", \n\tgetPrefCITimeFrames() = ");
		if (!prefCITimeFrames.isEmpty()) {
			for (PrefCITimeFrame prefCITimeFrame : prefCITimeFrames) {
				ret.append(prefCITimeFrame.toString(timeZone));
			}
		} else {ret.append("[ ]");}
		
		ret.append(", \n\tgetPrefCDTimeFrames() = ");
		if (!prefCDTimeFrames.isEmpty()) {
			for (PrefCDTimeFrame prefCDTimeFrame : prefCDTimeFrames) {
				ret.append(prefCDTimeFrame.toString(timeZone));
			}
		} else {ret.append("[ ]");}
		ret.append("\n]\n\n");

		return ret.toString();
	}
}
