/*
 * GeneralizedEpisodeCandidateGeneration.java
 *
 * Created on July 5, 2006, 10:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.GeneralizedEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import edu.iisc.tdminercore.util.PickMatrix;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 * @author phreed@gmail.com
 */
public class GeneralizedEpisodeCandidateGeneration 
        extends AbstractCandidateGen
{ 
    /** Creates a new instance of AprioriCandidateGeneration */
    public GeneralizedEpisodeCandidateGeneration() {}
    public void init(SessionInfo session)
    {
        GeneratorStateBagless state = new GeneratorStateBagless(1);
        session.setCandidateGeneratorProgress(state);
    }
    public GeneralizedEpisodeCandidateGeneration(SessionInfo session) 
        { init(session); }
    
    @Override
    public List<IEpisode> generateCandidates(List<IEpisode> episodes, 
            IObserver observer, SessionInfo session)
    throws IEpisode.NotImplementedException
    {
        IProgress progress = session.getCandidateGeneratorProgress();
        progress.record(episodes);
        if (!(progress instanceof GeneratorStateBagless)) {
            System.err.println("wrong candidate generator state type");
            return null;  // maybe throwing an exception would be better?
        }
        GeneratorStateBagless state = (GeneratorStateBagless)progress;
        
        List<Interval> durationsList = session.getDurationsList();
	if (durationsList == null || durationsList.size() == 0)
	{
	    throw new RuntimeException("This algorithm for generalized episode discovery" +
		    " cannot work without a list of durations");
	}
	observer.startup();
	List<IEpisode> episodesNextGen = new ArrayList<IEpisode>();
	int len = 0;
	
	if (episodes != null && episodes.size() != 0)
	{
	    Episode.sort(episodes, IEpisode.DICTIONARY_ORDER);
	    IEpisode e = episodes.get(0);
	    len = e.size();
	}
	if (len == 0)
	{
	    for (int jx = 0; jx < session.getEventFactor().getSize() && !observer.interrupted(); jx++)
	    {
                if (session.isDurationSingle())
                {
                    int index = 1;
                    for (int kx = 0; kx < durationsList.size() && !observer.interrupted(); kx++)
                    {
                        IEpisode gamma = new GeneralizedEpisode(1, session.getEventFactor(), durationsList);
                        gamma.setEvent(0, jx);
                        gamma.setDurationMap(0, index);
                        episodesNextGen.add(gamma);
                        System.out.println(gamma.toString());
                         if (hasPassedLimit(episodesNextGen, session)) {
                            observer.shutdown();
                            return episodesNextGen;
                        }
                        index = index << 1;
                    }
                }
                else
                {
                    /**
                     * if the durations list has N elements then there will
                     * be 2^N combinations.
                     */
                    int pow = 1 << durationsList.size();
                    for (int kx = 1; kx < pow && !observer.interrupted(); kx++)
                    {
                        IEpisode gamma = new GeneralizedEpisode(1, session.getEventFactor(), durationsList);
                        gamma.setEvent(0, jx);
                        gamma.setDurationMap(0, kx);
                        episodesNextGen.add(gamma);
                        if (hasPassedLimit(episodesNextGen, session)) {
                            observer.shutdown();
                            return episodesNextGen;
                        }
                    }
                }
	    }
	}
	else
	{
	    int i = 0;
	    while(i < episodes.size() && !observer.interrupted())
	    {
		int j = 0;
		List<EventDurationPair> ends = new ArrayList<EventDurationPair>();
		int eventType = episodes.get(i).getLastEvent();
		int duration = episodes.get(i).getLastDuration();
		ends.add(new EventDurationPair(eventType, duration));
		
		if (i < episodes.size()-1)
		{
		    for (j = i + 1; j < episodes.size() && !observer.interrupted(); j++)
		    {
			if (episodes.get(i).matchPrefix(episodes.get(j)))
			{
			    eventType = episodes.get(j).getLastEvent();
			    duration = episodes.get(j).getLastDuration();
			    ends.add(new EventDurationPair(eventType, duration));
			}
			else
			{
			    break;
			}
		    }
		}
		
		int[] base = episodes.get(i).getEventTypeIndices();
		PickMatrix<Interval> baseDurations = episodes.get(i).getDurations();
		//Generate combinations
		for (int p = 0; p < ends.size() && !observer.interrupted(); p++)
		{
		    int s1 = ((EventDurationPair)ends.get(p)).getEventTypeId();
		    int d1 = ((EventDurationPair)ends.get(p)).getDuration();
		    for (int q = 0; q < ends.size() && !observer.interrupted(); q++)
		    {
			if (!session.isAllowRepeat() && p == q) continue;
			int s2 = ((EventDurationPair)ends.get(q)).getEventTypeId();
			int d2 = ((EventDurationPair)ends.get(q)).getDuration();
			int[] eps = new int[len + 1];
			int[] durations = new int[len + 1];
			for (int r = 0; r < len - 1; r++)
			{
			    eps[r] = base[r];
			    durations[r] = baseDurations.getMap(r);
			}
			eps[len - 1] = s1;
			eps[len] = s2;
			durations[len - 1] = d1;
			durations[len] = d2;
			if (len == 2 && eps[0] == 0 && eps[1] == 1 && eps[2] == 2)
                        {
                            System.out.println(new GeneralizedEpisode(eps, session.getEventFactor(), durations, durationsList).toString());
                        }
			boolean freq = true;
			if (len > 1)
			{
			    int[] testArr = new int[len];
			    int[] testDurations = new int[len];
			    testArr[len - 2] = s1;
			    testArr[len - 1] = s2;
			    testDurations[len - 2] = d1;
			    testDurations[len - 1] = d2;
			    
			    for (int r = 0; r < len - 1 && !observer.interrupted(); r++)
			    {
				int k = 0;
				for (int s = 0; s < len - 1 && !observer.interrupted(); s++)
				{
				    if (r == s) continue;
				    testArr[k] = eps[s];
				    testDurations[k] = durations[s];
				    k++;
				}
                                IEpisode test = new GeneralizedEpisode(testArr, session.getEventFactor(), testDurations, durationsList);
				if (!episodes.contains(test))
				{
				    freq = false;
				    break;
				}
			    }
			}
			if (freq == true)
			{
			    IEpisode e = new GeneralizedEpisode(eps, session.getEventFactor(), durations, durationsList);
			    if (d1 == 0 || d2 == 0)
			    {
				System.out.println("Error on episode : " + e.toString(session.getEventFactor()) + " [ " + e.toString() + " ]");
			    }
			    
			    episodesNextGen.add(e);
                            if (hasPassedLimit(episodesNextGen, session)) 
                            {
                                observer.shutdown();
                                return episodesNextGen;
                            }
			}
		    }
		}
		if (i < episodes.size()-1)
		{
		    i = j;
		}
		else
		{
		    i++;
		}
		
		observer.update(i);
	    }
	}
	observer.shutdown();
	return episodesNextGen;
    }
    
    public IEpisode specifyCandidate( String signature, SessionInfo session) 
    throws IEpisode.TypeMisMatchException 
    {
        String[] sig = signature.split("->");
         
        List<Interval> durationsList = session.getDurationsList();
        IEpisode episode = new GeneralizedEpisode(sig.length, session.getEventFactor(), durationsList);
	episode.setVotes(0,0);
	
	for(int ix = 0; ix < sig.length; ix++)
	{
	    int eventType = session.getEventFactor().getId(sig[ix].trim());
	    if (eventType < 0)
	    {
		throw new IEpisode.TypeMisMatchException("Event Type mismatch in episode set and available sequence");
	    }
	    episode.setEvent(ix, eventType);
	}
	return episode;
    }
     
    public String getName()
    {
	return "Apriori based candidate generation";
    }
   
}

class EventDurationPair
{
    private int eventType;
    private int duration;
    
    public EventDurationPair(int eventType, int duration)
    {
	this.eventType = eventType;
	this.duration = duration;
    }
    
    public int getEventTypeId()
    {
	return eventType;
    }
    
    public int getDuration()
    {
	return duration;
    }
}
