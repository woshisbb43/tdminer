/*
 * PrefixSuffixMatchCandidateGen.java
 *
 * Created on March 19, 2006, 7:28 AM
 *
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.episode.SerialEpisode;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Deb
 * @author phreed@gmail.com
 */
public class PrefixSuffixMatchCandidateGen 
        extends AbstractCandidateGen
{
    
    /** Creates a new instance of PrefixSuffixMatchCandidateGen */
    public PrefixSuffixMatchCandidateGen() {}
    public void init(SessionInfo session)
    {
        GeneratorStateBagless state = new GeneratorStateBagless(2);
        session.setCandidateGeneratorProgress(state);
    }
    public PrefixSuffixMatchCandidateGen(SessionInfo session) 
        { init(session); }

    /**
     * This doesn't necessarily generate all the candidates.
     * It generates the next 'limit' candidates.
     * It remembers where it left off and may be called repeatedly.
     */
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
        
        List<IEpisode> episodesNextGen = new ArrayList<IEpisode>();
        
        // assumes that all episodes in episodes have same length .. = len
        int len = 0;
        if (episodes != null && episodes.size() != 0)
        {
            IEpisode e = episodes.get(0);
            len = e.size();
        }
        observer.startup();
        IEpisode alpha = null;
        IEpisode beta = null;
        
        IX_LOOP:
        while (state.getIx(0) < episodes.size() && !observer.interrupted())
        {
            alpha = episodes.get(state.getIx(0));

            JX_LOOP:
            while (state.getIx(1) < episodes.size() && !observer.interrupted())
            {
                beta = episodes.get(state.getIx(1));
                
                // Check for repeated events
                if (!session.isAllowRepeat() && alpha.getFirstEvent() == beta.getLastEvent())
                {
                    state.incrIx(1);
                    continue JX_LOOP;
                }

                // Prefix - Suffix check
                for (int mx = 1; mx < len && !observer.interrupted(); mx++)
                {
                    if (alpha.getEvent(mx) != beta.getEvent(mx - 1))
                    {
                        state.incrIx(1);
                        continue JX_LOOP;
                    }
                }

                IEpisode gamma = new SerialEpisode(len + 1, session.getEventFactor());
                for (int nx=0; nx < len && !observer.interrupted(); nx++)
                {
                    gamma.setEvent(nx, alpha.getEvent(nx));
                }
                gamma.setEvent(len, beta.getLastEvent());
                
                Object[] params = new Object[2];
                params[0] = alpha;
                params[1] = beta;
                gamma.evaluateRequiredVotes(session, params);
                
                episodesNextGen.add(gamma);
                state.incrIx(1);

                if (hasPassedLimit(episodesNextGen, session))
                {
                    observer.shutdown();
                    return episodesNextGen;
                }
            }
            // if the entire set of candidates were generated then reset the JX index
            observer.update(state.getIx(0));
            state.reset(1);
            state.incrIx(0);
        }
        // if the entire set of candidates were generated then reset the IX index
        state.reset(0);
        state.markComplete();
        observer.shutdown();
        return episodesNextGen;
    }
    
    /**
     * This selection parses its string into a list of event type names.
     * It expects a episode signature of the form...
     *   A ->  B ->C 
     * Where the event types are separated by arrows, '->'.
     * The blanks surrounding the event types are trimmed away.
     * @param signature the episode definition
     * @param session provides the event factor
     * @return a new episode 
     */
    public IEpisode specifyCandidate( String signature, SessionInfo session) 
    throws IEpisode.TypeMisMatchException 
    {
        String[] sig = signature.split("->");
        
        IEpisode episode = new SerialEpisode(sig.length, session.getEventFactor());
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
        return "Prefix and suffix match based candidate generation";
    }
}
