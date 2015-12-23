/*
 * CsvEventStreamReader.java
 *
 * @author phreed@gmail.com
 */

package edu.iisc.tdminercore.reader;

import au.com.bytecode.opencsv.CSVReader;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.util.TimeConstraint;
import edu.iisc.tdminercore.data.Interval;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.List;

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
public class CsvEventStreamReader implements IEventStreamReader
{
    static final boolean DEBUG = false;
    
    private CSVReader reader;
    private int eventTypeIndex = 0;
    private int startTimeIndex = 1;
    private int endTimeIndex = -1;
    private int seqKeyIndex = -1;
    private Hashtable<String,String> legendMap;
    private TimeConstraint<CONSTRAINT_MODE> constraints = null;

    /** Creates a new instance of CsvEventStreamReader */
    /** Creates a new instance of CsvEventStreamReader */
    public CsvEventStreamReader(CSVReader reader,
            Integer eventTypeIndex, Integer startTimeIndex, 
            Integer endTimeIndex, Integer seqKeyIndex) throws FileNotFoundException
    {
        this.reader = reader;
        this.eventTypeIndex = eventTypeIndex;
        this.startTimeIndex = startTimeIndex;
        this.endTimeIndex = endTimeIndex;
        this.seqKeyIndex = seqKeyIndex;
    }
    
    public CsvEventStreamReader(CSVReader reader) throws FileNotFoundException
    {
        this.reader = reader;
    }
    
    public CsvEventStreamReader(CSVReader reader,
            Hashtable<String,String> legendMap) throws FileNotFoundException
    {
        this.legendMap = legendMap;
        this.reader = reader;
    }

    public CsvEventStreamReader(String filename) throws FileNotFoundException
    {
        try {
            InputStream is = new FileInputStream(filename);
            this.reader = new CSVReader(new InputStreamReader(is));
        }
        catch (java.io.FileNotFoundException ex) {
            System.err.println("Event stream file not found: " + ex.getMessage() + filename);
        }
    }
    
    public void setEventTypeIndex(int index) { this.eventTypeIndex = index; }
    public void setStartIndex(int index) { this.startTimeIndex = index; }
    public void setStopIndex(int index) { this.endTimeIndex = index; }
    public void setKeyIndex(int index) { this.seqKeyIndex = index; }
    
    public int getEventTypeIndex() { return this.eventTypeIndex; }
    public int getStartIndex() { return this.startTimeIndex; }
    public int getStopIndex() { return this.endTimeIndex; }
    public int getKeyIndex() { return this.seqKeyIndex; }
    
    public void setSeparator(char separator) {
        reader = new CSVReader(
                this.reader.getReader(),
                separator,
                this.reader.getQuotechar(),
                this.reader.getSkipLines());
    }
    public void setQuotechar(char quotechar) {
        reader = new CSVReader(
                this.reader.getReader(),
                this.reader.getSeparator(),
                quotechar,
                this.reader.getSkipLines());
    }
    public void setSkipLines(Integer skiplines) {
        reader = new CSVReader(
                this.reader.getReader(),
                this.reader.getSeparator(),
                this.reader.getQuotechar(),
                skiplines);
    }
    
    public void setTimeConstraints(TimeConstraint<CONSTRAINT_MODE> constraints)
    {
        this.constraints = constraints;
    }
    
    public IEventDataStream read() throws IOException
    {
        return this.read(this.reader, 0, -1, 1, 2, null, null);
    }

    public IEventDataStream read(String filename) throws IOException
    {
        InputStream is = new FileInputStream(filename);
        return this.read(is,this.constraints);
    }
     
    public IEventDataStream read(String filename, 
            TimeConstraint<CONSTRAINT_MODE> constraints) throws IOException
    {
        InputStream is = new FileInputStream(filename);
        return this.read(is,constraints);
    }
    
    public IEventDataStream read(InputStream in, 
            TimeConstraint<CONSTRAINT_MODE> constraints) throws IOException
    {
        if (DEBUG) {
            System.out.println("reader = " + reader);
            System.out.println("reader.getSeparator() = " + reader.getSeparator());
            System.out.println("reader.getQuotechar() = " + reader.getQuotechar());
            System.out.println("reader.getSkipLines() = " + reader.getSkipLines());
        }
        this.reader = new CSVReader(
                new InputStreamReader(in),
                this.reader.getSeparator(),
                this.reader.getQuotechar(),
                this.reader.getSkipLines());
    	if (reader == null) {
                throw new IOException("Event stream file not specified");
	}
        return CsvEventStreamReader.read(this.reader, 
                this.eventTypeIndex, this.seqKeyIndex,
                this.startTimeIndex, this.endTimeIndex, 
                constraints, this.legendMap);
    }
      
    /* 
     * This is the main method of this class.
     * All the other methods are involved in setup for this method.
     */
    public static IEventDataStream read(CSVReader reader, 
            Integer eventTypeIndex, Integer seqKeyIndex, 
            Integer startTimeIndex, Integer endTimeIndex,
            TimeConstraint<CONSTRAINT_MODE> constraints,
            Hashtable<String,String> legendMap) 
            throws FileNotFoundException, java.io.IOException
    {
	FileEventDataStream dataStream = new FileEventDataStream();
        dataStream.setConstraints(constraints);
        if (legendMap != null)
        {
            dataStream.setLegend(legendMap);
        }
        
        String [] subStr;
        List<Interval> constraint = null;
        if (constraints != null) {
            constraint = constraints.getConstraints(CONSTRAINT_MODE.LOAD);
        }
        while ((subStr = reader.readNext()) != null) {
            //System.out.println(subStr[0] + subStr[1] + "etc...");
            
            String event = null;
            Double start = null;
            Double end = null;
            Integer key = null;

            for (int ix = 0; ix < subStr.length; ix++)
            {
                try {
                    if (ix == eventTypeIndex) {
                        event = subStr[ix];
                        continue;
                    }
                    if (ix == startTimeIndex) {
                        start = Double.parseDouble(subStr[ix]);
                        continue;
                    } 
                    if (ix == endTimeIndex) {
                        end = Double.parseDouble(subStr[ix]);
                        continue;
                    }
                    if (ix == seqKeyIndex) {
                        key = Integer.parseInt(subStr[ix]);
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("bad input " + subStr);
                }
            }
            dataStream.add(constraint,event,start,end,key);  
	}
	return dataStream;
    }
}
