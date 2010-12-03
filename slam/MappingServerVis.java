// author: Constantin Berzan
// template by: Paul Schermerhorn

package com.slam;

import ade.*;
import java.awt.*;
import java.awt.geom.*;
import java.rmi.*;
import javax.swing.*;

public class MappingServerVis extends ADEGuiPanel
{
    private RobotInfo robot           = null;
    private Dimension preferredDim    = null;
    private MappingServerVisData data = null;
    private int[][] buffer            = null;

    public MappingServerVis(ClientAndCallHelper helper) {
        super(helper);
        robot        = new RobotInfo();
        preferredDim = new Dimension(600, 600);
        /*
        if(robot == null) {
            System.err.println("getPreferredSize() called before RobotInfo was initialized");
            return new Dimension(600, 600);
        }
        double wWorld = robot.worldMax.x - robot.worldMin.x,
               hWorld = robot.worldMax.y - robot.worldMin.y;
        double wRat   = robot.screenDim.width / wWorld,
               hRat   = robot.screenDim.height / hWorld,
               rat    = Math.min(wRat, hRat);
        Dimension dim = new Dimension((int)(wWorld * rat), (int)(hWorld * rat));
        System.out.println("MappingServerVis dimension: " + dim);
        return dim;
        */
        buffer        = new int[preferredDim.width][preferredDim.height];
    }

    @Override
    public void refreshGui() {
        try {
            data = (MappingServerVisData)callServer("getVisData");
            repaint();
        } catch (Exception e) {
            System.err.println("refreshGui: failed to get VisData: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if(data == null)
            return;

        Dimension ssize = preferredDim;
        Dimension msize = new Dimension(data.map[0].length, data.map.length);
        double sPixPerMPixX = 1.0 * ssize.width / msize.width,
               sPixPerMPixY = 1.0 * ssize.height / msize.height;
        System.out.format("sPixPerMPixX=%f sPixPerMPixY=%f\n",
                sPixPerMPixX, sPixPerMPixY);

        // Zero buffer.
        for(int sx = 0; sx < ssize.width; sx++)
            for(int sy = 0; sy < ssize.height; sy++)
                buffer[sx][sy] = 0;

        // Fill buffer based on map.
        int maxSum = 0;
        for(int sx = 0; sx < ssize.width; sx++) {
            for(int sy = 0; sy < ssize.height; sy++) {
                int mx = (int)(sx / sPixPerMPixX),
                    my = (int)(sy / sPixPerMPixY);
                for(int dx = 0; dx < sPixPerMPixX; dx++) {
                    for(int dy = 0; dy < sPixPerMPixY; dy++) {
                        buffer[sx][sy] += data.map[mx + dx][my + dy];
                        if(buffer[sx][sy] > maxSum)
                            maxSum = buffer[sx][sy];
                    }
                }
            }
        }

        // Transfer buffer to screen.
        if(maxSum > 0) {
            for(int sx = 0; sx < ssize.width; sx++) {
                for(int sy = 0; sy < ssize.height; sy++) {
                    float component = (float)(1.0 - buffer[sx][sy] / maxSum);
                    g.setColor(new Color(component, component, component));
                    g.fillRect(sx, ssize.height - sy - 1, 1, 1);
                    // TODO is there really no setPixel?!
                }
            }
        }

        // Draw robot.
        final int robotRadius   = 10,
                  headingRadius = 15;
        Pose pose = data.robotPose;
        Point2D.Double robotCenter = new Point2D.Double(pose.x, pose.y);
        Point robotCenterS  = map2screen(data.world2map(robotCenter)),
              robotHeadingS = new Point(
                robotCenterS.x + (int)(headingRadius * Math.cos(pose.theta)),
                robotCenterS.y - (int)(headingRadius * Math.sin(pose.theta)));
        g.setColor(Color.red);
        g.drawOval(robotCenterS.x - robotRadius, robotCenterS.y - robotRadius,
                   robotRadius * 2, robotRadius * 2);
        g.drawLine(robotCenterS.x, robotCenterS.y,
                   robotHeadingS.x, robotHeadingS.y);
        //System.out.format("robotCenter: world %s map %s screen %s\n",
        //        robotCenter, data.world2map(robotCenter),
        //        map2screen(data.world2map(robotCenter)));
    }

    /// Converts from map coordinates to screen coordinates.
    Point map2screen(Point onMap) {
        Dimension ssize     = getSize();
        Dimension msize     = new Dimension(data.map[0].length, data.map.length);
        double mPixPerSPixX = 1.0 * msize.width / ssize.width;
        double mPixPerSPixY = 1.0 * msize.height / ssize.height;
        Point onScreen      = new Point();
        onScreen.x          = (int)((onMap.x / mPixPerSPixX));
        onScreen.y          = ssize.height - (int)((onMap.y / mPixPerSPixY)) - 1;
        return onScreen;
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredDim;
    }
}

