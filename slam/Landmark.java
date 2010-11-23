package com.slam;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Holds a landmark: its ID and global position in world coordinates.
 */
public class Landmark implements Serializable {
	public int id;
    public Point2D.Double position;
};
