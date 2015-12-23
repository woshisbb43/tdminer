/*
 * EventDataStream.java
 *
 * Created on March 14, 2006, 2:41 PM
 *
 */

package edu.iisc.tdminercore.reader;

import edu.iisc.tdminercore.data.AbstractEventStream;

/**
 * A sequence of events is represented.
 *
 * @author Deb
 * @author phreed@gmail.com
 */
public class FileEventDataStream 
        extends AbstractEventStream
{
    /** Creates a new instance of EventDataStream */
    public FileEventDataStream()
    {
        super();
    }
    
     public AbstractEventStream dup() 
     {
        return new FileEventDataStream(); 
     }
     
     @Override
     public Object clone()
     {
         FileEventDataStream seq = new FileEventDataStream();
         copy(seq);
         return seq;
     }
}
