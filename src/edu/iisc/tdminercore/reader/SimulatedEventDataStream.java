/*
 * EventDataStream.java
 *
 * Created on March 14, 2006, 2:41 PM
 *
 */

package edu.iisc.tdminercore.reader;

import edu.iisc.tdminercore.data.AbstractEventStream;
import edu.iisc.tdminercore.data.EventFactor;

/**
 * A sequence of events is represented.
 *
 * @author Deb
 * @author phreed@gmail.com
 */
public class SimulatedEventDataStream 
        extends AbstractEventStream
{
    /** Creates a new instance of EventDataStream */
    public SimulatedEventDataStream()
    {
        super();
    }
    
     public AbstractEventStream dup() 
     {
        return new SimulatedEventDataStream(); 
     }
     
     @Override
     public Object clone()
     {
         SimulatedEventDataStream seq = new SimulatedEventDataStream();
         copy(seq);
         return seq;
     }
     
     public void setEventFactor(EventFactor eventTypes)
     {
         this.eventTypes = eventTypes;
     }
}
