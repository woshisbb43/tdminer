/*
 * AbstractEpisode.java
 *
 * Created on January 8, 2007, 3:23 PM
 *
 */
package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.data.IEpisode.EpisodeInstance;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.PickMatrix;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * An episode has little meaning without a definition of
 * the events from which it is built.
 * This set of definitions is not carried by the episodes themselves.
 * Rather, it is left to the different episode containers.
 * The episode, then, does not exist without its container.
 *
 * The episode is a simple graph, as a graph it is composed of nodes and edges.
 * The events are the nodes, while the intervals are edges.
 *
 * An episode may be characterized by the following factors:
 * - non-overlap v. overlap: can two episodes of the same type overlap in time?
 * - episode closed v. open: does the episode expire?
 * - non-interleaf v. interleaf: a specific interval
 * - parallel v. serial
 *
 * - interval constraints: the time between events is constrained.
 *
 * The implementation of the episode with intervals.
 * Each episode keeps a reference to the intervals list.
 * Each episode keeps an array of indicies into that intervals list.
 * It is this array that qualifies the intervals between the events.
 *
 *
 * @author Deb
 * @author phreed@gmail.com
 * @see EpisodeSet - an episode collection
 * @see Epigraph - an episode collection
 *
 */
public abstract class AbstractEpisode
        implements IEpisode, Comparable, Iterable<IEpisode.EpisodeInstance>
{

    static final boolean DEBUG = false; // false to remove debugging
    // event is the id for the event types
    protected int[] event;
    private EventFactor factor;    // init is a sequencing value (usually a timestamp).
    //protected double[] init;
    /**
     * The votes record how many times the episode is found.
     */
    protected int[] votes = new int[1];
    protected int[] samplesize = new int[1];
    protected double[] requiredVotes = new double[1];  // there must be at least 2 to be meaningful.
    /**
     * When an episode is nominated it is expected to occur at some frequency.
     * Suppose that an episode is nominated from two episodes of lesser order.
     * e.g. A is observed to occur 20 times in a 1 minute period and
     * B is observed to occur 100 times in the same 1 minute period.
     * The frequencies are respectively 20 Hz and 100 Hz.
     * The expected frequency of an event A followed by B, without an
     * intervening A, is (something less than 20).
     * It is expected that this frequency will be useful when the votes are
     * tallied.
     */
    protected int expectedFrequency = 0;    // not sure what counter is for but it has something to do with parallel episodes.
    protected int counter = 0;
    protected boolean selected = false;
    protected int index = -1;
    /* an list of indecies into the intervals list
     * it may actually be more efficient to store references
     * to the items in the list rather than indecies.
     * For now though it will stay thus.
     * If the intervals vector is null it is equivalent to
     * the intervals all pointing to the same unlimited interval.
     */
    /** list of  intervals */
    protected List<Interval> intervalsList;
    protected int[] interval;    // Durations
    protected PickMatrix<Interval> durationPick;
    protected boolean deleted = false;
    private double[] Estr;    // Episode instances
    private List<EpisodeInstance> instanceList;
    private double[] beta;

    /** Creates a new instance of Episode */
    public AbstractEpisode(int size, EventFactor f)
    {
        this.event = new int[size];
        this.factor = f;
    //this.init = new double[size];
    }

    public AbstractEpisode(int[] events, EventFactor f)
    {
        this.event = events;
        this.factor = f;
    //this.init = new double[event.length];
    }

    protected AbstractEpisode()
    {
    }

    /** Creates a new instance of an episode
     * The number of intervals will be one less than the number of events
     * as the intervals separate events.
     * @param size the order of the episode.
     * @param intervalsList a list of intervals
     */
    public AbstractEpisode(int size, EventFactor f, List<Interval> intervalsList)
    {
        this(size, f);
        this.interval = new int[size - 1];
        this.intervalsList = intervalsList;
    }

    public AbstractEpisode(int[] events, EventFactor f, int[] interval, List<Interval> intervalsList)
    {
        this(events, f);
        this.interval = interval;
        this.intervalsList = intervalsList;
    }

    public static List<IEpisode> fromString(String line, EventFactor eventTypes)
            throws IEpisode.TypeMisMatchException
    {
        List<IEpisode> result = new ArrayList<IEpisode>(1);
        try
        {
            result.add(ComboEpisode.getEpisode(line, eventTypes));
        }
        catch (Exception ex)
        {
            System.err.println(ex.getLocalizedMessage());
            throw new IEpisode.TypeMisMatchException("bad episode: " + line);
        }
        return result;
    }

    /**
     * The original getEpisode creates an episode from a string of the form...
     * e.g.
     *  A B C : 34
     * Where 'A', 'B', and 'C' are the event names and 34 is the count.
     * @param
     */
    public static IEpisode getEpisode(String line, EventFactor eventTypes, List<Interval> ivlList)
            throws Exception
    {

        int[] freq = new int[1];
        String[] part = line.split(":");

        String[] epart = part[0].trim().split(" ");
        if (part.length == 2)
        {
            freq[0] = Integer.parseInt(part[1].trim());
        }
        return getEpisode(epart, freq, eventTypes);
    }

    /**
     * Construct an serial episode from a set of event type names.
     * @param epart the names of the event types
     * @param freq the number of times this episode was seen
     * @param eventTypes the event factor from which the event types are drawn
     * @return the built episode.
     */
    public static IEpisode getEpisode(String[] epart, int[] vote, EventFactor eventTypes)
            throws IEpisode.TypeMisMatchException
    {
        Episode episode = new Episode(epart.length, eventTypes);
        episode.setVotes(vote);

        for (int ix = 0; ix < epart.length; ix++)
        {
            int eventType = eventTypes.getId(epart[ix]);
            if (eventType < 0)
            {
                throw new IEpisode.TypeMisMatchException("Event Type mismatch in episode set and available sequence");
            }
            episode.setEvent(ix, eventType);
        }
        return episode;
    }

    /**
     * The definitions of the events type ids.
     * @return the event factor
     */
    public EventFactor getEventFactor()
    {
        return this.factor;
    }

    public Iterator<EpisodeInstance> iterator()
    {
        if (this.instanceList == null)
        {
            return new ArrayList<EpisodeInstance>().iterator();
        }
        return this.instanceList.iterator();
    }

    /**
     * base implementations
     */
    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public boolean isDeleted()
    {
        return deleted;
    }

    public void setIntervalId(int interEventIndex, int intervalId)
    {
        this.interval[interEventIndex] = intervalId;
    }

    public void setInterval(int interEventIndex, Interval interval)
    {
        int intervalId = 0;
        for (ListIterator<Interval> it = this.intervalsList.listIterator();
                it.hasNext(); intervalId++)
        {
            if (!it.next().equals(interval))
            {
                continue;
            }
            this.interval[interEventIndex] = intervalId;
            break;
        }
    }

    public int getIntervalId(int index)
    {
        return this.interval[index];
    }

    public Interval getInterval(int index)
    {
        if (this.intervalsList == null)
        {
            return null;
        }
        return this.intervalsList.get(this.interval[index]);
    }

    public boolean hasFiniteIntervals()
    {
        return this.interval == null ? false : true;
    }

    /**
     * Write the episode signature to a string.
     * @return the serialized signature ids and count.
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int kx = 0; kx < size(); kx++)
        {
            if (kx != 0)
            {
                if (interval != null)
                {
                    buf.append("(" + interval[kx - 1] + ")");
                }
                buf.append(" ");
            }
            buf.append(getEvent(kx));
        }
        buf.append(":" + getVotes());
        return buf.toString();
    }

    /**
     * Write the episode signature to a string.
     * @param eventTypes a dictionary of event types
     * @return the serialized signature and count.
     */
    public String toString(EventFactor eventTypes, int index)
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(5);
        StringBuffer buf = new StringBuffer();
        if (this.size() < 1)
        {
            return new String("");
        }
        EventFactor.EventType type = eventTypes.get(getEvent(0));
        buf.append(type.name);
        if (interval == null)
        {
            for (int kx = 1; kx < size(); kx++)
            {
                type = eventTypes.get(getEvent(kx));
                buf.append("-" + type.name);
            }
        }
        else
        {
            for (int kx = 1; kx < size(); kx++)
            {
                if (intervalsList != null && intervalsList.size() != 0)
                {
                    buf.append("[" + intervalsList.get(interval[kx - 1]).toString() + "]");
                }
                type = eventTypes.get(getEvent(kx));
                buf.append("-" + type.name);
            }
            if (index != -1 && getEstr() != null)
            {
                if (size() == 2 && beta != null)
                {
                    buf.append(" #(" + nf.format(Estr[index]) + "," + nf.format(beta[index]) + ")");
                }
                else
                {
                    buf.append(" #(" + nf.format(Estr[index]) + ")");
                }
            }
        }
        return buf.toString();
    }

    /**
     * Write the episode signature to a string.
     * @param eventTypes a dictionary of event types
     * @return the serialized signature and count.
     */
    public String toString(EventFactor eventTypes)
    {
        return toString(eventTypes, -1);
    }

    /**
     * Write the episode legend signature to a string.
     * @param eventTypes a dictionary of event types
     * @return the serialized legend signature.
     */
    public String sigLegend(EventFactor eventTypes)
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        StringBuffer buf = new StringBuffer();
        if (this.size() < 1)
        {
            return new String("");
        }
        EventFactor.EventType type = eventTypes.get(getEvent(0));
        buf.append(type.ordinal);
        if (interval == null)
        {
            for (int kx = 1; kx < size(); kx++)
            {
                type = eventTypes.get(getEvent(kx));
                buf.append("-" + type.ordinal);
            }
        }
        else
        {
            for (int kx = 1; kx < size(); kx++)
            {
                if (getEstr() == null)
                {
                    buf.append("[" + intervalsList.get(interval[kx - 1]).toString() + "]");
                }
                else
                {
                    buf.append("[" + intervalsList.get(interval[kx - 1]).toString() + "," + nf.format(getEstr()[kx - 1]) + "]");
                }
                type = eventTypes.get(getEvent(kx));
                buf.append("-" + type.ordinal);
            }
        }
        return buf.toString();
    }

    public List<Interval> getIntervalsList()
    {
        return intervalsList;
    }

    public double getRequiredVotes(int index)
    {
        return requiredVotes[index];
    }

    public abstract void evaluateRequiredVotes(SessionInfo session, Object[] params);

    public int getLevel()
    {
        return this.event.length;
    }

    public void setEvent(int index, int type)
    {
        this.event[index] = type;
    }

    public int getEvent(int index)
    {
        return this.event[index];
    }

    public void incrCounter()
    {
        counter++;
    }

    public void decrCounter()
    {
        counter--;
    }

    public int getCounter()
    {
        return counter;
    }

    public void setCounter(int counter)
    {
        this.counter = counter;
    }

    public void initVotes(int num_segs)
    {
        votes = new int[num_segs];
    }

    public void incrVotes(int index)
    {
        votes[index]++;
    }

    public int getVotes(int index)
    {
        return votes[index];
    }

    public void setVotes(int index, int votes)
    {
        this.votes[index] = votes;
    }

    public int[] getVotes()
    {
        return votes;
    }

    public void setVotes(int[] votes)
    {
        this.votes = new int[votes.length];
        for (int i = 0; i < votes.length; i++)
        {
            this.votes[i] = votes[i];
        }
    }

    public String getVoteString()
    {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < votes.length; i++)
        {
            if (i != 0)
            {
                str.append(",");
            }
            str.append(votes[i]);
        }
        return str.toString();
    }

    public String getRequiredVoteString()
    {
        StringBuilder str = new StringBuilder();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(3);

        for (int i = 0; i < votes.length; i++)
        {
            if (i != 0)
            {
                str.append(",");
            }
            str.append(nf.format(requiredVotes[i]));
        }
        return str.toString();
    }

    public void resetVotes()
    {
        for (int i = 0; i < this.votes.length; i++)
        {
            this.votes[i] = 0;
        }
    }

    public void resetCounter()
    {
        this.counter = 0;
    }

    public double getFrequency(int index)
    {
        return (double) this.getVotes(index) / (double) this.getSampleSize(index);
    }

    public void setSampleSize(int index, int size)
    {
        this.samplesize[index] = size;
    }

    public int getSampleSize(int index)
    {
        return this.samplesize[index];
    }

    public int size()
    {
        return event.length;
    }

    /**
     * The comparison proceeds in steps
     * - size
     * - event types
     * - inter-event intervals have overlap (or equivalence)
     */
    public int compareTo(Object o)
    {
        if (!(o instanceof AbstractEpisode))
        {
            throw new ClassCastException("Must be of class AbstractEpisode");
        }
        AbstractEpisode that = (AbstractEpisode) o;
        if (this.size() < that.size())
        {
            return -1;
        }
        if (this.size() > that.size())
        {
            return 1;
        }
        if (this.factor.equals(that.getEventFactor()))
        {
            for (int ix = 0; ix < event.length; ix++)
            {
                if (this.event[ix] < that.event[ix])
                {
                    return -1;
                }
                if (this.event[ix] > that.event[ix])
                {
                    return 1;
                }
            }
        }
        else
        {
            for (int ix = 0; ix < event.length; ix++)
            {
                int result = EventFactor.compareEvent(
                        this.event[ix], this.factor,
                        that.event[ix], that.getEventFactor());
                if (result < 0)
                {
                    return -1;
                }
                if (result > 0)
                {
                    return 1;
                }
            }
        }


        // }
        // if (this.intervalsList == null) return 0;
        return 0;
    }

    public int getFirstEvent()
    {
        return event[0];
    }

    public int getLastEvent()
    {
        return event[event.length - 1];
    }

    public int[] getEventTypeIndices()
    {
        return event;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    /** 
     * Check that each event has a corresponding event.
     * @param o an episode to compare
     * @return do they match?
     */
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        if (!o.getClass().isInstance(this))
        {
            return false; // are they of the same subclass?
        }
        if (!(o instanceof Comparable))
        {
            return false;
        }
        Comparable item = (Comparable) o;
        if (0 == item.compareTo(this))
        {
            return true;
        }
        return false;
    }

    /**
     * A permutation is...?
     */
    public boolean isPermutation(IEpisode e)
    {
        boolean ret = false;
        if (size() != e.size())
        {
            return false;        //Locate the first alphabet of array in eps
        }
        int n = size();
        for (int i = 0; i < n; i++)
        {
            if (e.getEvent(i) == event[0])
            {
                ret = true;
                for (int j = 1; j < n; j++)
                {
                    if (event[j] != e.getEvent((i + j) % n))
                    {
                        ret = false;
                        break;
                    }
                }
                if (ret)
                {
                    return true;
                }
            }
        }
        return ret;
    }

    public void clearInstances()
    {
        if (this.instanceList == null)
        {
            return;
        }
        this.instanceList = null;
    }

    public void addInstance(List<IEvent> events)
    {
        if (this.instanceList == null)
        {
            this.instanceList = new ArrayList<EpisodeInstance>();
        }
        this.instanceList.add(new EpisodeInstance(events));
    }

    public boolean isHarvested()
    {
        return (this.instanceList == null) ? false : true;
    }

    public double[] getTimeArray(int ordinal)
    {
        if (this.instanceList == null)
        {
            return new double[0];
        }
        double[] times = new double[this.instanceList.size()];

        int jx = 0;
        for (EpisodeInstance eachInstance : this.instanceList)
        {
            times[jx] = eachInstance.eventList.get(ordinal).getStartTime();
            jx++;
        }
        return times;
    }

    /**
     * Get the times of the constituent events as a ragged array.
     * @return one row for each instance
     */
    public double[][] asArray()
    {
        return getTimeArray();
    }

    /**
     * Return the timestamps associated with a set of episodes instances.
     * @return a list of episode instance timestamps.
     */
    /*
     * This is done in a way that will work for serial and parallel episodes
     * with repeated event types.
     * The problem with parallel episodes is that the times are recorded
     * in temporal order which will likely be different for each instance.
     */
    public double[][] getTimeArray()
    {
        double[] span =
        {
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        };
        return getTimeArray(span, ALL_IN_RANGE);
    }

    /**
     * Return the timestamps associated with a set of episodes instances.
     * @param span the time span into which the episodes must fall
     * @param mode how much of the
     * @return a list of episode instance timestamps.
     */
    public double[][] getTimeArray(double[] span, int mode)
    {
        if (this.instanceList == null)
        {
            return new double[0][0];        // we need to make two passes, once for the size...
        }
        int count = 0;
        if (Double.NEGATIVE_INFINITY < span[0] || span[1] < Double.POSITIVE_INFINITY)
        {
            for (EpisodeInstance eachInstance : this.instanceList)
            {
                if (isInRange(eachInstance.eventList, span, mode))
                {
                    count++;
                }
            }
        }
        else
        {
            count = this.instanceList.size();
        }
        // ...and once to populate the instances.
        double[][] results = new double[count][];

        int jx = 0;
        for (EpisodeInstance eachInstance : this.instanceList)
        {
            results[jx] = new double[eachInstance.eventList.size()];
            boolean[] usedFlag = new boolean[eachInstance.eventList.size()];
            for (int i = 0; i < usedFlag.length; i++)
            {
                usedFlag[i] = false;
            }
            for (IEvent eachEvent : eachInstance.eventList)
            {
                if (!isInRange(eachInstance.eventList, span, mode))
                {
                    continue;
                }
                for (int ix = 0; ix < this.event.length; ix++)
                {
                    if (this.event[ix] == eachEvent.getEventType() && !usedFlag[ix])
                    {
                        results[jx][ix] = eachEvent.getStartTime();
                        usedFlag[ix] = true;
                        break;
                    }
                }
            }
            jx++;
        }
        return results;
    }

    /**
     * Return the time spans associated with a set of episodes instances.
     * @return a list of episode instance time spans.
     */
    /*
     * This is done in a way that will work for serial and parallel episodes
     * with repeated event types.
     * The problem with parallel episodes is that the times are recorded
     * in temporal order which will likely be different for each instance.
     */
    public double[][] getIntervalArray()
    {
        double[] span =
        {
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        };
        return getIntervalArray(span, ALL_IN_RANGE);
    }

    /**
     * Return the time spans associated with a set of episodes instances.
     * @param span the time span into which the episodes must fall
     * @param mode how much of the
     * @return a list of episode instance time spans.
     */
    public double[][] getIntervalArray(double[] span, int mode)
    {
        if (this.instanceList == null)
        {
            return new double[0][0];        // we need to make two passes, once for the size...
        }
        int count = 0;
        if (Double.NEGATIVE_INFINITY < span[0] || span[1] < Double.POSITIVE_INFINITY)
        {
            for (EpisodeInstance eachInstance : this.instanceList)
            {
                if (isInRange(eachInstance.eventList, span, mode))
                {
                    count++;
                }
            }
        }
        else
        {
            count = this.instanceList.size();
        }
        // ...and once to populate the instances.
        double[][] results = new double[count][];

        int jx = 0;
        for (EpisodeInstance eachInstance : this.instanceList)
        {
            if (!isInRange(eachInstance.eventList, span, mode))
            {
                continue;
            }
            results[jx] = new double[eachInstance.eventList.size() - 1];

            boolean[] usedFlag = new boolean[eachInstance.eventList.size()];
            for (int i = 0; i < usedFlag.length; i++)
            {
                usedFlag[i] = false;
            }
            boolean firstFlag = true;
            double timestamp = 0.0D;
            for (IEvent eachEvent : eachInstance.eventList)
            {
                for (int ix = 0; ix < this.event.length; ix++)
                {
                    if (usedFlag[ix])
                    {
                        continue;
                    }
                    if (this.event[ix] != eachEvent.getEventType())
                    {
                        continue;
                    }
                    if (firstFlag)
                    {
                        firstFlag = false;
                        timestamp = eachEvent.getStartTime();
                        break;
                    }

                    results[jx][ix - 1] = eachEvent.getStartTime() - timestamp;
                    timestamp = eachEvent.getStartTime();
                    usedFlag[ix] = true;
                    break;
                }
            }
            jx++;
        }
        return results;
    }

    /**
     * Get the keys of the constituent events as a ragged array.
     * These can be used to look up detailed information from
     * the original event source.
     *
     * @return one row for each instance
     */
    public long[][] getKeyArray()
    {
        double[] span =
        {
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        };
        return getKeyArray(span, ALL_IN_RANGE);
    }

    public long[][] getKeyArray(double[] span, int mode)
    {
        if (this.instanceList == null)
        {
            return new long[0][0];        // we need to make two passes, once for the size...
        }
        int count = 0;
        if (Double.NEGATIVE_INFINITY < span[0] || span[1] < Double.POSITIVE_INFINITY)
        {
            for (EpisodeInstance eachInstance : this.instanceList)
            {
                if (isInRange(eachInstance.eventList, span, mode))
                {
                    count++;
                }
            }
        }
        else
        {
            count = this.instanceList.size();
        }
        // ...and once to populate the instances.
        long[][] results = new long[count][];

        int jx = 0;
        for (EpisodeInstance eachInstance : this.instanceList)
        {
            results[jx] = new long[eachInstance.eventList.size()];
            boolean[] usedFlag = new boolean[eachInstance.eventList.size()];
            for (int i = 0; i < usedFlag.length; i++)
            {
                usedFlag[i] = false;
            }
            for (IEvent eachEvent : eachInstance.eventList)
            {
                if (!isInRange(eachInstance.eventList, span, mode))
                {
                    continue;
                }
                for (int ix = 0; ix < this.event.length; ix++)
                {
                    if (this.event[ix] == eachEvent.getEventType() && !usedFlag[ix])
                    {
                        results[jx][ix] = eachEvent.getSourceId();
                        usedFlag[ix] = true;
                        break;
                    }
                }
            }
            jx++;
        }
        return results;
    }

    private static boolean isInRange(List<IEvent> ax, double[] span, int mode)
    {
        if (Double.NEGATIVE_INFINITY == span[0] && span[1] == Double.POSITIVE_INFINITY)
        {
            return true;
        }
        double firstTime = ax.get(0).getStartTime();
        double lastTime = ax.get(ax.size() - 1).getStartTime();

        switch (mode)
        {
            case ALL_IN_RANGE:
                if (firstTime < span[0])
                {
                    return false;
                }
                if (span[1] < lastTime)
                {
                    return false;
                }
            case START_IN_RANGE:
                if (firstTime < span[0])
                {
                    return false;
                }
                if (firstTime > span[1])
                {
                    return false;
                }
                break;
            case STOP_IN_RANGE:
                if (lastTime < span[0])
                {
                    return false;
                }
                if (lastTime > span[1])
                {
                    return false;
                }
                break;
            case SPAN_RANGE:
                if (lastTime < span[0])
                {
                    return false;
                }
                if (firstTime > span[1])
                {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public int getInstanceCount()
    {
        return this.instanceList.size();
    }

    /**
     * count how many instances there are within the time frame.
     * This is very similar to 'isInRange'.
     * @param span the time span over which the count is taken.
     * @return the number of instances found.
     */
    public int[] getInstanceCount(double[] span)
    {
        int[] counter = new int[4];
        if (this.instanceList == null)
        {
            return counter;
        }
        for (EpisodeInstance ax : this.instanceList)
        {
            double firstTime = ax.eventList.get(0).getStartTime();
            double lastTime = ax.eventList.get(ax.eventList.size() - 1).getStartTime();

            if (span[0] < firstTime && lastTime < span[1])
            {
                counter[ALL_IN_RANGE]++;
            }
            if (span[0] < firstTime && firstTime < span[1] && span[1] < lastTime)
            {
                counter[START_IN_RANGE]++;
            }
            if (firstTime < span[0] && span[0] < lastTime && lastTime < span[1])
            {
                counter[STOP_IN_RANGE]++;
            }
            if (firstTime < span[0] && span[1] < lastTime)
            {
                counter[SPAN_RANGE]++;
            }
        }
        return counter;
    }

    public String getInstanceStatistics(double[] span, double sliceCount)
    {
        if (this.instanceList == null)
        {
            return "You have no instances selected";
        }
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;

        int counter = 0;

        double timeSum = 0.0;
        double timeSS = 0.0;

        int eCard = this.event.length;
        double[] delaySum = new double[eCard - 1];
        double[] delaySS = new double[eCard - 1];
        class EpisodeSet
        {

            public int[] counts = null;

            public EpisodeSet(int size)
            {
                counts = new int[size];
            }
        }
        List<EpisodeSet> binList = new ArrayList<EpisodeSet>(10);
        int binListMax = 0;
        for (EpisodeInstance ax : this.instanceList)
        {
            double firstTime = ax.eventList.get(0).getStartTime();
            double lastTime = ax.eventList.get(ax.eventList.size() - 1).getStartTime();

            if (span[1] < firstTime)
            {
                continue;
            }
            if (lastTime < span[0])
            {
                continue;
            }
            if (firstTime < minimum)
            {
                minimum = firstTime;
            }
            if (maximum < lastTime)
            {
                maximum = lastTime;
            }
            counter++;

            double spanWidth = lastTime - firstTime;
            timeSum += spanWidth;
            timeSS += spanWidth * spanWidth;

            int card = ax.eventList.size();
            if (card < 2)
            {
                continue;
            }
            if (card != eCard)
            {
                continue;
            }
            for (int ix = 1; ix < eCard; ix++)
            {
                IEvent eventA = ax.eventList.get(ix - 1);
                IEvent eventB = ax.eventList.get(ix);
                double delay = eventB.getStartTime() - eventA.getStartTime();

                delaySum[ix - 1] += delay;
                delaySS[ix - 1] += delay * delay;

                int bin = (int) Math.round(Math.floor(delay / sliceCount));
                if (bin + 1 > binListMax)
                {
                    for (; binListMax < bin + 1; binListMax++)
                    {
                        binList.add(binListMax, new EpisodeSet(eCard - 1));
                    }
                }
                EpisodeSet eset = binList.get(bin);
                eset.counts[ix - 1]++;
            }
        }
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, java.util.Locale.US);

        double magnitude = (double) counter;

        if (counter < 1)
        {
            formatter.format(" : no instances found in range [ %10.8g - %10.8g ]\n", span[0], span[1]);
            return sb.toString();
        }
        formatter.format(" : %d [ %10.8g - %10.8g ]\n", counter, minimum, maximum);

        formatter.format(" episode duration: mean(%10.4g) stddev(%10.4g)\n",
                (timeSum / (magnitude - 1.0)),
                Math.sqrt((timeSS / (magnitude - 1.0)) - ((timeSum * timeSum) / (magnitude - 1.0) / magnitude)));

        if (eCard < 2)
        {
            sb.append(" no intervals for single node episodes ");
            return sb.toString();
        }
        sb.append("   episode intervals: \n");

        for (int ix = 0; ix < (eCard - 1); ix++)
        {
            double delayMean = delaySum[ix] / (magnitude);
            double delayVar = (delaySS[ix] / (magnitude - 1.0)) - ((delaySum[ix] * delaySum[ix]) / (magnitude - 1.0) / magnitude);
            formatter.format("    interval: %d  mean(%10.4g) stddev(%10.4g)\n",
                    ix, delayMean, Math.sqrt(delayVar));
            formatter.format("      histo: ");
            int bincnt = 0;
            for (EpisodeSet set : binList)
            {
                formatter.format(" [%8.4g] %5d", sliceCount * bincnt, set.counts[ix]);
                bincnt++;
            }
            formatter.format("\n");
        }

        return sb.toString();
    }

    /**
     * }
     * This method replaces the set of events constituting a episode instance
     * with a synchain event representing the instance.
     * @param events the event stream from which the episode instances occur.
     */
    public void integrate(IEventDataStream events)
    {
        // int count = 0;
        for (EpisodeInstance instance : this.instanceList)
        {
            // System.out.println(count++);
            String episodeName = '(' + this.toString(events.getEventFactor()) + ')';
            events.integrate(episodeName, instance.eventList);
        }
    }

    public void setEstr(double[] estr)
    {
        this.Estr = estr;
    }

    public double[] getEstr()
    {
        return this.Estr;
    }

    public double getEstr(int index)
    {
        if (getEstr() != null)
        {
            return getEstr()[index];
        }
        return 0.0;
    }

    public void postCountProcessing(SessionInfo session)
    {
    }

    protected void createCopy(AbstractEpisode e)
    {
        e.event = this.event;
        e.factor = this.factor;
        e.votes = this.votes;
        e.samplesize = this.samplesize;
        e.requiredVotes = this.requiredVotes;
        e.expectedFrequency = 0;
        e.counter = this.counter;
        e.selected = this.selected;
        e.index = this.index;
        e.intervalsList = this.intervalsList;
        e.interval = this.interval;
        e.durationPick = this.durationPick;
        e.deleted = this.deleted;
        e.setEstr(this.getEstr());
        e.setBeta(this.getBeta());
        e.instanceList = this.instanceList;
    }

    @Override
    public Object clone()
    {
        return this;
    }

    public double getBeta(int index)
    {
        return beta[index];
    }

    public void setBeta(int index, double beta)
    {
        this.beta[index] = beta;
    }

    public void setBeta(double[] beta)
    {
        this.beta = beta;
    }
    public double[] getBeta()
    {
        return this.beta;
    }
}


