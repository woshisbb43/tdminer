/*
 * PassiveObserver.java
 *
 * Created on November 8, 2006, 3:33 PM
 *
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.data.IEvent;

import java.util.List;

/**
 *
 * @author phreed@gmail.com
 */
public class PassiveObserver 
        implements IObserver
{
static final boolean DEBUG = false; // false to remove debugging
    
    private String title;
    private int range;
    private int value;
    private boolean interruption;
    
    private CONSTRAINT_MODE constraintMode;
    
    /** Creates a new instance of PassiveObserver */
    public PassiveObserver() {
        this.title = "";
        this.range = 0;
        this.value = 0;
        this.interruption = false;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    public void setExtent(int range) {
        this.range = range;
    }
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
        if (DEBUG) System.out.println("ProgressManager: handle episode completion");
    }

    public void taskStarted()
    {
    }
    

    public CONSTRAINT_MODE getConstraintMode() { return this.constraintMode; }
    public void setConstraintMode(CONSTRAINT_MODE mode) { this.constraintMode = mode; }
    
}
