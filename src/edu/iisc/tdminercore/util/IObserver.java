/*
 * IObserver.java
 *
 * Created on March 14, 2006, 10:46 PM
 *
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEpisode;
import java.util.List;

/**
 * An observer may be active or passive.
 * 
 * An active observer may be queried by the thing it is observing 
 * in order to determine whether its instructions have changed.
 * An active observer expects the update and interrupted methods returning
 * a true will cause the observee to promptly terminate.
 *
 * A passive observer records updates from what it is observing.
 * It is expected that some other thread will periodically interrogate 
 * the observer and deal with the observee without intervention by
 * or assistance from the observer.
 * 
 * @author Deb
 */
public interface IObserver
{
    /** 
     * Sets the title for the observer.
     * it should describe the thing being observered.
     */
    public void setTitle(String title);
    /**
     * Sets the upper bound of the progress variable.
     * It indicates the number of items to be processed.
     * Typically, 100, as in 100 percent.
     */
    public void setExtent(int range);
    /**
     * Set the progress variable.
     * The boolean return value indicates whether the observed
     * task was cancelled or not.
     */
    public boolean update(int value);
//    public int progress();
//    public void dispose();
    /**
     * Indicates if the observed task was cancelled.
     */
    public boolean interrupted();
    
    /** 
     * The master initialization.
     * It is possible for the observer to recognize phases.
     * Each phase makes a call to 'startup' when it begines.
     * The encompassing task calls 'taskStarted' and 'taskComplete'.
     */
    public void taskStarted();
    public void taskComplete();
   
    public void startup();
    public void shutdown();
    
    /**
     * Used to redirect the handling of an exception through the observer
     * rather than through the task's stack.
     */
    public void exceptionOccured(Exception e);
     
    public class NotImplementedException extends Exception {
        public NotImplementedException(String msg) {
            super(msg);
        }
    }
    /**
     * As each phase is completed it may be useful to harvest some information.
     * This mechanism should be generalized as it presently is very closely
     * associated with episodes and events.
     */
    public void handleEpisodeCompletion(int episodeIndex, int[] et, List<IEvent> events)
        throws IObserver.NotImplementedException;
    // public void handleEpisodeCompletion(int episodeIndex, int[] et, double[] t);
    
    /**
     * Holds the constraint context in which the observer is running.
     */
    public CONSTRAINT_MODE getConstraintMode();
    public void setConstraintMode(CONSTRAINT_MODE mode);
}
