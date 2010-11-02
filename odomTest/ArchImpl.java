// COMP150 Behavior-Based Robotics
// (c) Matthias Scheutz
//
// Modifed by:
// Andy Sayler
// Constantin Berzan
// 
// 
// 

package com.odomTest;

import com.action.ActionServerImpl;
import java.rmi.*;
import java.util.HashMap;
import java.util.Random;
import java.lang.Math;
import java.io.*;

public class ArchImpl extends ActionServerImpl implements Arch
{
    private static final long serialVersionUID = 1L;

    // Run parameters:
    private final String trial    = "4a";
    private final double stopDist = 1;
    private final double tSpeed   = 0.1,
                         rSpeed   = 0;

    // Run data:
    Object videre    = null;
    private int step = 0;

    // Logging:
    FileWriter logFile    = null;
    BufferedWriter logBuf = null;

    /**
     * Called once on first run. Gets Videre ref, resets odometry, and opens log
     * file.
     */
    private void init()
    {
        assert(videre == null);
        try {
            // Get Videre ref.
            videre = getClient("com.videre.VidereServer");
            System.out.println("Got Videre object: " + videre);    

            // Reset odometry.
            call(videre, "COMorigin"); // FIXME: probably need to publish it in
                                       //        the VidereServer interface.
            double[] pose = (double[])call(videre, "getPoseEgo");
            assert(pose[0] == 0 && pose[1] == 0 && pose[2] == 0);
            System.out.println("Reset odometry counters.");

            // Open log.
            String logName = "odom-" + trial + ".csv";
            logFile        = new FileWriter(logName, /*append = */ false);
            logBuf         = new BufferedWriter(logBuf);
            logBuf.write("step,x,y,theta\n");
            logBuf.write("0,0,0,0\n");
            System.out.println("Opened log.");
        } catch(Exception e) {
            System.err.println("Error in init(): " + e);
            System.err.println("Original cause: " + e.getCause());
            System.exit(1);
        }
    }

    /// Called when the trial is over. Closes the log and exits.
    private void cleanExit()
    {
        try {
            logBuf.flush();
            logBuf.close();
            System.out.println("Clean exit.");
            System.exit(0);
        } catch(Exception e) {
            System.err.println("Error in cleanExit(): " + e);
            System.err.println("Original cause: " + e.getCause());
            System.exit(1);
        }
    }

    /// Runs an odometry trial run.
    public void runArchitecture()
    {
        if(videre == null) {
            init();
        }

        try {
            assert(videre != null);
            step++;

            // Get odometry data and log it.
            double[] pose = (double[])call(videre, "getPoseEgo");
            double dist = Math.sqrt(pose[0]*pose[0] + pose[1]*pose[1]);
            System.out.format("step %d: x %lf y %lf theta %lf (dist %lf)\n",
                    step, pose[0], pose[1], pose[2], dist);
            logBuf.write(step + "," + pose[0] + "," + pose[1] + ","
                         + pose[2] + "\n");

            // Move forward, or stop if goal distance reached.
            if(dist >= stopDist) {
                stop();
                cleanExit();
            } else {
                setVels(tSpeed, rSpeed);
            }
        } catch(Exception e) {
            System.err.println("Error in runArchitecture(): " + e);
            System.err.println("Original cause: " + e.getCause());
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
