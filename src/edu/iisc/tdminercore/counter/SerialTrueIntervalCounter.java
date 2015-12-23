/*
 * SerialTrueIntervalCounter.java
 *
 * Created on April 30, 2006, 7:07 AM
 *
 */

package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.IEventIterable;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 */
public class SerialTrueIntervalCounter extends AbstractSerialEpisodeCounter
{
    static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of SerialTrueIntervalCounter */
    public SerialTrueIntervalCounter()
    {if (DEBUG) System.out.println("SerialTrueIntervalCounter: <constructor>");}
    
    @Override
    public void countEpisodes(List<IEpisode> episodes, IObserver observer, 
            SessionInfo session)
        throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        if (session.isTrackEpisodes())
        {
            new BruteforceSerialCounter(true).countEpisodes(episodes, observer, session);
        }
        else
        {
            countEpisodesWithoutTracking(episodes, observer, session);
        }
    }
    
    private void countEpisodesWithoutTracking(List<IEpisode> episodes, IObserver observer, 
            SessionInfo session)
        throws IObserver.NotImplementedException, IEpisode.NotImplementedException
    {
        IEventDataStream sequence = session.getSequence();
        if (DEBUG) System.out.println("SerialTrueIntervalCounter: countEpisodes");
        observer.startup();
        session.resetSegIndex();
        double intervalExpLow = session.getIntervalExpiryLow();
        double intervalExp = session.getIntervalExpiry();
        List<List<NodeTList>> waits = new ArrayList<List<NodeTList>>(sequence.getEventTypeCount());
       
        observer.update(0);
        for (int ix = 0; ix < sequence.getEventTypeCount(); ix++) {
            waits.add(new ArrayList<NodeTList>());
        }
        /* Using a reference to the fsm (nodelist) with an index 
         * is more efficient in compute (less copying/deleting) and space */
        for(int ix = 0; ix < episodes.size(); ix++)
        {
            IEpisode fsm = episodes.get(ix);
            fsm.resetVotes();
            int num_segs = 1;
            if (session.isSegmented()) num_segs = session.getSegIndexLen();
            fsm.initVotes(num_segs);
            
            int startState = fsm.getFirstEvent();
            NodeTList fsmState = NodeTList.createNode(fsm.size(), ix);
            waits.get(startState).add(fsmState);
        }
        
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
            int E_i = event.getEventType();
            double t_i = event.getStartTime();
            session.updateSegIndex(t_i);

	    List<NodeTList> autoList = waits.get(E_i);
            int nsize = autoList.size();
            /* ListIterator<ArrayList> allStates = autoList.listIterator(); */
            for (int ix = 0; ix < nsize; ix++)
            {
                NodeTList node = autoList.get(ix);
                int alphano = node.getEpisodeIndex();
                IEpisode alpha = episodes.get(alphano);
                boolean isAccepted = false;
                
                List<Double> tList = node.getTList();
                /* Remove the fsm state from wait(Ei) 
                 * unless it is a start state */
                for (int jx = 0; jx < tList.size(); jx++) {
                    double tValue = tList.get(jx).doubleValue();
                    if (t_i > tValue + intervalExp) {
                        tList.remove(jx);
                        jx--;
                    }
                    else {
                        break;
                    }
                }
                
                if (node.getIndex() == 0)
                {
                    tList.add(new Double(t_i));
                    isAccepted = true;
                    if (node.isNew())
                    {
                        node.setIsNew(false);
                        NodeTList next = node.getNext();
			if (next != null)
			{
			    int nextEventType = alpha.getEvent(next.getIndex());
			    waits.get(nextEventType).add(next);
			}
                    }
                }
                else
                {
                    List<Double> prevTList = node.getPrev().getTList();
                    for (int jx = 0; jx < prevTList.size(); jx++)
                    {
                        double prevTValue = prevTList.get(jx).doubleValue();
                        double deltaT = t_i - prevTValue;
                        if ((t_i != prevTValue) && (intervalExpLow <= deltaT && deltaT < intervalExp))
                        {
                            tList.add(new Double(t_i));
                            isAccepted = true;
                            if (node.isNew())
                            {
                                node.setIsNew(false);
                                if (node.getIndex() < alpha.size() - 1)
                                {
                                    NodeTList next = node.getNext();
                                    int nextEventType = alpha.getEvent(next.getIndex());
                                    waits.get(nextEventType).add(next);
                                }
                            }
                            break;
                        }//if
                        else
                        {
                            if (deltaT > intervalExp)
                            {
                                prevTList.remove(jx);
                                jx--;
                            }
                        }
                    }//for
                }//else
                
                if (isAccepted && node.getIndex() == alpha.size() - 1)
                {
                    alpha.incrVotes(session.getCurrentSegIndex());
                    
                    NodeTList delNode = node;
                    while(delNode != null)
                    {
                        delNode.setIsNew(true);
                        delNode.getTList().clear();
                        if (delNode.getIndex() != 0)
                        {
                            List<NodeTList> list = waits.get(alpha.getEvent(delNode.getIndex()));
                            int rmvIndex = list.indexOf(delNode);
                            if (rmvIndex == -1)
                            {
                                throw new RuntimeException("List does not contain the node: " + delNode.getIndex() + " of episode " + delNode.getEpisodeIndex());
                            }
                            
                            list.remove(rmvIndex);
                            if (list == autoList) 
                            {
                                nsize--;
                                if (rmvIndex <= ix) ix--;
                            }
                        }
                        delNode = delNode.getPrev();
                    }
                }
                
            }//for
        }
        iterable.setSampleSize(episodes);
        
//	observer.dispose();
        observer.update(sequence.getSize());
    }
    public String getName()
    {if (DEBUG) System.out.println("SerialTrueIntervalCounter: get name");
        return "Non-overlapped count with inter-event interval constraint(Serial)";
    }
}

class NodeTList
{
    static final boolean DEBUG = false; // false to remove debugging
    
    private List<Double> tList;
    private boolean isNew;
    private int index;
    private int episodeIndex;
    private NodeTList next = null;
    private NodeTList prev = null;
    
    private NodeTList(int size, int index, NodeTList parent, int episodeIndex)
    {if (DEBUG) System.out.println("NodeTList: <constructor>");
        this.prev = parent;
        this.index = index;
        this.episodeIndex = episodeIndex;
        this.isNew = true;
        this.tList = new ArrayList<Double>();
        if (index < size - 1)
        {
            this.next = new NodeTList(size, index + 1, this, episodeIndex);
        }
    }
    
    public static NodeTList createNode(int size, int episodeIndex)
    {if (DEBUG) System.out.println("NodeTList: create node");
        return new NodeTList(size, 0, null, episodeIndex);
    }
    
    public boolean isNew() { return isNew; }
    public void setIsNew(boolean isNew) { this.isNew = isNew; }
    
    public int getIndex(){ return index; }
    
    public int getEpisodeIndex(){ return episodeIndex; }
    
    public NodeTList getNext()  { return next; }
    public NodeTList getPrev() { return prev; }

    public List<Double> getTList(){ return tList; }
    public void setTList(List<Double> tList) { this.tList = tList; }
}
