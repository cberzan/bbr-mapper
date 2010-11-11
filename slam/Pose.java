package com.slam;

import java.io.Serializable;

public class Pose implements Serializable
{
    private static final long serialVersionUID = 1L;

    public double x;
    public double y;
    public double theta;

    public Pose() {
        x = y = theta = 0;
    }

    public Pose(double x0, double y0, double theta0) {
        x = x0;
        y = y0;
        theta = theta0;
    }

    @Override
    public String toString() {
        return String.format("x=%.4fm; y=%.4fm; theta=%.4frad",
                x, y, theta);
    }
};
