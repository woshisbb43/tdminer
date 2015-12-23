/*
 * AbstractFrequencyThreshold.java
 *
 * Created on November 17, 2006, 4:56 PM
 *
 */

package edu.iisc.tdminercore.filter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.List;
import java.util.ListIterator;

/**
 * @author phreed@gmail.com
 */
public abstract class AbstractFrequencyThreshold 
{   static final boolean DEBUG = false; // false to remove debugging
    
    /** 
     * Pruning the candidate list based on the count for each candidate.
     * @param candidates a list of episode candidates with their counts.
     * @param observer a mechanism for monitoring the progress of the candidate list generator.
     */
    public List<IEpisode> prune(List<IEpisode> candidates, IObserver observer, 
            SessionInfo session)
    {
        if (DEBUG) { 
            System.out.println("FrequencyThreshold: pruning episodes to " 
                    + session.getCurrentThreshold());
            System.out.println("FrequencyThreshold: start count ->" + candidates.size());
        }    
        observer.setExtent( candidates.size() );
        observer.startup();
        int index = session.getCurrentSegIndex();
        double threshold = session.getCurrentThreshold();
        for (ListIterator<IEpisode> li = candidates.listIterator(); li.hasNext(); ) 
        {
            IEpisode episode = li.next();
            if (episode.getFrequency(index) < threshold) { 
                li.remove(); 
            }
            if (observer.update(li.nextIndex())) break;
        }
        observer.shutdown();
        if (DEBUG) System.out.println("FrequencyThreshold: final count ->" + candidates.size());
        return candidates;
    }
    
}
