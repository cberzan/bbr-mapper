// COMP150 Behavior-Based Robotics
// (c) Matthias Scheutz
//
// Modifed by:
// Andy Sayler
// Constantin Berzan
// 
// 
// 

//package com.odomTest;

import com.action.ActionServerImpl;
import java.rmi.*;
import java.util.HashMap;
import java.util.Random;
import java.lang.Math;
import java.io.*;

public class ArchImpl extends ActionServerImpl implements Arch {
    //Constants
    private static final long serialVersionUID = 1L;
        
    /* 
     * <code>runArchitecture</code> is called periodically to perform
     * whatever sensing and acting is required by the architecture.
     */
    
    private boolean verbose = false;
    
    private int step = 0;
    
    private String logName = "odoLog.txt";
    private boolean append = true;
    
    private double stopDist = 2;
    private double tv = 0.1;
    private double rv = 0;
    private double[] basePose = null;
    FileWriter fstream = null;
    BufferedWriter out = null;

    Object videre = null;
    
    public void runArchitecture() {
        System.out.println();    
        System.out.println("step: " + step);    

        if(videre == null){
            try{
                videre = getClient("com.videre.VidereServer");
                System.out.println("Videre Object: " + videre);    
                basePose = (double[])call(videre,"getPoseEgo");

                // Create file.
                fstream = new FileWriter(logName, append);
                out = new BufferedWriter(fstream);

                // Log initial data.
                out.write("stopDist = " + stopDist + "\n");
                out.write("tv = " + tv + "\n");
                out.write("rv = " + rv + "\n");
                out.write("basePose[0] = " + basePose[0] + "\n");
                out.write("basePose[1] = " + basePose[1] + "\n");
                out.write("basePose[2] = " + basePose[2] + "\n");
                out.write("\n\n");

                System.out.print("stopDist = " + stopDist + "\n");
                System.out.print("tv = " + tv + "\n");
                System.out.print("rv = " + rv + "\n");
                System.out.print("basePose[0] = " + basePose[0] + "\n");
                System.out.print("basePose[1] = " + basePose[1] + "\n");
                System.out.print("basePose[2] = " + basePose[2] + "\n");
            }
            catch(Exception e){
                System.err.println("Error during first run: " + e.getMessage());
                return;
            }
        }
        
        //Get Pos
        double[] pose = null;
        try{
            pose = (double[])call(videre,"getPoseEgo");
            System.out.println("pose[0]: " + pose[0]);    
            System.out.println("pose[1]: " + pose[1]);    
            System.out.println("pose[2]: " + pose[2]);    
            pose[0] -= basePose[0];
            pose[1] -= basePose[1];
            pose[2] -= basePose[2];
            System.out.println("adjusted pose[0]: " + pose[0]);    
            System.out.println("adjusted pose[1]: " + pose[1]);    
            System.out.println("adjusted pose[2]: " + pose[2]);    
        }
        catch(Exception e){
            System.err.println("Error calling getPoseEgo: " + e.getMessage());
            System.err.println("Original cause: " + e.getCause());
            return;
        }

        double dist = Math.sqrt(pose[0]*pose[0] + pose[1]*pose[1]);
        System.out.println("dist: " + dist);

        //Log
        try{
            //Write Data
            out.write("step " + step + "\n");
            out.write("dist = " + dist + "\n");
            out.write("pose[0] = " + pose[0] + "\n");
            out.write("pose[1] = " + pose[1] + "\n");
            out.write("pose[2] = " + pose[2] + "\n");
            out.write("\n");
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        //Move
        if(dist >= stopDist){
            stop();
            try {
                out.flush();
                out.close();
            } catch(Exception e) {
                System.err.println("Error closing log file: " + e.getMessage());
            }
            System.exit(0);
        }
        else {
            setVels(tv, rv);
        }
        
        step = step + 1;
        return;
    }
    
    /** 
     * Constructs the architecture
     */
    public ArchImpl() throws RemoteException {
        super();
    }
}
