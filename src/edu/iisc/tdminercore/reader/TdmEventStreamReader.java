/*
 * TdmEventStreamReader.java
 *
 * Created on March 14, 2006, 2:43 PM
 *
 * @author phreed@gmail.com
 */

package edu.iisc.tdminercore.reader;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.GeneralEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.InstEvent;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.util.TimeConstraint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * An CSV (Comma Separated Values) file is an ASCII text file that 
 * describes a list of instances sharing a set of attributes. 
 *
 * There may be two or three columns of data.
 * The first colum is a string representing the event type.
 * It is case sensitive. It may be quoted.
 * If it contains blanks it must be quoted.
 * The second column is numeric and contains the time (or start time of the event).
 * The optional third column is numeric and contains the end time
 * of the event.
 *
 * http://en.wikipedia.org/wiki/Comma-separated_values
 */
public class TdmEventStreamReader implements IEventStreamReader
{
 
    public TdmEventStreamReader()
    {}
            
    public IEventDataStream read(InputStream in) throws IOException
    { return read(in, (TimeConstraint<CONSTRAINT_MODE>)null); }
    
    public IEventDataStream read(InputStream in, 
            TimeConstraint<CONSTRAINT_MODE> constraints) throws IOException 
    {
	FileEventDataStream dataStream = new FileEventDataStream();
        
	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	
	String record = null;
        List<Interval> constraint = null;
        if (constraints != null) 
            constraint = constraints.getConstraints(CONSTRAINT_MODE.LOAD);
	while((record = reader.readLine()) != null)
	{
	    String[] subStr = record.split(",");
	    if (subStr.length == 2)
	    {
		String event = subStr[0];
		Double start = Double.parseDouble(subStr[1]);
                dataStream.add(constraint, event,start,null);
	    }
	    else if (subStr.length == 3)
	    {
		String event = subStr[0].trim();
		Double start = Double.parseDouble(subStr[1].trim());
		Double end = Double.parseDouble(subStr[2].trim());
                dataStream.add(constraint, event,start,end);
	    }
	}
	return dataStream;
    }
}
