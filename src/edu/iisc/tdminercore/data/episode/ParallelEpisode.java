/*
 * ParallelEpisode.java
 *
 * Created on May 2, 2007, 10:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.data.episode;

import cern.jet.stat.Probability;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;
import java.util.List;

/**
 *
 * @author hzg3nc
 */
public class ParallelEpisode extends Episode
{
    
    /** Creates a new instance of ParallelEpisode */
    public ParallelEpisode(int size, EventFactor f)
    {
        super(size, f);
    }
    
    public ParallelEpisode(int[] events, EventFactor f)
    {
        super(events,f);
    }
    
    /** Creates a new instance of an episode 
     * The number of intervals will be one less than the number of events
     * as the intervals separate events.
     * @param size the order of the episode.
     * @param intervalsList a list of intervals 
     */
    public ParallelEpisode(int size, EventFactor f, List<Interval> intervalsList)
    {
        super(size,f, intervalsList);
    }
    
    public ParallelEpisode(int[] events, EventFactor f, int[] interval, List<Interval> intervalsList)
    {
	super(events,f,interval,intervalsList);
    }
    
    protected ParallelEpisode()
    {
    }

    /** 
     * Determine the number of votes required to qualify as an episode.
     * 2 is the bare minimum.
     */
    @Override
    public void evaluateRequiredVotes(SessionInfo session, Object[] params)
    {
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
            for(int seg_index = 0; seg_index < session.getSegIndexLen(); seg_index ++)
            {
                List<IEpisode> oneNodeList = session.getEventFactor().getEpisodeList();
                double[] lambda = new double[this.size()];
                double T_total = session.endTime(seg_index) - session.startTime(seg_index);
                for(int i = 0; i < this.size(); i++)
                {
                    int eIndex = this.event[i];
                    for(IEpisode eps : oneNodeList)
                    {
                        if (eIndex == eps.getEvent(0))
                        {
                            lambda[i] = ((double)eps.getVotes(seg_index)) / T_total;
                            //System.out.println("lambda[" + i + "] = " + lambda[i]);
                            break;
                        }
                    }
                }
                double T = session.getEpisodeExpiry();
                double n = T_total / T;
                double p = 1.0;
                for(double l : lambda) 
                {
                    p *= (1 - Math.exp(-l * T));
                }

                double mu = n * p;
                double sigma = Math.sqrt(n * p * (1.0 - p));
                double th = mu + sigma * Probability.normalInverse(1.0 - session.getPoissonError());
                requiredVotes[seg_index] = (int)th;
                if (requiredVotes[seg_index] < 2)
                {
                    requiredVotes[seg_index] = 2;
                }
            }
        }
    }
    @Override
    public Object clone()
    {
        ParallelEpisode e = new ParallelEpisode();
        createCopy(e);
        return e;
    }
}
