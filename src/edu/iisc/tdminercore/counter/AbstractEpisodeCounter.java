/*
 * AbstractEpisodeCounter.java
 *
 * Created on March 14, 2006, 2:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.IEventIterable;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All counters use the same mechanism for counting single node episodes.
 * 
 * @author Deb
 */
public abstract class AbstractEpisodeCounter
{
    static final boolean DEBUG = false; // false to remove debugging

    public abstract void countEpisodes(List<IEpisode> episodes, IObserver observer, 
            SessionInfo session)
    throws IObserver.NotImplementedException, IEpisode.NotImplementedException;
    
    /** 
     * Each counter counts the single node episode in the same way.
     * This method should be called when the single node case is encountered.
     * This method examines the event stream (found in the session) for
     * instances of the (single event) episodes defined in the episode list.
     * When it finds such an episode it is evaluated by the observer and the
     * episode is updated.
     * Finally, the episodes are each updated with the sample size.
     *
     * @param episodes the single node episode list
     * @param observer the observer/functor to be evaluated for each match
     * @param session the master mining object, contains all state information
     * 
     */
    public void countEvents(List<IEpisode> episodes, IObserver observer, SessionInfo session)
    throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        IEventDataStream sequence = session.getSequence();
        observer.startup();
        session.resetSegIndex();
        // make a hash map into the episodes
        Map<Integer,IEpisode> event2episode = new HashMap<Integer,IEpisode>(episodes.size());
        for( IEpisode episode : episodes ) {
            event2episode.put(episode.getFirstEvent(), episode);
            episode.resetVotes();
            int num_segs = 1;
            if (session.isSegmented()) num_segs = session.getSegIndexLen();
            episode.initVotes(num_segs);
            episode.evaluateRequiredVotes(session, null);
        }
	
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
            double t = event.getStartTime();
            session.updateSegIndex(t);
	    IEpisode episode = event2episode.get(event.getEventType());
            if (episode == null) {
                System.out.println("infrequent event: " + event.getEventType());
                continue;
            }
            episode.incrVotes(session.getCurrentSegIndex());
	}
        iterable.setSampleSize(episodes);
        
        observer.shutdown();
	observer.update(sequence.getSize());
    }
    
    /**
     * A placeholder function for dealing with whatever.
     * In the case of Generalized Episodes this means removing non-principle episodes.
     */
    public List<IEpisode> postCountProcessing(List<IEpisode> episodes, IObserver observer, SessionInfo session)
        throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        return episodes;
    }
    
    public abstract String getName();
}
