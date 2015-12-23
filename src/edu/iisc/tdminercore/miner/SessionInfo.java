/*
 * MinerConfig.java
 *
 * Created on May 1, 2007, 10:51 AM
 *
 * The session contains all the state information required to
 * perform an data mining process.
 *
 */
package edu.iisc.tdminercore.miner;

import edu.iisc.tdminercore.candidate.AbstractCandidateGen;
import edu.iisc.tdminercore.candidate.IProgress;
import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.filter.ThresholdFilterType;
import edu.iisc.tdminercore.util.TimeConstraint;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hzg3nc
 * @author phreed@gmail.com
 */
public class SessionInfo
{

    private double[] frequencyThreshold;
    private double freqDecay;
    private int minLevels;
    private int maxLevels;
    private EventFactor factor = null;
    private double intervalExpiry;
    private double episodeExpiry;
    private double intervalExpiryLow;
    private List<Interval> intervalsList = null;
    private List<Interval> durationsList = null;
    private AbstractCandidateGen candidateGenerator;
    private AbstractEpisodeCounter counter;
    private boolean allowRepeat;
    private boolean durationSingle = true;
    private int chunkLimit = 5000;
    private IProgress candidateGeneratorProgress = null;
    private EpisodeSet episodes;
    private EpisodeSet reference;
    private IEventDataStream sequence;
    private TimeConstraint<IEventDataStream.CONSTRAINT_MODE> timeConstraints;
    // the following are for controlling threshold filters
    private ThresholdFilterType thresholdType = ThresholdFilterType.STRENGTH_BASED;
    private double eStrong = 0.20;
    private double timeGranularity = 0.001;
    private double errorTypeI = 0.05;
    private double poissonError = 0.01;
    private boolean trackEpisodes = false;
    private int currentLevel = 1; // Starts from 1 for 1-node episodes
    private double currentThreshold = 0.0;
    private double adaptiveThresholdParameter = 1.0;
    private boolean backPruningEnabled = false;
    private double muchGreater = 5.0;
    private int fixedThresholdLimit = 2;
    private boolean segmented = false;
    private double segLen = 0.0;
    private int currentSegIndex = 0;
    private int segIndexLen;
    private int selectedModel = 0;
    
    public static final int NONE_MODEL = 0;
    public static final int RIGAT_MODEL = 1;
    public static final int DEB_MODEL = 2;

    /** Creates a new instance of MinerConfig */
    public SessionInfo()
    {
        this.frequencyThreshold = new double[2];
    }

    public int getSegIndexLen()
    {
        if (segmented)
        {
            double span = sequence.getSequenceEnd() - sequence.getSequenceStart();
            segIndexLen = (int)(span/segLen) + 1;
            return segIndexLen;
        }
        return 1;
    }

    public int getCurrentSegIndex()
    {
        return currentSegIndex;
    }

    public void setCurrentSegIndex(int currentSegIndex)
    {
        this.currentSegIndex = currentSegIndex;
    }

    public double getSegLen()
    {
        return segLen;
    }

    public void setSegLen(double segLen)
    {
        this.segLen = segLen;
    }

    public boolean isSegmented()
    {
        return segmented;
    }

    public void setSegmented(boolean segmented)
    {
        this.segmented = segmented;
    }

    public SessionInfo createCopy()
    {
        SessionInfo s = new SessionInfo();
        s.frequencyThreshold = frequencyThreshold;
        s.freqDecay = freqDecay;
        s.minLevels = minLevels;
        s.maxLevels = maxLevels;
        s.thresholdType = thresholdType;
        s.factor = factor;
        s.intervalExpiry = intervalExpiry;
        s.episodeExpiry = episodeExpiry;
        s.intervalExpiryLow = intervalExpiryLow;
        s.intervalsList = intervalsList;
        s.durationsList = durationsList;
        s.candidateGenerator = candidateGenerator;
        s.counter = counter;
        s.allowRepeat = allowRepeat;
        s.durationSingle = durationSingle;
        s.chunkLimit = chunkLimit;
        s.candidateGeneratorProgress = candidateGeneratorProgress;
        if (episodes != null)
        {
            try
            {
                s.episodes = episodes.getClone();
            }
            catch (CloneNotSupportedException ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            s.episodes = episodes;
        }

        if (sequence != null)
        {
            s.sequence = (IEventDataStream) sequence.clone();
        }
        else
        {
            s.sequence = sequence;
        }
        s.timeConstraints = timeConstraints;
        s.eStrong = eStrong;
        s.timeGranularity = timeGranularity;
        s.errorTypeI = errorTypeI;
        s.poissonError = poissonError;
        s.trackEpisodes = trackEpisodes;
        s.currentLevel = currentLevel;
        s.adaptiveThresholdParameter = adaptiveThresholdParameter;
        s.backPruningEnabled = backPruningEnabled;
        s.muchGreater = muchGreater;
        s.fixedThresholdLimit = fixedThresholdLimit;
        s.segmented = segmented;
        s.segLen = segLen;
        s.selectedModel = selectedModel;
        return s;
    }

    public double getFrequencyThreshold(int order)
    {
        if (order < 1)
        {
            return 0.0;
        }
        if (order <= this.frequencyThreshold.length)
        {
            return this.frequencyThreshold[order - 1];
        }
        double lastThreshold = frequencyThreshold[frequencyThreshold.length - 1];
        int neworder = order - this.frequencyThreshold.length;
        lastThreshold *= Math.pow(freqDecay, neworder);
        return lastThreshold;
    }

    public double[] getFrequencyThreshold()
    {
        return this.frequencyThreshold;
    }

    public void setFrequencyThreshold(Integer order, double f)
    {
        if (this.frequencyThreshold.length < order)
        {
            double[] ft = new double[order];
            for (int ix = 0; ix < this.frequencyThreshold.length; ix++)
            {
                ft[ix] = this.frequencyThreshold[ix];
            }
            this.frequencyThreshold = ft;
        }
        this.frequencyThreshold[order - 1] = f;
    }

    public void setFrequencyThreshold(double[] frequencyThreshold)
    {
        this.frequencyThreshold = new double[frequencyThreshold.length];
        for (int ix = 0; ix < frequencyThreshold.length; ix++)
        {
            this.frequencyThreshold[ix] = frequencyThreshold[ix];
        }
    }

    public double getFreqDecay()
    {
        return freqDecay;
    }

    public void setFreqDecay(double freqDecay)
    {
        this.freqDecay = freqDecay;
    }

    public int getGlevels()
    {
        return minLevels;
    }

    public void setGlevels(int minLevels)
    {
        this.minLevels = minLevels;
    }

    public int getPlevels()
    {
        return maxLevels;
    }

    public void setPlevels(int maxLevels)
    {
        this.maxLevels = maxLevels;
    }

    public ThresholdFilterType getThresholdType()
    {
        return this.thresholdType;
    }

    public void setThresholdType(ThresholdFilterType type)
    {
        this.thresholdType = type;
    }

    public EventFactor getEventFactor()
    {
        if (this.factor == null)
        {
            this.factor = new EventFactor();
        }
        return this.factor;
    }

    public void setEventFactor(EventFactor factor)
    {
        this.factor = factor;
    }

    public double getIntervalExpiry()
    {
        return intervalExpiry;
    }

    public void setIntervalExpiry(double intervalExpiry)
    {
        this.intervalExpiry = intervalExpiry;
    }

    public double getEpisodeExpiry()
    {
        return episodeExpiry;
    }

    public void setEpisodeExpiry(double episodeExpiry)
    {
        this.episodeExpiry = episodeExpiry;
    }

    public double getIntervalExpiryLow()
    {
        return intervalExpiryLow;
    }

    public void setIntervalExpiryLow(double intervalExpiryLow)
    {
        this.intervalExpiryLow = intervalExpiryLow;
    }

    public List<Interval> getIntervalsList()
    {
        if (intervalsList == null)
        {
            intervalsList = new ArrayList<Interval>();
        }
        return intervalsList;
    }
    /**
     * This pair of functions marshalls/unmarshalls the intevals list.
     * The intervals are represented as colon separated pairs of the form...
     * 0.001-0.002!!0.004-0.010!!0.005-0.020
     * Whitespace is to be collapsed in most cases.
     */
    private final String LIST_DELIMITER = "!!";

    public String getIntervalsListMarshall()
    {
        StringBuilder list = new StringBuilder();
        boolean firstFlag = true;
        for (Interval interval : this.intervalsList)
        {
            if (firstFlag)
            {
                firstFlag = false;
            }
            else
            {
                list.append(LIST_DELIMITER);
            }
            list.append(interval.toString());
        }
        return list.toString();
    }

    public void setIntervalsList(String intervalsList)
    {
        this.intervalsList = new ArrayList<Interval>();

        for (String interval : intervalsList.split(LIST_DELIMITER))
        {
            this.intervalsList.add(new Interval(interval));
        }
    }

    public void setIntervalsList(List<Interval> intervalsList)
    {
        this.intervalsList = intervalsList;
    }

    public void setIntervalsList(double[][] intervalArray)
    {
        this.intervalsList = new ArrayList<Interval>(intervalArray.length);
        for (int ix = 0; ix < intervalArray.length; ix++)
        {
            double[] intervalPair = intervalArray[ix];
            if (intervalPair.length < 2)
            {
                continue;
            }

            this.intervalsList.add(new Interval(intervalPair[0], intervalPair[1]));
        }
    }

    public List<Interval> getDurationsList()
    {
        return durationsList;
    }

    public void setDurationsList(List<Interval> durationsList)
    {
        this.durationsList = durationsList;
    }

    public String getDurationsListMarshall()
    {
        StringBuilder list = new StringBuilder();
        boolean firstFlag = true;
        for (Interval interval : this.durationsList)
        {
            if (firstFlag)
            {
                firstFlag = false;
            }
            else
            {
                list.append(LIST_DELIMITER);
            }
            list.append(interval.toString());
        }
        return list.toString();
    }

    public void setDurationsList(String durationsList)
    {
        this.durationsList = new ArrayList<Interval>();

        for (String interval : durationsList.split(LIST_DELIMITER))
        {
            this.durationsList.add(new Interval(interval));
        }
    }

    public AbstractCandidateGen getCandidateGenerator()
    {
        return candidateGenerator;
    }

    public void setCandidateGenerator(AbstractCandidateGen candidateGenerator)
    {
        this.candidateGenerator = candidateGenerator;
    }

    public AbstractEpisodeCounter getCounter()
    {
        return counter;
    }

    public void setCounter(AbstractEpisodeCounter counter)
    {
        this.counter = counter;
    }

    public boolean isAllowRepeat()
    {
        return allowRepeat;
    }

    public void setAllowRepeat(boolean allowRepeat)
    {
        this.allowRepeat = allowRepeat;
    }

    public boolean isDurationSingle()
    {
        return durationSingle;
    }

    public void setDurationSingle(boolean durationSingle)
    {
        this.durationSingle = durationSingle;
    }

    public int getChunkLimit()
    {
        return chunkLimit;
    }

    public void setChunkLimit(int chunkLimit)
    {
        this.chunkLimit = chunkLimit;
    }

    public IProgress getCandidateGeneratorProgress()
    {
        return candidateGeneratorProgress;
    }

    public void setCandidateGeneratorProgress(IProgress progress)
    {
        this.candidateGeneratorProgress = progress;
    }

    public EpisodeSet getEpisodes()
    {
        return episodes;
    }

    public void setEpisodes(EpisodeSet episodes)
    {
        this.episodes = episodes;
    }

    public EpisodeSet getReference()
    {
        return this.reference;
    }

    public void setReference(EpisodeSet reference)
    {
        this.reference = reference;
    }

    public void swapEpisodes()
    {
        EpisodeSet wip = this.reference;
        this.reference = this.episodes;
        this.episodes = wip;
    }

    public IEventDataStream getSequence()
    {
        return sequence;
    }

    public void setSequence(IEventDataStream sequence)
    {
        this.sequence = sequence;
        sequence.setConstraints(this.timeConstraints);
        this.factor = sequence.getEventFactor();
    }

    public void setTimeConstraints(TimeConstraint<CONSTRAINT_MODE> constraints)
    {
        if (this.timeConstraints == constraints)
        {
            return;
        }
        this.timeConstraints = constraints;
        if (this.sequence != null)
        {
            this.sequence.setConstraints(constraints);
        }
    }

    public TimeConstraint<CONSTRAINT_MODE> getTimeConstraints()
    {
        if (this.timeConstraints == null)
        {
            this.timeConstraints = new TimeConstraint<CONSTRAINT_MODE>();
        }
        return this.timeConstraints;
    }

    public double getEStrong()
    {
        return eStrong;
    }

    public void setEStrong(double eStrong)
    {
        this.eStrong = eStrong;
    }

    public double getErrorTypeI()
    {
        return errorTypeI;
    }

    public void setErrorTypeI(double errorTypeI)
    {
        this.errorTypeI = errorTypeI;
    }

    public void setTimeGranularity(double timeGranularity)
    {
        this.timeGranularity = timeGranularity;
    }

    public double getTimeGranularity()
    {
        return timeGranularity;
    }

    public boolean isTrackEpisodes()
    {
        return trackEpisodes;
    }

    public void setTrackEpisodes(boolean trackEpisodes)
    {
        this.trackEpisodes = trackEpisodes;
    }

    public int getCurrentLevel()
    {
        return currentLevel;
    }

    public void setCurrentLevel(int level)
    {
        this.currentLevel = level;
    }

    public void incrementLevel()
    {
        this.currentLevel++;
    }

    public double getCurrentThreshold()
    {
        return currentThreshold;
    }

    public void setCurrentThreshold(double currentThreshold)
    {
        this.currentThreshold = currentThreshold;
    }

    public double getAdaptiveThresholdParameter()
    {
        return adaptiveThresholdParameter;
    }

    public void setAdaptiveThresholdParameter(double adaptiveThresholdParameter)
    {
        this.adaptiveThresholdParameter = adaptiveThresholdParameter;
    }

    /**
     * Getter for property backPruningEnabled.
     * @return Value of property backPruningEnabled.
     */
    public boolean isBackPruningEnabled()
    {
        return this.backPruningEnabled;
    }

    /**
     * Setter for property backPruningEnabled.
     * @param backPruningEnabled New value of property backPruningEnabled.
     */
    public void setBackPruningEnabled(boolean backPruningEnabled)
    {
        this.backPruningEnabled = backPruningEnabled;
    }

    /**
     * Getter for property muchGreater.
     * @return Value of property muchGreater.
     */
    public double getMuchGreater()
    {
        return this.muchGreater;
    }

    /**
     * Setter for property muchGreater.
     * @param muchGreater New value of property muchGreater.
     */
    public void setMuchGreater(double muchGreater)
    {
        this.muchGreater = muchGreater;
    }

    public int getFixedThresholdLimit()
    {
        return fixedThresholdLimit;
    }

    public void setFixedThresholdLimit(int fixedThresholdLimit)
    {
        this.fixedThresholdLimit = fixedThresholdLimit;
    }

    public double getPoissonError()
    {
        return poissonError;
    }

    public void setPoissonError(double poissonError)
    {
        this.poissonError = poissonError;
    }
    
    public double startTime()
    {
        return startTime(currentSegIndex);
    }
    
    public double endTime()
    {
        return endTime(currentSegIndex);
    }
    
    public double startTime(int index)
    {
        if (segmented)
        {
            double t = sequence.getSequenceStart() + (index * segLen);
            return t;
        }
        return sequence.getSequenceStart();
    }
    
    public double endTime(int index)
    {
        if (segmented)
        {
            double t = sequence.getSequenceStart() + ((index + 1) * segLen);
            if (t > sequence.getSequenceEnd()) t = sequence.getSequenceEnd();
            return t;
        }
        return sequence.getSequenceEnd();
    }

    public void updateSegIndex(double t)
    {
        if (isSegmented() && t > endTime())
        {
            currentSegIndex++;
        }
    }
    
    public void resetSegIndex() { currentSegIndex = 0; }
    public void incrSegIndex() { currentSegIndex ++; }

    public int getSelectedModel()
    {
        return selectedModel;
    }

    public void setSelectedModel(int selectedModel)
    {
        this.selectedModel = selectedModel;
    }
}
