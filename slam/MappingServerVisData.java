// author: Constantin Berzan

package com.slam;

import java.awt.geom.Point2D;
import java.awt.Point;

/// Holds all the visualization data necessary required by MappingServerVis.
public class MappingServerVisData {
    public Point2D.Double mapMin;
    public Point2D.Double mapMax;
    public Point2D.Double mapCenter;
    public double mapPixPerMeter;
    public byte[][] map;
    public Pose robotPose;

    /// Converts from world coordinates to map coordinates.
    Point world2map(Point2D.Double onWorld) {
        Point onMap = new Point();
        onMap.x = (int)((onWorld.x - mapMin.x) * mapPixPerMeter);
        onMap.y = (int)((onWorld.y - mapMin.y) * mapPixPerMeter);
        assert(onMap.x >= 0 && onMap.x < map.length);
        assert(onMap.y >= 0 && onMap.y < map[0].length);
        return onMap;
    }
}
