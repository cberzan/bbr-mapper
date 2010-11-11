package com.slam;

import java.io.Serializable;

/**
 * Holds a 2D vector.
 * Dir is in radians.
 */
public class Vector2D implements Serializable {
    private static final long serialVersionUID = 1L;

    public double mag;
    public double dir; // rad

    public Vector2D() {
        mag = dir = 0;
    }

    public double getX() {
        return mag * Math.cos(dir);
    }

    public double getY() {
        return mag * Math.sin(dir);
    }

    public double getMag() {
        return mag;
    }

    public double getDir() {
        return dir;
    }

    public void setX(double x) {
        mag = Math.sqrt(x * x + getY() * getY());
        dir = Math.atan2(getY(), x);
    }

    public void setY(double y) {
        mag = Math.sqrt(getX() * getX() + y * y);
        dir = Math.atan2(y, getX());
    }

    public void setCart(double x, double y) {
        setX(x);
        setY(y);
    }

    public void setMag(double mag0) {
        mag = mag0;
    }

    public void setDir(double dir0) {
        assert(dir0 >= -Math.PI && dir0 <= Math.PI);
        dir = dir0;
    }

    public void setPol(double mag0, double dir0) {
        setMag(mag0);
        setDir(dir0);
    }

    public Vector2D plus(Vector2D other) {
        Vector2D result = new Vector2D();
        result.setX(getX() + other.getX());
        result.setY(getY() + other.getY());
        return result;
    }

    public Vector2D mul(double value) {
        Vector2D result = new Vector2D();
        mag *= value;
        return result;
    }
};
