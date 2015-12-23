package edu.iisc.tdminercore.simulation;

import java.text.NumberFormat;

public class FRamp extends F
{

    double min;
    double max;
    int steps;
    double step_size;
    double shift;
    double rise;

    public FRamp(int src, int dest, double min, double max, int steps,
            double step_size, double shift)
    {
        super(src, dest);
        setup(min, max, steps, step_size, shift);
    }

    @Override
    public double getValue( double t)
    {
        if (t < shift)
        {
            return min;
        }
        int i = (int) ((t - shift) / step_size) % (2 * steps);
        if (i >= steps)
        {
            i = 2 * steps - i;
        }
        return min + i * rise;
    }

    @Override
    public String toString()
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(4);
        return "ramp(min=" + nf.format(min) + ", max=" + nf.format(max) + ",steps=" + steps + ",step_size=" + nf.format(step_size) + ",shift=" + nf.format(shift);
    }

    public double getMax()
    {
        return max;
    }

    public double getMin()
    {
        return min;
    }

    public double getShift()
    {
        return shift;
    }

    public double getStep_size()
    {
        return step_size;
    }

    public int getSteps()
    {
        return steps;
    }

    public void setup(double min, double max, int steps,
            double step_size, double shift)
    {
        if (steps < 1)
        {
            throw new RuntimeException("Number of steps must be " +
                    "more than 0 [ steps =  " + steps + "]");
        }
        if (step_size <= 0.0)
        {
            throw new RuntimeException("Step size must be non-zero");
        }
        this.min = min;
        this.max = max;
        this.steps = steps;
        this.step_size = step_size;
        this.shift = shift;
        this.rise = (max - min) / steps;
    }
}
