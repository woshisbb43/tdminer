/*
 * SerialEpisodeCounterWithIntervals.java
 *
 * Created on July 31, 2007, 4:00pm
 *
 */

package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.IEventIterable;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Debprakash Patnaik
 */
public class SerialOverlapCounterWithIntervals extends AbstractSerialCounterInterEventConst
{   static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of SerialTrueIntervalCounter */
    public SerialOverlapCounterWithIntervals()
    {if (DEBUG) System.out.println("SerialEpisodeCounterWithIntervals: <constructor>");
    }
    
    @Override
    public void countEpisodes(List<IEpisode> episodes, IObserver observer,
            SessionInfo session)
            throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        if (DEBUG) System.out.println("SerialEpisodeCounterWithIntervals: count episodes");
        IEventDataStream sequence = session.getSequence();
        
        List<Interval> intervalsList = session.getIntervalsList();
        if (intervalsList == null || intervalsList.size() == 0)
        {
            throw new RuntimeException("This algorithm for interval discovery" +
                    " cannot work without a list of intervals");
        }
        if (episodes != null && episodes.size() != 0)
        {
            IEpisode e = episodes.get(0);
            if (!(e.hasFiniteIntervals()))
            {
                throw new RuntimeException("This counting algorithm " +
                        "must be used only with episode discovery with intervals");
            }
        }
        observer.startup();
        session.resetSegIndex();
        List<List<IntervalNodeTList2>> waits = new ArrayList<List<IntervalNodeTList2>>(sequence.getEventTypeCount());
        
        observer.update(0);
        for (int i = 0; i < sequence.getEventTypeCount(); i++)
        {
            waits.add(new ArrayList<IntervalNodeTList2>());
        }
        
        for(int i = 0; i < episodes.size(); i++)
        {
            if (episodes.get(i).hasFiniteIntervals())
            {
                IEpisode e = episodes.get(i);
                e.resetVotes();
                int num_segs = 1;
                if (session.isSegmented()) num_segs = session.getSegIndexLen();
                e.initVotes(num_segs);
                
                int A = e.getFirstEvent();
                IntervalNodeTList2 n = IntervalNodeTList2.createNode(e.size(), i);
                waits.get(A).add(n);
            }
            else
            {
                throw new RuntimeException("The interval discovery based algorithm can only display episodes discovered by it.\n" +
                        "Please remove any episode entered manually");
            }
        }
        
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
            int E_i = event.getEventType();
            double t_i = event.getStartTime();
            session.updateSegIndex(t_i);
            
            IntervalListNode2 listNode = null;
            
            List<IntervalNodeTList2> autoList = waits.get(E_i);
            int nsize = autoList.size();
            // System.out.println("nsize = " + nsize);
            for (int i = 0; i < nsize; i++)
            {
                IntervalNodeTList2 node = autoList.get(i);
                int alphano = node.getEpisodeIndex();
                IEpisode alpha = episodes.get(alphano);
                boolean isAccepted = false;
                
                List<IntervalListNode2> tList = node.getTList();
                
                if (node.getIndex() == 0)
                {
                    listNode = new IntervalListNode2(null, event);
                    tList.add(listNode);
                    isAccepted = true;
                    if (node.isIsNew())
                    {
                        node.setIsNew(false);
                        IntervalNodeTList2 next = node.getNext();
                        if (next != null)
                        {
                            int nextEventType = alpha.getEvent(next.getIndex());
                            waits.get(nextEventType).add(next);
                        }
                    }
                }
                else
                {
                    List<IntervalListNode2> prevTList = node.getPrev().getTList();
                    Interval interval = alpha.getInterval(node.getPrev().getIndex());
                    double tIntervalExp = interval.getTHigh();
                    double tIntervalExpLow = interval.getTLow();
                    for (int j = prevTList.size() - 1; j >= 0; j--)
                    {
                        IntervalListNode2 prevListNode = prevTList.get(j);
                        double prevTValue = prevListNode.getTValue();
                        
                        double deltaT = t_i - prevTValue;
                        if ((t_i != prevTValue) && (tIntervalExpLow <= deltaT && deltaT < tIntervalExp))
                        {
                            listNode = new IntervalListNode2(prevListNode, event);
                            tList.add(listNode);
                            isAccepted = true;
                            if (node.isIsNew())
                            {
                                node.setIsNew(false);
                                if (node.getIndex() < alpha.size() - 1)
                                {
                                    IntervalNodeTList2 next = node.getNext();
                                    int nextEventType = alpha.getEvent(next.getIndex());
                                    waits.get(nextEventType).add(next);
                                }
                            }
                            break;
                        }//if
                    }//for
                }//else
                
                if (!isAccepted) continue;
                if (node.getIndex() != alpha.size() - 1) continue;
                
                alpha.incrVotes(session.getCurrentSegIndex());
                if (session.isTrackEpisodes())
                {
                    List<IEvent> events = new ArrayList<IEvent>(alpha.size());
                    for(IntervalListNode2 alistNode = listNode;
                    alistNode != null;
                    alistNode = alistNode.getPrevNode() )
                    {
                        events.add(0,alistNode.getEvent());
                    }
                    if (events.size() < alpha.size())
                    {
                        continue;
                    }
                    observer.handleEpisodeCompletion(alphano, alpha.getEventTypeIndices(), events);
                }
                
                IntervalNodeTList2 delNode = node;
                IntervalListNode2 delListNode = listNode;
//                while(delNode != null && delListNode != null)
//                {
//                    List<IntervalListNode2> nodes = delNode.getTList();
//                    int index = nodes.indexOf(delListNode);
//                    if (index != -1)
//                    {
//                        nodes.subList(0, index).clear();
//                    }
//                    else
//                    {
//                        throw new RuntimeException("Unable to find node in tlist : Line 183");
//                    }
//                    
//                    delNode = delNode.getPrev();
//                    delListNode = delListNode.getPrevNode();
//                }
            }//for
        }
        iterable.setSampleSize(episodes);
        
        observer.shutdown();
//	observer.dispose();
        observer.update(sequence.getSize());
    }
    
    public String getName()
    {
        return "Discovery of episodes & inter-event intervals(Serial)";
    }
    
}

class IntervalListNode2
{
    static final boolean DEBUG = false; // false to remove debugging
    
    private IntervalListNode2 prevNode;
    private IEvent event;
    private double tValue;
    
    public IntervalListNode2(IntervalListNode2 prevListNode, IEvent event)
    {
        this.prevNode = prevListNode;
        this.event = event;
        this.tValue = event.getStartTime();
    }
    
    public double getTValue()
    { return tValue; }
    public IEvent getEvent()
    { return event; }
    public IntervalListNode2 getPrevNode()
    { return this.prevNode; }
}

class IntervalNodeTList2
{
    static final boolean DEBUG = false; // false to remove debugging
    
    private List<IntervalListNode2> tList;
    private boolean isNew;
    private int index;
    private int episodeIndex;
    private IntervalNodeTList2 next = null;
    private IntervalNodeTList2 prev = null;
    
    private IntervalNodeTList2(int size, int index, IntervalNodeTList2 parent, int episodeIndex)
    {
        if (DEBUG) System.out.println("IntervalNodeTList2: interval list node");
        
        this.prev = parent;
        this.index = index;
        this.episodeIndex = episodeIndex;
        this.isNew = true;
        this.tList = new ArrayList<IntervalListNode2>();
        if (index < size - 1)
        {
            this.next = new IntervalNodeTList2(size, index + 1, this, episodeIndex);
        }
    }
    
    public static IntervalNodeTList2 createNode(int size, int episodeIndex)
    {
        return new IntervalNodeTList2(size, 0, null, episodeIndex);
    }
    
    public boolean isIsNew()
    {
        return isNew;
    }
    
    public void setIsNew(boolean isNew)
    {
        this.isNew = isNew;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public int getEpisodeIndex()
    {
        return episodeIndex;
    }
    
    public IntervalNodeTList2 getNext()
    {
        return next;
    }
    
    public IntervalNodeTList2 getPrev()
    {
        return prev;
    }
    
    public List<IntervalListNode2> getTList()
    {
        return tList;
    }
    
    public void setTList(List<IntervalListNode2> tList)
    {
        this.tList = tList;
    }
}

