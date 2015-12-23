/*
 * HistogramPanel.java
 *
 * Created on March 16, 2006, 1:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.util;

import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EventFactor;
import java.awt.Color;
import java.util.List;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import ptolemy.plot.Plot;

/**
 *
 * @author Deb
 */
public class HistogramUtil
{
    /** Creates a new instance of HistogramPanel */
    public static void initHistogram(Plot histPlot, String title)
    {
        histPlot.setTitle(title);        
        histPlot.setYRange(0, 100);
        histPlot.setXRange(0, 100);
        histPlot.setXLabel("Event Type");
        histPlot.setYLabel("Freq");
        histPlot.setMarksStyle("none");
        histPlot.setBars(true);
        histPlot.setConnected(false,1);
    }
    
    public static void changeDataUsingJTable(Plot histPlot, List data, 
            EventFactor eventFactor, String title)
    {
        int col = 1;
        int maxfreq = 0;
        int count = 1;
        for (int row = 0; row < data.size(); row++)
        {
            int freq = ((Episode)data.get(row)).getVotes(0);
            if (maxfreq < freq) maxfreq = freq;
            count++;
        }
        if (maxfreq > 0)
        {
            histPlot.clear(1);
            histPlot.setTitle(title);        
            histPlot.setYRange(0, maxfreq);
            histPlot.setXRange(0, count);
            histPlot.setXLabel("Event Type");
            histPlot.setYLabel("Freq");
            histPlot.setMarksStyle("none");
            histPlot.setBars(true);
            histPlot.setConnected(false,1);
            Vector[] v = histPlot.getXTicks();
            if (v != null && v.length == 2)
            {
                v[0].clear();
                v[1].clear();
            }
        }
        
        count = 1;
        if (eventFactor.getSize() < 30)
        {
            for (int row = 0; row < data.size(); row++)
            {
                String label = ((Episode)data.get(row)).toString(eventFactor);
                histPlot.addXTick(label, count);
                count ++;
            }
        }
        count = 1;
        for (int row = 0; row < data.size(); row++)
        {
            int freq = ((Episode)data.get(row)).getVotes(0);
            histPlot.addPoint(1, count, freq, false);
            count ++;
        }
    }
    
    public static void drawLine(Plot plot, int y, int maxx)
    {
        plot.clear(0);
        plot.setConnected(true,0);
        plot.addPoint(0, 0, y, false);
        plot.addPoint(0, maxx + 1, y, true);
    }
}
