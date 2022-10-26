package com.emlogis.common;

import org.joda.time.LocalTime;

import java.util.Comparator;

/**
 * Created by Andrii Mozharovskyi on 9/11/15.
 */
public class SimpleTimeFrame implements Cloneable {
    private Long startTime;  // milliseconds offset into the day
    private Long endTime;    // milliseconds offset into the day

    public SimpleTimeFrame() {
    }

    public SimpleTimeFrame(Long startTime, Long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Comparator<SimpleTimeFrame> getNullableStartTimeComparator() {
        return new Comparator<SimpleTimeFrame>() {
            @Override
            public int compare(SimpleTimeFrame o1, SimpleTimeFrame o2) {
                if(o1.getStartTime() == null && o2.getStartTime() != null) {
                    return -1;
                } else if(o1.getStartTime() != null && o2.getStartTime() == null) {
                    return 1;
                } else if(o1.getStartTime() != null && o2.getStartTime() != null) {
                    if(o1.getStartTime() < o2.getStartTime()) {
                        return -1;
                    } else if(o1.getStartTime() > o2.getStartTime()){
                        return 1;
                    }
                }
                return 0;
            }
        };
    }

    public Long getStartTime() {return startTime;}
    public void setStartTime(Long startTime) {this.startTime = startTime;}
    public Long getEndTime() {return endTime;}
    public void setEndTime(Long endTime) {this.endTime = endTime;}

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static <T extends SimpleTimeFrame> T copy(T obj) throws CloneNotSupportedException {
        return (T) obj.clone();
    }

    @Override
    public String toString() {
        LocalTime startLocalTime = new LocalTime(startTime);
        LocalTime endLocalTime = new LocalTime(endTime);
//        return "[startTime=" + startTime + "/" + startLocalTime + ", endTime="
//                + endTime + "/" + endLocalTime + "]";

        return "[" + startTime + " - "
                + endTime + "]";
    }
}
