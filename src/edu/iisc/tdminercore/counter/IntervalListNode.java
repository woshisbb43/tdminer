package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEvent;

public class IntervalListNode
{
static final boolean DEBUG = false; // false to remove debugging

    private IntervalListNode prevNode;
    private IEvent event;
    private double tValue;
    
    public IntervalListNode(IntervalListNode prevListNode, IEvent event)
    {
	this.prevNode = prevListNode;
        this.event = event;
	this.tValue = event.getStartTime();
    }    
    
    public double getTValue() { return tValue; }
    public IEvent getEvent() { return event; }
    public IntervalListNode getPrevNode() { return this.prevNode; }
}
