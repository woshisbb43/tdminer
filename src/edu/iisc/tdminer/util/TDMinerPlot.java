/*
 * TDMinerPlot.java
 *
 * Created on March 28, 2006, 10:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import ptolemy.plot.Plot;

/**
 *
 * @author patnaik
 */
public class TDMinerPlot extends Plot
{
    
    /** Creates a new instance of TDMinerPlot */
    public TDMinerPlot()
    {
	super();
        setGrid(false);
    }
    
    /** Put a mark corresponding to the specified dataset at the
     *  specified x and y position. The mark is drawn in the current
     *  color. What kind of mark is drawn depends on the _marks
     *  variable and the dataset argument. If the fourth argument is
     *  true, then check the range and plot only points that
     *  are in range.
     *  This method should be called only from the event dispatch thread.
     *  It is not synchronized, so its caller should be.
     *  @param graphics The graphics context.
     *  @param dataset The index of the dataset.
     *  @param xpos The x position.
     *  @param ypos The y position.
     *  @param clip If true, then do not draw outside the range.
     */
    protected void _drawPoint(Graphics graphics, int dataset, long xpos,
	    long ypos, boolean clip)
    {
	int _radius = 5;
	int _diameter = 2 * _radius;
	// If the point is not out of range, draw it.
	boolean pointinside = (ypos <= _lry) && (ypos >= _uly)
                && (xpos <= _lrx) && (xpos >= _ulx);
	
	if (!clip || pointinside)
	{
	    int xposi = (int) xpos;
	    int yposi = (int) ypos;
	    
	    // square
//	    graphics.drawRect(xposi - _radius, yposi - _radius,
//		    _diameter, _diameter);
	    
	    // filled square
//	    graphics.fillRect(xposi - _radius, yposi - _radius,
//		    _diameter, _diameter);

            // spike
            Color current = graphics.getColor();
            graphics.setColor(Color.BLACK);
            Graphics2D g2D = (Graphics2D)graphics;
            Stroke stroke = (g2D).getStroke();
            g2D.setStroke(new BasicStroke(2.0f));
            g2D.drawLine(xposi, yposi - _diameter, xposi, yposi);
            //g2D.drawLine(xposi+1, yposi - _diameter, xposi+1, yposi);
            g2D.setStroke(stroke);
            graphics.setColor(current);
	}
    }  
}