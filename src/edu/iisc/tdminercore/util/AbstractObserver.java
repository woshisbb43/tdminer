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
 *
 * @author phreed@gmail.com
 */
public abstract class AbstractObserver implements IObserver
{
    static final boolean DEBUG = false; // false to remove debugging
    
    private String title;
    private int range;
    private int value;
    private boolean interruption;
    
    private CONSTRAINT_MODE constraintMode;
    
    /** Creates a new instance of MarkingObserver */
    public AbstractObserver( ) {
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
    
    protected void markEvents( List<IEvent> events ) {
        for( IEvent event : events ) {
            if (!event.hasActiveMarker()) {
                event.setMarker(new Integer(1));
                continue;
            }
            Object obj = event.getMarker();
            if (!(obj instanceof Integer)) continue;
            Integer marker = (Integer)obj;
            marker++;
        }
    }

    public void taskStarted() { }
    
    public CONSTRAINT_MODE getConstraintMode() { return this.constraintMode; }
    public void setConstraintMode(CONSTRAINT_MODE mode) { this.constraintMode = mode; }
    
}
