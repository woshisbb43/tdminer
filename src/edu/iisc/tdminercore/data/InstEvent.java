/*
 * InstEvent.java
 *
 * Created on March 14, 2006, 10:08 PM
 *
 */

package edu.iisc.tdminercore.data;

import java.util.List;

/**
 * Instantaneous event.
 * They differ from the general events in that they have no end time.
 * the eventType only has meaning when coupled with an EventFactor.
 * @author Deb
 * @see GeneralEvent
 * @see EventFactor
 */
public class InstEvent implements IEvent
{
    private int eventType;
    private double startTime;
    private long sourceid;
    private Object marker;
    
    public long getSourceId() { return sourceid; }
    public Object getMarker() { return marker; }
    public void setMarker( Object marker ) { this.marker = marker; }
    
    /** 
     * Creates a new instance of an instantaneous event.
     * 
     * @param eventType an index into and event type lookup array
     * @param startTime
     */
    public InstEvent(long sourceid, int eventType, double startTime)
    {
        //System.out.println("(" + eventType + " , " + startTime + ")");
        this.sourceid = sourceid;
        this.eventType = eventType;
        this.startTime = startTime;
    }
    /** 
     * @return an integer representing an event type.
     */
    public int getEventType() { return eventType; }
    
    public double getStartTime() { return startTime; }
    public void setStartTime(double time) { this.startTime = time; }
    
    public double getEndTime() { return startTime; }
    public void setEndTime(double time) { this.startTime = time; }
    
    public double getDuration() { return 0.0; }
    public void setDuration(double time) {}
    
   // java.util.Collections
    public int compareTo(java.lang.Object that) 
    {
        if (!(that instanceof InstEvent))  
            throw new java.lang.ClassCastException("events are not comparable");
        InstEvent thatEvent = (InstEvent)that;
        if (this.startTime < thatEvent.startTime) return -1;
        if (this.startTime > thatEvent.startTime) return 1;
        return 0;
    }
    public String toString()
    {
        return "[" + eventType + "," + startTime + "]";
    }
    
    public boolean rangeCheck(List<Interval> timespanList)
    {
        if (timespanList == null) return true;
        for(Interval interval :  timespanList) {
            if ( this.startTime < interval.getTLow() ) continue; 
            if ( interval.getTHigh() < this.startTime ) continue;
            return true;
        }
        return false;
    }

    public boolean hasActiveMarker() { return (this.marker == null ? false : true); }
    public void removeMarker() { this.marker = null; }
}
