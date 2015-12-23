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
public class ParallelAutomatonSig extends ParallelAutomaton
{
    double t_allow;
    /** Creates a new instance of ParallelAutomaton */
    public ParallelAutomatonSig(IEpisode e, int index)
    {
        super(e, index);
    }
    
    public void reset()
    {
        super.reset();
    }

    public void resetAllowTime(double episodeExp)
    {
	double startTime = pairs[0].getTList().getFirst().getStartTime();
	for(int i = 1; i < pairs.length; i++)
	{
	    double t = pairs[i].getTList().getFirst().getStartTime();
	    startTime = (t < startTime)? t : startTime;
	}
	t_allow = startTime + episodeExp;
    }
    
    public void addEvent(IEvent newEvent)
    {
	if (t_allow <= newEvent.getStartTime())
	{
	    super.addEvent(newEvent);
	}
    }
}
