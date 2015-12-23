package edu.iisc.tdminercore.simulation;

import edu.iisc.tdminercore.data.ComboEpisode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.util.IObserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class: IzhikevichModel
 *
 * @author debprakash
 */
public class IzhikevichModel extends NetworkBasedSimulator implements Serializable
{
    public static final double deltaT = 0.001;
    private int delay[][];
    private double w_strong;
    protected Integer synapticDelayLow = new Integer(1);
    protected Integer synapticDelayHigh = new Integer(30);
    IzhikevichModel.Neuron[] net;
    private ArrayList<F> flist;
    
    private String str_w_strong;
    
    double a, b, c, d;
    private String str_a;
    private String str_b;
    private String str_c;
    private String str_d;
    private String str_I_max;
    private String str_I_min;
    double I_const_min;
    double I_const_max;
    double randStim;
    private String strRandStim;
    double prand;

    /** Creates a new instance of SimulatorModelOne */
    public IzhikevichModel()
    {
        flist = new ArrayList<F>();
    }
    
    @Override
    public void setDefaultValues()
    {
        super.setDefaultValues();
        synapticDelayLow = new Integer(1);
        synapticDelayHigh = new Integer(30);
        str_w_strong = "20.0";
        str_a = "0.02"; str_b = "0.2"; str_c = "-65"; str_d="8";
        str_I_min = "0"; str_I_max = "5";
        strLow = "-5";
        strHigh = "5";
        strRandStim = "20";
    }
    
    public void computeParameters()
    {
	N = Double.parseDouble(ttotal);
        w_strong = Double.parseDouble(str_w_strong);
        if (synapticDelayLow <= 0 || synapticDelayHigh <= 0) 
            throw new NumberFormatException("Synaptic delay must >= 1 x T_update");
        startTime = Double.parseDouble(tstart);
        a = Double.parseDouble(str_a); 
        b = Double.parseDouble(str_b); 
        c = Double.parseDouble(str_c); 
        d = Double.parseDouble(str_d);
        I_const_min = Double.parseDouble(str_I_min);
        I_const_max = Double.parseDouble(str_I_max);
        randStim = Double.parseDouble(strRandStim);
        prand = (1 - Math.exp(-randStim * deltaT));
        System.out.println("prand = " + prand);
        for (int i = 0; i < size; i++)
        {
            net[i].setParams(a, b, c, d, I_const_min, I_const_max);
        }
    }

    @Override
    public ArrayList<F> getFlist()
    {
        return flist;
    }
    
    public void addExternalInput(int index, String file)
    {
        this.net[index].setExternalSource(file);
    }

    public int getDelay(int row, int col)
    {
        if (size > 0 && row < size && col < size && row > -1 && col > -1)
            return delay[row][col];
        else
            return 0;
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
        this.delay = new int[size][size];
        this.net = new IzhikevichModel.Neuron[size];
        for (int i = 0; i < size; i++) net[i] = new IzhikevichModel.Neuron(i);
        
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                weights[i][j] = 0.0;
                delay[i][j] = (int)((synapticDelayHigh - synapticDelayLow + 1) * Math.random()) 
                        + synapticDelayLow;
            }
        }
    }
    
    @Override
    public void interconnect() throws Exception
    {
        for (int i = 0; i < size; i++)
        {
            weights[i][i] = 0.0;
            for (int j = 0; j < size; j++)
            {
                delay[i][j] = (int)((synapticDelayHigh - synapticDelayLow + 1) * Math.random()) 
                        + synapticDelayLow;
            }
        }
        super.interconnect();
    }
    

    public void reset()
    {
        for (int i = 0; i < size; i++)
        {
            this.net[i].reset();
        }
    }
    
    @Override
    protected void customEpisodeInterConnect(List<IEpisode> episodes)
    {
        computeParameters();
        if (episodes == null) return;
        for (int i = 0; i < episodes.size(); i++)
        {
            IEpisode o = episodes.get(i);
            if (o instanceof ComboEpisode)
            {
                ComboEpisode ce = (ComboEpisode)o;
                int[][] event = ce.getIndices();

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
                                weights[event[j][k]][event[j_1][l]] = strength;
				int delayIndex = (int)(params[j].getDelay()/deltaT);
				if (delayIndex != 0) delay[event[j][k]][event[j_1][l]] = delayIndex;
			    }
			    else
			    {
                                weights[event[j][k]][event[j_1][l]] = w_strong;
                                delay[event[j][k]][event[j_1][l]] = 5;
			    }
                        }
                    }
                }
            }
            else
            {
                IEpisode e = o;
                for (int j = 0; j < e.size() - 1; j++)
                {
                    int src = e.getEvent(j);
                    int dst = e.getEvent(j + 1);
                    weights[src][dst] = w_strong;
                    weights[dst][src] = 0.0;
                }
                if (loopback)
                {
                    int src = e.getLastEvent();
                    int dst = e.getFirstEvent();
                    weights[src][dst] = w_strong;
                    weights[dst][src] = 0.0;
                }
            }
        }
    }

    @Override
    public double getProbability(int row, int col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveConnectionMatrix(PrintWriter out) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadConnectionMatrix(BufferedReader in) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
                setWeight(f.getSrc(), f.getDest(), f.getValue(clock));
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
                            double w =  estim.getProb(i);
                            IzhikevichModel.Neuron rneuron = net[i];
                            rneuron.injectInput(w * nfired, 1);
                        }
                    }
                }
            }
            
            spikeTimes.clear();

            // Evaluate input to each neuron's firing rate
            for (int i = 0; i < size; i++)
            {
                if (Math.random() < prand)
                    net[i].injectInput(w_strong, 1);
                net[i].update();
            }

            // Simulate each neuron's firing for deltaT
            // and inject inputs into receiver neurons
            // along side generate the data stream
            for (int i = 0; i < size && !observer.interrupted(); i++)
            {
                IzhikevichModel.Neuron neuron = net[i];
                int nfired = 0;
                if (!neuron.isExternallyDriven()) //INTERNAL NEURON
                {
                    if (neuron.v >= 30.0)
                    {
                        spikeTimes.add(new Spike(i, clock));
                        nfired ++;
                        neuron.v = -65.0;
                        neuron.u += neuron.d;
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
                // Feed input to receiver neurons
                for (int j = 0; j < size; j++)
                {
                    IzhikevichModel.Neuron rneuron = net[j];
                    rneuron.injectInput(weights[index][j], delay[index][j]);
                }
            }
            
            clock += deltaT;
            if (count > 50)
            {
                observer.update((int)clock);
                count = 0;
            }
        }
    }

    public String getStr_w_strong()
    {
        return str_w_strong;
    }

    public void setStr_w_strong(String str_w_strong)
    {
        this.str_w_strong = str_w_strong;
    }

    public String getStr_a()
    {
        return str_a;
    }

    public void setStr_a(String str_a)
    {
        this.str_a = str_a;
    }

    public String getStr_b()
    {
        return str_b;
    }

    public void setStr_b(String str_b)
    {
        this.str_b = str_b;
    }

    public String getStr_c()
    {
        return str_c;
    }

    public void setStr_c(String str_c)
    {
        this.str_c = str_c;
    }

    public String getStr_d()
    {
        return str_d;
    }

    public void setStr_d(String str_d)
    {
        this.str_d = str_d;
    }

    public String getStr_I_max()
    {
        return str_I_max;
    }

    public void setStr_I_max(String str_I_max)
    {
        this.str_I_max = str_I_max;
    }

    public String getStr_I_min()
    {
        return str_I_min;
    }

    public void setStr_I_min(String str_I_min)
    {
        this.str_I_min = str_I_min;
    }

    public String getStrRandStim()
    {
        return strRandStim;
    }

    public void setStrRandStim(String strRandStim)
    {
        this.strRandStim = strRandStim;
    }
    
    private class Neuron implements Serializable
    {
        private int index;
        private ArrayList<Input> list = new ArrayList<Input>();
        private boolean externalFlag;
        private String fileName;
        private transient ExternalSpikeSource es;

        //State
        double v = 0.0;
        double u = 0.0;
        //Parameters
        double a;
        double b;
        double c;
        double d;
        double I_const_min;
        double I_const_max;
        public Neuron(int index)
        {
            this.index = index;
        }
        
        public void setParams(double a, double b, double c, double d, double I_const_min, double I_const_max)
        {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.I_const_min = I_const_min;
            this.I_const_max = I_const_max;
            v = -65.0;
            u = b * v;
        }
        
        public void update()
        {
            if (externalFlag) return;
            double I = I_const_min + (I_const_max - I_const_min) * Math.random();
            if (list != null && list.size() > 0)
            {
                I += list.get(0).getValue();
            }
            v += 0.5 * (0.04 * v * v + 5.0 * v + 140 - u + I);
            v += 0.5 * (0.04 * v * v + 5.0 * v + 140 - u + I);
            u += a * (b * v - u);
            if (list != null && list.size() > 0) shift();
        }
        
        public void reset()
        {
            v = -65.0;
            u = b * v;
            if (!externalFlag)
            {
                list.clear();
                int lsize = delay[0][this.index];//get max delay
                for(int i = 1; i < size; i++)
                {
                    if (delay[i][this.index] > lsize) lsize = delay[i][this.index];
                }
                for (int i = 0; i <= lsize; i++) 
                    list.add(new Input());
            }
            else
            {
                es = new ExternalSpikeSource(this.fileName);
            }
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

        private void shift()
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
    
    private class Input implements Serializable
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
