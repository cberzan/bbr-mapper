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
    private final String trial     = "8c";
    private final double stopDist  = 2;
    private final int    moveTicks = 20,
                         stopTicks = 10;
    private final double tSpeed    = 0.1,
                         rSpeed    = 0;

    // Run data:
    private Object videre = null;
    private int step = 0;
    private int counter = 0;
    private boolean moving = true;

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
            // Doesn't work because function is not remotely callable.
            // call(videre, "COMorigin"); // FIXME: probably need to publish it in
            //                            //        the VidereServer interface.
            double[] pose = (double[])call(videre, "getPoseEgo");
            assert(pose[0] == 0 && pose[1] == 0 && pose[2] == 0);
            if(pose[0] != 0 || pose[1] != 0 || pose[2] != 0) {
                System.err.print("WARNING: Odometry not zero: ");
                System.out.format("x %f y %f theta %f\n",
                        pose[0], pose[1], pose[2]);
                
            }
            System.out.println("Verified zero initial odometry.");

            // Open log.
            String logName = "odom-" + trial + ".csv";
            logFile        = new FileWriter(logName, /*append = */ false);
            logBuf         = new BufferedWriter(logFile);
            logBuf.write("step,counter,mov,x,y,theta\n");
            logBuf.write("0,0,?,0,0,0\n");
            System.out.println("Opened log.");
        } catch(Exception e) {
            System.err.println("Error in init(): " + e);
            e.printStackTrace();
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
            e.printStackTrace();
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

            // Update state.
            counter++;
            if(moving && counter > moveTicks) {
                counter = 0;
                moving  = false;
            }
            if(!moving && counter > stopTicks) {
                counter = 0;
                moving  = true;
            }

            // Get odometry data and log it.
            double[] pose = (double[])call(videre, "getPoseEgo");
            double dist = Math.sqrt(pose[0]*pose[0] + pose[1]*pose[1]);
            System.out.format("step %d counter %d moving %s: x %f y %f theta %f (dist %f)\n",
                    step, counter, moving ? "yes" : "no",
                    pose[0], pose[1], pose[2], dist);
            logBuf.write(step + "," + counter + "," + moving + ","
                         + pose[0] + "," + pose[1] + ","
                         + pose[2] + "\n");

            // Move forward, or stop if goal distance reached.
            if(dist >= stopDist) {
                stop();
                cleanExit();
            } else {
                if(moving)
                    setVels(tSpeed, rSpeed);
                else
                    stop();
            }
        } catch(Exception e) {
            System.err.println("Error in runArchitecture(): " + e);
            e.printStackTrace();
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
