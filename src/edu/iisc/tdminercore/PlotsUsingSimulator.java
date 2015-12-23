/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details:
 * http://www.gnu.org/licenses/gpl.txt
 */

package edu.iisc.tdminercore;

import edu.iisc.tdminer.gui.ITaskPanel;
import edu.iisc.tdminer.model.ThreadedProgressManager;
import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithIntervals;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithRigidDelays;
import edu.iisc.tdminercore.data.ComboEpisode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.GenericMiner;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.reader.SimulatedEventDataStream;
import edu.iisc.tdminercore.util.PassiveObserver;
import edu.iisc.tdminercore.util.SerialEpisodeParser;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class: PlotsUsingSimulator
 *
 * @author debprakash
 */
public class PlotsUsingSimulator{
    SessionInfo session = new SessionInfo();
    edu.iisc.tdminercore.simulation.PoissonSimulator sim = 
                new edu.iisc.tdminercore.simulation.PoissonSimulator();
    IEventDataStream sequence;
    EventFactor eventTypes;
    GenericMiner miner;
    EpisodeSet episodes;
    IEpisode episode1, episode2;
    AbstractEpisodeCounter counter1, counter2;
    
    public static void main(String[] args) throws Exception
    {
        PlotsUsingSimulator p = new PlotsUsingSimulator();
        p.simulateData();
    }
    
    public PlotsUsingSimulator() throws Exception
    {
        sim = new edu.iisc.tdminercore.simulation.PoissonSimulator();
        counter1 = new SerialEpisodeCounterWithRigidDelays();
        counter2 = new SerialEpisodeCounterWithIntervals();
        session.setSegmented(false);
        miner = new GenericMiner();
        List<Interval> ivlList = new ArrayList();
        session.setIntervalsList(ivlList);
        session.setTimeGranularity(0.001);
        
	sim.setTtotal("20.0");
        sim.setEstrong("0.8");
        sim.setTstart("0.0");
	sim.setTupdate("0.001");
	sim.setFrest("20.0");
        sim.setSynapticDelayLow(5);
        sim.setSynapticDelayHigh(5);
        sim.setTrefractory("0.001");
        sim.setSlope("1");
        sim.groupInterconnect = false;
        sim.computeParameters();
        
	sim.interconnectType = 0;
	sim.randomLow = 0.02;
	sim.randomHigh = 0.02;
        sim.interConnectFactor = 0.05;

        //String events = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z";
        String events = "A B C D E F G";
        String[] types = events.split("[ ]+");
        eventTypes = new EventFactor(types);
        sim.setEventFactor(eventTypes);
        episodes = new EpisodeSet(new ArrayList(), eventTypes);
        session.setEpisodes(episodes);
        System.out.println("Adding all 1-node episodes");
        for(IEpisode e1 : eventTypes.getEpisodeList())
        {
            episodes.addEpisode(e1);
        }
        String episode_str = "A [0.005-0.005] B";
        episode1 = SerialEpisodeParser.getSerialEpisode(episode_str, eventTypes, session.getIntervalsList());
        episode_str = "A [0.004-0.006] B";
        episode2 = SerialEpisodeParser.getSerialEpisode(episode_str, eventTypes, session.getIntervalsList());
        episodes.addEpisode(episode1);
        episodes.addEpisode(episode2);
    }
    
    public void simulateData() throws Exception
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(6);
        PrintWriter out = new PrintWriter("diff.csv");
        List episodesList = sim.getEpisodesList();
        String eps_str = "A B";
        episodesList.add(ComboEpisode.getEpisode(eps_str, eventTypes));
        double[] e_array = {0.001, 0.003, 0.005, 0.008, 0.010, 0.012, 0.015, 0.02};
        out.println("Estr\t[0.005]\t[0.004-0.006]\t Difference");
        //double[] e_array = {0.001};
        for(double e : e_array)
        {
            for(int i = 0; i < 20; i ++)
            {
                System.out.println("Estr = " + nf.format(e) + " iteration = " + i);
                sim.setTtotal("100.0");
                sim.setEstrong(Double.toString(e));
                sim.computeParameters();
                sim.interconnect();
                SimulatedEventDataStream seq = new SimulatedEventDataStream();
                seq.setEventFactor(eventTypes);
                sequence = seq;
                session.setSequence(sequence);
                sim.simulate(sequence, new PassiveObserver());
                session.setCounter(counter1);
                miner.countEpisodes(episodes, new PassiveObserver(), session);
                int v1 = episode1.getVotes(0);
                String s = "rigid\t" + nf.format(sim.prob_fire) + " \t " + nf.format(episode1.getEstr(0)) 
                        + " \t " + episode1.getVotes(0);
                System.out.println(s);
//                out.println(s);
//                out.flush();
                session.setCounter(counter2);
                miner.countEpisodes(episodes, new PassiveObserver(), session);
                int v2 = episode2.getVotes(0);
                s = "sloppy\t" + nf.format(sim.prob_fire) + " \t " + nf.format(episode2.getEstr(0)) 
                        + " \t " + episode2.getVotes(0);
                System.out.println(s);

                s = nf.format(sim.prob_fire) + "\t" + v1 + "\t" + v2 + "\t" + (v2 - v1);
                System.out.println(s);
                out.println(s);
                out.flush();
            }
        }
        out.close();
    }
}
