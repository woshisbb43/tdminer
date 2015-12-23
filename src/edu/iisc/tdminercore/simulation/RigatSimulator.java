package edu.iisc.tdminercore.simulation;

import edu.iisc.tdminercore.data.ComboEpisode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.util.IObserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class: RigatSimulator
 *
 * @author debprakash
 */
public class RigatSimulator extends NetworkBasedSimulator
{
    private double[][] beta;
    private double[] eta;
    private int[] tou_index;
    private double deltaT;
    
    private double eta_base;
    private double beta_strong;
    private double beta_self;

    private String str_eta_base;
    private String str_beta_strong;
    private String str_beta_self;
    ArrayList<F> flist = new ArrayList<F>();

    @Override
    public void setEventFactor(EventFactor eventTypes)
    {
        super.setEventFactor(eventTypes);
	this.beta = this.weights;
        this.eta = new double[size];
        this.tou_index = new int[size];
    }

    public void computeParameters()
    {
	deltaT = Double.parseDouble(tupdate);
	N = Double.parseDouble(ttotal);
        startTime = Double.parseDouble(tstart);
        eta_base = Double.parseDouble(str_eta_base);
        beta_strong = Double.parseDouble(str_beta_strong);
        beta_self = Double.parseDouble(str_beta_self);
        for (int i = 0; i < size; i++)
        {
            eta[i] = eta_base;
        }
    }

    @Override
    public void setDefaultValues()
    {
        super.setDefaultValues();
        str_eta_base = "-3.892";
        str_beta_strong = "16.546";
        str_beta_self = "-8.2";
        
        strLow = "-1.916";
        strHigh = "1.125";
        
        resetModel();
    }
        
    public void resetModel()
    {
        for (int i = 0; i < size; i++)
        {
            eta[i] = eta_base;
            for (int j = 0; j < size; j++)
            {
                if (i == j) beta[i][j] = beta_self;
                else beta[i][j] = 0.0;
            }
        }
    }
    
    @Override
    public void simulate(IEventDataStream dataStream, IObserver observer) throws IOException
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        
        double t = startTime;
        for (int i = 0; i < size; i++) 
        {
            String event = eventTypes.getName(i);
            dataStream.add(event, t);
            this.tou_index[i] = assignIndex(dataStream);
        }
        t+= deltaT;
        int count = 0;
        double maxTime = N + startTime;
        while (t < maxTime && !observer.interrupted())
        {
            for(F f : getFlist())
            {
                beta[f.getSrc()][f.getDest()] = f.getValue(t);
            }
            
            int lastIndex = dataStream.getSize() - 1;
            // Update voltage equation and probability equations
            for(int i = 0; i < size; i++)
            {
                //System.out.print("Compute pi for " + eventTypes.getName(i) + " at t = " + nf.format(t) + ":");
                double volt = eta[i];
                //System.out.print("[" + (tou_index[i]) + " to " + lastIndex + "]");
                for (int idx = tou_index[i]; idx <= lastIndex; idx++)
                {
                    IEvent event = dataStream.get(idx);
                    int j = event.getEventType(); // j -> i
                    double w = event.getStartTime();
                    int diff = (int)((t - w)/deltaT);
                    volt += beta[j][i] * Math.exp(-(diff));
                    //System.out.print("(" + eventTypes.getName(j) + "," 
                    //        + nf.format(w) + "," + nf.format(beta[j][i]) + ")");

                }
                //System.out.print(volt + "\t");
                double pi = 1.0 / (1.0 + Math.exp(-volt));
                
                if (Math.random() < pi) // generate spike with probability pi
                {
                    String event = eventTypes.getName(i);
                    dataStream.add(event, t);
                    this.tou_index[i] = assignIndex(dataStream);
                    count++;
                    //System.out.println(": pi = " + pi + " SPIKE " + this.tou_index[i]);
                }
                else
                {
                    //System.out.println(": pi = " + pi + " -");
                }
            }
            //System.out.println();
            t+= deltaT;
            if (count % 50 == 0)
            {
                observer.update((int)(t - startTime));
            }
        }
    }
    
    private int assignIndex(IEventDataStream dataStream)
    {
        int index = dataStream.getSize() - 1;
        int currTime = (int)(dataStream.get(index).getStartTime()/deltaT);
        while(index > 0 && (currTime == (int)(dataStream.get(index - 1).getStartTime()/deltaT)))
            index --;
        return index;
    }
    
    @Override
    public void interconnect() throws Exception
    {
        super.interconnect();
        for (int i = 0; i < size; i++)
        {
            beta[i][i] = beta_self;
        }
    }

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
				//if (delayIndex != 0) delay[event[j][k]][event[j_1][l]] = delayIndex;
			    }
			    else
			    {
                                weights[event[j][k]][event[j_1][l]] = beta_strong;
                                //delay[event[j][k]][event[j_1][l]] = 5;
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
                    weights[src][dst] = beta_strong;
                    weights[dst][src] = 0.0;
                }
                if (loopback)
                {
                    int src = e.getLastEvent();
                    int dst = e.getFirstEvent();
                    weights[src][dst] = beta_strong;
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
        if (eventTypes == null)
        {
            throw new RuntimeException("Event types are not defined");
        }
        else
        {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
            nf.setMinimumFractionDigits(3);
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
            out.println("T_start = " + tstart);
            
            out.println("Eta = " + str_eta_base);
            out.println("Beta_strong = " + str_beta_strong);
            out.println("Beta_self = " + str_beta_self);
            
            out.println("Interconnection Type = " + interconnectType);
            out.println("Random connection strengths = [" + strLow + " - " + strHigh + "]");
            
            out.println();
            out.println("Beta matrix: [row]->[col]");
            for (int i = 0; i < size; i++)
            {
                for (int j = 0; j < size; j++)
                {
                    if (j != 0) out.print("\t");
                    out.print(nf.format(weights[i][j]));
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

    @Override
    public void loadConnectionMatrix(BufferedReader in) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getStr_beta_self()
    {
        return str_beta_self;
    }

    public void setStr_beta_self(String str_beta_self)
    {
        this.str_beta_self = str_beta_self;
    }

    public String getStr_beta_strong()
    {
        return str_beta_strong;
    }

    public void setStr_beta_strong(String str_beta_strong)
    {
        this.str_beta_strong = str_beta_strong;
    }

    public String getStr_eta_base()
    {
        return str_eta_base;
    }

    public void setStr_eta_base(String str_eta_base)
    {
        this.str_eta_base = str_eta_base;
    }

    @Override
    public ArrayList<F> getFlist()
    {
        return flist;
    }
}
