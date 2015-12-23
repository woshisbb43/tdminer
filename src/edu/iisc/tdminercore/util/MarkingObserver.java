/*
 * MarkingObserver.java
 *
 * Created on September 17, 2007
 *
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.miner.SessionInfo;

import java.util.List;

/**
 * This observer is to be used with a counter to mark the events
 * in a stream as participating in an episode.
 * The "IEvent" has a "getMarker()" method that is used to associate a key field with each event.
 * This key field is used to associate the events with other objects.
 * Originally, the reference associated events representing neurological 
 * spikes with waveforms.
 * Other uses of the marker...
 * <ul>
 * <li>to help in signal-to-noise calculations </li>
 * <li>to carry an episode instance identifier </li>
 * </ul>
 * As the miner runs it marks events as being part of an episode.
 * This is done in the same manner as the harvesting of instances.
 * That is, select a set of episodes, then make a pass to mark all 
 * the events involved with those episodes.
 * Then, run a method on the event stream that would examine the markers on each event.
 * The result would be events appropriately marked...
 * <ul>
 * <li>marked as signal and are part of an episode instance (true signal)</li>
 * <li>marked as signal but are not in an episode instance (false negative or false noise)</li>
 * <li>marked as noise but are part of an episode instance (false positive or false signal)</li>
 * <li>marked as noise but are not part of an episode instance (true noise)</li>
 * </ul>
 *
 * @author phreed@gmail.com
 */
public class MarkingObserver extends AbstractObserver 
{
    static final boolean DEBUG = false; // false to remove debugging
    
    private String title;
    private int range;
    private int value;
    private boolean interruption;
    
    private CONSTRAINT_MODE constraintMode;
    
    /** Creates a new instance of MarkingObserver */
    public MarkingObserver( ) {
        this.title = "";
        this.range = 0;
        this.value = 0;
        this.interruption = false;
    }
    
    public void setTitle(String title) { this.title = title; }
    public void setExtent(int range) { this.range = range; }
    
    // the return value indicates that the observee should terminate 
    public boolean update(int value) {
        this.value = value;
        return false;
    }
    public int progress() {
        return this.value;
    }
    public void dispose() {}
    public boolean interrupted() { return interruption; }
    public void taskComplete() { this.value = this.range; }
    public void exceptionOccured(Exception e) {}
    public void startup() { this.value = 0; }
    public void shutdown() {}
    public void handleEpisodeCompletion(int episodeIndex, int eventTypes[], List<IEvent> events)
        throws IObserver.NotImplementedException
    {
        this.markEvents(events);
    }

    public void taskStarted() { }
    
    public CONSTRAINT_MODE getConstraintMode() { return this.constraintMode; }
    public void setConstraintMode(CONSTRAINT_MODE mode) { this.constraintMode = mode; }
    
}
