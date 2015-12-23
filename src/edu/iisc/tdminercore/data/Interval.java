/*
 * Interval.java
 *
 * Created on April 5, 2006, 11:00 AM
 *
 */

package edu.iisc.tdminercore.data;

import java.text.NumberFormat;

/**
 *
 * @author patnaik
 * @author phreed@gmail.com
 */
public class Interval implements Cloneable
{
    private double tLow;
    private double tHigh;
    
    @Override
    public Interval clone() {
        return new Interval(tLow, tHigh);
    }
    
    private boolean nearEqual(double a, double b)
    {
        return Math.abs(a - b) < 0.000001;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Interval)
        {
            Interval i = (Interval)o;
            return nearEqual(i.tLow, tLow) && nearEqual(i.tHigh, tHigh);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.tLow) ^ (Double.doubleToLongBits(this.tLow) >>> 32));
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.tHigh) ^ (Double.doubleToLongBits(this.tHigh) >>> 32));
        return hash;
    }
    
    /** 
     * Creates a new instance of Interval 
     * The absence of parameters implies an unlimited interval [0 +Inf].
     * A null interval in an episode should be interpreted as an 
     * unlimited interval as well.
     */
    public Interval()
    {
        this.tLow = Double.NEGATIVE_INFINITY;
        this.tHigh = Double.POSITIVE_INFINITY;
    }
    
    public Interval(double tLow, double tHigh)
    {
        this.init(tLow,tHigh);
    }
    
    /**
     * Used for unmarshalling the stringified encoding from toString().
     */
    private final String INTERVAL_DELIMITER = "-";
    public Interval(String interval)
    {
        String[] values = interval.split( INTERVAL_DELIMITER );
        double tLow = Double.parseDouble(values[0].trim());
        double tHigh = Double.parseDouble(values[1].trim());
        this.init(tLow,tHigh);
    }
    
    private void init(double tLow, double tHigh)
    {
        if (tLow > tHigh) {
            this.tLow = tHigh;
            this.tHigh = tLow;
        }
        else {
            this.tLow = tLow;
            this.tHigh = tHigh;
        }
    }
    
    public String toString()
    {
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(10);	
	//return "[" + nf.format(this.tLow) + "-" + nf.format(this.tHigh) + "]";
        return nf.format(this.tLow) + INTERVAL_DELIMITER + nf.format(this.tHigh);
    }

    public double getTLow() { return tLow; }
    public double getTHigh() { return tHigh; }
    public double getGap() { return tHigh - tLow; }
    public double getAvg() {return (tHigh + tLow)/2.0;}
    
    public static Interval parse(String text) throws Exception
    {
        String[] parts = text.split("\\s*-\\s*");
        if (parts.length == 2)
        {
            double tLow = Double.parseDouble(parts[0]);
            double tHigh = Double.parseDouble(parts[1]);
            Interval interval = new Interval(tLow, tHigh);
            return interval;
        }
        throw new Exception("Improper interval string : " + text);
    }
}
