package com.slam;

import ade.*;
import com.*;
import com.interfaces.*;
import java.rmi.*;

public interface LandmarkServer extends ADEServer {
    public Vector2D[] getLandmarks() throws RemoteException;
}

