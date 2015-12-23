/*
 * EpisodeInstanceSet.java
 *
 * Created on November 20, 2006, 4:16 PM
 *
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.writer.IWriter;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * This class provides a set of Episodes along with their timing information.
 * A set of episodes and their instance information.
 *
 * @author phreed@gmail.com
 */
public class EpisodeInstanceSet
        implements Serializable {
    static final boolean DEBUG = false;
    
    public enum EPISODE_TYPE {
        NONE,
        SEQ, // a.k.a. serial episode
        ALL,  // a.k.a. parallel episode
        PICK }; // a.k.a. branch episode
        
        private EventFactor eventFactor;
        private List<IEpisode> episodeList;
        private IEventDataStream eventSeq;
        
        /** Creates a new instance of EpisodeInstanceSet */
        public EpisodeInstanceSet( IEventDataStream eventSeq ) {
            this.eventFactor = null;
            this.episodeList = new ArrayList<IEpisode>();
            this.eventSeq = null;
        }
        
        public void setDataStream( IEventDataStream seq ) {
            this.eventSeq = seq;
            this.eventFactor = seq.getEventFactor();
        }
        
        public void setEpisodeList(List<IEpisode> episodelist) {
            this.episodeList = episodelist;
        }
        public List<IEpisode> getEpisodeList() {
            return this.episodeList;
        }
        
        public void resetMarkers() {
            this.eventSeq.removeMarkers();
        }
        
        public double getMarkerRate() {
            return this.eventSeq.getMarkerRatio();
        }
        
        public void addEpisode(IEpisode episode) {
            this.episodeList.add(episode);
        }
        
        public void export(IWriter writer) { writer.write(this); }
        
        /**
         * Episodes are grouped by their order or level (i.e. number of events).
         * Each of these groups is then placed in the List.
         * @return - an interator of the episode sets.
         */
        public Iterator<IEpisode> iterator() {
            return this.episodeList.iterator();
        }
        
        /**
         * This method adds the new episodes to the instance set.
         * If the episode is already in the set it does not add a second occurance.
         */
        public void addEpisodeSet(List<IEpisode> episodeList) {
            for(IEpisode episode : episodeList) {
                if (this.episodeList.contains(episode)) continue;
                this.episodeList.add(episode);
            }
        }
        
        public void removeEpisode(int episodeIx) {
            this.episodeList.remove(episodeIx);
        }
        
        public int sizeEpisodeList() {
            return this.episodeList.size();
        }
        
        public IEpisode getEpisode(int ix) {
            return this.episodeList.get(ix);
        }
        
        /**
         * Serialization helper functions
         */
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
        }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            // our "pseudo-constructor"
            in.defaultReadObject();
            // now we are a "live" object again, so let's run rebuild and start
            //startAnimation();
        }
        /**
         * record/add the instance information to its appropriate episode element.
         */
        public void addInstance(int episodeIndex, int[] eventTypes, List<IEvent> events) {
            // check the event types
            this.episodeList.get(episodeIndex).addInstance(events);
        }
        
        public void setEventFactor(EventFactor ets) { this.eventFactor = ets; }
        public EventFactor getEventFactor() { return this.eventFactor; }
        
        public void clear() {
            if (this.episodeList != null) this.episodeList.clear();
        }
        
        /**
         * Given an episode (by its index in the episodeList) and the
         * event type ordinal.
         * @return the timestamps
         */
        public double[] asArray(int episodeIndex, int eventOrdinal) throws Exception {
            return this.episodeList.get(episodeIndex).getTimeArray(eventOrdinal);
        }
        /**
         * Given an episode (by its index in the episodeList).
         * @return  an array whose indices are:
         * - instance index
         * - event type ordinal
         */
        public double[][] asArray(int episodeIndex) throws Exception {
            return this.episodeList.get(episodeIndex).getTimeArray();
        }
        
        /**
         * Get all the timestamps that fall within a time range.
         * @return  an array whose indices are:
         * - episode index
         * - instance index
         * - event type ordinal
         */
        public double[][][] asArray() throws Exception {
            double[] span = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
            return asArray( span, IEpisode.ALL_IN_RANGE );
        }
        public double[][][] asArray(double[] span)
        throws Exception {
            return asArray(span, IEpisode.ALL_IN_RANGE);
        }
        public double[][][] asArray(double[] span, int mode)
        throws Exception {
            span = sortSpan(span);
            
            double[][][] times = new double[this.episodeList.size()][][];
            int ix = 0;
            for(IEpisode episode : this.episodeList) {
                times[ix] = episode.getTimeArray(span,mode);
            }
            return times;
        }
        
        public long[][][] idArray() throws Exception {
            double[] span = { Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };
            return idArray( span, IEpisode.ALL_IN_RANGE );
        }
        public long[][][] idArray(double[] span)
        throws Exception {
            return idArray( span, IEpisode.ALL_IN_RANGE);
        }
        public long[][][] idArray(double[] span, int mode)
        throws Exception {
            span = sortSpan(span);
            
            long[][][] times = new long[this.episodeList.size()][][];
            int ix = 0;
            for(IEpisode episode : this.episodeList) {
                times[ix] = episode.getKeyArray(span,mode);
            }
            return times;
        }
        
        public void clearInstances() {
            for(IEpisode episode : this.episodeList) {
                episode.clearInstances();
            }
        }
        
        /**
         * How many episode instances fall within the time window?
         * @return a list of all the episodes and a count of their
         *   instances that fall within a range.
         */
        public String[] getInstanceCount(double[] span)
        throws Exception {
            span = sortSpan(span);
            
            String[] out = new String[this.episodeList.size()];
            int ix = 0;
            for(IEpisode episode : this.episodeList) {
                int[] counter = episode.getInstanceCount(span);
                
                out[ix] = this.episodeList.get(ix).toString(this.eventFactor)
                + " count: " + counter[IEpisode.ALL_IN_RANGE];
                if (counter[IEpisode.START_IN_RANGE] > 0) out[ix]
                        += " start("+counter[IEpisode.START_IN_RANGE]+")";
                if (counter[IEpisode.STOP_IN_RANGE] > 0) out[ix]
                        += " stop("+counter[IEpisode.STOP_IN_RANGE]+")";
                if (counter[IEpisode.SPAN_RANGE] > 0) out[ix]
                        += " span("+counter[IEpisode.SPAN_RANGE]+")";
                ix++;
            }
            return out;
        }
        private double[] sortSpan(double[] span) {
            if (span[0] <= span[1]) return span;
            
            double tmp = span[0];
            span[0] = span[1];
            span[1] = tmp;
            return span;
        }
        
        /**
         * get all the events, as an array, that fit the range specified.
         */
        
    @Override
        public String toString() {
            String[] info = getStatistics();
            StringBuffer sb = new StringBuffer();
            for( String astring : info ) {
                sb.append(astring + "\n");
            }
            return sb.toString();
        }
        
        public String[] getStatistics() {
            double[] span = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
            return this.getStatistics(span,10);
        }
        
        public String[] getStatistics(double[] span) {
            return this.getStatistics(span,10); }
        
        public String[] getStatistics(double[] span, double sliceCount) {
            span = sortSpan(span);
            
            String[] out = new String[this.episodeList.size()];
            int jx = 0;
            for(IEpisode episode : this.episodeList) {
                out[jx] = episode.toString(this.eventFactor)
                + episode.getInstanceStatistics(span, sliceCount);
                jx++;
            }
            return out;
        }
        
}
