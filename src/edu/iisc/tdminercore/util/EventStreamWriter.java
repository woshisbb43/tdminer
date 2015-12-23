/*
 * EventStreamWriter.java
 *
 * Created on May 23, 2006, 7:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.GeneralEvent;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.InstEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Deb
 */
public class EventStreamWriter
{
    private static EventStreamWriter instance = new EventStreamWriter();
    public static double offset = 0;
    /** Creates a new instance of EventStreamWriter */
    private EventStreamWriter()
    {
    }
    
    public static EventStreamWriter instance()
    {
        return instance;
    }
    
    public void write(File outfile, IEventDataStream sequence) throws IOException
    {
        PrintWriter out = new PrintWriter(outfile);
        
        if (sequence != null)
        {
            for(IEvent event : sequence.iterable(null))
            {
                int E_i = event.getEventType();
                double t_start = event.getStartTime() + offset;
                double t_end = event.getEndTime() + offset;
                if (event instanceof InstEvent)
                {
                    out.println(sequence.getEventFactor().getName(E_i) + "," + t_start);
                }
                else if (event instanceof GeneralEvent)
                {
                    out.println(sequence.getEventFactor().getName(E_i) + "," + t_start + "," + t_end);
                }
            }
        }
        
        out.close();
        
    }
}
