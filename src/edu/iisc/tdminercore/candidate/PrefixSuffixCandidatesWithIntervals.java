/*
 * PrefixSuffixCandidatesWithIntervals.java
 *
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.episode.SerialEpisodeWithIntervals;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 * @author phreed@gmail.com
 */
public class PrefixSuffixCandidatesWithIntervals
        extends AbstractCandidateGen
{
    /** Creates a new instance of PrefixSuffixMatchCandidateGen */
    public PrefixSuffixCandidatesWithIntervals() {}
    public void init(SessionInfo session)
    {
        GeneratorStateBagless state = new GeneratorStateBagless(3);
        session.setCandidateGeneratorProgress(state);
    }
    public PrefixSuffixCandidatesWithIntervals(SessionInfo session) 
        { init(session); }
    
    public String getName()
    {
        return "Prefix and suffix match based candidate generation";
    }
    
    /*
     * Some episodes contain an interval while other do not.
     * Essencially, when an interval is not specified it may be
     * presumed to be equivalent to an interval [0 +Inf].
     * When an interval list is provided it is to be used to further constrain.
     * When an interval is further constrained in may result in multiple candidates.
     * e.g.  A-[0 +Inf]->B constrained by [1 4] and [5 12] results in
     * A-[1 4]->B and A-[5 12]->B.
     * Note that A-[5 12]->B constrained by [1 10] results in A-[5 10]->B.
     */
    
    /**
     * When episodes are generated from episodes, no constraining intervals are required.
     * If no intervals are provided then the presumed interval is [0 +Inf].
     */
    @Override
    public List<IEpisode> generateCandidates(List<IEpisode> episodes, 
            IObserver observer, SessionInfo session)
    throws IEpisode.NotImplementedException
    { 
        session.getCandidateGeneratorProgress().record(episodes);
        
        List<Interval> intervalsList = session.getIntervalsList();
        List<IEpisode> episodesNextGen = null;
        if (episodes == null) {
            return (List<IEpisode>)null;
        }

        if (intervalsList == null || intervalsList.size() == 0)
        {
            throw new RuntimeException("This algorithm for interval discovery" +
                    " cannot work without a list of intervals");
        }
        
        // assumes that all episodes in episodes have same length .. = len
        int len = 0;
        
        if (episodes != null && episodes.size() != 0)  {
            IEpisode e = episodes.get(0);
            len = e.size();
            // generate from event types
            switch (len) {
                case 1:
                    episodesNextGen = this.generateBinaryCandidates(episodes,observer,session);
                    break;
                default:
                    episodesNextGen = this.generateNaryCandidates(episodes,observer,session);
                    break;
            }
        }
        return episodesNextGen;
    }
    
    private List<IEpisode> generateBinaryCandidates(List<IEpisode> episodes, IObserver observer, SessionInfo session)
    
    {
        IProgress progress = session.getCandidateGeneratorProgress();
        if (!(progress instanceof GeneratorStateBagless)) {
            System.err.println("wrong candidate generator state type");
            return null;  // maybe throwing an exception would be better?
        }
        GeneratorStateBagless state = (GeneratorStateBagless)progress;
        
        observer.startup();
        List<IEpisode> episodesNextGen = new ArrayList<IEpisode>();
        List<Interval> intervalsList = session.getIntervalsList();
        
        while (state.getIx(0) < episodes.size() && !observer.interrupted())
        {
            IEpisode alpha = episodes.get(state.getIx(0));
            while (state.getIx(1) < episodes.size() && !observer.interrupted())
            {
                if (!session.isAllowRepeat() && (state.getIx(0) == state.getIx(1))) 
                {
                    state.incrIx(1);
                    continue;
                }

                IEpisode beta = episodes.get(state.getIx(1));

                while (state.getIx(2) < intervalsList.size() && !observer.interrupted())
                {
                    IEpisode gamma = new SerialEpisodeWithIntervals(2,session.getEventFactor(), intervalsList);
                    gamma.setEvent(0, alpha.getEvent(0));
                    gamma.setIntervalId(0, state.getIx(2));
                    gamma.setEvent(1, beta.getEvent(0));
		            //gamma.setEstr(0, 0.5);
		    
                    
                    Object[] params = new Object[2];
                    params[0] = alpha;
                    params[1] = beta;
                    gamma.evaluateRequiredVotes(session, params);

                    episodesNextGen.add(gamma);
                    state.incrIx(2);

                    if (hasPassedLimit(episodesNextGen, session)) {
                        observer.shutdown();
                        state.incrIx(2);
                        return episodesNextGen;
                    }
                }
                state.reset(2);
                state.incrIx(1);
            }
            state.reset(1);
            state.incrIx(0);
        }
        state.reset(0);
        state.markComplete();
        observer.shutdown();
        return episodesNextGen;
    }
    
    /**
     * When episodes are generated from episodes, no additional constraining
     * intervals are required, the intervals from the episodes from which
     * the new episodes are built are enlisted.
     */
    private List<IEpisode> generateNaryCandidates(List<IEpisode> episodes, IObserver observer, SessionInfo session)
    {
        IProgress progress = session.getCandidateGeneratorProgress();
        if (!(progress instanceof GeneratorStateBagless)) {
            System.err.println("wrong candidate generator state type");
            return null;  // maybe throwing an exception would be better?
        }
        GeneratorStateBagless state = (GeneratorStateBagless)progress;
        
        observer.startup();
        List<IEpisode> episodesNextGen = new ArrayList<IEpisode>();
        List<Interval> intervalsList = session.getIntervalsList();

        IX_LOOP:
        while (state.getIx(0) < episodes.size() && !observer.interrupted())
        {
            IEpisode alpha = episodes.get(state.getIx(0));
            int len = alpha.size();
            
            JX_LOOP:
            while (state.getIx(1) < episodes.size() && !observer.interrupted())
            {
                IEpisode beta = episodes.get(state.getIx(1));

                // Check for repeated event types
                if (!session.isAllowRepeat() && beta.getLastEvent() == alpha.getFirstEvent()) {
                    state.incrIx(1);
                    continue JX_LOOP;
                }

                // Prefix - Suffix check
                for (int mx = 1; mx < len && !observer.interrupted(); mx++)
                {
                    if (alpha.getEvent(mx) != beta.getEvent(mx - 1)) {
                        state.incrIx(1);
                        continue JX_LOOP;
                    }

                    if (mx > 1 && (alpha.getIntervalId(mx - 1) != beta.getIntervalId(mx - 2))) {
                        state.incrIx(1);
                        continue JX_LOOP;
                    }
                }

                IEpisode gamma = new SerialEpisodeWithIntervals(len + 1, session.getEventFactor(), beta.getIntervalsList());
                for (int mx = 0; mx < len; mx++)
                {
                    if (mx > 0) {
                        gamma.setIntervalId(mx - 1, alpha.getIntervalId(mx - 1));
			//gamma.setEstr(mx - 1, alpha.getEstr(mx - 1));
                    }
                    gamma.setEvent(mx, alpha.getEvent(mx));
                }
                gamma.setIntervalId(len - 1, beta.getIntervalId(len - 2));
		//gamma.setEstr(len - 1, beta.getEstr(len - 2));
                gamma.setEvent(len, beta.getEvent(len - 1));
                
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
            observer.update(state.getIx(0));
            state.reset(1);
            state.incrIx(0);
        }
        state.reset(0);
        state.markComplete();
        observer.shutdown();
        return episodesNextGen;
    }
    
    public IEpisode specifyCandidate( String signature, SessionInfo session ) 
    throws IEpisode.TypeMisMatchException 
    {
        String[] sig = signature.split(":");
        
        List<Interval> intervalsList = session.getIntervalsList();
        IEpisode episode = new SerialEpisodeWithIntervals(sig.length, session.getEventFactor(), intervalsList);
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

}

