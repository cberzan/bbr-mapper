package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

public interface MappingServer extends ADEServer {
    /// Updates map based on pose, laser readings, and detected landmarks.
    public void updateMap(Pose pose, double[] laser, Landmark[] landmarks)
        throws RemoteException;

    /// Return data for visualization.
    public MappingServerVisData getVisData() throws RemoteException;
}

