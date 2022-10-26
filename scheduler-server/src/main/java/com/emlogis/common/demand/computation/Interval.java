package com.emlogis.common.demand.computation;

public class Interval {

    private int startPosition;
    private Ball[] balls;

    public Interval(int length, int startPosition) {
        balls = new Ball[length];
        this.startPosition = startPosition;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public Ball[] getBalls() {
        return balls;
    }
    
    public void putBall(Ball ball, int position) {
        balls[position] = ball;
    }

    public int getLength() {
        return balls.length;
    }

    public int holeCount() {
        int result = 0;
        for (Ball ball : balls) {
            if (ball == null) {
                result++;
            }
        }
        return result;
    }

}
