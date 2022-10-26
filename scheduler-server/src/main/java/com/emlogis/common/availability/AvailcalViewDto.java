package com.emlogis.common.availability;

import com.emlogis.common.SimpleTimeFrame;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.employee.AvailabilityTimeFrame;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class AvailcalViewDto implements Serializable {

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

//		@Override
//		public String toString() {
//			return "\n    TimeFrameInstance [startDateTime=" + new DateTime(startDateTime)
//					+ ", endDateTime=" + new DateTime(endDateTime) + "]";
//		}

    }

    public static abstract class TimeFrame extends SimpleTimeFrame implements Serializable {
        protected String employeeId;

        protected abstract static class GroupKey {

            private String employeeId;
            private AvailabilityTimeFrame.AvailabilityType availabilityType;

            public GroupKey() {
            }

            public GroupKey(String employeeId, AvailabilityTimeFrame.AvailabilityType availabilityType) {
                this.employeeId = employeeId;
                this.availabilityType = availabilityType;
            }

            public String getEmployeeId() {
                return employeeId;
            }

            public void setEmployeeId(String employeeId) {
                this.employeeId = employeeId;
            }

            public AvailabilityTimeFrame.AvailabilityType getAvailabilityType() {
                return availabilityType;
            }

            public void setAvailabilityType(AvailabilityTimeFrame.AvailabilityType availabilityType) {
                this.availabilityType = availabilityType;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                GroupKey that = (GroupKey) o;

                if (availabilityType != that.availabilityType) return false;
                if (employeeId != null ? !employeeId.equals(that.employeeId) : that.employeeId != null) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = employeeId != null ? employeeId.hashCode() : 0;
                result = 31 * result + (availabilityType != null ? availabilityType.hashCode() : 0);
                return result;
            }
        }



        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }
    }

    public static abstract class CDTimeFrame extends TimeFrame implements Serializable {
        private Long dateTime;

        public static class GroupKey extends TimeFrame.GroupKey {
            private DateTime dateTime;

            public GroupKey() {
            }

            public GroupKey(String employeeId, AvailabilityTimeFrame.AvailabilityType availabilityType, DateTime dateTime) {
                super(employeeId, availabilityType);
                this.dateTime = dateTime;
            }

            public DateTime getDateTime() {
                return dateTime;
            }

            public void setDateTime(DateTime dateTime) {
                this.dateTime = dateTime;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                if (!super.equals(o)) return false;

                GroupKey that = (GroupKey) o;

                if (dateTime != null ? !dateTime.equals(that.dateTime) : that.dateTime != null) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = super.hashCode();
                result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
                return result;
            }
        }

        public Long getDateTime() {
            return dateTime;
        }

        public void setDateTime(Long dateTime) {
            this.dateTime = dateTime;
        }
    }

    public static abstract class CITimeFrame extends TimeFrame implements Serializable {
        private Long effectiveDateRangeStart;
        private Long effectiveDateRangeEnd;
        private DayOfWeek dayOfTheWeek;
        private Collection<TimeFrameInstance> timeFrameInstances = new ArrayList<TimeFrameInstance>();

        public static class GroupKey extends TimeFrame.GroupKey {
            private DateTime startDateTime;
            private DateTime endDateTime;
            private DayOfWeek dayOfTheWeek;

            public GroupKey() {
            }

            public GroupKey(String employeeId, AvailabilityTimeFrame.AvailabilityType availabilityType,
                            DateTime startDateTime, DateTime endDateTime, DayOfWeek dayOfTheWeek) {
                super(employeeId, availabilityType);
                this.startDateTime = startDateTime;
                this.endDateTime = endDateTime;
                this.dayOfTheWeek = dayOfTheWeek;
            }

            public DateTime getStartDateTime() {return startDateTime;}
            public void setStartDateTime(DateTime startDateTime) {this.startDateTime = startDateTime;}
            public DateTime getEndDateTime() {return endDateTime;}
            public void setEndDateTime(DateTime endDateTime) {this.endDateTime = endDateTime;}
            public DayOfWeek getDayOfTheWeek() {return dayOfTheWeek;}
            public void setDayOfTheWeek(DayOfWeek dayOfTheWeek) {this.dayOfTheWeek = dayOfTheWeek;}

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                if (!super.equals(o)) return false;

                GroupKey that = (GroupKey) o;

                if (dayOfTheWeek != that.dayOfTheWeek) return false;
                if (endDateTime != null ? !endDateTime.equals(that.endDateTime) : that.endDateTime != null) return false;
                if (startDateTime != null ? !startDateTime.equals(that.startDateTime) : that.startDateTime != null)
                    return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = super.hashCode();
                result = 31 * result + (startDateTime != null ? startDateTime.hashCode() : 0);
                result = 31 * result + (endDateTime != null ? endDateTime.hashCode() : 0);
                result = 31 * result + (dayOfTheWeek != null ? dayOfTheWeek.hashCode() : 0);
                return result;
            }
        }

        public Long getEffectiveDateRangeStart() {return effectiveDateRangeStart;}
        public void setEffectiveDateRangeStart(Long effectiveDateRangeStart) {this.effectiveDateRangeStart = effectiveDateRangeStart;}
        public Long getEffectiveDateRangeEnd() {return effectiveDateRangeEnd;}
        public void setEffectiveDateRangeEnd(Long effectiveDateRangeEnd) {this.effectiveDateRangeEnd = effectiveDateRangeEnd;}
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
        //		@Override
//		public String toString() {
//			return "\n  AvailCDTimeFrame [getAvailType()=" + getAvailType()
//					+ ", getStartDateTime()=" + new DateTime(getStartDateTime())
//					+ ", getEndDateTime()=" + new DateTime(getEndDateTime())
//					+ ", isPTO()=" + isPTO()
//					+ ", getReason()=" + getReason()
//					+ ", getAbsenceTypeName()=" + getAbsenceTypeName() + "]";
//		}

    }

    public static class AvailCITimeFrame extends CITimeFrame implements Serializable {
        private AvailType availType;
        private PreviewType preview = null;

        public AvailType getAvailType() {return availType;}
        public void setAvailType(AvailType availType) {this.availType = availType;}
        public PreviewType getPreview() {return preview;}
        public void setPreview(PreviewType preview) {this.preview = preview;}

    }

    public static class PrefCDTimeFrame extends CDTimeFrame implements Serializable {
        private PrefType prefType;

        public PrefType getPrefType() {return prefType;}
        public void setPrefType(PrefType prefType) {this.prefType = prefType;}

    }

    public static class PrefCITimeFrame extends CITimeFrame implements Serializable {
        private PrefType prefType;

        public PrefType getPrefType() {return prefType;}
        public void setPrefType(PrefType prefType) {this.prefType = prefType;}

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
    }

    // TODO - Can probably delete following since maxDaysperWeek no longer part of Availcal UI or APIs
//	public static class MaxDaysPerWeek implements Serializable {
//		private Integer week1 = null;
//		private Integer week2 = null;
//		private Integer week3 = null;
//		private Integer week4 = null;
//		private Integer week5 = null;
//
//		public Integer getWeek1() {return week1;}
//		public void setWeek1(Integer week1) {this.week1 = week1;}
//		public Integer getWeek2() {return week2;}
//		public void setWeek2(Integer week2) {this.week2 = week2;}
//		public Integer getWeek3() {return week3;}
//		public void setWeek3(Integer week3) {this.week3 = week3;}
//		public Integer getWeek4() {return week4;}
//		public void setWeek4(Integer week4) {this.week4 = week4;}
//		public Integer getWeek5() {return week5;}
//		public void setWeek5(Integer week5) {this.week5 = week5;}
//    }

    public enum WeekdayRotationValue {
        NONE,
        EVERY_OTHER,
        EVERY_THIRD,
        TWO_OF_EVERY_FOUR
    }


    private String employeeId;
    private long daterangestart;
    private long daterangeend;
    private boolean coupleWeekends;
    private WeekdayRotations weekdayRotations = new WeekdayRotations();

    // TODO - Can probably delete following since maxDaysperWeek no longer part of Availcal UI or APIs
//	private MaxDaysPerWeek maxDaysPerWeek = new MaxDaysPerWeek();

    private Collection<AvailCITimeFrame> availCITimeFrames = new ArrayList<>();
    private Collection<AvailCDTimeFrame> availCDTimeFrames = new ArrayList<>();
    private Collection<PrefCITimeFrame> prefCITimeFrames = new ArrayList<>();
    private Collection<PrefCDTimeFrame> prefCDTimeFrames = new ArrayList<>();



    public AvailcalViewDto(String employeeId, long daterangestart,
                           long daterangeend) {
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

    // TODO - Can probably delete following since maxDaysperWeek no longer part of Availcal UI or APIs
//	public MaxDaysPerWeek getMaxDaysPerWeek() {
//		return maxDaysPerWeek;
//	}
//	public void setMaxDaysPerWeek(MaxDaysPerWeek maxDaysPerWeek) {
//		this.maxDaysPerWeek = maxDaysPerWeek;
//	}

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

//	@Override
//	public String toString() {
//		return "AvailcalViewDto [\nemployeeId=" + employeeId
//				+ ",\n daterangestart=" + new DateTime(daterangestart)
//				+ ",\n daterangeend=" + new DateTime(daterangeend)
//				+ ",\n coupleWeekends=" + coupleWeekends
//				+ ",\n weekdayRotations=" + weekdayRotations
//				+ ",\n maxDaysPerWeek=" + maxDaysPerWeek
//				+ ",\n availCITimeFrames=" + availCITimeFrames
//				+ ",\n availCDTimeFrames=" + availCDTimeFrames
//				+ ",\n prefCITimeFrames=" + prefCITimeFrames
//				+ ",\n prefCDTimeFrames=" + prefCDTimeFrames + "\n]";
//	}

}
