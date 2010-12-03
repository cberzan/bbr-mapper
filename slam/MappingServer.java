package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

public interface MappingServer extends ADEServer {
    public void updateMap(Pose pose, double[] laser) throws RemoteException;
}

