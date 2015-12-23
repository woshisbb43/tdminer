/*
 * Episode.java
 *
 * Created on January 26, 2006, 3:08 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.iisc.tdminercore.simulation;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.Interval;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Deb
 */
public class SimEpisode
{
    String episode;
    String[] event;
    int[] event_times;
    Interval[] event_delay_intervals;
    
    Interval start_delay;
    double deltaT;
    boolean shuffle = false;
    
    int size;
    int state;
    int repeat;
    
    private int count = 0;
    private boolean selected = true;
    
    private EventFactor eventTypes;
    
    public static final int MAX_SIZE = 20;
    
    /** Creates a new instance of Episode */
    public SimEpisode(String episode, String startDelay, boolean shuffle, double deltaT)
    {
        this.deltaT = deltaT;
        this.episode = episode;
        ArrayList<Interval> ivals = new ArrayList<Interval>();
        
        StringBuffer myStringBuffer = new StringBuffer();
        Pattern myPattern = Pattern.compile("\\(\\s*([0-9]+[\\.]*[0-9]*)\\s*-\\s*([0-9]+[\\.]*[0-9]*)\\s*\\)");
        Matcher myMatcher = myPattern.matcher(episode);
        while (myMatcher.find()) {
            System.out.println(myMatcher.group());
            System.out.println(myMatcher.group(1));
            System.out.println(myMatcher.group(2));
            
            int low = (int)(Double.parseDouble(myMatcher.group(1))/deltaT);
            int high = (int)(Double.parseDouble(myMatcher.group(2))/deltaT);
            
            Interval i = new Interval(low, high);
            ivals.add(i);
            
            myMatcher.appendReplacement(myStringBuffer, ":");
        }
        myMatcher.appendTail(myStringBuffer);
        
        this.event = myStringBuffer.toString().trim().split("\\s*:\\s*");
        for(String s : event) System.out.println("Event : [" + s + "]");

        
        this.event_delay_intervals = ivals.toArray(new Interval[ivals.size()]);
        this.size = this.event.length;
        this.event_times = new int[MAX_SIZE];

        this.state = 0;
        this.shuffle = shuffle;
        myPattern = Pattern.compile("\\s*([0-9]+[\\.]*[0-9]*)\\s*-\\s*([0-9]+[\\.]*[0-9]*)\\s*");
        myMatcher = myPattern.matcher(startDelay);
        if (myMatcher.find()) {
            int low = (int)(Double.parseDouble(myMatcher.group(1))/deltaT);
            int high = (int)(Double.parseDouble(myMatcher.group(2))/deltaT);
            
            this.start_delay = new Interval(low, high);
        }
        System.out.println("Start Interval : " + start_delay.toString());
    }
    
    public String fire(int clock)
    {
        String ret = null;
        if (clock >= event_times[this.state])
        {
            ret = this.event[this.state];
            //System.out.print(ret + "-" + clock + " ");
            this.state ++;
            if (this.state >= this.size)
            {
                //System.out.println();
                this.state = 0;
                scheduleEvents(clock);
                count ++;
            }
        }
        return ret;
    }
    
    public void scheduleEvents(int clock)
    {
        this.event_times[0] = clock + (int)start_delay.getTLow() + (int)(this.start_delay.getGap() * Math.random());
        //System.out.print(this.event_times[0] + " ");
        for (int i = 1; i < this.size; i++)
        {
            this.event_times[i] = (int)(this.event_times[i-1] + this.event_delay_intervals[i - 1].getTLow() +
                    (Math.random() * this.event_delay_intervals[i - 1].getGap()));
            //System.out.print(this.event_times[i] + " ");
        }
        if (shuffle)
        {
            for(int i = size - 1; i > 0; i--)
            {
                int index = (int)((i+1) * Math.random());
                String tmp = event[index];
                event[index] = event[i];
                event[i] = tmp;
            }
        }
        //System.out.println();
    }
    
    public String toString()
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        return episode + " start delay : " + nf.format(start_delay.getTLow() * deltaT) + 
                "-" + nf.format(start_delay.getTHigh() * deltaT);
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
};

