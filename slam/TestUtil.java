package com.slam;

/**
 * Unit test for Util.java
 */
public class TestUtil {
    static public void main(String[] args) throws Exception {
        final double pi = Math.PI;
        Util.assert_feq(pi, Util.normalizeRad(pi));
        Util.assert_feq(pi, Util.normalizeRad(-pi));
        Util.assert_feq(pi, Util.normalizeRad(3 * pi));
        Util.assert_feq(pi + 0.1, Util.normalizeRad(5 * pi + 0.1));
        Util.assert_feq(pi - 0.1, Util.normalizeRad(5 * pi - 0.1));
        Util.assert_feq(pi + 0.1, Util.normalizeRad(-5 * pi + 0.1));
        Util.assert_feq(pi - 0.1, Util.normalizeRad(-5 * pi - 0.1));

        System.out.println("Tests passed.");
    }
};
