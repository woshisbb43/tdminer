package edu.iisc.tdminercore.counter;

import java.util.ArrayList;
import java.util.List;

class IntervalNodeTList
{
 static final boolean DEBUG = false; // false to remove debugging
 
    private List<IntervalListNode> tList;
    private boolean isNew;
    private int index;
    private int episodeIndex;
    private IntervalNodeTList next = null;
    private IntervalNodeTList prev = null;
    
    private IntervalNodeTList(int size, int index, IntervalNodeTList parent, int episodeIndex)
    {
    if (DEBUG) System.out.println("IntervalNodeTList: interval list node");
    
        this.prev = parent;
        this.index = index;
        this.episodeIndex = episodeIndex;
        this.isNew = true;
        this.tList = new ArrayList<IntervalListNode>();
        if (index < size - 1)
        {
            this.next = new IntervalNodeTList(size, index + 1, this, episodeIndex);
        }
    }
    
    public static IntervalNodeTList createNode(int size, int episodeIndex)
    {
        return new IntervalNodeTList(size, 0, null, episodeIndex);
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
    
    public IntervalNodeTList getNext()
    {
        return next;
    }
    
    public IntervalNodeTList getPrev()
    {
        return prev;
    }

    public List<IntervalListNode> getTList()
    {
        return tList;
    }

    public void setTList(List<IntervalListNode> tList)
    {
        this.tList = tList;
    }
}