/*
 * SerialIntervalCounter.java
 *
 * Created on April 29, 2006, 10:42 AM
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
public class SerialIntervalCounter extends AbstractSerialEpisodeCounter
{   static final boolean DEBUG = false; // false to remove debugging
    
    /** Creates a new instance of SerialIntervalCounter */
    public SerialIntervalCounter()
    {
    }
    
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
        observer.startup();
        session.resetSegIndex();
        double intervalExp = session.getIntervalExpiry();
        List<List<Node>> waits = new ArrayList<List<Node>>(sequence.getEventTypeCount());
        observer.update(0);
        for (int i = 0; i < sequence.getEventTypeCount(); i++)
        {
            waits.add(new ArrayList<Node>());
        }
        
        for(int i = 0; i < episodes.size(); i++)
        {
            IEpisode e = episodes.get(i);
            e.resetVotes();
            int num_segs = 1;
            if (session.isSegmented()) num_segs = session.getSegIndexLen();
            e.initVotes(num_segs);
            
            int A = e.getEvent(0);
            Node n = Node.createNode(e.size(), i);
            waits.get(A).add(n);
        }
        
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable)
        {
            int E_i = event.getEventType();
            double t_i = event.getStartTime();
            session.updateSegIndex(t_i);

	    List<Node> autoList = waits.get(E_i);
            int nsize = autoList.size();
            for (int i = 0; i < nsize; i++)
            {
                Node node = autoList.get(i);
                int alphano = node.getEpisodeIndex();
                IEpisode alpha = episodes.get(alphano);
                boolean isAccepted = false;
                
                if (node.getIndex() == 0)
                {
                    node.setTValue(t_i);
                    isAccepted = true;
                    if (node.isIsNew())
                    {
                        node.setIsNew(false);
                        Node next = node.getNext();
			if (next != null)
			{
			    int nextEventType = alpha.getEvent(next.getIndex());
			    waits.get(nextEventType).add(next);
			}
                    }
                }
                else
                {
                    if (t_i - node.getPrev().getTValue() < intervalExp)
                    {
                        node.setTValue(t_i);
                        isAccepted = true;
                        if (node.isIsNew())
                        {
                            node.setIsNew(false);
                            if (node.getIndex() < alpha.size() - 1)
                            {
                                Node next = node.getNext();
                                int nextEventType = alpha.getEvent(next.getIndex());
                                waits.get(nextEventType).add(next);
                            }
                        }
                    }
                }
                
                if (isAccepted && node.getIndex() == alpha.size() - 1)
                {
                    alpha.incrVotes(session.getCurrentSegIndex());
                    ArrayList<String> temp = new ArrayList<String>();
                    Node delNode = node;
                    while(delNode != null)
                    {
                        delNode.setIsNew(true);
                        temp.add("(" + delNode.getIndex() + "," + delNode.getTValue() + ")");
                        if (delNode.getIndex() != 0)
                        {
                            List<Node> list = waits.get(alpha.getEvent(delNode.getIndex()));
                            int rmvIndex = list.indexOf(delNode);
                            if (rmvIndex == -1)
                            {
                                throw new RuntimeException("List does not contain the node: " + delNode.getIndex() + " of episode " + delNode.getEpisodeIndex());
                            }
                            
                            list.remove(rmvIndex);
                            if (list == autoList) 
                            {
                                nsize--;
                                if (rmvIndex <= i) i--;
                            }
                        }
                        delNode = delNode.getPrev();
                    }
                    
//                    System.out.print("Eps:" + alpha.toString() + " = [");
//                    for(int indx = temp.size() - 1; i >= 0; i--) System.out.print(temp.get(indx));
//                    System.out.println("]");
                }
                
            }//for
        }
        iterable.setSampleSize(episodes);
        
//	observer.dispose();
        observer.update(sequence.getSize());
    }
    
    public String getName()
    {
        return "Non-overlapped count with inter-event expiry constraint(Serial)";
    }
    
}

class Node
{
    private double tValue;
    private boolean isNew = true;
    private int index;
    private int episodeIndex;
    private Node next = null;
    private Node prev = null;
    
    private Node(int size, int index, Node parent, int episodeIndex)
    {
        this.prev = parent;
        this.index = index;
        this.episodeIndex = episodeIndex;
        this.isNew = true;
        if (index < size - 1)
        {
            this.next = new Node(size, index + 1, this, episodeIndex);
        }
    }
    
    public static Node createNode(int size, int episodeIndex)
    {
        return new Node(size, 0, null, episodeIndex);
    }
    
    public double getTValue()
    {
        return tValue;
    }
    
    public void setTValue(double tValue)
    {
        this.tValue = tValue;
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
    
    public Node getNext()
    {
        return next;
    }
    
    public Node getPrev()
    {
        return prev;
    }
}
