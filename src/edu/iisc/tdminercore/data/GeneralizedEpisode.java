/*
 * GeneralizedEpisode.java
 *
 * Created on July 5, 2006, 10:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.miner.SessionInfo;
import java.util.List;
import edu.iisc.tdminercore.util.PickMatrix;

/**
 *
 * @author patnaik
 * @author phreed@gmail.com
 */
 
public class GeneralizedEpisode extends AbstractEpisode
{
    /**
     * This unordered set of items is used to compare sets whose
     * members are stored in a List.
     */
   
    /** Creates a new instance of GeneralizedEpisode */
    public GeneralizedEpisode(int size, EventFactor f, List<Interval> durationList)
    {
	super(size,f);
        this.durationPick = new PickMatrix(size,durationList);
    }
    
    public GeneralizedEpisode(int size, EventFactor f, PickMatrix<Interval> duration)
    {
	super(size,f);
        this.durationPick = duration;
    }

    public GeneralizedEpisode(int[] events, EventFactor f, int[] durationIn, List<Interval> durationList)
    {
	super(events,f);
        this.durationPick = new PickMatrix(events, durationList, durationIn);
    }
    
    @Override
    public String getSignature() { return ""; }
    
    /**
     * durationPick is an array of bit maps.
     * Each bit map indicating which time duration intervals are 
     * associated with each event type.
     */
    @Override
    public void setDurationMap(int index, int map)
    {
	this.durationPick.setMap(index, map);
    }
    
    @Override
    public int getDuration(int index)
    {
	return this.durationPick.getMap(index);
    }
    
    @Override
    public PickMatrix<Interval> getDurations()
    {
	return this.durationPick;
    }
    
    @Override
    public String toString(EventFactor eventTypes)
    {
	StringBuffer buf = new StringBuffer();
	for (int kx = 0; kx < size(); kx++)
	{
	    buf.append(eventTypes.getName(getEvent(kx)));
	    buf.append(durationPick.toString(kx));
            buf.append(" ");
        }
	return buf.toString();
    }
    
    @Override
    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	for (int kx = 0; kx < size(); kx++)
	{
	    buf.append(getEvent(kx));
            buf.append(durationPick.toString(kx));
	    buf.append(" ");
	}
	buf.append(":" + getVotes());
	return buf.toString();
    }
    
    @Override
    public List<Interval> getDurationsList()
    {
	return this.durationPick.getShingles();
    }
    
    /**
     * This function compares the episode types with another episode.
     */
    @Override
    public boolean matchPrefix(IEpisode episode)
    throws IEpisode.NotImplementedException
    {
        return (this.durationPick.matchPrefix(episode.getDurations()));
    }
    
    /**
     * An episode, alpha, is similar to another episode, beta,
     * if three conditions hold...
     * 1) one is a sub-episode of the other
     * 2) their event type vectors map 1:1
     * 3) their frequencies are the same
     */
    @Override
    public boolean isSimilar(IEpisode episode, int index)
    {
	if (this.size() != episode.size()) return false;
	
	for (int ix = 0; ix < this.size(); ix++)
	{
	    if (this.event[ix] != episode.getEvent(ix)) return false;
	}
	return this.getVotes(index) == episode.getVotes(index);
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
    @Override
    public int isSubEpisode(IEpisode that)
    {
	int retVal = 0;
        boolean flag = false;
	for (int ix = 0; ix < this.size(); ix++)
	{
	    int d1 = this.getDuration(ix);
	    int d2 = that.getDuration(ix);
	    int dAnd = d1 & d2;
            if(d1 != d2)
            {
                if (!flag)
                {
                    if (dAnd == d1) retVal = 2;// Non principal is the super-set
                    if (dAnd == d2) retVal = 1;
                    flag = true;
                }
                else
                {
                    if (dAnd == d1 && retVal == 1) { retVal = 0; break;}
                    if (dAnd == d2 && retVal == 2) { retVal = 0; break;}
                }
            }
	}
	return retVal;
    }
    
    /**
     * determine if the first of two episodes is relatively 'principle'.
     * And episode alpha is principle in an event sequence if...
     * 1) it is 'found' in the target sequence
     * 2) there exists no other strict super-episode beta such that 
     *    beta is 'similar' to alpha.
     */
    @Override
    public int findNonPrincipal(IEpisode that)
    {
	int retVal = 0;
        boolean flag = false;
	for (int i = 0; i < this.size(); i++)
	{
	    int d1 = this.getDuration(i);
	    int d2 = that.getDuration(i);
	    int dAnd = d1 & d2;
            if(d1 != d2)
            {
                if (!flag)
                {
                    if (dAnd == d1) retVal = 2;// Non principal is the super-set
                    if (dAnd == d2) retVal = 1;
                    flag = true;
                }
                else
                {
                    if (dAnd == d1 && retVal == 1) { retVal = 0; break;}
                    if (dAnd == d2 && retVal == 2) { retVal = 0; break;}
                }
            }
	}
	return retVal;
    }
    
    /**
     * return the last duration bit map.
     */
    @Override
    public int getLastDuration()
    {
	return this.durationPick.getLastMap();
    }
    
    @Override
    public int getDurationsListSize()
    {
	return durationPick.sizeShingles();
    }

    public void evaluateRequiredVotes(SessionInfo session, Object[] params) {
        int num_segs = 1;
        if (session.isSegmented()) num_segs = session.getSegIndexLen();
        requiredVotes = new double[num_segs];
        for (int i = 0; i < requiredVotes.length; i ++)
            requiredVotes[i] = 2;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final GeneralizedEpisode that = (GeneralizedEpisode) obj;
        
        //throw new RuntimeException("Who called me");
        return this.durationPick.equals(that.durationPick);
    }

    @Override
    public int hashCode()
    {
        return this.durationPick.hashCode();
    }  
}