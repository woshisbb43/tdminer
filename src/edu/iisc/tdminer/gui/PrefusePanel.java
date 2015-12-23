/*
 * PrefusePanel.java
 *
 * Created on September 5, 2007, 2:13 PM
 */
package edu.iisc.tdminer.gui;

import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminercore.miner.SessionInfo;


import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.Layout;
import prefuse.action.layout.SpecifiedLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;

/**
 *
 * @author  pbutler
 */
public class PrefusePanel extends javax.swing.JPanel implements ITaskPanel
{
    private JFrame frame;
    private JLabel jLabelStatus;
    private static final String graph = "graph";
    private static final String nodes = "graph.nodes";
    private static final String edges = "graph.edges";
    private Visualization m_vis;
    private Display display;
    private StateInfo stateInfo;
    private boolean started = false;
    /** Label data field included in generated Graphs */
    private static final String LABEL = "label";
    private static final String POSX = "posx";
    private static final String POSY = "posy";
    private static final String FIXED = "fixed";
    private static final String ESTR = "estr";
    private static final String FREQ = "freq";
    private static final String DELAY = "delay";
    private static final String EVTID = "event_id";
    /** Node table schema used for generated Graphs */
    private static final Schema SCHEMA = new Schema();
    private static final Schema ESCHEMA = new Schema();
    private ActionList animate;
    private ActionList pcanimate;
    private EpisodeSet episodes;    
    
    static
    {
        SCHEMA.addColumn(LABEL, String.class, "");
        SCHEMA.addColumn(POSX, float.class);//, 0.0);
        SCHEMA.addColumn(POSY, float.class);//, 0.0);
        SCHEMA.addColumn(FIXED, boolean.class, false);
        SCHEMA.addColumn(EVTID, int.class, 0);

        ESCHEMA.addColumn(ESTR, float.class, 0.0);
        ESCHEMA.addColumn(FREQ, int.class, 0);
        ESCHEMA.addColumn(DELAY, float.class, 0.0);
    }
    private boolean flag = true;
    private HashMap<InnerKey, InnerVal> edgemap;
    /** Creates new form PrefusePanel */
    public PrefusePanel()
    {
        initComponents();

        this.m_vis = new Visualization();

        // create a new, empty visualization for our data


        // --------------------------------------------------------------------
        // set up the renderers


        LabelRenderer tr = new LabelRenderer(LABEL);
        tr.setRoundedCorner(5, 5);

        DefaultRendererFactory drf = new DefaultRendererFactory(tr);
        m_vis.setRendererFactory(drf);
        //drf.add("ingroup('"+nodes+"')", tr);

        //Edge renderer
        EdgeRenderer er = new EdgeRenderer(0, prefuse.Constants.EDGE_ARROW_FORWARD);
        er.setArrowHeadSize(10, 10);
        drf.add("ingroup('" + edges + "')", er);

        // --------------------------------------------------------------------
        // register the data with a visualization

        // adds graph to visualization and sets renderer label field
        //g = GraphLib.getGrid(3,3);//new Graph();
        //Node n = g.addNode();

        // --------------------------------------------------------------------
        // set up a display to show the visualization

        display = new Display(m_vis);
        display.setSize(400, 400);
        display.pan(200, 200);
        display.setForeground(Color.GRAY);
        display.setBackground(Color.WHITE);

        // main display controls
        display.addControlListener(new FocusControl(1));
        display.addControlListener(new DragControl());
        display.addControlListener(new PanControl());
        display.addControlListener(new ZoomControl());
        display.addControlListener(new WheelZoomControl());
        display.addControlListener(new ZoomToFitControl());
        display.addControlListener(new NeighborHighlightControl());

        // overview display
//        Display overview = new Display(vis);
//        overview.setSize(290,290);
//        overview.addItemBoundsListener(new FitOverviewListener());

        display.setForeground(Color.GRAY);
        display.setBackground(Color.WHITE);

        //Setup some basic predicates
        Predicate springPred =
                new AndPredicate(new NonZeroFreqPredicate(), new MinEStrongPredicate());

        // --------------------------------------------------------------------
        // create actions to process the visual data

        int hops = 30;
        final GraphDistanceFilter filter = new GraphDistanceFilter(graph, hops);

        ColorAction fill = new ColorAction(nodes,
                VisualItem.FILLCOLOR, ColorLib.rgb(200, 200, 255));
        //fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));

        ActionList draw = new ActionList();
        draw.add(fill);
        draw.add(new VisibilityFilter(edges, springPred));
        draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 0));
        draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
        draw.add(new SizeAction(nodes, 4.0));

        //Here we define color rules based upon EStrong value for the fill (arrowhead) of the  edge
        draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib.rgb(220, 0, 0)));

        //Here we define color rules based upon EStrong value for the stroke of the edge
        draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib.rgb(220, 0, 0)));

        //Define size of edges based on fun things
        draw.add(new LinearMapSizeAction(edges, FREQ, 0.0f, 10.0f));

        ActionList setup = new ActionList(10);
        setup.add(new ConditionalForceLayout(graph, true, true, springPred));
        SpecifiedLayout sl = new SpecifiedLayout(graph, POSX, POSY);
        sl.setFixedField(FIXED);
        setup.add(sl);
        setup.add(new ZoomCenterAction(true));

        animate = new ActionList(Activity.INFINITY);
        animate.add(new ConditionalForceLayout(graph, springPred));
        //animate.add(fill);
        //animate.add(new PinnedLayout(graph, true, 0.0f));
        animate.add(new ZoomCenterAction());
        animate.add(new RepaintAction());

        pcanimate = new ActionList();
        pcanimate.add(new PolychronLayout(nodes)); //, springPred)); 
        //animate.add(fill);
        //animate.add(new PinnedLayout(graph, true, 0.0f));
        pcanimate.add(new ZoomCenterAction());
        pcanimate.add(new RepaintAction());

        // finally, we register our ActionList with the Visualization.
        // we can later execute our Actions by invoking a method on our
        // Visualization, using the name we've chosen below.

        m_vis.putAction("setup", setup);
        m_vis.putAction("draw", draw);
        m_vis.putAction("layout", animate);


        TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
        focusGroup.addTupleSetListener(new TupleSetListener()
        {

            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem)
            {
                for (int i = 0; i < rem.length; ++i)
                {
                    ((VisualItem) rem[i]).setFixed(false);
                }
                for (int i = 0; i < add.length; ++i)
                {
                    ((VisualItem) add[i]).setFixed(false);
                    ((VisualItem) add[i]).setFixed(true);
                }
                if (ts.getTupleCount() == 0)
                {
                    ts.addTuple(rem[0]);
                    ((VisualItem) rem[0]).setFixed(false);
                }
                refreshGraph();
            }
        });

        add(display, java.awt.BorderLayout.CENTER);
        started = true;
        //PolychronCheckBox.setVisible(false);
        setGraph(blankGraph(), LABEL);
    }

    public void setJLabelStatus(JLabel jLabelStatus)
    {
        this.jLabelStatus = jLabelStatus;
    }

    /**
     * If the frame supports a settings menu then attach to it.
     * The purpose of this function is to give the parent frame time 
     * to initialize before making it do work for the panel.
     */
    public void setFrame(JFrame frame) throws Exception
    {
        if (!(frame instanceof ParentMenu))
        {
            System.out.println("Not a Parent Menu");
        }
        this.frame = frame;
    }

    public javax.swing.JPanel getDisplay()
    {
        return this.getDisplay();
    }
    
    private static class InnerVal
    {
        public double estr;
        public int count;
        public double delay;
    }
    
    private static class InnerKey
    {
        public String src;
        public String tgt;

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final InnerKey other = (InnerKey) obj;
            if (this.src != other.src && (this.src == null || !this.src.equals(other.src)))
            {
                return false;
            }
            if (this.tgt != other.tgt && (this.tgt == null || !this.tgt.equals(other.tgt)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 53 * hash + (this.src != null ? this.src.hashCode() : 0);
            hash = 53 * hash + (this.tgt != null ? this.tgt.hashCode() : 0);
            return hash;
        }
    }
    
    private void filterEdges()
    {
        if (!flag) return;
        this.edgemap = new HashMap<InnerKey, InnerVal>();
        SessionInfo session = stateInfo.getSession();
        EventFactor factor = episodes.getEventFactor();
        
        int level = getLevel();
        int index = jSliderDataSlider.getValue();
        if (level <= episodes.getSize())
        {
            for (IEpisode ep : episodes.getEpisodeList(level))
            {
                //System.out.println(ep.toString(session.getEventFactor(), index));
                for (int i = 0; i < ep.size() - 1; i++)
                {
                    String srcname = factor.get(ep.getEvent(i)).name;
                    String tgtname = factor.get(ep.getEvent(i + 1)).name;
                    InnerKey edgename = new InnerKey();
                    edgename.src = srcname;
                    edgename.tgt = tgtname;
                    InnerVal edge = new InnerVal();

                    if (edgemap.containsKey(edgename))
                    {
                        edge = edgemap.get(edgename);
                        double prev_estr = edge.estr;
                        if (ep.getEstr(index) > prev_estr)
                        {
                            edge.estr = ep.getEstr(index);
                        }
                        int prev_count = edge.count;
                        edge.count = prev_count + ep.getVotes(index);
                    }
                    else
                    {
                        edge.estr = ep.getEstr(index);
                        edge.count = ep.getVotes(index);
                        edgemap.put(edgename, edge);
                    }
                    if (ep.size() == 2 && edge != null)
                    {
                        double delay;
                        if (ep.getInterval(0) == null)
                        {
                            delay = stateInfo.getSession().getIntervalExpiry() - stateInfo.getSession().getIntervalExpiryLow();
                        }
                        else
                        {
                            delay = ep.getInterval(0).getAvg();
                        }
                        edge.delay = delay;
                    }
                }
            }
        }
    }
    
    

    /**
     * This function builds a graph to display the currently mined data 
     * furthermore it creates a table containing positions for the nodes
     * (should they exist, and estrong values for the edges
     * @param factor EventFactor node describing the letters of all the events
     */
    public void makeGraph()
    {
        if (!flag) return;
        SessionInfo session = stateInfo.getSession();
        EventFactor factor;
        Graph g_new = new Graph(true);
        HashMap<String, Node> nodemap = new HashMap<String, Node>();
        if (EpisodeButton.isSelected())
        {
            episodes = session.getEpisodes();
        }
        else if (ReferenceButton.isSelected())
        {
            episodes = session.getReference();

        }
        else if (UnionButton.isSelected() && (session.getReference() != null))
        {
            episodes = new EpisodeSet(session.getEpisodes());
            episodes.addAll(session.getEpisodes());
            episodes.addAll(session.getReference());
        }
        else if (IntersectButton.isSelected() && (session.getReference() != null))
        {
            //episodes = new EpisodeSet(  );
            episodes = session.getEpisodes().intersect(session.getReference());
            //episodes.retainAll(session.getReference());
        }
        else
        {
            episodes = null;
        }

        if (episodes == null)
        {
            setGraph(blankGraph(), LABEL);
            return;
        }
        factor = episodes.getEventFactor();


        g_new.getNodeTable().addColumns(SCHEMA);

        Random r = new Random();
        for (int i = 0; i < factor.getSize(); i++)
        {
            EventFactor.EventType e = factor.get(i);

            if (nodemap.containsKey(e.name))
            {
                continue;
            }

            Node n = g_new.addNode();
            n.setString(LABEL, e.name);
            n.setInt(EVTID, i);
            nodemap.put(e.name, n);

            if (e.isPosSet())
            {
                n.setFloat(POSX, e.getX());
                n.setFloat(POSY, e.getY());
                n.setBoolean(FIXED, true);
//                System.out.println(e.name + " is pinned at (" +
//                        String.valueOf(e.getX()) + ", " + String.valueOf(e.getY()) + ")");
            }
            else
            {
                //hack to prevent everything getting stuck on the same axis
                n.setFloat(POSX, r.nextFloat());
                n.setFloat(POSY, r.nextFloat());
                n.setBoolean(FIXED, false);
            }
        }

        g_new.getEdgeTable().addColumns(ESCHEMA);
        filterEdges();
        for(InnerKey key : edgemap.keySet())
        {
            InnerVal val = edgemap.get(key);
            Node n1 = nodemap.get(key.src);
            Node n2 = nodemap.get(key.tgt);
            Edge e = g_new.addEdge(n1, n2);
            e.setFloat(ESTR, (float)val.estr);
            e.setInt(FREQ, val.count);
            e.setFloat(DELAY, (float)val.estr);
        }

        setGraph(g_new, LABEL);
        refreshGraph();
    }

    /**
     * Creates a blank graph containing only one node with the label
     * "No Data Available"
     * @return Graph
     */
    static public Graph blankGraph()
    {
        Graph g = new Graph();
        g.getNodeTable().addColumns(SCHEMA);
        Node n = g.addNode();
        n.setString(LABEL, "Click Display to load Episodes");
        return g;
    }

    public void setGraph(Graph g, String label)
    {
        // update labeling
        DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
        ((LabelRenderer) drf.getDefaultRenderer()).setTextField(label);

        // update graph
        m_vis.removeGroup(graph);
        VisualGraph vg = m_vis.addGraph(graph, g);
        m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
        if (vg.getNodeCount() > 1)
        {
            VisualItem f = (VisualItem) vg.getNode(0);
            m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
        }
        setupGraph();
    }

    public void setupGraph()
    {
        if (started)
        {
            m_vis.runAfter("setup", "draw");
            if (isAnimating() && this.isVisible())
            {
                m_vis.runAfter("draw", "layout");
            }
            m_vis.run("setup");
            m_vis.repaint();
        }
    }

    /**
     * Starts the actions to animate the graph
     */
    public void animateGraph()
    {
        if (started)
        {
            m_vis.runAfter("draw", "layout");
            m_vis.run("draw");
        }
    }

    public void refreshGraph()
    {
        m_vis.run("draw");
        m_vis.repaint();
    }

    public void pauseGraph()
    {
        if (m_vis != null)
        {
            m_vis.cancel("layout");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setOpButtonGroup = new javax.swing.ButtonGroup();
        prefuseControlPanel = new javax.swing.JPanel();
        displayPanel = new javax.swing.JPanel();
        updateButton = new javax.swing.JButton();
        setPanel = new javax.swing.JPanel();
        EpisodeButton = new javax.swing.JRadioButton();
        ReferenceButton = new javax.swing.JRadioButton();
        UnionButton = new javax.swing.JRadioButton();
        IntersectButton = new javax.swing.JRadioButton();
        swapButton = new javax.swing.JButton();
        minLevelPanel = new javax.swing.JPanel();
        levelSlider = new javax.swing.JSlider();
        eStrongPanel = new javax.swing.JPanel();
        eStrongSlider = new javax.swing.JSlider();
        eStrongValue = new javax.swing.JLabel();
        AutoZoomCheckBox = new javax.swing.JCheckBox();
        AnimateCheckBox = new javax.swing.JCheckBox();
        PolychronCheckBox = new javax.swing.JCheckBox();
        jButtonPosition = new javax.swing.JButton();
        jButtonSaveImg = new javax.swing.JButton();
        jPanelSlider = new javax.swing.JPanel();
        jSliderDataSlider = new javax.swing.JSlider();
        jPanelWindowOptions = new javax.swing.JPanel();
        jLabelWindowStatus = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(844, 703));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        prefuseControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        prefuseControlPanel.setAlignmentY(0.0F);

        displayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Display"));
        displayPanel.setLayout(new java.awt.GridBagLayout());

        updateButton.setText("Display");
        updateButton.setAlignmentY(1.0F);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        displayPanel.add(updateButton, gridBagConstraints);

        setPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sets"));
        setPanel.setLayout(new javax.swing.BoxLayout(setPanel, javax.swing.BoxLayout.Y_AXIS));

        setOpButtonGroup.add(EpisodeButton);
        EpisodeButton.setSelected(true);
        EpisodeButton.setText("Episode Set");
        EpisodeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        EpisodeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        setPanel.add(EpisodeButton);

        setOpButtonGroup.add(ReferenceButton);
        ReferenceButton.setText("Reference");
        ReferenceButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ReferenceButton.setEnabled(false);
        ReferenceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        setPanel.add(ReferenceButton);

        setOpButtonGroup.add(UnionButton);
        UnionButton.setText("Union");
        UnionButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        UnionButton.setEnabled(false);
        UnionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        setPanel.add(UnionButton);

        setOpButtonGroup.add(IntersectButton);
        IntersectButton.setText("Intersect");
        IntersectButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        IntersectButton.setEnabled(false);
        IntersectButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        setPanel.add(IntersectButton);

        swapButton.setText("Current -> Reference ");
        swapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swapButtonActionPerformed(evt);
            }
        });
        setPanel.add(swapButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        displayPanel.add(setPanel, gridBagConstraints);

        minLevelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Level"));
        minLevelPanel.setLayout(new javax.swing.BoxLayout(minLevelPanel, javax.swing.BoxLayout.Y_AXIS));

        levelSlider.setMajorTickSpacing(1);
        levelSlider.setPaintLabels(true);
        levelSlider.setPaintTicks(true);
        levelSlider.setSnapToTicks(true);
        levelSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                levelSliderStateChanged(evt);
            }
        });
        minLevelPanel.add(levelSlider);

        eStrongPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("EStrong"));
        eStrongPanel.setLayout(new javax.swing.BoxLayout(eStrongPanel, javax.swing.BoxLayout.Y_AXIS));

        eStrongSlider.setMajorTickSpacing(100);
        eStrongSlider.setMinorTickSpacing(10);
        eStrongSlider.setPaintTicks(true);
        eStrongSlider.setValue(0);
        eStrongSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                eStrongSliderStateChanged(evt);
            }
        });
        eStrongPanel.add(eStrongSlider);

        eStrongValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        eStrongValue.setText("0.0");
        eStrongPanel.add(eStrongValue);
        eStrongValue.getAccessibleContext().setAccessibleName("0.50");

        AutoZoomCheckBox.setSelected(true);
        AutoZoomCheckBox.setText("Auto Zoom");
        AutoZoomCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        AutoZoomCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        AnimateCheckBox.setSelected(true);
        AnimateCheckBox.setText("Animate");
        AnimateCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        AnimateCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        AnimateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AnimateCheckBoxActionPerformed(evt);
            }
        });

        PolychronCheckBox.setText("Polychron");
        PolychronCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        PolychronCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PolychronCheckBoxActionPerformed(evt);
            }
        });

        jButtonPosition.setText("Set Positions...");
        jButtonPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPositionActionPerformed(evt);
            }
        });

        jButtonSaveImg.setText("Save Image");
        jButtonSaveImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveImgActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout prefuseControlPanelLayout = new org.jdesktop.layout.GroupLayout(prefuseControlPanel);
        prefuseControlPanel.setLayout(prefuseControlPanelLayout);
        prefuseControlPanelLayout.setHorizontalGroup(
            prefuseControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(displayPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(minLevelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(eStrongPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(prefuseControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(AutoZoomCheckBox)
                .add(18, 18, 18)
                .add(AnimateCheckBox))
            .add(prefuseControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(PolychronCheckBox))
            .add(prefuseControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jButtonPosition)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonSaveImg))
        );
        prefuseControlPanelLayout.setVerticalGroup(
            prefuseControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(prefuseControlPanelLayout.createSequentialGroup()
                .add(displayPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(minLevelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(eStrongPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(prefuseControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(AutoZoomCheckBox)
                    .add(AnimateCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(PolychronCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(prefuseControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonPosition)
                    .add(jButtonSaveImg))
                .add(39, 39, 39))
        );

        add(prefuseControlPanel, java.awt.BorderLayout.EAST);

        jPanelSlider.setLayout(new java.awt.BorderLayout());

        jSliderDataSlider.setMajorTickSpacing(10);
        jSliderDataSlider.setMinorTickSpacing(1);
        jSliderDataSlider.setPaintTicks(true);
        jSliderDataSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderDataSliderStateChanged(evt);
            }
        });
        jPanelSlider.add(jSliderDataSlider, java.awt.BorderLayout.CENTER);

        jLabelWindowStatus.setText("Span: 0.0 to 0.0 sec");
        jPanelWindowOptions.add(jLabelWindowStatus);

        jPanelSlider.add(jPanelWindowOptions, java.awt.BorderLayout.EAST);

        jLabel1.setText("Time segment");
        jPanelSlider.add(jLabel1, java.awt.BorderLayout.WEST);

        add(jPanelSlider, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    private void PolychronCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PolychronCheckBoxActionPerformed
        //MUST CANCEL FIRST
        //m_vis.cancel("layout");
        pauseGraph();
        if (PolychronCheckBox.isSelected())
        {
            m_vis.putAction("layout", pcanimate);
        }
        else
        {
            m_vis.putAction("layout", animate);
        }
        animateGraph();
        
    }//GEN-LAST:event_PolychronCheckBoxActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        EpisodeSet eps = stateInfo.getSession().getEpisodes();
        updateSetButtons();
        //maxLevelSlider.setMaximum(10);
        if (eps == null || eps.getSize() == 0)
        {
            levelSlider.setEnabled(false);
        }
        else
        {
            int lvls = eps.getSize();

            synchronized(this)
            {
                flag = false;
                levelSlider.setEnabled(true);
                if (lvls < 2)
                {
                    levelSlider.setMinimum(1);
                }
                else
                {
                    levelSlider.setMinimum(2);
                }
                levelSlider.setValue(2);
                levelSlider.setMaximum(lvls);
                flag = true;
            }


            if (isAnimating())
            {
                animateGraph();
            }
        }
                        
    }//GEN-LAST:event_formComponentShown

    private void levelSliderStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_levelSliderStateChanged
    {//GEN-HEADEREND:event_levelSliderStateChanged
        //System.out.println("Calling makegraph 2 : " + evt);
        makeGraph();
}//GEN-LAST:event_levelSliderStateChanged

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
        pauseGraph();
    }//GEN-LAST:event_formComponentHidden

    private void AnimateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AnimateCheckBoxActionPerformed
        //MUST CANCEL FIRST
        m_vis.cancel("layout");
        if (isAnimating())
        {
            m_vis.putAction("layout", animate);
            m_vis.run("layout");
        }
        else
        {
        //m_vis.putAction("layout", noanimate);
        }
        
    }//GEN-LAST:event_AnimateCheckBoxActionPerformed

    private void swapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swapButtonActionPerformed
        SessionInfo session = stateInfo.getSession();
        session.setReference(session.getEpisodes());
        updateSetButtons();
    }//GEN-LAST:event_swapButtonActionPerformed

    public void updateSetButtons()
    {
        SessionInfo session = stateInfo.getSession();
        boolean epenabled = (session.getEpisodes() != null);
        boolean renabled = (session.getReference() != null);
        EpisodeButton.setEnabled(epenabled);
        ReferenceButton.setEnabled(renabled);
        UnionButton.setEnabled(epenabled && renabled);
        IntersectButton.setEnabled(epenabled && renabled);
    }

    private void eStrongSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_eStrongSliderStateChanged
        int i = eStrongSlider.getValue();
        double f = i / 100.;
        eStrongValue.setText(String.valueOf(f));
        refreshGraph();
    }//GEN-LAST:event_eStrongSliderStateChanged

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed

        if (this.stateInfo.getSession().getSequence() == null ||
                this.stateInfo.getSession().getEpisodes() == null)
        {
            JOptionPane.showMessageDialog(this, "Data sequence or Episode set not available", "" +
                    "Error in loading graph",
                    JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            synchronized(this)
            {
                flag = false;
                jSliderDataSlider.setMinimum(0);
                jSliderDataSlider.setMaximum(stateInfo.getSession().getSegIndexLen() - 1);
                jSliderDataSlider.setValue(0);
                flag = true;
            }

            if (!stateInfo.getSession().isSegmented())
            {
                jSliderDataSlider.setVisible(false);
            }
            else
            {
                jSliderDataSlider.setVisible(true);
            }
            makeGraph();
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void jSliderDataSliderStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSliderDataSliderStateChanged
    {//GEN-HEADEREND:event_jSliderDataSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        if (!source.getValueIsAdjusting())
        {
            //System.out.println("Calling makegraph 1: " + evt);
            //makeGraph();
            filterEdges();
            refreshGraph();
        }        
}//GEN-LAST:event_jSliderDataSliderStateChanged

private void jButtonPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPositionActionPerformed
    try
    {                                               
        PositionEventTypes.setPositions(stateInfo.getSession().getEventFactor(), this.frame);
    }
    catch (Exception ex)
    {
        JOptionPane.showMessageDialog(this, "Error setting event type positions",
                "Error setting event type positions", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_jButtonPositionActionPerformed

private void jButtonSaveImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveImgActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter()
        {
            @Override
            public boolean accept(File f)
            {
                return (f.getName().toLowerCase().endsWith("png") || f.isDirectory());
            }
            @Override
            public String getDescription()
            {
                return "Portable Network Graphics format (*.png)";
            }
        });
        if (edu.iisc.tdminer.util.Constants.CURRENT_DIR != null) 
            fc.setCurrentDirectory(edu.iisc.tdminer.util.Constants.CURRENT_DIR);
        int ret = fc.showSaveDialog(this);
        edu.iisc.tdminer.util.Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            File f = fc.getSelectedFile();
            int width = 600;
            int height = 300;
            
            BufferedImage image = display.getOffscreenBuffer();
            try
            {
                if (!f.getName().toLowerCase().endsWith(".png"))
                {
                    f = new File(f.getPath() + ".png");
                }
                ImageIO.write(image,"PNG",f);
            }
            catch(IOException ioe)
            {
                jLabelStatus.setText("Error while saving event sequence plot");
                JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error while saving event sequence plot", JOptionPane.ERROR_MESSAGE);
            }
        }
}//GEN-LAST:event_jButtonSaveImgActionPerformed

    public void refreshState()
    {
    }

    @Override
    public void handleTaskCompletion(int taskIndex)
    {
    }

    void setStateInfo(StateInfo stateInfo)
    {
        this.stateInfo = stateInfo;
    }

    /**
     * Getter for property autoZoom.
     * @return Value of property autoZoom.
     */
    public boolean isAutoZoom()
    {
        return AutoZoomCheckBox.isSelected();
    }

    /**
     * Setter for property autoZoom.
     * @param autoZoom New value of property autoZoom.
     */
    public void setAutoZoom(boolean autoZoom)
    {
        AutoZoomCheckBox.setSelected(autoZoom);
    }

    public float getMinEStr()
    {
        return ((float) eStrongSlider.getValue()) / 100.f;
    }

    public boolean isAnimating()
    {
        return AnimateCheckBox.isSelected();
    }

    public int getLevel()
    {
        return levelSlider.getValue();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox AnimateCheckBox;
    private javax.swing.JCheckBox AutoZoomCheckBox;
    private javax.swing.JRadioButton EpisodeButton;
    private javax.swing.JRadioButton IntersectButton;
    private javax.swing.JCheckBox PolychronCheckBox;
    private javax.swing.JRadioButton ReferenceButton;
    private javax.swing.JRadioButton UnionButton;
    private javax.swing.JPanel displayPanel;
    private javax.swing.JPanel eStrongPanel;
    private javax.swing.JSlider eStrongSlider;
    private javax.swing.JLabel eStrongValue;
    private javax.swing.JButton jButtonPosition;
    private javax.swing.JButton jButtonSaveImg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelWindowStatus;
    private javax.swing.JPanel jPanelSlider;
    private javax.swing.JPanel jPanelWindowOptions;
    private javax.swing.JSlider jSliderDataSlider;
    private javax.swing.JSlider levelSlider;
    private javax.swing.JPanel minLevelPanel;
    private javax.swing.JPanel prefuseControlPanel;
    private javax.swing.ButtonGroup setOpButtonGroup;
    private javax.swing.JPanel setPanel;
    private javax.swing.JButton swapButton;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
    public class ConditionalForceLayout extends ForceDirectedLayout
    {

        final private float NORMAL_LENGTH = 100.f;
        final private float COND_LENGTH = 100.0f;//20.f;
        final private float NORMAL_COEF = 1E-5f;
        final private float COND_COEF = 1E-5f;//.5E-4f;
        boolean runOnce = false;
        Predicate pred;

        ConditionalForceLayout(String graph, Predicate _pred)
        {
            super(graph);
            pred = _pred;

        }

        ConditionalForceLayout(String graph, boolean bounds, boolean _runOnce, Predicate _pred)
        {
            super(graph, bounds, _runOnce);
            runOnce = _runOnce;
            pred = _pred;
        }

        @Override
        protected float getSpringLength(EdgeItem e)
        {
            boolean satisfies = pred.getBoolean(e);
            if (satisfies)
            {
                return COND_LENGTH;
            }
            else
            {
                return NORMAL_LENGTH;
            }
        }

        @Override
        protected float getSpringCoefficient(EdgeItem e)
        {
            boolean satisfies = pred.getBoolean(e);
            if (satisfies)
            {
                return COND_COEF;
            }
            else
            {
                return NORMAL_COEF;
            }
        }
    }

    private class PinnedLayout extends Layout
    {

        private boolean pinX,  pinY;
        private double xVal,  yVal;

        PinnedLayout(String group, boolean pinX, double Val)
        {
            this(group, pinX, !pinX, pinX ? Val : 0., !pinX ? Val : 0.);
        }

        PinnedLayout(String group, boolean _pinX, boolean _pinY, double _xVal, double _yVal)
        {
            super(group);
            pinX = _pinX;
            pinY = _pinY;
            xVal = _xVal;
            yVal = _yVal;
        }

        public void run(double frac)
        {
            Iterator iter = m_vis.items(m_group);
            while (iter.hasNext())
            {
                VisualItem item = (VisualItem) iter.next();
                if (pinX)
                {
                    setX(item, null, xVal);
                }
                if (pinY)
                {
                    setY(item, null, yVal);
                }
            }
        }
    }

    private class PolychronLayout extends Layout
    {

        private double xScale,  yScale;
        private double xScale2 = 10.f;

        PolychronLayout(String group)
        {
            this(group, .001, 15.);
        }

        PolychronLayout(String group, double _xScale, double _yScale)
        {
            super(group);
            xScale = _xScale;
            yScale = _yScale;
        }

        private NodeItem findMinUngraphed(HashSet<String> hs)
        {
            int min = Integer.MAX_VALUE;
            NodeItem ret = null;
            Iterator start = m_vis.items(m_group);
            for (Iterator iter = start; iter.hasNext();)
            {
                //Item item = (Item)iter.next();
                NodeItem node = (NodeItem) iter.next();
                if (min > node.getInDegree() && !hs.contains(node.getString(LABEL)))
                {
                    min = node.getInDegree();
                    ret = node;
                }
            }
            return ret;
        }

        private void graphChildren(NodeItem node, HashSet<String> hs, double parDelay)
        {
            //System.err.println("gc");
            for (Iterator i = node.outEdges(); i.hasNext();)
            {
                EdgeItem edge = (EdgeItem) i.next();
                NodeItem child = edge.getTargetItem();
                if (!hs.contains(child.getString(LABEL)))
                {
                    //System.err.println(node.getString(LABEL)+"->"+child.getString(LABEL));             
                    double delay = (double) edge.getFloat(DELAY) + parDelay;
                    setY((VisualItem) child, null, (double) node.getInt(EVTID) * yScale);
                    setX((VisualItem) child, null, delay / xScale * xScale2);
                    hs.add(child.getString(LABEL));
                    graphChildren(child, hs, delay);
                }
            }
        }

        public void run(double frac)
        {
            HashSet<String> hs = new HashSet();
            NodeItem node;

            while ((node = findMinUngraphed(hs)) != null)
            {
                //System.err.println("TOP:" + node.getString(LABEL));
                setY((VisualItem) node, null, (double) node.getInt(EVTID) * yScale);
                setX((VisualItem) node, null, 0);
                hs.add(node.getString(LABEL));
                graphChildren(node, hs, 0);
            //node.get
            }
        }
    }

    public class ZoomCenterAction extends Action
    {

        private boolean forced = false;

        ZoomCenterAction(boolean _forced)
        {
            super();
            this.forced = _forced;
        }

        ZoomCenterAction()
        {
            super();
        }

        public void run(double frac)
        {
            //System.err.println(string.valueOf(display.hashCode()));
            if (!(isAutoZoom() || this.forced))
            {
                return;
            }
            if (display == null)
            {
                //System.err.println("disp null");
                return;
            }
            if (m_vis == null)
            {
                //System.err.println("vis null");
                return;
            }
            Rectangle2D bounds = m_vis.getBounds(Visualization.ALL_ITEMS);
            GraphicsLib.expand(bounds, 50 + (int) (1 / display.getScale()));
            DisplayLib.fitViewToBounds(display, bounds, 0);
        }
    }

    private class LinearMapSizeAction extends DataSizeAction
    {

        double minOut;
        double maxOut;

        LinearMapSizeAction(String group, String field, double minOut, double maxOut)
        {
            super(group, field);
            this.minOut = minOut;
            this.maxOut = maxOut;
        }

        @Override
        protected void setup()
        {
        }

        @Override
        public double getSize(VisualItem item)
        {
            if (item.canGetInt(m_dataField))
            {
                TableEdgeItem t = (TableEdgeItem) item;
                InnerKey key = new InnerKey();
                key.src =  t.getSourceNode().getString(LABEL);
                key.tgt = t.getTargetNode().getString(LABEL);
                InnerVal val = edgemap.get(key);
                item.setFloat(ESTR, (float)val.estr);
                item.setInt(FREQ, val.count);
                item.setFloat(DELAY, (float)val.estr);

                int index = jSliderDataSlider.getValue();
                double wStartTime = stateInfo.getSession().startTime(index);
                double wEndTime = stateInfo.getSession().endTime(index);
                double value = item.getInt(m_dataField) / (wEndTime - wStartTime);
                //double size = (1.0 - Math.exp(-0.6 * value)) * (maxOut - minOut) + minOut;
                double size = value / 2.0;
                if (size > 15.0) size = 15.0;
                return size;
            }
            return minOut;
        }
    }

    public static void main(String[] args)
    {
        double value = 500;
        System.out.println(1.0 - Math.exp(-0.01 * value));
    }

    protected class MinEStrongPredicate extends AbstractPredicate
    {

        private float eps;

        MinEStrongPredicate()
        {
            super();
            eps = 0.0f;
        }

        MinEStrongPredicate(float _eps)
        {
            super();
            eps = _eps;
        }

        @Override
        public boolean getBoolean(Tuple tup)
        {
            if (tup.canGetFloat(ESTR))
            {
                float mine = tup.getFloat(ESTR);
                return mine >= (getMinEStr() + eps);
            }
            else
            {
                return false;
            }
        }
    }

    protected class NonZeroFreqPredicate extends AbstractPredicate
    {

        @Override
        public boolean getBoolean(Tuple tup)
        {
            int freq = tup.getInt(FREQ);
            return freq > 0;
        }
    }
}

