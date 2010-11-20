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
import java.util.Random;
import java.lang.Math;
import java.io.*;
import static utilities.Util.*;

public class ArchImpl extends ActionServerImpl implements Arch {
    private static final long serialVersionUID = 1L;

    /// Laser data updated at every tick.
    double[] laser = null;

    /// Server references.
    private Object ekfServer      = null;
    private Object landmarkServer = null;

    /// Data for wanderSchema (TODO: consider making nested class?)
    private Random rand          = null;
    private long lastTurnStarted = 0;
    private double lastDirection = 0;
    private double lastTurnTime  = 0;
    private double lastKeepTime  = 0;

    /**
     * A schema to avoid obstacles.
     */
    public Vector2D obstacleSchema() {
        // Parameters obtained through experimentation.
        final double magnitudeDivisor = 1000.0;

        // Repel from obstacles as 1/dist^4.
        Vector2D resultant = new Vector2D();
        for(int a = 0; a <= 180; a++) {
            Vector2D component = new Vector2D();
            double val = 1 / laser[a];
            component.setMag(val * val * val * val);
            component.setDir(Util.deg2rad((a + 180) % 360));
            resultant = resultant.plus(component);
        }
        //System.out.format("Obstacles: mag %.2f dir %.2fdeg\n",
        //        resultant.mag, rad2deg(resultant.dir));
        // Put magnitude in desired range.
        resultant.setMag(resultant.getMag() / magnitudeDivisor);
        //System.out.format("Obstacles: adjusted mag %.2f; (wall front %.2f)\n",
        //        resultant.mag, laser[90]);

        //assert(resultant.mag >= 0 && resultant.mag <= 1);
        return resultant;
    }

    /**
     * A schema to wander randomly.
     * Since we don't have a compass, the way this works is:
     * - every once in a while, demand a turn
     * - the rest of the time, move forward
     */
    public Vector2D wanderSchema() {
        final long turnTimeBound = 5 * 1000;  // ms
        final long keepTimeBound = 20 * 1000; // ms
        long elapsed = System.currentTimeMillis() - lastTurnStarted;
        Vector2D result = new Vector2D();
        if(elapsed < lastTurnTime) {
            // Still turning.
            result.setPol(1.0, lastDirection);
        } else if(elapsed < lastTurnTime + lastKeepTime) {
            // Moving forward after turn.
            result.setPol(1.0, Util.deg2rad(90));
        } else {
            // Demand new turn.
            lastTurnStarted = System.currentTimeMillis();
            lastTurnTime    = rand.nextDouble() * turnTimeBound;
            lastKeepTime    = rand.nextDouble() * keepTimeBound;
            lastDirection   = rand.nextDouble() * 2 * Math.PI;
            result.setPol(1.0, lastDirection);
            System.out.format("wanderSchema: new turn ########\n");
        }
        return result;
    }

    /**
     * Returns overall desired motion vector.
     * The magnitude of the returned vector is no more than 1.
     */
    public Vector2D getSchemaSum() {
        // Schema gains.
        final double obstacleGain = 1.0,
                     wanderGain   = 1.0;

        // Weighted sum of motion vectors from each schema.
        Vector2D sum = new Vector2D();

        // Schema to avoid obstacles.
        Vector2D compObst = obstacleSchema();
        System.out.format("*** Obstacles: mag % 2.4f dir % 3.4f\n",
                compObst.getMag(), Util.rad2deg(compObst.getDir()));
        sum = sum.plus(compObst.mul(obstacleGain));

        // Schema to wander.
        Vector2D compWander = wanderSchema();
        System.out.format("*** Wander   : mag % 2.4f dir % 3.4f\n",
                compWander.getMag(), Util.rad2deg(compWander.getDir()));
        sum = sum.plus(compWander.mul(wanderGain));

        // Sum vectors, and clip magnitude.
        System.out.format("*** Overall  : mag % 2.4f dir % 3.4f deg\n",
                sum.getMag(), Util.rad2deg(sum.getDir()));
        if(sum.getMag() > 1) {
            //System.out.println("***** clipping!");
            sum.setMag(1);
        }
        System.out.format("*** Adjusted : mag % 2.4f dir % 3.4f deg\n",
                sum.getMag(), Util.rad2deg(sum.getDir()));

        return sum;
    }

    /**
     * Send the appropriate motor commands for a given desired motion vector.
     */
    public void doMotion(Vector2D sum) {
        // Velocity bounds:
        final double tv_max = 1.0, // m/s
                     rv_max = 1.0; // rad/s

        // Convert vector into translational and rotational velocity.
        // To get tv, we take the resultant vector magnitude, and multiply it by
        // a polynomial adjustment factor.
        double trigDir = sum.getDir();
        if(trigDir > Math.PI)
            trigDir -= 2 * Math.PI;
        double tv_factor;
        if(trigDir < 0) {
            // Need to move backwards. Slowly move backwards, focus on turning.
            tv_factor = -0.1;
        } else if(trigDir < Math.PI/2) {
            // Need to turn right. Move faster if close to correct direction.
            double tmp = trigDir / (Math.PI/2);
            tv_factor = tmp * tmp * tmp * tmp;
        } else {
            // Need to turn left. Move faster if close to correct direction.
            double tmp = (Math.PI - trigDir) / (Math.PI/2);
            tv_factor = tmp * tmp * tmp * tmp;
        }
        double tv = sum.getMag() * tv_factor;
        // To get rv, we look at the resultant vector direction.
        double rv_factor;
        if(trigDir < -Math.PI/2) {
            // Need to turn left, a lot.
            rv_factor = 1;
        } else if(trigDir < 0) {
            // Need to turn right, a lot.
            rv_factor = -1;
        } else {
            // Use sigmoid to turn more if we need more correction.
            double tmp = Math.exp(2 * (Math.PI/2 - trigDir));
            rv_factor = 2 / (1 + tmp) - 1;
        }
        if(sum.getMag() < 1e-7) // no motion
            rv_factor = 0;
        double rv = 1 * rv_factor;

        if(tv > 1)
            tv = 1;
        else if(tv < -1)
            tv = -1;
        assert(rv >= -1 && rv <= 1);
        tv *= tv_max;
        rv *= rv_max;

        System.out.format("Setting tv=%.4f rv=%.4f\n\n", tv, rv);
        setVels(tv, rv);
    }

    /**
     * Returns true if all required servers are ready for operation.
     * At startup, tries to get references to all servers.
     */
    public boolean allServersReady() {
        try {
            Vector2D.test();
        } catch(Exception e) {
            System.err.println("Vector2D tests failed.");
            e.printStackTrace();
            return false;
        }

        if(rand == null) {
            rand = new Random();
        }

        if(ekfServer == null) {
            ekfServer = getClient("com.slam.EKFServer");
            if(ekfServer == null) {
                System.out.println("allServersReady: waiting for EKFServer.");
                return false;
            }
        }

        if(landmarkServer == null) {
            landmarkServer = getClient("com.slam.LandmarkServer");
            if(landmarkServer == null) {
                System.out.println("allServersReady: waiting for LandmarkServer.");
                return false;
            }
        }

        return true;
    }

    public void runArchitecture() {
        if(!allServersReady())
            return;

        // Update senses.
        laser = getLaserReadings();
        //for(int i = 0; i <= 180; i++)
        //    System.out.format("%f, ", laser[i]);
        //System.out.println();

        // Test sim landmarks.
        /*
        try {
            Landmark[] landmarks = (Landmark[])call(landmarkServer, "getLandmarks",
                FIXME need to pass pose);
            System.out.format("Got %d landmarks:\n", landmarks.length);
            for(Landmark l : landmarks)
                System.out.format("id=%d mag=%f dir=%f\n",
                        l.id, l.position.getMag(), l.position.getDir());
        } catch(Exception e) {
            System.out.println("FAILED to get landmarks: " + e);
            e.printStackTrace();
        }
        */

        // Perform desired motion.
        doMotion(getSchemaSum());

        /*
        try {
            Pose pose = (Pose)call(ekfServer, "getPose");
            System.out.println("Pose: " + pose);
        } catch(Exception e) {
            System.err.println("Error getting pose from EKFServer: " + e);
            System.err.println("Original cause: " + e.getCause());
            e.printStackTrace();
            System.exit(1);
        }
        */
    }

    /** 
     * Constructs the architecture
     */
    public ArchImpl() throws RemoteException {
        super();
    }
}
