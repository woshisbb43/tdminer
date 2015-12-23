/*
 * SorterObj.java
 *
 * Created on November 2, 2007, 10:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.counter;

import edu.iisc.tdminercore.data.IEvent;

/**
 *
 * @author Deb
 */
public class SorterObj implements Comparable
{
    public int index;
    public IEvent event;
    public SorterObj(int in, IEvent event)
    {
	this.index = in;
        this.event = event;
    }
    public int compareTo(Object o)
    {
	SorterObj s = (SorterObj)o;
	if (index < s.index) return -1;
	else if (index > s.index) return +1;
	else return 0;
    }
    public String toString()
    {
	return "[" + index + "]";
    }
}
