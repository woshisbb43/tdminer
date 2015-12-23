/*
 * IEvent.java
 *
 * Created on March 14, 2006, 3:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.data;

import java.util.List;

/**
 * The atomic unit of temporal data mining.
 * @author Deb
 */
public interface IEvent extends java.lang.Comparable
{
    public int getEventType();
    
    public double getStartTime();
    public void setStartTime(double time);
    
    public double getEndTime();
    public void setEndTime(double time);
    
    public double getDuration();
    public void setDuration(double time);
    
    /**
     * Check to see if the next item in the sequence meets the criteria.
     */
    public boolean rangeCheck(List<Interval> timespanList);
    
    /**
     * The id is immutable.
     * Not only does it provide a unique identifier it also
     * provides an address back into the data source.
     */
    public long getSourceId();
    public void setMarker(Object obj);
    public Object getMarker();
    public boolean hasActiveMarker();
    public void removeMarker();
}
