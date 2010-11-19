package com.slam;

import java.io.Serializable;

/**
 * Holds a landmark: its ID and relative position to the robot.
 */
public class Landmark implements Serializable {
	public int id;
	public Vector2D position;
};
