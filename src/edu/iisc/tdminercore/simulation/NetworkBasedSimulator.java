/*
 * NetworkBasedSimulator.java
 *
 * Created on August 27, 2006, 9:21 PM
 *
 */

package edu.iisc.tdminercore.simulation;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Deb
 */
public abstract class NetworkBasedSimulator extends ISimulator implements Serializable
{
    protected double [][] weights = null;
    protected int size;
    
    public static final int UNIFORM = 0;
    public static final int BINOMIAL = 1;
    public static final int FULLY_CONNECTED = 2;

    public int interconnectType = 0;
    public boolean loopback = true;
    public double randomLow = -1;
    public double randomHigh = +1;
    public double interConnectFactor = 0.5;
    
    protected boolean connect = false;
    protected List<ExternalInput> externalInputList = new ArrayList<ExternalInput>();
    protected List<ExternalStimulator> externalStimulatorList = new ArrayList<ExternalStimulator>();
    
    protected abstract void customEpisodeInterConnect(List<IEpisode> episodes) throws Exception;
    public abstract void computeParameters();
    
    protected String fieldEventTypes = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z";
    protected int interconType = 0;
    protected boolean loop = true;
    protected String strLow = "-0.25";
    protected String strHigh = "0.25";
    protected String interConnect = "0.5";
    
    protected String tupdate = "0.001";
    protected String ttotal = "20.0";
    protected String tstart = "0.0";

    public void setDefaultValues()
    {
        fieldEventTypes = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z";
        interconType = 0;
        loop = true;
        strLow = "-0.25";
        strHigh = "0.25";
        tupdate = "0.001";
        ttotal = "20.0";
        tstart = "0.0";
    }

    @Override
    public void setEventFactor(EventFactor eventTypes)
    {
        super.setEventFactor(eventTypes);
        this.size = eventTypes.getSize();
        this.weights = new double[size][size];
    }
    
    
    public void interconnect() throws Exception
    {
        connect = true;
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                if (i == j) continue;
                weights[i][j] = 0.0;
            }
        }
        switch(interconnectType)
        {
            case UNIFORM:
                for (int i = 0; i < size; i++)
                {
                    int n = (int)(Math.random() * size);
                    int[] mem = new int[n];
                    for (int j = 0; j < n; j++)
                    {
                        boolean flag = true;
                        double w = (randomHigh - randomLow) * Math.random() + randomLow;
                        int index = 0;
                        
                        while(flag)
                        {
                            index = (int)(Math.random() * size);
                            flag = false;
                            if (index == i)
                            {
                                flag = true;
                                continue;
                            }
                            for (int k = 0; k < j; k++)
                            {
                                if (index == mem[k])
                                {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        mem[j] = index;
                        weights[i][index] = w;
                    }
                }
                break;
            case FULLY_CONNECTED:
                for (int i = 0; i < size; i++)
                {
                    for (int j = 0; j < size; j++)
                    {
                        weights[i][j] = (randomHigh - randomLow) * Math.random() + randomLow;
                    }
                }
                break;
            case BINOMIAL:
                for (int i = 0; i < size; i++)
                {
                    for (int j = 0; j < size; j++)
                    {
                        int prob = (int)(2.0 * Math.random());
                        if (prob == 0) 
                            weights[i][j] = (randomHigh - randomLow) * Math.random() + randomLow;
                    }
                }
                break;
        }
        customEpisodeInterConnect(episodesList);
    }
    
    public double[] getRandomWeight()
    {
        double[] w = new double[2];
        w[0] = this.randomLow;
        w[1] = this.randomHigh;
        return w;
    }
    
    public void setRandomWeight(double[] w)
    {
        this.randomLow = w[0];
        this.randomHigh = w[1];
    }
    
    public double getWeight(int row, int col)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
            return weights[row][col];
        else
            return 0.0;
    }
    
    public abstract double getProbability(int row, int col);
    
    public void setWeight(int row, int col, double value)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
            weights[row][col] = value;
    }
    
    public abstract void saveConnectionMatrix(PrintWriter out) throws IOException;
    
    public String getEventFactorString()
    {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < size; i++) 
        {
            if (i != 0) ret.append(" ");
            ret.append(eventTypes.getName(i));
        }
        return ret.toString();
    }
    
    public abstract void loadConnectionMatrix(BufferedReader in) throws IOException;

    public String getFieldEventTypes()
    {
        return fieldEventTypes;
    }

    public void setFieldEventTypes(String fieldEventTypes)
    {
        this.fieldEventTypes = fieldEventTypes;
    }

    public int getInterconType()
    {
        return interconType;
    }

    public void setInterconType(int interconType)
    {
        this.interconType = interconType;
    }

    public boolean isLoop()
    {
        return loop;
    }

    public void setLoop(boolean loop)
    {
        this.loop = loop;
    }

    public List<ExternalInput> getExternalInputList()
    {
        return externalInputList;
    }

    public void setExternalInputList(List<ExternalInput> externalInputList)
    {
        this.externalInputList = externalInputList;
    }

    public String getInterConnect()
    {
        return interConnect;
    }

    public String getStrLow() {
        return strLow;
    }

    public void setStrLow(String strLow) {
        this.strLow = strLow;
    }

    public String getStrHigh() {
        return strHigh;
    }

    public void setStrHigh(String strHigh) {
        this.strHigh = strHigh;
    }

    public List<ExternalStimulator> getExternalStimulatorList()
    {
        return externalStimulatorList;
    }

    public void setExternalStimulatorList(List<ExternalStimulator> externalStimulatorList)
    {
        this.externalStimulatorList = externalStimulatorList;
    }
    
    public String getTupdate()
    {
        return tupdate;
    }
    
    public void setTupdate(String tupdate)
    {
        this.tupdate = tupdate;
    }
    
    public String getTtotal()
    {
        return ttotal;
    }
    
    public void setTtotal(String ttotal)
    {
        this.ttotal = ttotal;
    }

    public String getTstart()
    {
        return tstart;
    }

    public void setTstart(String tstart)
    {
        this.tstart = tstart;
    }
}
