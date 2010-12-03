// author: Constantin Berzan
// template by: Paul Schermerhorn

package com.slam;

import ade.*;
import java.awt.*;
import java.rmi.*;
import javax.swing.*;

public class MappingServerVis extends ADEGuiPanel
{
    private MappingServerVisData data = null;

    public MappingServerVis(ClientAndCallHelper helper) {
        super(helper);
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

        // Update every pixel based on map data.
        Dimension ssize = getSize();
        Dimension msize = new Dimension(data.map[0].length, data.map.length);
        double mPixPerSPixX = 1.0 * msize.width / ssize.width;
        double mPixPerSPixY = 1.0 * msize.height / ssize.height;
        int maxSum = (int)(mPixPerSPixX * mPixPerSPixY * 127);
        for(int sx = 0; sx < ssize.width; sx++) {
            for(int sy = 0; sy < ssize.height; sy++) {
                // More than one map pixel corresponds to a screen pixel.
                int sum = 0;
                for(int mx = 0; mx < mPixPerSPixX; mx++) {
                    for(int my = 0; my < mPixPerSPixY; my++) {
                        int mxx = (int)(sx * mPixPerSPixX + mx),
                            myy = (int)(sy * mPixPerSPixY + my);
                        sum += data.map[mxx][myy];
                    }
                }
                float component = (float)(1.0 - sum / maxSum);
                g.setColor(new Color(component, component, component));
                g.fillRect(sx, sy, 1, 1); // FIXME really? no putPixel??
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }
}

