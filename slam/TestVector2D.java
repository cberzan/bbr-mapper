package com.slam;

/**
 * Unit test for Vector2D.java
 */
public class TestVector2D {
    static public void main(String[] args) throws Exception {
        double mag1 = 0.076, dir1 = 195.5 * Math.PI / 180;
        double x1 = mag1 * Math.cos(dir1), y1 = mag1 * Math.sin(dir1);
        Vector2D v1 = new Vector2D();
        v1.setMag(mag1);
        v1.setDir(dir1);
        Util.assert_feq(v1.getMag(), mag1);
        Util.assert_feq(v1.getDir(), dir1);
        Util.assert_feq(v1.getX(), x1);
        Util.assert_feq(v1.getY(), y1);

        double mag2 = 1.0, dir2 = 275.8 * Math.PI / 180;
        double x2 = mag2 * Math.cos(dir2), y2 = mag2 * Math.sin(dir2);
        Vector2D v2 = new Vector2D();
        v2.setMag(mag2);
        v2.setDir(dir2);
        Util.assert_feq(v2.getMag(), mag2);
        Util.assert_feq(v2.getDir(), dir2);
        Util.assert_feq(v2.getX(), x2);
        Util.assert_feq(v2.getY(), y2);

        double sumx = x1 + x2, sumy = y1 + y2;
        double summag = Math.sqrt(sumx * sumx + sumy * sumy),
               sumdir_trig = Math.atan2(sumy, sumx),
               sumdir = Vector2D.myatan2(sumy, sumx);
        Util.assert_feq(Math.sin(sumdir_trig), Math.sin(sumdir));
        Util.assert_feq(Math.cos(sumdir_trig), Math.cos(sumdir));

        Vector2D sum = new Vector2D();
        Util.assert_feq(sum.getMag(), 0);
        Util.assert_feq(sum.getDir(), 0);
        Util.assert_feq(sum.getX(), 0);
        Util.assert_feq(sum.getY(), 0);
        sum = sum.plus(v1);
        sum = sum.plus(v2);
        Util.assert_feq(sum.getX(), sumx);
        Util.assert_feq(sum.getY(), sumy);
        Util.assert_feq(sum.getMag(), summag);
        Util.assert_feq(sum.getDir(), sumdir);

        double factor = 123;
        sum = sum.mul(factor);
        Util.assert_feq(sum.getX(), sumx * factor);
        Util.assert_feq(sum.getY(), sumy * factor);
        Util.assert_feq(sum.getMag(), summag * factor);
        Util.assert_feq(sum.getDir(), sumdir); // unchanged

        System.out.println("Tests passed.");
    }
};
