/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminercore.candidate.PrefixSuffixCandidatesWithIntervals;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithIntervals;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.filter.ThresholdFilterType;
import edu.iisc.tdminercore.miner.GenericMiner;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.reader.CsvEventStreamReader;
import edu.iisc.tdminercore.util.PassiveObserver;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Class: SimpleMiner
 *
 * @author debprakash
 */
class SimpleMiner {

    public static void mine(String[] args) throws IOException
    {
        System.out.println("Running TDMiner in batch mode");
        String paramFile = args[1];
        String inputFile = args[2];
        String outputFile = args[3];

        SessionInfo session = new SessionInfo();

        CsvEventStreamReader reader = new CsvEventStreamReader(inputFile);
        IEventDataStream seq = reader.read();
        session.setSequence(seq);

        System.out.println("Start time: " + seq.getSequenceStart());
        System.out.println("End time: " + seq.getSequenceEnd());
        System.out.println("# Event types: " + seq.getEventTypeCount());

        Properties p = new Properties();
        p.load(new FileReader(paramFile));
        session.setIntervalsList(p.getProperty("intervals"));
        
        System.out.println("Using Intervals:");
        for(Interval i : session.getIntervalsList()) System.out.println(i);

        session.setCounter(new SerialEpisodeCounterWithIntervals());
        session.setThresholdType(ThresholdFilterType.STRENGTH_BASED);
        double errorTypeI = Double.parseDouble(p.getProperty("type1error"));
        session.setErrorTypeI(errorTypeI);
        double eStrong = Double.parseDouble(p.getProperty("estrong"));
        session.setEStrong(eStrong);
        double timeGranularity = Double.parseDouble(p.getProperty("timeGranularity"));
        session.setTimeGranularity(timeGranularity);

        session.setPlevels(Integer.parseInt(p.getProperty("maxlevels")));
        session.setCandidateGenerator(new PrefixSuffixCandidatesWithIntervals(session));
        session.setAllowRepeat(Boolean.parseBoolean(p.getProperty("repeated_events")));

        GenericMiner miner = new GenericMiner();
        try
        {
            miner.mineSequence(new PassiveObserver(), session);
            EpisodeSet episodes = session.getEpisodes();
            PrintWriter out = new PrintWriter(outputFile);
            out.println(episodes.toString(session));
            out.close();
            System.out.println("Results written to " + outputFile);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception
    {
        String[] cmd = {"-batch",
            "D:/tdminer-batch/ivl.txt",
            "D:/tdminer-batch/event-seq.csv",
            "D:/tdminer-batch/episodes.txt" };
        mine(cmd);
    }
}
