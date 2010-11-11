package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

public interface EKFServer extends ADEServer {
    void reset(Pose initPose) throws RemoteException;
    Pose getPose() throws RemoteException;
}
