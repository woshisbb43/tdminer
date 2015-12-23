/*
 * ScaledForIntervalThreshold.java
 *
 * Created on July 11, 2007, 9:50 AM
 *
 */

package edu.iisc.tdminercore.filter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Debprakash Patnaik
 */
public class ScaledForIntervalThreshold extends AbstractFrequencyThreshold  
{
    @Override
    public List<IEpisode> prune(List<IEpisode> candidates, IObserver observer, 
            SessionInfo session)
    {
        int index = session.getCurrentSegIndex();
        int level = session.getCurrentLevel();
        if (level == 1)
        {
            session.setCurrentThreshold(session.getFrequencyThreshold(1));
        }
        else
        {
            double t = session.getFrequencyThreshold(2);
            for (int i = 0; i < (level - 2); i++) t *= session.getFreqDecay();
            session.setCurrentThreshold(t);
        }
        observer.setExtent( candidates.size() );
        observer.startup();
        for (ListIterator<IEpisode> li = candidates.listIterator(); li.hasNext(); ) 
        {
            double threshold = session.getCurrentThreshold();
            //System.out.println("INIT threshold = " + threshold);
            IEpisode episode = li.next();
            if (episode.getIntervalsList() != null)
            {
                double len = 0.0;
                for(int i = 0; i < episode.size() - 1; i++)
                {
                    Interval interval = episode.getInterval(i);
                    if (interval != null) len += (interval.getTLow() + interval.getTHigh())/2.0;
                }
                if (len > 0.0)
                {
                    double t = session.getAdaptiveThresholdParameter() * len;
                    if (t < 0.0 || t > 1.0) System.out.println("ERROR: Multiplying " +
                            "factor for interval out of bounds : " + t);
                    threshold *= (1 - t);
                }
//                System.out.println("Len : " + len + " threshold : " + threshold + 
//                        " episode: " + episode.toString(session.getSequence().getEventFactor()) + 
//                        " votes : " + episode.getVotes());
            }
            
            if (episode.getFrequency(index) < threshold || episode.getVotes(index) < 2) { 
                li.remove();
            }
            if (observer.update(li.nextIndex())) break;
        }
        observer.shutdown();
        if (DEBUG) System.out.println("FrequencyThreshold: final count ->" + candidates.size());
        return candidates;
    }    
}
