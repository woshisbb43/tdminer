/*
 * VisualizationPanel.java
 *
 * Created on March 25, 2006, 11:06 AM
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminer.util.Constants;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminer.model.VisualizationDisplayTableModel;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEpisode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.table.TableColumn;



/**
 * @author  Deb
 * @author phreed@gmail.com
 */
public class StrengthPlotPanel extends javax.swing.JPanel implements ITaskPanel
{ 
    private StateInfo stateInfo;
    private JLabel jLabelStatus;
    private VisualizationDisplayTableModel tableModel;
    private boolean resetFlag = true;
    private JFrame frame;
    private EpisodeInstanceSet prospect;
    private IEventDataStream sequence;
    private EpisodeSet episodes;
    
    private int plotVar = STRENGTH_PLOT;
    public static final int STRENGTH_PLOT = 0;
    public static final int BETA_PLOT = 1;
    
    /** Creates new form VisualizationPanel */
    public StrengthPlotPanel()
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
    }
    
    public void refreshState()
    {
        if (sequence != stateInfo.getSession().getSequence())
        {
            sequence = stateInfo.getSession().getSequence();
            resetFlag = true;
        }
        if (episodes != stateInfo.getSession().getEpisodes())
        {
            episodes = stateInfo.getSession().getEpisodes();
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
                prospect.clear();

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
               
                tableModel.fireTableDataChanged();
                resetFlag = false;
                redrawPlot();
            }
        }  
    }
    
    public void redrawPlot()
    {
        if (this.sequence == null) return;
        
        tDMinerPlot.clear(true);
        tDMinerPlot.setYRange(0, 1.0);
        tDMinerPlot.setXRange(
                this.sequence.getSequenceStart(), 
                this.sequence.getSequenceEnd());
        
        tDMinerPlot.setXLabel("Time (in sec)");
        tDMinerPlot.setYLabel("Episode Strength");
        tDMinerPlot.setMarksStyle("various");
        tDMinerPlot.setGrid(false);

        tDMinerPlot.repaint();
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
        this.frame = frame;
    }

    private void displayEpisodeStrength()
    {
        double y_max = Double.MIN_VALUE;
        boolean flag = false;
        for(int i = 0; i < prospect.sizeEpisodeList(); i++)
        {
            IEpisode e = prospect.getEpisode(i);
            e.setIndex(i);
            for(int j = 0; j < stateInfo.getSession().getSegIndexLen(); j++)
            {
                double x1 = stateInfo.getSession().startTime(j);
                double x2 = stateInfo.getSession().endTime(j);
                double y = 0.0;                
                switch(plotVar)
                {
                    case STRENGTH_PLOT:
                        y = e.getEstr(j);
                        break;
                    case BETA_PLOT:
                        y = e.getBeta(j);
                        break;
                }
                
                if (y < 1e10)
                {
                    tDMinerPlot.addPoint(i, x1, y, flag);
                    tDMinerPlot.addPoint(i, x2, y, true);
                    flag = true;
                    if (y_max < y) y_max = y;
                }
                else
                    flag = false;
            }
        }
        switch(plotVar)
        {
            case STRENGTH_PLOT:
                tDMinerPlot.setYRange(0.0, 1.0);
                break;
            case BETA_PLOT:
                tDMinerPlot.setYRange(0.0, y_max * 1.1);
                break;
        }
        tDMinerPlot.repaint();
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
        tDMinerPlot = new ptolemy.plot.Plot();
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
        jButtonPlotVar = new javax.swing.JButton();
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

        jPanelSpikeTrain.setBorder(javax.swing.BorderFactory.createTitledBorder("Episode Strength Plot"));
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

        jButtonPlotVar.setText("Select Plot Variable");
        jButtonPlotVar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlotVarActionPerformed(evt);
            }
        });
        jPanelOperationOption.add(jButtonPlotVar);

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
       
        JDialog dialog = new JDialog(frame, "Select/Add Episodes", true);
        EpisodeSelectionDialogPanel panel = new EpisodeSelectionDialogPanel(stateInfo.getSession(), dialog);
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
        prospect.addEpisodeSet(panel.getSelectedEpisodes());

        tableModel.setData(prospect);
        tableModel.fireTableDataChanged();

        // Count Episodes
        displayEpisodeStrength();
    }//GEN-LAST:event_jButtonAddEpisodesActionPerformed
                        
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
            int width = 600;
            int height = 300;
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
    prospect.clear();
    tableModel.setData(prospect);
    tableModel.fireTableDataChanged();
}//GEN-LAST:event_jButtonClearEpisodesActionPerformed

private void jButtonPlotVarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlotVarActionPerformed
// TODO add your handling code here:
    final JDialog dialog = new JDialog(this.frame, "Select Plot Variable", true);
    
    dialog.setLayout(new GridLayout(3, 1, 5, 5));
    ButtonGroup grp = new ButtonGroup();
    
    JRadioButton radio = null;
    
    radio = new JRadioButton("Conditional Probability");
    radio.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            JRadioButton radio = (JRadioButton)e.getSource();
            if (radio.isSelected()) plotVar = STRENGTH_PLOT;
        }
    });        
    grp.add(radio);    
    dialog.getContentPane().add(radio);
    if (plotVar == STRENGTH_PLOT)
        radio.setSelected(true);
    
    radio = new JRadioButton("Estimated Model Parameter(for selected model)");
    radio.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            JRadioButton radio = (JRadioButton)e.getSource();
            if (radio.isSelected()) plotVar = BETA_PLOT;
        }
    });        
    grp.add(radio);    
    dialog.getContentPane().add(radio);
    if (plotVar == BETA_PLOT)
        radio.setSelected(true);
    
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.CENTER));
    JButton btn = new JButton("Ok");
    btn.setMaximumSize(new Dimension(80, 25));
    btn.setPreferredSize(new Dimension(80, 25));
    btn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            dialog.setVisible(false);
            dialog.dispose();
        }
    });
    panel.add(btn, BorderLayout.CENTER);
    dialog.getContentPane().add(panel);
    
    dialog.setSize(300,150);
    if (frame != null)
    {
        int x = (int)(frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth())/2);
        int y = (int)(frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight())/2);
        dialog.setLocation(x,y);
    }            
    dialog.setVisible(true);
    
    tDMinerPlot.clear(false);
    displayEpisodeStrength();
}//GEN-LAST:event_jButtonPlotVarActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddEpisodes;
    private javax.swing.JButton jButtonClearEpisodes;
    private javax.swing.ButtonGroup jButtonGroupExport;
    private javax.swing.JButton jButtonHorizontalZoomIn;
    private javax.swing.JButton jButtonHorizontalZoomOut;
    private javax.swing.JButton jButtonPlotVar;
    private javax.swing.JButton jButtonRefreshPlot;
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
    private ptolemy.plot.Plot tDMinerPlot;
    // End of variables declaration//GEN-END:variables
    
}
