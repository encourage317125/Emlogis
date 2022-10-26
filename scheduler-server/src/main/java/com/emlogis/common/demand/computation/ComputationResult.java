package com.emlogis.common.demand.computation;

import java.util.ArrayList;
import java.util.List;

public class ComputationResult implements Cloneable {

    private List<Interval> intervals = new ArrayList<>();

    public List<Interval> getIntervals() {
        return intervals;
    }

    public void addInterval(Interval interval) {
        intervals.add(interval);
    }

    public void removeInterval(Interval interval) {
        intervals.remove(interval);
    }

    public int holeCount() {
        int result = 0;
        for (Interval interval : intervals) {
            result += interval.holeCount();
        }
        return result;
    }

    public int intervalCount() {
        return intervals.size();
    }

    @Override
    protected ComputationResult clone() {
        ComputationResult result = new ComputationResult();
        result.intervals.addAll(intervals);
        return result;
    }

}
