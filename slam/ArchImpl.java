// COMP150 Behavior-Based Robotics
// (c) Matthias Scheutz
//
// Modifed by:
// Andy Sayler
// Constantin Berzan
// 

package com.slam;

import com.action.ActionServerImpl;
import java.rmi.*;
import java.util.HashMap;
import java.util.Random;
import java.lang.Math;
import java.io.*;
import static utilities.Util.*;

public class ArchImpl extends ActionServerImpl implements Arch {
    private static final long serialVersionUID = 1L;

    private Object ekfServer = null;

    /**
     * Moves ahead stopDist meters with speed tSpeed, then stops.
     * Prints out odometry and global pose at each step.
     */
    public void runArchitecture() {
        while(ekfServer == null) {
            ekfServer = getClient("com.slam.EKFServer");
            if(ekfServer != null)
                break;
            System.out.println("ArchImpl: waiting for EKFServer.");
            //return;
            Sleep(200);
        }

        System.out.println("ArchImpl: ekfServer is " + ekfServer);

        try {
            Pose pose = (Pose)call(ekfServer, "getPose");
            System.out.println("Pose: " + pose);
        } catch(Exception e) {
            System.err.println("Error getting pose from EKFServer: " + e);
            System.err.println("Original cause: " + e.getCause());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /** 
     * Constructs the architecture
     */
    public ArchImpl() throws RemoteException
    {
        super();
    }
}
