/*
 * ParallelAutomaton.java
 *
 * Created on April 9, 2007, 1:00 PM
 *
 */

package edu.iisc.tdminercore.data;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Debprakash Patnaik <debprakash@gmail.com>
 */
public class ParallelAutomaton
{
    protected IEpisode episode = null;
    protected int index;
    protected int size = 0;
    protected Pair[] pairs = null;
    protected IEvent lastTransit = null;
    /** Creates a new instance of ParallelAutomaton */
    public ParallelAutomaton(IEpisode e, int index)
    {
        this.index = index;
        this.episode = e;
        ArrayList<Pair> pList = new ArrayList<Pair>();
        for(int event : e.getEventTypeIndices())
        {
            boolean notFound = true;
            for(Pair p : pList)
            {
                if (p.getEvent() == event)
                {
                    p.increment();
                    notFound = false;
                    break;
                }
            }
            if (notFound) pList.add(new Pair(event));
        }
        pairs = pList.toArray(new Pair[pList.size()]);
    }
    
    public int[] getIndices()
    {
        int[] ret = new int[pairs.length];
        for(int i = 0; i < pairs.length; i++) ret[i] = pairs[i].getEvent();
        return ret;
    }
    
    public String toString()
    {
        StringBuffer b = new StringBuffer();
        for(Pair p : pairs) 
            b.append("(" + p.getEvent() + "," + p.getCount() + ") ");
        b.append(this.size);
        return b.toString();
    }
    
    public void reset()
    {
        this.size = 0;
        for(Pair p : pairs)
        {
            p.getTList().clear();
        }
    }
    
    public void correctTLists(double episodeExp)
    {
        for(Pair p : pairs)
        {
            while(!p.getTList().isEmpty() && 
                 (lastTransit.getStartTime() - p.getTList().getFirst().getStartTime()
                 >= episodeExp))
            {
                p.getTList().remove();
                size --;
            }
        }
    }
    
    public void addEvent(IEvent newEvent)
    {
        int event = newEvent.getEventType();
        for(Pair p : pairs)
        {
            if (p.getEvent() == event)
            {
                p.addEvent(newEvent);
                break;
            }
        }
    }
    
    public class Pair
    {
        private int event;
        private int count;
        private LinkedList<IEvent> tList;
        public Pair(int event)
        {
            this.event = event;
            this.count = 1;
            this.tList = new LinkedList<IEvent>();
        }
        public void increment() { this.count ++; }
        public void addEvent(IEvent newEvent)
        {
            lastTransit = newEvent;
            tList.add(newEvent);
            size ++;
            while(tList.size() > count)
            {
                tList.remove();
                size --;
            }
        }

        public int getEvent()
        {
            return event;
        }

        public int getCount()
        {
            return count;
        }

        public LinkedList<IEvent> getTList()
        {
            return tList;
        }
    }

    public int size()
    {
        return size;
    }

    public IEpisode getEpisode()
    {
        return episode;
    }
    public int getEpisodeIndex()
    {
        return index;
    }

    public Pair[] getPairs()
    {
        return pairs;
    }
}
