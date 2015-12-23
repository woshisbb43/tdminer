/*
 * SerialEpisodeCounterWithIntervals.java
 *
 * Created on May 2, 2006, 7:07 AM
 *
 */

package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.IEventIterable;
import edu.iisc.tdminercore.data.InstEvent;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.IObserver;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Deb
 */
public class SerialEpisodeCounterWithRigidDelays extends AbstractSerialCounterInterEventConst
{   static final boolean DEBUG = false; // false to remove debugging
     
    /** Creates a new instance of SerialTrueIntervalCounter */
    public SerialEpisodeCounterWithRigidDelays()
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
        else
            return;
        double binsize;
        if (session.getTimeGranularity() < 0)
        {
            throw new RuntimeException("This counting algorithm " +
                    "must be used only with episode discovery with intervals");
        }
        else
            binsize = session.getTimeGranularity();
        
        observer.startup();
        session.resetSegIndex();
        List<List<IntervalNodeTList>> waits = new ArrayList<List<IntervalNodeTList>>(sequence.getEventTypeCount());
       
        observer.update(0);
        for (int i = 0; i < sequence.getEventTypeCount(); i++)
        {
            waits.add(new ArrayList<IntervalNodeTList>());
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
		IntervalNodeTList n = IntervalNodeTList.createNode(e.size(), i);
		waits.get(A).add(n);
	    }
	    else
	    {
		throw new RuntimeException("The interval discovery based algorithm can only display episodes discovered by it.\n" +
			"Please remove any episode entered manually");
	    }
        }
        
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent seqevent : iterable)
        {
            int E_i = seqevent.getEventType();
            double t_i = (int)((seqevent.getStartTime())/binsize) * binsize;
            IEvent event = new InstEvent(seqevent.getSourceId(), E_i, t_i);
            session.updateSegIndex(t_i);
	    
            IntervalListNode listNode = null;

	    List<IntervalNodeTList> autoList = waits.get(E_i);
            int nsize = autoList.size();
            for (int i = 0; i < nsize; i++)
            {
                IntervalNodeTList node = autoList.get(i);
                int alphano = node.getEpisodeIndex();
                IEpisode alpha = episodes.get(alphano);
                boolean isAccepted = false;
                
                List<IntervalListNode> tList = node.getTList();
		
		if(!session.isTrackEpisodes())
		{
		    if (node.getIndex() != alpha.size() - 1)
		    {
			double tIntervalExp = alpha.getInterval(node.getIndex()).getTHigh();
			for (int j = 0; j < tList.size(); j++)
			{
			    double tValue = tList.get(j).getTValue();
			    if (t_i > (tValue + tIntervalExp + binsize))
			    {
				tList.remove(j);
				j--;
			    }
			    else
			    {
				break;
			    }
			}
		    }
		}
                
                if (node.getIndex() == 0)
                {
		    listNode = new IntervalListNode(null, event);
                    tList.add(listNode);
                    isAccepted = true;
                    if (node.isIsNew())
                    {
                        node.setIsNew(false);
                        IntervalNodeTList next = node.getNext();
			if (next != null)
			{
			    int nextEventType = alpha.getEvent(next.getIndex());
			    waits.get(nextEventType).add(next);
			}
                    }
                }
                else
                {
                    List<IntervalListNode> prevTList = node.getPrev().getTList();
                    Interval interval = alpha.getInterval(node.getPrev().getIndex());
		    double tIntervalExp = interval.getTHigh();
		    //double tIntervalExpLow = interval.getTLow();
                    for (int j = prevTList.size() - 1; j >= 0; j--)
                    {
			IntervalListNode prevListNode = prevTList.get(j);
			double prevTValue = prevListNode.getTValue();
			
                        double deltaT = t_i - prevTValue;
                        if ((t_i != prevTValue) && (Math.abs(deltaT - tIntervalExp)) < 1.0e-6)
                        {
			    listNode = new IntervalListNode(prevListNode, event); 
                            tList.add(listNode);
                            isAccepted = true;
                            if (node.isIsNew())
                            {
                                node.setIsNew(false);
                                if (node.getIndex() < alpha.size() - 1)
                                {
                                    IntervalNodeTList next = node.getNext();
                                    int nextEventType = alpha.getEvent(next.getIndex());
                                    waits.get(nextEventType).add(next);
                                }
                            }
                            break;
                        }//if
                        else
                        {
			    if (!session.isTrackEpisodes())
			    {
				if (deltaT > tIntervalExp)
				{
				    prevTList.remove(j);
				    j--;
				}
			    }
                        }
                    }//for
                }//else
                
                if (!isAccepted) continue;
                if (node.getIndex() != alpha.size() - 1) continue;
                
                alpha.incrVotes(session.getCurrentSegIndex());
                if (session.isTrackEpisodes())
                {
                    List<IEvent> events = new ArrayList<IEvent>(alpha.size());
                    for(IntervalListNode alistNode = listNode;
                        alistNode != null; 
                        alistNode = alistNode.getPrevNode() ) 
                    {
                        events.add(0,alistNode.getEvent());
                    }
                    if (events.size() < alpha.size()) {    
                        continue;
                    }
                    observer.handleEpisodeCompletion(alphano, alpha.getEventTypeIndices(), events);
                }

                IntervalNodeTList delNode = node;
                while(delNode != null)
                {
                    delNode.setIsNew(true);
                    delNode.getTList().clear();
                    if (delNode.getIndex() != 0)
                    {
                        List<IntervalNodeTList> list = waits.get(alpha.getEvent(delNode.getIndex()));
                        int rmvIndex = list.indexOf(delNode);
                        if (rmvIndex == -1)
                        {
                            throw new RuntimeException("List does not contain the node: " 
                                    + delNode.getIndex() + " of episode " + delNode.getEpisodeIndex());
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
            }//for
        }
        iterable.setSampleSize(episodes);
         
        observer.shutdown();
//	observer.dispose();
        observer.update(sequence.getSize());
    }
    
    public String getName()
    {
        return "Discovery of episodes & inter-event rigid delays(Serial)";
    }
    
}
