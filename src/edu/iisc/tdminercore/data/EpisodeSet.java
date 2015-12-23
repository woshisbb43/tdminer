/*
 * EpisodeSet.java
 *
 * Created on March 14, 2006, 2:41 PM
 *
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.data.episode.SerialEpisodeWithIntervals;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.writer.IWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;

/**
 * Objects of this class are sets of episodes.
 * The episodes in a set may be of different sizes.
 *
 * @author Deb
 * @author phreed@gmail.com
 */
public class EpisodeSet
        extends AbstractSet<IEpisode>
        implements IEpisodeSet, Cloneable, Serializable
{
    static final boolean DEBUG = false; // false to remove debugging
    
    private List<List<IEpisode>> episodeListSet;
    private EventFactor eventTypes;
    
    private static final String title1 = "Frequent Episode discovery results";
    private static final String title2 = "Episode set size";
    private static final String title3 = "Episodes of size";
    private static final String title4 = "No. of";
    
    
    /** Creates a new instance of EpisodeSet */
    public EpisodeSet() {
        this.episodeListSet = new ArrayList<List<IEpisode>>();
    }
    
    public EpisodeSet(EpisodeSet that) {
        this.episodeListSet = new ArrayList<List<IEpisode>>();
        if (that == null || that.eventTypes == null) return;
        this.eventTypes = that.eventTypes;
    }
    
    public EpisodeSet(List<List<IEpisode>> episodeListSet, EventFactor eventTypes) {
        this.episodeListSet = episodeListSet;
        this.eventTypes = eventTypes;
    }
    
    /**
     * A special copy constructor that creates a new episode set from a
     * subset of an older one.
     * In the case where levels/episodes is an empty array all items are
     * selected.
     *
     * @param that  the old episode set
     * @param levels the indecies of the levels that form a subset of the old set
     * @param episodes the indecies of the episodes within the levels
     */
    public EpisodeSet(EpisodeSet that, Integer[] levels, Integer[] episodes) {
        this.eventTypes = that.eventTypes;
        boolean allLevels = (levels == null || levels.length == 0) ? true : false;
        boolean allEpisodes = (episodes == null || episodes.length == 0) ? true : false;
        
        int levelsCount = allLevels ? that.episodeListSet.size() : levels.length;
        
        this.episodeListSet = new ArrayList<List<IEpisode>>(levelsCount);
        for( int ix = 0; ix < levelsCount; ix++ ) {
            if (DEBUG) {
                System.out.println("level: " + ix + " src: " + levels[ix]);
                System.out.println("size: " + that.episodeListSet.size());
            }
            // keep in mind the level index starts at 1 and is stored at offset 0
            if (!allLevels && levels[ix] > that.episodeListSet.size()) continue;
            
            List<IEpisode> oldEpisodeLevel = that.episodeListSet.get(allLevels ? ix : levels[ix]-1);
            
            int episodesCount = allEpisodes ? oldEpisodeLevel.size() : episodes.length;
            List<IEpisode> newEpisodeLevel = new ArrayList<IEpisode>(episodesCount);
            for( int jx = 0; jx < episodesCount; jx++ ) {
                if (DEBUG) {
                    System.out.println("item: " + jx + " src: " + episodes[jx]);
                    System.out.println("size: " + oldEpisodeLevel.size());
                }
                if (!allEpisodes && episodes[jx] >= oldEpisodeLevel.size()) continue;
                IEpisode episode = oldEpisodeLevel.get(allEpisodes ? jx : episodes[jx]);
                newEpisodeLevel.add(episode);
            }
            this.episodeListSet.add(newEpisodeLevel);
        }
    }
    
    /**
     * A special copy constructor that creates a new episode set from a
     * subset of an older one.
     * In the case where levels/episodes is an empty array all items are
     * selected.
     * In this method each element is explicitly requested by it coordinates.
     *
     * @param that  the old episode set
     * @param coordinate the indecies of the levels and episodes that
     *      forming a subset of the old set
     */
    public EpisodeSet(EpisodeSet that, int[][] coordinate) {
        this.eventTypes = that.eventTypes;
        
        HashMap<Integer,List<Integer>> levels =
                new HashMap<Integer,List<Integer>>(that.episodeListSet.size());
        
        for( int ix = 0; ix < coordinate.length; ix++ ) {
            if (DEBUG) {
                System.out.print("episode id: " + ix );
                System.out.print(" level: " + coordinate[ix][0]);
                System.out.println(" index: " + coordinate[ix][1]);
            }
            if (coordinate.length < 1) continue;
            
            Integer level = coordinate[ix][0];
            Integer index = coordinate[ix][1];
            List<Integer> indexes = null;
            if (levels.containsKey(level)) {
                indexes = levels.get(level);
                if (indexes.contains(index)) continue;
            } else {
                indexes = new ArrayList<Integer>();
                levels.put(level,indexes);
            }
            indexes.add(index);
        }
        
        this.episodeListSet = new ArrayList<List<IEpisode>>(levels.size());
        
        for( Integer key : levels.keySet() ) {
            if (that.episodeListSet.size() < key) {
                System.out.println("requesting non-existent episode level, "+key);
                continue;
            }
            List<IEpisode> oldEpisodeLevel = that.episodeListSet.get(key-1);
            List<Integer> indexes = levels.get(key);
            if (indexes.size() < 1) {
                List<IEpisode> newEpisodeLevel = new ArrayList<IEpisode>(oldEpisodeLevel.size());
                for( IEpisode episode : oldEpisodeLevel ) {
                    newEpisodeLevel.add(episode);
                }
                this.episodeListSet.add(newEpisodeLevel);
            } else {
                List<IEpisode> newEpisodeLevel = new ArrayList<IEpisode>(indexes.size());
                int oldSize = oldEpisodeLevel.size();
                for( Integer index : indexes ) {
                    if (oldSize <= index) {
                        System.out.println("requesting non-existent episode, "+index);
                        continue;
                    }
                    newEpisodeLevel.add(oldEpisodeLevel.get(index));
                }
                this.episodeListSet.add(newEpisodeLevel);
            }
            
        }
    }
    
    /**
     * @return the number of episode levels in this set
     */
    public int getSize() { return this.episodeListSet.size(); }
    /**
     * @param ix an episode level's index
     * @return the number of episode types in this set at the specified level
     */
    public int getSize(int ix) {
        if (this.episodeListSet.size() < ix) { return 0; }
        return this.episodeListSet.get(ix-1).size();
    }
    /**
     * @param ix an episode level's index
     * @param jx an episode type's index within its level
     * @return the number of episode instances for an episode type
     *      in this set at the specified level
     */
    public int getSize(int ix, int jx) {
        if (this.episodeListSet.size() < ix) { return 0; }
        List<IEpisode> episodeList = this.episodeListSet.get(ix-1);
        if (episodeList.size() < jx) { return 0; }
        return episodeList.get(jx-1).getInstanceCount();
    }
    /**
     * Get the maximum sizes of levels, episodes, and instances
     * @return a vector of sizes
     */
    public int[] getMaxSizes() {
        int[] sizes = { 0, 0, 0 };
        sizes[0] = this.episodeListSet.size();
        for( int ix=0; ix < sizes[0]; ix++ ) {
            List<IEpisode> episodeList = this.episodeListSet.get(ix);
            int episodeCount = episodeList.size();
            if (sizes[1] < episodeCount) { sizes[1] = episodeCount; }
            for( int jx=0; jx < episodeCount; jx++ ) {
                IEpisode episode = episodeList.get(jx);
                int instanceCount = episode.getInstanceCount();
                if (sizes[2] < instanceCount) { sizes[2] = instanceCount; }
            }
        }
        return sizes;
    }
    
    /**
     * Empty the specified level.
     * @param ix an episode level's index
     */
    public void clear(int ix) {
        if (this.episodeListSet.size() < ix) { return; }
        List<IEpisode> episodeList = this.episodeListSet.get(ix-1);
        episodeList.clear();
    }
    
    public void export(IWriter writer) { writer.write(this); }
    
    public List<IEpisode> getEpisodeList(int episodeLevel) {
        if (this.episodeListSet.size() < episodeLevel) { return null; }
        return this.episodeListSet.get(episodeLevel - 1);
    }
    
    public void setEpisodeList(int episodeLevel, List<IEpisode> list) {
        if (this.episodeListSet.size() < episodeLevel) return;
        this.episodeListSet.set(episodeLevel - 1, list);
    }
    
    /**
     * opening a list is the same as getting an object from the list
     * if the object exists in the list.
     * but if it doesn't exist then the list is extended with empty lists.
     */
    public List<IEpisode> openEpisodeList(int episodeLevel) {
        if (this.episodeListSet.size() >= episodeLevel) {
            return this.episodeListSet.get(episodeLevel - 1);
        }
        List<IEpisode> episodeList = null;
        for(int ix = this.episodeListSet.size(); ix < episodeLevel; ix++) {
            episodeList = new ArrayList<IEpisode>();
            this.episodeListSet.add(episodeList);
        }
        return episodeList;
    }
    
    public IEpisode openEpisode(int episodeLevel, int episodeIndex) {
        List<IEpisode> anEpisodeList = openEpisodeList(episodeLevel);
        
        if (anEpisodeList.size() >= episodeIndex) {
            return anEpisodeList.get(episodeIndex - 1);
        }
        
        IEpisode episode = new Episode(1, this.eventTypes);
        
        anEpisodeList.add(episode);
        return episode;
    }
    
    public EpisodeSet getClone() throws CloneNotSupportedException {
        return (EpisodeSet) this.clone();
    }
    
    @Override
    public Object clone()
    {
        EpisodeSet e = new EpisodeSet();
        e.eventTypes = this.eventTypes;
        
        if (this.episodeListSet != null)
        {
            for(List<IEpisode> srcList : this.episodeListSet)
            {
                List<IEpisode> destList = new ArrayList<IEpisode>();
                e.episodeListSet.add(destList);
                if (srcList != null)
                {
                    for(IEpisode eps : srcList)
                    {
                        destList.add((IEpisode)eps.clone());
                    }
                }
            }
        }
        return e;
    }
    
    public int addEpisodeList(List<IEpisode> list) {
        this.episodeListSet.add(list);
        return new Integer(this.episodeListSet.size());
    }
    
    /**
     * If the list already exists then augment it with the new list.
     * If it does not exist then add the list at the level specified.
     * In order to add the list at the levele specified it may be necessary
     * to add lists ast the the intervening levels.
     */
    public void addEpisodeList(int level, List<IEpisode> update) {
        List<IEpisode> episodeList = openEpisodeList(level);
        episodeList.addAll(update);
    }
    
    
    public static List<IEpisode> postProcess(final List<IEpisode> candidates, final SessionInfo session)
    {
        for(IEpisode e : candidates) e.postCountProcessing(session);
        return candidates;
    }
    
    public int maxEpisodeSize() {
        return this.episodeListSet.size();
    }
    
    public String toString(SessionInfo session)
    {
        String endl = System.getProperty("line.separator");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        
        StringBuffer buf = new StringBuffer();
        for (int index = 0; index < session.getSegIndexLen(); index ++)
        {
            String title = "Time Segment: " + nf.format(session.startTime(index)) + "-" 
                    + nf.format(session.endTime(index)) + " sec";
            buf.append(title + endl);
            buf.append("----------------------------------" + endl);
            buf.append(toString(index));
            buf.append(endl);
        }
        
        return buf.toString();
    }
    public String toString(int index) {
       
        String endl = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        buf.append(title1 + endl);
        buf.append("----------------------------------" + endl);
        buf.append(title2 + " = " + this.episodeListSet.size() + endl + endl);
        for (int i = 0; i < this.episodeListSet.size(); i++) {
            buf.append(title3 + " = " + (i + 1) + endl);
            buf.append("-----------------------" + endl + endl);
            List<IEpisode> list = this.episodeListSet.get(i);
            if (list != null) {
                buf.append(title4 + " " + (i + 1) + " node frequent episodes = " + list.size() + endl);
                
                Episode.sort(list, IEpisode.FREQUENCY_ORDER);
                //Episode.sort(list, IEpisode.DICTIONARY_ORDER, true, eventTypes);
                for (int j = 0; j < list.size(); j++) {
                    IEpisode e = list.get(j);
                    buf.append(e.toString(eventTypes, index) + " : " + e.getVotes(index));
                    //buf.append(" [" + nf.format(e.getFrequency()) + "]");
                    buf.append(endl);
                }
                buf.append("-----------------------" + endl);
            }
        }
        
        return buf.toString();
    }
    
    public EventFactor getEventFactor() {
        return eventTypes;
    }
    
    
    public void setEventFactor(EventFactor eventTypes) {
        this.eventTypes = eventTypes;
    }
    
    public static EpisodeSet buildEpisodeSet(File file, EventFactor eventTypes, List<Interval> intervalsList) 
        throws Exception 
    {
        EpisodeSet episodes = null;
        int epsSize = 0;
        int size = 0;
        int count = 0;
        List<IEpisode> list = null;
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = reader.readLine();
        if (line == null) {
            reader.close();
            return episodes;
        }
        
        Pattern title1pat = Pattern.compile("Frequent\\sEpisode\\sdiscovery\\sresults");
        Pattern hlinepat = Pattern.compile("\\s*-+\\s*");
        Pattern title2pat = Pattern.compile("Episode\\sset\\ssize\\s=\\s(\\d+)");
        Pattern title3pat = Pattern.compile("Episodes\\sof\\ssize\\s=\\s(\\d+)");
        Pattern title4pat = Pattern.compile("No\\.\\sof\\s(\\d+)\\snode\\sfrequent\\sepisodes\\s=\\s(\\d+)");
        Pattern episodepat = Pattern.compile("\\s*([\\w-]+)\\s:\\s(\\d+)\\s*");
        Pattern eventpat = Pattern.compile("\\s*([\\w]+)[\\-]*\\s*");
        Matcher patmatcher = null;
        
        patmatcher = title1pat.matcher(line);
        if (!patmatcher.find()) {
            reader.close();
            return episodes;
        }
        
        while((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) continue;
            System.out.println("Line: " + line);
            
//            patmatcher = hlinepat.matcher(line);
//            if (patmatcher.find()) continue;
            
            patmatcher = title2pat.matcher(line);
            if (patmatcher.find()) {
                System.out.println(line);
                epsSize = Integer.parseInt(patmatcher.group(1));
                List<List<IEpisode>> episodeListSet = new ArrayList<List<IEpisode>>(epsSize);
                episodes = new EpisodeSet(episodeListSet, eventTypes);
                continue;
            }
            
            patmatcher = title3pat.matcher(line);
            if (patmatcher.find()) {
                size = Integer.parseInt(patmatcher.group(1));
                list = null;
                System.out.println("List Size = " + size);
                continue;
            }
            
//            patmatcher = title4pat.matcher(line);
//            if (patmatcher.find()) continue;
            
            System.out.println("Trying " + episodepat);
            patmatcher = episodepat.matcher(line);
            if (patmatcher.find()) {
                System.out.println("Found :: " + patmatcher.group(1) + " and " + patmatcher.group(2));
                if (list == null) {
                    list = new ArrayList<IEpisode>();
                    // clear the episode list
                    int index = size - 1;
                    while (index >= episodes.episodeListSet.size()) {
                        episodes.episodeListSet.add(null);
                    }
                    episodes.episodeListSet.set(index, list);
                }

                // Parse episodes and add to list
                IEpisode e = new Episode(size, eventTypes);
                int freq = Integer.parseInt(patmatcher.group(2));
                e.setVotes(0, freq);
                
                Matcher evtmatcher = eventpat.matcher(patmatcher.group(1));
                
                for(int evtcount=0; evtmatcher.find(); evtcount++) {
                    if (evtcount > size) {
                        System.out.println("Episode larger than expected: " + line);
                        break;
                    }
                    String evt = evtmatcher.group(1);
                    System.out.println("evt = " + evt);
                    int eventType = eventTypes.getId(evt);
                    e.setEvent(evtcount, eventType);
                }
                list.add(e);
                continue;
            }
            
            Pattern ivlEpisodepat = Pattern.compile("\\s*([A-Za-z0-9\\.\\[\\]\\-]+)[\\s#[0-9]\\.\\)\\(]*\\s:\\s(\\d+)\\s*");
            Pattern ivlEventpat = Pattern.compile("(\\w+)\\[|\\-(\\w+)$");
            Pattern ivlPat = Pattern.compile("\\[([0-9.\\-]+)\\]");

            System.out.println("Trying for episodes with intervals : " + ivlEpisodepat);
            patmatcher = ivlEpisodepat.matcher(line);
            if (patmatcher.find()) {
                System.out.println("Found :: " + patmatcher.group(1) + " and " + patmatcher.group(2));
                if (list == null) {
                    list = new ArrayList<IEpisode>();
                    // clear the episode list
                    int index = size - 1;
                    while (index >= episodes.episodeListSet.size()) {
                        episodes.episodeListSet.add(null);
                    }
                    episodes.episodeListSet.set(index, list);
                }

                // Parse episodes and add to list
                IEpisode e = new SerialEpisodeWithIntervals(size, eventTypes, intervalsList);
                int freq = Integer.parseInt(patmatcher.group(2));
                e.setVotes(0, freq);
                
                Matcher evtmatcher = ivlEventpat.matcher(patmatcher.group(1));                
                for(int evtcount=0; evtmatcher.find(); evtcount++) {
                    if (evtcount > size) {
                        System.out.println("Episode larger than expected: " + line);
                        break;
                    }
                    String evt = evtmatcher.group(1);
                    if (evt == null) evt = evtmatcher.group(2);
                    System.out.println("evt = " + evt);
                    int eventType = eventTypes.getId(evt);
                    e.setEvent(evtcount, eventType);
                }
                
                Matcher ivlmatcher = ivlPat.matcher(patmatcher.group(1));
                for(int ivlcount=0; ivlmatcher.find(); ivlcount++) {
                    if (ivlcount > size - 1) {
                        System.out.println("Episode larger than expected: " + line);
                        break;
                    }
                    String ivl = ivlmatcher.group(1);
                    System.out.println("ivl = " + ivl);
                    Interval ivlObj = Interval.parse(ivl);
                    if (!intervalsList.contains(ivlObj))
                    {
                        intervalsList.add(ivlObj);
                    }
                    
                    int ivlIndex = intervalsList.indexOf(ivlObj);
                    e.setIntervalId(ivlcount, ivlIndex);                    
                }
                        
                list.add(e);
                continue;
            }
            System.out.println("Line skipped (not parsed): " + line);
        }
        reader.close();
        return episodes;
    }
    
   /**
     * some environments, Matlab in particular, want to place items at
     * specific locations in a list.
     * If the position is already occupied by an episode the existing
     * episode is replaced with the new one.
     * If the level specified  is beyond the end of the levels, intermediate
     * levels will be added as needed.
     * If the level specified is beyond the end of the episodes, add
     * it at the end not at the specified position.
     * @param level the list into which the episode is to be placed.
     * @param position the position in that list where it should be placed.
     * @param episode the episode to be placed.
     * @return the position in the list where the episode was placed.
     */
    public int addEpisode(int level, int position, IEpisode episode) {
        int index = episode.size() - 1;
        while (index >= this.episodeListSet.size()) {
            this.episodeListSet.add(null);
        }
        List<IEpisode> list = this.episodeListSet.get(index);
        if (list == null) {
            list = new ArrayList<IEpisode>();
            this.episodeListSet.set(index, list);
        }
        list.add(episode);
        return list.size() - 1;
    }
    
    public void addEpisode(IEpisode episode) {
        /*int index = episode.size() - 1;
        while (index >= this.episodeListSet.size()) {
            this.episodeListSet.add(null);
        }
        List<IEpisode> list = this.episodeListSet.get(index);
        if (list == null) {
            list = new ArrayList<IEpisode>();
            this.episodeListSet.set(index, list);
        }
        list.add(episode);*/
        addEpisode(0, 0, episode);
    }
    
    public void addEpisode(String line) throws Exception {
        if (eventTypes == null) eventTypes = new EventFactor();
        
        List<IEpisode> eps = Episode.fromString(line, eventTypes);
        for( IEpisode ep : eps ) 
            addEpisode(ep);
    }
    
    public void integrate(IEventDataStream events) {
        for( List<IEpisode> list : this.episodeListSet) {
            for( IEpisode episode : list ) {
                episode.integrate(events);
            }
        }
        events.sort();
    }
    
    
    // Basic Set operations
    public int size() {
        int size = 0;
        for( List<IEpisode> list : this.episodeListSet ) {
            size += list.size();
        }
        return size;
    }
    
    /**
     * add the episode to the collection
     * @param episode the episode to be added
     * @return was the addition successful.
     */
    public boolean add(IEpisode episode) {
        //System.err.println("ADD: "+episode.toString()+" " + episode.toString(episode.getEventFactor()));
        addEpisode( episode );
        return true;
    }
    
    /**
     * addAll the episode to the collection
     * @param episode the episode to be added
     * @return was the addition successful.
     */
    public EpisodeSet intersect(EpisodeSet others) {
        EpisodeSet ret = new EpisodeSet(this);
        for(IEpisode episode : this) {
           if (others.looseHas(episode)) {
               ret.add(episode);
               // System.err.println("INT: "+episode.toString()+" " + episode.toString(episode.getEventFactor()));
           }
            
        }
        return ret; 
    }
    
    private boolean looseHas(IEpisode check) {
        for(IEpisode episode : this) {
            if (episode.size() != check.size()) continue;
            boolean found = true;
            for(int i = 0; i < episode.size (); i++) {
                String cevent =   check.getEventFactor().getName( check.getEvent(i) );
                String eevent = episode.getEventFactor().getName( episode.getEvent(i) );
                if (cevent != eevent) {
                    found = false;
                    continue;
                }
            }
            if (found)
                return true;
        }
        return false;
    }
    
    /**
     * A method for interating over the sets episode levels.
     */
    public Iterable<List<IEpisode>> levels() {
        return new Iterable() {
            public Iterator<List<IEpisode>> iterator() {
                return episodeListSet.iterator();
            }
        };
    }
    /**
     * Episodes are grouped by their order or level (i.e. number of events).
     * Each of these groups is then placed in the List.
     * @return - an interator of the episode lists.
     */
    public Iterator<IEpisode> iterator() {
        return new Iterator<IEpisode>() {
            private int status = -1; // <0 not started; ==0 running; >0 finished
            private int listIx = -1; // < 0 indicates not initialized
            private Iterator<IEpisode> episodeIx = null;
            
            /* Take care that hasNext not actually change the state 
             * of the base episode interator (episodeIx) as it needs to 
             * continue to reference the current episode.
             */
            public boolean hasNext() {
                if (status > 0) return false;
                
                // if the listIx has not been set then initialize it.
                if (status < 0) {
                    if (episodeListSet.size() < 1) {
                        status = 1;
                        return false;
                    }
                    for( listIx = 0; listIx < episodeListSet.size(); listIx++ ) 
                    {
                        List<IEpisode> list = episodeListSet.get(listIx);
                        if (list == null || list.size() < 1) continue;
                        
                        status = 0;
                        episodeIx = list.iterator();
                        return true;
                    }
                    status = 1;
                    return false;
                }
                
                if (episodeIx.hasNext()) return true;
                
                for( listIx++; listIx < episodeListSet.size(); listIx++ ) 
                    {
                        List<IEpisode> list = episodeListSet.get(listIx);
                        if (list.size() < 1) continue;
                        episodeIx = list.iterator();
                        return true;
                    }
                status = 1;
                return false;
            }
            
            public IEpisode next() {
                hasNext();
                return episodeIx.next();
            }
            
            public void remove() {
                episodeIx.remove();
            }
            
        }; // end new Iterator
    }
    
    /**
     * Bulk operations perform an operation on an entire Collection. 
     * You could implement these shorthand operations using the basic operations, 
     * though in most cases such implementations would be less efficient. 
     * The addAll, removeAll, and retainAll methods all return true 
     * if the target Collection was modified in the process of executing the operation.
     */
    
    /**
     * clear
     * postcondition: all elements from the Collection are removed.
     */ 
    public void clear() {
        for( List<IEpisode> list : this.episodeListSet )
            list.clear();
    }
    
}
