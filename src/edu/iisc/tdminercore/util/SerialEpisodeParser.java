/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.episode.SerialEpisodeWithIntervals;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author debprakash
 */
public class SerialEpisodeParser
{

    private static String numpattern = "\\d+\\.?\\d*";
    private static String ivlpattern = "\\[\\s*(" + numpattern + ")\\s*\\-\\s*(" + numpattern + ")\\s*\\]";
    private static String evttype = "\\w";
    private static Pattern episodepat = Pattern.compile(evttype + "\\s*[" + ivlpattern + "\\s*-?" + evttype + "]*");
    private static Pattern ivlpat = Pattern.compile(ivlpattern);

    public static boolean hasSerialEpisode(String line)
    {
        System.out.println(evttype + "\\s*[" + ivlpattern + "\\s*-?" + evttype + "]*");
        Matcher patmatcher = episodepat.matcher(line);
        return patmatcher.find();
    }

    public static IEpisode getSerialEpisode(String line, EventFactor eventTypes, List<Interval> masterList)
            throws Exception
    {
        Matcher patmatcher = null;
        patmatcher = episodepat.matcher(line);
        if (patmatcher.find())
        {
            String eLine = patmatcher.group();
            System.out.println(eLine);

            //Get the event types
            ArrayList<String> eventlist = new ArrayList<String>();
            ArrayList<String> ivllist = new ArrayList<String>();
            patmatcher = ivlpat.matcher(eLine);
            int start = 0, end = 0;
            String event = null;
            while (patmatcher.find())
            {
                int startl = patmatcher.start();
                int endl = patmatcher.end();
                end = startl;

                event = eLine.substring(start, end).trim();
                if (event.startsWith("-"))
                {
                    event = event.substring(1);
                }
                eventlist.add(event);
                String interval = eLine.substring(startl + 1, endl - 1);
                ivllist.add(interval);
                System.out.println("Event :'" + event + "'");
                System.out.println("Interval :'" + interval + "'");
                start = endl;
            }
            event = eLine.substring(start).trim();
            if (event.startsWith("-"))
            {
                event = event.substring(1);
            }
            eventlist.add(event);
            System.out.println("Event :'" + event + "'");

            //Build episode
            int size = eventlist.size();
            IEpisode e = new SerialEpisodeWithIntervals(size, eventTypes, masterList);
            int freq = 0;
            e.setVotes(0, freq);
            int index = 0;
            for (String evt : eventlist)
            {
                int eventType = eventTypes.getId(evt);
                System.out.println("evt : " + evt + " index : " + eventType);
                if (eventType == -1) throw new Exception("Event type not in data sequence");
                e.setEvent(index, eventType);
                index++;
            }
            
            index = 0;
            for(String ivl : ivllist)
            {
                Interval ivlObj = Interval.parse(ivl);
                if (!masterList.contains(ivlObj))
                {
                    masterList.add(ivlObj);
                }
                int ivlIndex = masterList.indexOf(ivlObj);
                e.setIntervalId(index, ivlIndex);
                index++;
            }
            return e;
        }
        return null;
    }
}
