/*
 * MatlabEventStream.java
 *
 * Created on April 20, 2007, 3:29 PM
 *
 * @author phreed@gmail.com
 */

package edu.iisc.tdminercore.reader;

import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.AbstractEventStream;

import java.util.List;
import java.lang.Iterable;
import java.util.Iterator;

/*
 * Maybe someday we will be able to use this to access Matlab files directly.
 *
 * This data stream is implemented on a Matlab input structure.
 * This class must be used in conjunction with the appropriate Matlab jar files.
 * In other words, it will almost certainly be used with a JVM running Matlab.
 */

//import com.mathworks.toolbox.javabuilder.*;


/**
 * This data stream is implemented on a Matlab input structure.
 * This class must be used in conjunction with the appropriate Matlab jar files.
 * In other words, it will almost certainly be used with a JVM running Matlab.
 *
 */
public class MatlabEventStream  
        extends AbstractEventStream
{
    /** Creates a new instance of MatlabEventStream */
    public MatlabEventStream() {
        super();
    }
    public MatlabEventStream(AbstractEventStream that, Integer[] indecies) 
    {
        super(that, indecies);
    }
    
     public AbstractEventStream dup() 
     {
        return new MatlabEventStream(); 
     }

    @Override
     public Object clone()
     {
         MatlabEventStream seq = new MatlabEventStream();
         copy(seq);
         return seq;
     }
}
