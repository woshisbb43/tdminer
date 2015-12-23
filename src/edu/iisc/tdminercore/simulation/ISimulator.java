/*
 * ISimulator.java
 *
 * Created on March 30, 2006, 12:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.simulation;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.reader.FileEventDataStream;
import edu.iisc.tdminercore.reader.SimulatedEventDataStream;
import edu.iisc.tdminercore.util.IObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 */
public abstract class ISimulator
{
    protected EventFactor eventTypes = null;
    public double N = 10000.0;
    protected transient IEventDataStream eventStream;
    protected List<IEpisode> episodesList = new ArrayList<IEpisode>();
    public double startTime = 0.0;
    
    public List getEpisodesList()
    {
        return episodesList;
    }

    public abstract ArrayList<F> getFlist();

    public void setEpisodesList(List episodesList)
    {
        this.episodesList = episodesList;
    }

    public IEventDataStream getEventStream()
    {
        return eventStream;
    }
    public abstract void simulate(IEventDataStream dataStream, IObserver observer) throws IOException;
    
    public void setEventFactor(EventFactor eventTypes)
    {
        this.eventTypes = eventTypes;
    }

    public EventFactor getEventFactor()
    {
        return this.eventTypes;
    }

    public void generateDataStream(final IObserver observer)
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                SimulatedEventDataStream dataStream = new SimulatedEventDataStream();
                try
                {
                    if (eventTypes != null)
                    {
                        eventTypes.resetCounts();
                        dataStream.setEventFactor(eventTypes);
                    }
                    observer.taskStarted();
                    observer.setTitle("Generating simulated events...");
                    observer.setExtent((int)N);
                    observer.startup();
                    observer.update(0);
                    simulate(dataStream, observer);
                    observer.shutdown();
                    eventStream = dataStream;
                    observer.taskComplete();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    observer.exceptionOccured(ex);
                }
            }
        });
        thread.start();
    }
    
    
    /** 
     * Use this method when you are willing to wait for the result
     * @return the generate event data stream.
     */
    public IEventDataStream generate() 
    {
        FileEventDataStream dataStream = new FileEventDataStream();
        IObserver observer = new edu.iisc.tdminercore.util.PassiveObserver();
        try
        {
            simulate(dataStream, observer);
        } catch (IOException ex)
        {
            ex.printStackTrace();
            observer.exceptionOccured(ex);
        }
        return dataStream;
    }
}
