/*
 * Automaton.java
 *
 * Created on March 14, 2006, 3:44 PM
 *
 */

package edu.iisc.tdminercore.data;

import edu.iisc.tdminercore.data.IEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 * @author phreed@gmail.com
 */
public class Automaton
{
    private int episodeIndex;
    private int state;
    private IEvent lastTransition;
    private List<IEvent> events;
    private IEvent startEvent;
    
    /** Creates a new instance of Automaton */
    public Automaton(int episodeIndex, int state)
    {
	this.episodeIndex = episodeIndex;
	this.state = state;
    }
    
    public Automaton(int episodeIndex, int state, int size)
    {
	this.episodeIndex = episodeIndex;
	this.state = state;
        this.events = new ArrayList<IEvent>(size);
    }
    
    public int getEpisodeIndex()
    {
	return episodeIndex;
    }
    
    public void setEpisodeIndex(int episodeIndex)
    {
	this.episodeIndex = episodeIndex;
    }
    
    public int getState()
    {
	return state;
    }
    
    public void setState(int state)
    {
	this.state = state;
    }
    
    public boolean equals(Object obj)
    {
	Automaton auto = (Automaton)obj;
	if (auto.episodeIndex == episodeIndex && auto.state == state)
	    return true;
	
	return false;
    }
    
    public IEvent getLastTransit()
    {
	return lastTransition;
    }
    public double getLastTransitTime()
    {
	return lastTransition.getStartTime();
    }
   
    // an event mode for setLastTransit
    public void setLastTransit(IEvent lastTransition)
    {
        this.lastTransition = lastTransition;
	if (this.events == null) return;
        for(int ix = this.events.size(); ix <= state; ix++) { 
            this.events.add(state, (IEvent)null);
        }
        this.events.set(state, lastTransition);
    }
    
    public List<IEvent> getEvents()
    {
	return this.events;
    }
    
    public void setEvents(Automaton auto)
    {
        this.setEvents(auto.events);
    }
    public void setEvents(List<IEvent> eventList)
    {
	if (this.events == null) return;
	
        if (this.events.size() != eventList.size())  {
            System.err.println("Episode event list length mismatch");
            return;
        }
        for (int ix = 0; ix < eventList.size(); ix++) {
                this.events.set(ix, eventList.get(ix));
        }	
    }

    public IEvent getStartEvent() {
        return startEvent;
    }

    public void setStartEvent(IEvent startEvent) {
        this.startEvent = startEvent;
    }
}
