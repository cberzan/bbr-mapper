/**
 * ADE 1.0
 * Copyright 1997-2010 HRILab (http://hrilab.org/)
 *
 * All rights reserved.  Do not copy and use without permission.
 * For questions contact Matthias Scheutz at mscheutz@indiana.edu
 *
 * @author Andy Sayler & Constantin Berzan
 * (original template by Paul Schermerhorn)
 */

package com.slam;

import ade.*;
import ade.exceptions.ADECallException;
import com.*;
import java.io.*;
import java.math.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import static utilities.Util.*;
import java.util.Hashtable.*;
import Jama.*;

public class EKFServerImpl extends ADEServerImpl implements EKFServer {
    private static final long serialVersionUID = 1L;

    /* ADE-related fields (pseudo-refs, etc.) */
    private static String prg = "EKFServerImpl";
    private static String type = "EKFServer";
    private static boolean verbose = false;

    /* Server-specific fields */
    private Updater u;
    private Object odomServer = null;
    private Object landmarkServer = null;
    private Hashtable landmarkTable = null;
    private Pose currentPose;

    // ***********************************************************************
    // *** Abstract methods in ADEServerImpl that need to be implemented
    // ***********************************************************************
    /**
     * This method will be activated whenever a client calls the
     * requestConnection(uid) method. Any connection-specific initialization
     * should be included here, either general or user-specific.
     */
    protected void clientConnectReact(String user) {
        System.out.println(myID + ": got connection from " + user + "!");
        return;
    }

    /**
     * This method will be activated whenever a client that has called the
     * requestConnection(uid) method fails to update (meaning that the
     * heartbeat signal has not been received by the reaper), allowing both
     * general and user specific reactions to lost connections. If it returns
     * true, the client's connection is removed.
     */
    protected boolean clientDownReact(String user) {
        System.out.println(myID + ": lost connection with " + user + "!");
        return false;
    }

    /**
     * This method will be activated whenever a client that has called
     * the requestConnection(uid) method fails to update (meaning that the
     * heartbeat signal has not been received by the reaper). If it returns
     * true, the client's connection is removed. 
     */
    protected boolean attemptRecoveryClientDown(String user) {
        return false;
    }

    /**
     * This method will be activated whenever the heartbeat returns a
     * remote exception (i.e., the server this is sending a
     * heartbeat to has failed). 
     */
    protected void serverDownReact(String serverkey, String[][] constraints) {
        String s = constraints[0][1];
        System.out.println(myID + ": reacting to down " + s + "!");

        //if (s.indexOf("VelocityServer") >= 0)
        //    gotVel = false;
        return;
    }

    /** This method will be activated whenever the heartbeat reconnects
     * to a client (e.g., the server this is sending a heartbeat to has
     * failed and then recovered). <b>NOTE:</b> the pseudo-reference will
     * not be set until <b>after</b> this method is executed. To perform
     * operations on the newly (re)acquired reference, you must use the
     * <tt>ref</tt> parameter object.
     * @param s the ID of the {@link ade.ADEServer ADEServer} that connected
     * @param ref the pseudo-reference for the requested server */
    protected void serverConnectReact(String serverkey, Object ref, String[][] constraints) {
        String s = constraints[0][1];
        System.out.println(myID + ": reacting to connecting " + s + "!");

        //if (s.indexOf("VelocityServer") >= 0)
        //    gotVel = true;
        return;
    }

    /**
     * Adds additional local checks for credentials before allowing a shutdown
     * must return "false" if shutdown is denied, true if permitted
     */
    protected boolean localrequestShutdown(Object credentials) {
        return false;
    }

    /**
     * Implements the local shutdown mechanism that derived classes need to
     * implement to cleanly shutdown
     */
    protected void localshutdown() {
        System.out.print("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.print(prg + " shutting down...");
        //finalize();
        u.halt();
        Sleep(100);
        System.out.println("done.");
    }

    /** The server is always ready to provide its service after it has come up */
    protected boolean localServicesReady() {
        return true;
    }

    // ***********************************************************************
    // Methods available to remote objects via RMI
    // ***********************************************************************
    public EKFServerImpl() throws RemoteException {
        super();

        System.err.println("Entered EKFServerImpl() constructor.");

        // Get ref to odometry server, which can be either ADESim or Videre.
        while(odomServer == null) {
            // Try to connect to the simulator.
            System.err.println("Trying to connect to ADESimActorServer.");
            odomServer = getClient("com.adesim2010.ADESimActorServer");
            if(odomServer != null) {
                System.err.println("Connected to ADESimActorServer.");
                break;
            }
            
            // Try to connect to the robot.
            System.err.println("Trying to connect to VidereServer.");
            odomServer = getClient("com.videre.VidereServer");
            if(odomServer != null) {
                System.err.println("Connected to VidereServer.");
                try {
                    call(odomServer, "resetOdometry");
                } catch(Exception e) {
                    System.err.println("Error resetting Videre odometry: " + e);
                    System.err.println("Original cause: " + e.getCause());
                    e.printStackTrace();
                    System.exit(1);
                }
                break;
            }
            
            System.out.println("EKFServerImpl waiting for odomServer ref.");
            Sleep(200);
        }
        
        // Initialize odometry to zero.
        currentPose = new Pose();

        // Get ref to landmark server.
        while(landmarkServer == null) {
            // Try to connect to the simulator.
            System.out.println("Trying to connect to LandmarkServer.");
            landmarkServer = getClient("com.slam.LandmarkServer");
            if(landmarkServer != null) {
                System.out.println("Connected to LandmarkServer.");
                break;
            }
            System.out.println("EKFServerImpl waiting for landmarkServer ref.");
            Sleep(200);
        }
        
        //Initialize Kalman Matricies and vars
        try {
            initKalman();
        } catch(Exception e){
            System.err.println("Error on EKF Initialization: " + e);
            System.err.println("Original cause: " + e.getCause());
            e.printStackTrace();
        }
        //Initialize Hashtable to empty
        landmarkTable = new Hashtable();
        
        // Thread to do whatever periodic updating needs to be done
        u = new Updater();
        u.start();
        System.out.println("EKFServerImpl() constructor finished ----------------------------------------");
    }

    /// Get current pose as predicted by EKF.
    public Pose getPose() throws RemoteException {
        
        System.out.println("EKFServerImpl: currentPose " + currentPose);
        return currentPose;
    }
    //Kalman Vars
    private Matrix matX = null; //State
    private Matrix matP = null; //Covariance
    private Matrix matK = null; //Kalman gain
    private Matrix[] matH; //Measurement Jacobian
    private Matrix matA = null; //Prediction Jacobian
    private Matrix[] matJxr; //Landmark Prediction Jacobian - cartesian 
    private Matrix[] matJz;  //Landmark Prediction Jacobian - polar
    private Matrix matQ = null; //Process Noise
    private double cq; //Process Noise Gaussian 

    private void updatePose() {
        currentPose.x = matX.get(0,0);
        currentPose.y = matX.get(1,0);
        //Theta limited to between 0 and 2pi
        double theta = matX.get(2,0);
        while(theta < 0){
            theta = theta + (2 * Math.PI);
        }
        while(theta > 2 * Math.PI) {
            theta = theta - (2 * Math.PI);
        }
        currentPose.theta = theta;
    }
    
    //Step 0: Initialize
    private void initKalman() {
        //Global Constatns
        cq = .01;

        //Local Contants
        double initXvar = .01;
        double initYvar = .01;
        double initThetaVar = .01;

        //Initialize X
        matX = new Matrix(3, 1);
        matX.set(0, 0, 0);
        matX.set(1, 0, 0);
        matX.set(2, 0, 0);
        
        //Initialize P
        matP = new Matrix(3, 3);
        matP.set(0, 0, initXvar);
        matP.set(1, 1, initYvar);
        matP.set(2, 2, initThetaVar);
    }
    
    //Step 1: Update from Odometry
    private void odoStateUpdate(double delX, double delY, double delTheta) {
        
        //Update X - State
        double newX = matX.get(0, 0) + delX;
        matX.set(0, 0, newX);
        double newY = matX.get(1, 0) + delY;
        matX.set(1, 0, newY);
        double newTheta = matX.get(2, 0) + delTheta;
        matX.set(2, 0, newTheta);
        
        //Update A - Prediction Jacobian
        matA = Jama.Matrix.identity(3, 3);
        matA.set(0, 2, (-1 * delY)); //(1,3) = -delY
        matA.set(1, 2, (delX));      //(2,3) = delX

        //Update Q
        double delT = Math.sqrt(delX*delX + delY*delY); //delT
        matQ = new Matrix(3, 3);
        matQ.set(0, 0, (cq*delX*delX)); //(1,1) = cq * delX^2
        matQ.set(0, 1, (cq*delX*delY)); //(1,2) = cq * delX * delY
        matQ.set(0, 2, (cq*delX*delT)); //(1,3) = cq * delX * delT
        matQ.set(1, 0, (cq*delY*delX)); //(2,1) = cq * delY * delX
        matQ.set(1, 1, (cq*delY*delY)); //(2,2) = cq * delY^2
        matQ.set(1, 2, (cq*delY*delT)); //(2,3) = cq * delY * delT
        matQ.set(2, 0, (cq*delT*delX)); //(3,1) = cq * delT * delX
        matQ.set(2, 1, (cq*delT*delY)); //(3,2) = cq * delT * delY
        matQ.set(2, 2, (cq*delT*delT)); //(3,3) = cq * delT^2

        //Update P upper left
        Matrix matPrr = matP.getMatrix(0, 2, 0, 2);
        Matrix temp = matA.copy(); // temp = A
        temp.times(matPrr);        // temp = A * Prr
        temp.times(matA);          // temp = (A * Prr * A)
        temp.plus(matQ);           // temp = (A * Prr * A) + Q
        matP.setMatrix(0, 2, 0, 2, temp);

        //Update P upper rows
        //TODO: Check if we need to include cols 0-2? Already updated?
        Matrix matPri = matP.getMatrix(0, 2, 0, matP.getColumnDimension()-1); 
        temp = matA.copy(); // temp = A
        temp.times(matPri);
        //TODO: Check if we need to include cols 0-2? Already updated?
        matP.setMatrix(0, 2, 0, matP.getColumnDimension()-1, temp); 

    }

    //Step 2
    private void updateKalman(){
        
    }

    
    /**
     * Provide additional information for usage...
     */
    protected String additionalUsageInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Server-specific options:\n\n");
        sb.append("  -verbose                  <verbose printing>\n");
        return sb.toString();
    }

    /** 
     * Parse additional command-line arguments
     * @return "true" if parse is successful, "false" otherwise 
     */
    protected boolean parseadditionalargs(String[] args) {
        boolean found = false;
        // Note that class fields set here must be declared static above
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-verbose")) {
                verbose = true;
                found = true;
            } else {
                System.out.println("Unrecognized argument: " + args[i]);
                return false;  // return on any unrecognized args
            }
        }
        return found;
    }

    /**
     * The <code>Updater</code> is the main loop that does whatever this
     * server does.
     */
    private class Updater extends Thread {
        
        private boolean shouldRead;
        
        public Updater() {
            shouldRead = true;
        }
        
        public void run() {
            int i = 0;
            Pose odomPose = new Pose();
            Pose truePose = new Pose();

            while (shouldRead) {
                //Get Noisy Odometry
                System.err.println("-----EKF Update-----");
                try {
                    double[] odom = (double[])call(odomServer,
                                                   "getPoseEgo");
                    if(odom != null) {
                        // HACK: figure out how to handle this better
                        odomPose.x = odom[0];
                        odomPose.y = odom[1];
                        odomPose.theta = odom[2];
                    }
                    else {
                        System.err.println("Warning! odometry Null!");
                    }
                } catch(Exception e) {
                    System.err.println("Error getting odometry: " + e);
                    System.err.println("Original cause: " + e.getCause());
                    e.printStackTrace();
                    // Don't exit, hoping this is a temporary problem.
                }

                System.err.println("Initial currentPose: " + currentPose);
                System.err.println("Initial odomPose: " + odomPose);
                
                //Find Odom Deltas 
                Pose odomDelPose = new Pose();
                odomDelPose.x = odomPose.x - currentPose.x;
                odomDelPose.y = odomPose.y - currentPose.y;
                odomDelPose.theta = odomPose.theta - currentPose.theta;
                System.err.println("Native odomDelPose: " + odomDelPose);
                //Unwrap Theta
                while(odomDelPose.theta > Math.PI) {
                    odomDelPose.theta = ((-1 * odomDelPose.theta) +
                                         (2 * Math.PI));
                    System.err.println("Warning: Unwrapping Theta - Pos!");
                }
                while(odomDelPose.theta < (-1 * Math.PI)) {
                    odomDelPose.theta = ((-1 * odomDelPose.theta) -
                                         (2 * Math.PI));
                    System.err.println("Warning: Unwrapping Theta - Neg!");
                }

                System.err.println("Unwrapped odomDelPose: " + odomDelPose);

                //EKF: Step 1 - Updated Pose from Odo Estimate
                try {
                    odoStateUpdate(odomDelPose.x,
                                   odomDelPose.y, odomDelPose.theta);
                } catch(Exception e){
                    System.err.println("Error on EKF step 1: " + e);
                    System.err.println("Original cause: " + e.getCause());
                    e.printStackTrace();
                    // Don't exit, hoping this is a temporary problem.
                }

                //Update Pose with Estimated Position
                try {
                    updatePose();
                } catch(Exception e){
                    System.err.println("Error on EKF updatePose: " + e);
                    System.err.println("Original cause: " + e.getCause());
                    e.printStackTrace();
                    // Don't exit, hoping this is a temporary problem.
                }
                System.err.println("Odom Updated currentPose: " + currentPose);
                
                //Get Landmarks
                try {
                    Landmark[] landmarks = (Landmark[])call(landmarkServer,
                                                            "getLandmarks",
                                                            currentPose);
                    if(landmarks == null) {
                        // HACK: figure out how to handle this better
                        System.out.println("Warning: Null Landmarks!");
                    }
                } catch(Exception e) {
                    System.err.println("Error getting landmarks: " + e);
                    System.err.println("Original cause: " + e.getCause());
                    e.printStackTrace();
                    // Don't exit, hoping this is a temporary problem.
                }
                
                //Update Kalman Filter
                try {
                    updateKalman();
                } catch(Exception e) {
                    System.err.println("Error updating Kalman: " + e);
                    System.err.println("Original cause: " + e.getCause());
                    e.printStackTrace();
                    // Don't exit, hoping this is a temporary problem.
                }

                System.err.println("Final Updated currentPose: " + currentPose);
                /*
                //Get True Position
                try {
                    double[] odom = (double[])call(odomServer,
                                                   "getPoseEgo");
                    if(odom != null) {
                        // HACK: figure out how to handle this better
                        truePose.x = odom[0];
                        truePose.y = odom[1];
                        truePose.theta = odom[2];
                        }
                    
                    } catch(Exception e) {
                System.err.println("Error getting true odometry: " + e);
                    System.err.println("Original cause: " + e.getCause());
                    e.printStackTrace();
                    // Don't exit, hoping this is a temporary problem.
                    }
                
                System.out.println("truePose: " + truePose);
                */
                Sleep(100);
            }
            System.out.println(prg + ": Exiting Updater thread...");
        }
        
        public void halt() {
            System.out.print("halting update thread...");
            shouldRead = false;
        }
    }

    /**
     * Log a message using ADE Server Logging, if possible.  The ASL facility
     * takes care of adding timestamp, etc.
     * @param o the message to be logged
     */
    protected void logItASL(Object o) {
        canLogIt(o);
    }

    /**
     * Set the state of ADE Server Logging.  When true and logging is not
     * started, this starts logging.  When false and logging is started, this
     * stops logging and causes the log files to be written to disk.  ADE server
     * logging is a global logging facility, so starting logging here enables
     * logging in a currently instantiated ADE servers.  NOTE: You want to stop
     * ADE server logging before quitting, or the files will not be complete.
     * @param state indicates whether to start (true) or stop (false) logging.
     */
    protected void setASL(boolean state) {
        try {
            setADEServerLogging(state);
        } catch (Exception e) {
            System.out.println("setASL: " + e);
        }
    }

    /** update the server once */
    protected void updateServer() {
    }

    /** read in one update from the log once */
    protected void updateFromLog(String logEntry) {
    }

    /**
     * <code>main</code> passes the arguments up to the ADEServerImpl
     * parent.  The parent does some magic and gets the system going.
     *
    public static void main(String[] args) throws Exception {
        ADEServerImpl.main(args);
    }
     */
}
