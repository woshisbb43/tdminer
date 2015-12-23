/*
 * ParallelEpisodesCounterWithExpiry.java
 *
 * Created on April 14, 2006, 1:51 PM
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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Deb
 */
public class ParallelEpisodesCounterWithExpiry extends AbstractParallelEpisodeCounter
{  static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of ParallelEpisodesCounterWithExpiry */
    public ParallelEpisodesCounterWithExpiry() 
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

        double episodeExp = session.getEpisodeExpiry();
	List<List<Automaton>> waits = new ArrayList<List<Automaton>>(sequence.getEventTypeCount());
        List<List<Automaton>> epsiodeAutoLists = new ArrayList<List<Automaton>>(episodes.size());
        
	for (int ix = 0; ix < sequence.getEventTypeCount(); ix++)
	{
	    waits.add(new ArrayList<Automaton>());
	}
	
	for(int ix = 0; ix < episodes.size(); ix++)
	{
	    IEpisode e = episodes.get(ix);
            List<Automaton> epsAutoList = new ArrayList<Automaton>();
            epsiodeAutoLists.add(epsAutoList);
            for(int jx = 0; jx < e.size(); jx++)
            {
                int A = e.getEvent(jx);
                // Assuming unique episode types
                Automaton auto = null;
                auto = new Automaton(ix, 1);
                waits.get(A).add(auto);
                epsAutoList.add(auto);
            }
	    e.resetVotes();
            int num_segs = 1;
            if (session.isSegmented()) num_segs = session.getSegIndexLen();
            e.initVotes(num_segs);
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
		
		if (j > 0)
		{
		    auto.setState(j - 1);
                    alpha.incrCounter();
		}
                auto.setLastTransit(event);

                //Expiry check
                if (alpha.getCounter() == alpha.size())
		{
                    List<Automaton> epsAutoList = epsiodeAutoLists.get(alphano);
                    for (int k = 0; k < epsAutoList.size(); k++)
                    {
                        Automaton epsAuto = epsAutoList.get(k);
                        if (t_i - epsAuto.getLastTransit().getStartTime() >= episodeExp)
                        {
                            alpha.decrCounter();
                            epsAuto.setState(j + 1);
                        }
                    }
                }
                
                // Update count
                if (alpha.getCounter() == alpha.size())
		{
		    alpha.incrVotes(session.getCurrentSegIndex());
		    alpha.resetCounter();
                    List<Automaton> epsAutoList = epsiodeAutoLists.get(alphano);
		    int [] etypes = new int[epsAutoList.size()];
		    List<SorterObj> list = new ArrayList<SorterObj>();
                    for (int k = 0; k < epsAutoList.size(); k++)
                    {
                        Automaton epsAuto = epsAutoList.get(k);
			list.add(new SorterObj(alpha.getEvent(k), epsAuto.getLastTransit()));
                        epsAuto.setState(1);
                    }
		    
		    Collections.sort(list);
		    //System.out.println("list = " + list);
                    try {
                        List<IEvent> events = new ArrayList<IEvent>(epsAutoList.size());
                        for (int kx = 0; kx < list.size(); kx++) {
                            SorterObj sorter = list.get(kx);
                            etypes[kx] = sorter.index;
                            events.add(kx, sorter.event);
                        }	
                        observer.handleEpisodeCompletion(alphano, etypes, events);
                    }
                    catch (Exception ex) { 
                        System.out.println("Error with episode completion: " + ex.getMessage());
                    }
		}
	    }
	}
        iterable.setSampleSize(episodes);
         
        observer.shutdown();
	//observer.dispose();
        
        observer.update(sequence.getSize());
    }

    public String getName()
    {
	return "Non-overlapped count with episode expiry constraint(Parallel)";
    }
      
}

