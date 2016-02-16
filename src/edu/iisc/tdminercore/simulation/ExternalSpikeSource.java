package edu.iisc.tdminercore.simulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class: ExternalSpikeSource
 *
 * @author debprakash
 */
public class ExternalSpikeSource {
    
    private String fileName;
    private ArrayList<Double> spikeTimes;
    private double duration;
    private boolean repeats;
    
    private BufferedReader in;
    private double lastSpike = -1;
    private boolean isLoaded = false;
    private int index = 0;
    private int loopCount = 0;
    private int callCount = 0;
    private boolean start = true;
    private double prevclock = 0.0;
    
    public ExternalSpikeSource(String fileName)
    {
        this.fileName = fileName;
        try
        {
            in = new BufferedReader(new FileReader(fileName));
            String line = null;
            // Get duration and repeat
            while(true)
            {
                line = in.readLine();
                if (line == null)
                {
                    in.close();
                    throw new RuntimeException("Pre-mature termination of " + fileName);
                }
                line = line.trim();
                if ("end".equalsIgnoreCase(line)) break;
                String[] parts = line.split("=");
                if (parts.length != 2) throw new RuntimeException("Invalid header in " + fileName);
                String key = parts[0].trim();
                String value = parts[1].trim();
                if ("duration".equalsIgnoreCase(key))
                {
                    duration = Double.parseDouble(value);
                }
                else if ("repeat".equalsIgnoreCase(key))
                {
                    repeats = Boolean.parseBoolean(value);
                    if (repeats)
                    {
                        spikeTimes = new ArrayList<Double>();
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            in = null;
        }
    }
    
    public ArrayList<Double> getSpikes(double deltaT, double clock)
    {
        int span = (int)((duration)/deltaT);
        clock = (int)(clock/duration) * duration;
        ArrayList<Double> t = new ArrayList<Double>();
        int lastSpikeIndex = (int)((lastSpike)/deltaT) % span;
        //System.out.println(lastSpike + "," + duration + "," + lastSpikeIndex + "," + callCount);
        if (callCount == lastSpikeIndex || start)
        {
            if (!start) 
            {
                t.add(prevclock + lastSpike);
                lastSpike = -1;
            }
            start = false;
            if (isLoaded)
            {
                if (repeats)
                {
                    while(true)
                    {
                        lastSpike = spikeTimes.get(index);
                        prevclock = clock;
                        int prev = index;
                        index = (index + 1) % spikeTimes.size();
                        lastSpikeIndex = (int)((lastSpike)/deltaT) % span;                        
                        if (callCount == lastSpikeIndex && prev != index)
                        {
                            t.add(clock + lastSpike);
                        }
                        else
                            break;
                    }
                }
            }
            else
            {
                String line = null;
                while(true)
                {
                    try
                    {
                        line = in.readLine();
                        if (line == null)
                        {
                            in.close();
                            isLoaded = true;
                            if (repeats) 
                            {
                                while(true)
                                {
                                    lastSpike = spikeTimes.get(index);
                                    prevclock = clock;
                                    int prev = index;
                                    index = (index + 1) % spikeTimes.size();
                                    lastSpikeIndex = (int)((lastSpike)/deltaT) % span;
                                    if (callCount == lastSpikeIndex && prev != index)
                                    {
                                        t.add(clock + lastSpike);
                                    }
                                    else
                                        break;
                                }
                            }
                            break;
                        }
                        else
                        {
                            lastSpike = Double.parseDouble(line);
                            prevclock = clock;
                            lastSpikeIndex = (int)((lastSpike)/deltaT) % span;
                            if (repeats)
                            {
                                spikeTimes.add(lastSpike);
                            }
                            if (callCount == lastSpikeIndex)
                            {
                                t.add(clock + lastSpike);
                            }
                            else
                                break;
                        }
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException("Unable to read = " + fileName, ex);
                    }
                }
            }
        }
        callCount ++;
        if (callCount == span)
        {
            callCount = 0;
            loopCount ++;
        }
        return t;
    }
    
    public static void main(String[] args)
    {
        ExternalSpikeSource es = new ExternalSpikeSource("ext-spike-input.txt");
        
        double clock = 0.0;
        double deltaT = 0.001;
        for(int i = 0; i < 100000; i++)
        {
            final ArrayList<Double> spikes = es.getSpikes(deltaT, clock);
            if (spikes.size() > 0)
            {
                System.out.printf("%.3f %s\n", clock,spikes);
            }
            clock += deltaT;
        }
    }
}
