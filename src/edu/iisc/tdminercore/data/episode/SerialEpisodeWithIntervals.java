/*
 * SerialEpisodeWithIntervals.java
 *
 * Created on May 4, 2007, 9:11 AM
 *
 */
package edu.iisc.tdminercore.data.episode;

import edu.iisc.tdminercore.data.AbstractEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.miner.SessionInfo;
import java.util.List;
import edu.iisc.tdminercore.filter.SastryEstimator;
import edu.iisc.tdminercore.filter.SastryNegativeEstimator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author hzg3nc
 */
public class SerialEpisodeWithIntervals extends Episode
{

    private static final boolean DEBUG = false;

    /** Creates a new instance of SerialEpisodeWithFixedInterval */
    public SerialEpisodeWithIntervals(int size, EventFactor f)
    {
        super(size, f);
        //setEstr(new double[1]);
    }

    public SerialEpisodeWithIntervals(int[] events, EventFactor f)
    {
        super(events, f);
        //setEstr(new double[1]);
    }

    public SerialEpisodeWithIntervals(int size, IEpisode that)
    {
        super(size, that);
        this.setEstr(that.getEstr());
    }

    /** Creates a new instance of an episode
     * The number of intervals will be one less than the number of events
     * as the intervals separate events.
     * @param size the order of the episode.
     * @param intervalsList a list of intervals
     */
    public SerialEpisodeWithIntervals(int size, EventFactor f, List<Interval> intervalsList)
    {
        super(size, f, intervalsList);
        //setEstr(new double[1]);
    }

    public SerialEpisodeWithIntervals(int[] events, EventFactor f, int[] interval, List<Interval> intervalsList)
    {
        super(events, f, interval, intervalsList);
        //setEstr(new double[1]);
    }

    protected SerialEpisodeWithIntervals()
    {
        //setEstr(new double[1]);
    }

    @Override
    public void evaluateRequiredVotes(SessionInfo session, Object[] params)
    {

        IEpisode alpha = (IEpisode) params[0];
        IEpisode beta = (IEpisode) params[1];

        switch (session.getThresholdType())
        {
            case STRENGTH_BASED:
                if (size() == 1)
                {
                    int num_segs = 1;
                    if (session.isSegmented()) num_segs = session.getSegIndexLen();
                    requiredVotes = new double[num_segs];
                    for (int i = 0; i < requiredVotes.length; i ++)
                        requiredVotes[i] = 2;
                }
                else
                {
                    SastryEstimator se = new SastryEstimator();
                    this.requiredVotes = se.threshold(session, this);
                }
                break;
            case NEG_STRENGTH:
                if (size() == 1)
                {
                    int num_segs = 1;
                    if (session.isSegmented()) num_segs = session.getSegIndexLen();
                    requiredVotes = new double[num_segs];
                    for (int i = 0; i < requiredVotes.length; i ++)
                        requiredVotes[i] = Integer.MAX_VALUE;
                }
                else
                {
                    SastryNegativeEstimator se = new SastryNegativeEstimator();
                    this.requiredVotes = se.threshold(session, this);
                }
                break;
        }
        if (DEBUG)
        {
            System.out.println("Threshold: " + this.requiredVotes);
        }
    }

    @Override
    public void postCountProcessing(SessionInfo session)
    {
        if (this.size() < 2)
        {
            return;
        }
        SastryEstimator se = new SastryEstimator();
        try
        {
            double[] estr = se.solve(session, this);
            this.setEstr(estr);
        }
        catch (SastryEstimator.UndefinedStrengthException ex)
        {
            System.out.println("Could not estimate Estrong " +
                    ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Check that two episodes are equal.
     * Check that the event types match by invoking the parent comparison.
     * Provided that check passes, compare the intervals.
     * @param o the episode object
     * @result true if they are equal false if not.
     */
    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
        {
            return false;
        }

        // Compare intervals 
        Episode e = (Episode) o;
        // at present if both interval lists are null false will be returned.
        if (e.getIntervalsList() == null)
        {
            return false;
        }
        if (this.intervalsList == null)
        {
            return false;
        }

        for (int i = 0; i < interval.length; i++)
        {
            if (interval[i] == e.getIntervalId(i))
            {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Check to see if this episode is subset of that
     * @param o the episode object
     * @result true if they are equal false if not.
     */
    @Override
    public int isSubEpisode(IEpisode that)
    {
        int retVal = 0;
        if (that instanceof SerialEpisodeWithIntervals)
        {
            SerialEpisodeWithIntervals beta = this;
            SerialEpisodeWithIntervals alpha = (SerialEpisodeWithIntervals) that;
            int[] b = beta.event;//short
            int[] a = alpha.event;//long
            int[] b_ivl = beta.interval;
            int[] a_ivl = alpha.interval;
            int rval = 1;
            if (a.length < b.length)
            {
                b = alpha.event;
                a = beta.event;
                b_ivl = alpha.interval;
                a_ivl = beta.interval;
                rval = 2;
            }

            int i = 0;
            boolean firstfound = false;
            for (int j = 0; j < a.length; j++)
            {
                if (firstfound)
                {
                    if (b[i] != a[j] || b_ivl[i - 1] != a_ivl[j - 1])
                    {
                        break;
                    }
                    i++;
                    if (i >= b.length)
                    {
                        break;
                    }
                }
                else if (b[i] == a[j])
                {
                    firstfound = true;
                    i++;
                    if (i >= b.length)
                    {
                        break;
                    }
                }
            }
            if (firstfound && i == b.length)
            {
                retVal = rval;
            }
        }
        return retVal;
    }

    /**
     * Create the appropriate episode by unmarshalling the episode string.
     * sample:
     *  two node episodes...
     *  A B   > no inter-event interval set to 0-Inf and stregth to 1.0
     *  A [x-y:S] B > with an inter-event interval [x-y] and strength S
     *  A [x:S] B > with an inter-event delay and strength S
     *
     * @param eps a serialized representation of an episode.
     * @param eventTypes an object containing the event types.
     */
    public static IEpisode getEpisode(String eps, EventFactor eventTypes)
            throws IEpisode.TypeMisMatchException
    {
        List<Token> v1 = new ArrayList<Token>();  // Either of Param or Vector<String>
        StringTokenizer tok = new StringTokenizer(eps, " ()", true);

        int size = 0;
        try
        {
            // extract the tokens putting them in a vector
            while (tok.hasMoreTokens())
            {
                String token = tok.nextToken();
                System.out.print(token + " ");
                if (token.equals(" "))
                {
                    continue;
                }

                if (token.startsWith("[") && token.endsWith("]"))
                {
                    String inp = token.substring(1, token.length() - 1);
                    int index = inp.indexOf(",");
                    double strength = 1.0;
                    double delay = 0.0;
                    if (index != -1)
                    {
                        delay = Double.parseDouble(inp.substring(0, index));
                        strength = Double.parseDouble(inp.substring(index + 1));
                    }
                    else
                    {
                        delay = Double.parseDouble(inp);
                    }
                    v1.add(new Token(delay, strength));
                }
                else
                {
                    v1.add(new Token(token));
                    size++;
                }
            }

            System.out.println("size = " + size);
            int[] events = new int[size];
            List<Interval> intervalsList = new ArrayList<Interval>();
            int[] intervals = new int[size - 1];

            AbstractEpisode episode = new SerialEpisodeWithIntervals(size, eventTypes);

            int eIndex = 0;
            for (Token token : v1)
            {
                switch (token.getType())
                {
                    case EVENT:
                        int typeid = eventTypes.put(token.getName());
                        episode.setEvent(eIndex, typeid);
                        eIndex++;
                        break;
                    case INTERVAL:
                        Interval interval = token.getInterval();
                        episode.setInterval(eIndex, interval);
                        double[] estr = new double[1];
                        estr[0] = token.getStrength();
                        episode.setEstr(estr);
                        break;
                }
            }

            System.out.println("returned episode");

        }
        catch (RuntimeException re)
        {
            System.out.println(re.toString());
            re.printStackTrace();
            throw new IEpisode.TypeMisMatchException("Invalid input string");
        }
        System.out.println("returned episode");
        return null;
    }

    /**
     * a token can be either an event name or an interval description.
     */
    private static class Token
    {

        public enum Type
        {

            EVENT, INTERVAL
        }

        
        
          ;
        private  Type type;
        private  String name;
        private   Interval 

         

         interval ;  
        
              
               private double strength;
        
        // use this constructor for intervals
	public Token(double delay, double strength)
	{
            this.type = Type.INTERVAL;
	    this.interval = new Interval(0.0, delay);
            this.strength = strength;
        }
        // use this constructor for event names
        public Token(String name)
        {
            this.type = Type.EVENT;
            this.name = name;
        }

        public Type getType()
        {
            return this.type;
        }

        public String getName()
        {
            return this.name;
        }

        public Interval getInterval()
        {
            return this.interval;
        }

        public double getStrength()
        {
            return strength;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Token))
            {
                return false;
            }

            SerialEpisodeWithIntervals.Token that = (SerialEpisodeWithIntervals.Token) obj;
            if (this.type != that.type)
            {
                return false;
            }

            switch (this.type)
            {
                case EVENT:
                    if (!this.name.equals(that.name))
                    {
                        return false;
                    }
                    break;
                case INTERVAL:
                    if (Math.abs(this.interval.getGap() - that.getInterval().getGap()) > 0.00001)
                    {
                        return false;
                    }
                    if (Math.abs(this.getStrength() - that.getStrength()) > 0.000001)
                    {
                        return false;
                    }
                    break;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 17 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 17 * hash + (this.interval != null ? this.interval.hashCode() : 0);
            hash = 17 * hash + (int) (Double.doubleToLongBits(this.strength) ^ (Double.doubleToLongBits(this.strength) >>> 32));
            return hash;
        }

        @Override
        public String toString()
        {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(6);
            double delay = this.interval.getGap();
            if (strength > 0.0)
            {
                return "[" + nf.format(delay) + "," +
                        nf.format(strength) + "]";
            }
            return "[" + nf.format(delay) + "]";
        }
    }

    @Override
    public Object clone()
    {
        SerialEpisodeWithIntervals e = new SerialEpisodeWithIntervals();
        createCopy(e);
        return e;
    }
    /**
     * Create the appropriate episode by unmarshalling the episode string.
     * sample:
     *  two node episodes...
     *  A B   > no inter-event interval set to 0-Inf and stregth to 1.0
     *  A [x-y:S] B > with an inter-event interval [x-y] and strength S
     *  A [x:S] B > with an inter-event delay and strength S
     *
     * @param eps a serialized representation of an episode.
     * @param eventTypes an object containing the event types.
     */
    /*
     * /**
     * Parses a line that is presumed to contain a episode signature.
     * The episode signature is as follows:
     * (ET1 [TD1:ES1] ET2 ... ETn)
     * 
     * The surrounding parenthesis are required.
     * ET#  represents an event type
     * 
     * TD# represents an inter-event interval/delay.
     * If subsequent TD# values are omitted they are presumed to be "0-Inf", 
     * If TD# is '=' then it is presumed to be the same as the previous.
     * TD# can be a range, e.g. 0.001-0.005, as the hyphen cannot be mistaken
     * for a negative sign given that intervals must be positive real values.
     * 
     * ES# the connection strength.
     * If ES# is missing then it should be taken as unknown.
     * If the ES# is specified the preceding colon is required.
     * 
     * When episodes are loaded to the simulator any ranges are 
     * collapsed to the midpoint of the range.
     */
    /*
    // find the largest parenthesis bounded substring.
    private static final Pattern episodePat = 
    Pattern.compile("(?x)" +
    "\\(\\s*" + // the opening '('
    "([\\p{Graph}\\s]+)" +  // the payload
    "\\s*\\)");  // the closing ')'
     * find an interval designator, includes an optional interval and an required event
     * decompose an interval into its parts, i.e. range, and estrong
     * e.g. the generall pattern for an interval is
     *  ... [...] aaa ...
     */
    /*
    private static final Pattern intervalPat = 
    Pattern.compile("(?x)" +
    "\\s*" +  // pass by any leading blanks
    "(?:\\[(.*)\\]+)?" + // capture the [interval:estrong] (may be empty)
    "([\\p{Alnum}_])"); // capture the event type name
    private static final Pattern rangePat = 
    Pattern.compile("(?x)" +
    "\\s*" +  // pass by any leading blanks
    "([\\p{Digit}\\s.]+)\\s*" + // capture the start of the interval (may be empty)
    "(?:-\\s*([\\p{Digit}\\s.]+))\\s*" + // capture the end of the interval (may be empty)
    "(?::([\\w\\s.]*))?" ); // capture estrong
    public static IEpisode fromString(String eps, EventFactor eventTypes)
    throws IEpisode.TypeMisMatchException
    {
    Matcher episodeMatch = AbstractEpisode.episodePat.matcher(line);
    int episodeCount = 0;
    for( ; episodeMatch.find(); episodeCount++ ) {
    String epStr = episodeMatch.group(1);
    Matcher intervalMatch = AbstractEpisode.intervalPat.matcher(epStr);
    double delayLowPrev = 0.0D;
    double eStrongPrev = 0.0D;
    List<Double> delayLow = new ArrayList<Double>();
    List<Double> delayHigh = new ArrayList<Double>();
    List<Double> eStrong = new ArrayList<Double>();
    int size = 0;
    for(; intervalMatch.lookingAt(); size++) {
    String intervalStr = intervalMatch.group(1);
    Matcher rangeMatch = AbstractEpisode.rangePat.matcher(intervalStr);
    if (rangeMatch.matches()) {
    String delayLowStr = rangeMatch.group(1);
    if (delayLowStr.equals('=')) 
    delayLow.add(delayLowPrev);
    try {
    delayLow.add(Double.parseDouble( delayLowStr ));
    } catch (NumberFormatException ex) {
    System.err.println("could not parse: " + delayLowStr);
    delayLow.add(0.0D);
    }
    } else {
    if (intervalStr.equals('='))
    delayLow.add(delayLowPrev);
    try {
    delayLow.add(Double.parseDouble( intervalStr ));
    } catch (NumberFormatException ex) {
    System.err.println("could not parse: " + intervalStr);
    delayLow.add(0.0D);
    }
    }
    String eventStr = intervalMatch.group(1);
    }
    // Parse episodes and add to list
    IEpisode e = new Episode(size, eventTypes);
    result.add(e);
    }
     */
}


