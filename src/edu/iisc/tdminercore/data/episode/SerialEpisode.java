/*
 * SerialEpisodeWithFixedInterval.java
 *
 * Created on May 2, 2007, 10:18 AM
 *
 */

package edu.iisc.tdminercore.data.episode;

import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;
import java.util.List;
import edu.iisc.tdminercore.filter.SastryEstimator;
import edu.iisc.tdminercore.filter.SastryNegativeEstimator;

/**
 *
 * @author hzg3nc
 */
public class SerialEpisode extends Episode
{
    private static final boolean DEBUG = false;
    
    /** Creates a new instance of SerialEpisodeWithFixedInterval */
    /** Creates a new instance of ParallelEpisode */
    public SerialEpisode(int size, EventFactor f)
    {
        super(size, f);
        //setEstr(new double[1]);
    }
    
    public SerialEpisode(int[] events, EventFactor f)
    {
        super(events,f);
        //setEstr(new double[1]);
    }
    
    /** Creates a new instance of an episode 
     * The number of intervals will be one less than the number of events
     * as the intervals separate events.
     * @param size the order of the episode.
     * @param intervalsList a list of intervals 
     */
    public SerialEpisode(int size, EventFactor f, List<Interval> intervalsList)
    {
        super(size,f,intervalsList);
        //setEstr(new double[1]);
    }
    
    public SerialEpisode(int[] events, EventFactor f, int[] interval, List<Interval> intervalsList)
    {
	super(events,f,interval,intervalsList);
        //setEstr(new double[1]);
    }

    protected SerialEpisode()
    {
    }
    
    @Override
    public Interval getInterval(int index)
    {
        if (this.intervalsList == null)
        {
            return null;
        }
        return this.intervalsList.get(this.interval[index]);
    }
    
    @Override
    public void evaluateRequiredVotes(SessionInfo session, Object[] params)
    {
        IEpisode alpha = (IEpisode)params[0];
        IEpisode beta  = (IEpisode)params[1];

        if (DEBUG)
        {
            System.err.println("Threshold Type: " + session.getThresholdType());
        }
        switch (session.getThresholdType())
        {

            case STRENGTH_BASED:
                if (size() == 1)
                {
                    int num_segs = 1;
                    if (session.isSegmented()) num_segs = session.getSegIndexLen();
                    requiredVotes = new double[num_segs];
                    for (int i = 0; i < requiredVotes.length; i ++)
                        requiredVotes[i] = 2;
                }
                else
                {
                    SastryEstimator se = new SastryEstimator();
                    this.requiredVotes = se.threshold(session, this);
                }
                break;
            case NEG_STRENGTH:
                if (size() == 1)
                {
                    int num_segs = 1;
                    if (session.isSegmented()) num_segs = session.getSegIndexLen();
                    requiredVotes = new double[num_segs];
                    for (int i = 0; i < requiredVotes.length; i ++)
                        requiredVotes[i] = Integer.MAX_VALUE;
                }
                else
                {
                    SastryNegativeEstimator se = new SastryNegativeEstimator();
                    this.requiredVotes = se.threshold(session, this);
                }
                break;
            default:
                break;
        }
        if (DEBUG)
        {
            System.err.println("Threshold: " + this.requiredVotes);
        }
    }
    
    @Override
    public void postCountProcessing(SessionInfo session)
    {
        if (this.size() != 2)
        {
            return;
        }
        
//	SastryEstimator se = new SastryEstimator();
//        try
//        {
//	    //System.out.println("Estr estimate :: " + this.toString(session.getEventFactor()));
//	    double[] estr = se.solve(session, this);
//            this.setEstr(estr);
//	    System.out.println("Estr estimate :: " +
//                    this.toString(session.getEventFactor()) + " : " +
//                    String.valueOf(estr) );
//        }
//        catch (SastryEstimator.UndefinedStrengthException ex)
//        {
//            System.out.println("Could not estimate Estrong " +
//                    ex.getLocalizedMessage() );
//            return;
//        }
    }
    
    @Override
    public Object clone()
    {
        SerialEpisode e = new SerialEpisode();
        createCopy(e);
        return e;
    }
}
