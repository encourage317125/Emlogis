package com.emlogis.common.demand.computation;

import java.util.*;

public class BallPool {

    private Map<Integer, Queue<Ball>> pool = new HashMap<>();

    public Map<Integer, Queue<Ball>> getPool() {
        return pool;
    }

    public void putBall(Ball ball) {
        int position = ball.getPosition();
        Queue<Ball> ballQueue = pool.get(position);
        if (ballQueue == null) {
            ballQueue = new LinkedList<>();
            pool.put(position, ballQueue);
        }
        ballQueue.add(ball);
    }

    public void putBalls(Ball... balls) {
        for (Ball ball : balls) {
            if (ball != null) {
                putBall(ball);
            }
        }
    }

    public Ball takeBall(int position) {
        Queue<Ball> ballQueue = pool.get(position);
        if (ballQueue == null) {
            return null;
        }
        return ballQueue.poll();
    }

    public boolean isEmpty() {
        return ballCount() == 0;
    }

    public int getBallMinPosition() {
        int result = -1;
        for (Queue<Ball> ballQueue : pool.values()) {
            if (ballQueue != null && !ballQueue.isEmpty()) {
                Ball ball = ballQueue.element();
                if (result == -1 || ball.getPosition() < result) {
                    result = ball.getPosition();
                }
            }
        }

        return result;
    }

    public int ballCount() {
        int result = 0;
        for (Queue<Ball> ballQueue : pool.values()) {
            if (ballQueue != null) {
                result += ballQueue.size();
            }
        }
        return result;
    }

}
