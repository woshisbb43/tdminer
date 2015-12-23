/*
 * IEpisode.java
 *
 * Created on January 8, 2007, 2:01 PM
 *
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.PickMatrix;
import java.io.Serializable;

import java.util.List;
import java.util.Iterator;

/**
 *
 * @author phreed@gmail.com
 */
public interface IEpisode 
        extends Comparable, Iterable<IEpisode.EpisodeInstance>, Serializable, Cloneable
{
    public class NotImplementedException extends RuntimeException {
        public NotImplementedException(String msg) {
            super(msg);
        }
    }
    public class TypeMisMatchException extends Exception {
        public TypeMisMatchException(String msg) {
            super(msg);
        }
    }
    public enum SUBSET_TYPE { proper, strict };
    
    public enum ORDERING { frequency, dictionary, dictionary_frequency };
    public static final int FREQUENCY_ORDER = 0;
    public static final int DICTIONARY_ORDER = 1;
    public static final int DICTIONARY_FREQ_ORDER = 2;
    
    public enum IN_RANGE { all, start, stop, span };
    public static final int ALL_IN_RANGE = 0; // both starts and stops in range
    public static final int START_IN_RANGE = 1; // start is in range
    public static final int STOP_IN_RANGE = 2; // stop is in range
    public static final int SPAN_RANGE = 3; // starts in, stops in or spans range
    
    /**
     * General methods
     */
    public class EpisodeInstance {
        public List<IEvent> eventList;
        public EpisodeInstance(List<IEvent> eventList) 
        {
            this.eventList = eventList;
        }
    }

    public Iterator<EpisodeInstance> iterator();

    public void setBeta(int index, double beta);
    public double getBeta(int index);
    public void setBeta(double[] beta);
    public double[] getBeta();
        
    public void setDeleted(boolean deleted);
    public boolean isDeleted();

    public void setVotes(int[] i);
    public int[] getVotes();
    
    
    public String toString(EventFactor eventTypes, int index);
    public String toString(EventFactor eventTypes);
    @Override
    public String toString();
    
    public Object clone();
    /**
     * The level indicates the number of nodes in the episode.
     * @return the number of nodes
     */
    public int getLevel(); 
    /**
     * Set the event type id. 
     * @param index of the specified event in the episode.
     * @param type is the id for the event type and is the index into
     *    an EventFactor object.
     */
    public void setEvent(int index, int type);
    /**
     * Get the event type id. 
     * @param index of the specified event in the episode.
     * @return is the event type id, the index into an EventFactor object.
     */
    public int getEvent(int index); 
    
    public EventFactor getEventFactor();
    
    public int getFirstEvent(); 
    public int getLastEvent();
    
    public void incrCounter();
    public void decrCounter();
    public int getCounter();
    public void setCounter(int counter);

    public void initVotes(int num_segs);
    public void incrVotes(int index);
    public int getVotes(int index);
    public void setVotes(int index, int votes);
    
    public double getRequiredVotes(int index);
    public void evaluateRequiredVotes(SessionInfo session, Object[] params);
    
    /*
     * The frequency is the ratio of votes to sample size.
     */
    public double getFrequency(int index);
    public void setSampleSize(int index, int size);
  
    public void resetVotes();
    public void resetCounter();
    
    public int size();
    
    /**
     * The signature should uniquely identify the episode within its context.
     * It should be rigorous and provide a means for reconstructing the episode.
     */
    public String getSignature();
    
    /**
     * determine if the first of two episodes is relatively 'principle'.
     * And episode alpha is principle in an event sequence if...
     * 1) it is 'found' in the target sequence
     * 2) there exists no other strict super-episode beta such that 
     *    beta is 'similar' to alpha.
     */
    public int findNonPrincipal(IEpisode that) throws IEpisode.NotImplementedException;
    
     /**
     * Compares two Episodes to see if they are the same.
     */
    public int compareTo(Object o);

    /**
     * The following methods are only meaningful for episodes with inter-event intervals.
     */
    public void setIntervalId(int interEventIndex, int intervalId);
    public void setInterval(int interEventIndex, Interval interval);
    public int getIntervalId(int index);
    public Interval getInterval(int index);
    public boolean hasFiniteIntervals();
    public List<Interval> getIntervalsList();
//    public void setInit(int index, double value);
//    public double getInit(int index);
    
    /**
     * This function compares the episode types (including durations) with another episode.
     */
    public boolean matchPrefix(IEpisode episode) throws IEpisode.NotImplementedException;
    
    public int[] getEventTypeIndices();
    public boolean isSelected();
    
    public void setSelected(boolean selected);
    public int getIndex();
    public void setIndex(int index);
    @Override
    public boolean equals(Object o);
    public boolean isPermutation(IEpisode e);
    
    /**
     * duration is a matrix of maps.
     * Each  map indicating which time duration intervals are 
     * associated with each event type.
     */
    public void setDurationMap(int index, int map);
    public int getDuration(int index);
    public PickMatrix<Interval> getDurations();
    public List<Interval> getDurationsList();
   
    /**
     * An episode, alpha, is similar to another episode, beta,
     * if three conditions hold...
     * 1) one is a sub-episode of the other
     * 2) their event type vectors map 1:1
     * 3) their frequencies are the same
     */
    public boolean isSimilar(IEpisode episode, int index);
    
    /**
     * Determine if the current episode, beta, is a sub-episode 
     * of another episode, alpha.
     * A sub-episode may be either strict or proper.
     * 1) the relative ordering of the event types must match
     * 2) the duration set for alpha is a proper/struct subset of that for beta.
     * There are three possible return values:
     * 0: neither alpha nor beta are sub-episodes of the other
     * 1: beta is a sub-episode of alpha
     * 2: alpha is a sub-episode of beta
     */
    public int isSubEpisode(IEpisode that) throws IEpisode.NotImplementedException;
    
    /**
     * return the last duration bit map.
     */
    public int getLastDuration() throws IEpisode.NotImplementedException;
    public int getDurationsListSize();
    
    /**
     * Routines for dealing with episode instances.
     */
    public void addInstance( List<IEvent> events );
    
    public boolean isHarvested();
    
    public double[] getTimeArray(int ordinal);
    public double[][] asArray();  // an accomdation for Matlab
    public double[][] getTimeArray();
    public double[][] getTimeArray(double[] span, int mode);
    
    public long[][] getKeyArray();
    public long[][] getKeyArray(double[] span, int mode);
    
    public void clearInstances( );
    public int getInstanceCount();
    public int[] getInstanceCount(double[] span);
    public String getInstanceStatistics(double[] span, double sliceSize); 
    
    public void integrate(IEventDataStream events);
    public void setEstr(double[] estr);
    public double[] getEstr();
    public double getEstr(int index);
    
    public void postCountProcessing(SessionInfo session);
    public String getVoteString();
    public String getRequiredVoteString();
}
