/*
 * ThresholdFilterType.java
 *
 * Created on August 27, 2007, 3:50 PM
 *
 */
package edu.iisc.tdminercore.filter;

/**
 *
 * @author phreed@gmail.com
 */
public enum ThresholdFilterType
{
    STRENGTH_BASED("Episode Connection Strength"),
    EXPLICIT_DECAY("Explicit Decaying Threshold"),
    POISSON_BASED("Auto-threshold for parallel episode"),
    NEG_STRENGTH("Negative Connection Strength");
    private final String title;

    private ThresholdFilterType(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return this.title;
    }

    @Override
    public String toString()
    {
        return this.title;
    }
}
