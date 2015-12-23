/*
 * SimpleThreshold.java
 *
 * Created on November 17, 2006, 2:43 PM
 *
 */

package edu.iisc.tdminercore.filter;

/**
 * This functor reduces the event stream by removing all events not
 * found in an event filter.
 * The event filter may be from a passed episode list.
 * Or, it may be provided as a set of event types.
 *
 * @author phreed@gmail.com
 */
public class SimpleThreshold extends AbstractFrequencyThreshold
{
    
    /** Creates a new instance of DecayingFrequencyThreshold */
    public SimpleThreshold() 
    {
    }
}
