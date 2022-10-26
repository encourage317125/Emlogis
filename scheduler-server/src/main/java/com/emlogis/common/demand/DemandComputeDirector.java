package com.emlogis.common.demand;

import com.emlogis.common.demand.computation.*;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.shiftpattern.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;

import java.util.*;

public class DemandComputeDirector {

    private int granule = 15;

    public Set<ShiftReq> computeDemand(ShiftPattern shiftPattern, Collection<ShiftLength> allowedShiftLengths) {
        Set<ShiftReq> result = new HashSet<>();

        Collection<ShiftDemand> shiftDemands = shiftPattern.getShiftDemands();

        Map<Integer, Collection<Integer>> allowedLengths = getAllowedLengths(allowedShiftLengths);
        BallPool ballPool = createBallPool(shiftDemands);

        Computer computer = new Computer(allowedLengths, ballPool);
        computer.compute();

        ComputationResult computationResult = computer.getResultHolder().getFirstComputationResult();

        if (computationResult != null) {
            for (Interval interval : computationResult.getIntervals()) {
                ShiftReq shiftReq = new ShiftReq(new PrimaryKey(shiftPattern.getTenantId()));
                shiftReq.setEmployeeCount(1);
                shiftReq.setShiftPattern(shiftPattern);

                int lengthInMin = interval.getLength() * granule;
                long startTime = interval.getStartPosition() * granule * 60 * 1000L;

                ShiftLength shiftLength = findShiftLength(allowedShiftLengths, lengthInMin);
                ShiftType shiftType = findShiftType(shiftLength, startTime);

                shiftReq.setShiftType(shiftType);

                result.add(shiftReq);
            }
        }

        return result;
    }

    public Set<ShiftReq> computeDemand(Collection<ShiftDemand> shiftDemands,
                                       Collection<ShiftLength> allowedShiftLengths) {
        Set<ShiftReq> result = new TreeSet<>(new Comparator<ShiftReq>() {
            @Override
            public int compare(ShiftReq o1, ShiftReq o2) {
                return 1;
            }
        });

        Map<Integer, Collection<Integer>> allowedLengths = getAllowedLengths(allowedShiftLengths);
        BallPool ballPool = createBallPool(shiftDemands);

        Computer computer = new Computer(allowedLengths, ballPool);
        computer.compute();

        ComputationResult computationResult = computer.getResultHolder().getFirstComputationResult();

        if (computationResult != null) {
            for (Interval interval : computationResult.getIntervals()) {
                ShiftReq shiftReq = new ShiftReq(new PrimaryKey(null, StringUtils.EMPTY));
                shiftReq.getPrimaryKey().setId(null);
                shiftReq.setEmployeeCount(1);

                int lengthInMin = interval.getLength() * granule;
                long startTime = interval.getStartPosition() * granule * 60 * 1000L;

                ShiftLength shiftLength = findShiftLength(allowedShiftLengths, lengthInMin);
                ShiftType shiftType = findShiftType(shiftLength, startTime);

                shiftReq.setShiftType(shiftType);

                result.add(shiftReq);
            }
        }

        return result;
    }

    private ShiftLength findShiftLength(Collection<ShiftLength> shiftLengths, int length) {
        for (ShiftLength shiftLength : shiftLengths) {
            if (shiftLength.getLengthInMin() == length) {
                return shiftLength;
            }
        }
        return null;
    }

    private ShiftType findShiftType(ShiftLength shiftLength, long startTime) {
        for (ShiftType shiftType : shiftLength.getShiftTypes()) {
            if (shiftType.getStartTime().getMillisOfDay() == startTime) {
                return shiftType;
            }
        }
        return null;
    }

    private Map<Integer, Collection<Integer>> getAllowedLengths(Collection<ShiftLength> allowedShiftLengths) {
        Map<Integer, Collection<Integer>> result = new HashMap<>();

        for (ShiftLength shiftLength : allowedShiftLengths) {
            Collection<Integer> startPositions = new HashSet<>();
            for (ShiftType shiftType : shiftLength.getShiftTypes()) {
                int startPosition = shiftType.getStartTime().getMillisOfDay() / (granule * 60 * 1000);
                startPositions.add(startPosition);
            }
            result.put(shiftLength.getLengthInMin() / granule, startPositions);
        }

        return result;
    }

    private BallPool createBallPool(Collection<ShiftDemand> shiftDemands) {
        BallPool result = new BallPool();

        for (ShiftDemand shiftDemand : shiftDemands) {
            int startMinutes = 60 * shiftDemand.getStartTime().getHourOfDay()
                    + shiftDemand.getStartTime().getMinuteOfHour();
            int endMinutes = startMinutes + shiftDemand.getLengthInMin();

            int startPosition = startMinutes / granule;
            int endPosition = endMinutes / granule;
            for (int i = startPosition; i < endPosition; i++) {
                for (int j = 0; j < shiftDemand.getEmployeeCount(); j++) {
                    Ball ball = new Ball(i);
                    result.putBall(ball);
                }
            }
        }

        return result;
    }

}
