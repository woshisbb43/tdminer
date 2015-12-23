/*
 * Epigraph.java
 *
 * Created on November 3, 2006, 9:57 AM
 *
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.Episode;
import java.util.List;
import java.util.ArrayList;

/**
 * Objects of this class represent episode structures.
 * It is contructed from serial/parallel episodes, those episodes are 
 * knit together along sub-episodes.
 *
 * @author phreed@gmail.com
 */
public class Epigraph 
{
    /** a two way map of event types names to ids */
    private EventFactor eventTypes;
    /** as intervals are used more than once... a list of all intervals */
    private List<Interval> intervals;
    /** episodes grouped by their size/order. 
     * They need to be grouped by their other properties as well.
     */
    private List<List<Episode>> episodesList;
    /** connections between episodes */
    private List<EpisodeLink> links;
    
    /** Creates a new instance of Epigraph */
    public Epigraph(EventFactor et, List<Interval> li) {
        eventTypes = et;
        intervals = li;
    }
    
    class EpisodeLink  {
        public EpisodeLink() {
        }
    } 
     
}