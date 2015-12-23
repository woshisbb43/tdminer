/*
 * ParallelEpisodesCounterWithExpiry.java
 *
 * Created on April 14, 2006, 1:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.IEventIterable;
import edu.iisc.tdminercore.data.ParallelAutomaton;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Deb
 */
public class ParallelEpisodesCounterWithRepeatedEvents extends AbstractParallelEpisodeCounter
{

    static final boolean DEBUG = false; // false to remove debugging

    /** Creates a new instance of ParallelEpisodesCounterWithExpiry */
    public ParallelEpisodesCounterWithRepeatedEvents()
    {
    }

    public void countEpisodes(List<IEpisode> episodes, IObserver observer,
            SessionInfo session)
    {
        IEventDataStream sequence = session.getSequence();
        observer.startup();
        session.resetSegIndex();
        observer.update(0);

        double episodeExp = session.getEpisodeExpiry();
        List<List<ParallelAutomaton>> waits = new ArrayList<List<ParallelAutomaton>>(sequence.getEventTypeCount());

        for (int ix = 0; ix < sequence.getEventTypeCount(); ix++)
        {
            waits.add(new ArrayList<ParallelAutomaton>());
        }

        for (int ix = 0; ix < episodes.size(); ix++)
        {
            IEpisode e = episodes.get(ix);
            ParallelAutomaton auto = new ParallelAutomaton(e, ix);
            //System.out.println(auto);
            int[] indices = auto.getIndices();
            for (int A : indices)
            {
                waits.get(A).add(auto);
            }
            e.resetVotes();
            int num_segs = 1;
            if (session.isSegmented())
            {
                num_segs = session.getSegIndexLen();
            }
            e.initVotes(num_segs);
            e.resetCounter();
        }

        IEventIterable iterable = sequence.iterable(observer);
        for (IEvent event : iterable)
        {
            int E_i = event.getEventType();
            double t_i = event.getStartTime();
            session.updateSegIndex(t_i);

            List<ParallelAutomaton> autoList = waits.get(E_i);
            for (int i = 0; i < autoList.size(); i++)
            {
                ParallelAutomaton auto = autoList.get(i);
                IEpisode alpha = auto.getEpisode();
                auto.addEvent(event);

                //Expiry check
                if (auto.size() == alpha.size())
                {
                    auto.correctTLists(episodeExp);
                }

                // Update count
                if (auto.size() == alpha.size())
                {
                    alpha.incrVotes(session.getCurrentSegIndex());
                    if (session.isTrackEpisodes())
                    {
                        //System.out.println("TRACK " + auto);
                        int alphano = auto.getEpisodeIndex();
                        int[] etypes = new int[alpha.size()];
                        List<SorterObj> list = new ArrayList<SorterObj>();

                        for (ParallelAutomaton.Pair p : auto.getPairs())
                        {
                            int eventIndex = p.getEvent();
                            for (IEvent pEvent : p.getTList())
                            {
                                list.add(new SorterObj(eventIndex, pEvent));
                            }
                        }

                        Collections.sort(list);
                        //System.out.println("list = " + list);
                        try
                        {
                            List<IEvent> events = new ArrayList<IEvent>(alpha.size());
                            for (int kx = 0; kx < list.size(); kx++)
                            {
                                SorterObj sorter = list.get(kx);
                                etypes[kx] = sorter.index;
                                events.add(kx, sorter.event);
                            }
                            observer.handleEpisodeCompletion(alphano, etypes, events);
                        }
                        catch (Exception ex)
                        {
                            System.out.println("Error with episode completion: " + ex.getMessage());
                        }
                    }
                    auto.reset();
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
        //return "Non-overlapped count with episode expiry constraint(Parallel + Repeated)";
        return "Non-overlapped count with episode expiry constraint(Parallel)";
    }
}
