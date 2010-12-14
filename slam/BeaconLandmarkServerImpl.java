/**
 * ADE 1.0
 * Copyright 1997-2010 HRILab (http://hrilab.org/)
 *
 * All rights reserved.  Do not copy and use without permission.
 * For questions contact Matthias Scheutz at mscheutz@indiana.edu
 *
 * @author Constantin Berzan
 * (template by Paul Schermerhorn)
 */
package com.slam;

import ade.*;
import ade.exceptions.ADECallException;
import com.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.math.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import static utilities.Util.*;

/**
 * @see BeaconLandmarkServer
 */
public class BeaconLandmarkServerImpl extends ADEServerImpl implements BeaconLandmarkServer {
    /* ADE-related fields (pseudo-refs, etc.) */

    private static String prg = "BeaconLandmarkServerImpl";
    private static String type = "LandmarkServer";
    private static boolean verbose = false;

    /* Server-specific fields */
    private Object actionServer = null;
    private Double[][] beacons  = null;
    final int HEADING = 0, DISTANCE = 1;

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
    public BeaconLandmarkServerImpl() throws RemoteException {
        super();
    }

    /// Gets refs to all necessary servers.
    private boolean allServersReady() {
        // Get ref to action server, from which we get the beacon data.
        if(actionServer == null) {
            // Try to connect to the simulator.
            actionServer = getClient("com.action.ActionServer");
            if(actionServer == null) {
                System.out.println("BeaconLandmarkServerImpl waiting for actionServer ref.");
                return false;
            }
        }

        return true;
    }

    /// @see LandmarkServer.getLandmarks()
    public Landmark[] getLandmarks(Pose robotPose) throws RemoteException {
        if(!allServersReady()) {
            System.err.println("getLandmarks: not initialized yet!");
            return new Landmark[0];
        }

        try {
            beacons = (Double[][])call(actionServer, "getBeaconData");
        } catch(Exception e) {
            System.err.println("ERROR: getLandmarks could not getBeaconData: " + e);
            e.printStackTrace();
            return new Landmark[0];
        }

        Landmark[] landmarks = new Landmark[beacons.length];
        for(int i = 0; i < beacons.length; i++) {
            //System.out.format("id = %d, dist=%f heading=%f\n",
            //        i, beacons[i][DISTANCE], beacons[i][HEADING]);

            // Convert from robot's coord sys to world coord sys.
            // Apparently, in ADESim2010 the beacon orientation is relative to
            // the robot position only, i.e. it is independent of pose.theta.
            Vector2D v = new Vector2D();
            v.setPol(beacons[i][DISTANCE], Util.deg2rad(beacons[i][HEADING]));
            v.setX(v.getX() + robotPose.x);
            v.setY(v.getY() + robotPose.y);

            Landmark lm  = new Landmark();
            lm.id        = i;
            lm.position  = new Point2D.Double(v.getX(), v.getY());
            landmarks[i] = lm;
        }
        System.out.format("BeaconLandmarkServer returning %d landmarks.\n",
                landmarks.length);
        return landmarks;
    }

    /// @see LandmarkServer.flushDiscardedLandmarks()
    public int[] flushDiscardedLandmarks() throws RemoteException {
        return new int[0]; // never discards anything
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
