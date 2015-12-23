/*
 * EpisodeComparator.java
 *
 * Created on March 17, 2006, 10:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.AbstractEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.GeneralizedEpisode;
import java.util.Comparator;

/**
 *
 * @author patnaik
 */
public class EpisodeComparator  implements Comparator
{
    private int sortOrder;
    private boolean ascending = true;
    private EventFactor eventTypes;
    private int index = 0;
    
    /** Creates a new instance of EpisodeComparator */
    public EpisodeComparator(int sortOrder)
    {
	this.sortOrder = sortOrder;
	if (sortOrder == Episode.DICTIONARY_ORDER)
	{
	    this.ascending = true;
	}
	else if (sortOrder == Episode.FREQUENCY_ORDER)
	{
	    this.ascending = false;
	}
        else
        {
            this.ascending = true;
        }
    }
    
    public EpisodeComparator(int sortOrder, boolean ascending, int index)
    {
	this.sortOrder = sortOrder;
	this.ascending = ascending;
        this.index = index;
    }
    
    public EpisodeComparator(int sortOrder, boolean ascending, EventFactor eventTypes, int index)
    {
	this.sortOrder = sortOrder;
	this.ascending = ascending;
	this.eventTypes = eventTypes;
        this.index = index;
    }
    
    public int compare(Object o1, Object o2)
    {
	int retVal = 0;
	AbstractEpisode e1 = (AbstractEpisode)o1;
	AbstractEpisode e2 = (AbstractEpisode)o2;
	
	retVal = e1.size() - e2.size();
	
	if (retVal == 0)
	{
	    if (sortOrder == Episode.FREQUENCY_ORDER)
	    {
		retVal = (int)(e2.getVotes(index) - e1.getVotes(index));
		if (ascending) retVal *= -1;
	    }
	    else if (sortOrder == Episode.DICTIONARY_ORDER)
	    {
		retVal = comparehelper(e1, e2, ascending);
	    }
            else if (sortOrder == Episode.DICTIONARY_FREQ_ORDER)
            {
                retVal = comparehelper(e1, e2, ascending);
                if (retVal == 0)
                {
                    retVal = (int)(e2.getVotes(index) - e1.getVotes(index));
                }
            }
	}
	return retVal;
    }
    
    
    private int comparehelper(AbstractEpisode e1, AbstractEpisode e2, boolean ascending)
    {
	int retVal = 0;
	int len = e1.size();
	if (eventTypes == null)
	{
	    for (int i = 0; i < len; i++)
	    {
		if (e1.getEvent(i) < e2.getEvent(i))
		{
		    if (ascending)
			retVal = -1;
		    else
			retVal = 1;
		    break;
		}
		else if (e1.getEvent(i) > e2.getEvent(i))
		{
		    if (ascending)
			retVal = 1;
		    else
			retVal = -1;
		    break;
		}
                else if (e1 instanceof GeneralizedEpisode && e2 instanceof GeneralizedEpisode)
                {
                    if (e1.getDuration(i) < e2.getDuration(i))
                    {
                        if (ascending)
                            retVal = -1;
                        else
                            retVal = 1;
                        break;
                    }
                    else if (e1.getDuration(i) > e2.getDuration(i))
                    {
                        if (ascending)
                            retVal = 1;
                        else
                            retVal = -1;
                        break;
                    }
                }
	    }
	}
	else
	{
	    for (int i = 0; i < len; i++)
	    {
		String s1 = eventTypes.getName(e1.getEvent(i));
		String s2 = eventTypes.getName(e2.getEvent(i));
		retVal = s1.compareTo(s2);
		if (!ascending) retVal *= -1;
                
                if (retVal == 0 && (e1 instanceof GeneralizedEpisode && e2 instanceof GeneralizedEpisode))
                {
                    if (e1.getDuration(i) < e2.getDuration(i))
                    {
                        if (ascending)
                            retVal = -1;
                        else
                            retVal = 1;
                        break;
                    }
                    else if (e1.getDuration(i) > e2.getDuration(i))
                    {
                        if (ascending)
                            retVal = 1;
                        else
                            retVal = -1;
                        break;
                    }
                }
		if (retVal != 0) break;
	    }
	}
	return retVal;
    }
}

