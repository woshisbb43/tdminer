/*
 * GeneralEvent.java
 *
 * Created on March 14, 2006, 10:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.data;

import java.util.List;

/**
 * General event.
 * They differ from the instantaneous events in that they have an end time.
 * The eventType only has meaning when coupled with an EventFactor.
 * @author Deb
 * @see InstEvent
 * @see EventFactor
 */
/**
 *
 * @author Deb
 */
public class GeneralEvent implements IEvent 
{   
    private int eventType;
    private double startTime;
    private double endTime;
    private long sourceid;
    private Object marker;
    
    public long getSourceId() { return sourceid; }
    public Object getMarker() { return marker; }
    public void setMarker( Object marker ) { this.marker = marker; }
    
    /** 
     * Creates a new instance of a General event.
     * 
     * @param eventType an index into and event type lookup array
     * @param startTime
     * @param endTime
     */
    public GeneralEvent(long sourceid, int eventType, double startTime, double endTime) 
    {
        this.sourceid = sourceid;
        this.eventType = eventType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public int getEventType() 
        { return eventType; }
    
    public double getStartTime() 
        { return startTime; }
    public void setStartTime(double time)
        { this.startTime = time; }
    
    public double getEndTime() 
        { return endTime; }
    public void setEndTime(double time)
        { this.endTime = time; }
    
    public double getDuration() 
        { return this.endTime - this.startTime; }
    public void setDuration(double duration) 
        { this.endTime = this.startTime + duration; }
    
    public boolean rangeCheck(List<Interval> timespanList)
    {
        if (timespanList == null) return true;
        for(Interval interval :  timespanList) {
            if ( interval.getTLow() < this.startTime  
              && this.startTime < interval.getTLow() ) return true;
        }
        return false;
    }
    
    // java.util.Collections
    public int compareTo(java.lang.Object that) 
    {
        if (!(that instanceof GeneralEvent))  
            throw new java.lang.ClassCastException("events are not comparable");
        GeneralEvent thatEvent = (GeneralEvent)that;
        if (this.startTime < thatEvent.startTime) return -1;
        if (this.startTime > thatEvent.startTime) return 1;
        if (this.endTime < thatEvent.endTime) return -1;
        if (this.endTime > thatEvent.endTime) return 1;
        return 0;
    }
    
    public boolean hasActiveMarker() { return (this.marker == null ? false : true); }
    public void removeMarker() { this.marker = null; }
}
