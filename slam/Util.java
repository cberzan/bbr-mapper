package com.slam;

import java.lang.Math;

/**
 * Defines a bunch of static utility functions.
 */
public class Util {
    /// Convert from degrees to radians.
    static double deg2rad(double rad) {
        return rad * Math.PI / 180;
    }

    /// Convert from degrees to radians.
    static double rad2deg(double rad) {
        return rad * 180 / Math.PI;
    }

    /// Normalizes a radian value into the range [0, 2pi).
    static double normalizeRad(double rad) {
        final double twopi = 2 * Math.PI;
        double excess = twopi * Math.floor(Math.abs(rad / twopi));
        if(rad >= 0)
            return rad - excess;
        else
            return rad + twopi + excess;
    }

    /// Return true if doubles are roughly equal.
    static boolean feq(double a, double b) {
        return Math.abs(a - b) < 1e-7;
    }

    /// Assert that two doubles are roughly equal, with verbose failure.
    static void assert_feq(double a, double b) throws Exception {
        if(!feq(a, b)) {
            System.err.format("assert_feq failed: a=%f b=%f\n", a, b);
            throw new Exception("assert_feq failed");
        }
    }
}
