/*
 * AbstractEventStream.java
 *
 * Created on April 13, 2007, 1:51 PM
 *
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.util.IObserver;

import edu.iisc.tdminercore.util.TimeConstraint;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * @author Patrick Butler - jitter
 * @author phreed@gmail.com
 */
public abstract class AbstractEventStream 
        implements IEventDataStream, Cloneable
{
    java.util.Random randomizer;
    protected List<IEvent> eventSequence = null;
    protected EventFactor eventTypes = null;
    protected EpisodeSet oneNodeEpisodes = null;
    protected List<IEpisode> oneNodeEpisodeList = null;
    protected Integer index = 0; 
    protected Map<String,String> legendMap;
    protected long sourceid = 0;
    
    // A null time constraint indicates that there are not time constraints.
    protected TimeConstraint<CONSTRAINT_MODE> timeConstraints;
    
    /** Creates a new instance of AbstractEventStream */
    public AbstractEventStream() 
    {
        this.eventSequence = new ArrayList<IEvent>();
        this.oneNodeEpisodes = new EpisodeSet();
        this.oneNodeEpisodeList = new ArrayList<IEpisode>();
        this.oneNodeEpisodes.addEpisodeList(this.oneNodeEpisodeList);
        this.eventTypes = new EventFactor();
        this.timeConstraints = new TimeConstraint<CONSTRAINT_MODE>();
    }
    
    public void copy(AbstractEventStream s)
    {
        s.eventSequence = this.eventSequence;
        s.oneNodeEpisodes = this.oneNodeEpisodes;
        s.oneNodeEpisodeList = this.oneNodeEpisodeList;
        s.eventTypes = this.eventTypes;
        s.timeConstraints = new TimeConstraint<CONSTRAINT_MODE>();
    }
    
    @Override
    public Object clone() { return this; }
    
    /** 
     * Subset Copy Constructor 
     * The copy shares much of the same base information.
     * namely, the event type definitions and the actual events.
     */
    public AbstractEventStream(AbstractEventStream that, Integer[] indecies) 
    {
        this.eventSequence = new ArrayList<IEvent>(indecies.length);
        this.eventTypes = new EventFactor();
        this.eventTypes.addAll(that.eventTypes);
        for(int ix = 0; ix < indecies.length; ix++) {
            this.eventSequence.add(that.eventSequence.get(indecies[ix]));
        }
        this.oneNodeEpisodes = new EpisodeSet();
        this.oneNodeEpisodeList = new ArrayList<IEpisode>();
        this.oneNodeEpisodes.addEpisodeList(this.oneNodeEpisodeList);
        this.timeConstraints = new TimeConstraint<CONSTRAINT_MODE>();
    }
    
    public abstract AbstractEventStream dup();
   
     /** 
      * Produce two new event streams.
      * The first represents all those events which occur in the 'busy'.
      * Periods of the stream second those that occur in the 'quiet' portions.
      * @param that the stream to split in two
      * @param radius the half time span over which the count is made
      * @param threshold the rate at which the event goes to busy.
      * @return 
     */
    public IEventDataStream[] burst(double radius, double threshold) 
    {
        this.sort();
        long cthreshold = (long)(threshold * 2.0 * radius);
        
        AbstractEventStream busy = this.dup();
        AbstractEventStream quiet = this.dup();
        AbstractEventStream[] result = { busy, quiet };
        
        busy.eventSequence = new ArrayList<IEvent>(this.eventSequence.size()/2);
        quiet.eventSequence = new ArrayList<IEvent>(this.eventSequence.size()/2);
        
        busy.eventTypes = this.eventTypes;
        quiet.eventTypes = this.eventTypes;
        
        Iterator<IEvent> leaderIx = this.eventSequence.iterator();
        Iterator<IEvent> trailerIx = this.eventSequence.iterator();
        if (!leaderIx.hasNext()) {
            return null;
        }
        IEvent leader = leaderIx.next();
        IEvent trailer = trailerIx.next();
        long count = 0;
        for(IEvent current : this.eventSequence) {
            while (leaderIx.hasNext()) {
                if (leader.getStartTime() > current.getStartTime() + radius) 
                    break;
                leader = leaderIx.next();
                count++;
            }
            while (trailerIx.hasNext()) {
                if (trailer.getStartTime() > current.getStartTime() - radius) { 
                    break;
                }
                trailer = trailerIx.next();
                count--;
            }
            if (count < cthreshold) {
                quiet.eventSequence.add( current );
            }
            else {
                busy.eventSequence.add( current );
            }          
        }
  
        busy.oneNodeEpisodes = this.oneNodeEpisodes;
        quiet.oneNodeEpisodes = this.oneNodeEpisodes;
        
        busy.timeConstraints = this.timeConstraints;
        quiet.timeConstraints = this.timeConstraints;
        
        return result;
    }
    
    /**
     * Utility function creates a separate amount to jitter each event type by wsize.
     * @param jits - (output) an array of baby jitters :), so cute
     */
    private double[] jitPopulator(int n, double wsize)
    {
        double[] jits = new double[n];
        for(int ix = 0; ix < n; ix++ ) {
            // Allows to be jittered to the left too
            jits[ix] = wsize * (this.randomizer.nextDouble() - 0.5);
        }
        return jits;
    }

    /* Jitters the data 
     * IN:        es -- contains the events and their times
     * @param wsize -- window size to do jitters
     * @return Nothing, changes are made to es directly
     */
    public void jitter(double wsize)
    {
        Date currentDate = new Date();
        this.randomizer = new java.util.Random(currentDate.getTime());

        int numtypes = this.getEventTypeCount();
        
        double[] jits; // holds the amount by which to jitter each type.

        //Create initial times to jitter by
        jits = jitPopulator(numtypes, wsize);

        IEvent firstEvent = this.eventSequence.get(0);
        double wstart = firstEvent.getStartTime();
        //foreach episode
        for(int ix = 0; ix < this.eventSequence.size(); ix++) {
            //Check to see if  we are in a new windwo, if so generat new jitter times
            IEvent currentEvent = this.eventSequence.get(ix);
            if (currentEvent.getStartTime() > (wstart + wsize)) {
                jits = jitPopulator(numtypes, wsize);
                wstart = currentEvent.getSourceId();
            }
            //es.time returns an alias so this works
            currentEvent.setStartTime( currentEvent.getStartTime() + jits[ currentEvent.getEventType() ] );
            // negative times are not a problem, so don't worry about handling them
        }
    }
    
    /*
     * Sort the event stream into time order.
     */
    public void sort()
    {
        java.util.Collections.sort(eventSequence, null);
    }
    
    public int getSize() { return eventSequence.size(); }
    public int getEventTypeCount() { return eventTypes.getSize(); }
     
   
    EpisodeSet getOneNodeEpisodes()
    {
        return oneNodeEpisodes;
    }
    public EpisodeSet getFirstOrderEpisodeSet()
    {
        EpisodeSet oneNodeEpisodes = new EpisodeSet();
        oneNodeEpisodes.addEpisodeList(this.eventTypes.getEpisodeList());
	oneNodeEpisodes.setEventFactor(eventTypes);
        return oneNodeEpisodes;
    }
    
    public EventFactor getEventFactor() { return eventTypes; }
    
    /**
     * The following reset and getter functions are used for loop control.
     */
    public double getSequenceStart()
    {
        if (eventSequence.size() > 0)
            return eventSequence.get(0).getStartTime();
        return -1;
    }
    
    public double getSequenceEnd()
    {
        if (eventSequence.size() > 0)
            return eventSequence.get(eventSequence.size() - 1).getStartTime();
        return -1;
    }
   
    /**
     * Or, an iterator may be used
     * The difficulty with the standard interator is that 
     * the progress is difficult to determine as items 
     * may have been removed or added since the iterator was created.
     */
    public class LocalIterable implements IEventIterable
    {
        List<IEvent> eventlist = null;
        private Iterator<IEvent> iterator = null;
        private IEvent current = null;
        private IEvent next = null;
        
        private int size = 0;
        private int itemsProcessed = 0;
        private IObserver observer = null;
        private boolean isEventConstrained = false;
        private List<Interval> eventTimeConstraintList = null;
        
        public long itemsProcessed() { return itemsProcessed; }
        
        public LocalIterable(IEventDataStream.CONSTRAINT_MODE mode, IObserver observer) {
            this.observer = observer;
            
            eventlist = AbstractEventStream.this.eventSequence;
            this.iterator = eventlist.iterator();
            this.size = eventlist.size();
            if (mode == null) return; // not necessary?
            
            if (AbstractEventStream.this.timeConstraints != null)
            {
                this.eventTimeConstraintList 
                        = AbstractEventStream.this.timeConstraints.getConstraints(mode);
                this.isEventConstrained = this.eventTimeConstraintList.size() < 1
                        ? false : true;
            }
        }
        public LocalIterable() { 
            this(null, null); 
        }
        
        /** 
         * The implementation of the iterator is critical to its use in 
         * the for-each construct.
         * It is not sufficient for the hasNext to determine if the end 
         * of the container has been reached, it needs to determine if there
         * are any elements that meet the constraints.
         */
        public Iterator<IEvent> iterator() {
            return new Iterator<IEvent>() {
                
                /**
                 * When hasNext is called it will advance until it finds
                 * an Event that meets the constraints, which it will 
                 * then save as next.
                 * The idea is this, if 'current' == 'next' then we don't
                 * know if there is a next and we will look for one.
                 * If they aren't equal then we already have the 'next'
                 * so of course it exists.
                 */
                public boolean hasNext() {
                    if (observer != null && observer.interrupted()) return false;
                    if (current != next) return true;
                    if (!iterator.hasNext()) return false;
                    
                    if (!isEventConstrained) {
                        next = iterator.next();
                        return true;
                    }
                    while (iterator.hasNext()) {
                        next = iterator.next();
                        if (next.rangeCheck(eventTimeConstraintList)) {
                            return true;
                        } 
                    }
                    return false;
                }
                /** 
                 * ...next works in concert with hasNext.
                 * As before if 'current' != 'next' we have the next already.
                 * If not then call hasNext to get the next.
                 */
                public IEvent next() {
                    itemsProcessed++; 
                    if (observer != null && itemsProcessed % 50 == 0) {
                        observer.update(itemsProcessed);
                    }
                    if (current == next) {
                        if (!hasNext()) return null;
                    }
                    current = next;
                    return current;
                }
                public void remove() {
                    eventlist.remove(current);
                }
                
            }; // end new Iterator
        }
        public void remove() { eventlist.remove(current); }
        public int percentComplete() { 
            return (int)(itemsProcessed * 100.0 / size); 
        }
        public void setSampleSize( List<IEpisode> episodes )
        {
            for(IEpisode ep : episodes) {
                ep.setSampleSize(0, itemsProcessed);
            }
        }
    }
   
    public IEventIterable iterable(IObserver observer) { 
        CONSTRAINT_MODE mode = (observer == null) ? null : observer.getConstraintMode(); 
        return new LocalIterable(mode, observer); 
    }
    
    public TimeConstraint<CONSTRAINT_MODE> getConstraints() 
    {
        return this.timeConstraints;
    }
    public void setConstraints(TimeConstraint<CONSTRAINT_MODE> constraints)
    {
        this.timeConstraints = constraints;
    }
    public void deactivateConstraints() {
        this.timeConstraints = null;
    }
    
    public void setLegend(Map<String,String> legendMap)
    {
        this.legendMap = legendMap;
    }
   
    public void integrate(String name, List<IEvent> eventset) 
    {
        IEventDataStream that = this;
        IEvent first = eventset.get(0);
        this.add(name,first.getStartTime());
        for( IEvent event : eventset) {
            // it may be that the event was previously removed that is ok
            this.eventSequence.remove(event);
        }
    }
    
    /**
     * Add the content of one event stream to another
     */
    public void add(IEventDataStream obj) 
    {
        AbstractEventStream that = (AbstractEventStream)obj;
        
        // combine the event types
        int size = that.eventTypes.getSize();
        Map<Integer, Integer> id2id = new HashMap<Integer, Integer>(size);
        for(EventFactor.EventType type : that.eventTypes )
        {
            String typename = type.name;
            id2id.put(type.id, this.eventTypes.put(typename));
        }
        
        // append the events
        for(IEvent event :  that.eventSequence) 
        {
            Integer eventIndex = id2id.get(event.getEventType());
            this.eventTypes.incrById(eventIndex);
            IEvent clone = null;
            Integer sourceKey = new Integer((int)event.getSourceId());
            Double start = new Double(event.getStartTime());
            if (event instanceof InstEvent) {
                clone = new InstEvent(sourceKey, eventIndex, start);
            } else
            if (event instanceof GeneralEvent) {
                Double end = new Double(event.getEndTime());
                clone = new GeneralEvent(sourceKey, eventIndex, start, end);
            }
            else {
                continue;
            }
            this.eventSequence.add( clone );
        }
  
        this.sort();
    }
    
    /**
     * Add the content of one event stream to another
     */
    public void add(double[] type, double[] time, int[] key) 
    {
    }
    
    /**
     * Called to construct an event stream by adding items to it.
     * @param event 
     *      a string naming the event type
     * @param start
     *      the time when the event occured
     * @param end [optional disable by setting to null]
     *      the time when the event stopped
     * @param key [optional autogenerated by setting to null]
     *      a unique key value from the input source.
     *   
     */
    private void update(int eventIndex, double start, double end, int key) {
        this.eventTypes.incrById(eventIndex);
        int sourceKey = (key < 0) ? (int)this.sourceid++ : key;
        this.eventSequence.add( (end < 0)
            ? (new InstEvent(sourceKey, eventIndex, start))
            : (new GeneralEvent(sourceKey, eventIndex, start, end)) ); 
    }
    
    public void add(int typeid, double start, double end, int key)
    {  
        EventFactor.EventType eventType = this.eventTypes.get(typeid);
        update(eventType.id, start, end, key); 
    }
    public void add(int eventIndex, double start) { add(eventIndex, start, -1, -1); }
    public void add(int eventIndex, double start, double end) { add(eventIndex, start, end, -1); }
    public void add(int eventIndex, double start, int key) { add(eventIndex, start, -1, key); }
   
    public void add(String eventName, Double start, Double end, Integer key) 
    {
        if (legendMap != null) {
            String tevent = legendMap.get(eventName);
            if (tevent != null) eventName = tevent;
        }

        int eventIndex = this.eventTypes.put(eventName);
        if (key == null) key = new Integer(-1);
        if (end == null) end = new Double(-1.0);
        update(eventIndex, start.doubleValue(), end.doubleValue(), key.intValue());
    }
    public void add(String event, Double start) { add(event, start, null, null); }
    public void add(String event, Double start, Double end) { add(event, start, end, null); }
    public void add(String event, Double start, Integer key) { add(event, start, null, key); }
   
    /**
     * If the constraints are present then the event is allowed if
     * it meets the constraint.  If the constraint is not present 
     * then the event is added.
     */
    @Deprecated
    public boolean add(List<Interval> constraints, String event, Double start, Double end)
    { return add(constraints, event, start, end, null); }
      
    public boolean add(List<Interval> constraints, String event, Double start, Double end, Integer key) 
    {
            if (event == null || start == null) {
            if (event == null && start == null) {
                System.err.println("bad event stream: both type and time are null");
            } else
            if (event == null) {
                System.err.println("bad event stream: type is null at time: " + start.toString());
            } else
            if (start == null) {
                System.err.println("bad event stream: time is null when type: " + event);
            }
            return false;
        }
        
        if (constraints == null || constraints.size() < 1) {
            this.add(event,start,end, key);
            return true;
        }
        if (timeConstraints == null) { 
            this.add(event,start,end, key); 
            return true;
        }
        
        if (!TimeConstraint.check(constraints, start)) { return true; } 
          
        this.add(event,start,end, key);
        return true;
    }    
    
    public IEvent get(Integer ix) {
         return this.eventSequence.get(ix);
    }
    public IEvent get(int ix) {
         return this.eventSequence.get(ix);
    }
    
    public void set(Integer ix, IEvent event) {
        this.eventSequence.set(ix,event);
    }
    public void set(Integer ix, Integer eventIndex, Double start, Double end, Integer key) 
    {
        int sourceKey = (key == null || key < 0) ? (int)this.sourceid++ : key;
        this.eventSequence.set( ix, 
                (end == null || end < 0)
            ? (new InstEvent(sourceKey, eventIndex, start))
            : (new GeneralEvent(sourceKey, eventIndex, start, end)) );
    }
    
    /**
     * Retrieve all the event's id types in a single array.
     * @return the type ids as a single array.
     */
    public double[] getTypeArray() {
        double[] result = new double[this.eventSequence.size()];
        for( int ix = 0; ix < this.eventSequence.size(); ix++ ) {
            result[ix] = this.eventSequence.get(ix).getEventType();
        }
        return result;
    }
    /**
     * Retrieve all the event's ordinal values in a single array.
     * @return the type ids as a single array.
     */
    public int[] getOrdinalArray() {
        int[] result = new int[this.eventSequence.size()];
        for( int ix = 0; ix < this.eventSequence.size(); ix++ ) {
            int eventId = this.eventSequence.get(ix).getEventType();
            result[ix] = this.eventTypes.get(eventId).ordinal;
        }
        return result;
    }
    /**
     * Retrieve all the event's specifiers
     * @param specifier the type of specifier required
     * @return the type ids as a single array.
     */
    public String[] getTypeNameArray() {
        String[] result = new String[this.eventSequence.size()];
        for( int ix = 0; ix < this.eventSequence.size(); ix++ ) {
            result[ix] = 
              this.eventTypes.getName(this.eventSequence.get(ix).getEventType());
        }
        return result;            
    }
     /**
     * Retrieve all the event's id types in a single array.
     * @return the type ids as a single array.
     */
    public double[] getStartTimeArray() {
        double[] result = new double[this.eventSequence.size()];
        for( int ix = 0; ix < this.eventSequence.size(); ix++ ) {
            result[ix] = this.eventSequence.get(ix).getStartTime();
        }
        return result;
    }
    /**
     * Retrieve all the event's durations in a single array.
     * @return the type ids as a single array.
     */
    public double[] getDurationArray() {
        double[] result = new double[this.eventSequence.size()];
        for( int ix = 0; ix < this.eventSequence.size(); ix++ ) {
            result[ix] = this.eventSequence.get(ix).getDuration();
        }
        return result;
    }
    /**
     * Retrieve all the event's durations in a single array.
     * @return the type ids as a single array.
     */
    public long[] getKeyArray() {
        long[] result = new long[this.eventSequence.size()];
        for( int ix = 0; ix < this.eventSequence.size(); ix++ ) {
            result[ix] = this.eventSequence.get(ix).getSourceId();
        }
        return result;
    }
    
    public void removeMarkers() {
        for( IEvent event : this.eventSequence ) {
            event.removeMarker();
        }
    }
    
    public double getMarkerRatio() {
        int eventCount = 0;
        int episodicEventCount = 0;
        for( IEvent event : new LocalIterable() ) {
            eventCount++;
            if (!event.hasActiveMarker()) continue;
            
            Object obj = event.getMarker();
            if (!(obj instanceof Integer)) continue;
            episodicEventCount++;
        }
        return (double)episodicEventCount / (double)eventCount;
    }
}
