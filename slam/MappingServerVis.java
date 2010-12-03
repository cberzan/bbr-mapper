// author: Constantin Berzan
// template by: Paul Schermerhorn

package com.slam;

import ade.*;
import java.awt.*;
import java.rmi.*;
import javax.swing.*;

public class MappingServerVis extends ADEGuiPanel
{
    public MappingServerVis(ClientAndCallHelper helper) {
        super(helper);
    }

    @Override
    public void refreshGui() {
        try {
            MappingServerVisData data = (MappingServerVisData)callServer("getVisData");
            System.out.println("Got VisData");
        } catch (Exception e) {
            System.err.println("refreshGui: failed to get VisData: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }
}

