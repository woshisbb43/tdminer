/*
 * Algorithms.java
 *
 * Created on April 29, 2006, 1:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.util;

import edu.iisc.tdminercore.candidate.AbstractCandidateGen;
import edu.iisc.tdminercore.candidate.AprioriCandidateGeneration;
import edu.iisc.tdminercore.candidate.GeneralizedEpisodeCandidateGeneration;
import edu.iisc.tdminercore.candidate.ParallelAprioriCandidateGeneration;
import edu.iisc.tdminercore.candidate.PrefixSuffixCandidatesWithIntervals;
import edu.iisc.tdminercore.candidate.PrefixSuffixMatchCandidateGen;
import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;
import edu.iisc.tdminercore.counter.FastNonOverlappedEpisodeCounter;
import edu.iisc.tdminercore.counter.GeneralizedEpisodeCounter;
import edu.iisc.tdminercore.counter.GeneralizedEpisodeCounterWithExpiry;
import edu.iisc.tdminercore.counter.NonInterleavedEpisodeCounter;
import edu.iisc.tdminercore.counter.NonOverlappedEpisodeCounter;
import edu.iisc.tdminercore.counter.ParallelEpisodesCounterWithExpiry;
import edu.iisc.tdminercore.counter.ParallelEpisodesCounterWithRepeatedEvents;
import edu.iisc.tdminercore.counter.ParallelEpisodesCounterWithSignificance;
import edu.iisc.tdminercore.counter.ParallelNonOverlapperEpisodeCounter;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithIntervals;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithRigidDelays;
import edu.iisc.tdminercore.counter.SerialIntervalCounter;
import edu.iisc.tdminercore.counter.SerialTrueIntervalCounter;
import java.util.ArrayList;

/**
 *
 * @author Deb
 */
public class Algorithms {
    
    public static ArrayList<AlgoEntry> COUNTER_ALGOS;
    private static final ArrayList<AlgoEntry> COUNTER_ALGOS_ALL = new ArrayList<AlgoEntry>();
    private static final ArrayList<AlgoEntry> COUNTER_ALGOS_MINIMAL = new ArrayList<AlgoEntry>();
    static
    {
        AlgoEntry algoEntry = null;
        AbstractEpisodeCounter counter = null;        
        
        /*0*/algoEntry = new AlgoEntry("- Serial Episode", AlgoEntry.GraphType.serial);
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new FastNonOverlappedEpisodeCounter();
        /*1*/algoEntry = new AlgoEntry(counter,
                0,// candidateIndex
                AlgoEntry.GraphType.serial,
                false,// epsExpiryEnable
                false,// ivlHighEnable
                false,// ivlLowEnable
                true,// allowRepeatedEventTypes
                AlgoEntry.EXPLICIT // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        //COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new NonOverlappedEpisodeCounter();
        /*2*/algoEntry = new AlgoEntry(counter,
                0,// candidateIndex
                AlgoEntry.GraphType.serial,
                true,// epsExpiryEnable
                false,// ivlHighEnable
                false,// ivlLowEnable
                true,// allowRepeatedEventTypes
                AlgoEntry.EXPLICIT // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new SerialIntervalCounter();
        /*3*/algoEntry = new AlgoEntry(counter,
                2,// candidateIndex
                AlgoEntry.GraphType.serial,
                false,// epsExpiryEnable
                true,// ivlHighEnable
                false,// ivlLowEnable
                false,// allowRepeatedEventTypes
                AlgoEntry.EXPLICIT // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new SerialTrueIntervalCounter();
        /*4*/algoEntry = new AlgoEntry(counter,
                2,// candidateIndex
                AlgoEntry.GraphType.serial,
                false,// epsExpiryEnable
                true,// ivlHighEnable
                true,// ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.EXPLICIT // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new SerialEpisodeCounterWithIntervals();
        /*5*/algoEntry = new AlgoEntry(counter, // counter
                3, // candidateIndex
                AlgoEntry.GraphType.serial,
                false, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.INTER_EVENT_DISCOVERY  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new SerialEpisodeCounterWithRigidDelays();
        /*6*/algoEntry = new AlgoEntry(counter, // counter
                3, // candidateIndex
                AlgoEntry.GraphType.serial,
                false, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.INTER_EVENT_DISCOVERY  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);

        counter = new GeneralizedEpisodeCounter();
        /*7*/algoEntry = new AlgoEntry(counter, // counter
                4, // candidateIndex
                AlgoEntry.GraphType.serial,
                false, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.DURATION_DISCOVERY  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new GeneralizedEpisodeCounterWithExpiry();
        /*8*/algoEntry = new AlgoEntry(counter, // counter
                4, // candidateIndex
                AlgoEntry.GraphType.serial,
                true, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.DURATION_DISCOVERY_WITH_EXPIRY  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new NonInterleavedEpisodeCounter();
        /*9*/algoEntry = new AlgoEntry(counter, // counter
                2, // candidateIndex
                AlgoEntry.GraphType.serial,
                false, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.EXPLICIT  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        
        
        /*10*/algoEntry = new AlgoEntry("- Parallel Episode", AlgoEntry.GraphType.parallel);
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new ParallelNonOverlapperEpisodeCounter();
        /*11*/algoEntry = new AlgoEntry(counter, // counter
                1, // candidateIndex
                AlgoEntry.GraphType.parallel,
                false, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.EXPLICIT  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        
        counter = new ParallelEpisodesCounterWithExpiry();
        /*12*/algoEntry = new AlgoEntry(counter, // counter
                1, // candidateIndex
                AlgoEntry.GraphType.parallel,
                true, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                false, // allowRepeatedEventTypes
                AlgoEntry.EXPLICIT  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        //COUNTER_ALGOS_MINIMAL.add(algoEntry);
        
        counter = new ParallelEpisodesCounterWithRepeatedEvents();
        /*13*/algoEntry = new AlgoEntry(counter, // counter
                1, // candidateIndex
                AlgoEntry.GraphType.parallel,
                true, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.EXPLICIT  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        COUNTER_ALGOS_MINIMAL.add(algoEntry);
	
        counter = new ParallelEpisodesCounterWithSignificance();
        /*14*/algoEntry = new AlgoEntry(counter, // counter
                1, // candidateIndex
                AlgoEntry.GraphType.parallel,
                true, // epsExpiryEnable
                false, // ivlHighEnable
                false, // ivlLowEnable
                true, // allowRepeatedEventTypes
                AlgoEntry.EXPLICIT  // constraintType
                );
        COUNTER_ALGOS_ALL.add(algoEntry);
        //COUNTER_ALGOS_MINIMAL.add(algoEntry);
	
        // Select MINIMAL or ALL here (Later this can be loaded from configuration file)
        //COUNTER_ALGOS = COUNTER_ALGOS_MINIMAL;
        COUNTER_ALGOS = COUNTER_ALGOS_ALL;
    }
    
    
    public static class CandidateGlossary {
        public String name;
        public AbstractCandidateGen functor;
        public CandidateGlossary(String name, AbstractCandidateGen functor) {
            this.name = name; this.functor = functor;
        }
    }
    
    public final static CandidateGlossary[] CANDIDATE_GEN_ALGOS;
    static {
        CANDIDATE_GEN_ALGOS = new CandidateGlossary[] {
            /*0*/ new CandidateGlossary("Apriori candidate generation(Serial)", new AprioriCandidateGeneration()),
            /*1*/ new CandidateGlossary("Apriori candidate generation(Parallel)",new ParallelAprioriCandidateGeneration()),
            /*2*/ new CandidateGlossary("Prefix-suffix candidate generation", new PrefixSuffixMatchCandidateGen()),
            /*3*/ new CandidateGlossary("Candidate generation for interval discovery", new PrefixSuffixCandidatesWithIntervals()),
            /*4*/ new CandidateGlossary("Candidate generation for generalized episodes", new GeneralizedEpisodeCandidateGeneration())
        };
    }
}
