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

public class RansacLandmarkServerImpl extends ADEServerImpl implements RansacLandmarkServer {
    /* ADE-related fields (pseudo-refs, etc.) */
    private static String prg = "RansacLandmarkServerImpl";
    private static String type = "LandmarkServer";
    private static boolean verbose = false;

    /// Internal structure holding all info about a landmark.
    private class Seamark {
        /// Last time seen.
        public long msLastSeen = 0;
        /// Seen in the most recent run.
        public boolean seenLastRun = false;
        /// Number of times it has been seen.
        public int timesSeen = 0;
        /// Line seen last as this landmark.
        public Line line = null;
        /// Point this landmark has been converted to.
        public Point2D.Double point = null;
    };

    /// Server-specific fields
    private Object lrfServer                    = null;
    private double[] laser                      = null;
    private Seamark[] landmarkDB                = null;
    private PriorityQueue<Integer> availableIDs = null;
    private ArrayList<Integer> discardedIDs     = null;
    private long msLastUpdate                   = 0;
    private boolean initialized                 = false;
    private RobotInfo robot                     = null;

    /// Max number of landmarks in database.
    private int maxLandmarks = 400;
    /// Time it takes to forget a landmark after last seeing it.
    private int msForget = 60 * 1000;
    /// Times a landmark has to be seen before it is shown to the EKF.
    private int seeCount = 15;
    /// Maximum recommended time between polls.
    private long msBetweenPolls = 100;

    /** Distance a landmark has to be from its predicted position in order to be
        identified as the same landmark. */
    private double assocDist = 0.1;

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
    public RansacLandmarkServerImpl() throws RemoteException {
        super();

        // Get ref to LRF server, from which we get the laser data.
        while(lrfServer == null) {
            lrfServer = getClient("com.interfaces.LaserServer");
            if(lrfServer != null)
                break;

            System.out.println("RansacLandmarkServerImpl waiting for lrfServer ref.");
            Sleep(200);
        }

        // Initialize data structures.
        landmarkDB   = new Seamark[maxLandmarks];
        discardedIDs = new ArrayList<Integer>();
        availableIDs = new PriorityQueue<Integer>();
        robot        = new RobotInfo();
        for(int i = 0; i < maxLandmarks; i++)
            availableIDs.add(i);

        // This server does not have an Updater thread -- the landmarks are
        // extracted and updated only when getLandmarks() is called.

        initialized = true;
    }

    public Landmark[] getLandmarks(Pose robotPose) throws RemoteException {
        if(!initialized) {
            System.err.println("getLandmarks: not initialized yet!");
            return new Landmark[0];
        }

        updateLandmarks(robotPose);
        ArrayList<Landmark> good = new ArrayList<Landmark>();
        for(int i = 0; i < maxLandmarks; i++) {
            if(landmarkDB[i] != null && landmarkDB[i].seenLastRun &&
                    landmarkDB[i].timesSeen >= seeCount) {
                Landmark lm   = new Landmark();
                lm.id         = i;
                lm.ransacLine = landmarkDB[i].line;
                lm.position   = landmarkDB[i].point;
                good.add(lm);
            }
        }

        System.out.println("======================================================================");
        System.out.format("getLandmarks (pose x=%f y=%f theta=%f):\n",
                robotPose.x, robotPose.y, robotPose.theta);
        for(int i = 0; i < maxLandmarks; i++) {
            if(landmarkDB[i] == null)
                continue;
            System.out.format("(id=%d timesSeen=%d x=%f y=%f)\n",
                    i, landmarkDB[i].timesSeen,
                    landmarkDB[i].point.x, landmarkDB[i].point.y);
        }
        System.out.format("-> about to return %d landmarks\n", good.size());
        System.out.println("======================================================================");

        return good.toArray(new Landmark[0]);
    }

    public int[] flushDiscardedLandmarks() throws RemoteException {
        int[] result = new int[discardedIDs.size()];
        for(int i = 0; i < discardedIDs.size(); i++) {
            result[i] = discardedIDs.get(i);
            availableIDs.add(result[i]);
        }
        discardedIDs.clear();
        return result;
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


    // ***********************************************************************
    // Landmark updating
    // ***********************************************************************

    /**
     * Updates the landmarkDB: extracts new landmarks, associates them with old
     * landmarks if possible, and discards long-lost landmarks.
     */
    private void updateLandmarks(Pose robotPose) {
        long ms = System.currentTimeMillis();

        // Mark everything as not seen.
        for(int i = 0; i < maxLandmarks; i++) {
            if(landmarkDB[i] != null)
                landmarkDB[i].seenLastRun = false;
        }

        // Extract new landmarks.
        if(!updateLasers()) {
            System.err.println("ERROR: updateLandmarks could not updateLasers");
            return;
        }
        ArrayList<Seamark> fresh = extractLandmarks(robotPose);
        // TODO on the real robot, may want to adjust pose to deal with
        // difference between center of rotation and LRF position.

        // Associate landmarks to previously-seen landmarks.
        // Simple strategy: associate to nearest landmark.
        for(Seamark lm : fresh) {
            int nearest = findNearestLandmark(lm);
            if(nearest != -1) {
                // Seen an old landmark.
                if(landmarkDB[nearest].seenLastRun)
                    System.err.println("ERROR: updateLandmarks saw duplicate landmarks");
                lm.timesSeen        = landmarkDB[nearest].timesSeen + 1;
                landmarkDB[nearest] = lm;
                System.out.format("updateLandmarks: re-observed landmark %d: x=%f y=%f\n",
                        nearest, lm.point.x, lm.point.y);
            } else {
                // Seen a new landmark.
                if(availableIDs.isEmpty()) {
                    System.err.println("ERROR: updateLandmarks has no free slots");
                } else {
                    int id = availableIDs.poll();
                    lm.timesSeen   = 1;
                    landmarkDB[id] = lm;
                    System.out.println("updateLandmarks: observed new landmark " + id);
                }
            }
        }

        // Discard landmarks that have not been seen for a long time.
        for(int i = 0; i < maxLandmarks; i++) {
            if(landmarkDB[i] != null && !landmarkDB[i].seenLastRun &&
                    ms - landmarkDB[i].msLastSeen > msForget) {
                landmarkDB[i] = null;
                discardedIDs.add(i);
                System.out.println("updateLandmarks: forgot landmark " + i);
            }
        }

        // Warn if not called frequently enough.
        if(msLastUpdate > 0 && ms - msLastUpdate > msBetweenPolls)
            System.err.println("WARNING: updateLandmarks called too infrequently");
        msLastUpdate = ms;
    }

    /**
     * Gets laser readings from the LRFServer, performs some preprocessing on
     * them.
     * @return true on success
     */
    boolean updateLasers() {
        try {
            laser = (double[])call(lrfServer, "getLaserReadings");
            // Limit laser range.
            // TODO discuss if we want this on the real robot.
            for(int i = 0; i < robot.numLaser; i++) {
                if(laser[i] > robot.lrfRange)
                    laser[i] = robot.lrfRange;
            }
            return true;
        } catch(Exception e) {
            System.err.println("Error getting lasers:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extracts landmarks.
     * Uses Ransac to detect lines, then uses the robotPose to determine the
     * projection of the origin onto each line, and that is the non-moving point
     * that is taken as a landmark.
     */
    ArrayList<Seamark> extractLandmarks(Pose robotPose) {
        long ms                = System.currentTimeMillis();
        ArrayList<Seamark> lms = new ArrayList<Seamark>();
        Ransac ransac          = new Ransac();
        Line[] lines           = ransac.findLines(laser);
        for(Line line : lines) {
            Seamark lm     = new Seamark();
            lm.msLastSeen  = ms;
            lm.seenLastRun = true;
            lm.timesSeen   = -1;
            lm.line        = line;
            lm.point       = landmarkPoint(robotPose, line);
            lms.add(lm);
            System.out.format("extracted landmark: line a=%f b=%f c=%f, point x=%f y=%f\n",
                    line.a, line.b, line.c, lm.point.x, lm.point.y);
        }
        return lms;
    }

    /**
     * Computes a landmark point from a line, using the robotPose.
     * A landmark point is the projection of (0, 0) in the world coordinates
     * onto the given line.
     */
    // TODO There is probably a more efficient / elegant way of doing this...
    Point2D.Double landmarkPoint(Pose robotPose, Line line) {
        // Take two points on the line, w.r.t. robot origin.
        Point2D.Double ar = new Point2D.Double(),
                       br = new Point2D.Double();
        double slope = -line.a / line.b;
        if(slope >= -1 && slope <= 1) {
            // Line closer to horizontal.
            double m = -line.a / line.b,
                   b = -line.c / line.b;
            ar.x = 0;
            ar.y = m * ar.x + b;
            br.x = 1;
            br.y = m * br.x + b;
        } else {
            // Line closer to vertical.
            double rm = -line.b / line.a,
                   rb = -line.c / line.a;
            ar.y = 0;
            ar.x = rm * ar.y + rb;
            br.y = 1;
            br.x = rm * br.y + rb;
        }
        // Convert the points to world coordinates.
        Point2D.Double aw = robot.robot2world(robotPose, ar),
                       bw = robot.robot2world(robotPose, br);
        // Compute projection of (0, 0) onto line through aw and bw.
        double abx = bw.x - aw.x,
               aby = bw.y - aw.y,
               acx = 0 - aw.x,
               acy = 0 - aw.y;
        double ab2 = abx * abx + aby * aby;
        double r   = (abx * acx + aby * acy) / ab2;
        Point2D.Double result = new Point2D.Double();
        result.x = aw.x + r * (bw.x - aw.x);
        result.y = aw.y + r * (bw.y - aw.y);
        return result;
    }

    /**
     * Returns id of landmark closest to the given landmark.
     * If no landmark is within assocDist, returns -1.
     */
    int findNearestLandmark(Seamark lm) {
        int bestId = -1;
        double bestDist2 = assocDist * assocDist;
        for(int i = 0; i < maxLandmarks; i++) {
            if(landmarkDB[i] != null) {
                double dx = landmarkDB[i].point.x - lm.point.x,
                       dy = landmarkDB[i].point.y - lm.point.y,
                       dist2 = dx * dx + dy * dy;
                if(dist2 < bestDist2) {
                    bestId    = i;
                    bestDist2 = dist2;
                }
            }
        }
        return bestId;
    }
}
