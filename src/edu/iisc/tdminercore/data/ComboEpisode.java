/*
 * ComboEpisode.java
 *
 * Created on October 25, 2006, 6:43 PM
 *
 */

package edu.iisc.tdminercore.data;

import java.text.NumberFormat;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author Pickloo
 */
public class ComboEpisode extends Episode implements IEpisode 
{
    private int[][] event2d;
    private Params[] params;
    /** 
     * Creates a new instance of ComboEpisode 
     * This constructor is called by the getEpisode method below.
     */
    private ComboEpisode(int[][] event, EventFactor f, Params[] params) {
        super(event.length, f);
        this.event2d = event;
	this.params = params;
    }
    
    public int[][] getIndices() {
        return event2d;
    }

    public Params[] getParams()
    {
	return params;
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
        int[][] event = null;
	Params[] params = null;
        Vector v1 = new Vector();  // Either of Param or Vector<String>
        StringTokenizer tok = new StringTokenizer(eps, " ()", true);
        
        Vector curr = v1;
        Stack s = new Stack();
	System.out.print("Tokens : ");
	int paramCount = 0;
        try {
            while(tok.hasMoreTokens()) {
                String token = tok.nextToken();
		System.out.print(token + " ");
                if (token.equals(" ")) continue;
                if (token.equals("(")) {
                    Vector t = new Vector();
                    curr.add(t);
                    s.push(curr);
                    if (s.size() > 1) 
                        throw new RuntimeException("Nesting not supported");
                    curr = t;
                } else if (token.equals(")")) {
                    curr = (Vector)s.pop();
		} else if (token.startsWith("[") && token.endsWith("]")) {
		    if (s.size() == 1) 
                        throw new RuntimeException("Delay and connection strength not valid within groups : " + token);
		    String inp = token.substring(1, token.length() - 1);
		    int index = inp.indexOf(",");
		    double strength = -1.0;
		    double delay = 0.0;
                    String range = inp;
		    if (index != -1) {
                        range = inp.substring(0, index);
			strength = Double.parseDouble(inp.substring(index + 1));
		    }
                    
                    int rangeIx = range.indexOf('-');
                    if (rangeIx != -1) {
                        double start = Double.parseDouble(range.substring(0, rangeIx).trim());
                        double stop = Double.parseDouble(range.substring(rangeIx+1).trim());
                        delay = (start + stop)/2.0D;
                    } else {
			delay = Double.parseDouble(range);
                    }
                        
		    Params p = new Params(delay, strength);
		    v1.add(p);
		    paramCount ++;
                } else {
                    if (curr == v1) {
                        Vector t = new Vector();
                        t.add(token);
                        curr.add(t);
                    } else {
                        curr.add(token);
                    }
                }
            }
	    System.out.println();
            
            if (!s.empty()) throw new RuntimeException("Stack non-empty after processing");
            
            System.out.println("v = " + v1);
            // Process the lists in v
            int size = v1.size() - paramCount;
            System.out.println("size = " + size);
            event = new int[size][];
	    params = new Params[size - 1];
	    int eIndex = 0;
            for (int i = 0; i < v1.size(); i++) {		
		Object o = v1.get(i);
		if (o instanceof Vector) {
		    Vector l = (Vector)o;
		    int len = l.size();
		    System.out.println("len = " + len);
		    event[eIndex] = new int[len];
		    for (int j = 0; j < len; j++) {
                        Object os = l.get(j);
                        if (!(os instanceof String)) {
                            continue;
                        }
			String e = (String)l.get(j);
                        /*
			int eventType = eventTypes.getId(e);
			if (eventType == -1) {
			    throw new IEpisode.TypeMisMatchException("Event Type mismatch in episode set and available sequence");
			}
                         */
                        int eventType = eventTypes.put(e);
			event[eIndex][j] = eventType;
		    }
		    eIndex++;
		} else if (o instanceof Params) {
		    System.out.println("o : " + o);
		    params[eIndex - 1] = (Params)o;
		}		   
            }
        } catch (RuntimeException re) {
            System.out.println(re.toString());
            re.printStackTrace();
            throw new IEpisode.TypeMisMatchException("Invalid input string");
        }
        System.out.println("returned episode");
        return new ComboEpisode(event, eventTypes, params);
    }
    
    @Override
    public String toString(EventFactor eventTypes) {
        StringBuffer buf = new StringBuffer();
        for (int k = 0; k < size(); k++) {
            if (k != 0) 
	    {
		if (params[k-1] != null) buf.append(params[k-1]);
		else buf.append(" ");		
	    }
            if (event2d[k].length > 1) buf.append("(");
            for (int l = 0; l < event2d[k].length; l++) {
                if (l != 0) buf.append(" ");
                buf.append(eventTypes.getName(event2d[k][l]));
            }
            if (event2d[k].length > 1) buf.append(")");
        }
        return buf.toString();
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int k = 0; k < size(); k++) {
            if (k != 0) 
	    {
		if (params[k-1] != null) buf.append(params[k-1]);
		else buf.append(" ");		
	    }
            if (event2d[k].length > 1) buf.append("(");
            for (int l = 0; l < event2d[k].length; l++) {
                if (l != 0) buf.append(" ");
                buf.append(event2d[k][l]);
            }
            if (event2d[k].length > 1) buf.append(")");
        }
        return buf.toString();
    }
    
    @Override
    public int size() {
        return event2d.length;
    }
    
    @Override
    public int getEvent(int index) { return this.event2d[index][0]; }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof ComboEpisode) {
            ComboEpisode e = (ComboEpisode)o;
            boolean retVal = true;
            if (this.event2d.length == e.event2d.length && 
		    this.params.length == e.params.length) {
                for (int i = 0; i < event2d.length; i++) {
                    if (e.event2d[i].length != this.event2d[i].length) {
                        retVal = false;
                        break;
                    } else {
                        for (int j = 0; j < event2d[i].length; j++) {
                            if (event2d[i][j] != e.event2d[i][j]) {
                                retVal = false;
                                break;
                            }
                        }
			for (int j = 0; j < params.length; j++)
			{
			    if (params[j] == null && e.params[j] == null) continue;
			    if (params[j] != null && !params[j].equals(e.params[j]))
			    {
                                retVal = false;
                                break;
			    }
			}
                        if (!retVal) break;
                    }
                }
            } else {
                retVal = false;
            }
            return retVal;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 97 * hash + (this.event2d != null ? this.event2d.hashCode() : 0);
        hash = 97 * hash + (this.params != null ? this.params.hashCode() : 0);
        return hash;
    }
    
    public static class Params
    {
	private double delay;
	private double strength;
	public Params(double delay, double strength)
	{
	    this.delay = delay;
	    this.strength = strength;
	}
	public double getDelay()
	{
	    return delay;
	}
	public double getStrength()
	{
	    return strength;
	}
        @Override
	public boolean equals(Object obj)
	{
	    if (obj instanceof Params)
	    {
		ComboEpisode.Params p = (ComboEpisode.Params)obj;
		return Math.abs(delay - p.delay) < 0.000001 && 
			Math.abs(strength - p.strength) < 0.000001;
	    }
	    return false;
	}
        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 23 * hash + (int) (Double.doubleToLongBits(this.delay) ^ (Double.doubleToLongBits(this.delay) >>> 32));
            hash = 23 * hash + (int) (Double.doubleToLongBits(this.strength) ^ (Double.doubleToLongBits(this.strength) >>> 32));
            return hash;
        }
        @Override
	public String toString()
	{
	    NumberFormat nf = NumberFormat.getInstance();
	    nf.setMaximumFractionDigits(6);
	    if (strength > 0.0) return "[" + nf.format(delay) + "," + nf.format(strength) + "]";
	    return "[" + nf.format(delay) + "]";
	}
    }    
    
    public double getEstr(int index)
    {
        if (this.params == null) return 1.0;
        if (this.params.length-1 < index) return 1.0;
        if (this.params[index] == null) return 1.0;
        return this.params[index].getStrength();
    }
    
    @Override
    public Interval getInterval(int index) {
        if (this.params == null) return new Interval(0.0, 0.001);
        if (this.params.length-1 < index) return new Interval(0.0, 0.001);
        if (this.params[index] == null) return new Interval(0.0, 0.001);
        return new Interval(0.0, this.params[index].getDelay());
    }
   /* 
    public int getEvent(int index) { 
        if (this.event[index].length > 1) {
            System.out.println("excess types");
        }
        return this.event[index][0]; 
    }
    */
    @Override
    public Object clone(){ return this;}
}
