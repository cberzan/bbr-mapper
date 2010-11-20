package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

public interface LandmarkServer extends ADEServer {
    public Landmark[] getLandmarks(Pose robotPose) throws RemoteException;
    public int[] flushDiscardedLandmarks() throws RemoteException;
}

