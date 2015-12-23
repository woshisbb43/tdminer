/*
 * RamdomVariableGen.java
 *
 * Created on April 17, 2006, 11:40 AM
 *
 */

package edu.iisc.tdminercore.util;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author patnaik
 */
public class RandomVariableGen
{
    
    /** Creates a new instance of RamdomVariableGen */
    private RandomVariableGen()
    {
    }
    
    /* =========================================================
     * Returns an exponentially distributed positive real number.
     * NOTE: use m > 0.0
     * =========================================================
     */
    private static double exponential(double lambda)
    {
	return (- Math.log(1.0 - Math.random()) / lambda);
    }
    
    /** 
     * Defines a Poisson distributed non-negative integer.
     *
     * @param deltaT the time 
     * @param lamda the frequency of the poisson process
     * @param start the time offset 
     * @return an array of real numbers indicating the time(s) the neuron fires.
     */
    public static double[] poisson(double deltaT, double lambda, double start)
    {
	List<Double> l = new ArrayList<Double>();
	
	double t = exponential(lambda);
	while (t < deltaT)
	{
	    l.add(new Double(t));
	    t += exponential(lambda);
	    //System.out.print(".");
	}
	
	//System.out.println(" " + l.size());
	double[] ret = new double[l.size()];
	for (int i = 0; i < ret.length; i++)
	{
	    ret[i] = start + l.get(i).doubleValue();
	}
	
	return ret;
    }
}
