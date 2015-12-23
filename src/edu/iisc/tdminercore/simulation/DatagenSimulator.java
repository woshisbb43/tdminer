/*
 * DatagenSimulator.java
 *
 * Created on April 30, 2006, 1:43 PM
 *
 */
package edu.iisc.tdminercore.simulation;

import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.util.IObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 */
public class DatagenSimulator extends ISimulator
{
    private String[] noiseEventTypes = null;
    
    private double deltaT = 0.01;
    public double noiseProb = 0.75;
    private List<SimEpisode> internalepisodes = new ArrayList<SimEpisode>();
    
    /** Creates a new instance of DatagenSimulator */
    public DatagenSimulator()
    {}
    
    public void simulate(IEventDataStream dataStream, IObserver observer) throws IOException
    {
        resetCount();
        int clock = 0, length = 0;
        
        for (int i = 0; i < internalepisodes.size(); i++)
        {
            SimEpisode e = internalepisodes.get(i);
            e.state = 0;
            e.scheduleEvents(0);
        }
        
        while(!observer.interrupted())
        {
            boolean flag = true;
            String symbol = null;
            for (int i = 0; i < internalepisodes.size(); i++)
            {
                SimEpisode e = internalepisodes.get(i);
                // Check if an episode event fires
                if ((symbol = e.fire(clock)) != null)
                {
                    dataStream.add(symbol, new Double(clock) * deltaT);
                    flag = false;
                    length ++;
                }
            }
            if (flag && Math.random() < noiseProb && noiseEventTypes != null)
            {
                symbol = noiseEventTypes[(int)(noiseEventTypes.length * 
                        Math.random())];
                dataStream.add(symbol, new Double(clock) * deltaT);
                flag = false;
                length ++;
            }
            clock ++;
            if (length >= N)
                break;
            observer.update(length);
        }
    }
    
    public void setEventFactor(String events)
    {
        if (events != null && events.length() > 0)
            this.noiseEventTypes = events.trim().split("[ ]+");
        else
            this.noiseEventTypes = null;
    }
    
    public void addEpisode(String eps, String startdelay, boolean shuffle, double deltaT)
    {
        SimEpisode episode = new SimEpisode(eps, startdelay, shuffle, deltaT);
        internalepisodes.add(episode);
    }
    
    public List<SimEpisode> getEpsiodeList()
    {
        return internalepisodes;
    }
    
    public void resetCount()
    {
        for(int i = 0; i < internalepisodes.size(); i++)
        {
            ((SimEpisode)internalepisodes.get(i)).setCount(0);
        }
    }

    public double getDeltaT() {
        return deltaT;
    }

    public void setDeltaT(double deltaT) {
        this.deltaT = deltaT;
    }

    @Override
    public ArrayList<F> getFlist()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
