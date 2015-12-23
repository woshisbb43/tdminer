/*
 * NonInterleavedEpisodeCounter.java
 *
 * Created on March 24, 2006, 1:51 PM
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
public class NonInterleavedEpisodeCounter extends AbstractSerialEpisodeCounter
{  static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of NonInterleavedEpisodeCounter */
    public NonInterleavedEpisodeCounter()
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
	    int A = e.getFirstEvent();
	    e.resetVotes();
            int num_segs = 1;
            if (session.isSegmented()) num_segs = session.getSegIndexLen();
            e.initVotes(num_segs);
            
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
		int alphano = auto.getEpisodeIndex();
		IEpisode alpha = episodes.get(alphano);
		int j = auto.getState(); // current state
		
/*
9:		if j < N then
10:		    if (alpha, j + 1) does not belong to waits(alpha[j + 1]) then
11:			Remove (alpha,j) from waits(E_i)
12:			if alpha[j + 1] = E_i then
13:			    Add (alpha, j + 1) to bag
14:			else
15:			    Add (alpha, j + 1) to waits(E_i)
16:			if j = 1 then
17:			    Add (alpha, 1) to bag
 */
		if (j < alpha.size() - 1)
		{
		    Automaton next_auto = new Automaton(alphano, j + 1);
		    
		    List<Automaton> waitsList = waits.get(alpha.getEvent(j + 1));
		    if (!waitsList.contains(next_auto))
		    {
			auto.setLastTransit(event);
			next_auto.setEvents(auto);
			autoList.remove(i);
			i--;
			if (alpha.getEvent(j + 1) == E_i)
			{
			    bag.add(next_auto);
			}
			else
			{
			    waitsList.add(next_auto);
			}
			if (j == 0)
			{
			    if (!session.isTrackEpisodes())
				bag.add(new Automaton(alphano, 0));
			    else
				bag.add(new Automaton(alphano, 0, alpha.size()));
			}
		    }
		}
/*		
18:		if j = N then
19:		    Remove (alpha, j) from waits(E_i)
20:		    Update alpha.freq = alpha.freq + 1
 */
		if (j == alpha.size() - 1)
		{
		    autoList.remove(i);
		    i--;
		    alpha.incrVotes(session.getCurrentSegIndex());
		    observer.handleEpisodeCompletion(alphano, alpha.getEventTypeIndices(), auto.getEvents());
		}
	    }//for
	    
/*
21:		Empty bag into waits(Ei)
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
        observer.update(sequence.getSize());
        
    }
    
    public String getName()
    {
	return "Non-interleaved count(Serial)";
    }
    
}
