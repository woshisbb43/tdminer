/*
 * ThresholdEstimator.java
 *
 * Created on August 29, 2007, 4:34 PM
 *
 */

package edu.iisc.tdminercore.filter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;

/**
 *
 * @author cznwdr
 */
public abstract class ThresholdEstimator
{
    protected double[] requiredVotes;
    
    /** Creates a new instance of ThresholdEstimator */
    public ThresholdEstimator() {
    }
     
    public double deriveDuration(SessionInfo session, IEpisode gamma) {
        
        if (gamma.getIntervalsList() == null) { //then we have one universal interval
           return (gamma.size()) * //BUGFIXEd? 1 event ep = 0int 2 ep = 1 int
               (session.getIntervalExpiry() + session.getIntervalExpiryLow()) /
                    2.0;
        } 
        double T = 0;
        
        for(int k = 0; k < gamma.size() - 1; k++) {
            Interval i = gamma.getInterval(k);
            T += (i.getTHigh() + i.getTLow()) / 2.0; 
        }
        
        return T;
    }
    
    public double deriveLastIntervalDuration(SessionInfo session, IEpisode gamma) {  
        if (gamma.getIntervalsList() == null) { //then we have one universal interval
            return (session.getIntervalExpiry() - session.getIntervalExpiryLow());
        } 
        return gamma.getInterval(gamma.size() - 2).getGap();
    }
    
    /**
     * It is unreasonable to have a threshold less than two.
     * It is impossible to make any estimate of a period with less.
     * @param threshold the computed threshold from the estimator
     */
    protected final void setThreshold(double threshold, int index) {
        //this.requiredVotes[index] = (threshold < 2) ? 2 : threshold;
        this.requiredVotes[index] = threshold;
    }
    
    public double[] threshold() {
        return this.requiredVotes;
    } 
}
