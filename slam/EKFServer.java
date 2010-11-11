package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

public interface EKFServer extends ADEServer {
    /// Get current pose as predicted by EKF.
    Pose getPose() throws RemoteException;
}
