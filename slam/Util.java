package com.slam;

import java.lang.Math;

public class Util {
    static double deg2rad(double rad) {
        return rad * Math.PI / 180;
    }

    static double rad2deg(double rad) {
        return rad * 180 / Math.PI;
    }

    static boolean feq(double a, double b) {
        return Math.abs(a - b) < 1e-7;
    }

    static void assert_feq(double a, double b) throws Exception {
        if(!feq(a, b)) {
            System.err.format("assert_feq failed: a=%f b=%f\n", a, b);
            throw new Exception("assert_feq failed");
        }
    }
}
