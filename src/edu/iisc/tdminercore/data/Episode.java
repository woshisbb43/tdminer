/*
 * Episode.java
 *
 * Created on March 14, 2006, 2:40 PM
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.EpisodeComparator;
import java.util.Collections;
import java.util.List;
import edu.iisc.tdminercore.util.PickMatrix;

/**
 * An episode has little meaning without a definition of
 * the events from which it is built.
 * This set of definitions is not carried by the episodes themselves.
 * Rather, it is left to the different episode containers.
 * The episode, then, does not exist without its container.
 *
 * The episode is a simple graph, as a graph it is composed of nodes and edges.
 * The events are the nodes, while the intervals are edges.
 * 
 * An episode may be characterized by the following factors:
 * - non-overlap v. overlap: can two episodes of the same type overlap in time?
 * - episode closed v. open: does the episode expire?
 * - non-interleaf v. interleaf: a specific interval
 * - parallel v. serial
 *
 * - interval constraints: the time between events is constrained.
 *
 * The implementation of the episode with intervals.
 * Each episode keeps a reference to the intervals list.
 * Each episode keeps an array of indicies into that intervals list.
 * It is this array that qualifies the intervals between the events.
 * 
 * 
 * @author Deb
 * @author phreed@gmail.com
 * @see EpisodeSet - an episode collection
 * @see Epigraph - an episode collection
 *
 */

public class Episode extends AbstractEpisode
{
    static final boolean DEBUG = false; // false to remove debugging

    public static int episodeType;
    public static final int PARALLEL = 0;
    public static final int SERIAL = 1;
    
    /** Creates a new instance of Episode */
    public Episode(int size, EventFactor f)
    {
        super(size,f);
    }
    
    public Episode(int[] events, EventFactor f)
    {
        super(events,f);
    }
    
    public Episode(int size, IEpisode that) {
        super(size, that.getEventFactor(), that.getIntervalsList());
    }
    
    protected Episode(){}
    
    /** Creates a new instance of an episode 
     * The number of intervals will be one less than the number of events
     * as the intervals separate events.
     * @param size the order of the episode.
     * @param intervalsList a list of intervals 
     */
    public Episode(int size, EventFactor f, List<Interval> intervalsList)
    {
        super(size,f, intervalsList);
    }
    
    public Episode(int[] events, EventFactor f, int[] interval, List<Interval> intervalsList)
    {
	super(events,f, interval,intervalsList);
    }
    
    public String getSignature() {
        String out = "";
        for (int ix=0; ix < this.size(); ix++) {
            out += String.valueOf(this.event[ix]);
        }
        return out;
    }
     
    public boolean matchPrefix(IEpisode episode)
    throws IEpisode.NotImplementedException
    {
	boolean retVal = true;
	
	if (this.size() != episode.size()) return false;
	
	if (this.size() == episode.size() && this.size() == 1) return true;
	
	for (int ix = 0; ix < this.size() - 1; ix++) {
	    if (this.event[ix] != episode.getEvent(ix)) {
		return false;
	    }
	}
	return true;
    }
    
    
    public int findNonPrincipal(IEpisode that)
    { return 0; }
    
    /**
     * Empty duration methods,
     * They should give every appearance of having one and only one 
     * duration and attendant mapping to it.  Namely, [0 0].
     */
    public void setDurationMap(int index, int map) throws IEpisode.NotImplementedException 
    {
        throw new IEpisode.NotImplementedException("setDuration not possible");
    }
    public int getDuration(int index) throws IEpisode.NotImplementedException 
    {
        throw new IEpisode.NotImplementedException("getDuration not possible");
    }
    public PickMatrix<Interval> getDurations() throws IEpisode.NotImplementedException 
    {
        throw new IEpisode.NotImplementedException("getDurations not possible");
    }
    public List<Interval> getDurationsList() throws IEpisode.NotImplementedException 
    {
        throw new IEpisode.NotImplementedException("getDurationsList not possible");
    }
    public int getDurationsListSize() {
        return 0;
    }
    public int getLastDuration() throws IEpisode.NotImplementedException 
    {
        throw new IEpisode.NotImplementedException("getLastDuration not possible");
    }
    /**
     * Determine if the current episode, beta, is a sub-episode 
     * of another episode, alpha.
     * A sub-episode may be either strict or proper.
     * 1) the relative ordering of the event types must match
     * 2) the duration set for alpha is a proper/struct subset of that for beta.
     * There are three possible return values:
     * 0: neither alpha nor beta are sub-episodes of the other
     * 1: beta is a sub-episode of alpha
     * 2: alpha is a sub-episode of beta
     */
    public int isSubEpisode(IEpisode that) 
    {
        int retVal = 0;
        IEpisode beta = this;
        IEpisode alpha = that;
        int[] b = beta.getEventTypeIndices();//short
        int[] a = alpha.getEventTypeIndices();//long
        int rval = 1;
        if (a.length < b.length)
        {
            b = alpha.getEventTypeIndices();
            a = beta.getEventTypeIndices();
            rval = 2;
        }
        
        int type = episodeType;//0
        switch(type)
        {
            case 0: //PARALLEL
            {
                int i = 0;
                for(int j = 0; j < a.length; j++)
                {
                    if (b[i] == a[j]) 
                    {
                        i++;
                        if (i >= b.length) break;
                    }                
                }
                if (i == b.length)
                {
                    retVal = rval;
                }
            }
            break;
            case 1: //SERIAL
            {
                int i = 0;
                boolean firstfound = false;
                for(int j = 0; j < a.length; j++)
                {
                    if (firstfound)
                    {
                        if (b[i] != a[j]) break;
                        i++;
                        if (i >= b.length) break;
                    }
                    else if (b[i] == a[j])
                    {
                        firstfound = true;
                        i++;
                        if (i >= b.length) break;
                    }
                }
                if (firstfound && i == b.length)
                {
                    retVal = rval;
                }
            }
            break;
        }
        return retVal; 
    }
    
    /**
     * By default the required number of votes to be considered an episode
     * must be at least 2.
     * @param session the state information regarding the mining session.
     * @param params whatever the evaluator needs to determine the
     *     number of required votes.
     */
    public void evaluateRequiredVotes(SessionInfo session, Object[] params)
    {
        int num_segs = 1;
        if (session.isSegmented()) num_segs = session.getSegIndexLen();
        requiredVotes = new double[num_segs];
        for (int i = 0; i < requiredVotes.length; i ++)
            requiredVotes[i] = 2;
    }
    
    /**
     * check to see if the event list matches
     */
    public boolean isSimilar(IEpisode episode, int index) { 
        return false;
    }
   
    /**
     * Class methods
     */
    public static void sort(List<IEpisode> episodes, int sortOrder)
    {
        if (episodes == null) {
            // System.out.println("no need to sort an empty list");
            return;
        }
	Collections.sort(episodes, new EpisodeComparator(sortOrder));
    }
    
    public static void sort(List<IEpisode> episodes, int sortOrder, boolean ascending, int index)
    {
         if (episodes == null) {
            // System.out.println("no need to sort an empty list");
            return;
        }
	Collections.sort(episodes, new EpisodeComparator(sortOrder, ascending, index));
    }
    
    public static void sort(List<IEpisode> episodes, int sortOrder, boolean ascending, 
            EventFactor eventTypes, int index)
    {
         if (episodes == null) {
            // System.out.println("no need to sort an empty list");
            return;
        }
	Collections.sort(episodes, new EpisodeComparator(sortOrder, ascending, eventTypes, index));
    }
    @Override
    public Object clone(){ return this;}
}
