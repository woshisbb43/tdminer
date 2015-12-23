/*
 * NonOverlappedEpisodeCounter.java
 *
 * Created on March 16, 2006, 8:40 PM
 *
 */

package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.Automaton;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.IEventIterable;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 */
public class NonOverlappedEpisodeCounter extends AbstractSerialEpisodeCounter
{   static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of NonOverlappedEpisodeCounter */
    public NonOverlappedEpisodeCounter()
    {
    }
    
    @Override
    public void countEpisodes(List<IEpisode> episodes, IObserver observer, 
            SessionInfo session)
        throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        IEventDataStream sequence = session.getSequence();
	observer.startup();
        session.resetSegIndex();
        double episodeExp = session.getEpisodeExpiry();
	List<List<Automaton>> waits = new ArrayList<List<Automaton>>(sequence.getEventTypeCount());
/*
1:	for all event types A do
2:		Initialize waits(A) = null;
3:	for all alpha in C do
4:		Add (alpha, 1) to waits(alpha[1])
5:		Initialize alpha.freq = 0
 */
	observer.update(0);
	for (int i = 0; i < sequence.getEventTypeCount(); i++)
	{
	    waits.add(new ArrayList());
	}

	for(int i = 0; i < episodes.size(); i++)
	{
	    IEpisode e = episodes.get(i);
	    e.resetVotes();
            int num_segs = 1;
            if (session.isSegmented()) num_segs = session.getSegIndexLen();
            e.initVotes(num_segs);
            
	    int A = e.getFirstEvent();
	    if (!session.isTrackEpisodes())
	    {
		waits.get(A).add(new Automaton(i,0));
	    }
	    else
	    {
		waits.get(A).add(new Automaton(i,0,e.size()));
	    }
	}
/*
6:	Initialize bag = null;
 */
	List<Automaton> bag = new ArrayList<Automaton>();
	
/*
7:	for i = 1 to n do
 */
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
            if (observer.interrupted()) break;
            
	    int E_i = event.getEventType();
	    double t_i = event.getStartTime();
            session.updateSegIndex(t_i);
/*
8:		for all (alpha, j) in waits(E_i) do
 */
	    List<Automaton> autoList = waits.get(E_i);
	    for (int i = 0; i < autoList.size(); i++)
	    {
		Automaton auto = autoList.get(i);
		Automaton next_auto = null;
		int alphano = auto.getEpisodeIndex();
		IEpisode alpha = episodes.get(alphano);
		int j = auto.getState(); // current state
/*
9:		if j = 1 then
10:		    Update alpha.init[1] = t_i
11:		else
12:		    Update alpha.init[j] = alpha.init[j-1]
13:		    Remove (alpha,j) from waits(E_i)
 */
		if (j == 0)
		{
		    //alpha.setInit(0, t_i);
		    if (!session.isTrackEpisodes())
			next_auto = new Automaton(alphano, j); //Create a clone automaton
		    else
			next_auto = new Automaton(alphano, j, alpha.size()); //Create a clone automaton

                    next_auto.setStartEvent(event);
		}
		else
		{
		    //alpha.setInit(j, alpha.getInit(j - 1));
		    next_auto = auto;
		    autoList.remove(i);
		    // The list is being modified hence index
		    // must be kept at the same location
		    i--;
		}

/*
 * 14:		if j < N then
 * 15:		    if alpha[j + 1] = E_i then
 * 16:			Add (alpha, j + 1) to bag
 * 17:		    else
 * 18:			Add (alpha, j + 1) to waits(E_i)
 */
		next_auto.setLastTransit(event);
		next_auto.setState(j + 1);
		if (j < alpha.size() - 1)
		{
		    if (alpha.getEvent(j + 1) == E_i)
		    {
			while(bag.contains(next_auto))
			{
			    bag.remove(next_auto);
			}
			bag.add(next_auto);
		    }
		    else
		    {
			List<Automaton> waitsList = waits.get(alpha.getEvent(j + 1));
			while(waitsList.contains(next_auto))
			{
			    waitsList.remove(next_auto);
			}
			waitsList.add(next_auto);
		    }
		}
		
/*
 * 19:		if j = N and (t_i - alpha.init[j]) < Tx then
 * 20:		    Update alpha.freq = alpha.freq + 1
 * 21:		    for all 1<=k<=|alpha| do
 * 22:			Remove (alpha, k + 1) from waits(alpha[k+1])
 * 23:			Remove (alpha, k + 1) from bag
 */
                //System.out.println(episodeExp);
		//if ((j == alpha.size() - 1) && (t_i - alpha.getInit(j)) <= episodeExp)
                if ((j == alpha.size() - 1) && (t_i - next_auto.getStartEvent().getStartTime()) < episodeExp)
		{
		    alpha.incrVotes(session.getCurrentSegIndex());
		    for (int k = 1; k < alpha.size(); k++)
		    {
			int E_k = alpha.getEvent(k);
			List<Automaton> list = waits.get(E_k);
			for (int l = 0; l < list.size(); l++)
			{
			    Automaton beta = list.get(l);
			    if (beta.getEpisodeIndex() == alphano && beta.getState() != 0)
			    {
				list.remove(l);
				l--;
			    }
			}
		    }
		    for (int l = 0; l < bag.size(); l++)
		    {
			Automaton beta = bag.get(l);
			if (beta.getEpisodeIndex() == alphano && beta.getState() != 0)
			{
			    bag.remove(l);
			    l--;
			}
		    }
                    if (session.isTrackEpisodes())
                        observer.handleEpisodeCompletion(alphano, alpha.getEventTypeIndices(), auto.getEvents());
		}
	    }//for
/*
20:		Empty bag into waits(Ei)
 */
	    List<Automaton> bagList = waits.get(E_i);
	    bagList.addAll(bag);
	    bag.clear();
	}
        iterable.setSampleSize(episodes);
        
        observer.shutdown();
        observer.update(sequence.getSize());
    }
    
    public String getName()
    {
	return "Non-overlapped count with episode expiry constraint(Serial)";
    }
}
