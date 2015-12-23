/*
 * TDMinerMain2.java
 *
 * Created on March 13, 2006, 10:12 PM
 *
 */

package edu.iisc.tdminercore;

import au.com.bytecode.opencsv.CSVReader;
import edu.iisc.tdminercore.candidate.AprioriCandidateGeneration;
import edu.iisc.tdminercore.candidate.GeneralizedEpisodeCandidateGeneration;
import edu.iisc.tdminercore.candidate.ParallelAprioriCandidateGeneration;
import edu.iisc.tdminercore.candidate.PrefixSuffixCandidatesWithIntervals;
import edu.iisc.tdminercore.candidate.PrefixSuffixMatchCandidateGen;
import edu.iisc.tdminercore.counter.FastNonOverlappedEpisodeCounter;
import edu.iisc.tdminercore.counter.GeneralizedEpisodeCounter;
import edu.iisc.tdminercore.counter.GeneralizedEpisodeCounterWithExpiry;
import edu.iisc.tdminercore.counter.NonInterleavedEpisodeCounter;
import edu.iisc.tdminercore.counter.NonOverlappedEpisodeCounter;
import edu.iisc.tdminercore.counter.ParallelEpisodesCounterWithRepeatedEvents;
import edu.iisc.tdminercore.counter.ParallelNonOverlapperEpisodeCounter;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithIntervals;
import edu.iisc.tdminercore.counter.SerialIntervalCounter;
import edu.iisc.tdminercore.counter.SerialTrueIntervalCounter;
import edu.iisc.tdminercore.reader.CsvEventStreamReader;

import edu.iisc.tdminercore.writer.IWriter;
import edu.iisc.tdminercore.writer.XMLWriter;

import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.data.IEpisodeSet;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.filter.ThresholdFilterType;
import edu.iisc.tdminercore.miner.GenericMiner;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.PassiveObserver;
import edu.iisc.tdminercore.util.TimeConstraint;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.EnumSet;

// xml DOM processing
// see http://java.sun.com/webservices/jaxp/dist/1.1/docs/tutorial/dom/1_read.html
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;  
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;

import javax.xml.XMLConstants;

/**
 * Formerly, this class was called SampleMain and served as a placeholder.
 * The main takes one argument, the path or url of a configuration file.
 * This file contains all values necessary to load a data stream, mine it
 * and write those results to a data sink.
 * 
 * @author Deb
 * @author phreed@gmail.com
 */
public class TDMinerMain2 {
      
    private static Document configDoc;
    private static localNamespaceContextImpl nsContext;
    
    private String algorithmName;
    
    private static TimeConstraint<CONSTRAINT_MODE> intervalConstraints;
  

    /**
     * Creates a new instance of TDMinerMain2
     */
    public TDMinerMain2() {
        nsContext = new localNamespaceContextImpl();
        intervalConstraints = new TimeConstraint();
    }
    
    private static void initConfigurationDOM(String[] args) 
    {
        System.out.println("TDMiner core engine");
        InputStream configStream = null;
      
        try {
            if (args.length == 0) {
                System.out.println("Expecting input on standard input.");
                configStream = System.in;
            }
            else if (args.length == 1) {
                configStream = new FileInputStream(args[0]);
            }
            else if (args.length == 2) {
                if (args[0].equals("--file")) {
                    configStream = new FileInputStream(args[1]);
                }
                else
                if (args[0].equals("--url")) {
                    URL url = new URL(args[1]);
                    configStream = url.openStream();
                }
            }
            else {
                System.err.println("too many arguments");
                System.exit(1);
            }
        } catch (FileNotFoundException ex) {
            System.err.println("configuration file not found: " + ex.getMessage());
            System.exit(5);
        } catch (MalformedURLException ex) {
            System.err.println("configuration url not correctly formed: " + ex.getMessage());
            System.exit(6);
        } catch (IOException ex) {
            System.err.println("configuration file: " + ex.getMessage());
            System.exit(7);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            configDoc = builder.parse( configStream );
        } catch (SAXException sxe) {
           // Error generated during parsing
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
           x.printStackTrace();

        } catch (ParserConfigurationException pce) {
           // Parser with specified options can't be built
           pce.printStackTrace();

        } catch (IOException ioe) {
           // I/O error
           ioe.printStackTrace();
        }
    }
    
    /* 
     * A class to handle namespaces
     */
    private static class localNamespaceContextImpl implements NamespaceContext
    {
        private String[] prefix = {"v0", "v1"};
        private String[] nspace = {
            "urn:neural-code.org:tdminer/2007-03-01",
            "urn:neural-code.org:tdminer/2007-04-01"};
        
        public String getNamespaceURI(String nsprefix)
        {
            for(int ix=0; ix < this.prefix.length; ix++) {
                if (! nsprefix.equals(this.prefix[ix])) continue;
                return this.nspace[ix];
            }
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespace)
        {
            for(int ix=0; ix < this.nspace.length; ix++) {
                if (! namespace.equals(this.nspace[ix])) continue;
                return this.prefix[ix];
            }
            return null;
        }

        public Iterator<String> getPrefixes(String namespace)
        {
            List<String> prefixes = null;
            for(int ix=0; ix < this.nspace.length; ix++) {
                if (! namespace.equals(this.nspace[ix])) continue;
                prefixes.add(this.prefix[ix]);
            }
            return prefixes.iterator();
        }
    }  
     
    private static Element getChildElement(Element base, String ns, String name) 
    {
        NodeListIterable baseChildren =  new NodeListIterable(base.getChildNodes());
        for( Node baseChild : baseChildren ) {
            if (baseChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (baseChild.getNamespaceURI() != nsContext.getNamespaceURI(ns)) continue;
            if (!baseChild.getLocalName().equalsIgnoreCase(name)) continue;
            return (Element)baseChild;
        }
        return null;
    }
    
    private static class NodeListIterable implements Iterable<Node>
    {
        private NodeList nodelist;
        public NodeListIterable(NodeList nl) {
            this.nodelist = nl;
        }
        public NodeListIterable(Object nl) {
            this.nodelist = (NodeList)nl;
        }
        public Iterator<Node> iterator() {
             return new Iterator<Node>() {
                 private int position = -1; // indicates before the first node
                /**
                 * The position points at the current element.
                 */
                public boolean hasNext() {
                    if (nodelist.getLength() > position + 1) return true;
                    return false;
                }
                /** 
                 *
                 */
                public Node next() {
                    if (nodelist.getLength() < position + 2) return null;
                    position++;
                    return NodeListIterable.this.nodelist.item(position);
                }
                public void remove() {
                    System.err.println("removal of nodes is not allowed");
                    return;
                }
                
            }; // end new Iterator
        }
        public NodeList get() { 
            return this.nodelist;
        }
    }
    
     /**
     * A set of routines for retrieving values from the attributes of an element.
     */
    private static double getAttributeDouble(Element el, String name, double deflt )
    {
        if (! el.hasAttribute(name)) return deflt;
        
        String str = el.getAttribute(name);
        if (str == null) return deflt;
        
        try {
          return Double.parseDouble(str);
        } 
        catch (NumberFormatException ex) {
          return deflt; 
        }
    }
    
    private static boolean getAttributeBoolean(Element el, String name, boolean deflt)
    {
        if (! el.hasAttribute(name)) return deflt;
        
        String str = el.getAttribute(name);
        if (str == null) return deflt;
        
        if (str.equalsIgnoreCase("true")) return true;
        if (str.equalsIgnoreCase("false")) return false;
        if (str.equalsIgnoreCase("yes")) return true;
        if (str.equalsIgnoreCase("no")) return false;
        
        return deflt;
    }
      
    private static int getAttributeInteger(Element el, String name, int deflt)
    {
        return getAttributeInteger(el,name,deflt,"");
    }
    private static int getAttributeInteger(Element el, String name, int deflt, String msg )
    {
        if (! el.hasAttribute(name)) return deflt;
        
        String str = el.getAttribute(name);
        if (str == null) return deflt;
        
        try {
            Float value = Float.parseFloat(str);
            return Math.round(value.floatValue());
        }
        catch (NumberFormatException ex) {
            System.err.println("could not parse integer: " + msg);
            return deflt;
        }
    }
    
    
    private static char getAttributeChar(Element el, String name, char deflt )
    {
        if (! el.hasAttribute(name)) return deflt;
        
        String str = el.getAttribute(name);
        if (str == null) return deflt;
        
        if (str.length() < 1) return deflt;
        
        return str.charAt(0);
     }
    
    /**
     * Load the event stream from a file
     */
    private static IEventDataStream openEventDataStream(Element base) 
    {
        NodeListIterable baseChildren =  new NodeListIterable(base.getChildNodes());
        EVENT_STREAM:
        for( Node baseChild : baseChildren ) {
            if (baseChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (baseChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
            if (!baseChild.getLocalName().equalsIgnoreCase("event-stream")) break;
            
            Element source = (Element)baseChild;
                    
            NodeListIterable sourceChildren =  new NodeListIterable(source.getChildNodes());
            for( Node sourceChild : sourceChildren ) 
            {
                if (sourceChild.getNodeType() != Node.ELEMENT_NODE) continue;
                if (sourceChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
                if (!sourceChild.getLocalName().equalsIgnoreCase("time-constraint")) continue;
                    
                Element interval = (Element)sourceChild;

                TimeConstraint.Constraint constraint = TDMinerMain2.intervalConstraints.add(CONSTRAINT_MODE.class);
                constraint.setActivation(CONSTRAINT_MODE.LOAD, true);
                
                if (interval.hasAttribute("start")) {
                  double startTime = Double.NEGATIVE_INFINITY;
                  try {
                      startTime = Double.parseDouble(interval.getAttribute("start"));
                  } catch (NumberFormatException ex) {
                      System.err.println("interval start time is not parsable -Inf will be used");
                  }
                  constraint.setTimeStart(startTime);
                }

                if (interval.hasAttribute("stop")) {
                  double stopTime = Double.POSITIVE_INFINITY;
                  try {
                      stopTime = Double.parseDouble(interval.getAttribute("stop"));
                  } catch (NumberFormatException ex) {
                      System.err.println("interval stop time is not parsable +Inf will be used");
                  }
                  constraint.setTimeStop(stopTime);
                }
            }
             
            sourceChildren =  new NodeListIterable(source.getChildNodes());
            for( Node sourceChild : sourceChildren ) 
            {
                if (sourceChild.getNodeType() != Node.ELEMENT_NODE) continue;
                if (sourceChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
                if (!sourceChild.getLocalName().equalsIgnoreCase("file")) continue;
                    
                Element sourceFile = (Element)sourceChild;

                InputStream eventInputStream = null;
                try {
                    if (sourceFile.hasAttribute("url")) {
                      String urlName = sourceFile.getAttribute("url");
                      URL url = new URL(urlName);
                      eventInputStream = url.openStream();
                    }
                    else if (sourceFile.hasAttribute("name")) {
                      String fileName = sourceFile.getAttribute("name");
                      eventInputStream = new FileInputStream(fileName);
                    }
                } catch (FileNotFoundException ex) {
                    System.err.println("event stream file not found: " + ex.getMessage());
                    System.exit(5);
                } catch (MalformedURLException ex) {
                    System.err.println("event stream url not correctly formed: " + ex.getMessage());
                    System.exit(6);
                } catch (IOException ex) {
                    System.err.println("event stream problem: " + ex.getMessage());
                    System.exit(7);
                }

                String filetype = sourceFile.getAttribute("type");
                NodeListIterable nodelist = new NodeListIterable(sourceFile.getChildNodes());
                for( Node child : nodelist) 
                {
                    if (child.getNodeType() != child.ELEMENT_NODE) continue;

                    if (!filetype.equals(child.getLocalName())) continue;

                    if (filetype.equals("csv")) {
                        Element csv = (Element)child;
                        IEventDataStream eventDataStream = null;
                        int skiplines = getAttributeInteger(csv,"skip-lines",0);
                        char separatorChar = getAttributeChar(csv,"separator", CSVReader.DEFAULT_SEPARATOR);
                        char quoteChar = getAttributeChar(csv,"quote", CSVReader.DEFAULT_QUOTE_CHARACTER);

                        CSVReader csvreader = new CSVReader(
                                new InputStreamReader(eventInputStream), 
                                separatorChar, quoteChar, skiplines);

                        int eventTypeIndex = 0;
                        int seqKeyIndex = -1;
                        int startTimeIndex = 1;
                        int stopTimeIndex = -1;
                        NodeListIterable csvChildren = new NodeListIterable(csv.getChildNodes());
                        for( Node csvChild : csvChildren ) {
                            if (csvChild.getNodeType() != csvChild.ELEMENT_NODE) continue;

                            if (csvChild.getLocalName().equals("ordinal")) {
                                Element ordinal = (Element)csvChild;
                                startTimeIndex = getAttributeInteger(ordinal,"time-column",1,
                                        "start time column is not parsable, " +  String.valueOf(startTimeIndex) + " will be used");

                                stopTimeIndex = getAttributeInteger(ordinal,"duration-column",-1,
                                        "duration column is not parsable, " +  String.valueOf(stopTimeIndex) + " will be used");

                                seqKeyIndex = getAttributeInteger(ordinal,"key",-1,
                                        "seqence key column is not parsable, " +  String.valueOf(seqKeyIndex) + " will be used");

                                continue;
                            }
                            if (csvChild.getLocalName().equals("factor")) {
                                Element factor = (Element)csvChild;
                                eventTypeIndex = getAttributeInteger(factor,"column",1,
                                        "factor column is not parsable, " +  String.valueOf(eventTypeIndex) + " will be used");

                                continue;
                            }
                        }
                        try {
                            CsvEventStreamReader ereader = new CsvEventStreamReader(csvreader);
                            ereader.setEventTypeIndex(eventTypeIndex);
                            ereader.setStartIndex(startTimeIndex);
                            ereader.setStopIndex(stopTimeIndex);
                            ereader.setStopIndex(seqKeyIndex);

                            return ereader.read(eventInputStream, TDMinerMain2.intervalConstraints);
                        } catch (FileNotFoundException ex) {
                            System.out.println("File: " + ex.getMessage());
                        } catch (IOException ex) {
                            System.out.println("File: " + ex.getMessage());
                        }
                    }
                }
            }
        } 
        // if you get here no IEventDataStream could be made by any means.
        return null;
    }
    
    
    /**
     * The routines for loading the element parameters
     */
    private enum PropertyEnum {
        None,
        // the following imply types of nomination and counting
        InterEventIntervalLower,
        InterEventInterval,
        CandidateIntervals,
        CandidateDurations,
        AllowRepeatedEvents,
        SingleInterval,
        EpisodeExpiry,
        // the following imply pruning type
        FrequencyThreshold,
        AdaptiveThreshold,
        BaysianThreshold,
        // the following imply episode instance interaction
        DistinctEpisode,
        OverlappedEpisode,
        InterleavedEpisode,
        // the following imply serial v. parallel
        DirectedEpisode,
        UndirectedEpisode  
    }
    
    private long candidateChunkLimit = Math.round(Math.floor(1e5));
    private void parseProcessParameters(Element base) 
    {
        this.candidateChunkLimit = getAttributeInteger(base, "candidate-chunk-limit", Integer.MAX_VALUE);
    }
    
    private int[] cardinality = { 0, 10 };
    private void parseCardinality(Element base) 
    {
        this.cardinality[0] = getAttributeInteger(base, "start", 0);
        this.cardinality[1] = getAttributeInteger(base, "stop", 10);
    }
    
    private void parseTimeConstraint(Element base) 
    {
        Double startTime = getAttributeDouble(base, "start", 0);
        Double stopTime = getAttributeDouble(base, "stop", Double.POSITIVE_INFINITY);
        TimeConstraint.Constraint constraint = this.intervalConstraints.add(CONSTRAINT_MODE.class);
        constraint.setActivation(CONSTRAINT_MODE.PROSPECT, true);
        constraint.setTimeStart(startTime);    
        constraint.setTimeStop(stopTime);    
    }
    
    private double[] interEventIntervalValue = { 0.0, Double.POSITIVE_INFINITY };
    private boolean parseInterEventInterval(Element base) 
    {
        this.interEventIntervalValue[0] = getAttributeDouble(base, "min", 0.0);
        this.interEventIntervalValue[1] = getAttributeDouble(base, "max", 0.0);
        return interEventIntervalValue[0] < 1E-8 ? false : true;
    }
    
    private List<Interval> candidateIntervalValues = null;
    private void parseCandidateIntervals(Element base) 
    {
        this.candidateIntervalValues = new ArrayList<Interval>();
        
        NodeListIterable setChildren =  new NodeListIterable(base.getChildNodes());
        for( Node setChild : setChildren ) {
            if (setChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (setChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
            if (!setChild.getLocalName().equalsIgnoreCase("interval")) continue;
            Element candidate = (Element)setChild;
            Interval interval = new Interval(
                getAttributeDouble(candidate, "min", 0.0),
                getAttributeDouble(candidate, "max", 0.0) );
            candidateIntervalValues.add(interval);
        }
    }
    
    private List<Interval> candidateDurationValues = null;
    private void parseCandidateDurations(Element base) 
    {
        this.candidateDurationValues = new ArrayList<Interval>();
       
        NodeListIterable setChildren =  new NodeListIterable(base.getChildNodes());
        for( Node setChild : setChildren ) {
            if (setChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (setChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
            if (!setChild.getLocalName().equalsIgnoreCase("duration")) continue;
            Element candidate = (Element)setChild;
            Interval interval = new Interval(
                getAttributeDouble(candidate, "min", 0.0),
                getAttributeDouble(candidate, "max", 0.0) );
            candidateDurationValues.add(interval);
        }
    }
    
    private boolean allowRepeatedEvents = false;
    private void parseAllowRepeatedEvents(Element base) 
    {
        this.allowRepeatedEvents = getAttributeBoolean(base, "value", true); 
    }
    
    private boolean singleInterval = false;
    private void parseSingleInterval(Element base) 
    {
         this.singleInterval = getAttributeBoolean(base, "value", true); 
    }
    
    private double[] expiryValue = { 0.0, Double.POSITIVE_INFINITY };
    private void parseEpisodeExpiry(Element base) 
    {
        expiryValue[0] = getAttributeDouble(base, "min", 0.0); 
        expiryValue[1] = getAttributeDouble(base, "max", Double.POSITIVE_INFINITY); 
    }
    
    private double singleThresholdValue = 0.0;
    private double baseThresholdValue = 0.0;
    private double decayThresholdValue = 0.0;
    private void parseFrequencyThreshold(Element base, IEventDataStream eventDataStream) 
    {
        singleThresholdValue = getAdaptiveThreshold(base, 1.0, "single",0.0, eventDataStream); 
        baseThresholdValue = getAdaptiveThreshold(base,2.0, "base",0.0, eventDataStream);
        decayThresholdValue = getAttributeDouble(base, "decay", 0.6);
    }
    private static double getAdaptiveThreshold(Element base, double eventLength, String name, double dflt, IEventDataStream seq)
    {
        if (!base.getAttribute(name).equalsIgnoreCase("default")) {
            return getAttributeDouble(base, name, dflt); 
        }
        double fth = 1.0 / ((double)seq.getEventTypeCount() + 1.0);
        return (fth / eventLength);
    }
    
    private ThresholdFilterType thresholdType = ThresholdFilterType.STRENGTH_BASED;
    private void parseAdaptiveThreshold(Element base) 
    {
         // thresholdType = getThresholdType(base, "value", true);
    }
    
   /*
    * based on information found in the configuration file...
    * select prospecting objects
    * - nominator
    * - counter
    * - filter
    */  
    
    private enum AlgorithmEnum {
        Nominator, Counter, Pruner
    }
    
    private enum NominatorEnum { unknown,
          None,
          SerialApriori ,
          ParallelApriori ,
          PrefixSuffixMatch , 
          PrefixSuffixWithIntervals ,
          Generalized };
    
    private enum CounterEnum { unknown,
          None,
          FastNonOverlapped ,
          ExpiringNonOverlapped , 
          SerialInterval , 
          SerialTrueInterval ,
          SerialWithIntervals ,
          Generalized ,
          GeneralizedWithExpiry ,
          NonInterleaved , 
          ParallelNonOverlapped ,
          ParallelWithExpiry };   
          
    private enum PrunerEnum { unknown, 
        None,
        Geometric, 
        Adaptive, 
        Baysian };
  
    
    private enum OrderingEnum { unknown, ordered, unordered };
    private enum OverlapEnum { unknown, none, interleaf };
    
    private enum FileTypeEnum { unknown, csv, matlab, xml };
    
    /*
     * A discovery algorithm object is a finite constraint machine of sorts.
     * It keeps a list of potential algorithms.
     * As constraints/parameters are thrown at it, the object pares down its list.
     * When a single algorithm remains in the list, its name is printed.
     * If a constraint would eliminate all remaining algorithms then an 
     * exception is thrown.
     * When the algorithm is used a message is printed indicating any other
     * algorithms that could have been used, but were not.
     */
   
    public class DiscoveryAlgorithmList 
    {
        private class AlgoProxy<T>
        {
            public T type;
            public EnumSet<PropertyEnum> properties;
            public AlgoProxy(T theType, EnumSet<PropertyEnum> usage) 
            {
                this.type = theType;
                this.properties = usage;
            }
        }
        EnumSet<PropertyEnum> nominatorPropSet;
        List<AlgoProxy<NominatorEnum>> nominatorList;
        
        EnumSet<PropertyEnum> counterPropSet;      
        List<AlgoProxy<CounterEnum>> counterList;
        
        EnumSet<PropertyEnum> prunerPropSet;
        List<AlgoProxy<PrunerEnum>> prunerList;
            
        public String algorithmName;
        /**
         * The algorithm may be inferred or explicitly given.
         * In either case once the algorithm is created it must be complete.
         */
        public DiscoveryAlgorithmList() 
        {
            NOMINATE: {
                nominatorList = new ArrayList<AlgoProxy<NominatorEnum>>(); 
                AlgoProxy<NominatorEnum> nominatorProxy;
            
                nominatorList.add( new AlgoProxy<NominatorEnum>(
                    NominatorEnum.SerialApriori,
                    EnumSet.of(PropertyEnum.None,
                        PropertyEnum.DirectedEpisode)) );
            
                nominatorList.add( new AlgoProxy<NominatorEnum>(
                    NominatorEnum.ParallelApriori,
                    EnumSet.of(PropertyEnum.EpisodeExpiry,
                        PropertyEnum.UndirectedEpisode)) );
                         
                nominatorList.add( new AlgoProxy<NominatorEnum>(
                    NominatorEnum.PrefixSuffixMatch,
                    EnumSet.of(PropertyEnum.EpisodeExpiry,
                        PropertyEnum.DirectedEpisode)) );
                
                nominatorList.add( new AlgoProxy<NominatorEnum>(
                    NominatorEnum.PrefixSuffixWithIntervals,
                    EnumSet.of(PropertyEnum.EpisodeExpiry,
                        PropertyEnum.DirectedEpisode)) );
                
                nominatorList.add( new AlgoProxy<NominatorEnum>(
                    NominatorEnum.Generalized,
                    EnumSet.of(PropertyEnum.EpisodeExpiry,
                        PropertyEnum.DirectedEpisode)) );
                
                nominatorPropSet = EnumSet.of(PropertyEnum.None,
                    PropertyEnum.DirectedEpisode,
                    PropertyEnum.UndirectedEpisode  );
            }
            
            COUNT: {
                counterList = new ArrayList<AlgoProxy<CounterEnum>>(); 
                AlgoProxy<CounterEnum> counterProxy;
            
                counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.FastNonOverlapped,
                    EnumSet.of(PropertyEnum.None,
                        PropertyEnum.AllowRepeatedEvents,
                        PropertyEnum.DistinctEpisode,
                        PropertyEnum.DirectedEpisode)) );
            
                counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.ExpiringNonOverlapped,
                    EnumSet.of(PropertyEnum.EpisodeExpiry,
                        PropertyEnum.AllowRepeatedEvents,
                        PropertyEnum.DistinctEpisode,
                        PropertyEnum.DirectedEpisode)) );
          
                counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.SerialInterval,
                    EnumSet.of(PropertyEnum.InterEventInterval,
                        PropertyEnum.DistinctEpisode,
                        PropertyEnum.DirectedEpisode)) );
           
                counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.SerialTrueInterval,
                    EnumSet.of(PropertyEnum.InterEventInterval,
                        PropertyEnum.AllowRepeatedEvents,
                        PropertyEnum.InterEventIntervalLower,
                        PropertyEnum.DistinctEpisode,
                        PropertyEnum.DirectedEpisode)) );
           
                counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.SerialWithIntervals,
                    EnumSet.of(PropertyEnum.CandidateIntervals,
                        PropertyEnum.AllowRepeatedEvents,
                        PropertyEnum.DistinctEpisode,
                        PropertyEnum.DirectedEpisode)) );
           
                  counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.Generalized,
                    EnumSet.of(PropertyEnum.DistinctEpisode,
                          PropertyEnum.DirectedEpisode)) );
           
                  counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.GeneralizedWithExpiry,
                    EnumSet.of(PropertyEnum.CandidateDurations,
                          PropertyEnum.SingleInterval,
                          PropertyEnum.DistinctEpisode,
                          PropertyEnum.DirectedEpisode)) );
           
                  counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.NonInterleaved,
                    EnumSet.of(PropertyEnum.None,
                          PropertyEnum.OverlappedEpisode,
                          PropertyEnum.DirectedEpisode)) );
          
                  counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.ParallelNonOverlapped,
                    EnumSet.of(PropertyEnum.DistinctEpisode,
                          PropertyEnum.AllowRepeatedEvents,
                          PropertyEnum.UndirectedEpisode)) );
           
                  counterList.add( new AlgoProxy<CounterEnum>(
                    CounterEnum.ParallelWithExpiry,
                    EnumSet.of(PropertyEnum.EpisodeExpiry,
                          PropertyEnum.AllowRepeatedEvents,
                          PropertyEnum.DistinctEpisode,
                          PropertyEnum.UndirectedEpisode)) );
                
                counterPropSet = EnumSet.of(
                    PropertyEnum.InterEventIntervalLower,
                    PropertyEnum.InterEventInterval,
                    PropertyEnum.CandidateIntervals,
                    PropertyEnum.CandidateDurations,
                    PropertyEnum.AllowRepeatedEvents,
                    PropertyEnum.SingleInterval,
                    PropertyEnum.EpisodeExpiry,
                        
                    PropertyEnum.DistinctEpisode,
                    PropertyEnum.OverlappedEpisode,
                    PropertyEnum.InterleavedEpisode,
                        
                    PropertyEnum.DirectedEpisode,
                    PropertyEnum.UndirectedEpisode  
                        );
            };
            
            PRUNE: {
                prunerList = new ArrayList<AlgoProxy<PrunerEnum>>(); 
                AlgoProxy<PrunerEnum> prunerProxy;
            
                prunerList.add( new AlgoProxy<PrunerEnum>(
                    PrunerEnum.Geometric,
                    EnumSet.of(PropertyEnum.FrequencyThreshold)) );
            
                prunerList.add( new AlgoProxy<PrunerEnum>(
                    PrunerEnum.Adaptive,
                    EnumSet.of(PropertyEnum.AdaptiveThreshold)) );
                
                prunerList.add( new AlgoProxy<PrunerEnum>(
                    PrunerEnum.Baysian,
                    EnumSet.of(PropertyEnum.BaysianThreshold)) );
                
                prunerPropSet = EnumSet.of(
                        PropertyEnum.FrequencyThreshold,
                        PropertyEnum.AdaptiveThreshold,
                        PropertyEnum.BaysianThreshold);
            }
               
        }
        
        public NominatorEnum getNominatorType() 
        {
            return nominatorList.get(0).type;
        }
        public CounterEnum getCounterType() 
        {
            if (counterList == null || counterList.size() < 1) {
                System.err.println("No algorithms meet constraint criterion");
                return CounterEnum.None;
            }
            if (counterList.size() > 1) {
                System.out.println("Excess counter algorithms remain: " + counterList.size());
            }
            return counterList.get(0).type;
        }
        public PrunerEnum getPrunerType() 
        {
            return prunerList.get(0).type;
        }
        /**
         * explicit selection of an algorithm.
         */
        public void forceNominator(NominatorEnum type) 
        { forceSelect(nominatorList, type); }
        public void forceCounter(CounterEnum type) 
        { forceSelect(counterList, type); }
        public void forcePruner(PrunerEnum type) 
        { forceSelect(prunerList, type); }
        
        private <T> void forceSelect(List<AlgoProxy<T>> list, T type) 
        {
           for( Iterator<AlgoProxy<T>> iter = list.iterator(); iter.hasNext(); ) 
            {
                AlgoProxy<T> algo = iter.next();
                if (algo.type == type) continue;
                iter.remove();
            }  
        }
        public void select(Element algorithm) 
        {
            if (algorithm == null) return;
            
            NOMINATOR: {
                if (!algorithm.hasAttribute("nomination")) break NOMINATOR;
                
                String nominationName = algorithm.getAttribute("nomination");
                String abbreviation = nominationName.replace("-","");
                NominatorEnum nominatorType =
                        nominationName == null ? NominatorEnum.unknown
                      : nominationName.equalsIgnoreCase("prefixsuffix") ? NominatorEnum.PrefixSuffixMatch
                      : nominationName.equalsIgnoreCase("prefix-suffix") ? NominatorEnum.PrefixSuffixMatch
                        : abbreviation.equalsIgnoreCase("PrefixSuffixMatch") ? NominatorEnum.PrefixSuffixMatch
                      : nominationName.equalsIgnoreCase("apriori") ? NominatorEnum.SerialApriori
                        : abbreviation.equalsIgnoreCase("SerialApriori") ? NominatorEnum.SerialApriori
                      : nominationName.equalsIgnoreCase("parallel-apriori") ? NominatorEnum.ParallelApriori
                        : abbreviation.equalsIgnoreCase("ParallelApriori") ? NominatorEnum.ParallelApriori
                      : nominationName.equalsIgnoreCase("prefix-suffix-interval") ? NominatorEnum.PrefixSuffixWithIntervals
                        : abbreviation.equalsIgnoreCase("PrefixSuffixWithIntervals") ? NominatorEnum.PrefixSuffixWithIntervals
                      : nominationName.equalsIgnoreCase("generalized") ? NominatorEnum.Generalized
                        : abbreviation.equalsIgnoreCase("Generalized") ? NominatorEnum.Generalized
                      : NominatorEnum.unknown;
                if (NominatorEnum.unknown == nominatorType) break NOMINATOR;
                forceSelect(nominatorList, nominatorType);
            }

            COUNTER: {
                if (!algorithm.hasAttribute("counter")) break COUNTER;
                String counterName = algorithm.getAttribute("counter");
                String abbreviation = counterName.replace("-","");
                CounterEnum counterType =
                         counterName == null ? CounterEnum.unknown
                        : counterName.equalsIgnoreCase("serial") ? CounterEnum.SerialTrueInterval
                        : counterName.equalsIgnoreCase("ordered") ? CounterEnum.SerialTrueInterval
                        : counterName.equalsIgnoreCase("unordered") ? CounterEnum.ParallelWithExpiry
                        : counterName.equalsIgnoreCase("parallel") ? CounterEnum.ParallelWithExpiry
                        : counterName.equalsIgnoreCase("serial-fast") ? CounterEnum.FastNonOverlapped
                        : abbreviation.equalsIgnoreCase("FastNonOverlapped") ? CounterEnum.FastNonOverlapped
                        : counterName.equalsIgnoreCase("serial-expiry") ? CounterEnum.ExpiringNonOverlapped
                        : abbreviation.equalsIgnoreCase("ExpiringNonOverlapped") ? CounterEnum.ExpiringNonOverlapped
                        : counterName.equalsIgnoreCase("serial-interval") ? CounterEnum.SerialInterval
                        : abbreviation.equalsIgnoreCase("SerialInterval") ? CounterEnum.SerialInterval
                        : counterName.equalsIgnoreCase("serial-interevent-interval") ? CounterEnum.SerialTrueInterval
                        : abbreviation.equalsIgnoreCase("SerialTrueInterval") ? CounterEnum.SerialTrueInterval
                        : counterName.equalsIgnoreCase("serial-interevent-interval-discovery") ? CounterEnum.SerialWithIntervals
                        : abbreviation.equalsIgnoreCase("SerialWithIntervals") ? CounterEnum.SerialWithIntervals
                        : counterName.equalsIgnoreCase("serial-durable-event") ? CounterEnum.Generalized
                        : abbreviation.equalsIgnoreCase("Generalized") ? CounterEnum.Generalized
                        : counterName.equalsIgnoreCase("serial-experial-durable-event-discovery") ? CounterEnum.GeneralizedWithExpiry
                        : abbreviation.equalsIgnoreCase("GeneralizedWithExpiry") ? CounterEnum.GeneralizedWithExpiry
                        : counterName.equalsIgnoreCase("serial-overlapping") ? CounterEnum.NonInterleaved
                        : abbreviation.equalsIgnoreCase("NonInterleaved") ? CounterEnum.NonInterleaved
                        : counterName.equalsIgnoreCase("parallel-fast") ? CounterEnum.ParallelNonOverlapped
                        : abbreviation.equalsIgnoreCase("ParallelNonOverlapped") ? CounterEnum.ParallelNonOverlapped
                        : counterName.equalsIgnoreCase("ParallelWithExpiry") ? CounterEnum.ParallelWithExpiry
                        : CounterEnum.None;
                if (CounterEnum.unknown == counterType) break COUNTER;
                forceSelect(counterList, counterType);
            }

            PRUNER: {
                if (!algorithm.hasAttribute("pruner")) break PRUNER;
                String pruningName = algorithm.getAttribute("pruner");
                PrunerEnum prunerType =
                     pruningName == null ? PrunerEnum.unknown
                   : pruningName.equalsIgnoreCase("geometric") ? PrunerEnum.Geometric
                   : pruningName.equalsIgnoreCase("adaptive") ? PrunerEnum.Adaptive
                   : pruningName.equalsIgnoreCase("baysian") ? PrunerEnum.Baysian
                   : PrunerEnum.unknown;
                if (PrunerEnum.unknown == prunerType) break PRUNER;
                forceSelect(prunerList, prunerType);
            }
        }
        
        /**
         * This makes use of the synchronicity element to reduce the algorithms.
         * type:
             Overlap - episode instances may overlap but they may not 
                share an event or have events of the same type simultaneously active.
             Distinct - episode instances may not overlap
             Interleaved - ?

            ordered:
             True(Serial) - the order of events is important, 
                 (a.k.a. serial, directed)
                 i.e. A->B is distinct from B->A.
             False(Parallel) - the order of events is unimportant 
                 (a.k.a. parallel, undirected)
                 i.e. A->B is of the same episode as B->A.    
         */
        public void constrain(Element synchronicity) 
        {   
             if (synchronicity.hasAttribute("type")) {
                String type = synchronicity.getAttribute("type");
                if (0 == type.compareToIgnoreCase("distinct")) {
                    applyConstraint(PropertyEnum.DistinctEpisode, true);
                } else
                if (0 == type.compareToIgnoreCase("overlapped")) {
                   applyConstraint(PropertyEnum.OverlappedEpisode, true);
                }
                else
                if (0 == type.compareToIgnoreCase("interleaved")) {
                   applyConstraint(PropertyEnum.InterleavedEpisode, true);
                }
            }
            
            if (synchronicity.hasAttribute("ordered")) {
                String ordered = synchronicity.getAttribute("ordered");
                if (0 == ordered.compareToIgnoreCase("serial")) {
                    applyConstraint(PropertyEnum.DirectedEpisode, true);
                } else
                if (0 == ordered.compareToIgnoreCase("true")) {
                    applyConstraint(PropertyEnum.DirectedEpisode, true);
                } else
                if (0 == ordered.compareToIgnoreCase("parallel")) {
                    applyConstraint(PropertyEnum.UndirectedEpisode, true);
                } else
                if (0 == ordered.compareToIgnoreCase("false")) {
                   applyConstraint(PropertyEnum.UndirectedEpisode, true);
                }
            }
        }
        
        /**
         * This sets the property flag for each of the algorithms that use it.
         */
        public void constrain(PropertyEnum propertyType, Element parameter) 
        {          
            if (! parameter.hasAttribute("require")) return;
            
            String requirement = parameter.getAttribute("require");
            if (requirement == null) return;
            
            if (0 == requirement.compareToIgnoreCase("active")) {
                return;
            } else
            if (0 == requirement.compareToIgnoreCase("")) {
                return;
            } else
            if (0 == requirement.compareToIgnoreCase("on")) {
                applyConstraint(propertyType, true);
            } else
            if (0 == requirement.compareToIgnoreCase("use")) {
                applyConstraint(propertyType, true);;
            } else
            if (0 == requirement.compareToIgnoreCase("true")) {
                applyConstraint(propertyType, true);
            } else
            if (0 == requirement.compareToIgnoreCase("off")) {
                applyConstraint(propertyType, false);
            } else
            if (0 == requirement.compareToIgnoreCase("suppress")) {
                applyConstraint(propertyType, false);
            } else
            if (0 == requirement.compareToIgnoreCase("false")) {
                applyConstraint(propertyType, false);
            }
        }   
        
         /**
         * This sets the property flag for each of the algorithms that use it.
         */
        private void applyConstraint(PropertyEnum propertyType, boolean clusion) 
        {          
            if (nominatorPropSet.contains(propertyType)) {
                for( Iterator<AlgoProxy<NominatorEnum>> iter = nominatorList.iterator(); iter.hasNext(); ) 
                {
                    AlgoProxy<NominatorEnum> actor = iter.next();
                    boolean uses = actor.properties.contains(propertyType);
                    if (clusion && !uses) {
                        System.out.println("Nominator algorithm ["+actor.type+"] removed having no ["+propertyType+"]");
                        iter.remove();
                        continue;
                    }
                    if (!clusion && uses) {
                        System.out.println("Nominator algorithm ["+actor.type+"] suppressed having ["+propertyType+"]");
                        iter.remove();
                        continue;
                    }
                }
            }
            if (counterPropSet.contains(propertyType)) {
                for( Iterator<AlgoProxy<CounterEnum>> iter = counterList.iterator(); iter.hasNext(); ) 
                {
                    AlgoProxy<CounterEnum> actor = iter.next();
                    boolean uses = actor.properties.contains(propertyType);
                    if (clusion && !uses) {
                        System.out.println("Counter algorithm ["+actor.type+"] removed having no ["+propertyType+"]");
                        iter.remove();
                        continue;
                    }
                    if (!clusion && uses) {
                        System.out.println("Counter algorithm ["+actor.type+"] suppressed having ["+propertyType+"]");
                        iter.remove();
                        continue;
                    }
                }
            }
            if (prunerPropSet.contains(propertyType)) {
                for( Iterator<AlgoProxy<PrunerEnum>> iter = prunerList.iterator(); iter.hasNext(); ) 
                {
                    AlgoProxy<PrunerEnum> actor = iter.next();
                    boolean uses = actor.properties.contains(propertyType);
                    if (clusion && !uses) {
                        System.out.println("Pruner algorithm ["+actor.type+"] removed having no ["+propertyType+"]");
                        iter.remove();
                        continue;
                    }
                    if (!clusion && uses) {
                        System.out.println("Pruner algorithm ["+actor.type+"] suppressed having ["+propertyType+"]");
                        iter.remove();
                        continue;
                    }
                }
            }           
        }   
    }
    
    
    /**
     * write the output to a file
     */
    private static boolean report(Element base, IEpisodeSet episodes) 
    {
         NodeListIterable baseChildren =  new NodeListIterable(base.getChildNodes());
         for( Node baseChild : baseChildren ) {
            if (baseChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!baseChild.getNamespaceURI().equals(nsContext.getNamespaceURI("v0"))) continue;
            if (!baseChild.getLocalName().equalsIgnoreCase("target")) continue;
            Element target = (Element)baseChild;
            
            NodeListIterable targetChildren =  new NodeListIterable(target.getChildNodes());
            for( Node targetChild : targetChildren ) {
                if (targetChild.getNodeType() != Node.ELEMENT_NODE) continue;
                if (targetChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
                if (!targetChild.getLocalName().equalsIgnoreCase("file")) continue;
                Element xFile = (Element)targetChild;

                if (! xFile.hasAttribute("name")) return false;

                String fileName = xFile.getAttribute("name");
                IWriter writer = null;
                FileTypeEnum filetype = FileTypeEnum.unknown;

                if (xFile.hasAttribute("type")) {
                  String fileType = xFile.getAttribute("type");
                  filetype = 
                      fileType.equalsIgnoreCase("xml") ? FileTypeEnum.xml
                    : fileType.equalsIgnoreCase("matlab") ? FileTypeEnum.matlab
                    : FileTypeEnum.unknown; 
                }
                switch (filetype) {
                    case xml:
                       XMLWriter xwriter = new XMLWriter(fileName);
                       xwriter.setConfiguration(configDoc);
                       writer = xwriter;
                       break;
                    case matlab:
                        throw new RuntimeException("MatlabWriter no longer supported");
                       //writer = new MatlabWriter(fileName);
                       //break;     
                }
                episodes.export(writer);
                return true;
            }
         }
        return false;
    }
    
    /**
     * Examine the action for discover elements.
     */
    private void prospect(Element action, SessionInfo session) 
    {
        IEventDataStream eventDataStream = session.getSequence();
        GenericMiner miner = new GenericMiner();

        long t = System.currentTimeMillis();

        NodeListIterable actionChildren =  new NodeListIterable(action.getChildNodes());
        Element discover = null;
        for( Node actionChild : actionChildren ) {
            if (actionChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (actionChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
            if (!actionChild.getLocalName().equalsIgnoreCase("discover")) continue;
            discover = (Element)actionChild;
            break;
        }
        if (discover == null) {
            if (action.hasAttribute("name")) {
                System.out.println("No discover element found in action: "+action.getAttribute("name"));
            }
            else {
                System.out.println("No discover element found in unnamed action");
            }
            return;
        }

        /* =================
        * if the algorithm is supplied it overrides the deduction process
        */
        DiscoveryAlgorithmList algorithm = new DiscoveryAlgorithmList();
        NodeListIterable discoverChildren =  new NodeListIterable(discover.getChildNodes());
        for( Node discoverChild : discoverChildren ) {
            if (discoverChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (discoverChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
            if (!discoverChild.getLocalName().equalsIgnoreCase("algorithm")) continue;
            algorithm.select((Element)discoverChild);
            break;
        }
        /* 
         * Ensure that the selected algorithm fulfills the specified constraint
         */
        discoverChildren =  new NodeListIterable(discover.getChildNodes());
        for( Node discoverChild : discoverChildren ) {
            if (discoverChild.getNodeType() != Node.ELEMENT_NODE) continue;
            if (discoverChild.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
            Element discoverChildElement = (Element)discoverChild;
            String parameterName = discoverChildElement.getLocalName();
            
            if (parameterName.equalsIgnoreCase("Synchronicity")) { 
                algorithm.constrain(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Process")) { 
                this.parseProcessParameters(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Cardinality")) { 
                this.parseCardinality(discoverChildElement);
                continue;
            }
             if (parameterName.equalsIgnoreCase("Time-Constraint")) { 
                this.parseTimeConstraint(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Inter-Event-Interval")) { 
                boolean hasLowerLimit = this.parseInterEventInterval(discoverChildElement);
                if (hasLowerLimit) {
                    algorithm.constrain(PropertyEnum.InterEventIntervalLower, discoverChildElement);
                }
                else {
                    algorithm.constrain(PropertyEnum.InterEventInterval, discoverChildElement);
                }
                continue;
            }
            if (parameterName.equalsIgnoreCase("Candidate-Intervals")) {   
                algorithm.constrain(PropertyEnum.CandidateIntervals, discoverChildElement);
                this.parseCandidateIntervals(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Candidate-Durations")) {   
                algorithm.constrain(PropertyEnum.CandidateDurations, discoverChildElement);
                this.parseCandidateDurations(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Allow-Repeated-Events")) {   
                algorithm.constrain(PropertyEnum.AllowRepeatedEvents, discoverChildElement);
                this.parseAllowRepeatedEvents(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Single-Interval")) {   
                algorithm.constrain(PropertyEnum.SingleInterval, discoverChildElement);
                this.parseSingleInterval(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Episode-Expiry")) {   
                algorithm.constrain(PropertyEnum.EpisodeExpiry, discoverChildElement);
                this.parseEpisodeExpiry(discoverChildElement);
                continue;
            }
            if (parameterName.equalsIgnoreCase("Frequency-Threshold")) {   
                algorithm.constrain(PropertyEnum.FrequencyThreshold, discoverChildElement);
                this.parseFrequencyThreshold(discoverChildElement, eventDataStream);
                continue;
            }
        }
              
        /* ================
         * set the miner algorithms.
         */

        switch (algorithm.getCounterType()) {
            case None:
                return;

            case FastNonOverlapped:
                session.setCounter(new FastNonOverlappedEpisodeCounter());
                algorithm.forceNominator(NominatorEnum.SerialApriori);
                break;
                
            case ExpiringNonOverlapped:       
                session.setCounter(new NonOverlappedEpisodeCounter());
                algorithm.forceNominator(NominatorEnum.SerialApriori);
                
                session.setEpisodeExpiry(this.expiryValue[1]);
                break;
                
            case SerialInterval:
                session.setCounter(new SerialIntervalCounter());
                algorithm.forceNominator(NominatorEnum.PrefixSuffixMatch);
                
                session.setIntervalExpiry(this.interEventIntervalValue[1]);
                break;
                
            case SerialTrueInterval:
                session.setCounter(new SerialTrueIntervalCounter());
                algorithm.forceNominator(NominatorEnum.PrefixSuffixMatch);
                
                session.setIntervalExpiryLow(this.interEventIntervalValue[0]);
                session.setIntervalExpiry(this.interEventIntervalValue[1]);
                break;
                
            case SerialWithIntervals:
                algorithmName = "Discovery of episodes & inter-event intervals(Serial)";
                session.setCounter(new SerialEpisodeCounterWithIntervals());
                algorithm.forceNominator(NominatorEnum.PrefixSuffixWithIntervals);
                
                session.setIntervalsList(this.candidateIntervalValues);
                break;
                
            case Generalized:
                algorithmName = "Discovery of generalized episodes(Serial)";
                session.setCounter(new GeneralizedEpisodeCounter());
                algorithm.forceNominator(NominatorEnum.Generalized);
                break;
                
            case GeneralizedWithExpiry:
                algorithmName = "Discovery of generalized episodes with expiry constraint(Serial)";
                session.setCounter(new GeneralizedEpisodeCounterWithExpiry());
                algorithm.forceNominator(NominatorEnum.Generalized);
                
                session.setDurationsList(this.candidateDurationValues);
                break;
                
            case NonInterleaved:
                algorithmName = "Non-interleaved count(Serial)";  
                session.setCounter(new NonInterleavedEpisodeCounter());
                algorithm.forceNominator(NominatorEnum.PrefixSuffixMatch);
                break;
                
            case ParallelNonOverlapped:
                algorithmName = "Non-overlapped count(Parallel)";
                session.setCounter(new ParallelNonOverlapperEpisodeCounter());
                algorithm.forceNominator(NominatorEnum.ParallelApriori);
                break;
                
            case ParallelWithExpiry:  
                algorithmName = "Non-overlapped count with episode expiry constraint(Parallel)";
                session.setCounter(new ParallelEpisodesCounterWithRepeatedEvents());
                algorithm.forceNominator(NominatorEnum.ParallelApriori);
                
                session.setEpisodeExpiry(this.expiryValue[1]);
                break;

            default:
                System.err.println("No counting algorithm selected");
                return;
        }           
        System.out.println("Counter: " + session.getCounter().getName());       
        
        if (true)
        {
            switch (algorithm.getNominatorType()) {
                case None:
                    return;
                case SerialApriori:
                    session.setCandidateGenerator(new AprioriCandidateGeneration(session));
                    break;
                case PrefixSuffixMatch:
                    session.setCandidateGenerator(new PrefixSuffixMatchCandidateGen(session));
                    break;
                case PrefixSuffixWithIntervals:
                    session.setCandidateGenerator(new PrefixSuffixCandidatesWithIntervals(session));
                    break;
                case Generalized:
                    session.setCandidateGenerator(new GeneralizedEpisodeCandidateGeneration(session));
                    break;
                case ParallelApriori:             
                    session.setCandidateGenerator(new ParallelAprioriCandidateGeneration(session));
                    break;
                default:
                    System.err.println("No nomination algorithm selected");
                    return;
            }   
            System.out.println("Nominator: " + session.getCandidateGenerator().getName());
            
            switch (algorithm.getPrunerType()) {
                case None:
                    return;
                case Geometric:
                    System.out.println("Pruner: Geometric");
                    break;
                case Adaptive:
                    System.out.println("Pruner: Adaptive");
                    break;
                case Baysian:
                    System.out.println("Pruner: Baysian (not implemented)");
                    return;
                default:
                    return;
            }
        
            session.setFrequencyThreshold(2,this.baseThresholdValue);
            session.setFrequencyThreshold(1,this.singleThresholdValue);
            session.setFreqDecay(this.decayThresholdValue);
            session.setThresholdType(this.thresholdType);
            session.setGlevels(this.cardinality[0]);
            session.setPlevels(this.cardinality[1]);
            session.setAllowRepeat(this.allowRepeatedEvents);
            session.setDurationSingle(this.singleInterval);
            session.setChunkLimit((int)this.candidateChunkLimit);

            try {
                miner.mineSequence(new PassiveObserver(), session);
            } catch (IEpisode.NotImplementedException ex) {
                System.err.println("Episode method not implemented: "+ ex.getMessage());
            } catch (edu.iisc.tdminercore.util.IObserver.NotImplementedException ex) {
                System.err.println("Observer method not implemented: "+ ex.getMessage());
            }

            EpisodeSet result = session.getEpisodes();
            report(discover, result);
            return;
        }
        return;
    }
    
    /**
     * Examine the action for discover elements.
     */
    /*
    private EpisodeInstanceSet harvest(Element action, IEventDataStream eventDataStream) 
    {
        return new EpisodeInstanceSet();
    }
     */
    
    /**
     * @param args the command line arguments
     */
    private IEventDataStream seq = null;
    EpisodeSet episodes = null;
    public void processAction(Element action, SessionInfo session) throws IOException {
        // This little trick preserves the old seq
        IEventDataStream wseq = openEventDataStream(action);
        if (seq == null && wseq == null) { return; }
        if (seq == null) seq = wseq;
        
        // System.out.println(seq.getEventTypeCount() + "\n Event Types: \n" + seq.getEventFactor());
        // System.out.println("Sequence length = " + seq.getSize());
        
        session.setSequence(seq);
        prospect(action, session);
        this.episodes = session.getEpisodes();
        
        // EpisodeInstanceSet instances = harvest(action, seq);
    }
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Starting Data Mining");
        initConfigurationDOM(args);
        Element root = configDoc.getDocumentElement();
        
        TDMinerMain2 instance = new TDMinerMain2();
        SessionInfo session = new SessionInfo();
        instance.processAction(root, session);
        
        NodeListIterable rootChildren =  new NodeListIterable(root.getChildNodes());
            
        for( Node child : rootChildren ) {
            if (child.getNodeType() != child.ELEMENT_NODE) continue;
            if (child.getNamespaceURI() != nsContext.getNamespaceURI("v0")) continue;
            if (!child.getLocalName().equalsIgnoreCase("action")) continue;

            Element action = (Element)child;
       
            instance.processAction(action, session);
        } 
    }
   
}
