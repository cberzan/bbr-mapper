package com.slam;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Holds a 2D line as two constants m and b, s.t. y = mx + b.
 *
 * TODO: This means vertical lines (m=inf) break everything.
 *       Consider alternative representation: ax + by + c = 0
 */
public class Line implements Serializable {
    public double m;
    public double b;

    /**
     * Point-to-line distance.
     * See http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
     */
    public double distance(Point2D.Double point) {
        return Math.abs(m * point.x + b - point.y) / Math.sqrt(m * m + 1);
    }
};
