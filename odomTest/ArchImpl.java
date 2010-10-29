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
    
    private double stopDist = 1;
    private double tv = 0.1;
    private double rv = 0;

    Object videre = null;
    
    public void runArchitecture() {
	
	System.out.println("");	
	System.out.println("step: " + step);	

	if(videre == null){
	    try{
		videre = getClient("com.videre.VidereServer");
		System.out.println("Videre Object: " + videre);	
	    }
	    catch(Exception e){
		System.err.println("Error calling getClient: " + e.getMessage());
		return;
	    }
	}
	
	//Get Pos
	double[] pose = null;
	try{
	    pose = (double[])call(videre,"getPoseEgo");
	}
	catch(Exception e){
	    System.err.println("Error calling getPoseEgo: " + e.getMessage());
	    System.err.println("Original cause: " + e.getCause());
	    return;
	}

	//Print
	System.out.println("pose[0]: " + pose[0]);	
	System.out.println("pose[1]: " + pose[1]);	
	System.out.println("pose[2]: " + pose[2]);	
	
	//Move
	double dist = Math.sqrt(pose[0]*pose[0] + pose[1]*pose[1]);
	System.out.println("dist: " + pose[2]);	
	if(dist >= stopDist){
	    stop();
	}
	else {
	    setVels(tv, rv);
	}
	
	//Log
	try{
	    //Open/Create file 
	    FileWriter fstream = new FileWriter(logName, append);
	    BufferedWriter out = new BufferedWriter(fstream);
	    //Write Data
	    out.write("step " + step + "\n");
	    out.write("dist = " + dist + "\n");
	    out.write("pose[0] = " + pose[0] + "\n");
	    out.write("pose[1] = " + pose[1] + "\n");
	    out.write("pose[2] = " + pose[2] + "\n");
	    out.write("\n");
	    //Close the output stream
	    out.close();
	}catch (Exception e){//Catch exception if any
	    System.err.println("Error: " + e.getMessage());
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
