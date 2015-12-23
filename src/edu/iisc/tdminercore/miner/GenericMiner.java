/*
 * GenericMiner.java
 *
 * Created on March 19, 2006, 6:36 PM
 *
 */

package edu.iisc.tdminercore.miner;

import edu.iisc.tdminercore.candidate.AbstractCandidateGen;
import edu.iisc.tdminercore.candidate.GeneralizedEpisodeCandidateGeneration;
import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;
import edu.iisc.tdminercore.counter.GeneralizedEpisodeCounter;
import edu.iisc.tdminercore.counter.GeneralizedEpisodeCounterWithExpiry;
import edu.iisc.tdminercore.filter.AbstractFrequencyThreshold;
import edu.iisc.tdminercore.filter.DecayingFrequencyThreshold;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.filter.NegativeStrengthThreshold;
import edu.iisc.tdminercore.filter.NewAdaptiveFrequencyThreshold;
import edu.iisc.tdminercore.util.IObserver;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 */ 
public class GenericMiner
{   static final boolean DEBUG = false; // false to remove debugging
    
    private boolean run = false;
    
    /** Creates a new instance of GenericMiner */
    public GenericMiner()
    {}
    
    /** 
     * The count episodes thread and method are typically called when
     * loaded episodes are being counted.
     */
    public void countEpisodesThread(final EpisodeSet anepisodes, 
            final IObserver anobserver, final SessionInfo session)
    {
        if (DEBUG) System.out.println("GenericMiner: count episode thread");
        Thread t = new Thread( 
            new Runnable() {
                public void run() {
                    try {
                        countEpisodes(anepisodes, anobserver, session);
                    }
                    catch (Exception re)
                    {
                        System.err.println("Printing stacktrace of exception in TDMiner");
                        re.printStackTrace();
                        anobserver.exceptionOccured(re);
                    }
                }
            }, "Counter Thread");
        t.start();
    }
    
    public void countEpisodes(EpisodeSet anepisodes, 
            IObserver observer, SessionInfo session)
        throws Exception
    {
        IEventDataStream sequence = session.getSequence();
        observer.setConstraintMode(IEventDataStream.CONSTRAINT_MODE.PROSPECT);
        try {
            if (DEBUG) System.out.println("GenericMiner: count episodes");
            this.run = true;
            observer.taskStarted();
            
            for (int ix = 1; ix <= anepisodes.maxEpisodeSize(); ix++)
            {
                List<IEpisode> candidates = anepisodes.getEpisodeList(ix);
                if (candidates != null)
                {
                    observer.setTitle(this.getTitle(ix,candidates));
                    observer.setExtent(sequence.getSize());
                    if (ix == 1)
                    {
                        session.getCounter().countEvents(candidates, observer, session);
                    }
                    else
                    {
                        session.getCounter().countEpisodes(candidates, observer, session);
                    }
                    candidates = EpisodeSet.postProcess(candidates, session);
                }
            }
            observer.taskComplete();
            this.run = false;
        }
        catch (RuntimeException re)
        {
            System.err.println("Printing stacktrace of exception in TDMiner");
            re.printStackTrace();
            observer.exceptionOccured(re);
        }
    }
    
    /** 
     * The count episodes thread and method are typically called when
     * havesting in being performed.
     */
    public void countListEpisodesThread(final List<IEpisode> anepisodesList, 
            final IObserver observer, final SessionInfo session)
    {
        final IEventDataStream sequence = session.getSequence();
        observer.setConstraintMode(IEventDataStream.CONSTRAINT_MODE.EXTRACT);
        if (DEBUG) System.out.println("GenericMiner: count list episodes");
        
        Thread t = new Thread( 
            new Runnable() {
                public void run() {
                    try {
                        List<IEpisode> candidates = anepisodesList;
                        GenericMiner.this.run = true;
                        int level = 0;
                        observer.setTitle(GenericMiner.this.getTitle(level,candidates));
                        observer.setExtent(sequence.getSize());
                        session.getCounter().countEpisodes(candidates, observer, session);
                        observer.taskComplete();
                        GenericMiner.this.run = false;
                    }
                    catch (RuntimeException re)
                    {
                        System.err.println("Printing stacktrace of exception in TDMiner");
                        re.printStackTrace();
                        observer.exceptionOccured(re);
                    }
                    catch (Exception re)
                    {
                        System.err.println("Printing stacktrace of exception in TDMiner");
                        re.printStackTrace();
                        observer.exceptionOccured(re);
                    }
                }
            }, "List Counter Thread");
       
        t.start();
    }
     
    /**
     * Mining always implies discovery/prospecting.
     */  
    public void mineSequenceThread(final IObserver observer, final SessionInfo session)
    {
        if (DEBUG) System.out.println("GenericMiner: mine sequence thread");
        final IEventDataStream sequence = session.getSequence();
        final Thread t = new Thread( 
            new Runnable() {
                public void run() {
                    try {
                        if (DEBUG) System.out.println("GenericMiner: mine sequence");
                        mineSequence(observer,session);
                    }
                    catch (OutOfMemoryError ome)
                    {
                        System.gc();
                        Thread.yield();
                        System.err.println("Printing stacktrace of exception in TDMiner");
                        ome.printStackTrace();
                        observer.exceptionOccured(new Exception("Out of Memory !!!"));
                    }
                    catch (RuntimeException re)
                    {
                        System.err.println("Printing stacktrace of exception in TDMiner");
                        re.printStackTrace();
                        observer.exceptionOccured(re);
                    }
                    catch (Exception re)
                    {
                        System.err.println("Printing stacktrace of exception in TDMiner");
                        re.printStackTrace();
                        observer.exceptionOccured(re);
                    }
                }
            }, "Miner Thread");
        t.start();
        
        Thread f = new Thread(new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    boolean flag = t.isAlive();
                    if (!flag) break;
                    try { Thread.sleep(5000); } catch (InterruptedException ex) { ex.printStackTrace(); }
                }
                System.out.println("Alive = " + t.isAlive());
            }
            
        });
        f.start();
    }

    public void mineSequence(IObserver observer, SessionInfo session)
        throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        observer.setConstraintMode(IEventDataStream.CONSTRAINT_MODE.PROSPECT);
        IEventDataStream sequence = session.getSequence();
        
        if (DEBUG) System.out.println("GenericMiner: mine sequence");
        this.run = true;
        observer.taskStarted();

        List<IEpisode> candidates = null;
        EpisodeSet episodes = new EpisodeSet();
        session.setEpisodes(episodes);
        EventFactor eventTypes = sequence.getEventFactor();
        episodes.setEventFactor(eventTypes);
        
        AbstractEpisodeCounter counter = session.getCounter();
        AbstractCandidateGen candidateGenerator = session.getCandidateGenerator();

       if ((counter instanceof GeneralizedEpisodeCounter
                || 
                counter instanceof GeneralizedEpisodeCounterWithExpiry)
            && candidateGenerator instanceof GeneralizedEpisodeCandidateGeneration)
        {
            System.out.println("Counting one node generalized episodes with durations...");
            session.setEventFactor(sequence.getEventFactor());
            
            observer.setTitle("Generating candidate episodes of size 1");
            candidates = candidateGenerator.generateCandidates(null, observer, session);

            observer.setTitle(this.getTitle(1,candidates));
            observer.setExtent(sequence.getSize());
            // this count is necessary as duration intervals make the votes meaningless.
            counter.countEpisodes(candidates, observer, session);
            
            observer.setTitle("Principality check for episodes of size 1");
	    candidates = counter.postCountProcessing(candidates, observer, session);
        }
        else
        {
            System.out.println("One node episode counts available...");
            candidates = eventTypes.getEpisodeList();
            counter.countEvents(candidates, observer, session);
        }
        
        AbstractFrequencyThreshold aftf = null;
        
        switch (session.getThresholdType()) {
            case EXPLICIT_DECAY:
                 aftf = new DecayingFrequencyThreshold();
                 break;
            case STRENGTH_BASED:
                 aftf = new NewAdaptiveFrequencyThreshold();
                 break;
            case POISSON_BASED:
                 aftf = new NewAdaptiveFrequencyThreshold();
                 break;
            case NEG_STRENGTH:
                 aftf = new NegativeStrengthThreshold();
                 break;
        }
        
        //aftf = new FixedFrequencyThreshold(2);
        System.out.println("aftf = " + aftf.getClass().toString());

        observer.setTitle("Eliminating non-frequent episodes of size: 1");
        candidates = aftf.prune(candidates, observer, session);
        Episode.sort(candidates, Episode.FREQUENCY_ORDER);
        candidates = EpisodeSet.postProcess(candidates, session);
        episodes.addEpisodeList(candidates);
        
        System.out.println("Candidate size = " + candidates.size());
        if (candidates == null || candidates.size() < 1) {
            System.out.println("Mining complete at level = " + (session.getCurrentLevel()));
            observer.taskComplete();
            this.run = false;
            return;
        }
        session.incrementLevel();
        
        System.out.println("Candidate gen type : " + candidateGenerator.getClass().getName());
        System.out.println("Counter type : " + counter.getClass().getName());
               
        PHASE_BLOCK:
        while(session.getPlevels() == 0 || session.getCurrentLevel() <= session.getPlevels())
        {
            int level = session.getCurrentLevel();
            System.out.println("Level = " + level);

            // Generate candidates
            observer.setTitle("Generating " + (level) + 
                    "-node candidate episodes from " + candidates.size() + 
                    " frequent " + (level-1) + "-node episodes");
            observer.setExtent(candidates.size());
      
            int partialCount = 0;
            CANDIDATE_BLOCK: {
                candidateGenerator.reset(session);
                
                // Dont waste time trying to estimate the number of candidates
                //int candidateEstimate = candidateGenerator.estimate(candidates);
                List<IEpisode> nominees = new ArrayList();
                do {
                    partialCount++;

                    List<IEpisode> subcandidates  
                            = candidateGenerator.generateCandidates(candidates, observer,session);
                    if (observer.interrupted()) break PHASE_BLOCK;
                    if (subcandidates == null) break;

                    System.out.println("Candidate size = " + subcandidates.size());
                    if (subcandidates.size() < 1) break;

                    // Obtain frequency count
                    observer.setTitle(makeTitle(
                            "Counting candidate episodes",
                            level, subcandidates.size(), 
                            partialCount));
                    observer.setExtent(sequence.getSize());

                    counter.countEpisodes(subcandidates, observer, session);
                    if (observer.interrupted()) break PHASE_BLOCK;
                    System.out.println("Counting is done");

                    //Post count processing
                    observer.setTitle("Post processing for episodes of size " + level);
                    observer.setExtent(subcandidates.size());
                    subcandidates = counter.postCountProcessing(subcandidates, observer, session);
                    if (observer.interrupted()) break PHASE_BLOCK;
                    System.out.println("Post processing is done");

                    // Retain only frequent episodes
                    observer.setTitle("Eliminating non-frequent episodes of size " + level);
                    subcandidates = aftf.prune(subcandidates, observer, session);
                    
                            
                    nominees.addAll(subcandidates);
                    System.out.println("Nominees size = " + nominees.size());

                } while (! candidateGenerator.atBeginning(session));
                
                Episode.sort(candidates, Episode.FREQUENCY_ORDER);
                candidates = nominees;
            }
            
            if (observer.interrupted()) break PHASE_BLOCK;
            if (candidates == null) break;
            if (candidates.size() < 1) break;

            System.out.println("Threshold = " + session.getCurrentThreshold() + 
                    " Frequent episode size = " + candidates.size());
            candidates = EpisodeSet.postProcess(candidates, session);
            
            episodes.addEpisodeList(candidates);
//            if (session.isBackPruningEnabled() && level == 3) {
//                System.out.println("Backpruning (mg=" + String.valueOf(session.getMuchGreater()) + ") ...");
//                SubEpisodeHeuristic.backPruneEpisodes(episodes, session);
//            }
            session.incrementLevel();
            
            if (observer.interrupted()) break PHASE_BLOCK;
        }
        System.out.println("Mining complete at level = " + session.getCurrentLevel());
        observer.taskComplete();
        this.run = false;
    }

    private void printEpisodes(List<IEpisode> candidates, EventFactor eventTypes)
    {
        if (DEBUG) System.out.println("GenericMiner: print episodes");
        System.out.println("------------------------------------------------ " + 
                candidates.size());
        for (int ix = 0; ix < candidates.size(); ix++)
        {
            System.out.println(candidates.get(ix).toString(eventTypes) + " : 0");
        }
        System.out.println("------------------------------------------------");
    }
    
    private String getTitle(int level, List<IEpisode>episodes) 
    {
        // System.out.println("ENTER # countEpisodes at level " + level);
        if (level == 0) {
            return "Counting candidate episodes (total = " + episodes.size() + ")";
        }
        return "Counting candidate episodes of size " + level 
                + " (total = " + episodes.size() + ")";
    }
    
    private String makeTitle(String head, int level, int count, int part) 
    {
        // System.out.println("ENTER # countEpisodes at level " + level);
        if (level == 0) {
            return  head + " (total = " + count + ")";
        }
        String cardinal;
        switch (part) {
            case 1: cardinal = "first"; break;
            case 2: cardinal = "second"; break;
            case 3: cardinal = "third"; break;
            default:
                cardinal = String.valueOf(part).toString() + "th";
        }
        return head + " of size " + level 
                + " (" + cardinal + " pass, chunk size = " + count + ")";
    }
    
    public boolean isRun(){ return this.run; }
    public void setRun(boolean run){ this.run = run; }
}
