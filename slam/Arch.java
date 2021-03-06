// COMP150 Behavior-Based Robotics
// Assignment 1
// (c) Matthias Scheutz

package com.slam;

import java.rmi.RemoteException;
import ade.ADEServer;
import com.action.ActionServer;
public interface Arch extends ActionServer {
    public Double[][] getBeaconData() throws RemoteException;
}
