/*
 * AbstractCandidateGen.java
 *
 * Created on March 17, 2006, 11:11 AM
 *
 */

package edu.iisc.tdminercore.candidate;

import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.EventFactor;

import java.util.List;
import java.util.ArrayList;

/**
 * Abstract candidate generator. This defines the interface for all candidate generators.
 * Hence all candidate generators must extend this abstract class.
 * @author Debprakash Patnaik <debprakash@gmail.com>
 * @author Fred Eisele <phreed@gmail.com>
 */
public abstract class AbstractCandidateGen 
{
   
    /**
     * When the candidate generator is ready to be used it needs to have
     * certain stateful information set in the session object.
     * @param session
     */
    public abstract void init(SessionInfo session);
    
    /**
     * This method takes a list of N-node frequent episodes and generated the next level 
     * (N+1)-node candidate episodes.
     * @param episodes List of frequent episodes
     * @param observer The state observer for candidate generation
     * @param session The session object for the candidate generator. Contains session variables like 
     * the current event sequence.
     * @return A list of next level candidate episodes
     * @throws edu.iisc.tdminercore.data.IEpisode.NotImplementedException This exception is thrown by candidate generators under-construction.
     */
    public abstract List<IEpisode> generateCandidates(List<IEpisode> episodes, 
            IObserver observer, SessionInfo session) 
    throws IEpisode.NotImplementedException;
    
     /**
     * This method takes a list of event type names and constructs an episode.
     * @param sig a list of event names
     * @param observer The state observer for candidate generation
     * @param session The session object for the candidate generator. Contains session variables like 
     * the current event sequence.
     * @return a candidate episode
     * @throws edu.iisc.tdminercore.data.IEpisode.NotImplementedException This exception is thrown by candidate generators under-construction.
     */
    public abstract IEpisode specifyCandidate( String sig, SessionInfo session) 
    throws IEpisode.TypeMisMatchException;
    
    /** 
     * Similar to selectCandidate except that the candidate episode is added to
     * a list rather than returned as a single episode.
     * @param sig a list of event names
     * @param observer The state observer for candidate generation
     * @param session The session object for the candidate generator. Contains session variables like 
     * the current event sequence.
     * @return a candidate episode
     */
    public List<IEpisode> updateCandidates(List<IEpisode> list, String signature,  SessionInfo session) 
    throws IEpisode.TypeMisMatchException 
    {
        if (list == null) { list = new ArrayList<IEpisode>(1); }
        list.add( this.specifyCandidate(signature,session) );
        return list;
    }
    
   
    /**
     * The name of the candidate generator
     * @return a string name describing the candidate generator.
     */
    public abstract String getName();
    
    /**
     * This is used to internally check if the chunk of candidates required for
     * the current pass has been generated already.
     * @param episodelist This is the candidate episodes in the current chunk. 
     * @param session this contains the current session information like the user specified chunking size
     * for the candidates to be generated.
     * @return true if the size of the list of candidates if equal to the user specified chunking
     * limit specified in the session object. The candidate generator returns when enough
     * number of candidates are generated for current pass.
     */
    protected boolean hasPassedLimit(List<IEpisode> episodelist, SessionInfo session) {
        int limit = session.getChunkLimit();
        if (limit < 1) return false;
        if (episodelist.size() < limit) return false;
        return true;
    }
    
    /**
     * Resets the internal counters of the candidate generator which remembers the 
     * point in the list of frequent episodes provided as input. This needs to called
     * before giving a new set frequent episodes for generating the next level of 
     * candidates.
     */
    public void reset(SessionInfo session) {
        session.getCandidateGeneratorProgress().reset();
    }
    
    /**
     * Indicates whether the candidate generator is at the begining of the list of 
     * input frequent episodes. This is required for the chunking feature where the 
     * candidate generator generates the candidate episodes in chunks of fixed sizes
     * and remembers where it left off before generating the next set of candidates.
     * @return true if the candidate generator is at the begining of the list of frequent
     * episodes from which it generates candidate episodes of the next level
     */
     public boolean atBeginning(SessionInfo session) {
        return session.getCandidateGeneratorProgress().atBeginning();
    }
    
}
