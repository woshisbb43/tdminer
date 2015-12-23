/*
 * FastNonOverlapperdEpisodeCounter.java
 *
 * Created on March 16, 2006, 8:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
public class FastNonOverlappedEpisodeCounter extends AbstractSerialEpisodeCounter
{   static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of FastNonOverlapperdEpisodeCounter */
    public FastNonOverlappedEpisodeCounter()
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
	    waits.add(new ArrayList<Automaton>());
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
8:		// n is length of data stream //
 */
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
	    int E_i = event.getEventType();
	    double t_i = event.getStartTime();
            session.updateSegIndex(t_i);
/*
9:		for all (alpha, j) in waits(E_i) do
 */
	    List<Automaton> autoList = waits.get(E_i);
	    for (int i = 0; i < autoList.size(); i++)
	    {
		Automaton auto = autoList.get(i);
		int alphano = auto.getEpisodeIndex();
		IEpisode alpha = episodes.get(alphano);
		int j = auto.getState(); // current state
/*
10:			Remove (alpha, j) from waits(E_i)
11:			Set j_1 = j + 1
12:			if j_1 = (N + 1) then
13:				Set j_1 = 1
 */
		autoList.remove(i);
		// The list is being modified hence index
		// must be kept at the same location
		i--;
		
		int j_1 = (j + 1) % alpha.size(); // next state
/*
14:			if alpha[j_1] = E_i then
15:				Add (alpha, j_1) to bag
16:			else
17:				Add (alpha, j_1) to waits(alpha[j_1])
 */
		auto.setLastTransit(event);
		auto.setState(j_1);
		if (alpha.getEvent(j_1) == E_i)
		{
		    bag.add(auto);
		}
		else
		{
		    waits.get(alpha.getEvent(j_1)).add(auto);
		}
/*
18:			if j = N then
19:				Update alpha.freq = alpha.freq + 1
 */
		if ( j == alpha.size() - 1)
		{
		    alpha.incrVotes(session.getCurrentSegIndex());
                    if (session.isTrackEpisodes())
                    {
                        observer.handleEpisodeCompletion(alphano, 
                                alpha.getEventTypeIndices(), auto.getEvents());
                    }
		}
	    }//for
/*
20:		Empty bag into waits(Ei)
 */
	    for(int k=0; k < bag.size(); k++)
	    {
		waits.get(E_i).add(bag.get(k));
	    }
	    bag.clear();
	}
        iterable.setSampleSize(episodes);
        
        observer.shutdown();
//	observer.dispose();
//        observer.update(sequence.size());
        
    }
    
    public String getName()
    {
	return "Fast Non-overlapped count(Serial)";
    }
}
