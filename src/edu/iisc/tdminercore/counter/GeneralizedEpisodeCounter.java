/*
 * GeneralizedEpisodeCounter.java
 *
 * Created on July 5, 2006, 10:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.GeneralizedEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.IEventIterable;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.Automaton;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author patnaik
 */
public class GeneralizedEpisodeCounter extends AbstractSerialEpisodeCounter
{  static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of GeneralizedEpisodeCounter */
    public GeneralizedEpisodeCounter()
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
        List<Interval> durationsList = session.getDurationsList();
	if (durationsList == null || durationsList.size() == 0)
	{
	    throw new RuntimeException("This algorithm for generalized episode discovery" +
		    " cannot work without a list of durations");
	}
	if (episodes == null) return;
        if (episodes.size() < 1) return;
	
        IEpisode eTemp = episodes.get(0);
        if (!(eTemp instanceof GeneralizedEpisode))
        {
            throw new RuntimeException("This counting algorithm " +
                    "must be used only for generalized episode discovery with durations");
        }

        List<List<List<Automaton>>> waits = new ArrayList<List<List<Automaton>>>(sequence.getEventTypeCount());
        observer.update(0);
        IEpisode e1 = episodes.get(0);
        for (int i = 0; i < sequence.getEventTypeCount(); i++)
        {
            waits.add(new ArrayList<List<Automaton>>(e1.getDurationsListSize()));
        }
        for (int i=0; i< sequence.getEventTypeCount(); i++)
        {
            for (int j=0; j< e1.getDurationsListSize(); j++)
            {
                waits.get(i).add(new ArrayList<Automaton>());
            }
        }

        for(int i = 0; i < episodes.size(); i++)
        {
            if (episodes.get(i) instanceof GeneralizedEpisode)
            {
                IEpisode e = episodes.get(i);
                e.resetVotes();
                int num_segs = 1;
                if (session.isSegmented()) num_segs = session.getSegIndexLen();
                e.initVotes(num_segs);
                
                int A = e.getFirstEvent();
                int first_duration = e.getDuration(0);
                for( int j=0; j< e.getDurationsListSize(); j++)
                {
                    if ((first_duration & 1) == 1)
                    {
                         if (!session.isTrackEpisodes()) {
                            waits.get(A).get(j).add(new Automaton(i,0));
                         }
                         else
                         {
                            waits.get(A).get(j).add(new Automaton(i,0, e.size()));
                         }
                    }
                    first_duration = first_duration >> 1;
                }
                //waits.get(A).add(n);
            }
            else
            {
                throw new RuntimeException("The generalized episode discovery algorithm can only display episodes discovered by it.\n" +
                        "Please remove any episode entered manually");
            }
        }

        List<Automaton> bag = new ArrayList<Automaton>();
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
            int E_i = event.getEventType();
            double st_i = event.getStartTime();
            double en_i = event.getEndTime();
            session.updateSegIndex(st_i);
            double duration_i = en_i - st_i;
            int duration_indicate;
            boolean flag = false;
            for( duration_indicate =0; duration_indicate < e1.getDurationsListSize(); duration_indicate++)
            {
                Interval iVal = e1.getDurationsList().get(duration_indicate);
                if( duration_i <= iVal.getTHigh() && duration_i >= iVal.getTLow())
                {
                    flag = true;
                    break;
                }
            }
//		System.out.println(e1.getDurationsListSize());
//		System.out.println(E_i);
//		System.out.println(duration_indicate);
            if (flag != true) continue;
            
            List<Automaton> autoList = waits.get(E_i).get(duration_indicate);
            for (int i = 0; i < autoList.size(); i++)
            {
                Automaton auto = (Automaton)autoList.get(i);
                int alphano = auto.getEpisodeIndex();
                IEpisode alpha = episodes.get(alphano);
                int j = auto.getState();
                int this_duration = alpha.getDuration(j);
                for( int k=0; k < e1.getDurationsListSize() ; k++)
                {
                    if ((this_duration & 1) == 1)
                    {
                        waits.get(E_i).get(k).remove(auto);
                    }
                    this_duration = this_duration >> 1;
                }
                i--;
                int j_1 = (j + 1)%alpha.size();

                auto.setLastTransit(event);
                auto.setState(j_1);
                int next_alphano = alpha.getEvent(j_1);
                int next_duration = alpha.getDuration(j_1);
                for( int k=0; k < e1.getDurationsListSize() ; k++)
                {
                    if((next_duration & 1)==1)
                    {

                        if (alpha.getEvent(j_1) == E_i && k==duration_indicate)
                        {
                            bag.add(auto);
                        }
                        else
                        {
                            waits.get(next_alphano).get(k).add(auto);
                        }
                    }
                    next_duration = next_duration>>1;
                }

                if ( j == alpha.size() - 1)
                {
                    alpha.incrVotes(session.getCurrentSegIndex());
                    if (session.isTrackEpisodes())
                    {
                        observer.handleEpisodeCompletion(alphano, alpha.getEventTypeIndices(), auto.getEvents());
                    }
                }

            }

            for(int k=0; k < bag.size(); k++)
            {
                waits.get(E_i).get(duration_indicate).add(bag.get(k));
            }
            bag.clear();

            //double durationStart = ((Interval)durationsList.get().getTHigh();
            //double durationEnd = ((Interval)durationsList.get().getTLow();
        }
	
        iterable.setSampleSize(episodes);
         
        observer.shutdown();
//	observer.dispose();
	observer.update(sequence.getSize());
	
    }
    
    // Principal episode check
    @Override
    public List<IEpisode> postCountProcessing(List<IEpisode> episodes,  IObserver observer, SessionInfo session)
            throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
	System.out.println("Principality check for episodes");
	List<IEpisode> retVal = new ArrayList<IEpisode>();
        int len = 0;
	if (episodes != null && episodes.size() != 0)
	{
	    Episode.sort(episodes, IEpisode.DICTIONARY_FREQ_ORDER);
	    IEpisode e = episodes.get(0);
	    len = e.size();
            observer.setExtent(episodes.size());
            observer.startup();
	    int i = 0;
	    while(i < episodes.size() && !observer.interrupted())
	    {
		int j = 0;
                IEpisode genEpisode1 = episodes.get(i);
		if (i < episodes.size()-1)
		{
		    // Finding a block of similar episodes
		    for (j = i + 1; j < episodes.size() && !observer.interrupted(); j++)
		    {
			IEpisode genEpisode2 = episodes.get(j);
			if (!genEpisode1.isSimilar(genEpisode2, session.getCurrentSegIndex())) break;
		    }
		    //System.out.println("i = " + i + " j = " + j);
		    
		    // Eliminating non-principal episodes from the block
		    for(int k = i; k < j; k++)
		    {
			IEpisode genEpisode_k = episodes.get(k);
			if (genEpisode_k.isDeleted()) continue;
			for(int l = k + 1; l < j; l++)
			{
			    IEpisode genEpisode_l = episodes.get(l);
			    if (genEpisode_l.isDeleted()) continue;
			    int nonpricipal = genEpisode_k.findNonPrincipal(genEpisode_l);
			    if (nonpricipal == 1)
			    {
				genEpisode_k.setDeleted(true);
			    }
			    else if (nonpricipal == 2)
			    {
				genEpisode_l.setDeleted(true);
			    }
			}
		    }
		    // Moving to next block start
		    i = j;
		}
		else
		{
		    i++;
		}
                if (observer.update(i)) break;
            }
	    
            observer.update(episodes.size());
            observer.shutdown();

	    for (int k = 0; k < episodes.size(); k++)
	    {
		IEpisode genEpisode = episodes.get(k);
		if (!genEpisode.isDeleted())
		{
		    retVal.add(genEpisode);
		}
	    }
	    
	}
        
        return (List<IEpisode>)retVal;
    }
    
    
    
    public String getName()
    {
	return "Discovery of generalized episodes(Serial)";
    }
}
