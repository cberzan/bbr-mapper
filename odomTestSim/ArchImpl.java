// COMP150 Behavior-Based Robotics
// (c) Matthias Scheutz
//
// Modifed by:
// Constantin Berzan
// 

package com.odomTestSim;

import com.action.ActionServerImpl;
import java.rmi.*;
import java.util.HashMap;
import java.util.Random;
import java.lang.Math;
import java.io.*;

public class ArchImpl extends ActionServerImpl implements Arch
{
    private static final long serialVersionUID = 1L;

    private final double stopDist = 1;
    private final double tSpeed   = 0.1,
                         rSpeed   = 0;

    private int step = 0;
    private double[] poseEgoInit,
                     poseGlobalInit;
    private Object posServer = null,
                   velServer = null,
                   robotRef  = null;

    /**
     * Moves ahead stopDist meters with speed tSpeed, then stops.
     * Prints out odometry and global pose at each step.
     */
    public void runArchitecture()
    {
        if(robotRef == null) {
            try {
                // Get reference to robot.
                // getPoseEgo() seems to be defined in the various robot
                // classes, not a common interface. No way to get a generic ref?
                posServer = getClient("com.interfaces.PositionServer");
                velServer = getClient("com.interfaces.VelocityServer");
                robotRef  = getClient("com.adesim2010.ADESimActorServer");
                System.out.println("Got posServer " + posServer
                        + ", velServer " + velServer
                        + ", robotRef " + robotRef);

                // Check initial odometry and position.
                poseGlobalInit = (double[])call(posServer, "getPoseGlobal");
                System.out.format("Initial global position: x %f y %f theta %f\n",
                        poseGlobalInit[0], poseGlobalInit[1], poseGlobalInit[2]);
                poseEgoInit = (double[])call(robotRef, "getPoseEgo");
                System.out.format("Initial odometry: x %f y %f theta %f\n",
                        poseEgoInit[0], poseEgoInit[1], poseEgoInit[2]);
            } catch(Exception e) {
                System.err.println("Error during init: " + e);
                e.printStackTrace();
                stop();
                System.exit(1);
            }
        }

        try {
            assert(posServer != null && velServer != null && robotRef != null);
            step++;

            // Get odometry data.
            double[] poseEgo    = (double[])call(robotRef,  "getPoseEgo");
            double[] poseGlobal = (double[])call(posServer, "getPoseGlobal");
            double dist = Math.sqrt(poseEgo[0] * poseEgo[0] + poseEgo[1] * poseEgo[1]);
            System.out.format("step % 5d:  x %f  y %f  theta %f (dist %f)\n",
                    step, poseEgo[0], poseEgo[1], poseEgo[2], dist);
            System.out.format("            gx %f gy %f gtheta %f\n",
                    poseGlobal[0], poseGlobal[1], poseGlobal[2]);

            /*
            // Move forward, or stop if goal distance reached.
            if(dist >= stopDist) {
                stop();

                double dxOdo = poseEgo[0] - poseEgoInit[0],
                       dyOdo = poseEgo[1] - poseEgoInit[1],
                       dtOdo = poseEgo[2] - poseEgoInit[2];
                double dxGlob = poseGlobal[0] - poseGlobalInit[0],
                       dyGlob = poseGlobal[1] - poseGlobalInit[1],
                       dtGlob = poseGlobal[2] - poseGlobalInit[2];
                System.out.format("Odometry:        dx %f dy %f dtheta %f\n",
                        dxOdo, dyOdo, dtOdo);
                System.out.format("Global position: dx %f dy %f dtheta %f\n",
                        dxGlob, dyGlob, dtGlob);

                System.exit(0);
            } else {
                setVels(tSpeed, rSpeed);
            }
            */
        } catch(Exception e) {
            System.err.println("Error during run: " + e);
            System.err.println("Original cause: " + e.getCause());
            stop();
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
