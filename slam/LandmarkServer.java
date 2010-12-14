package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

/**
 * Keeps track of landmarks -- re-observable features in the environment.
 */
public interface LandmarkServer extends ADEServer {
    /**
     * Return the landmarks that have been seen in the last scan AND have been
     * seen a sufficient number of times. Landmarks have unique IDs.
     */
    public Landmark[] getLandmarks(Pose robotPose) throws RemoteException;

    /**
     * Return the IDs of landmarks that have been forgotten, and mark those IDs
     * as being useable again for new landmarks.
     */
    public int[] flushDiscardedLandmarks() throws RemoteException;
}

