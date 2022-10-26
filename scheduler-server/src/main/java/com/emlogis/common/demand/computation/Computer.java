package com.emlogis.common.demand.computation;

import com.emlogis.common.exceptions.ValidationException;

import java.util.*;

public class Computer {

    static private final long ONE_MINUTE = 60 * 1000;

    private long processStartTime;
    private Map<Integer, Collection<Integer>> allowedLengths;
    private BallPool ballPool;
    private ResultHolder resultHolder = new ResultHolder();

    public Computer(Map<Integer, Collection<Integer>> allowedLengths, BallPool ballPool) {
        this.allowedLengths = allowedLengths;
        this.ballPool = ballPool;
    }

    public ResultHolder getResultHolder() {
        return resultHolder;
    }

    public void compute() {
        if (isValid()) {
            processStartTime = System.currentTimeMillis();
            fitInterval(new ComputationResult());
        } else {
            throw new ValidationException("Demand is invalid");
        }
    }

    private void fitInterval(ComputationResult computationResult) {
        if (System.currentTimeMillis() - processStartTime > ONE_MINUTE) {
            return;
        }

        if (!ballPool.isEmpty()) {
            for (Integer length : allowedLengths.keySet()) {
                List<Integer> startPositions = getStartPositions(ballPool.getBallMinPosition(), length);
                for (int startPosition : startPositions) {
                    Interval interval = new Interval(length, startPosition);
                    boolean isValidInterval = false;
                    for (int i = 0; i < interval.getBalls().length; i++) {
                        Ball ball = ballPool.takeBall(i + startPosition);
                        if (ball != null) {
                            interval.putBall(ball, i);
                            isValidInterval = true;
                        }
                    }
                    if (isValidInterval) {
                        computationResult.addInterval(interval);
                        if (ballPool.isEmpty()) {
                            resultHolder.addIfFit(computationResult);
                        } else {
                            fitInterval(computationResult);
                        }
                        computationResult.removeInterval(interval);
                        ballPool.putBalls(interval.getBalls());
                    }
                }
            }
        }
    }

    private List<Integer> getStartPositions(int ballStartPosition, Integer length) {
        List<Integer> result = new ArrayList<>();
        Collection<Integer> startPositions = allowedLengths.get(length);
        if (startPositions != null) {
            for (Integer start : startPositions) {
                int oddLength = ballStartPosition - start;
                if (oddLength >= 0 && start + length > ballStartPosition) {
                    result.add(start);
                }
            }
        }
        return result;
    }

    private boolean isValid() {
        Map<Integer, Queue<Ball>> pool = ballPool.getPool();
        for (Queue<Ball> ballQueue : pool.values()) {
            if (ballQueue != null && !ballQueue.isEmpty()) {
                Ball ball = ballQueue.element();
                if (ball != null) {
                    for (Integer length : allowedLengths.keySet()) {
                        for (Integer startPosition : allowedLengths.get(length)) {
                            if (ball.getPosition() >= startPosition && ball.getPosition() < startPosition + length) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}
