/*
 * PoissonSimulator.java
 *
 * Created on April 12, 2006, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.simulation;

import edu.iisc.tdminercore.data.ComboEpisode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.util.IObserver;
import edu.iisc.tdminercore.util.RandomVariableGen;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Debprakash Patnaik
 */
public class PoissonSimulator2 
        extends PoissonSimulator 
        implements Serializable
{
    public double deltaT;
    public double lambda_normal;
    public double prob_fire;
    public int history;
    public double refractoryPeriod;
    public boolean groupInterconnect;
    public double delta_lambda;
    public double lambda_eps_factor = 1.5;
    public boolean isNormalized = false;
    
    protected PoissonSimulator2.Neuron[] net;
    protected int[] episodeStartTypes;
    
    private double beta;
    private double lambda_eps_normal;
    private double lambda_max;
    //private double EPS_WEIGHT;
    
    private int delay[][];
    private double P_matrix[][];
    
    protected String tupdate = "0.001";
    protected String ttotal = "20.0";
    protected String trefractory = "0.001";
    protected String estrong = "0.80";
    protected String frest = "20.0";
    protected String slope = "1.0";
    protected Integer synapticDelay = new Integer(5);
    protected double P_max = 0.99;
    
    public void setDefaultValues()
    {
        super.setDefaultValues();
        tupdate = "0.001";
        ttotal = "20.0";
        trefractory = "0.001";
        estrong = "0.80";
        frest = "20.0";
        slope = "1.0";
        synapticDelay = new Integer(5);
        groupInterconnect = false;
        lambda_eps_factor = 1.5;
	
	strLow = "0.01";
	strHigh = "0.04";
	
        resetModel();
    }
    
    
    /** Creates a new instance of SimulatorModelOne */
    public PoissonSimulator2()
    {
        connect = true;
    }
    
    public int getDelay(int row, int col)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
            return delay[row][col];
        else
            return 0;
    }
    
    public double getEStrong(int row, int col)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
            return P_matrix[row][col];
        else
            return 0.0;
    }

    public void setDelay(int row, int col, int value)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
            delay[row][col] = value;
    }
    
    /**
     * Takes a list of strings
     */
    public void setEventFactor(EventFactor eventTypes)
    {
        this.size = eventTypes.getSize();
        this.weights = new double[size][size];
	this.P_matrix = new double[size][size];
        this.delay = new int[size][size];
        this.eventTypes = eventTypes;
        this.net = new PoissonSimulator2.Neuron[size];
        for (int i = 0; i < size; i++) net[i] = new PoissonSimulator2.Neuron(i);
    }
    
    private static double getEstrongFromWeight(double weight, double lambda_normal, double lambda_max, double deltaT, double delta_lambda)
    {
	double lambda = delta_lambda * weight + lambda_normal;
	lambda = (lambda < 0.0) ? 0.0 : lambda;
	lambda = (lambda > lambda_max) ? lambda_max : lambda;
	double E_strong = 1.0 - Math.exp(- lambda * deltaT);
	return E_strong;
    }
    
    public static double getWeightFromRatio(double lambda_eps_normal, double lambda_max, double beta, double delta_lambda)
    {
        double w = ((beta * lambda_max) - lambda_eps_normal) / delta_lambda;
        return w;
    }
    
    public static double getLambdaFromProbability(double probability_atleast_one_arrival, double deltaT)
    {
        double lambda = -1.0 * Math.log(1.0 - probability_atleast_one_arrival)/deltaT;
        return lambda;
    }
    
    private static double getLambdaEpsFromLambdaNormal(double lambda_normal, double probability_atleast_one_arrival, double lambda_eps_factor)
    {
        return lambda_eps_factor * lambda_normal * (1.0 - probability_atleast_one_arrival);
    }
    
    public void computeParameters()
    {
        System.out.println("Compute Parameters");
        prob_fire = Double.parseDouble(estrong);
	deltaT = Double.parseDouble(tupdate);
	N = Double.parseDouble(ttotal);
	lambda_normal = Double.parseDouble(frest);
        history = synapticDelay.intValue();
        if (history <= 0) throw new NumberFormatException("Synaptic delay must >= 1 x T_update");
        refractoryPeriod = Double.parseDouble(trefractory);
        delta_lambda = Double.parseDouble(slope);
	if (isNormalized)
	{
	    lambda_eps_normal = getLambdaEpsFromLambdaNormal(lambda_normal, prob_fire, lambda_eps_factor);
	}
	else
	{
	    lambda_eps_normal = lambda_normal;
	}
        beta = 0.95;
        lambda_max = getLambdaFromProbability(P_max, deltaT);
        //EPS_WEIGHT = getWeightFromRatio(lambda_eps_normal, lambda_max, beta, delta_lambda);
    }
    
    public void resetModel()
    {
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                weights[i][j] = 0.0;
                delay[i][j] = 0;
            }
        }
    }
    
    /**
     * Simulate advances through a time using a 'clock'.
     * The clock starts at zero and advances to N.
     * At each tick of the clock a set of spikes are generated.
     */
    @Override
    public void simulate(IEventDataStream dataStream, IObserver observer) throws IOException
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        double clock = 0;
        int count = 0;
        reset();
        List<Spike> spikeTimes = new ArrayList<Spike>();
        
        computeParameters();
        if (!connect) return;

        while (clock < N && !observer.interrupted())
        {
            spikeTimes.clear();

            // Evaluate input to each neuron's firing rate
            for (int i = 0; i < size; i++)
            {
                net[i].evaluateRate();
            }

            // Simulate each neuron's firing for deltaT
            // and inject inputs into receiver neurons
            // along side generate the data stream
            for (int i = 0; i < size && !observer.interrupted(); i++)
            {
                PoissonSimulator2.Neuron neuron = net[i];
                double lambda = neuron.getRate();
                double[] ftimes = RandomVariableGen.poisson(deltaT, lambda, clock);
                int nfired = 0;
                for (int j = 0; j < ftimes.length; j++)
                {
                    if (ftimes[j] > neuron.getLastSpikeTime() + refractoryPeriod)
                    {
                        neuron.setLastSpikeTime(ftimes[j]);
                        spikeTimes.add(new Spike(i, ftimes[j]));
                        nfired ++;
                    }
                }

                for (int j = 0; j < size; j++)
                {
                    PoissonSimulator2.Neuron rneuron = net[j];
                    rneuron.injectInput(weights[i][j] * nfired, delay[i][j]);
                }
            }

            Collections.sort(spikeTimes);

            for (int i = 0; i < spikeTimes.size() && clock < N && !observer.interrupted(); i++)
            {
                Spike spike = (Spike)spikeTimes.get(i);
                int index = spike.getIndex();
                double t = spike.getSpikeTime();
                String event = eventTypes.getName(index);
                if (event == null || event.length() < 1) {
                    System.err.println("no name found for event type "+ index);
                    continue;
                }
                Double start = new Double(t);
                Double end = null;
                dataStream.add(event, start, end);
                count ++;
            }

            clock += deltaT;
            if (count % 50 == 0)
            {
                observer.update((int)clock);
            }
        }
    }
    
    public void reset()
    {
        for (int i = 0; i < size; i++)
        {
            this.net[i].reset();
        }
    }
    
    public void interconnect() throws Exception
    {
        connect = true;
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                weights[i][j] = 0.0;
		P_matrix[i][j] = 1 - Math.exp(-lambda_normal * deltaT);
            }
        }
        switch(interconnectType)
        {
            case UNIFORM:
                for (int i = 0; i < size; i++)
                {
                    int n = (int)(2.0 * interConnectFactor * Math.random() * size);
                    if (n > size) n = size;
                    int[] mem = new int[n];
                    for (int j = 0; j < n; j++)
                    {
                        boolean flag = true;
                        double p = (randomHigh - randomLow) * Math.random() + randomLow;
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
			P_matrix[i][index] = p;
                    }
                }
                break;
            case FULLY_CONNECTED:
                for (int i = 0; i < size; i++)
                {
                    for (int j = 0; j < size; j++)
                    {
                        P_matrix[i][j] = (randomHigh - randomLow) * Math.random() + randomLow;
                    }
                }
                break;
            case BINOMIAL:
                for (int i = 0; i < size; i++)
                {
                    for (int j = 0; j < size; j++)
                    {
                        double prob = Math.random();
                        if (prob < interConnectFactor) 
                            P_matrix[i][j] = (randomHigh - randomLow) * Math.random() + randomLow;
                    }
                }
                break;
        }
        customEpisodeInterConnect(episodesList);
	
	//Convert probabilities to matrix weights here
	for (int i = 0; i < size; i++)
	{
	    for(int j = 0; j < size; j++)
	    {
		double p = P_matrix[i][j];
		double lambda = getLambdaFromProbability(p, deltaT);
		if (lambda > lambda_max * beta) lambda = lambda_max * beta;
		double l_norm = lambda_normal;
		if (net[j].is_eps) l_norm = lambda_eps_normal;
		double w = getWeightFromRatio(l_norm, lambda_max, lambda/lambda_max, delta_lambda);
		weights[i][j] = w;
	    }
	}
    }
    
    
    @Override
    protected void customEpisodeInterConnect(List<IEpisode> episodes)
    {
        computeParameters();
        for (int i = 0; i < net.length; i++)
        {
            net[i].is_eps = false;
        }
        
        if (episodes == null) return;
        
        this.episodeStartTypes = new int[episodes.size()];
        for (int i = 0; i < episodes.size(); i++)
        {
            IEpisode o = episodes.get(i);
            if (o instanceof ComboEpisode)
            {
                ComboEpisode ce = (ComboEpisode)o;
                int[][] event = ce.getIndices();

                // Interconnet within groups
                for(int j = 0; j < event.length; j++)
                {
		    if (event[j] != null)
		    {
			for(int k = 0; k < event[j].length; k++)
			{
			    if (j != 0)
			    {
				net[event[j][k]].is_eps = true;
			    }
			    if (groupInterconnect)
			    {
				for(int l = k + 1; l < event[j].length; l++)
				{
				    P_matrix[event[j][k]][event[j][l]] = prob_fire;
				    P_matrix[event[j][l]][event[j][k]] = prob_fire;
				}
			    }
			}
		    }
		}

                // Interconnect between groups
                for(int j = 0; j < event.length - 1; j++)
                {
                    int j_1 = (j + 1) % event.length;

                    // Forward connections
                    //double fweight = EPS_WEIGHT / event[j].length;
                    for(int k = 0; k < event[j].length; k++)
                    {
                        for(int l = 0; l < event[j_1].length; l++)
                        {
			    ComboEpisode.Params[] params = ce.getParams();
			    if (params[j] != null)
			    {
				double strength = params[j].getStrength();
				if (!(strength > 0.0)) strength = prob_fire;
				P_matrix[event[j][k]][event[j_1][l]] = strength;
				int delayIndex = (int)(params[j].getDelay()/deltaT);
				if (delayIndex != 0) delay[event[j][k]][event[j_1][l]] = delayIndex;
			    }
			    else
			    {
				P_matrix[event[j][k]][event[j_1][l]] = prob_fire;
			    }
                            if (groupInterconnect)
                            {
                                P_matrix[event[j_1][l]][event[j][k]] = 0.0;
                            }
                        }
                    }
                }
            }
            else
            {
                IEpisode e = o;
                if (e.size() > 0) this.episodeStartTypes[i] = e.getFirstEvent();
                for (int j = 0; j < e.size() - 1; j++)
                {
                    int src = e.getEvent(j);
                    int dst = e.getEvent(j + 1);
                    P_matrix[src][dst] = prob_fire;
                    P_matrix[dst][src] = 0.0;
                    net[dst].is_eps = true;
                }
                if (loopback)
                {
                    int src = e.getLastEvent();
                    int dst = e.getFirstEvent();
                    P_matrix[src][dst] = prob_fire;
                    P_matrix[dst][src] = 0.0;
                }
            }
        }
    }
    
    class Neuron implements Serializable
    {
        private int index;
        private double lastSpikeTime;
        private double rate;
        private ArrayList<Input> list = new ArrayList<Input>();
        private boolean is_eps;
        //private int histIndex = -1;
        
        public Neuron(int index)
        {
            this.index = index;
            this.rate = lambda_normal;
            this.is_eps = false;
            this.lastSpikeTime = -10.0 * refractoryPeriod;
        }
        
        public void reset()
        {
            this.rate = lambda_normal;
            this.lastSpikeTime = -10.0 * refractoryPeriod;
            list.clear();
            int lsize = history;
            for(int i = 0; i < size; i++)
            {
                if (delay[i][this.index] > lsize) lsize = delay[i][this.index];
            }
            for (int i = 0; i < lsize; i++) 
                list.add(new Input());
        }
        
        public void shift()
        {
            int last = list.size() - 1;
            for (int i = 0; i < last; i++)
            {
                list.set(i, list.get(i + 1));
            }
            list.set(last, new Input());
        }
        
        public int getIndex()
        {
            return index;
        }
        
        public void injectInput(double in, int delay)
        {
            if (delay == 0) delay = history;
            Input inp = list.get(delay - 1);
            double val = inp.getValue() + in;
            inp.setValue(val);
        }
        
        public void evaluateRate()
        {
            double input = list.get(0).getValue();
            double d = 0.0;
            if (is_eps)
            {
                d = lambda_eps_normal;
            }
            else
            {
                d = lambda_normal;
            }
            rate = delta_lambda * input + d;
	    rate = (rate < 0.0)? 0.0 : rate;
	    rate = (rate > lambda_max) ? lambda_max : rate;

            shift();
        }
        
        public double getRate()
        {
            return rate;
        }
        
        public double getLastSpikeTime()
        {
            return lastSpikeTime;
        }
        
        public void setLastSpikeTime(double lastSpikeTime)
        {
            this.lastSpikeTime = lastSpikeTime;
        }
    }
    
    /**
     * The spike is of a particular event type, indicated by the index,
     * occuring at a specific time.
     */
    private class Spike implements Comparable
    {
        private int index;
        private double spikeTime;
        
        public Spike(int index, double spikeTime)
        {
            this.index = index;
            this.spikeTime = spikeTime;
        }
        
        public int getIndex()
        {
            return index;
        }
        
        public double getSpikeTime()
        {
            return spikeTime;
        }
        
        public int compareTo(Object o)
        {
            Spike s = (Spike)o;
            return this.spikeTime < s.spikeTime? -1 : 1;
        }
    }
    
    class Input implements Serializable
    {
        private double value = 0;
        
        public double getValue()
        {
            return value;
        }
        
        public void setValue(double value)
        {
            this.value = value;
        }
    }
    
    public void setWeight(int row, int col, double value)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
        {
            weights[row][col] = value;
            net[col].is_eps = true;
	    double l_normal = lambda_normal;
	    if (net[col].is_eps) l_normal = lambda_eps_normal;
	    P_matrix[row][col] = getEstrongFromWeight(weights[row][col], l_normal, lambda_max, deltaT, delta_lambda);
            System.out.println("net[" + col + "].is_eps = " + net[col].is_eps);
        }
    }
    
    public void setProbability(int row, int col, double p)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
        {
            P_matrix[row][col] = p;
            net[col].is_eps = true;
	    double lambda = getLambdaFromProbability(p, deltaT);
	    if (lambda > lambda_max * beta) lambda = lambda_max * beta;
	    double l_norm = lambda_normal;
	    if (net[col].is_eps) l_norm = lambda_eps_normal;
	    double w = getWeightFromRatio(l_norm, lambda_max, lambda/lambda_max, delta_lambda);
	    weights[row][col] = w;
        }
    }

    public void setHistoryIndex(int neuron_i, int neuron_j, int tdelay)
    {
        if (size > 0 && neuron_i < size && neuron_i > -1 &&
                neuron_j < size && neuron_j > -1 && tdelay > 0)
        {
            delay[neuron_i][neuron_j] = tdelay;
            System.out.println("delay[" + neuron_i + "][" + neuron_j + "]="
                    + delay[neuron_i][neuron_j]);
        }
    }
    
    public void saveModelParameters(PrintWriter out)
    {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < net.length; i++)
        {
            if (i != 0) buffer.append(":");
            if (net[i].is_eps)
                buffer.append("1");
            else
                buffer.append("0");
        }
        out.println(buffer.toString());
    }
    
    public void loadModelParameters(BufferedReader in)
    {
        try
        {
            String line = in.readLine();
            if (line != null)
            {
                String[] eps = line.split(":");
                if (eps.length != net.length)
                {
                    System.out.println("Mismatch in network and loaded parameters");
                }
                else
                {
                    for (int i = 0; i < net.length; i++)
                    {
                        net[i].is_eps = (eps[i].equals("1"));
                        //System.out.println("net[" + i + "].is_eps = " + net[i].is_eps);
                    }
                }
            }
        }
        catch(IOException ioe)
        {}
        
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
    
    public String getTrefractory()
    {
        return trefractory;
    }
    
    public void setTrefractory(String trefractory)
    {
        this.trefractory = trefractory;
    }
    
    public String getEstrong()
    {
        return estrong;
    }
    
    public void setEstrong(String estrong)
    {
        this.estrong = estrong;
    }
    
    public String getFrest()
    {
        return frest;
    }
    
    public void setFrest(String frest)
    {
        this.frest = frest;
    }
    
    public String getSlope()
    {
        return slope;
    }
    
    public void setSlope(String slope)
    {
        this.slope = slope;
    }
    
    public Integer getSynapticDelay()
    {
        return synapticDelay;
    }
    
    public void setSynapticDelay(Integer synapticDelay)
    {
        this.synapticDelay = synapticDelay;
    }
}
