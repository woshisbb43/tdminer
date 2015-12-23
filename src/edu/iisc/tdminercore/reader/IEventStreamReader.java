/*
 * IEventStreamReader.java
 *
 * Created on November 6, 2006, 4:09 PM
 *
 */

package edu.iisc.tdminercore.reader;

import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.util.TimeConstraint;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input readers given ascii input.
 * @author  phreed@gmail.com
 */
public interface IEventStreamReader {
     public IEventDataStream read(InputStream in,  
             TimeConstraint<CONSTRAINT_MODE> constraints) throws IOException; 
}
