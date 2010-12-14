// author: Constantin Berzan
// template by: Paul Schermerhorn

package com.slam;

import ade.*;
import java.awt.*;
import java.awt.geom.*;
import java.rmi.*;
import javax.swing.*;

/**
 * Visualization for MappingServer, displaying the map and currently detected
 * landmarks.
 */
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
        final int crossRadius = 10;
        g.setColor(Color.green);
        myDrawCross(g, new Point2D.Double(0, 0), crossRadius);

        // Draw line landmarks.
        if(data.landmarks != null) {
            for(Landmark lm : data.landmarks) {
                if(lm.ransacLine != null)
                    drawRansacLine(g, lm.ransacLine);
            }
        }

        // Draw point landmarks.
        if(data.landmarks != null) {
            for(Landmark lm : data.landmarks)
                drawLandmark(g, lm, crossRadius);
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
        Dimension msize     = new Dimension(data.map.length, data.map[0].length);
        double mPixPerSPixX = 1.0 * msize.width / ssize.width;
        double mPixPerSPixY = 1.0 * msize.height / ssize.height;
        Point onScreen      = new Point();
        onScreen.x          = (int)((onMap.x / mPixPerSPixX));
        onScreen.y          = ssize.height - (int)((onMap.y / mPixPerSPixY)) - 1;
        return onScreen;
    }

    /// Draws a crosshair representing a point in the world.
    void myDrawCross(Graphics g, Point2D.Double pW, int crosshairRadius) {
        Point pS = map2screen(data.world2map(pW));
        g.drawLine(pS.x - crosshairRadius, pS.y, pS.x + crosshairRadius, pS.y);
        g.drawLine(pS.x, pS.y - crosshairRadius, pS.x, pS.y + crosshairRadius);
    }

    /// Draws a landmark given in world coordinates.
    void drawLandmark(Graphics g, Landmark lm, int landmarkRadius) {
        g.setColor(Color.magenta);
        myDrawCross(g, lm.position, landmarkRadius);
        Point pS = map2screen(data.world2map(lm.position));
        g.drawRect(pS.x - landmarkRadius, pS.y - landmarkRadius,
                   2 * landmarkRadius, 2 * landmarkRadius);
        g.drawString(String.valueOf(lm.id), pS.x + landmarkRadius,
                                            pS.y - landmarkRadius);
    }

    /// Draws a line given in robot's coordinate system.
    private void drawRansacLine(Graphics g, Line l) {
        g.setColor(new Color(1.0f, 0.5f, 0.0f, 0.7f));
        // Larger than world boundaries:
        final double large = Math.max(robot.worldMax.x - robot.worldMin.x,
                                      robot.worldMax.y - robot.worldMin.y);
        // Points relative to the robot:
        Point2D.Double r1 = new Point2D.Double(),
                       r2 = new Point2D.Double();
        double slope = -l.a / l.b;
        if(slope >= -1 && slope <= 1) {
            // Line is closer to horizontal.
            double m = -l.a / l.b,
                   b = -l.c / l.b;
            r1.x = -large;
            r1.y = m * r1.x + b;
            r2.x = large;
            r2.y = m * r2.x + b;
        } else {
            // Line is closer to vertical.
            double rm = -l.b / l.a,
                   rb = -l.c / l.a;
            r1.y = -large;
            r1.x = rm * r1.y + rb;
            r2.y = large;
            r2.x = rm * r2.y + rb;
        }
        // Points in the world:
        Point2D.Double w1 = robot.robot2world(data.robotPose, r1),
                       w2 = robot.robot2world(data.robotPose, r2);
        // Points on the screen:
        Point s1 = map2screen(data.world2map(w1)),
              s2 = map2screen(data.world2map(w2));
        System.out.println("Point 1: robot " + r1 + " world " + w1 + " scren " + s1);
        System.out.println("Point 2: robot " + r2 + " world " + w2 + " scren " + s2);
        g.drawLine(s1.x, s1.y, s2.x, s2.y);
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredDim;
    }
}

