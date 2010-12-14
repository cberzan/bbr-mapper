package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

/**
 * Simple mapping based on an ocupancy grid. The word coordinates are defined in
 * RobotInfo.
 */
public interface MappingServer extends ADEServer {
    /// Updates map based on pose, laser readings, and detected landmarks.
    public void updateMap(Pose pose, double[] laser, Landmark[] landmarks)
        throws RemoteException;

    /// Return data for visualization.
    public MappingServerVisData getVisData() throws RemoteException;
}

