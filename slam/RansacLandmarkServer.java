package com.slam;

/**
 * Landmark server that detects lines in the laser readings (RANSAC), and uses
 * the pose estimate to convert the lines to point landmarks.
 * @see LandmarkServer
 */
public interface RansacLandmarkServer extends LandmarkServer {
}

