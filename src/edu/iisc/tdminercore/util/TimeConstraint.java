/*
 * TimeConstraint.java
 *
 * Created on January 30, 2007, 10:44 AM
 *
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.Interval;

import java.util.List;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Thise objects record time frames.
 * In particular time frames over an event data stream.
 * The data stream may be traversed for various reasons.
 * The time frames may be turned on/off for the enumerated reasons.
 *
 * @author phreed@gmail.com
 */
public class TimeConstraint<T extends Enum<T>>
{
    public boolean isActive = true;
    
    public class Constraint {
        private Interval timeframe;
        private EnumMap<T, Boolean> activation;
        
        public Constraint(Class<T> theClass) { 
            timeframe = new Interval(); 
            activation = new EnumMap<T,Boolean>(theClass); 
        }
        
        public double getTimeStart() { return timeframe.getTLow(); }
        public void setTimeStart(double val) {
            timeframe = new Interval(val, timeframe.getTHigh());
        }
        public double getTimeStop() { return timeframe.getTHigh(); }
        public void setTimeStop(double val) {
            timeframe = new Interval(timeframe.getTLow(), val);
        }
        public void setActivation(T mode, boolean val) {
            activation.put(mode,val);
        }
        public boolean isActivated(T mode) {
            if (! activation.containsKey(mode)) return false;
            return activation.get(mode); 
        }
    }
    private List<Constraint> constraintList;
    
    /**
     * Creates a new instance of TimeConstraint
     */
    public TimeConstraint() {
        constraintList = new ArrayList<Constraint>();
    }
    
    public int getSize() { 
        return constraintList.size();
    }
    public int getStoredCount() {  return constraintList.size(); }
    
    public Constraint getAt(int ix) {
        return constraintList.get(ix); 
    }
    
    public void putConstraint(int index, Constraint constraint) {
        constraintList.add(constraint);
    }
    
    /**
     * This method is used by event stream iterators.
     * If the constraint list:
     * - is missing or 
     * - is switched off or
     * - does not mention the mode in any of its constraints;
     * then actions are unconstrained (true).
     */
    public boolean check(T mode, double start) 
    {
        if (! this.isActive) return true;
        if (this.constraintList == null) return true;
        boolean modeActiveFlag = false;
        for(Constraint constraint :  this.constraintList) {
            if (!constraint.isActivated(mode)) continue;
            modeActiveFlag = true;
            if (start < constraint.timeframe.getTLow()) continue;
            if (constraint.timeframe.getTHigh() < start) continue;
            return true;
        }
        return (modeActiveFlag ? false : true);
    }
    
    /**
     * These routine is similar to the one above except rather than
     * interogating the checklist directly it uses an extract of it.
     * The extract would normally be made via a preemptive call as ...
     * List<Interval> loadConstraints = 
     *          constraints.getConstraints(CONSTRAINT_MODE.LOAD);
     */
    public static boolean check(List<Interval> intervals, double start) {
        if (intervals == null) return true;
        if (intervals.size() < 1) return true;
        for(Interval interval :  intervals) {
            if (start < interval.getTLow()) continue;
            if (interval.getTHigh() < start) continue;
            return true;
        }
        return false;
    }
    /**
     * This method is used by loaders of event streams.
     * It returns a set of constraints.
     * A null list is presumed to mean unconstrained.
     */
    public List<Interval> getConstraints(T mode)
    {
        List<Interval> alist = new ArrayList<Interval>();
        if (! this.isActive) return alist;
        if (this.constraintList == null) return alist;
        for( Constraint constraint : this.constraintList) {
            if (!constraint.isActivated(mode)) continue;
            alist.add(constraint.timeframe.clone());
        }
        return alist;
    }
    
       
    public Constraint add(Class<T> theClass) {
        Constraint wip = new Constraint(theClass);
        this.constraintList.add(wip);
        return wip;
    }
    
    public void remove(int ix) {
        this.constraintList.remove(ix);
    }

    public void remove(Constraint c) {
        this.constraintList.remove(c);
    }
            
}
