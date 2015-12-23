/*
 * ParallelAprioriCandidateGeneration.java
 *
 * Created on March 19, 2006, 7:27 AM
 *
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.episode.ParallelEpisode;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * The apriori nomination scheme constructs new candidates
 * or order N from prior candidates of order N-1.
 * Given two prior candidates, of order N-1 and identical for 
 * the first N-2 nodes, two new candidates or order N are constructed 
 * identical for the first N-2 nodes.
 * The last two nodes for new candidates are the last node from each of
 * the two candidates.
 * e.g.  
 * {A B C} & {A B D} being the N-1 candidates
 * {A B} being the first N-2 nodes of those candidates with {C} and {D}
 * being the last nodes of the the two N-1 candidates.
 * The constructed one N order candidates is therefore,
 * {A B C D}.
 *
 * @author Deb
 * @author phreed@gmail.com
 */
public class ParallelAprioriCandidateGeneration 
        extends AbstractCandidateGen
{
    /** Creates a new instance of ParallelAprioriCandidateGeneration */
    public ParallelAprioriCandidateGeneration() {}
    public void init(SessionInfo session) 
    {
        GeneratorStateBagged state = new GeneratorStateBagged(1);
        session.setCandidateGeneratorProgress(state);
    }
    public ParallelAprioriCandidateGeneration(SessionInfo session) 
        { init(session); }
   
    @Override
    public List<IEpisode> generateCandidates(List<IEpisode> episodes, 
            IObserver observer, SessionInfo session)
    throws IEpisode.NotImplementedException
    {
        IProgress progress = session.getCandidateGeneratorProgress();
        progress.record(episodes);
        if (!(progress instanceof GeneratorStateBagged)) {
            System.err.println("wrong candidate generator state type");
            return null;  // maybe throwing an exception would be better?
        }
        GeneratorStateBagged state = (GeneratorStateBagged)progress;
        
        observer.startup();
	List<IEpisode> episodesNextGen = new ArrayList<IEpisode>();
	
        if (atBeginning(session))
        {
            Episode.sort(episodes, IEpisode.DICTIONARY_ORDER);
        }
        else 
        {
            while(!state.isBagEmpty() && !hasPassedLimit(episodesNextGen, session))
            {
                episodesNextGen.add(state.popBag());
            }
            if (hasPassedLimit(episodesNextGen, session))
            {
                observer.shutdown();
                return episodesNextGen;
            }
        }
	int len = 0;
	if (episodes != null && episodes.size() != 0)
	{
	    IEpisode e = episodes.get(0);
	    len = e.size();
	}
	
        IX_LOOP:
	while(state.getIx(0) < episodes.size() && !observer.interrupted())
	{
	    int jx = 0;
	    List<Integer> ends = new ArrayList<Integer>();
	    ends.add(episodes.get(state.getIx(0)).getLastEvent());
	    
	    if (state.getIx(0) < episodes.size()-1)
	    {
                FOR_LOOP:
		for (jx = state.getIx(0) + 1; jx < episodes.size() && !observer.interrupted(); jx++)
		{
		    if (episodes.get(state.getIx(0)).matchPrefix(episodes.get(jx)))
		    {
			ends.add(episodes.get(jx).getLastEvent());
		    }
		    else
		    {
			break FOR_LOOP;
		    }
		}
	    }
	    
	    int[] base = episodes.get(state.getIx(0)).getEventTypeIndices();
	    
	    //Generate combinations
	    for (int p = 0; p < ends.size() && !observer.interrupted(); p++)
	    {
		int s1 = ends.get(p).intValue();
		for (int q = p; q < ends.size() && !observer.interrupted(); q++)
		{
		    if (!session.isAllowRepeat() && p == q) continue;
		    int s2 = ends.get(q).intValue();
		    int[] eps = new int[len + 1];
		    for (int r = 0; r < len-1; r++)
		    {
			eps[r] = base[r];
		    }
		    eps[len - 1] = s1;
		    eps[len] = s2;
		    
		    boolean freq = true;
		    if (len > 1)
		    {
			int[] testArr = new int[len];
			IEpisode test = new ParallelEpisode(testArr, session.getEventFactor());
			testArr[len - 2] = s1;
			testArr[len - 1] = s2;
			for (int r = 0; r < len - 1 && !observer.interrupted(); r++)
			{
			    int k = 0;
			    for (int s = 0; s < len - 1 && !observer.interrupted(); s++)
			    {
				if (r == s) continue;
				testArr[k++] = eps[s];
			    }
			    if (!episodes.contains(test))
			    {
				freq = false;
				break;
			    }
			}
		    }
		    if (freq == true)
		    {
			IEpisode e = new ParallelEpisode(eps, session.getEventFactor());
			e.evaluateRequiredVotes(session, null);
			state.pushBag(e);
		    }
		}
	    }
            
            state.setIx(0, (state.getIx(0) < episodes.size()-1) ? jx : state.getIx(0)+1);
	    observer.update(state.getIx(0));
            while(!state.isBagEmpty() && !hasPassedLimit(episodesNextGen, session))
            {
                episodesNextGen.add(state.popBag());
            }
            if (hasPassedLimit(episodesNextGen, session))
            {
                observer.shutdown();
                return episodesNextGen;
            }
	}
        state.markComplete();
	observer.shutdown();
	return episodesNextGen;
    }
    
    /* should the signatures should be in sorted order? */
    public IEpisode specifyCandidate( String signature, SessionInfo session) 
    throws IEpisode.TypeMisMatchException 
    {
        String[] sig = signature.split(":");
        Episode episode = new Episode(sig.length, session.getEventFactor());
	episode.setVotes(new int [session.getSegIndexLen()]);
	
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
	return "Apriori based candidate generation for parallel episodes";
    }
}
