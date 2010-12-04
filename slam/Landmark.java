package com.slam;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Holds a landmark: its ID and global position in world coordinates.
 */
public class Landmark implements Serializable {
    /// Unique id the landmark is identified as.
    public int id;
    /// Line landmark (only used by RansacLandmarkServer).
    public Line line;
    /// Point landmark (used by BeaconLandmarkServer and RansacLandmarkServer).
    public Point2D.Double position;
};
