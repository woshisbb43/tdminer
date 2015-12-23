/*
 * SastryEstimator.java
 *
 * Created on August 29, 2007, 12:29 PM
 *
 */

package edu.iisc.tdminercore.filter;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.simulation.PoissonSimulator;
import java.text.NumberFormat;
import java.util.List;

/**
 *
 * @author pbutler
 */
public class SastryEstimator extends ThresholdEstimator
{
    double eps;
    double grainSize;
    double T;
    int n;
    double f,v;
            
    public SastryEstimator()
    {
    }
    
    public double[] threshold(SessionInfo session, IEpisode gamma)
    {
        this.eps = session.getErrorTypeI();
        this.grainSize = session.getTimeGranularity();
        if (session.isSegmented()) requiredVotes = new double[session.getSegIndexLen()];
        else requiredVotes = new double[1];
        
        this.n = gamma.size();
        this.T = deriveDuration(session, gamma);
        for (int index = 0; index < session.getSegIndexLen(); index++)
        {
            double L = session.endTime(index) - session.startTime(index);
            int startEvent = gamma.getEvent(0);
            int count = 0;
            List<IEpisode> oneNodeEpsList = session.getEpisodes().getEpisodeList(1);
            for(IEpisode e : oneNodeEpsList)
            {
                if (e.getEvent(0) == startEvent)
                {
                    count = e.getVotes(index);
                    break;
                }
            }

            double lambda = (double)count/L;  // in spikes / sec
            double pr = probability(session.getEStrong(), lambda);
            //System.out.println("count = " + count + " L = " + L + " T = " + T);
            this.setThreshold(variation(pr, L), index);
        }
        return this.requiredVotes;
    }

    /**
     * Basic constructor
     *
     * @parm error the type I error
     * @param grain the grain size of the run
     * @param order the number of nodes in the episode
     * @param frame the duration of the event stream
     * @param rate the base firing rate of the episodes first event
     * @param span the time span that an episode covers
     */
    public SastryEstimator(double eps, double grainSize, int n, double T)
    {
        this.eps = eps;
        this.grainSize = grainSize;
        this.n = n;
        this.T = T;
    }
    
    /**
    * This is approximate, as lambda is somewhat smaller than Frest.
    * @param lambda the frequency of each of the N poisson processes a.k.a. Frest
    * @param estrong the probability that one event causes another
    * @param N order of the episode (how many nodes)
    * @param span the duration of the episode
    */
    private double probability(double estrong, double lambda)
    {
        double rho = 1.0 - java.lang.Math.exp(-1.0 * lambda * this.grainSize);
        return  rho * java.lang.Math.pow( estrong, (this.n-1));
    }
    
    public double threshold(double estrong, double L, int oneNodeCount)
    {
        double lambda = (double)oneNodeCount/L;
        return variation(probability(estrong, lambda), L);
    }
    
    /**
     * This calculates the expected value and the variance.
     * @param dataIn the size of the data stream in time (seconds)
     * @param spanIn the duration of the episode in time (seconds)
     * @param pr the probablity that the episode occurs
     */
    public double variation(double pr, double L)
    {
        int data = (int)(L/grainSize);
        int span = (int)(T/grainSize);
        if (span == 0) span = 1;
        double fArray[];     // the intermediate values of F
        double gArray[];     // the intermediate values of G
        int pos;
        int delta = data - span;

        if (delta < 0)
        {
	    throw new RuntimeException("Data sequence smaller than episode span: " +
                    "L = " + L + ", T = " + T);
        }

        int len = span + 1;
        fArray = new double[len];
        fArray[0] = pr;
        gArray = new double[len];
        gArray[0] = pr;
        for (pos = 1; pos <= delta; pos++)
        {
            // F(L,T) = (1-p)F(L-1,T) + p(1+F(L-T,T))
            fArray[pos % len] = (1.0 - pr) * fArray[(pos - 1) % len];
            fArray[pos % len] += pr * (1.0 + (pos < span ? 0.0 : fArray[(pos - span) % len]));

            // G(L,T) = (1-p)G(L-1,T) + p(1+G(L-T,T) + 2F(L-T,T))
            gArray[pos % len] = (1.0 - pr) * gArray[(pos - 1) % len];

            gArray[pos % len] += pr * (1.0 +
                    (pos < span ? 0.0 : (gArray[(pos - span) % len] + 2.0 * fArray[(pos - span) % len])));
        }
        
        this.f = fArray[delta % len];
        double g = gArray[delta % len];
        this.v = g - (f * f);
        
        return (f + Math.sqrt(v/eps));
    }
    
    /**
     * This calculates the expected value and the variance.
     * @param dataIn the size of the data stream in time (seconds)
     * @param spanIn the duration of the episode in time (seconds)
     * @param pr the probablity that the episode occurs
     */
    public double meanF(double pr, double L)
    {
        int data = (int)(L/grainSize);
        int span = (int)(T/grainSize);
        if (span == 0) span = 1;
        double fArray[];     // the intermediate values of F
        int pos;
        int delta = data - span;

        if (delta < 0)
        {
	    throw new RuntimeException("Data sequence smaller than episode span: " +
                    "L = " + L + ", T = " + T);
        }

        int len = span + 1;
        fArray = new double [len];
        fArray[0] = pr;
        for (pos = 1; pos <= delta; pos++)
        {
            // F(L,T) = (1-p)F(L-1,T) + p(1+F(L-T,T))
            fArray[pos % len] = (1.0 - pr) * fArray[(pos - 1) % len];
            fArray[pos % len] += pr * (1.0 + (pos < span ? 0.0 : fArray[(pos - span) % len]));
        }

        return fArray[delta % len];        
    }

    public double getF()
    {
        return this.f;
    }

    public double getV()
    {
        return this.v;
    }
    
    double foptimize(int count, double pr, double L)
    {
        return (count - meanF(pr, L));
	//return (count - variation(pr, L));
    }
    
    
    public double[] solve(SessionInfo session, IEpisode gamma)
            throws UndefinedStrengthException
    {
        double[] retVal = null;
        double estr = session.getEStrong();
        this.eps = session.getErrorTypeI();
        this.grainSize = session.getTimeGranularity();
        if (session.isSegmented()) retVal = new double[session.getSegIndexLen()];
        else retVal = new double[1];
        
        this.n = gamma.size();
        this.T = deriveDuration(session, gamma);
        int selectedModel = session.getSelectedModel();
        
        switch(selectedModel)
        {
            case SessionInfo.RIGAT_MODEL:
            case SessionInfo.DEB_MODEL:
                if (session.isSegmented()) gamma.setBeta(new double[session.getSegIndexLen()]);
                else gamma.setBeta(new double[1]);
                break;
            default:
                gamma.setBeta(null);
                break;
        }
                
        for (int index = 0; index < session.getSegIndexLen(); index++)
        {
            int targetCount = gamma.getVotes(index);
            double L = session.endTime(index) - session.startTime(index);
            int startEvent = gamma.getEvent(0);
            int lastEvent =  gamma.getEvent(n - 1);
            int firstNodeCount = 0, lastNodeCount = 0;
            List<IEpisode> oneNodeEpsList = session.getEpisodes().getEpisodeList(1);
            for(IEpisode e : oneNodeEpsList)
            {
                if (e.getEvent(0) == startEvent)
                {
                    firstNodeCount = e.getVotes(index);
                }
                if (e.getEvent(0) == lastEvent)
                {
                    lastNodeCount = e.getVotes(index);
                }
            }
            retVal[index] = solve(targetCount, estr, L, firstNodeCount);
//            System.out.println("estr = " + retVal[index] + " targetCount = " 
//                    + targetCount + " estr = " + estr + " L = " + L 
//                    + " oneNodeCount = " + oneNodeCount);
            
            // Compute beta for rigat's model TODO:Remove this model specific
            // calculation in future
            if (n == 2)
            {
                switch(selectedModel)
                {
                    case SessionInfo.RIGAT_MODEL:
                        {
                            double pi_high = retVal[index];
                            double frest = (double)lastNodeCount / L;
                            double deltaT = grainSize;
                            double beta = Math.E * (Math.log(1.0/(frest * deltaT) - 1.0) - Math.log(1.0/pi_high - 1.0));
                            //System.out.println("beta = " + beta);
                            gamma.setBeta(index, beta);
                        }
                        break;
                    case SessionInfo.DEB_MODEL:
                        {
                            double p = retVal[index];
                            double beta = 0.95, P_max = 0.99, delta_lambda = 1.0;
                            double deltaT = grainSize;
                            double lambda_max = PoissonSimulator.getLambdaFromProbability(P_max, deltaT);
                            double lambda = PoissonSimulator.getLambdaFromProbability(p, deltaT);
                            if (lambda > lambda_max * beta) lambda = lambda_max * beta;
                            double lambda_normal = ((double)firstNodeCount)/L;
                            double w = PoissonSimulator.getWeightFromRatio(lambda_normal, lambda_max, lambda/lambda_max, delta_lambda);
                            gamma.setBeta(index, w);
                        }
                        break;
                    default:
                        break;
                }
            }
            
        }
        return retVal;        
    }
    
    /**
     * Obtain the most likely actual estrong.
     * Find the root by binary search.
     * It can be assumed that the function is monotonically increasing
     * with one x intercept.
     *
     * @param targetCount the actual count obtained.
     * @param estr the value used by the miner.
     * @return likely estrong value.
     */
    public double solve(int targetCount, double estr, double L, int oneNodeCount)
    throws UndefinedStrengthException
    {
        double lambda = (double)oneNodeCount/L;
	// double min = estr; 
        double min = 0.0;
	double max = 1.0;
	double tolerance = 0.00001;
        // compute initial values
        double x1 = min;
        double y1 = foptimize(targetCount, probability(min, lambda), L);
        double x2 = max;
        double y2 = foptimize(targetCount, probability(max, lambda), L);

        // validate that function stand as some chance of monotonicity
        if ((y1 <  0.0) && (y2 < 0.0)) 
	{
            throw new UndefinedStrengthException(targetCount, estr, y1, y2);
	}
        if ((y1 >  0.0) && (y2 > 0.0)) 
	{
            //throw new UndefinedStrengthException(targetCount, estr, y1, y2);
            if (y1 < y2)
                return x1;
            else
                return x2;
	}

        // iterate
        while (Math.abs(x1 - x2) > tolerance)
        {
            // compute a value midway within the current interval
            double newX = 0.5 * (x1 + x2);
            double newY = foptimize(targetCount, probability(newX, lambda), L);

            /* candidate algorithm
             if (newY == 0.0) return newX;
             if (newY > 0.0) {
                x1 = newX;
             else 
                x2 = newX;
            */
            // determine new interval
            if (newY >= 0.0)
            {
                if (y1 >= 0.0)
                {
                    y1 = newY;
                    x1 = newX;
                }
                else
                {
                    y2 = newY;
                    x2 = newX;
                }
            }
            else if (newY < 0.0)
            {
                if (y1 >= 0.0)
                {
                    y2 = newY;
                    x2 = newX;
                }
                else
                {
                    y1 = newY;
                    x1 = newX;
                }
            }
        }

        // converged: return midpoint of interval
        return 0.5 * (x1 + x2);
    }
    
    public class UndefinedStrengthException extends Exception
    {

        public UndefinedStrengthException(String msg)
        {
            super(msg);
        }
        public UndefinedStrengthException(int count, double estr, double y1, double y2) 
        {
            super(  " min = " + y1 + 
                    " max = " + y2 + 
                    " targetCount = " + count + 
                    " estr = " + estr);
        }
    }
    
    public static void main1(String[] args)
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(6);
        nf.setGroupingUsed(false);
        try
        {
            double eps = 0.05;
            double grainSize = 0.001;
            int n = 2;
            double L = 20.0;
            double T = grainSize * (n - 1) * 5;
            double lambda = 20.0;
            SastryEstimator s = new SastryEstimator(eps, grainSize, n, T);
            
            System.out.println("estr \t F \t V");
            System.out.println("----------------------------------");
            //double[] e_array = {0.001, 0.003, 0.005, 0.008, 0.010, 0.012, 0.015, 0.02};
            double[] e_array = {0.3, 0.4, 0.5, 0.6, 0.7};
            //double[] e_array = {0.02, 0.05, 0.1, 0.15, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
            for(double estr : e_array)
            {
                double pr = s.probability(estr, lambda);
                //double count = s.meanF(pr, L);
                double count = s.variation(pr, L);
                System.out.println(nf.format(estr) + "," + nf.format(s.f) + "," + nf.format(s.v));
            }
            System.out.println();

            int oneNodeCount = (int)(lambda * L);
            System.out.println("size \t count \t E_str(estimated)");
            System.out.println("----------------------------------");
            for(int targetCount = 0; targetCount <= -1; targetCount++)
            {
                double festr = s.solve(targetCount, 0.0, L, oneNodeCount);
                System.out.println(n + " \t " + targetCount + " \t " + nf.format(festr));
            }
        }
        catch (SastryEstimator.UndefinedStrengthException ex)
        {
            ex.printStackTrace();
        }

    }
    
    
    public static void main(String[] args)
    {
        // Inverting count to estr
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(6);
        nf.setGroupingUsed(false);
        try
        {
            double eps = 0.05;
            double grainSize = 0.001;
            int n = 5;
            double L = 50.0;
            double T = grainSize * (n - 1) * 7;
            double lambda = 20.0;
            SastryEstimator s = new SastryEstimator(eps, grainSize, n, T);
            
            int oneNodeCount = 1045;//(int)(lambda * L);
            System.out.println("size \t count \t E_str(estimated)");
            System.out.println("----------------------------------");
            for(int targetCount = 0; targetCount <= 10; targetCount++)
            {
                double festr = s.solve(targetCount, 0.0, L, oneNodeCount);
                System.out.println(n + " \t " + targetCount + " \t " + nf.format(festr));
            }
        }
        catch (SastryEstimator.UndefinedStrengthException ex)
        {
            ex.printStackTrace();
        }

    }
}


