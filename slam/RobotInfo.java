package com.slam;

import java.awt.geom.Point2D;

/**
 * Defines several constants about the robot, and several utility functions.
 */
public class RobotInfo {
    /// Laser range for detecting anomalous readings.
    final public double lrfRange = 4; // meters

    /// Number of laser readings.
    final public int numLaser = 181;

    /**
     * Orientation of laser's 0deg vs. robot's 0deg. Depends on robot, but
     * usually 90 degrees.
     */
    final public double laserTheta = -Math.PI / 2;

    /**
     * Distance between laser's origin and robot's center of rotation. Depends
     * on robot. Assuming LRF is on x axis in the robot's coordinate system.
     */
    final public double laserX = 0.2; // empirically determined, see sim-rot-center.ods

    /// Converts a point from robot coordinates to world coordinates.
    public Point2D.Double robot2world(Pose robotPose, Point2D.Double point) {
        Vector2D v = new Vector2D();
        v.setCart(point.x, point.y);

        // Convert from LRF coord sys to robot's coord sys.
        v.setDir(Util.normalizeRad(v.getDir() + laserTheta));
        v.setX(v.getX() + laserX);

        // Convert from robot's coord sys to world coord sys.
        v.setDir(Util.normalizeRad(v.getDir() + robotPose.theta));
        v.setX(v.getX() + robotPose.x);
        v.setY(v.getY() + robotPose.y);

        return new Point2D.Double(v.getX(), v.getY());
    }
}
