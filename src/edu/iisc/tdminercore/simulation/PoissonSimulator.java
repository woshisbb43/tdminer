/*
 * PoissonSimulator.java
 *
 * Created on April 12, 2006, 11:58 AM
 *
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
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Debprakash Patnaik
 */
public class PoissonSimulator extends NetworkBasedSimulator implements Serializable
{
    public double deltaT;
    public double lambda_normal;
    public HashMap<Integer,Double> base_rates = new HashMap<Integer, Double>();

    public double prob_fire;
    
    public double refractoryPeriod;
    public boolean groupInterconnect;
    public double delta_lambda;
    public double lambda_eps_factor = 1.5;
    public boolean isNormalized = false;
    
    PoissonSimulator.Neuron[] net;
    protected int[] episodeStartTypes;
    
    private double beta;
    private double lambda_eps_normal;
    private double lambda_max;
    //private double EPS_WEIGHT;
    
    private int delay[][];
    private double P_matrix[][];
    
    protected String estrong = "0.80";
    protected String frest = "20.0";
    protected String slope = "1.0";
    protected Integer synapticDelayLow = new Integer(5);
    protected Integer synapticDelayHigh = new Integer(5);
    protected double P_max = 0.99;
    protected String trefractory = "0.001";
    
    private ArrayList<F> flist;

    public ArrayList<F> getFlist()
    {
        return flist;
    }
    
    
    
    /** Creates a new instance of SimulatorModelOne */
    public PoissonSimulator()
    {
        flist = new ArrayList<F>();
    }
    
    public void addExternalInput(int index, String file)
    {
        this.net[index].setExternalSource(file);
    }
    
    @Override
    public void setDefaultValues()
    {
        super.setDefaultValues();
        trefractory = "0.001";
        estrong = "0.80";
        frest = "20.0";
        slope = "1.0";
        synapticDelayLow = new Integer(5);
        synapticDelayHigh = new Integer(5);
        groupInterconnect = false;
        lambda_eps_factor = 1.5;
	
	//strProbLow = "0.01";
	//strProbHigh = "0.03";
	
        resetModel();
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
    
    @Override
    public void setEventFactor(EventFactor eventTypes)
    {
        super.setEventFactor(eventTypes);
	this.P_matrix = new double[size][size];
        this.delay = new int[size][size];
        this.net = new PoissonSimulator.Neuron[size];
        for (int i = 0; i < size; i++) net[i] = new PoissonSimulator.Neuron(i);
        
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                weights[i][j] = 0.0;
		P_matrix[i][j] = 1 - Math.exp(-lambda_normal(j) * deltaT);
                delay[i][j] = (int)((synapticDelayHigh - synapticDelayLow + 1) * Math.random()) 
                        + synapticDelayLow;
            }
        }
        
    }
    
    private static double getEstrongFromWeight(double weight, double lambda_normal, double lambda_max, double deltaT, double delta_lambda)
    {
	double d = Math.log(lambda_max/lambda_normal - 1.0);
	double lambda = (lambda_max / (1 + Math.exp(-delta_lambda * weight + d)));
	double E_strong = 1.0 - Math.exp(- lambda * deltaT);
	return E_strong;
    }
    
    public static double getWeightFromRatio(double lambda_eps_normal, double lambda_max, double beta, double delta_lambda)
    {
        double w = Math.log((beta / (1 - beta)) * (lambda_max/lambda_eps_normal - 1.0)) / delta_lambda;
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
        prob_fire = Double.parseDouble(estrong);
	deltaT = Double.parseDouble(tupdate);
	N = Double.parseDouble(ttotal);
	lambda_normal = Double.parseDouble(frest);
        if (synapticDelayLow <= 0 || synapticDelayHigh <= 0) throw new NumberFormatException("Synaptic delay must >= 1 x T_update");
        refractoryPeriod = Double.parseDouble(trefractory);
        delta_lambda = Double.parseDouble(slope);
        startTime = Double.parseDouble(tstart);
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
        if (net != null)
        {
            for(Neuron n : net)
            {
                n.externalFlag = false;
            }
        }
    }
    
    /**
     * Simulate advances through a time using a 'clock'.
     * The clock starts at zero and advances to N.
     * At each tick of the clock a set of spikes are generated.
     */
    public void simulate(IEventDataStream dataStream, IObserver observer) throws IOException
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);
        double clock = startTime;
        double maxTime = N + startTime;
        int count = 0;
        if (net != null)
        {
            for(Neuron n : net) n.externalFlag = false;
            for (ExternalInput es : externalInputList)
            {
                net[es.getNeuron()].setExternalSource(es.getFile());
            }
        }
        for(ExternalStimulator estim : externalStimulatorList)
        {
            estim.reset();
        }
        reset();
        List<Spike> spikeTimes = new ArrayList<Spike>();
        
        computeParameters();

        while (clock < maxTime && !observer.interrupted())
        {
            for(F f : getFlist())
            {
                setProbability(f.getSrc(), f.getDest(), f.getValue(clock));
            }
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
                PoissonSimulator.Neuron neuron = net[i];
                int nfired = 0;
                if (!neuron.isExternallyDriven()) //INTERNAL NEURON
                {
                    double lambda = neuron.getRate();
                    double[] ftimes = RandomVariableGen.poisson(deltaT, lambda, clock);
                    for (int j = 0; j < ftimes.length; j++)
                    {
                        if (ftimes[j] > neuron.getLastSpikeTime() + refractoryPeriod)
                        {
                            neuron.setLastSpikeTime(ftimes[j]);
                            spikeTimes.add(new Spike(i, ftimes[j]));
                            nfired ++;
                        }
                    }
                }
                else
                {
                    // Processing external input
                    List<Double> ftimes = neuron.getExternalSource().getSpikes(deltaT, clock);
                    for (int j = 0; j < ftimes.size(); j++)
                    {
                        spikeTimes.add(new Spike(i, ftimes.get(i)));
                        nfired ++;
                    }
                }
                // Feed input to receiver neurons
                for (int j = 0; j < size; j++)
                {
                    PoissonSimulator.Neuron rneuron = net[j];
                    rneuron.injectInput(weights[i][j] * nfired, delay[i][j]);
                }
            }

            Collections.sort(spikeTimes);

            for (int i = 0; i < spikeTimes.size() && clock < maxTime && !observer.interrupted(); i++)
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
            
            // Externally driven source feeds input to receiver neurons
            for(ExternalStimulator estim : externalStimulatorList)
            {
                List<Double> ftimes = estim.getExternalSource().getSpikes(deltaT, clock);
                int nfired = ftimes.size();
                if (nfired > 0)
                {
                    for (int i = 0; i < size; i++)
                    {
                        if (estim.isConnected(i))
                        {
                            double w =  computeWeightfromProb(estim.getProb(i),i);
                            PoissonSimulator.Neuron rneuron = net[i];
                            rneuron.injectInput(w * nfired, 1);
                        }
                    }
                }
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
    
    @Override
    public void interconnect() throws Exception
    {
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                weights[i][j] = 0.0;
		P_matrix[i][j] = 1 - Math.exp(-lambda_normal(j) * deltaT);
                delay[i][j] = (int)((synapticDelayHigh - synapticDelayLow + 1) * Math.random()) 
                        + synapticDelayLow;
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
                        weights[i][index] = computeWeight(index);
			P_matrix[i][index] = computeProbfromWeight(weights[i][index], index);
                    }
                }
                break;
            case FULLY_CONNECTED:
                for (int i = 0; i < size; i++)
                {
                    for (int j = 0; j < size; j++)
                    {
                        weights[i][j] = computeWeight(j);
                        P_matrix[i][j] = computeProbfromWeight(weights[i][j], j);
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
                        {
                            weights[i][j] = computeWeight(j);
                            P_matrix[i][j] = computeProbfromWeight(weights[i][j],j);
                        }
                    }
                }
                break;
        }
        customEpisodeInterConnect(episodesList);
    }
    
    private double computeWeight(int target)
    {
        double w_low = computeWeightfromProb(randomLow, target);
        double w_high = computeWeightfromProb(randomHigh, target);
        double w = (w_high - w_low) * Math.random() + w_low;
        return w;
    }
    
    private double computeWeightfromProb(double p, int target) {
        if (p < 0.000001) p = 0.000001;
        double lambda = getLambdaFromProbability(p, deltaT);
        if (lambda > lambda_max * beta) {
            lambda = lambda_max * beta;
        }
        double l_norm = lambda_normal(target);
        double w = getWeightFromRatio(l_norm, lambda_max, lambda / lambda_max, delta_lambda);
        return w;
    }
    
    private double computeProbfromWeight(double w, int target) {
        double d = Math.log(lambda_max/lambda_normal(target) - 1.0);
        double lambda = (lambda_max / (1 + Math.exp(-delta_lambda * w + d)));
        double p = 1 - Math.exp(-lambda * deltaT);
        return p;
    }    
    
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
				    weights[event[j][k]][event[j][l]] = computeWeightfromProb(prob_fire, event[j][l]);
				    weights[event[j][l]][event[j][k]] = computeWeightfromProb(prob_fire, event[j][k]);                                    
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
                    //double fweight = EPS_WEIGHT / event[i].length;
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
                                weights[event[j][k]][event[j_1][l]] = computeWeightfromProb(strength, event[j_1][l]);
				int delayIndex = (int)(params[j].getDelay()/deltaT);
				if (delayIndex != 0) delay[event[j][k]][event[j_1][l]] = delayIndex;
                                else delay[event[j][k]][event[j_1][l]] = 5;
			    }
			    else
			    {
				P_matrix[event[j][k]][event[j_1][l]] = prob_fire;
                                weights[event[j][k]][event[j_1][l]] = computeWeightfromProb(prob_fire, event[j_1][l]);
                                delay[event[j][k]][event[j_1][l]] = 5;
			    }
                            if (groupInterconnect)
                            {
                                P_matrix[event[j_1][l]][event[j][k]] = computeProbfromWeight(0.0, event[j][k]);
                                weights[event[j_1][l]][event[j][k]] = 0.0;
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

    public double getProbability(int row, int col)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
            return P_matrix[row][col];
        else
            return 0.0;
    }

    private double lambda_normal(int j)
    {
        double ret = lambda_normal;
        if (base_rates.containsKey(j))
        {
            ret = base_rates.get(j);
        }
        return ret;
    }
    
    public HashMap<Integer, Double> getBaseRates()
    {
        return base_rates;
    }

    class Neuron implements Serializable
    {
        private int index;
        private double lastSpikeTime;
        private double rate;
        private ArrayList<Input> list = new ArrayList<Input>();
        private boolean is_eps;
        private boolean externalFlag;
        private String fileName;
        private transient ExternalSpikeSource es;
        public Neuron(int index)
        {
            this.index = index;
            this.rate = lambda_normal(index);
            this.is_eps = false;
            this.lastSpikeTime = -10.0 * refractoryPeriod;
        }
        
        public void reset()
        {
            if (!externalFlag)
            {
                this.rate = lambda_normal(index);
                this.lastSpikeTime = -10.0 * refractoryPeriod;
                list.clear();
                int lsize = delay[0][this.index];//get max delay
                for(int i = 1; i < size; i++)
                {
                    if (delay[i][this.index] > lsize) lsize = delay[i][this.index];
                }
                for (int i = 0; i < lsize; i++) 
                    list.add(new Input());
            }
            else
            {
                es = new ExternalSpikeSource(this.fileName);
            }
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
            if (!externalFlag && list != null && list.size() > 0)
            {
                Input inp = list.get(delay - 1);
                double val = inp.getValue() + in;
                inp.setValue(val);
            }
        }
        
        public void evaluateRate()
        {
            if (externalFlag) return;
            double input = 0.0;
            if (list != null && list.size() > 0)
            {
                input = list.get(0).getValue();
            }
            double d = 0.0;
            if (is_eps)
            {
                d = Math.log(lambda_max/lambda_eps_normal - 1.0);
            }
            else
            {
                d = Math.log(lambda_max/lambda_normal(index) - 1.0);
            }
            rate = (lambda_max / (1 + Math.exp(-delta_lambda * input + d)));

            if (list != null && list.size() > 0) shift();
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

        public boolean isExternallyDriven()
        {
            return externalFlag;
        }
        public void setExternallyDriven(boolean flag)
        {
            externalFlag = flag;
        }
        
        public void setExternalSource(String f)
        {
            this.fileName = f;
            this.externalFlag = true;
        }
        
        public ExternalSpikeSource getExternalSource()
        {
            return es;
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
    
    @Override
    public void setWeight(int row, int col, double value)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
        {
            weights[row][col] = value;
            net[col].is_eps = true;
	    double l_normal = lambda_normal(col);
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
	    double l_norm = lambda_normal(col);
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
    
    
    public void saveConnectionMatrix(PrintWriter out) throws IOException
    {
        if (eventTypes == null)
        {
            throw new RuntimeException("Event types are not defined");
        }
        else
        {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(4);
            nf.setGroupingUsed(false);
            out.println("Poisson Simulation Model");
            out.print("Event Types = [");
            for (int i = 0; i < size; i++)
            {
                if (i != 0) out.print(" ");
                String eventType = eventTypes.getName(i);
                out.print(eventType);
            }
            out.println("]");
            
            out.println("T_update = " + tupdate);
            out.println("T_total = " + ttotal);
            out.println("F_rest = " + frest);
            out.println("E_strong = " + estrong);
            out.println("T_synapse_low = " + synapticDelayLow);
            out.println("T_synapse_high = " + synapticDelayHigh);
            out.println("T_refractory = " + trefractory);
            out.println("Interconnect = " + groupInterconnect);
            out.println("Slope of sigmoid = " + nf.format(delta_lambda));
            out.println("Interconnection Type = " + interconnectType);
            out.println("Random connection strengths = [" + strLow + " - " + strHigh + "]");
            
            out.println();
            out.println("Probability(e_strong) matrix:");
            for (int i = 0; i < size; i++)
            {
                for (int j = 0; j < size; j++)
                {
                    if (j != 0) out.print(" ");
                    out.print(nf.format(P_matrix[i][j]));
                }
                out.println();
            }
            out.println();
            out.println("Weight matrix:");
            for (int i = 0; i < size; i++)
            {
                for (int j = 0; j < size; j++)
                {
                    if (j != 0) out.print(" ");
                    out.print(nf.format(weights[i][j]));
                }
                out.println();
            }
            out.println();
            out.println("Delay matrix:");
            for (int i = 0; i < size; i++)
            {
                for (int j = 0; j < size; j++)
                {
                    if (j != 0) out.print(" ");
                    int d = delay[i][j];
                    out.print(nf.format(d));
                }
                out.println();
            }
            out.println();
            out.println("Episodes:");
            for (IEpisode e : episodesList)
            {
                out.println(e.toString(eventTypes));
            }
            out.close();
        }
    }
    
    
    public void loadConnectionMatrix(BufferedReader in) throws IOException
    {
        String eventTypesStr = in.readLine();
        String[] types = eventTypesStr.split(",");
        size = types.length;
        //System.out.println("size = " + size);
        EventFactor neweventTypes = new EventFactor(types);
        //System.out.println("eventTypes = " + eventTypes);
        setEventFactor(neweventTypes);
        
        for(int i = 0; i < size; i++)
        {
            String line = in.readLine();
            String[] w = line.split(",");
            if (w.length != size) throw new RuntimeException("Mismatch in weight " +
                    "matrix size and data read");
            for (int j = 0; j < size; j++)
            {
                double weight = Double.parseDouble(w[j]);
                weights[i][j] = weight;
            }
        }
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
    
    public Integer getSynapticDelayLow()
    {
        return synapticDelayLow;
    }
    
    public void setSynapticDelayLow(Integer synapticDelay)
    {
        this.synapticDelayLow = synapticDelay;
    }

    public Integer getSynapticDelayHigh()
    {
        return synapticDelayHigh;
    }
    
    public void setSynapticDelayHigh(Integer synapticDelay)
    {
        this.synapticDelayHigh = synapticDelay;
    }
}
