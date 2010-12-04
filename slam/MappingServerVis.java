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

    public MappingServerVis(ClientAndCallHelper helper) {
        super(helper);

        robot         = new RobotInfo();
        double wWorld = robot.worldMax.x - robot.worldMin.x,
               hWorld = robot.worldMax.y - robot.worldMin.y;
        double wRat   = robot.screenDim.width / wWorld,
               hRat   = robot.screenDim.height / hWorld,
               rat    = Math.min(wRat, hRat);
        preferredDim  = new Dimension((int)(wWorld * rat), (int)(hWorld * rat));
        System.out.println("MappingServerVis preferredDim: " + preferredDim);
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

        Dimension ssize = getSize();
        Dimension msize = new Dimension(data.map.length, data.map[0].length);
        double sPixPerMPixX = 1.0 * ssize.width / msize.width,
               sPixPerMPixY = 1.0 * ssize.height / msize.height;
        System.out.format("sPixPerMPixX=%f sPixPerMPixY=%f\n",
                sPixPerMPixX, sPixPerMPixY);

        // Fill buffer based on map.
        int[][] buffer = new int[ssize.width][ssize.height];
        int maxSum = 0;
        for(int sx = 0; sx < ssize.width; sx++) {
            for(int sy = 0; sy < ssize.height; sy++) {
                int mx = (int)(sx / sPixPerMPixX),
                    my = (int)(sy / sPixPerMPixY);
                for(int dx = 0; dx < sPixPerMPixX; dx++) {
                    if(mx + dx >= msize.width)
                        break;
                    for(int dy = 0; dy < sPixPerMPixY; dy++) {
                        if(my + dy >= msize.height)
                            break;
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
                    float component = (float)(1.0 - 1.0 * buffer[sx][sy] / maxSum);
                    g.setColor(new Color(component, component, component));
                    g.fillRect(sx, ssize.height - sy - 1, 1, 1);
                    // TODO is there really no setPixel?!
                }
            }
        } else {
            g.setColor(Color.white);
            g.fillRect(0, 0, ssize.width, ssize.height);
        }

        // Draw coordinate system origin.
        final int crosshairRadius = 10;
        Point2D.Double centerW    = new Point2D.Double(0, 0);
        Point   centerS           = map2screen(data.world2map(centerW));
        g.setColor(Color.green);
        g.drawLine(centerS.x - crosshairRadius, centerS.y,
                   centerS.x + crosshairRadius, centerS.y);
        g.drawLine(centerS.x, centerS.y - crosshairRadius,
                   centerS.x, centerS.y + crosshairRadius);

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
        Dimension msize     = new Dimension(data.map.length, data.map[0].length);
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

