/*
 * NewAdaptiveFrequencyThreshold.java
 *
 * Created on May 2, 2007, 2:15 PM
 *
 */

package edu.iisc.tdminercore.filter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Debprakash
 */
public class NegativeStrengthThreshold  extends AbstractFrequencyThreshold 
{   static final boolean DEBUG = true; // false to remove debugging
    
    /** 
     * Pruning the candidate list based on the count for each candidate.
     * @param candidates a list of episode candidates with their counts.
     * @param observer a mechanism for monitoring the progress of the candidate list generator.
     */
    
    @Override
    public List<IEpisode> prune(List<IEpisode> candidates, IObserver observer, 
            SessionInfo session)
    {
        if (DEBUG) { 
            System.out.println("NegativeStrengthThreshold: pruning episodes base on individual predictions");
            System.out.println("NegativeStrengthThreshold: start count ->" + candidates.size());
        }    
        observer.setExtent( candidates.size() );
        observer.startup();
        IEventDataStream sequence = session.getSequence();
        for (ListIterator<IEpisode> li = candidates.listIterator(); li.hasNext(); ) 
        {
            IEpisode episode = li.next();
            if (episode.size() == 1) return candidates;
            
            if (DEBUG)
            {
                System.out.println(episode.toString(sequence.getEventFactor()) + 
                        " : " + episode.getVoteString() + " < " + episode.getRequiredVoteString());
            }
            
            boolean flag = false;
            for(int index = 0; index < session.getSegIndexLen(); index++)
            {
                if (episode.getVotes(index) > episode.getRequiredVotes(index))
                {
                    flag = true;
                    break;
                }
            }
            if (flag)
            {
                li.remove();
            }
            if (observer.update(li.nextIndex())) break;
        }
        observer.shutdown();
        if (DEBUG) System.out.println("NegativeStrengthThreshold: final count ->" + candidates.size());
        return candidates;
    }
}
