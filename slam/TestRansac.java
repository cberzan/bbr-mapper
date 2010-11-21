package com.slam;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.*;

public class TestRansac extends JPanel {
    final int numLaser = 181;
    double[] laser     = null;
    Ransac ransac      = null;
    Line[] result      = null;
    int displayedIter  = 0;

    // Stuff filled-in by Ransac.
    protected ArrayList<Point2D.Double[]> points;
    protected ArrayList<Integer> beamBegin;
    protected ArrayList<Point2D.Double[]> sample;
    protected ArrayList<Line> sampleFitLine;
    protected ArrayList<ArrayList<Integer>> consenting;
    protected ArrayList<Line> consentingFitLine;
    protected int totalIter;

    public TestRansac() {
        super();
        setPreferredSize(new Dimension(800,600));

        double[] hereLaser = {
            0.705001, 0.705084, 0.705382, 0.705895, 0.706624, 0.707571,
            0.708737, 0.710123, 0.711732, 0.713566, 0.715629, 0.717923,
            0.720452, 0.723220, 0.726232, 0.729493, 0.733008, 0.736783,
            0.740824, 0.745138, 0.749733, 0.754617, 0.759799, 0.765287,
            0.771092, 0.777225, 0.783698, 0.790523, 0.797713, 0.805284,
            0.813251, 0.821631, 0.830441, 0.839702, 0.849434, 0.859660,
            0.870405, 0.881694, 0.893557, 0.906025, 0.919130, 0.932910,
            0.947403, 0.962652, 0.978705, 0.995612, 1.013428, 1.032216,
            1.052041, 1.072977, 1.079572, 1.102650, 1.127089, 1.235946,
            1.265420, 1.296741, 1.330069, 1.365585, 1.403491, 1.444016,
            1.487420, 1.533998, 1.584089, 1.638080, 1.696419, 1.759625,
            1.828306, 1.825693, 1.851840, 1.963579, 2.057409, 2.161351,
            2.244790, 2.543253, 2.697631, 2.872899, 3.009756, 3.127908,
            3.384229, 3.687541, 4.051937, 4.497777, 5.055602, 5.773401,
            7.113069, 7.113069, 7.113069, 7.113069, 7.113069, 7.113069,
            7.113069, 7.113069, 7.113069, 7.113069, 7.113069, 7.113069,
            7.149321, 5.773401, 5.055602, 4.497777, 4.051937, 3.687541,
            3.384229, 3.127908, 3.009756, 3.023427, 3.038156, 4.753521,
            2.244790, 2.161351, 2.057409, 1.963579, 1.851840, 1.825693,
            1.834484, 1.824343, 1.814866, 1.806036, 1.797839, 1.790261,
            1.783289, 1.776913, 1.771121, 1.765906, 1.761257, 1.757169,
            1.753635, 1.750649, 1.127089, 1.102650, 1.079572, 1.072977,
            1.052041, 1.032216, 1.013428, 0.995612, 0.978705, 0.962652,
            0.947403, 0.932910, 0.919130, 0.906025, 0.893557, 0.881694,
            0.870405, 0.859660, 0.849434, 0.839702, 0.830441, 0.821631,
            0.813251, 0.805284, 0.797713, 0.790523, 0.783698, 0.777225,
            0.771092, 0.765287, 0.759799, 0.754617, 0.749733, 0.745138,
            0.740824, 0.736783, 0.733008, 0.729493, 0.726232, 0.723220,
            0.720452, 0.717923, 0.715629, 0.713566, 0.711732, 0.710123,
            0.708737, 0.707571, 0.706624, 0.705895, 0.705382, 0.705084,
            0.705001
        };
        laser = hereLaser;

        points            = new ArrayList<Point2D.Double[]>();
        beamBegin         = new ArrayList<Integer>();
        sample            = new ArrayList<Point2D.Double[]>();
        sampleFitLine     = new ArrayList<Line>();
        consenting        = new ArrayList<ArrayList<Integer>>();
        consentingFitLine = new ArrayList<Line>();

        // Run RANSAC, saving intermediary steps for visualization.
        ransac = new Ransac(this);
        result = ransac.findLines(laser);

        assert(points.size()            == totalIter);
        assert(beamBegin.size()         == totalIter);
        assert(sample.size()            == totalIter);
        assert(sampleFitLine.size()     == totalIter);
        assert(consenting.size()        == totalIter);
        assert(consentingFitLine.size() == totalIter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        final double scale = 50;
        int height = getHeight(), width = getWidth();
        int xcenter = width / 2, ycenter = 10;

        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);

        // Draw laser beams.
        g.setColor(Color.green);
        for(int i = 0; i < numLaser; i++) {
            Vector2D v = new Vector2D();
            v.setPol(laser[i], i * Math.PI / 180);
            double x = v.getX() * scale,
                   y = v.getY() * scale;
            x += xcenter;
            y += ycenter;
            g.drawLine(xcenter, ycenter, (int)x, (int)y);
        }

        // Draw iteration number.
        g.setColor(Color.black);
        assert(displayedIter >= 0 && displayedIter < totalIter);
        g.drawString("Iteration: " + displayedIter + " (n and p to change)",
                     0, height / 2 - 20);

        // Draw points being considered in current iteration.
        g.setColor(Color.blue);
        for(Point2D.Double p : points.get(displayedIter)) {
            double x = p.x * scale + xcenter,
                   y = p.y * scale + ycenter;
            g.drawOval((int)x - 2, (int)y - 2, 4, 4);
        }

        // Draw beginning of random beam.
        /*
        g.setColor(Color.cyan);
        {
            Point2D.Double[] iterPoints = points.get(displayedIter);
            int index = beamBegin.get(displayedIter);
            Point2D.Double p = iterPoints[index];
            double x = p.x * scale + xcenter,
                   y = p.y * scale + ycenter;
            g.drawOval((int)x - 3, (int)y - 3, 6, 6);
        }
        */

        // Draw sample.
        g.setColor(Color.cyan);
        for(Point2D.Double p : sample.get(displayedIter)) {
            double x = p.x * scale + xcenter,
                   y = p.y * scale + ycenter;
            g.fillOval((int)x - 2, (int)y - 2, 4, 4);
        }

        // Draw best-fit line through sample.
        g.setColor(Color.red);
        {
            Line l = sampleFitLine.get(displayedIter);
            // In the robot's coordinate system:
            double x1 = -xcenter / scale,
                   y1 = l.m * x1 + l.b,
                   x2 = xcenter / scale,
                   y2 = l.m * x2 + l.b;
            // In the canvas coordinate system:
            double x1s = x1 * scale + xcenter,
                   y1s = y1 * scale + ycenter,
                   x2s = x2 * scale + xcenter,
                   y2s = y2 * scale + ycenter;
            g.drawLine((int)x1s, (int)y1s, (int)x2s, (int)y2s);
        }

        // Draw consenting points.
        g.setColor(Color.magenta);
        for(int index : consenting.get(displayedIter)) {
            Point2D.Double p = points.get(displayedIter)[index];
            double x = p.x * scale + xcenter,
                   y = p.y * scale + ycenter;
            g.drawOval((int)x - 2, (int)y - 2, 4, 4);
        }

        // Show square error of best-fit line through sample.
        g.setColor(Color.black);
        double errSample = calcSquareDist(sampleFitLine.get(displayedIter),
                                          points.get(displayedIter),
                                          consenting.get(displayedIter));
        errSample /= sample.get(displayedIter).length;
        g.drawString("Sample error:       " + errSample, 0, height / 2);

        // Draw best-fit line through consenting points.
        g.setColor(Color.black);
        if(consentingFitLine.get(displayedIter) != null) {
            Line l = consentingFitLine.get(displayedIter);
            // In the robot's coordinate system:
            double x1 = -xcenter / scale,
                   y1 = l.m * x1 + l.b,
                   x2 = xcenter / scale,
                   y2 = l.m * x2 + l.b;
            // In the canvas coordinate system:
            double x1s = x1 * scale + xcenter,
                   y1s = y1 * scale + ycenter,
                   x2s = x2 * scale + xcenter,
                   y2s = y2 * scale + ycenter;
            g.drawLine((int)x1s, (int)y1s, (int)x2s, (int)y2s);
        }

        // Show square error of best-fit line through sample.
        g.setColor(Color.black);
        if(consentingFitLine.get(displayedIter) != null) {
            double errCons = calcSquareDist(consentingFitLine.get(displayedIter),
                                            points.get(displayedIter),
                                            consenting.get(displayedIter));
            errCons /= consenting.get(displayedIter).size();
            g.drawString("Consensus error: " + errCons, 0, height / 2 + 20);
        } else {
            g.drawString("No consensus", 0, height / 2 + 20);
        }
    }

    private static double calcSquareDist(Line line, Point2D.Double[] points,
                                         ArrayList<Integer> indices) {
        double sum = 0;
        for(int index : indices) {
            double dist = line.distance(points[index]);
            sum += dist * dist;
        }
        return sum;
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame();
        TestRansac panel = new TestRansac();
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        // from: http://download.oracle.com/javase/tutorial/uiswing/examples/components/index.html#FrameDemo
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

    }
};
