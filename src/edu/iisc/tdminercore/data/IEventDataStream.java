/*
 * IEventDataStream.java
 *
 * Created on March 14, 2006, 3:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.util.IObserver;
import edu.iisc.tdminercore.util.TimeConstraint;

import java.util.List;

/**
 * A data stream of events.
 * 
 * @author Deb
 * @author phreed@gmail.com
 */
public interface IEventDataStream
{
    public int getSize();
    public int getEventTypeCount();
    public EventFactor getEventFactor();
    public EpisodeSet getFirstOrderEpisodeSet();
    
    public double getSequenceStart();
    public double getSequenceEnd();
    
    public IEvent get(int ix);
    public IEvent get(Integer ix);
    public void set(Integer ix, IEvent event);
    public void set(Integer ix, Integer eventIndex, Double start, Double end, Integer key);
    public Object clone();
    /* 
     * Break the event stream up into equal time frames.
     * Within each time frame move every event of a particular type by,
     * the same value of a random variable.  
     * The expected value of the random variable being half 
     * the width of the time frame.
     */
    public void jitter(double wsize);
    
    /* In order for the counting algorithms to work correctly the event
     * stream needs to be sorted.
     */
    public void sort();
    
    /**
     * The following constraint functions manage event data stream
     * constraint sets.
     * These sets make it possible to limit or temporarily limit 
     * the impact of potentially irrelevant events on processing.
     * For example, it may be adventageous to limit episode discovery to 
     * some enclosed time frames due to a special interest in finding 
     * episodes, characteristic of those time frames.
     * Then, later, it may be necessary to collect the instances of those
     * episodes across a larger, more inclusive, time frame.
     */
    
    /**
     * Add events to the event data stream.
     *
     * In the calling routine the return code of add may be checked.
     * When the return code is false the start time of the last event 
     * is greater than the largest end constraint.
     */
   
    
    public void add(String event, Double start);
    public void add(String event, Double start, Double end);
    public void add(String event, Double start, Integer key);
    public void add(String event, Double start, Double end, Integer key);
    
    /*
    public void add(int eventIndex, double start);
    public void add(int eventIndex, double start, int key);
    public void add(int eventIndex, double start, double end);
    public void add(int eventIndex, double start, double end, int key);
     */
    
    public boolean add(List<Interval> constraint, String event, Double start, Double end);
    // public void append(List<IEvent> events);
    
    /**
     * Note that there is also an event constraint state.
     * This state determines whether and when the constraints specified
     * are to be applied.
     * These values are used to express the context in which the constraints apply.
     */
    public enum CONSTRAINT_MODE {
        LOAD,       // apply when adding new events
        PROSPECT,   // apply when traversing the event stream for discovery
        EXTRACT,    // apply when traversing the event stream for harvesting
        REPORT     // apply when traversing the event stream for reporting
    };   
    
    public TimeConstraint<CONSTRAINT_MODE> getConstraints();
    public void setConstraints(TimeConstraint<CONSTRAINT_MODE> constraints);
    
    interface IEventIterable extends Iterable<IEvent> {
        public void remove();
        public int percentComplete();
        public void setSampleSize( List<IEpisode> episodes );
        public long itemsProcessed();
    }
    public IEventIterable iterable(IObserver observer);
    
    /**
     * Integrate the episodes provided into single events.
     * Replacing the constituent enents with a single new event.
     * The new event type is the episode signature.
     */
    public void integrate(String name, List<IEvent> eventset);
    
    /**
     * The marker is a generally unspecified object that records that
     * an event was visited by an episode during a mining phase.
     */
    public void removeMarkers();
    public double getMarkerRatio();
    
}
