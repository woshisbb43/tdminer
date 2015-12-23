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
 * @author patnaik
 * @author phreed@gmail.com
 */
public class GeneralizedEpisodeCounterWithExpiry extends AbstractSerialEpisodeCounter
{   static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of GeneralizedEpisodeCounter */
    public GeneralizedEpisodeCounterWithExpiry()
    {
    }
    
    @Override
    public void countEpisodes(List<IEpisode> episodes, IObserver observer, 
            SessionInfo session)
        throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        IEventDataStream sequence = session.getSequence();
        List<Interval> durationsList = session.getDurationsList();
        double episodeExp = session.getEpisodeExpiry();
        
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
        observer.startup();
        session.resetSegIndex();

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
                    if ((first_duration & 1) == 1) {
                        if (!session.isTrackEpisodes()) {
                            waits.get(A).get(j).add(new Automaton(i,0) );
                        } else {
                            waits.get(A).get(j).add(new Automaton(i,0,e.size()) );
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
            double t_i = event.getStartTime();
            session.updateSegIndex(t_i);
            long srcEventId = event.getSourceId();
            double st_i = event.getStartTime();
            double en_i = event.getEndTime();
            double duration_i = en_i - st_i;
            int duration_indicate;
            boolean flag = false;
            for( duration_indicate = 0; duration_indicate < e1.getDurationsListSize(); duration_indicate++)
            {
                Interval iVal = e1.getDurationsList().get(duration_indicate);
                if( duration_i <= iVal.getTHigh() && duration_i >= iVal.getTLow())
                {
                    flag = true;
                    break;
                }
            }
            
            if(flag != true) continue;
        
            List<Automaton> autoList = waits.get(E_i).get(duration_indicate);
		
	    for (int ix = 0; ix < autoList.size(); ix++)
	    {
                Automaton auto = autoList.get(ix);
                Automaton next_auto = null;
                int alphano = auto.getEpisodeIndex();
		IEpisode alpha = episodes.get(alphano);
		int j = auto.getState();
		int this_duration = alpha.getDuration(j);
/*
9:		if j = 1 then
10:		    Update alpha.init[1] = t_i
11:		else
12:		    Update alpha.init[j] = alpha.init[j-1]
13:		    Remove (alpha,j) from waits(E_i)
 */
		if (j == 0)
		{
		    //alpha.setInit(0, st_i);
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
                  for( int k=0; k < e1.getDurationsListSize() ; k++) {
                      if ((this_duration & 1) == 1) {
                          waits.get(E_i).get(k).remove(next_auto);
                      }
                      this_duration = this_duration >> 1;
                  }
                  // The list is being modified hence index
                  // must be kept at the same location
                  ix--;
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
                if (j < alpha.size() - 1) {
                    int next_duration = alpha.getDuration(j+1);
                    for( int k=0; k < e1.getDurationsListSize() ; k++) {
                        if((next_duration & 1)==1) {
                            if (alpha.getEvent(j + 1) == E_i && k==duration_indicate) {
                                while(bag.contains(next_auto)) {
                                    bag.remove(next_auto);
                                }
                                bag.add(next_auto);
                            } else {
                                
                                
                                List<Automaton> waitsList = waits.get(alpha.getEvent(j + 1)).get(k);
                                while(waitsList.contains(next_auto)) {
                                    waitsList.remove(next_auto);
                                }
                                waitsList.add(next_auto);
                                
                            }
                        }
                       next_duration = next_duration>>1; 
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
		//if ((j == alpha.size() - 1) && (st_i - alpha.getInit(j)) <= episodeExp)
                if ((j == alpha.size() - 1) && (st_i - next_auto.getStartEvent().getStartTime()) <= episodeExp)
		{
		    alpha.incrVotes(session.getCurrentSegIndex());
		    for (int k = 1; k < alpha.size(); k++)
		    {
			int E_k = alpha.getEvent(k);
                        int duration_k = alpha.getDuration(k);
                        for( int n=0; n < e1.getDurationsListSize() ; n++) {
                            if((duration_k & 1)==1) {
                               List<Automaton> list = waits.get(E_k).get(n);
			
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
                            duration_k = duration_k >> 1;
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
                    {
                        observer.handleEpisodeCompletion(alphano, alpha.getEventTypeIndices(), auto.getEvents());
                    }
		}
	    }//for
/*
20:		Empty bag into waits(Ei)
 */
	    List<Automaton> bagList = waits.get(E_i).get(duration_indicate);
	    bagList.addAll(bag);
	    bag.clear();
           
	}
        iterable.setSampleSize(episodes);
        
        observer.shutdown();
//	observer.dispose();
        observer.update(sequence.getSize());
    }
    
    /**
     * Principal episode check
     */
    @Override
    public List<IEpisode> postCountProcessing(List<IEpisode> episodes, IObserver observer, SessionInfo session)
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
        
        return retVal;
    }
    
    
    
    public String getName()
    {
	return "Discovery of generalized episodes with expiry constraint(Serial)";
    }
}
