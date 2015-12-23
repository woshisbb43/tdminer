/*
 * XmlEventStreamReader.java
 *
 * Created on November 6, 2006, 2:19 PM
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
import edu.iisc.tdminercore.util.TimeConstraint;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.InstEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * An XML (eXtensible Markup Language) file is an ASCII text file that 
 * describes a tree of elements.
 * XML files were developed by the W3C.
 * The format for these files is characterized by the following sample.
 *
 * <?xml version="1.0"?>
 * <event-sequence>
 *  <dimension>
 *    <time units="seconds" exponent="-3">millisecond</time>
 *  </dimension>
 *  <event-type-set>
 *    <event-type name="A" index="1"/>
 *    <event-type name="B" index="2"/>
 *  </event-type-set>
 *  <sequence>
 *    <event type="A" start="12345" duration="12.0"/>
 *    <event index="1" start="12345.02"/>
 *    <event index="1" start="12345.02"/>
 *    <event index="1" start="12345.02"/>
 *  </sequence>
 * <event-sequence>
 *
 * It is expected that the /event-sequence/event-type-set/event-type/@index
 * values will be choosen so that there will be few if any gaps.
 * The index values may be changed so long as the association remains.
 * The event-type-set element may be left out entirely but then the 
 * index attribute cannot be used on the event elements.
 * The cannonical form is to have no event/@type attributes but only 
 * event/@index and an appropriate event-type-set.
 * Further, the index of each event type will be ordinal starting with 1.
 * @author Deb
 */

/**
 *
 * 
 */

public class XmlEventStreamReader implements IEventStreamReader
{
    private TimeConstraint<CONSTRAINT_MODE> constraints = null;
  
    public XmlEventStreamReader()
    {}
  
    public IEventDataStream read(InputStream in, 
            TimeConstraint<CONSTRAINT_MODE> constraints) throws IOException
    {
	FileEventDataStream dataStream = new FileEventDataStream();
	
	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	
	String record = null;
        List<Interval> constraint = constraints.getConstraints(CONSTRAINT_MODE.LOAD);      
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
