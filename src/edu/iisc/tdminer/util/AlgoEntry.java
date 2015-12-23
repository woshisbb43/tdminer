/*
 * AlgoEntry.java
 *
 * Created on July 4, 2006, 1:44 PM
 *
 */

package edu.iisc.tdminer.util;

import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;

/**
 * These describe frequent episode counting algorithms.
 *
 * @author patnaik
 * @author phreed@gmail.com
 */
public class AlgoEntry
{
    // Algorithm name
    private String name;
    // Algorithm
    private AbstractEpisodeCounter counter;
    //   Candidate algo index,
    private int candidateIndex;
    //   episode expiry enable,
    private boolean epsExpiryEnable;
    //   interval expiry enable,
    private boolean ivlHighEnable;
    //   interval Low enable,
    private boolean ivlLowEnable;
    //   allow repeated events,
    private boolean allowRepeatedEventTypes;
    private int constraintType;
    
    public enum GraphType { serial, parallel }
    private GraphType graphType = GraphType.serial;
    
    public static final int EXPLICIT = 0;
    public static final int INTER_EVENT_DISCOVERY = 1;
    public static final int DURATION_DISCOVERY = 2;
    public static final int DURATION_DISCOVERY_WITH_EXPIRY = 3;
    
    private static final String SPC = "      ";
    public boolean fake = false;
    
    /** Creates a new instance of AlgoEntry */
    public AlgoEntry(String name, GraphType graphType)
    {
	this.name = name;
	this.fake = true;
        this.graphType = graphType;
    }
    /** Indicates if the algorithm is 'real' or a 'fake'.
     * Fake algorithms can act as placeholders but should not be used.
     */
    public boolean isFake() { return this.fake; }
    public boolean isSerial() { return this.graphType == GraphType.serial; }
    public boolean isParallel() { return this.graphType == GraphType.parallel; }
    
    /** Creates a new instance of AlgoEntry */
    public AlgoEntry(AbstractEpisodeCounter counter,
	    int candidateIndex,
            GraphType graphType,
	    boolean epsExpiryEnable,
	    boolean ivlHighEnable,
	    boolean ivlLowEnable,
	    boolean allowRepeatedEventTypes,
	    int constraintType)
    {
	this.name = SPC + counter.getName();
	this.counter = counter;
	this.candidateIndex = candidateIndex;
        this.graphType = graphType;
	this.epsExpiryEnable = epsExpiryEnable;
	this.ivlHighEnable = ivlHighEnable;
	this.ivlLowEnable = ivlLowEnable;
	this.allowRepeatedEventTypes = allowRepeatedEventTypes;
	this.constraintType = constraintType;
    }
    
    public int getCandidateIndex()
    {
	return candidateIndex;
    }
    
    public boolean isEpsExpiryEnable()
    {
	return epsExpiryEnable;
    }
    
    public boolean isIvlHighEnable()
    {
	return ivlHighEnable;
    }
    
    public boolean isIvlLowEnable()
    {
	return ivlLowEnable;
    }
    
    public boolean isAllowRepeatedEventTypes()
    {
	return allowRepeatedEventTypes;
    }
    
    public String getName()
    {
	return name;
    }
    
    public AbstractEpisodeCounter getCounter()
    {
	return counter;
    }
    
    public String toString()
    {
        return name;
    }

    public int getConstraintType()
    {
        return constraintType;
    }
    
}
