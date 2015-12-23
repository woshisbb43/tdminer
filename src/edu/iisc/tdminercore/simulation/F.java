package edu.iisc.tdminercore.simulation;

import java.io.Serializable;

public abstract class F implements Serializable
{
    int src;
    int dest;
    boolean selected;

    public F(int src, int dest)
    {
        this.src = src;
        this.dest = dest;
    }

    public int getSrc()
    {
        return src;
    }

    public int getDest()
    {
        return dest;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public abstract double getValue(double t);
}
