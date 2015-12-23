/*
 * CSVWriter.java
 *
 * Created on March 12, 2007, 11:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.writer;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;
import edu.iisc.tdminercore.data.IEpisodeSet;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.List;
import java.io.IOException;

/**
 *
 * @author phreed@gmail.com
 */
public class CSVWriter implements IWriter
{
    String fileName;
    
    /** Creates a new instance of MatlabWriter */
    public CSVWriter(String fileName) {
        this.fileName = fileName;
    }
    
    public boolean write(EpisodeSet prospect) { return write((IEpisodeSet)prospect); }      
    public boolean write(IEpisodeSet prospect) 
    {  
        try {
            if (prospect == null) return false;
            EventFactor factor = prospect.getEventFactor();
            if (factor == null) return false;
            if (factor.getSize() < 1) return false;

            PrintStream stream = new PrintStream(this.fileName); 
            
            int episodeLevel = 0;
            
                for( IEpisode episode : prospect ) {
                
                    StringBuilder sb = new StringBuilder();
                    Formatter formatter = new Formatter(sb, java.util.Locale.US);
                    
                    formatter.format("%1$d,%2$d", episodeLevel, episode.size());

                    stream.println(sb);
                }
            stream.close();
            return true;
        }
        catch (IOException ex) {
            System.out.println("Exception writing CSV File " + ex.getMessage());
        }
        return false;        
    }
    
     public boolean write(EpisodeInstanceSet prospect) 
    {        
          try {
            if (prospect == null) return false;
            EventFactor factor = prospect.getEventFactor();
            if (factor == null) return false;
            if (factor.getSize() < 1) return false;

            PrintStream stream = new PrintStream(this.fileName); 
            int episodeIx = 0;
            for( IEpisode episode : prospect.getEpisodeList() ) {
                for( IEpisode.EpisodeInstance instance : episode ) {
                    StringBuilder sb = new StringBuilder();
                    Formatter formatter = new Formatter(sb, java.util.Locale.US);

                    formatter.format("%1$d", episodeIx);

                    // int[] indicies = episode.getEventTypeIndices();
                    for( IEvent event : instance.eventList ) 
                    {
                        formatter.format(", %1$d, %2$10.8g ",
                                event.getSourceId(),
                                event.getStartTime() );
                    }
                    stream.println(sb);
                }
                episodeIx++;
            }
            stream.close();
            return true;
        }
        catch (IOException ex) {
            System.out.println("Exception writing CSV File " + ex.getMessage());
        }
        return false;        
    }
}

  