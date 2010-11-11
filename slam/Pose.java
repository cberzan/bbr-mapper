package com.slam;

import java.io.Serializable;

class Pose implements Serializable
{
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
};
