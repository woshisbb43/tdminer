/*
 * Crosscorrelogram.java
 *
 * Created on June 16, 2006, 12:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import java.text.NumberFormat;

/**
 *
 * @author patnaik
 */
public class Crosscorrelogram
{
    private static Crosscorrelogram instance = new Crosscorrelogram();
    /** Creates a new instance of Crosscorrelogram */
    private Crosscorrelogram()
    {
    }
    
    public static Crosscorrelogram instance()
    {
	return instance;
    }
    
    public String[] analyze(IEventDataStream sequence, int bins, 
            double binwidth, String ref, String target)
    {
	if (bins % 2 == 0) bins ++;
	String[] ret = new String[2];
	// ret[0]   x axis values
	// ret[1]   y axis values
	// bins must be odd
	
	int iRef = sequence.getEventFactor().getId(ref);
	int iTarget = sequence.getEventFactor().getId(target);
        int[] hist = computeHist(binwidth, iTarget, sequence, iRef, bins);
	StringBuffer x = new StringBuffer("[");
	StringBuffer y = new StringBuffer("[");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(6);
	int k = 0;
	for (int j = -bins/2; j <= bins/2; j++)
	{
	    x.append(nf.format(binwidth * j) + " ");
	    y.append(hist[k] + " ");
	    k++;
	}
	x.append("];");
	y.append("];");
	
	ret[0] = x.toString();
	ret[1] = y.toString();
	return ret;
    }

    public int[] computeHist(final double binwidth, final int iTarget, final IEventDataStream sequence, 
            final int iRef, final int bins)
    {
        int[] hist = new int[bins];
        if (iRef > -1 && iTarget > -1)
        {
            System.out.println("processing started");
            int len = (int)((sequence.getSequenceEnd() - sequence.getSequenceStart())/binwidth) + 2;
            int start = (int)(sequence.getSequenceStart()/binwidth);
            
            System.out.println("iRef = " + iRef + " iTarget = " + iTarget +" Len = " + len + " Start = " + start);
            char[] A = new char[len];
            char[] B = new char[len];
            
            
            System.out.println("Sequence opened");
            for(IEvent event : sequence.iterable(null))
            {
                int E_i = 0, t_i = 0;
                try
                {
                    E_i = event.getEventType();
                    t_i = (int)(event.getStartTime()/binwidth);
                    if (E_i == iRef) A[t_i - start] = 1;
                    if (E_i == iTarget) B[t_i - start] = 1;
                }
                catch(ArrayIndexOutOfBoundsException e)
                {
                    System.out.println("E_i = " + E_i);
                    System.out.println("t_i = " + t_i);
                    System.out.println("iRef = " + iRef);
                    System.out.println("iTarget = " + iTarget);
                    System.out.println("start = " + start);
                    System.out.println("binwidth = " + binwidth);
                    System.out.println("len = " + len);
                    throw e;
                }
            }
            System.out.println("Sequence parsed");
            
            for (int i = 0; i < len; i++)
            {
        	if (A[i] == 1)
        	{
        	    int k = 0;
        	    for (int j = i - bins/2; j <= i + bins/2; j++)
        	    {
        		if (j >= 0 && j < len)
        		{
        		    if (B[j] == 1) hist[k] ++;
        		}
        		k++;
        	    }
        	}
            }
            System.out.println("Correlogram constructed");
        }
        else
        {
            System.out.println("Error in input");
        }
        return hist;
    }
}
