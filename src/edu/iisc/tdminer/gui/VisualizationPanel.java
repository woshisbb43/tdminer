/*
 * VisualizationPanel.java
 *
 * Created on March 25, 2006, 11:06 AM
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminer.model.ThreadedProgressManager;
import edu.iisc.tdminer.util.AlgoEntry;
import edu.iisc.tdminer.util.Constants;
import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.GeneralEvent;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminercore.miner.GenericMiner;
import edu.iisc.tdminer.model.VisualizationDisplayTableModel;
import edu.iisc.tdminer.util.Algorithms;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.writer.IWriter;
import edu.iisc.tdminercore.writer.XMLWriter;
import edu.iisc.tdminercore.writer.CSVWriter;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;



/**
 * @author  Deb
 * @author phreed@gmail.com
 */
public class VisualizationPanel extends javax.swing.JPanel implements ITaskPanel
{ 
    private StateInfo stateInfo;
    private JLabel jLabelStatus;
    
    private IEventDataStream sequence;
    private VisualizationDisplayTableModel tableModel;
    
    private boolean resetFlag = true;
    
    private int episodeStartIndex;
    
    
    private JFrame frame;
    
    private List<String> orderedLabels;
    private EpisodeInstanceSet prospect;
    
    private ArrayList algos = Algorithms.COUNTER_ALGOS;
    
    private GenericMiner miner;
    
    private SessionInfo vsession;
    
    private int counterIndex;
    private File exportEpisodeFile;
    
    private boolean eventsOn = true;
    EventFactor.DisplayMode displaymode = EventFactor.DisplayMode.ofName;
    EventFactor.OrderMode ordermode = EventFactor.OrderMode.byNameLexical;
      
    /** Creates new form VisualizationPanel */
    public VisualizationPanel()
    {
        initComponents();
        this.prospect = new EpisodeInstanceSet( null );
        tableModel = (VisualizationDisplayTableModel)jTableSelectedEpisodes.getModel();
        tableModel.setPlot(tDMinerPlot);
        tableModel.setData(this.prospect);
        tableModel.fireTableDataChanged();
        
        TableColumn col = jTableSelectedEpisodes.getColumnModel().getColumn(0);
        col.setMaxWidth(40);
        col = jTableSelectedEpisodes.getColumnModel().getColumn(2);
        col.setMaxWidth(120);
        miner = new GenericMiner();
    }
    
    public void refreshState()
    {
        if (sequence != stateInfo.getSession().getSequence())
        {
            sequence = stateInfo.getSession().getSequence();
            resetFlag = true;
        }
        if (sequence == null) {
            resetFlag = true;
        }
        else {
            EventFactor eventTypes = sequence.getEventFactor();
            tableModel.setEventTypes(eventTypes);
            if (resetFlag)
            {
                
                int seqSize = this.sequence.getSize();

                int minimum = -10;  // some extra on the left
                int maximum = seqSize + 10;  // some extra on the right
                int extent = seqSize / 100;  // the width of the handle
                int position = ( maximum + minimum ) / 2;
                
                this.jScrollBarHorizontal.setUnitIncrement((maximum-minimum)/1000);
                this.jScrollBarHorizontal.setBlockIncrement((maximum-minimum)/100);
                this.jScrollBarHorizontal.setValues(position, extent, minimum, maximum);
               
                minimum = 0;
                maximum = eventTypes.getSize();
                extent = 1;
                position = ( maximum + minimum ) / 2;
                this.jScrollBarVertical.setUnitIncrement(1);
                this.jScrollBarVertical.setBlockIncrement(10);
                this.jScrollBarVertical.setValues(position, extent, minimum, maximum);
               
                this.prospect.clear();
                this.prospect.setDataStream(sequence);
                tableModel.fireTableDataChanged();
                resetFlag = false;
                redrawPlot();
            }
        }  
        
        this.counterIndex = stateInfo.getCounterIndex();
        vsession = stateInfo.getSession().createCopy();
    }
    
    public void redrawPlot()
    {
        if (this.sequence == null) return;
        
        episodeStartIndex = sequence.getEventTypeCount();
        EventFactor eventTypesList = sequence.getEventFactor();

        tDMinerPlot.clear(true);
        tDMinerPlot.setYRange(0, episodeStartIndex - 1);
        tDMinerPlot.setXRange(
                this.sequence.getSequenceStart(), 
                this.sequence.getSequenceEnd());
        
        tDMinerPlot.setXLabel("Time (in sec)");
        tDMinerPlot.setYLabel("Event Types");
        tDMinerPlot.setMarksStyle("various");
        tDMinerPlot.setGrid(false);
        
        eventTypesList.setOrdinal(ordermode);
        relabel();
        
        if (eventsOn) plotEvents();
        if (prospect.sizeEpisodeList() != 0) countEpisodes();
        tDMinerPlot.repaint();
    }

    private void changeCountingAlgo()
    {
        if (sequence == null)
        {
            jLabelStatus.setText("Error loading episodes");
            JOptionPane.showMessageDialog(this, "Events data stream is empty.", "Error loading episodes", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(frame, "Parameter Selection", true);
        ParamSelectionDialogPanel panel = new ParamSelectionDialogPanel(dialog, vsession, counterIndex, algos);
        dialog.getContentPane().add(panel);
        dialog.setSize(500, 160);
        if (frame != null)
        {
            int x = (int) (frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth())/2);
            int y = (int) (frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight())/2);
            dialog.setLocation(x, y);
        }
        dialog.setVisible(true);
        //System.out.println("Parameter selection done");
        this.counterIndex = panel.getCounterIndex();

        // Count Episodes
        countEpisodes();
    }
    
    private void plotEvents() 
    {
        if (sequence != null)
        {
            EventFactor factor = sequence.getEventFactor();

            for(IEvent event : sequence.iterable(null)) {
                int eventType = event.getEventType();
                EventFactor.EventType type = factor.get(eventType);
                double time = event.getStartTime();

                int eventTypeIndex = type.ordinal;
                if (eventTypeIndex == -1) {
                    throw new RuntimeException("Horrible index mismatch !!!");
                }
                tDMinerPlot.addPoint(eventTypeIndex, time, type.ordinal, false);
                if (!(event instanceof GeneralEvent)) continue;

                double endtime = event.getEndTime();
                tDMinerPlot.addPoint(eventTypeIndex, endtime, type.ordinal, true);          
            }
            tDMinerPlot.fillPlot();
        }
    }
    
    public void handleTaskCompletion(int taskIndex)
    {
        //ADD: Code for handling adding of datasets to the plot goes here
        tableModel.fireTableDataChanged();
    }
    
    public StateInfo getStateInfo()
    {
        return stateInfo;
    }
    
    public void setStateInfo(StateInfo stateInfo)
    {
        this.stateInfo = stateInfo;
    }
    
    public JLabel getJLabelStatus()
    {
        return jLabelStatus;
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
        if (!(frame instanceof ParentMenu)) {
             System.out.println("Not a Parent Menu");
        }
        else {
            ParentMenu parentmenu = (ParentMenu) frame;
            this.initFileMenuComponents(parentmenu.getExportFileMenu());
            
            this.initSettingsMenuComponents(parentmenu.getVisualizationSettingsMenu());
        }
        this.frame = frame;
    }
    
    public enum Toggle {on, off};
    public enum ExportMode { none, xml, csv, matlab};
         
    private void countEpisodes()
    {
        AbstractEpisodeCounter counter = null;
        Object objEntry = this.algos.get(counterIndex);
        if (!(objEntry instanceof AlgoEntry)) return;
        
        AlgoEntry algoEntry = (AlgoEntry)objEntry;
        for (int ix = 0; ix < this.prospect.sizeEpisodeList(); ix++)
        {
            int dataSet = this.prospect.getEpisode(ix).getIndex();
            if (dataSet >= 0)
                tDMinerPlot.clear(dataSet);
        }

        counter = algoEntry.getCounter();
        if (counter == null)
        {
            JOptionPane.showMessageDialog(frame, "Please select a counter first", 
                    "Visualization Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        vsession.setTrackEpisodes(true);
        vsession.setCounter(counter);
       
        final int startingEpisodeDataset = sequence.getEventTypeCount();
        
        stateInfo.setHarvest(this.prospect);
        
        miner.countListEpisodesThread(this.prospect.getEpisodeList(), 
                // Similar in function to the Gleaner class.
                new ThreadedProgressManager(this.frame, this)
        {
             Toggle plotMode = Toggle.on;
             Toggle prospectMode = Toggle.off;
             
             public void startup() {
                if ( jCheckBoxMenuItemMakePlot.isSelected() ) 
                    plotMode = Toggle.on;
                if ( jCheckBoxMenuItemHarvestEpisode.isSelected() ) 
                    prospectMode = Toggle.on;
                
            }

             public void shutdown() {
                 System.out.println("Finished prospecting episode instances"); 
            }    

            // This method should probably also write out the algorithm used.
            @Override
            public void handleEpisodeCompletion(int episodeIx, int[] eventTypes, List<IEvent> events)
                throws edu.iisc.tdminercore.util.IObserver.NotImplementedException
            {
                EventFactor factor = sequence.getEventFactor();
                int dataSetIndex = startingEpisodeDataset + episodeIx + 1;
                VisualizationPanel.this.prospect.getEpisode(episodeIx).setIndex(dataSetIndex);
                
                if (events.size() < eventTypes.length) 
                        return;
                 
                switch (plotMode) {
                    case on:
                    tDMinerPlot.setMarksStyle("none", dataSetIndex);

                    for (int ix = 0; ix < eventTypes.length; ix++) {
                        EventFactor.EventType type = factor.get(eventTypes[ix]);

                        tDMinerPlot.addPoint(
                                dataSetIndex, 
                                events.get(ix).getStartTime(), 
                                type.ordinal, 
                                (ix == 0 ? false:true) );
                    }
                    break;
                }
                switch (prospectMode) {
                    case on:
                    if (eventTypes.length < 1) break;

                    EpisodeInstanceSet prospect = stateInfo.getHarvest();
                    prospect.addInstance(episodeIx, eventTypes, events);
                }
                this.markEvents(events);
            }
        }, vsession);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButtonGroupExport = new javax.swing.ButtonGroup();
        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemSavePlot = new javax.swing.JMenuItem();
        jPanelSpikeTrain = new javax.swing.JPanel();
        jPanelVerticalMovement = new javax.swing.JPanel();
        jButtonVerticalZoomIn = new javax.swing.JButton();
        jScrollBarVertical = new javax.swing.JScrollBar();
        jButtonVerticalZoomOut = new javax.swing.JButton();
        tDMinerPlot = new edu.iisc.tdminer.util.TDMinerPlot();
        jButtonShowLegend = new javax.swing.JButton();
        jButtonRefreshPlot = new javax.swing.JButton();
        jPanelHorizontalMovement = new javax.swing.JPanel();
        jButtonHorizontalZoomOut = new javax.swing.JButton();
        jScrollBarHorizontal = new javax.swing.JScrollBar();
        jButtonHorizontalZoomIn = new javax.swing.JButton();
        jPanelBottom = new javax.swing.JPanel();
        jPanelEpisodeDisplay = new javax.swing.JPanel();
        jScrollPaneEpisodeDisplay = new javax.swing.JScrollPane();
        jTableSelectedEpisodes = new javax.swing.JTable();
        jPanelOperations = new javax.swing.JPanel();
        jPanelOperationOption = new javax.swing.JPanel();
        jButtonAddEpisodes = new javax.swing.JButton();
        jButtonClearEpisodes = new javax.swing.JButton();

        jMenuItemSavePlot.setText("Save Plot");
        jMenuItemSavePlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSavePlotActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSavePlot);

        setLayout(new java.awt.BorderLayout());

        jPanelSpikeTrain.setBorder(javax.swing.BorderFactory.createTitledBorder("Event Sequence Plot"));
        jPanelSpikeTrain.setLayout(new java.awt.GridBagLayout());

        jPanelVerticalMovement.setMaximumSize(new java.awt.Dimension(21, 2147483647));
        jPanelVerticalMovement.setMinimumSize(new java.awt.Dimension(21, 48));
        jPanelVerticalMovement.setPreferredSize(new java.awt.Dimension(21, 91));
        jPanelVerticalMovement.setLayout(new java.awt.BorderLayout(0, 1));

        jButtonVerticalZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom-in.gif"))); // NOI18N
        jButtonVerticalZoomIn.setToolTipText("Vertical Zoom In");
        jButtonVerticalZoomIn.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonVerticalZoomIn.setMaximumSize(new java.awt.Dimension(20, 20));
        jButtonVerticalZoomIn.setMinimumSize(new java.awt.Dimension(20, 20));
        jButtonVerticalZoomIn.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonVerticalZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVerticalZoomInActionPerformed(evt);
            }
        });
        jPanelVerticalMovement.add(jButtonVerticalZoomIn, java.awt.BorderLayout.NORTH);

        jScrollBarVertical.setPreferredSize(new java.awt.Dimension(15, 48));
        jScrollBarVertical.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarVerticalAdjustmentValueChanged(evt);
            }
        });
        jPanelVerticalMovement.add(jScrollBarVertical, java.awt.BorderLayout.CENTER);

        jButtonVerticalZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom-out.gif"))); // NOI18N
        jButtonVerticalZoomOut.setToolTipText("Vertical Zoom Out");
        jButtonVerticalZoomOut.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonVerticalZoomOut.setMaximumSize(new java.awt.Dimension(22, 21));
        jButtonVerticalZoomOut.setMinimumSize(new java.awt.Dimension(22, 21));
        jButtonVerticalZoomOut.setPreferredSize(new java.awt.Dimension(22, 21));
        jButtonVerticalZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVerticalZoomOutActionPerformed(evt);
            }
        });
        jPanelVerticalMovement.add(jButtonVerticalZoomOut, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanelSpikeTrain.add(jPanelVerticalMovement, gridBagConstraints);

        tDMinerPlot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tDMinerPlotMouseClicked(evt);
            }
        });
        tDMinerPlot.setLayout(new java.awt.GridBagLayout());

        jButtonShowLegend.setText("Legend");
        jButtonShowLegend.setToolTipText("Graph Legend");
        jButtonShowLegend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonShowLegendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tDMinerPlot.add(jButtonShowLegend, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 40, 0);
        jPanelSpikeTrain.add(tDMinerPlot, gridBagConstraints);

        jButtonRefreshPlot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/refresh.gif"))); // NOI18N
        jButtonRefreshPlot.setToolTipText("Refresh Plot");
        jButtonRefreshPlot.setIconTextGap(0);
        jButtonRefreshPlot.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonRefreshPlot.setMaximumSize(new java.awt.Dimension(20, 20));
        jButtonRefreshPlot.setMinimumSize(new java.awt.Dimension(20, 20));
        jButtonRefreshPlot.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonRefreshPlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshPlotActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelSpikeTrain.add(jButtonRefreshPlot, gridBagConstraints);

        jPanelHorizontalMovement.setLayout(new java.awt.BorderLayout());

        jButtonHorizontalZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom-out.gif"))); // NOI18N
        jButtonHorizontalZoomOut.setToolTipText("Horizontal Zoom Out ");
        jButtonHorizontalZoomOut.setIconTextGap(0);
        jButtonHorizontalZoomOut.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonHorizontalZoomOut.setMaximumSize(new java.awt.Dimension(22, 21));
        jButtonHorizontalZoomOut.setMinimumSize(new java.awt.Dimension(22, 21));
        jButtonHorizontalZoomOut.setPreferredSize(new java.awt.Dimension(22, 21));
        jButtonHorizontalZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHorizontalZoomOutActionPerformed(evt);
            }
        });
        jPanelHorizontalMovement.add(jButtonHorizontalZoomOut, java.awt.BorderLayout.WEST);

        jScrollBarHorizontal.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarHorizontal.setPreferredSize(new java.awt.Dimension(48, 21));
        jScrollBarHorizontal.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarHorizontalAdjustmentValueChanged(evt);
            }
        });
        jPanelHorizontalMovement.add(jScrollBarHorizontal, java.awt.BorderLayout.CENTER);

        jButtonHorizontalZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom-in.gif"))); // NOI18N
        jButtonHorizontalZoomIn.setToolTipText("Horizonatal Zoom In");
        jButtonHorizontalZoomIn.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonHorizontalZoomIn.setMaximumSize(new java.awt.Dimension(22, 21));
        jButtonHorizontalZoomIn.setMinimumSize(new java.awt.Dimension(22, 21));
        jButtonHorizontalZoomIn.setPreferredSize(new java.awt.Dimension(22, 21));
        jButtonHorizontalZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHorizontalZoomInActionPerformed(evt);
            }
        });
        jPanelHorizontalMovement.add(jButtonHorizontalZoomIn, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        jPanelSpikeTrain.add(jPanelHorizontalMovement, gridBagConstraints);

        add(jPanelSpikeTrain, java.awt.BorderLayout.CENTER);

        jPanelBottom.setLayout(new java.awt.BorderLayout());

        jPanelEpisodeDisplay.setBorder(javax.swing.BorderFactory.createTitledBorder("Episodes displayed"));
        jPanelEpisodeDisplay.setLayout(new java.awt.BorderLayout());

        jScrollPaneEpisodeDisplay.setPreferredSize(new java.awt.Dimension(300, 80));

        jTableSelectedEpisodes.setModel(new VisualizationDisplayTableModel());
        jScrollPaneEpisodeDisplay.setViewportView(jTableSelectedEpisodes);

        jPanelEpisodeDisplay.add(jScrollPaneEpisodeDisplay, java.awt.BorderLayout.CENTER);

        jPanelBottom.add(jPanelEpisodeDisplay, java.awt.BorderLayout.CENTER);

        jPanelOperations.setLayout(new java.awt.GridBagLayout());

        jPanelOperationOption.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Operations"), javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        jPanelOperationOption.setLayout(new java.awt.GridLayout(3, 1, 0, 5));

        jButtonAddEpisodes.setText("Add/Select Episode");
        jButtonAddEpisodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddEpisodesActionPerformed(evt);
            }
        });
        jPanelOperationOption.add(jButtonAddEpisodes);

        jButtonClearEpisodes.setText("Clear Episodes");
        jButtonClearEpisodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearEpisodesActionPerformed(evt);
            }
        });
        jPanelOperationOption.add(jButtonClearEpisodes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanelOperations.add(jPanelOperationOption, gridBagConstraints);

        jPanelBottom.add(jPanelOperations, java.awt.BorderLayout.WEST);

        add(jPanelBottom, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonRefreshPlotActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRefreshPlotActionPerformed
    {//GEN-HEADEREND:event_jButtonRefreshPlotActionPerformed
// TODO add your handling code here:
        tDMinerPlot.fillPlot();
    }//GEN-LAST:event_jButtonRefreshPlotActionPerformed

    private void jButtonVerticalZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVerticalZoomOutActionPerformed
        // adjust the screen
        double[] aRange = tDMinerPlot.getYRange();
        double midRange = (aRange[1] + aRange[0]) / 2.0;
        double newHalfRange = (aRange[1] - aRange[0]);  // div by 2 mult by 2
        tDMinerPlot.setYRange(midRange-newHalfRange, midRange+newHalfRange);
        tDMinerPlot.repaint();
    }//GEN-LAST:event_jButtonVerticalZoomOutActionPerformed

    private void jButtonVerticalZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVerticalZoomInActionPerformed
        // adjust the screen
        double[] aRange = tDMinerPlot.getYRange();
        double midRange = (aRange[1] + aRange[0]) / 2.0;
        double newHalfRange = (aRange[1] - aRange[0]) / 4.0; // mult by 2 mult by 2
        tDMinerPlot.setYRange(midRange-newHalfRange, midRange+newHalfRange);
        tDMinerPlot.repaint();
    }//GEN-LAST:event_jButtonVerticalZoomInActionPerformed

    private void jButtonHorizontalZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHorizontalZoomOutActionPerformed
        // adjust the screen
        double[] xRange = tDMinerPlot.getXRange();
        double midRange = (xRange[1] + xRange[0]) / 2.0;
        double newHalfRange = (xRange[1] - xRange[0]);
        tDMinerPlot.setXRange(midRange-newHalfRange, midRange+newHalfRange);
        tDMinerPlot.repaint();
 
        // adjust the scrollbar
        int newUnitIncrement = this.jScrollBarHorizontal.getUnitIncrement();
        newUnitIncrement *= 2;
        this.jScrollBarHorizontal.setUnitIncrement(newUnitIncrement);
             
        int newBlockIncrement = this.jScrollBarHorizontal.getBlockIncrement();
        newBlockIncrement *= 2;
        this.jScrollBarHorizontal.setBlockIncrement(newBlockIncrement);
    }//GEN-LAST:event_jButtonHorizontalZoomOutActionPerformed

    private void jScrollBarVerticalAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarVerticalAdjustmentValueChanged
        // preconditions
        if (evt.getValueIsAdjusting()) return;
        
        java.awt.Adjustable scroller = evt.getAdjustable();
        int scrollBegin = scroller.getMinimum();
        int scrollEnd = scroller.getMaximum();
        int scrollPos = evt.getValue();
        double newPercent = (double)(scrollPos - scrollBegin) / (double)(scrollEnd - scrollBegin);
        
        double[] yRange = tDMinerPlot.getYRange();
        double midRange = (yRange[1] + yRange[0]) / 2.0;
        double halfRange =  (yRange[1] - yRange[0]) / 2.0;
        
        double newMidRange = midRange;
        if (sequence == null) {
            newMidRange = midRange + newPercent * halfRange;
        }
        else {
            double typeBegin = 0.0;
            double typeEnd = sequence.getEventTypeCount();
            newMidRange = typeBegin + (newPercent * (typeEnd-typeBegin));
        }
        
        tDMinerPlot.setYRange(newMidRange-halfRange, newMidRange+halfRange);
        tDMinerPlot.repaint();
    }//GEN-LAST:event_jScrollBarVerticalAdjustmentValueChanged

    private void jScrollBarHorizontalAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarHorizontalAdjustmentValueChanged
        // preconditions
        if (evt.getValueIsAdjusting()) return;
        
        java.awt.Adjustable scroller = evt.getAdjustable();
        int scrollBegin = scroller.getMinimum();
        int scrollEnd = scroller.getMaximum();
        int scrollPos = evt.getValue();
        double newPercent = (double)(scrollPos - scrollBegin) / (double)(scrollEnd - scrollBegin);
        
        double[] xRange = tDMinerPlot.getXRange();
        double midRange = (xRange[1] + xRange[0]) / 2.0;
        double halfRange =  (xRange[1] - xRange[0]) / 2.0;
        
        double newMidRange = midRange;
        if (sequence == null) {
            newMidRange = midRange + newPercent * halfRange;
        }
        else {
            double seqBegin = sequence.getSequenceStart();
            double seqEnd = sequence.getSequenceEnd();
            newMidRange = seqBegin + (newPercent * (seqEnd-seqBegin));
        }
        
        tDMinerPlot.setXRange(newMidRange-halfRange, newMidRange+halfRange);
        tDMinerPlot.repaint();
    }//GEN-LAST:event_jScrollBarHorizontalAdjustmentValueChanged


    private void tDMinerPlotMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_tDMinerPlotMouseClicked
    {//GEN-HEADEREND:event_tDMinerPlotMouseClicked
        
            System.out.println("Click : " + evt.getButton());
            if (evt.getButton() == MouseEvent.BUTTON3)
            {
                jPopupMenu.show(tDMinerPlot, evt.getX(), evt.getY());
            }
    }//GEN-LAST:event_tDMinerPlotMouseClicked
        
    private void jButtonAddEpisodesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddEpisodesActionPerformed
    {//GEN-HEADEREND:event_jButtonAddEpisodesActionPerformed
        if (sequence == null) {   
            jLabelStatus.setText("Error loading episodes");
            JOptionPane.showMessageDialog(this, "Events data stream is empty.", "Error loading episodes", JOptionPane.ERROR_MESSAGE);
            return;
        }
        AlgoEntry algoEntry = (AlgoEntry) this.algos.get(counterIndex);
        AbstractEpisodeCounter counter = algoEntry.getCounter();
        if (counter == null)
        {
            JOptionPane.showMessageDialog(frame, "Please select a counter first", 
                    "Visualization Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        vsession.setTrackEpisodes(true);
        vsession.setCounter(counter);

        JDialog dialog = new JDialog(frame, "Select/Add Episodes", true);
        EpisodeSelectionDialogPanel panel = new EpisodeSelectionDialogPanel(vsession, dialog);
        dialog.getContentPane().add(panel);
        dialog.setSize(600,500);
        if (frame != null)
        {
            int x = (int)(frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth())/2);
            int y = (int)(frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight())/2);
            dialog.setLocation(x,y);
        }            
        dialog.setVisible(true);
        //System.out.println("Episode selection done");
        prospect.clearInstances();
        prospect.resetMarkers();
        prospect.addEpisodeSet(panel.getSelectedEpisodes());
        tableModel.setData(prospect);
        tableModel.fireTableDataChanged();

        // Count Episodes
        countEpisodes();
    }//GEN-LAST:event_jButtonAddEpisodesActionPerformed
    
    private void jButtonShowLegendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonShowLegendActionPerformed
        if (orderedLabels == null) return;
        
        JDialog dialog = new JDialog(frame, "Event Types", false);
        EventTypeDispDialog dialogPanel = new EventTypeDispDialog();
        dialogPanel.setValuesInList(orderedLabels);
        dialog.getContentPane().add(dialogPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(200,400);
        if (frame != null)
        {
            int x = (int)(frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth())/2);
            int y = (int)(frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight())/2);
            dialog.setLocation(x,y);
        }            
        dialog.setVisible(true);
    }//GEN-LAST:event_jButtonShowLegendActionPerformed
                    
    private void jButtonHorizontalZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHorizontalZoomInActionPerformed
        // adjust the screen
        double[] xRange = tDMinerPlot.getXRange();
        double midRange = (xRange[1] + xRange[0]) / 2.0;
        double newHalfRange = (xRange[1] - xRange[0]) / 4.0;
        tDMinerPlot.setXRange(midRange-newHalfRange, midRange+newHalfRange);
        tDMinerPlot.repaint();
 
        // adjust the scrollbar
        int newUnitIncrement = this.jScrollBarHorizontal.getUnitIncrement();
        newUnitIncrement /= 2;
        this.jScrollBarHorizontal.setUnitIncrement(newUnitIncrement);
             
        int newBlockIncrement = this.jScrollBarHorizontal.getBlockIncrement();
        newBlockIncrement /= 2;
        this.jScrollBarHorizontal.setBlockIncrement(newBlockIncrement);
    }//GEN-LAST:event_jButtonHorizontalZoomInActionPerformed
    
    private void jMenuItemSavePlotActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSavePlotActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSavePlotActionPerformed
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
        if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
        int ret = fc.showSaveDialog(this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            File f = fc.getSelectedFile();
            int width = 1600;
            int height = 800;
            BufferedImage image = tDMinerPlot.exportImage(new Rectangle(width, height));
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
    }//GEN-LAST:event_jMenuItemSavePlotActionPerformed

private void jButtonClearEpisodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearEpisodesActionPerformed
        
    for(int row = 0; row < prospect.getEpisodeList().size(); row ++)
    {
        IEpisode e = prospect.getEpisodeList().get(row);
        if (tDMinerPlot != null && e.getIndex() != -1)
        {
            tDMinerPlot.clear(e.getIndex());
        }
    }   
    
    prospect.clearInstances();
    prospect.resetMarkers();
    prospect.clear();
    tableModel.setData(prospect);
    tableModel.fireTableDataChanged();    
}//GEN-LAST:event_jButtonClearEpisodesActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddEpisodes;
    private javax.swing.JButton jButtonClearEpisodes;
    private javax.swing.ButtonGroup jButtonGroupExport;
    private javax.swing.JButton jButtonHorizontalZoomIn;
    private javax.swing.JButton jButtonHorizontalZoomOut;
    private javax.swing.JButton jButtonRefreshPlot;
    private javax.swing.JButton jButtonShowLegend;
    private javax.swing.JButton jButtonVerticalZoomIn;
    private javax.swing.JButton jButtonVerticalZoomOut;
    private javax.swing.JMenuItem jMenuItemSavePlot;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelEpisodeDisplay;
    private javax.swing.JPanel jPanelHorizontalMovement;
    private javax.swing.JPanel jPanelOperationOption;
    private javax.swing.JPanel jPanelOperations;
    private javax.swing.JPanel jPanelSpikeTrain;
    private javax.swing.JPanel jPanelVerticalMovement;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollBar jScrollBarHorizontal;
    private javax.swing.JScrollBar jScrollBarVertical;
    private javax.swing.JScrollPane jScrollPaneEpisodeDisplay;
    private javax.swing.JTable jTableSelectedEpisodes;
    private edu.iisc.tdminer.util.TDMinerPlot tDMinerPlot;
    // End of variables declaration//GEN-END:variables
    
    // Variables used in the settings menu
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemHarvestEpisode;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemMakePlot;
    
    private javax.swing.JSeparator jSeparatorFactorLabel;
    private javax.swing.ButtonGroup buttonGroupFactorLabel;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemNameLabel;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemOrdinalLabel;
    
    private javax.swing.JSeparator jSeparatorFactorOrdinal;
    private javax.swing.ButtonGroup buttonGroupFactorOrdinal;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemNameOrder;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemExtractOrder;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemVoteOrder;
    
    private javax.swing.JSeparator jSeparatorEventVisibility;
    private javax.swing.ButtonGroup buttonGroupEventVisibility;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemEventVisibilityOn;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemEventVisibilityOff;
    
    /** 
     * This method connects a Visualization Settings specific menu to 
     * the parent frame.
     * @param jMenuBase the menu to which this menu will be attached.
     */
    private void initSettingsMenuComponents(javax.swing.JMenu jMenuBase) {
        
        jCheckBoxMenuItemMakePlot = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemHarvestEpisode = new javax.swing.JCheckBoxMenuItem();
        
        jCheckBoxMenuItemMakePlot.setSelected(true);
        jCheckBoxMenuItemMakePlot.setText("Make Plot");
        jMenuBase.add(jCheckBoxMenuItemMakePlot);

        jCheckBoxMenuItemHarvestEpisode.setSelected(true);
        jCheckBoxMenuItemHarvestEpisode.setText("Harvest Episodes");
        jMenuBase.add(jCheckBoxMenuItemHarvestEpisode);

       //=====================
    
        jSeparatorFactorLabel = new javax.swing.JSeparator();
        jSeparatorFactorLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Factor Ordinal"));
        jMenuBase.add(jSeparatorFactorLabel);
        
        buttonGroupFactorLabel = new javax.swing.ButtonGroup();
        
        jRadioButtonMenuItemNameLabel = new javax.swing.JRadioButtonMenuItem();
        buttonGroupFactorLabel.add(jRadioButtonMenuItemNameLabel);
        jRadioButtonMenuItemNameLabel.setText("Label by Name");
        jRadioButtonMenuItemNameLabel.setSelected(true);
        jMenuBase.add(jRadioButtonMenuItemNameLabel);
        jRadioButtonMenuItemNameLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displaymode = EventFactor.DisplayMode.ofName;
                relabel();
            }
        });
        
        jRadioButtonMenuItemOrdinalLabel = new javax.swing.JRadioButtonMenuItem();
        buttonGroupFactorLabel.add(jRadioButtonMenuItemOrdinalLabel);
        jRadioButtonMenuItemOrdinalLabel.setText("Label by Ordinal");
        jMenuBase.add(jRadioButtonMenuItemOrdinalLabel);
        jRadioButtonMenuItemOrdinalLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displaymode = EventFactor.DisplayMode.ofOrdinal;
                relabel();
            }
        });
        
        //=====================
    
        jSeparatorEventVisibility = new javax.swing.JSeparator();
        jSeparatorEventVisibility.setBorder(javax.swing.BorderFactory.createTitledBorder("Event Visibility"));
        jMenuBase.add(jSeparatorEventVisibility);
        
        buttonGroupEventVisibility = new javax.swing.ButtonGroup();
        
        jRadioButtonMenuItemEventVisibilityOn = new javax.swing.JRadioButtonMenuItem();
        buttonGroupEventVisibility.add(jRadioButtonMenuItemEventVisibilityOn);
        jRadioButtonMenuItemEventVisibilityOn.setText("Show Event On");
        jRadioButtonMenuItemEventVisibilityOn.setSelected(true);
        jMenuBase.add(jRadioButtonMenuItemEventVisibilityOn);
        jRadioButtonMenuItemEventVisibilityOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventsOn = true;
                redrawPlot();
                //VisualizationPanel.this.plotEvents();
            }
        });
        
        jRadioButtonMenuItemEventVisibilityOff = new javax.swing.JRadioButtonMenuItem();
        buttonGroupEventVisibility.add(jRadioButtonMenuItemEventVisibilityOff);
        jRadioButtonMenuItemEventVisibilityOff.setText("Show Event Off");
        jMenuBase.add(jRadioButtonMenuItemEventVisibilityOff);
        jRadioButtonMenuItemEventVisibilityOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventsOn = false;
                redrawPlot();
                //tDMinerPlot.clear(true);
            }
        });
        
    }
        
     private void relabel()
     {
         System.out.println("RELABEL");
        if (sequence == null) return;
        EventFactor factor = sequence.getEventFactor();
        List<EventFactor.OrderedLabel> orderedLabelSet = factor.getLabels(this.displaymode);
        int ordinalCount = orderedLabelSet.size();

        Vector[] vlist = tDMinerPlot.getYTicks();
        if (vlist != null)
        {
            for(Vector v : vlist) v.clear();
        }
        
        int ordinalStep = ordinalCount / 10;
        if (ordinalStep == 0) ordinalStep = 1;
        for(int ix = 0; ix < ordinalCount; ix += ordinalStep)
        {
             String label = orderedLabelSet.get(ix).text;
             int coordinate = orderedLabelSet.get(ix).ordinate;
             tDMinerPlot.addYTick(label, coordinate);
             //System.out.println("label = " + label);
        }
        
        switch (this.displaymode)
        {
            case ofName:
                List<String> wipLabels = new ArrayList<String>();
                for( EventFactor.OrderedLabel label : orderedLabelSet ) {
                    wipLabels.add(label.text);
                }
                this.orderedLabels = wipLabels;
                break;
            case ofOrdinal:
                this.orderedLabels = null;
                break;
        }
        tDMinerPlot.repaint();
     }
        
    private javax.swing.JSeparator jSeparatorExportFile;
    private javax.swing.ButtonGroup buttonGroupExportMode;
    private javax.swing.JMenuItem jRadioButtonMenuItemXmlExport;
    private javax.swing.JMenuItem jRadioButtonMenuItemCsvExport;
    private javax.swing.JMenuItem jRadioButtonMenuItemMatlabExport;
    
    private void initFileMenuComponents(javax.swing.JMenu jMenuBase) {
        //=====================
        jSeparatorExportFile = new javax.swing.JSeparator();
        jSeparatorExportFile.setBorder(javax.swing.BorderFactory.createTitledBorder("Export File"));
        jMenuBase.add(jSeparatorExportFile);
        
        buttonGroupExportMode = new javax.swing.ButtonGroup();
       
        jRadioButtonMenuItemXmlExport = new javax.swing.JMenuItem();
        buttonGroupExportMode.add(jRadioButtonMenuItemXmlExport);
        jRadioButtonMenuItemXmlExport.setText("Export XML");
        jMenuBase.add(jRadioButtonMenuItemXmlExport);
        jRadioButtonMenuItemXmlExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportHarvestAsFile(evt, ExportMode.xml);
            }
        });

        jRadioButtonMenuItemCsvExport = new javax.swing.JMenuItem();
        buttonGroupExportMode.add(jRadioButtonMenuItemCsvExport);
        jRadioButtonMenuItemCsvExport.setText("Export CSV");
        jMenuBase.add(jRadioButtonMenuItemCsvExport);
        jRadioButtonMenuItemCsvExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportHarvestAsFile(evt, ExportMode.csv);
            }
        });

        
        jRadioButtonMenuItemMatlabExport = new javax.swing.JMenuItem();
        buttonGroupExportMode.add(jRadioButtonMenuItemMatlabExport);
        jRadioButtonMenuItemMatlabExport.setText("Export Matlab");
        jMenuBase.add(jRadioButtonMenuItemMatlabExport);
        jRadioButtonMenuItemMatlabExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportHarvestAsFile(evt, ExportMode.matlab);
            }
        });
    }
    
    
    private void exportHarvestAsFile(java.awt.event.ActionEvent evt, ExportMode exportMode )                                                 
    {    
        JFileChooser fc = new JFileChooser();
        if (exportEpisodeFile == null) 
             fc.setCurrentDirectory(new File("."));
        else fc.setSelectedFile(exportEpisodeFile);

        fc.setDialogTitle ("Open Export File");

        // Choose only files, not directories
        fc.setFileSelectionMode ( JFileChooser.FILES_ONLY);

        switch ( fc.showSaveDialog(VisualizationPanel.this) ) {
            case JFileChooser.APPROVE_OPTION:
                System.out.println("Open file.");
                break;
            default:
                System.out.println("Open cancelled by user.");
                return;
        };

        exportEpisodeFile = fc.getSelectedFile();
        try {
            IWriter writer = null;
            switch (exportMode) {
                case none:
                    break;
                case xml: {
                    writer = new XMLWriter(exportEpisodeFile.toString());        
                }
                    break;
                case csv: {
                    writer = new CSVWriter(exportEpisodeFile.toString());
                }
                    break;
                case matlab: {
                    
                    throw new Exception("MatlabWriter no longer supported");
                    //writer = new MatlabWriter(exportEpisodeFile.toString());
                }
                    //break;
            }
            if (writer.write(this.prospect)) 
                System.out.println("episode instances written");
            else if (writer.write(stateInfo.getSession().getEpisodes()))
                System.out.println("episode signatures written");
            else
                System.out.println("no episode information to write");
            
        }
        catch (NullPointerException ex) {
            System.out.println(
                  "could not write to file: "+exportEpisodeFile.toString() 
                + " because " + ex.getMessage() );
            ex.printStackTrace();
            return;
        }
        catch (Exception ex) {
            System.out.println(
                  "could not write to file: "+exportEpisodeFile.toString() 
                + " because " + ex.getMessage() );
            ex.printStackTrace();
            return;
        }
        System.out.println("Opening export file: " + exportEpisodeFile.getName() + ".");     
    }
}
