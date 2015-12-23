/*
 * GeneratorState.java
 *
 * Created on August 10, 2007, 1:59 PM
 *
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.data.IEpisode;
import java.util.List;

/**
 *
 * @author phreed@gmail.com
 */
abstract public class GeneratorState 
{
    private int size = 0;
    private int[] ix;
    private boolean isComplete = false;
    
    /** Creates a new instance of GeneratorState */
    public GeneratorState(int size) {
        ix = new int[size];
    }

    public void record(List<IEpisode> episodes) {
        if (episodes != null)
            this.size = episodes.size();
        else
            this.size = 0;
    }
    
    public int getIx(int dimension)
    {
        return this.ix[dimension];
    }
    
    public void setIx(int dimension, int value)
    {
       this.ix[dimension] = value;
    }
    
    
    public boolean incrIx(int dimension)
    {
        // if (this.ix[dimension] >= this.size) return false;
        
        this.ix[dimension]++;
        return true;
    }
     
    public double getProgress() 
    {
        if (this.isComplete) return 1.0;
        
        long steps = 0L;
        long total = 1L;
        for( int jx : ix ) {
            steps *= this.size;
            steps += jx;
            total *= this.size;
        }
        return (double)steps/total;
    }
    
    public void reset()
    {
        for (int jx = 0; jx < ix.length; jx++) 
        {
             this.ix[jx] = 0;
        }
        this.isComplete = false;
    }
    public void reset(int dimension)
    {
        this.ix[dimension] = 0;
    }
    
    public boolean atBeginning()
    {
        for (int jx = 0; jx < this.ix.length; jx++) 
        {
             if (this.ix[jx] > 0) return false;
        }
        return true;
    }
    
    public void markComplete() { 
        this.isComplete = true;
    }
    public boolean isComplete() { 
        return this.isComplete; 
    }
    
}

