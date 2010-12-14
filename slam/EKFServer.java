/*
 * Andy Sayler
 * Constantin Berzan
 *
 */

package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

/**
 * Extended Kalman Filter for SLAM.
 * Uses odometry and laser data to determine robot's pose.
 *
 * See the tutorial by Riisgaard, Blas: SLAM for Dummies
 * http://ocw.mit.edu/courses/aeronautics-and-astronautics/16-412j-cognitive-robotics-spring-2005/projects/1aslam_blas_repo.pdf
 */
public interface EKFServer extends ADEServer {
    /// Get current pose as predicted by EKF.
    public Pose getPose() throws RemoteException;
}
