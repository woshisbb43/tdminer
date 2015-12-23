/*
 * BruteforceSerialCounter.java
 *
 * Created on May 2, 2006, 1:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
public class BruteforceSerialCounter extends AbstractSerialEpisodeCounter {
    static final boolean DEBUG = false; // false to remove debugging
    
    private boolean inner;
    /** Creates a new instance of BruteforceSerialCounter */
    BruteforceSerialCounter(boolean inner) {
        this.inner = inner;
    }
    
    @Override
    public void countEpisodes(List<IEpisode> episodes, IObserver observer, SessionInfo session)
    throws IObserver.NotImplementedException, IEpisode.NotImplementedException {
        IEventDataStream sequence = session.getSequence();
        observer.startup();
        session.resetSegIndex();
        double intervalExpLow = session.getIntervalExpiryLow();
        double intervalExp = session.getIntervalExpiry();
        
        List<List<BFNodeTList>> waits = new ArrayList<List<BFNodeTList>>(sequence.getEventTypeCount());
        observer.update(0);
        for (int i = 0; i < sequence.getEventTypeCount(); i++) {
            waits.add(new ArrayList<BFNodeTList>());
        }
        
        for(int i = 0; i < episodes.size(); i++) {
            IEpisode e = episodes.get(i);
            e.resetVotes();
            int num_segs = 1;
            if (session.isSegmented()) num_segs = session.getSegIndexLen();
            e.initVotes(num_segs);
            int A = e.getFirstEvent();
            BFNodeTList n = BFNodeTList.createNode(e.size(), i);
            waits.get(A).add(n);
        }
        
        IEventIterable iterable = sequence.iterable(observer);
        for(IEvent event : iterable) {
            //System.out.println("seq = " + seqindex);
            int E_i = event.getEventType();
            double t_i = event.getStartTime();
            session.updateSegIndex(t_i);
            
            ListNode listNode = null;
            
            List<BFNodeTList> autoList = waits.get(E_i);
            int nsize = autoList.size();
            for (int i = 0; i < nsize; i++) {
                BFNodeTList node = autoList.get(i);
                int alphano = node.getEpisodeIndex();
                IEpisode alpha = episodes.get(alphano);
                boolean isAccepted = false;
                
                List<ListNode> tList = node.getTList();
                
                if (node.getIndex() == 0) {
                    listNode = new ListNode(null, event);
                    tList.add(listNode);
                    isAccepted = true;
                    if (node.isIsNew()) {
                        node.setIsNew(false);
                        BFNodeTList next = node.getNext();
                        if (next != null) {
                            int nextEventType = alpha.getEvent(next.getIndex());
                            waits.get(nextEventType).add(next);
                        }
                    }
                } else {
                    List<ListNode> prevTList = node.getPrev().getTList();
                    if (inner) {
                        for (int j = prevTList.size() - 1; j >= 0; j--) {
                            ListNode prevListNode = prevTList.get(j);
                            double prevTValue = prevListNode.getTValue();
                            double deltaT = t_i - prevTValue;
                            if ((t_i != prevTValue) && (intervalExpLow <= deltaT && deltaT < intervalExp)) {
                                listNode = new ListNode(prevListNode, event);
                                tList.add(listNode);
                                isAccepted = true;
                                if (node.isIsNew()) {
                                    node.setIsNew(false);
                                    if (node.getIndex() < alpha.size() - 1) {
                                        BFNodeTList next = node.getNext();
                                        int nextEventType = alpha.getEvent(next.getIndex());
                                        waits.get(nextEventType).add(next);
                                    }
                                }
                                break;
                            }//if
                        }//for
                    } else {
                        for (int j = 0; j < prevTList.size(); j++) {
                            ListNode prevListNode = prevTList.get(j);
                            double prevTValue = prevListNode.getTValue();
                            double deltaT = t_i - prevTValue;
                            if (intervalExpLow <= deltaT && deltaT < intervalExp) {
                                listNode = new ListNode(prevListNode, event);
                                tList.add(listNode);
                                isAccepted = true;
                                if (node.isIsNew()) {
                                    node.setIsNew(false);
                                    if (node.getIndex() < alpha.size() - 1) {
                                        BFNodeTList next = node.getNext();
                                        int nextEventType = alpha.getEvent(next.getIndex());
                                        waits.get(nextEventType).add(next);
                                    }
                                }
                                break;
                            }//if
                        }//for
                    }
                }//else
                
                if (!isAccepted) continue;
                if (node.getIndex() != alpha.size()-1) continue;
                
                alpha.incrVotes(session.getCurrentSegIndex());
                List<IEvent> events = new ArrayList<IEvent>(alpha.size());
                for(ListNode alistNode = listNode;
                alistNode != null;
                alistNode = alistNode.getPrevNode()) {
                    events.add(0,alistNode.getEvent());
                }
                
                observer.handleEpisodeCompletion(alphano, alpha.getEventTypeIndices(), events);
                
                BFNodeTList delNode = node;
                while(delNode != null) {
                    delNode.setIsNew(true);
                    delNode.getTList().clear();
                    if (delNode.getIndex() != 0) {
                        List<BFNodeTList> list = waits.get(alpha.getEvent(delNode.getIndex()));
                        int rmvIndex = list.indexOf(delNode);
                        if (rmvIndex == -1) {
                            throw new RuntimeException("List does not contain the node: " + delNode.getIndex() + " of episode " + delNode.getEpisodeIndex());
                        }
                        
                        list.remove(rmvIndex);
                        if (list == autoList) {
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
    
    public String getName() {
        return "Brute force algorithm for counting serial episode with interval expiry";
    }
    
}

class ListNode {
    private ListNode prevNode = null;
    private double tValue;
    private IEvent event;
    public ListNode(ListNode prevListNode, IEvent event) {
        this.prevNode = prevListNode;
        this.event = event;
        this.tValue = event.getStartTime();
    }
    public double getTValue() {
        return tValue;
    }
    public IEvent getEvent() {
        return this.event;
    }
    
    public ListNode getPrevNode() {
        return this.prevNode;
    }
}

class BFNodeTList {
    private List<ListNode> tList;
    private boolean isNew;
    private int index;
    private int episodeIndex;
    private BFNodeTList next = null;
    private BFNodeTList prev = null;
    
    private BFNodeTList(int size, int index, BFNodeTList parent, int episodeIndex) {
        this.prev = parent;
        this.index = index;
        this.episodeIndex = episodeIndex;
        this.isNew = true;
        this.tList = new ArrayList<ListNode>();
        if (index < size - 1) {
            this.next = new BFNodeTList(size, index + 1, this, episodeIndex);
        }
    }
    
    public static BFNodeTList createNode(int size, int episodeIndex) {
        return new BFNodeTList(size, 0, null, episodeIndex);
    }
    
    public boolean isIsNew() {
        return isNew;
    }
    
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getEpisodeIndex() {
        return episodeIndex;
    }
    
    public BFNodeTList getNext() {
        return next;
    }
    
    public BFNodeTList getPrev() {
        return prev;
    }
    
    public List<ListNode> getTList() {
        return tList;
    }
    
    public void setTList(List<ListNode> tList) {
        this.tList = tList;
    }
}

