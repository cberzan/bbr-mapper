/*
 * Andy Sayler
 * Constantine Berzan
 *
 */

package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

public interface EKFServer extends ADEServer {
    /// Get current pose as predicted by EKF.
    public Pose getPose() throws RemoteException;
}
