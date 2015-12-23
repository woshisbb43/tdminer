package edu.iisc.tdminercore.simulation;

import java.io.Serializable;

/**
 * Class: ExternalStimulator
 *
 * @author debprakash
 */
public class ExternalStimulator implements Serializable {
    
    private int size;
    private String file;
    private boolean[] isConnected;
    private double[] prob;
    private transient ExternalSpikeSource es;
    private boolean selected = true;
    
    public ExternalStimulator(int size, String file)
    {
        this.size = size;
        this.file = file;
        this.isConnected = new boolean[size];
        this.prob = new double[size];
    }
    
    public void setConnected(int index, boolean val)
    {
        isConnected[index] = val;
    }
    
    public boolean isConnected(int index)
    {
        return isConnected[index];
    }
    
    public void setProb(int index, double value)
    {
        prob[index] = value;
    }
    
    public double getProb(int index)
    {
        return prob[index];
    }
    
    public void connectAll(double prob)
    {
        for(int i = 0; i < size; i++)
        {
            this.isConnected[i] = true;
            this.prob[i] = prob;
        }
    }
    
    public void init()
    {
        this.es = new ExternalSpikeSource(file);
    }
    
    public void reset()
    {
        init();
    }
    
    public ExternalSpikeSource getExternalSource()
    {
        return es;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public String getFile()
    {
        return file;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ExternalStimulator other = (ExternalStimulator) obj;
        if (this.size != other.size)
        {
            return false;
        }
        if (this.file == null || !this.file.equals(other.file))
        {
            return false;
        }
        boolean flag = true;
        for(int i = 0; i < isConnected.length; i++)
        {
            if (this.isConnected[i] != other.isConnected[i])
            {
                flag = false;
                break;
            }
        }
        if (this.isConnected != other.isConnected && (this.isConnected == null || !flag))
        {
            return false;
        }
        flag = true;
        for(int i = 0; i < isConnected.length; i++)
        {
            if (Math.abs(this.prob[i] - other.prob[i]) > 1E-6)
            {
                flag = false;
                break;
            }
        }
        if (this.prob != other.prob && (this.prob == null || !flag))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + this.size;
        hash = 97 * hash + (this.file != null ? this.file.hashCode() : 0);
        hash = 97 * hash + (this.isConnected != null ? this.isConnected.hashCode() : 0);
        hash = 97 * hash + (this.prob != null ? this.prob.hashCode() : 0);
        return hash;
    }   
}
