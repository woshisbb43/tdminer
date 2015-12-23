/*
 * XMLWriter.java
 *
 * Created on March 9, 2007, 11:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminercore.writer;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEpisodeSet;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;

import java.util.List;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

//JAXP 1.1
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import org.w3c.dom.Comment;

//W3C DOM
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


/**
 *
 * @author phreed@gmail.com
 */
public class XMLWriter implements IWriter 
{
    StreamResult stream = null;
    String fileName;
    final static String ns0 = "urn:neural-code.org:tdminer/2007-03-01";
    final static String ns1 = "urn:neural-code.org:tdminer/2007-03-01/control";
    final static String ns2 = "urn:neural-code.org:tdminer/2007-03-01/product";
    
    /** Creates a new instance of XMLWriter */
    public XMLWriter(String fileName) {
        this.fileName = fileName;
    }    
    
    private Document configDoc = null;
    public void setConfiguration(Document configDoc) 
    {
        this.configDoc = configDoc;
    }
     
    /** 
     * a general function for serializing a DOM using SAX
     */
    private void serializeConfig(TransformerHandler hd, Node node) 
    {
        AttributesImpl atts = new AttributesImpl();
        Document doc = node.getOwnerDocument();
        try {
            DocumentTraversal traversal = (DocumentTraversal)doc;

            TreeWalker walker = traversal.createTreeWalker(
                    node, NodeFilter.SHOW_ALL, null, true);

            traverseLevel(hd, walker, 1);
            
        } catch (Exception ex) {
            System.out.println("could not write the DOM "+ex.getMessage());
            ex.printStackTrace();
        }
    }
    /** 
     * a helper method for serializeConfig
     */
    private static final void traverseLevel(TransformerHandler hd, TreeWalker walker, int level) 
    {
        try {
            Node baseNode = walker.getCurrentNode();
            switch (baseNode.getNodeType()) {
                case Node.TEXT_NODE: {
                    String sometext = ((Text)baseNode).getData();
                    // System.out.println("TEXT NODE: "+ sometext);
                    hd.characters(sometext.toCharArray(),0,sometext.length());
                }
                break;
                case Node.ELEMENT_NODE: {
                    Element anElement = (Element)baseNode;
                    
                    String nsName = anElement.getNamespaceURI();
                    if (nsName.compareTo(ns2) == 0) {
                        break;
                    }
                    String elementName = anElement.getLocalName();
                    String tagName = anElement.getTagName();
                    
                    AttributesImpl attrs = new AttributesImpl();
                    NamedNodeMap nodeMap = anElement.getAttributes();
                    for( int ix=0; ix < nodeMap.getLength(); ix++ ) {
                        Node anAttr = nodeMap.item(ix);
                        String attrName = anAttr.getNodeName();
                        String attrValue = anAttr.getNodeValue();
                        attrs.addAttribute("","",attrName,"CDATA",attrValue);
                    }
                    hd.startElement(nsName,elementName,tagName,attrs);

                    // traverse children:
                    for (Node child = walker.firstChild();
                        child != null; 
                        child = walker.nextSibling()) 
                    {
                        traverseLevel(hd, walker, level+1);
                    }
                    hd.endElement(nsName,elementName,tagName);
                }
                break;
                case Node.COMMENT_NODE: {
                    String sometext = ((Comment)baseNode).getData();
                    // System.out.println("COMMENT NODE: "+ sometext);
                    hd.comment(sometext.toCharArray(),0,sometext.length());
                }
                break;
                default:
                    System.out.println("Unhandled type " + baseNode.getNodeType());
            }
            // return position to the current (level up):
            walker.setCurrentNode(baseNode);
        } 
        catch (SAXException ex) {
            System.out.println("Problem writing XML: " + ex.getMessage());
        }
    }
    
    private void serializeFactor(TransformerHandler hd, EventFactor factor) 
    {
        AttributesImpl atts = new AttributesImpl();
        try {
            hd.startElement(ns2,"factor","r:factor",atts);
            for( EventFactor.EventType type : factor ) {
                atts.clear();
                atts.addAttribute("","","name","CDATA",
                            (type.name == null ? "null" : type.name));

                if (type.alias != null) atts.addAttribute("","","alias","CDATA",type.alias);
                hd.startElement(ns2,"type","r:type",atts);
                hd.endElement(ns2,"type","r:type");
            }
            hd.endElement(ns2,"factor","r:factor");
        } catch (SAXException ex) {
            System.out.println("Problem writing XML: " + ex.getMessage());
        }
    }
    
    private void serializeEpisodeExpiry(TransformerHandler hd)
    {
        return;
        /*
        AttributesImpl atts = new AttributesImpl();
        try {
            atts.clear();
            atts.addAttribute("","","min","CDATA", "0.0");
            hd.startElement(ns2,"expiry","r:expiry",atts);
            hd.endElement(ns2,"expiry","r:expiry");
        } catch (SAXException ex) {
            System.out.println("Problem writing XML: " + ex.getMessage());
        }
         */
    }
    
    private void serializeEpisodeSignature(TransformerHandler hd, IEpisode episode, EventFactor factor) 
    {
        AttributesImpl atts = new AttributesImpl();
        try {
            atts.clear();
            hd.startElement(ns2,"signature","r:signature",atts);
            for( int index : episode.getEventTypeIndices() ) {
                atts.clear();
                atts.addAttribute("","","refid","CDATA", 
                        factor.get(index).name);
                //atts.addAttribute("","","min","CDATA", "0.0");
                //atts.addAttribute("","","max","CDATA", "+Inf");
                hd.startElement(ns2,"event","r:event",atts);
                hd.endElement(ns2,"event","r:event");
            }
            hd.endElement(ns2,"signature","r:signature");
        } catch (SAXException ex) {
            System.out.println("Problem writing XML: " + ex.getMessage());
        }
    }
    
    private void serializeEpisodeInstances(TransformerHandler hd, IEpisode episode)
    {
        AttributesImpl atts = new AttributesImpl();
        try {
            atts.clear();
            atts.addAttribute("","","count","CDATA", 
                   String.valueOf(episode.getInstanceCount()).toString());
            hd.startElement(ns2,"instance-list","r:signature",atts);
            for( IEpisode.EpisodeInstance instance : episode ) {
                atts.clear();
                hd.startElement(ns2,"instance","r:instance",atts);
                for( IEvent event : instance.eventList ) {
                    atts.addAttribute("","","source","CDATA", 
                            String.valueOf(event.getSourceId()).toString());
                    atts.addAttribute("","","time","CDATA", 
                            String.valueOf(event.getStartTime()).toString());
                    hd.startElement(ns2,"event","r:event",atts);
                    hd.endElement(ns2,"event","r:event");
                }
                hd.endElement(ns2,"instance","r:instance");
            }
            hd.endElement(ns2,"instance-list","r:instance-list");
        } catch (SAXException ex) {
            System.out.println("Problem writing XML: " + ex.getMessage());
        }
    }
    
    
    /**
     * create an output stream of the episode set.
     * Include the input configuration file in this set.
     */
    public boolean write(EpisodeSet prospect) { return write((IEpisodeSet)prospect); } 
    public boolean write(EpisodeInstanceSet prospect) { return write((IEpisodeSet)prospect); } 
    public boolean write(IEpisodeSet prospect) 
    {            
        if (prospect == null) return false;
        EventFactor factor = prospect.getEventFactor();
        if (factor == null) return false;
        if (factor.getSize() < 1) return false;
        
        try {
            StreamResult stream = new StreamResult(this.fileName); 

            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
           
            // SAX2.0 ContentHandler.
            TransformerHandler hd = tf.newTransformerHandler();
            Transformer serializer = hd.getTransformer();
            
            serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
            // serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");
           
            hd.setResult(stream);

            hd.startDocument();
            AttributesImpl atts = new AttributesImpl();
           
            // root element.
            hd.startPrefixMapping("r",ns2);
            
            hd.startElement(ns0,"tdminer","tdminer",atts);
            
            if (this.configDoc != null) {
                Element root = configDoc.getDocumentElement();
                NodeList nodelist = root.getChildNodes();
                for( int ix = 0; ix < nodelist.getLength(); ix++ ) {
                    Node anode = nodelist.item(ix);
                    serializeConfig(hd, anode);
                }
            }

            serializeFactor(hd, factor);

            int episodeLevel = 0;
            for( List<IEpisode> episodeList : prospect.levels()) {
                episodeLevel++;
            
                int episodeIx = 0;
                if (episodeList == null) continue;
                
                hd.startElement(ns2,"episode-list","r:episode-list",atts);
                for( IEpisode episode : episodeList ) {
                    atts.clear();
                    
                    //atts.addAttribute("","","name","CDATA", String.valueOf(episode.getIndex()).toString());
                    
                    atts.addAttribute("","","card","CDATA",
                            String.valueOf(episode.size()).toString());
                    hd.startElement(ns2,"episode","r:episode",atts);
                    
                    serializeEpisodeExpiry(hd);
                    serializeEpisodeSignature(hd, episode, factor);
                    serializeEpisodeInstances(hd, episode);
                   
                    hd.endElement(ns2,"episode","r:episode");
                    episodeIx++;
                }
                hd.endElement(ns2,"episode-list","r:episode-list");
            }
           
            hd.endElement(ns0,"tdminer","tdminer");
            hd.startPrefixMapping("r",ns2);
            
            hd.endDocument();
        } 
        catch (TransformerConfigurationException ex) {
            System.out.println("Problem writing XML: " + ex.getMessage());
        }
        catch (SAXException ex) {
            System.out.println("Problem writing XML: " + ex.getMessage());
        }
        return true;
    }
    
    
}
