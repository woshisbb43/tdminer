/*
 * FixedFrequencyThreshold.java
 *
 * Created on February 2, 2007, 10:34 AM
 *
 */

package edu.iisc.tdminercore.filter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Debprakash Patnaik
 */
public class FixedFrequencyThreshold extends AbstractFrequencyThreshold  
{
    /** Creates a new instance of FixedFrequencyThreshold */
    public FixedFrequencyThreshold()
    {
    }

    /** 
     * Pruning the candidate list based on the count for each candidate.
     * @param candidates a list of episode candidates with their counts.
     * @param observer a mechanism for monitoring the progress of the candidate list generator.
     */
    public List<IEpisode> prune(List<IEpisode> candidates, IObserver observer, 
            SessionInfo session, int index)
    {
        int limit  = session.getFixedThresholdLimit();
        if (DEBUG) { 
            System.out.println("FrequencyThreshold: pruning episodes to " 
                    + limit);
            System.out.println("FrequencyThreshold: start count ->" + candidates.size());
        }    
        observer.setExtent( candidates.size() );
        observer.startup();
        for (ListIterator<IEpisode> li = candidates.listIterator(); li.hasNext(); ) 
        {
            IEpisode episode = li.next();
            if (episode.getVotes(index) < limit) { 
                li.remove(); 
            }
            if (observer.update(li.nextIndex())) break;
        }
        observer.shutdown();
        if (DEBUG) System.out.println("FrequencyThreshold: final count ->" + candidates.size());
        return candidates;
    }
}
