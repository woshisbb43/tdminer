/*
 * PoissonMatrixPlot.java
 *
 * Created on February 11, 2007, 6:56 PM
 *
 */

package edu.iisc.tdminer.gui.simulator;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.simulation.PoissonSimulator;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import java.util.Vector;
import ptolemy.plot.Plot;

/**
 *
 * @author debprakash
 */
public class PoissonMatrixPlot extends Plot
{
    private Color[] carray = null;
    
    private int range = 20;
    private ISimulatorPanel container;
    
    /** Creates a new instance of PoissonMatrixPlot */
    public PoissonMatrixPlot()
    {
        _marks = 2;
        _padding = 0.0;
        _grid = false;
        setColors();
        removeListeners();
    }
    
    public void setColors()
    {
        Color[] colors = new Color[range];
        if (this.container instanceof PoissonSimulatorComponent)
        {
            PoissonSimulatorComponent simpan = (PoissonSimulatorComponent)this.container;
            int changeIndex = (int)(simpan.getBaseConnStrength() * range);
            // range = 10, changeIndex = 6
            int k = 0;
            int dec = (int)(255/(double)(range - changeIndex));
            int c = 255;
            for(int i = range - 1; i > changeIndex; i--)
            {
                colors[k++] = new Color(0, c, 0);
                c -= dec;
            }
            c = 0;
            int inc = (int)(255/(double)(changeIndex));
            for(int i = changeIndex; i >= 0; i--)
            {
                colors[k++] = new Color(c, 0, 0);
                c += inc;
            }
        }
        else
        {
            int k = 0;//0 - stands for p = 1.0 and 19 stands for p = 0.0
            double d = 255.0 / (double)range;
            for(int i = range - 1; i >= 0; i--)
                colors[k++] = new Color(0, (int)(d * (i + 1)), 0);
        }
        this.carray = colors;
    }
    
    private void removeListeners()
    {
        for(KeyListener k : getKeyListeners())
        {
            this.removeKeyListener(k);
        }
        for(MouseListener ml : getMouseListeners())
        {
            this.removeMouseListener(ml);
        }
        for(MouseMotionListener mml : getMouseMotionListeners())
        {
            this.removeMouseMotionListener(mml);
        }
    }
    
    @Override
    protected void _drawPoint(Graphics graphics, int dataset, long xpos,
	    long ypos, boolean clip)
    {
        boolean mark = false;
        int width = _lrx - _ulx;
        _xscale = width / (_xMax - _xMin);
        
        int height = _lry - _uly;
        _yscale = height / (_yMax - _yMin);

        int _width_y = (int)_yscale + 1;
	int _width_x = (int)_xscale + 1;
        //System.out.println("_width_y = " + _width_y + " _width_x = " + _width_x);
	// If the point is not out of range, draw it.
	boolean pointinside = (ypos <= _lry) && (ypos >= _uly)
	&& (xpos <= _lrx) && (xpos >= _ulx);
	
	if (!clip || pointinside)
	{
	    int xposi = (int) xpos;
	    int yposi = (int) ypos;
	    
	    // filled square
            Color c = graphics.getColor();
            if (dataset >= range) 
            {
                dataset -= range;
                mark = true;
            }
            if (mark)
            {
                graphics.setColor(this.carray[dataset]);
                graphics.fillRect(xposi, yposi - _width_y + 1,
                        _width_x, _width_y);
//                graphics.setColor(Color.YELLOW);
//                graphics.fillRect(xposi + 1, yposi - _width_y + 2,
//                        3, 3);
//                graphics.drawLine(xposi + 1, yposi - _width_y + 2, xposi + 1 + _width_x/2, yposi - _width_y + 2);
//                graphics.drawLine(xposi + 1, yposi - _width_y + 2, xposi + 1, yposi - _width_y + 2 + _width_y/2);
            }
            else
            {
                graphics.setColor(this.carray[dataset]);
                graphics.fillRect(xposi, yposi - _width_y + 1,
                        _width_x, _width_y);
            }
            graphics.setColor(c);
	}
    }
    
    public int locateColumn(int ypos)
    {
        int y = (int)(_yMin + (_lry - ypos)/_yscale);
        return y;
    }
    
    public int locateRow(int xpos)
    {
        int x = (int)(_xMin + (xpos - _ulx)/_xscale);
        return x;
    }

    @Override
    public void repaint()
    {
        clear(true);
        setColors();
        if (container != null)
        {
            _marks = 2;
            _padding = 0.0;
            _grid = false;
            PoissonSimulator sim = (PoissonSimulator)container.getSimulator();
            EventFactor eventTypes = sim.getEventFactor();
            setXRange(0.0, eventTypes.getSize());
            setYRange(0.0, eventTypes.getSize());
            clearLegends();
            int k = 0;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);
            
            for(int i = 0; i < eventTypes.getSize(); i++)
            {
                addXTick(eventTypes.getName(i), i + 0.5);
                //addYTick(eventTypes.getName(i), i + 0.5);
            }
            Vector[] vlist = getYTicks();
            if (vlist != null)
            {
                for(Vector v : vlist) v.clear();
            }

            int ordinalStep = eventTypes.getSize() / 26;
            if (ordinalStep == 0) ordinalStep = 1;
            for(int ix = 0; ix < eventTypes.getSize(); ix += ordinalStep)
            {
                 String label = eventTypes.getName(ix);
                 double coordinate = ix + 0.5;
                 addYTick(label, coordinate);
            }
            
            
            for(int i = range - 1; i >= 0; i--)
                addLegend(k++, "" + nf.format((double)i/(double)range) + "-" + nf.format((double)(i+1)/(double)range) + "" );
            
            for(int i = 0; i < eventTypes.getSize(); i++)
            {
                for(int j = 0; j < eventTypes.getSize(); j++)
                {
                    int c = (int)(sim.getProbability(i,j) * (double)range);
                    if (c < 0) c = 0;
                    if (c >= range) c = range - 1;
                    //System.out.println("c = " + c);
                    
                    if (sim instanceof PoissonSimulator)
                    {
                        PoissonSimulator psim = (PoissonSimulator)sim;
                        if (psim.getDelay(i,j) > 0)
                        {
                            int dataset = range + (range - c - 1);
                            addPoint(dataset, i, j, false);
                        }
                        else
                        {
                            addPoint(range - c - 1, i, j, false);
                        }
                    }
                    else
                    {
                        addPoint(range - c - 1 , i, j, false);
                    }
                }
            }
        }        
        super.repaint();
    }

    public void setContainer(ISimulatorPanel container)
    {
        this.container = container;
    }
}
