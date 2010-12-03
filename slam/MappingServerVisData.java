// author: Constantin Berzan

package com.slam;

import java.awt.geom.Point2D;

/// Holds all the visualization data necessary required by MappingServerVis.
public class MappingServerVisData {
    public Point2D.Double mapMin;
    public Point2D.Double mapMax;
    public Point2D.Double mapCenter;
    public double mapPixPerMeter;
    public byte[][] map;
}
