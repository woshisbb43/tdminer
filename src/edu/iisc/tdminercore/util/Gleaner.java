/*
 * Gleaner.java
 *
 * Created on November 17, 2006, 1:00 PM
 *
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.miner.SessionInfo;

import java.util.List;


/**
 * This class is used during counting to dispatch episode instances.
 * Specifically, it loads the event sets into the instance list of the
 * appropriate episode.
 *
 * @author phreed@gmail.com
 */

public class Gleaner extends AbstractObserver 
{   static final boolean DEBUG = false; // false to remove debugging
    
    private String title = "";
    private int range = 0;
    private int value = 0;
    private boolean interruption = false;
    private List<IEpisode> episodes;
    
    private int level = 0;
  
    /** Creates a new instance of Gleaner */
    public Gleaner(List<IEpisode> episodes) {
        this.episodes = episodes;
    }
    
    public Gleaner(SessionInfo session, int level) {
         this.episodes = session.getEpisodes().getEpisodeList(level);
    }
     
    public void setTitle(String title) {
        this.title = title;
    }
    public void setExtent(int range) {
        this.range = range;
    }
    
    /**
     * @return true indicates that the observee should terminate 
     */
    public boolean update(int value) {
        this.value = value;
        return false;
    }
    public int progress() {
        return this.value;
    }
    public void dispose() {}
    public boolean interrupted() { return interruption; }
    public void taskComplete() { this.value = this.range; }
    public void exceptionOccured(Exception e) {}
    
    /** 
     * Prepare to harvest the information while the detector is running.
     */
    public void startup() 
    { if (DEBUG) System.out.println("Gleaner: handle startup");
      for(IEpisode episode : episodes) {
        episode.resetVotes();
        episode.clearInstances();
      }
    }
    /**
     * The detector is shutting down.  Do whatever cleanup is needed.
     */
    public void shutdown() 
    { if (DEBUG) System.out.println("Gleaner: handle shutdown");
    }
    
    /**
     * The detector has found the end of a candidate episode.
     * Process the episode as needed.
     */
    public void handleEpisodeCompletion(int episodeIndex, int[] eventTypes, List<IEvent> events)
        throws IObserver.NotImplementedException
    {if (DEBUG) System.out.println("Gleaner: handle episode completion");
        episodes.get(episodeIndex).addInstance(events);
        this.markEvents(events);
    }

   
    public CONSTRAINT_MODE getConstraintMode() { return null; }
    public void setConstraintMode(CONSTRAINT_MODE mode) {}
}
