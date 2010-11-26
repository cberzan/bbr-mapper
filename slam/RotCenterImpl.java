// author: Constantin Berzan
// template by: Matthias Scheutz

package com.slam;

import com.action.ActionServerImpl;
import java.rmi.*;
import java.lang.Math;
import java.io.*;
import static utilities.Util.*;

public class RotCenterImpl extends ActionServerImpl implements RotCenter {
    private static final long serialVersionUID = 1L;

    /// Laser data updated at every tick.
    double[] laser = null;

    /// CSV file to dump landmark positions into.
    final int maxLandmarks = 10;
    final String csvPath   = "landmarks.csv";
    FileWriter csvFile    = null;

    /// Server references.
    private Object positionServer = null; // sim testing only
    private Object landmarkServer = null;

    /**
     * Returns true if all required servers are ready for operation.
     * At startup, tries to get references to all servers.
     */
    public boolean allServersReady() {
        if(csvFile == null) {
            try {
                csvFile = new FileWriter(csvPath, /*append = */ false);
                csvFile.write("theta");
                for(int i = 0; i < maxLandmarks; i++)
                    csvFile.write(",lm" + i + "x,lm" + i + "y");
                csvFile.write("\n");
            } catch(Exception e) {
                System.out.println("allServersReady: failed to open csv file.");
                csvFile = null;
                return false;
            }
        }

        if(positionServer == null) {
            positionServer = getClient("com.interfaces.PositionServer");
            if(positionServer == null) {
                System.out.println("allServersReady: waiting for PositionServer.");
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

        // Turn in place, recording history of theta and detected landmark positions.
        try {
            // We pass in the true global position -- this is impossible in the
            // real world.
            double[] poseADE = (double[])call(positionServer, "getPoseGlobal");
            Pose pose        = new Pose();
            pose.x           = poseADE[0];
            pose.y           = poseADE[1];
            pose.theta       = poseADE[2];
            //System.out.format("Pose: x=%f y=%f theta=%f\n", pose.x, pose.y, pose.theta);

            Landmark[] landmarks = (Landmark[])call(landmarkServer, "getLandmarks", pose);
            Landmark[] allKnown  = new Landmark[maxLandmarks];
            System.out.format("Got %d landmarks:\n", landmarks.length);
            for(Landmark l : landmarks) {
                System.out.format("id=%d x=%f y=%f\n",
                        l.id, l.position.x, l.position.y);
                if(l.id < maxLandmarks) {
                    assert(allKnown[l.id] == null);
                    allKnown[l.id] = l;
                } else {
                    System.err.println("ERROR: landmark IDs running too high!");
                }
            }

            csvFile.write(String.valueOf(pose.theta));
            for(int i = 0; i < maxLandmarks; i++) {
                if(allKnown[i] == null)
                    csvFile.write(",,");
                else
                    csvFile.write("," + allKnown[i].position.x + "," + allKnown[i].position.y);
            }
            csvFile.write("\n");
        } catch(Exception e) {
            System.out.println("FAILED to get landmarks: " + e);
            e.printStackTrace();
        }

        setVels(0, 0.1);
    }

    /** 
     * Constructs the architecture
     */
    public RotCenterImpl() throws RemoteException {
        super();
    }
}
