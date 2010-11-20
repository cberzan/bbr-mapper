package com.slam;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


/**
 * RANSAC line-detection algorithm.
 * All parameters are public fields.
 */
public class Ransac {
    /// Iterations to attmept to find lines.
    public int iterations = 1000;
    /// Size of initial sample.
    public int sampleSize = 10;
    /// Width of laser beam to consider for initial sample.
    public int sampleBeamWidth = 20;
    /// Max distance of a point that still counts as being on the line.
    public double maxBelongDist = 0.05;
    /// Min number of points on a line to count it as valid.
    public int minConsensus = 30;
    /// Number of laser readings.
    final public int numLaser = 181;

    /// Finds lines in the given laser readings, using RANSAC.
    public Line[] findLines(final double[] laser) {
        // Benchmark.
        long timer = System.currentTimeMillis();
        // Points considered for next matching.
        Point2D.Double[] points = laser2cartesian(laser);
        // Good lines saved.
        ArrayList<Line> lines = new ArrayList<Line>();

        int iter;
        for(iter = 0; iter < iterations; iter++) {
            // Quit early if out of points.
            if(points.length < minConsensus)
                break;
            // TODO: also break early if we run X times in a row unsuccessfully?

            // Get an initial sample.
            Point2D.Double[] sample = getInitialSample(points);

            // Compute best-fit line.
            Line bestFit = bestFitLine(sample);

            // See how many points fit this line well.
            ArrayList<Integer> consenting = new ArrayList<Integer>();
            for(int i = 0; i < points.length; i++) {
                if(bestFit.distance(points[i]) < maxBelongDist)
                    consenting.add(i);
            }

            // If there is consensus, compute new best-fit line of consenting
            // points, store it, and remove consenting points from further
            // consideration.
            if(consenting.size() >= minConsensus) {
                Point2D.Double[] selected = new Point2D.Double[consenting.size()];
                for(int i = 0; i < consenting.size(); i++)
                    selected[i] = points[consenting.get(i)];
                bestFit = bestFitLine(selected);
                lines.add(bestFit);
                points = removeIndices(points, consenting);
            }
            // Otherwise discard the line and try again.
        }

        timer = System.currentTimeMillis() - timer;
        System.out.format("RANSAC ran %d iterations in %.3f seconds, " +
                          "found %d lines, %d points left unmatched.\n",
                          iter + 1, timer / 1000.0, lines.size(), points.length);
        return lines.toArray(new Line[0]); // I can't believe Java requires the type like this.
    }

    /// Converts the laser readings to cartesian points.
    private Point2D.Double[] laser2cartesian(final double laser[]) {
        assert(laser.length == numLaser);
        Point2D.Double[] points = new Point2D.Double[numLaser];
        Vector2D tmp = new Vector2D();
        for(int i = 0; i < numLaser; i++) {
            tmp.setPol(laser[i], i * Math.PI / 180);
            points[i] = new Point2D.Double(tmp.getX(), tmp.getY());
        }
        return points;
    }

    /// Get an initial sample out of a set of points sorted by angle.
    private Point2D.Double[] getInitialSample(Point2D.Double[] points) {
        Random rand = new Random();
        assert(sampleBeamWidth <= points.length);
        int beamBegin = rand.nextInt(points.length - sampleBeamWidth + 1);
        ArrayList<Integer> indices = new ArrayList<Integer>(sampleBeamWidth);
        for(int i = 0; i < sampleBeamWidth; i++)
            indices.add(i, beamBegin + i);
        Collections.shuffle(indices);
        Point2D.Double[] sample = new Point2D.Double[sampleSize];
        for(int i = 0; i < sampleSize; i++)
            sample[i] = points[indices.get(i)];
        return sample;
    }

    /**
     * Returns copy of array, with specified indices removed.
     * Alters original array in-place.
     */
    private Point2D.Double[] removeIndices(Point2D.Double[] points, ArrayList<Integer> indices) {
        // Create new point list, removing matched points.
        for(int index : indices)
            points[index] = null;
        Point2D.Double[] newPoints = new Point2D.Double[points.length - indices.size()];
        int pos = 0;
        for(Point2D.Double p : points) {
            if(p != null) {
                assert(pos < newPoints.length);
                newPoints[pos] = p;
                pos++;
            }
        }
        return newPoints;
    }

    /**
     * Finds the least-squares best-fit line for a given set of points.
     * According to http://mathworld.wolfram.com/LeastSquaresFitting.html
     * (formulae 16 and on).
     */
    Line bestFitLine(Point2D.Double[] points) {
        int n = points.length;

        double ss_xx = 0, ss_yy = 0, ss_xy = 0,
               mean_x = 0, mean_y = 0;
        for(Point2D.Double p : points) {
            ss_xx += p.x * p.x;
            ss_yy += p.y * p.y;
            ss_xy += p.x * p.y;
            mean_x += p.x;
            mean_y += p.y;
        }
        mean_x /= n;
        mean_y /= n;
        ss_xx -= n * mean_x * mean_x;
        ss_yy -= n * mean_y * mean_y;
        ss_xy -= n * mean_x * mean_y;

        Line line = new Line();
        line.m = ss_xy / ss_xx;
        line.b = mean_y - line.m * mean_x;
        return line;
    }
};
