/*
 * ParallelNonOverlapperEpisodeCounter.java
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
public class ParallelNonOverlapperEpisodeCounter extends AbstractParallelEpisodeCounter
{   static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of NonInterleavedEpisodeCounter */
    public ParallelNonOverlapperEpisodeCounter()
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
	observer.update(0);

	List<List<Automaton>> waits = new ArrayList<List<Automaton>>(sequence.getEventTypeCount());
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
	    // a = Number of event of type A in alpha
	    int a = 1;
	    for (int j = 1; j < e.size(); j++)
	    {
		int B = e.getEvent(j);
		if (A == B)
		{
		    a ++;
		}
		else
		{
		    waits.get(A).add(new Automaton(i, a));
		    //System.out.println("automatons are = (" + i + "," + a +")");
		    a = 1;
		}
		A = B;
	    }
	    waits.get(A).add(new Automaton(i, a));
	    e.resetVotes();
	    e.resetCounter();
	}
	
	List<Automaton> bag = new ArrayList<Automaton>();
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
	    int E_i = event.getEventType();
	    double t_i = event.getStartTime();
            session.updateSegIndex(t_i);

	    List<Automaton> autoList = waits.get(E_i);
	    for (int i = 0; i < autoList.size(); i++)
	    {
		Automaton auto = autoList.get(i);
		int alphano = auto.getEpisodeIndex();
		IEpisode alpha = episodes.get(alphano);
		int j = auto.getState(); // current state
		
                alpha.incrCounter();
		autoList.remove(i);
		i--;
		
		if (j > 1)
		{
		    auto.setState(j - 1);
		    bag.add(auto);
		}
		
		if (alpha.getCounter() == alpha.size())
		{
		    alpha.incrVotes(session.getCurrentSegIndex());
		    alpha.resetCounter();
		    int A = alpha.getFirstEvent();
		    // a = Number of event of type A in alpha
		    int a = 1;
		    for (int k = 1; k < alpha.size(); k++)
		    {
			int B = alpha.getEvent(k);
			if (A == B)
			{
			    a ++;
			}
			else
			{
			    if (A == E_i)
				bag.add(new Automaton(alphano, a));
			    else
				waits.get(A).add(new Automaton(alphano, a));
			    a = 1;
			}
			A = B;
		    }
		    if (A == E_i)
			bag.add(new Automaton(alphano, a));
		    else
			waits.get(A).add(new Automaton(alphano, a));
		}
	    }

	    // Empty bag into waits(Ei)
	    waits.get(E_i).addAll(bag);
	    bag.clear();
	}
        iterable.setSampleSize(episodes);
        
//	observer.dispose();
        observer.update(sequence.getSize());
    }
	    
    public String getName()
    {
	return "Non-overlapped count(Parallel)";
    }
}
