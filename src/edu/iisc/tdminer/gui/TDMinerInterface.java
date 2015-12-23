/*
 * TDMinerInterface.java
 *
 * Created on March 16, 2006, 7:28 PM
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminer.model.ThreadedProgressManager;
import edu.iisc.tdminer.util.AlgoEntry;
import edu.iisc.tdminer.util.Algorithms;
import edu.iisc.tdminer.util.Constants;

import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEvent;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.miner.GenericMiner;
import edu.iisc.tdminercore.util.Crosscorrelogram;
import edu.iisc.tdminercore.util.EventStreamWriter;
import edu.iisc.tdminercore.util.IObserver;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.iisc.tdminer.util.MatlabEngine;
import edu.iisc.tdminer.util.PrintUtilities;
import edu.iisc.tdminercore.miner.SessionInfo;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.prefs.Preferences;

/**
 * This is a singleton object.
 * It is so because when used as a standalone application there is no
 * need for more than one top level object.
 * More importantly when it is started from other applications, such
 * as Matlab, it is important to get the same application each time.
 *
 * @author  Deb
 * @author phreed@gmail.com
 */
public class TDMinerInterface extends javax.swing.JFrame
        implements ParentMenu {

    private static final long serialVersionUID = -4286583342347587300L;
    // Make a singleton
    private static TDMinerInterface that;
    
    public static TDMinerInterface getSingleton() {
        getSingleton("");
        return that;
    }
    public static TDMinerInterface getSingleton(String mode) {
        if (that == null) that = new TDMinerInterface(mode);
        return that;
    }
    
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    private StateInfo stateInfo = new StateInfo();
    private static boolean isMatlab = false;
    MatlabEngine engine = null;
    
    /** Creates new form TDMinerInterface */
    private TDMinerInterface() {
        this(""); }
    
    private TDMinerInterface(String mode) {
        try {
            initComponents();
            eventSeqLoaderPanel.setJLabelStatus(jLabelStatus);
            eventSeqLoaderPanel.setStateInfo(stateInfo);
            eventSeqLoaderPanel.setFrame(this);

            frequentEpisodeMinerPanel.setJLabelStatus(jLabelStatus);
            frequentEpisodeMinerPanel.setStateInfo(stateInfo);
            frequentEpisodeMinerPanel.setFrame(this);

            visualizationPanel.setJLabelStatus(jLabelStatus);
            visualizationPanel.setStateInfo(stateInfo);
            visualizationPanel.setFrame(this);
            
            strengthPlotPanel.setJLabelStatus(jLabelStatus);
            strengthPlotPanel.setStateInfo(stateInfo);
            strengthPlotPanel.setFrame(this);

//            jGraphPanel.setJLabelStatus(jLabelStatus);
//            jGraphPanel.setStateInfo(stateInfo);
//            jGraphPanel.setFrame(this);

            simulatorsMainPanel.setJLabelStatus(jLabelStatus);
            simulatorsMainPanel.setStateInfo(stateInfo);
            simulatorsMainPanel.setFrame(this);

            prefusePanel.setJLabelStatus(jLabelStatus);
            prefusePanel.setStateInfo(stateInfo);
            prefusePanel.setFrame(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("could not load menu: " + ex.getMessage());  // + " stack: " + ex.getStackTrace());
        }
        
        // Register a change listener
        jTabbedPane.addChangeListener(new ChangeListener() {
            // This method is called whenever the selected tab changes
            public void stateChanged(ChangeEvent evt) {
                JTabbedPane pane = (JTabbedPane)evt.getSource();
                
                ((ITaskPanel)pane.getSelectedComponent()).refreshState();
            }
        });
        
        if (mode.equalsIgnoreCase("MATLAB")) {
            System.out.println("tdminer started from matlab");
            isMatlab = true;
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        } else {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAction();
            }
        });
        
        //Display the window.
        //setTitle("GMiner");
        Toolkit t = Toolkit.getDefaultToolkit();
        int wHeight = (int)t.getScreenSize().getHeight();
        int wWidth = (int)t.getScreenSize().getWidth();
        int height = 95  * (wHeight / 100);
        int width = 8 * (wWidth/10);
        setSize(width, height);
        //pack();
        height = getSize().height;
        width = getSize().width;
        setLocation((wWidth - width)/2, (wHeight - height)/2);
    }
    
    /**
     * These functions are of particular use to external threads such as MATLAB.
     */
    public StateInfo getStateInfo() {
        return stateInfo;
    }
    
    /**
     * Any special behavior related to closing the application down in
     * particular situations.
     * MATLAB - Don't close down the JVM it is still being used to run MATLAB.
     */
    private void closeAction() {
        int ans = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Confirm exit", JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;
        
        if (isMatlab) {
            System.out.println("tdminer completed with matlab");
            dispose();
            return;
        }
        System.out.println("tdminer completed");
        System.exit(0);
    }
    
    /**
     * Provide access to the settings menus
     */
    
    public javax.swing.JMenu getExportFileMenu() {
        return jMenuFileExport;
    }
    public javax.swing.JMenu getVisualizationSettingsMenu() {
        return jMenuVisualization;
    }
    public javax.swing.JMenu getProspectorSettingsMenu() {
        return jMenuProspector;
    }
    
    public javax.swing.JMenu getSimulatorSettingsMenu() {
        return jMenuSimulator;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupExportMode = new javax.swing.ButtonGroup();
        buttonGroupSortHarvestFactor = new javax.swing.ButtonGroup();
        buttonGroupSortDiscoveryFactor = new javax.swing.ButtonGroup();
        jTabbedPane = new javax.swing.JTabbedPane();
        eventSeqLoaderPanel = new edu.iisc.tdminer.gui.EventSeqLoaderPanel();
        frequentEpisodeMinerPanel = new edu.iisc.tdminer.gui.FrequentEpisodeMinerPanel();
        visualizationPanel = new edu.iisc.tdminer.gui.VisualizationPanel();
        strengthPlotPanel = new edu.iisc.tdminer.gui.StrengthPlotPanel();
        prefusePanel = new edu.iisc.tdminer.gui.PrefusePanel();
        simulatorsMainPanel = new edu.iisc.tdminer.gui.simulator.SimulatorsMainPanel();
        jPanelStatus = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuOpen = new javax.swing.JMenuItem();
        jMenuSave = new javax.swing.JMenuItem();
        jMenuItemExport = new javax.swing.JMenuItem();
        jMenuFileImport = new javax.swing.JMenu();
        jMenuItemLoadPositions = new javax.swing.JMenuItem();
        jMenuItemLoadRefEpisodes = new javax.swing.JMenuItem();
        jMenuFileExport = new javax.swing.JMenu();
        jMenuItemSaveEpisodeSet = new javax.swing.JMenuItem();
        jMenuSaveImage = new javax.swing.JMenu();
        jMenuItemSaveImagePrefuse = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuTool = new javax.swing.JMenu();
        jMenuItemTimeFrameConstraint = new javax.swing.JMenuItem();
        jMenuItemWindowCount = new javax.swing.JMenuItem();
        jMenuItemCorrelogram = new javax.swing.JMenuItem();
        jMenuItemChangeOffset = new javax.swing.JMenuItem();
        jMenuJitterEventStream = new javax.swing.JMenuItem();
        jMenuItemAllPair3D = new javax.swing.JMenuItem();
        jMenuItemAllPair2D = new javax.swing.JMenuItem();
        jMenuSettings = new javax.swing.JMenu();
        jMenuProspector = new javax.swing.JMenu();
        jRadioButtonMenuItemSortDiscoverByNameLexical = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemSortDiscoveryByNameNumeric = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemSortDiscoveryByVote = new javax.swing.JRadioButtonMenuItem();
        jMenuVisualization = new javax.swing.JMenu();
        jRadioButtonMenuItemSortHarvestByNameLexical = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemSortHarvestByNameNumeric = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemSortHarvestByCount = new javax.swing.JRadioButtonMenuItem();
        jMenuSimulator = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TDMiner");
        setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/miner.gif")));

        jTabbedPane.setDoubleBuffered(true);
        jTabbedPane.addTab("Event Sequence Loader", eventSeqLoaderPanel);
        jTabbedPane.addTab("Frequent Episode Miner", frequentEpisodeMinerPanel);
        jTabbedPane.addTab("Event Sequence Display", visualizationPanel);
        jTabbedPane.addTab("Episode Strength Plot", strengthPlotPanel);
        jTabbedPane.addTab("Graph Visualization", prefusePanel);
        jTabbedPane.addTab("Simulators", simulatorsMainPanel);

        getContentPane().add(jTabbedPane, java.awt.BorderLayout.CENTER);

        jPanelStatus.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanelStatus.setLayout(new java.awt.BorderLayout(10, 5));

        jLabel1.setForeground(new java.awt.Color(0, 51, 204));
        jLabel1.setText("Status:");
        jPanelStatus.add(jLabel1, java.awt.BorderLayout.WEST);

        jLabelStatus.setText("Ready");
        jPanelStatus.add(jLabelStatus, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelStatus, java.awt.BorderLayout.SOUTH);

        jMenuFile.setText("File");

        jMenuOpen.setText("Open event stream...");
        jMenuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuOpenActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuOpen);

        jMenuSave.setText("Save event stream...");
        jMenuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSaveActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuSave);

        jMenuItemExport.setText("Export episodes as events...");
        jMenuItemExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExport);

        jMenuFileImport.setText("Import File");
        jMenuFileImport.setToolTipText("Import information of the specified type.");

        jMenuItemLoadPositions.setText("Node positions...");
        jMenuItemLoadPositions.setToolTipText("Load the position coordinates for a set of nodes.");
        jMenuItemLoadPositions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadPositionsActionPerformed(evt);
            }
        });
        jMenuFileImport.add(jMenuItemLoadPositions);

        jMenuItemLoadRefEpisodes.setText("Reference episodes...");
        jMenuItemLoadRefEpisodes.setToolTipText("Load a set of reference episodes to compare with those mined.");
        jMenuItemLoadRefEpisodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadRefEpisodesActionPerformed(evt);
            }
        });
        jMenuFileImport.add(jMenuItemLoadRefEpisodes);

        jMenuFile.add(jMenuFileImport);

        jMenuFileExport.setText("Export File");
        jMenuFileExport.setToolTipText("Export information into one of several file formats.");

        jMenuItemSaveEpisodeSet.setText("Episode set...");
        jMenuItemSaveEpisodeSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveEpisodeSetActionPerformed(evt);
            }
        });
        jMenuFileExport.add(jMenuItemSaveEpisodeSet);

        jMenuFile.add(jMenuFileExport);

        jMenuSaveImage.setText("Save Image");

        jMenuItemSaveImagePrefuse.setText("Prefuse graph...");
        jMenuItemSaveImagePrefuse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveImagePrefuseActionPerformed(evt);
            }
        });
        jMenuSaveImage.add(jMenuItemSaveImagePrefuse);

        jMenuFile.add(jMenuSaveImage);
        jMenuFile.add(jSeparator2);

        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenuTool.setText("Tools");
        jMenuTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuToolActionPerformed(evt);
            }
        });

        jMenuItemTimeFrameConstraint.setText("Time Frame Constraint");
        jMenuItemTimeFrameConstraint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTimeFrameConstraintActionPerformed(evt);
            }
        });
        jMenuTool.add(jMenuItemTimeFrameConstraint);

        jMenuItemWindowCount.setText("Count Scoped Instances");
        jMenuItemWindowCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWindowCountActionPerformed(evt);
            }
        });
        jMenuTool.add(jMenuItemWindowCount);

        jMenuItemCorrelogram.setText("Cross Correlogram");
        jMenuItemCorrelogram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCorrelogramActionPerformed(evt);
            }
        });
        jMenuTool.add(jMenuItemCorrelogram);

        jMenuItemChangeOffset.setText("Sequence Offset");
        jMenuItemChangeOffset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemChangeOffsetActionPerformed(evt);
            }
        });
        jMenuTool.add(jMenuItemChangeOffset);

        jMenuJitterEventStream.setText("Jitter EventStream");
        jMenuJitterEventStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuJitterEventStreamActionPerformed(evt);
            }
        });
        jMenuTool.add(jMenuJitterEventStream);

        jMenuItemAllPair3D.setText("All pair cross correlogram (3D)");
        jMenuItemAllPair3D.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAllPair3DActionPerformed(evt);
            }
        });
        jMenuTool.add(jMenuItemAllPair3D);

        jMenuItemAllPair2D.setText("All pair cross correlogram (2D)");
        jMenuItemAllPair2D.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAllPair2DActionPerformed(evt);
            }
        });
        jMenuTool.add(jMenuItemAllPair2D);

        jMenuBar.add(jMenuTool);

        jMenuSettings.setText("Settings");

        jMenuProspector.setText("Frequent Episode Miner");

        buttonGroupSortDiscoveryFactor.add(jRadioButtonMenuItemSortDiscoverByNameLexical);
        jRadioButtonMenuItemSortDiscoverByNameLexical.setText("Sort by name lexically");
        jRadioButtonMenuItemSortDiscoverByNameLexical.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemSortDiscoverByNameLexicalActionPerformed(evt);
            }
        });
        jMenuProspector.add(jRadioButtonMenuItemSortDiscoverByNameLexical);

        buttonGroupSortDiscoveryFactor.add(jRadioButtonMenuItemSortDiscoveryByNameNumeric);
        jRadioButtonMenuItemSortDiscoveryByNameNumeric.setText("Sort by name numerically");
        jRadioButtonMenuItemSortDiscoveryByNameNumeric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemSortDiscoveryByNameNumericActionPerformed(evt);
            }
        });
        jMenuProspector.add(jRadioButtonMenuItemSortDiscoveryByNameNumeric);

        buttonGroupSortDiscoveryFactor.add(jRadioButtonMenuItemSortDiscoveryByVote);
        jRadioButtonMenuItemSortDiscoveryByVote.setText("Sort by vote count");
        jRadioButtonMenuItemSortDiscoveryByVote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemSortDiscoveryByVoteActionPerformed(evt);
            }
        });
        jMenuProspector.add(jRadioButtonMenuItemSortDiscoveryByVote);

        jMenuSettings.add(jMenuProspector);

        jMenuVisualization.setText("Visualization");

        buttonGroupSortHarvestFactor.add(jRadioButtonMenuItemSortHarvestByNameLexical);
        jRadioButtonMenuItemSortHarvestByNameLexical.setText("Sort by name lexical");
        jRadioButtonMenuItemSortHarvestByNameLexical.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemSortHarvestByNameLexicalActionPerformed(evt);
            }
        });
        jMenuVisualization.add(jRadioButtonMenuItemSortHarvestByNameLexical);

        buttonGroupSortHarvestFactor.add(jRadioButtonMenuItemSortHarvestByNameNumeric);
        jRadioButtonMenuItemSortHarvestByNameNumeric.setText("Sort by name numeric");
        jRadioButtonMenuItemSortHarvestByNameNumeric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemSortHarvestByNameNumericActionPerformed(evt);
            }
        });
        jMenuVisualization.add(jRadioButtonMenuItemSortHarvestByNameNumeric);

        buttonGroupSortHarvestFactor.add(jRadioButtonMenuItemSortHarvestByCount);
        jRadioButtonMenuItemSortHarvestByCount.setText("Sort by count");
        jRadioButtonMenuItemSortHarvestByCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemSortHarvestByCountActionPerformed(evt);
            }
        });
        jMenuVisualization.add(jRadioButtonMenuItemSortHarvestByCount);

        jMenuSettings.add(jMenuVisualization);

        jMenuSimulator.setText("Simulator");
        jMenuSettings.add(jMenuSimulator);

        jMenuBar.add(jMenuSettings);

        jMenuHelp.setText("Help");
        jMenuHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHelpActionPerformed(evt);
            }
        });

        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemSaveImagePrefuseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveImagePrefuseActionPerformed
        try {
            PrintUtilities.printToFile(this.prefusePanel.getDisplay());
        } catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_jMenuItemSaveImagePrefuseActionPerformed

    private void jMenuItemSaveEpisodeSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveEpisodeSetActionPerformed
   
        JFileChooser fc = new JFileChooser();
        if (Constants.CURRENT_DIR != null)
            fc.setCurrentDirectory(Constants.CURRENT_DIR);
        
        int returnVal = fc.showSaveDialog(this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        
        File f = fc.getSelectedFile();
        String fileName = f.getPath();
        
        // open the file and load the positions of the event types
        EventFactor factor = this.stateInfo.getSession().getEventFactor();
        BufferedReader in = null;
        try {
         in = new BufferedReader( new FileReader( fileName ));
        } catch (FileNotFoundException ex) {
            System.err.println("Requested file " + fileName + " not available " + ex.getMessage());
            return;
        }
        String line;
        try {
            while(( line = in.readLine()) != null) {
                try {
                    factor.setPosition( line.trim() );
                } catch (EventFactor.PositionError pe) {
                    System.err.println("Invalid position request: " + pe.getMessage());
                }
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("error reading file " + fileName + " : " + ex.getMessage());
        }
    }//GEN-LAST:event_jMenuItemSaveEpisodeSetActionPerformed

    /**
     * Open the file and load the episode set, overwriting the existing info.
     * It is assumed that the new episodes are compatible with...
     * - the event factor
     * - the event stream
     * - the interval list
     */
    
    private void jMenuItemLoadRefEpisodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadRefEpisodesActionPerformed
   
        JFileChooser fc = new JFileChooser();
        if (Constants.CURRENT_DIR != null)
            fc.setCurrentDirectory(Constants.CURRENT_DIR);
        
        int returnVal = fc.showOpenDialog(this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        
        File f = fc.getSelectedFile();
        String fileName = f.getPath();
        
        SessionInfo session = this.stateInfo.getSession();
        EpisodeSet episodeSet = new EpisodeSet(session.getEpisodes());
        
        BufferedReader in = null;
        try {
         in = new BufferedReader( new FileReader( fileName ));
        } catch (FileNotFoundException ex) {
            System.err.println("Requested file " + fileName + " not available " + ex.getMessage());
            return;
        }
        String line;
        try {
            while(( line = in.readLine()) != null) {
                try {
                    episodeSet.addEpisode( line.trim() );
                } catch (Exception ex) {
                    System.err.println("Invalid episode definition: " 
                            + line + " " + ex.getMessage());
                }
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("error reading file " + fileName + " : " + ex.getMessage());
        }
        session.setReference( episodeSet );
        this.prefusePanel.updateSetButtons();
    }//GEN-LAST:event_jMenuItemLoadRefEpisodesActionPerformed
    
    private void jMenuItemLoadPositionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadPositionsActionPerformed
        SessionInfo session = this.stateInfo.getSession();
        loadEpisodePositions( session );
    }//GEN-LAST:event_jMenuItemLoadPositionsActionPerformed
    
    
    private void loadEpisodePositions(SessionInfo session) {                                                         
   
       JFileChooser fc = new JFileChooser();
        if (Constants.CURRENT_DIR != null)
            fc.setCurrentDirectory(Constants.CURRENT_DIR);
        
        int returnVal = fc.showOpenDialog(this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        
        File f = fc.getSelectedFile();
        String fileName = f.getPath();
        
        // open the file and load the positions of the event types
        BufferedReader in = null;
        try {
         in = new BufferedReader( new FileReader( fileName ));
        } catch (FileNotFoundException ex) {
            System.err.println("Requested file " + fileName + " not available " + ex.getMessage());
            return;
        }
        String line;
        
        EventFactor masterFactor = session.getEventFactor();
        EventFactor mainFactor = (session.getEpisodes() == null) ?
            null : session.getEpisodes().getEventFactor();
        EventFactor referFactor = (session.getReference() == null) ?
            null : session.getReference().getEventFactor();
        
        try {
            while(( line = in.readLine()) != null) {
                String trimline = line.trim();
                
                try {
                    if (masterFactor != null)
                        masterFactor.setPosition( trimline );
                } catch (EventFactor.PositionError pe) {
                    System.err.println("Invalid position request: " + pe.getMessage());
                }
                try {
                    if (mainFactor != null)
                        mainFactor.setPosition( trimline );
                } catch (EventFactor.PositionError pe) {
                    System.err.println("Invalid position request: " + pe.getMessage());
                }
                try {
                    if (referFactor != null)
                        referFactor.setPosition( trimline );
                    
                } catch (EventFactor.PositionError pe) {
                    System.err.println("Invalid position request: " + pe.getMessage());
                }
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("error reading file " + fileName + " : " + ex.getMessage());
        }
    }
        
    private void jRadioButtonMenuItemSortDiscoveryByVoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemSortDiscoveryByVoteActionPerformed
        setDiscoveryFactorOrdinal(evt, EventFactor.OrderMode.byVoteCount);
    }//GEN-LAST:event_jRadioButtonMenuItemSortDiscoveryByVoteActionPerformed
    
    private void jRadioButtonMenuItemSortDiscoveryByNameNumericActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemSortDiscoveryByNameNumericActionPerformed
        setDiscoveryFactorOrdinal(evt, EventFactor.OrderMode.byNameNumeric);
    }//GEN-LAST:event_jRadioButtonMenuItemSortDiscoveryByNameNumericActionPerformed
    
    private void jRadioButtonMenuItemSortDiscoverByNameLexicalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemSortDiscoverByNameLexicalActionPerformed
        setDiscoveryFactorOrdinal(evt, EventFactor.OrderMode.byNameLexical);
    }//GEN-LAST:event_jRadioButtonMenuItemSortDiscoverByNameLexicalActionPerformed
    
    private void jRadioButtonMenuItemSortHarvestByNameLexicalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemSortHarvestByNameLexicalActionPerformed
        setHarvestFactorOrdinal(evt, EventFactor.OrderMode.byNameLexical);
        this.visualizationPanel.redrawPlot();
    }//GEN-LAST:event_jRadioButtonMenuItemSortHarvestByNameLexicalActionPerformed
    
    private void jRadioButtonMenuItemSortHarvestByNameNumericActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemSortHarvestByNameNumericActionPerformed
        setHarvestFactorOrdinal(evt, EventFactor.OrderMode.byNameNumeric);
        this.visualizationPanel.redrawPlot();
    }//GEN-LAST:event_jRadioButtonMenuItemSortHarvestByNameNumericActionPerformed
    
    private void jRadioButtonMenuItemSortHarvestByCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemSortHarvestByCountActionPerformed
        setHarvestFactorOrdinal(evt, EventFactor.OrderMode.byVoteCount);
        this.visualizationPanel.redrawPlot();
    }//GEN-LAST:event_jRadioButtonMenuItemSortHarvestByCountActionPerformed
            
    public void refreshEventSeqLoaderPanel() {
        this.eventSeqLoaderPanel.repaint();
    }
    
    private void jMenuItemTimeFrameConstraintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTimeFrameConstraintActionPerformed
        // Open a dialog with to establish time frames.
        if (this.stateInfo == null) {
            jLabelStatus.setText("Error loading state information");
            return;
        }
        JDialog dialog = new EventDataStreamTimeConstraintDialog(this, false, stateInfo);
        dialog.setVisible(true);
    }//GEN-LAST:event_jMenuItemTimeFrameConstraintActionPerformed
    
    private void startMatlab(java.awt.event.ActionEvent evt) {
        
        engine = new MatlabEngine();
        try {
            engine.open("matlab -nosplash");
            System.out.println(engine.getOutputString(500));
            engine.evalString("A = gallery('lehmer',10);");
            engine.evalString("f = ones(10,1);");
            engine.evalString("pcg(A,f,1e-5)");
            System.out.println(engine.getOutputString(500));
            engine.close();
        } catch (Exception ex) {
            jLabelStatus.setText("Error while starting MATLAB");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error while starting MATLAB", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void jMenuJitterEventStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuJitterEventStreamActionPerformed
        // Open a dialog with a time frame, i.e. two fields, start, stop.
        // ...and two buttons, jitter, close.
        if (this.stateInfo == null) {
            jLabelStatus.setText("Error loading state information");
            return;
        }
        IEventDataStream eventStream = this.stateInfo.getSession().getSequence();
        if (eventStream == null) {
            jLabelStatus.setText("Error loading event stream");
            JOptionPane.showMessageDialog(this, "Event stream is empty.", "Error getting event stream", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new EventStreamJitterDialog(this, false, eventStream);
        dialog.setVisible(true);
    }//GEN-LAST:event_jMenuJitterEventStreamActionPerformed
    
    private void jMenuItemWindowCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWindowCountActionPerformed
        // Open a dialog with two timestamp fields, start, stop.
        // ...and two buttons, count, close
        // ...and a text field showing the results of the count.
        if (this.stateInfo == null) {
            jLabelStatus.setText("Error loading state information");
            return;
        }
        EpisodeInstanceSet harvest = this.stateInfo.getHarvest();
        if (harvest == null) {
            jLabelStatus.setText("Error loading state information");
            JOptionPane.showMessageDialog(this, "Harvest episode set is empty.", "Error loading episode instances", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new EpisodeCountRangeDialog(this, false, harvest);
        dialog.setVisible(true);
    }//GEN-LAST:event_jMenuItemWindowCountActionPerformed
    
    private void jMenuItemExportActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportActionPerformed
        // Pre-processing the list of episodes
        EpisodeSet episodes = stateInfo.getSession().getEpisodes();
        if (episodes == null) {
            JOptionPane.showMessageDialog(this,
                    "There is no episode set that can be exported. " +
                    "Load and mine an event sequence first.",
                    "Error exporting episodes", JOptionPane.ERROR_MESSAGE);
            return;
        }
        EpisodeExportDialog dialog = new EpisodeExportDialog(this, true, episodes);
        dialog.setVisible(true);
        if (dialog.isCancelled()) return;
        
        JFileChooser fc = new JFileChooser();
        if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
        int returnVal = fc.showSaveDialog(this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        
        File f1 = new File("C:/legend.txt");
        File f2 = new File("C:/sequence.txt");
        
        File f = fc.getSelectedFile();
        String prefix = f.getName();
        int index = prefix.lastIndexOf('.');
        if (index == -1) {
            f1 = new File(f.getParentFile(), prefix + Constants.LEGEND_FILE_STR + ".txt");
            f2 = new File(f.getParentFile(), prefix + Constants.SEQ_FILE_STR + ".txt");
        } else {
            f1 = new File(f.getParentFile(), prefix.substring(0, index) + Constants.LEGEND_FILE_STR + prefix.substring(index));
            f2 = new File(f.getParentFile(), prefix.substring(0, index) + Constants.SEQ_FILE_STR + prefix.substring(index));
        }
        
        final File legendFile = f1;
        final File sequenceFile = f2;
        
        GenericMiner miner = new GenericMiner();
        
        int counterIndex = stateInfo.getCounterIndex();
        ArrayList algos = Algorithms.COUNTER_ALGOS;
        
        final IEventDataStream sequence = stateInfo.getSession().getSequence();
        if (sequence == null) {
            jLabelStatus.setText("Error while exporting episodes as events");
            JOptionPane.showMessageDialog(this,
                    "There are no episodes to export. Please load data and run mining algorithm.",
                    "Error while exporting episodes as events",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        EventFactor eventTypes = episodes.getEventFactor();
        AbstractEpisodeCounter counter = null;
        Object objEntry = algos.get(counterIndex);
        if (!(objEntry instanceof AlgoEntry)) {
            // print some error about this.
            return;
        }
        
        counter = ((AlgoEntry)objEntry).getCounter();
        stateInfo.getSession().setTrackEpisodes(true);
        stateInfo.getSession().setCounter(counter);
        
        final int start = sequence.getEventTypeCount();
        final ArrayList<IEpisode> episodesList = new ArrayList<IEpisode>();
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(4);
        nf.setMinimumFractionDigits(1);
        
        dialog.getEpisodesList(episodesList);
        final int tstampChoice = dialog.getTimeStampChoice();
        if (episodesList.isEmpty()) {
            jLabelStatus.setText("No episodes to export");
            JOptionPane.showMessageDialog(this, "No episodes to export", "Episode Export Failed!!!", JOptionPane.ERROR_MESSAGE);
        }
        
        // All preconditions have been met.
        try {
            final NumberFormat nf1 = NumberFormat.getInstance();
            String s = Integer.toString(episodesList.size() - 1);
            nf1.setMinimumIntegerDigits(s.length());
            nf1.setGroupingUsed(false);
            PrintWriter legendOut = new PrintWriter(legendFile);
            for(int i = 0; i < episodesList.size(); i++) {
                Episode e = (Episode)episodesList.get(i);
                legendOut.println(nf1.format(i) + " : " + e.toString(eventTypes));
            }
            legendOut.close();
            
            final PrintWriter sequenceOut = new PrintWriter(sequenceFile);
            
            miner.countListEpisodesThread(episodesList,
                    new ThreadedProgressManager(this, null) {
                @Override
                public void handleEpisodeCompletion(int episodeIndex, int[] eventTypes, List<IEvent> events)
                throws IObserver.NotImplementedException {
                    double t_avg = 0.0;
                    double t_max = Double.MIN_VALUE;
                    double t_min = Double.MAX_VALUE;
                    for (int ix = 0; ix < eventTypes.length; ix++) {
                        double time = events.get(ix).getStartTime();
                        if (time > t_max) t_max = time;
                        if (time < t_min) t_min = time;
                        t_avg += time;
                    }
                    t_avg /= eventTypes.length;
                    double t_stamp = 0.0;
                    switch (tstampChoice) {
                        case 0:
                            t_stamp = t_min;
                            break;
                        case 1:
                            t_stamp = t_max;
                            break;
                        case 2:
                        default:
                            t_stamp = t_avg;
                            break;
                    }
                    sequenceOut.println(nf1.format(episodeIndex) + "," + nf.format(t_stamp));
                }
                
                public void taskComplete() {
                    System.out.println("Task complete !!!");
                    sequenceOut.close();
                    JOptionPane.showMessageDialog(TDMinerInterface.this,
                            "Episodes have been successfully saved in " + sequenceFile.getName()
                            + " with legend stored in " + legendFile.getName(),
                            "Files saved in " + sequenceFile.getParent(),
                            JOptionPane.INFORMATION_MESSAGE);
                }
                
                public void exceptionOccured(Exception e) {
                    sequenceOut.close();
                    JOptionPane.showMessageDialog(TDMinerInterface.this, e.getClass().toString() + " : " + e.getMessage(),
                            "Error occured while processing", JOptionPane.ERROR_MESSAGE);
                }
            }, stateInfo.getSession());
        } catch (IOException ioe) {
            jLabelStatus.setText("Error while exporting episodes as events");
            JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error while exporting episodes as events", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemExportActionPerformed
    
    private void jMenuItemChangeOffsetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemChangeOffsetActionPerformed
    {//GEN-HEADEREND:event_jMenuItemChangeOffsetActionPerformed
        try {
            String input = JOptionPane.showInputDialog(this,"Enter the new sequence offset","Sequence offset", JOptionPane.QUESTION_MESSAGE);
            if (input != null && input.length() != 0) {
                int offset = Integer.parseInt(input);
                EventStreamWriter.offset = offset;
            }
        } catch(Exception e) {
            jLabelStatus.setText("Error while generating cross-correlogram");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_jMenuItemChangeOffsetActionPerformed
    
    private void jMenuItemCorrelogramActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCorrelogramActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCorrelogramActionPerformed
        try {
            String input = JOptionPane.showInputDialog(this,"Enter [Reference neuron] [Target neuron] [No. of bins] [bin width]\n e.g. A B 51 0.001","Cross-correlogram", JOptionPane.QUESTION_MESSAGE);
            if (input != null && input.length() != 0) {
                String[] parts = input.split("[ ,]+");
                for (int i = 0; i < parts.length; i++) {
                    System.out.println("parts[" + i + "] = " + parts[i]);
                }
                String ref = parts[0];
                String target = parts[1];
                int bins = Integer.parseInt(parts[2]);
                double binwidth = Double.parseDouble(parts[3]);
                
                String[] ret = Crosscorrelogram.instance().analyze(
                        stateInfo.getSession().getSequence(),bins,binwidth,ref,target);
                
                JDialog dialog = new JDialog(this, "Matlab code for cross-correlogram plot", true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
                String str =
                        "x = " + ret[0] + "\n" +
                        "y = " + ret[1] + "\n" +
                        "bar(x,y);\n" +
                        "title('Cross correlogram');\n" +
                        "xlabel('Time shifts \\rightarrow');\n" +
                        "ylabel('Count of coincidences');\n";
                
                JTextArea text = new JTextArea(5,40);
                text.setText(str);
                
                JScrollPane scrollPane =
                        new JScrollPane(text,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                text.setEditable(false);
                dialog.getContentPane().setLayout(new BorderLayout());
                dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
                
                dialog.setSize(600,300);
                int x = (int) (getLocation().getX() + (getSize().getWidth() - dialog.getSize().getWidth()) / 2);
                int y = (int) (getLocation().getY() + (getSize().getHeight() - dialog.getSize().getHeight()) / 2);
                dialog.setLocation(x, y);
                dialog.setVisible(true);
            }
        } catch(Exception e) {
            jLabelStatus.setText("Error while generating cross-correlogram");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_jMenuItemCorrelogramActionPerformed
    
    private void jMenuSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuSaveActionPerformed
    {//GEN-HEADEREND:event_jMenuSaveActionPerformed
        JFileChooser fc = new JFileChooser();
        if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
        int returnVal = fc.showSaveDialog(this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            //This is where a real application would open the file.
            try {
                EventStreamWriter.instance().write(file, stateInfo.getSession().getSequence());
            } catch (Exception ioe) {
                jLabelStatus.setText("Error while saving event sequence");
                JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("Save command cancelled by user.");
        }
        
    }//GEN-LAST:event_jMenuSaveActionPerformed
    
    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemAboutActionPerformed
    {//GEN-HEADEREND:event_jMenuItemAboutActionPerformed
        new AboutWindow("GMiner", this);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed
        
    private void jMenuHelpActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuHelpActionPerformed
    {//GEN-HEADEREND:event_jMenuHelpActionPerformed
        
    }//GEN-LAST:event_jMenuHelpActionPerformed
    
    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExitActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExitActionPerformed
        closeAction();
    }//GEN-LAST:event_jMenuItemExitActionPerformed

private void jMenuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOpenActionPerformed
        if (eventSeqLoaderPanel.openDataSequence())
        {
            jTabbedPane.setSelectedComponent(eventSeqLoaderPanel);
        }
}//GEN-LAST:event_jMenuOpenActionPerformed

private void jMenuItemAllPair3DActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAllPair3DActionPerformed
    try {
            String input = JOptionPane.showInputDialog(this,"Enter [Set of neurons] [No. of bins] [bin width]\n e.g. A B C D 51 0.001","All pair Cross-correlogram", JOptionPane.QUESTION_MESSAGE);
            if (input != null && input.length() != 0) {
                String[] parts = input.split("[ ,]+");
                for (int i = 0; i < parts.length; i++) {
                    System.out.println("parts[" + i + "] = " + parts[i]);
                }

                int last = parts.length - 1;
                int bins = Integer.parseInt(parts[last - 1]);
                double binwidth = Double.parseDouble(parts[last]);
                JDialog dialog = new JDialog(this, "Matlab code for all pair cross-correlogram plot", true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
                StringBuffer str = new StringBuffer();
                StringBuffer legend = new StringBuffer();
                
                boolean flag = true;
                int count = 1;
                for (int i = 0; i < last - 1; i++)
                {
                    for (int j = 0; j < last - 1; j++)
                    {
                        if (i == j) continue;
                        String ref = parts[i];
                        String target = parts[j];

                        String[] ret = Crosscorrelogram.instance().analyze(
                                stateInfo.getSession().getSequence(),bins,binwidth,ref,target);

                        if (!flag) legend.append(",");
                        legend.append("'" + parts[i] + " - " + parts[j] + "'");

                        if (flag)
                        {
                            str.append("x = " + ret[0] + "\n");
                            str.append("Y = [];\n");
                            flag = false;
                        }
                        
                        str.append("Y(" + count + ",:) = "+ ret[1] +"\n");
                                
                        count ++;
                    }
                }
                str.append("bar3(x', Y','detached');\n");
                str.append("title('Cross correlogram');\n");
                str.append("ylabel('Time shifts \\rightarrow');\n");
                str.append("zlabel('Count of coincidences');\n");
                str.append("legend(" + legend + ");\n");
                
                JTextArea text = new JTextArea(5,40);
                text.setText(str.toString());
                
                JScrollPane scrollPane =
                        new JScrollPane(text,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                text.setEditable(false);
                dialog.getContentPane().setLayout(new BorderLayout());
                dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
                
                dialog.setSize(600,300);
                int x = (int) (getLocation().getX() + (getSize().getWidth() - dialog.getSize().getWidth()) / 2);
                int y = (int) (getLocation().getY() + (getSize().getHeight() - dialog.getSize().getHeight()) / 2);
                dialog.setLocation(x, y);
                dialog.setVisible(true);
            }
        } catch(Exception e) {
            jLabelStatus.setText("Error while generating cross-correlogram");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

}//GEN-LAST:event_jMenuItemAllPair3DActionPerformed

private void jMenuItemAllPair2DActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAllPair2DActionPerformed
    try {
            String input = JOptionPane.showInputDialog(this,
                    "Enter [Set of neurons] [No. of bins] [bin width]\n e.g. A B C D 51 0.001",
                    "All pair Cross-correlogram", 
                    JOptionPane.QUESTION_MESSAGE);
            if (input != null && input.length() != 0) {
                String[] parts = input.split("[ ,]+");
                for (int i = 0; i < parts.length; i++) {
                    System.out.println("parts[" + i + "] = " + parts[i]);
                }

                int last = parts.length - 1;
                int bins = Integer.parseInt(parts[last - 1]);
                double binwidth = Double.parseDouble(parts[last]);
                JDialog dialog = new JDialog(this, "Matlab code for all pair cross-correlogram plot", true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
                StringBuffer str = new StringBuffer();
                
                boolean flag = true;
                int count = 0;
                int n = last - 1;
                
                String input2 = JOptionPane.showInputDialog(this,
                        "Enter [rows] [cols] per figure \n e.g. ",
                        "All pair Cross-correlogram", 
                        JOptionPane.QUESTION_MESSAGE, null, null, "5 2").toString();
                
                boolean c = JOptionPane.showConfirmDialog(this, "Normalize scale?","",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                System.out.println("Input2 = " + input2);
                int rows = 5;
                int cols = 2;
                if (input2 != null && input2.length() != 0) {
                    String[] parts2 = input2.split("[ ,]+");
                    try{
                    rows = Integer.parseInt(parts2[0]);
                    cols = Integer.parseInt(parts2[1]);
                    if (rows <= 0) rows = 5;
                    if (cols <= 0) cols = 2;
                    } catch (NumberFormatException nfe) { nfe.printStackTrace();}
                }
                count = 1;
                for (int i = 0; i < n; i++)
                {
                    for (int j = 0; j < n; j++)
                    {
                        if (i == j) continue;
                        String ref = parts[i];
                        String target = parts[j];

                        String[] ret = Crosscorrelogram.instance().analyze(
                                stateInfo.getSession().getSequence(),bins,binwidth,ref,target);

                        if (flag)
                        {
                            str.append("x = " + ret[0] + "\n");
                            str.append("Y  = [];\n\n");
                            flag = false;
                        }
                        str.append("Y(" + count + ",:) = "+ ret[1] +"\n");
                        count ++;
                    }
                }
                count = 0;
                if (c) str.append("ymax = max(max(Y));\n\n");
                int k = 1;
                for (int i = 0; i < n; i++)
                {
                    for (int j = 0; j < n; j++)
                    {
                        if (i == j) continue;

                        if (count == 0) str.append("figure;\n\n");
                        str.append("subplot(" + rows + "," + cols + "," + (count + 1) 
                                + "), bar(x, Y(" + k + ",:));\n");
                        if (c) str.append("ylim([0 ymax]);\n");
                        str.append("xlim([min(x) max(x)]);\n");
                        str.append("title('" + parts[i] + " - " + parts[j] + "');\n");
                        //str.append("xlabel('Time shifts \\rightarrow');\n");
                        //str.append("ylabel('Count');\n\n");
                                
                        count = (count + 1) % (rows * cols);
                        k ++;
                    }
                }
                JTextArea text = new JTextArea(5,40);
                text.setText(str.toString());
                
                JScrollPane scrollPane =
                        new JScrollPane(text,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                text.setEditable(false);
                dialog.getContentPane().setLayout(new BorderLayout());
                dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
                dialog.setSize(600,300);
                int x = (int) (getLocation().getX() + (getSize().getWidth() - dialog.getSize().getWidth()) / 2);
                int y = (int) (getLocation().getY() + (getSize().getHeight() - dialog.getSize().getHeight()) / 2);
                dialog.setLocation(x, y);
                dialog.setVisible(true);
            }
        } catch(Exception e) {
            jLabelStatus.setText("Error while generating cross-correlogram");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

}//GEN-LAST:event_jMenuItemAllPair2DActionPerformed

private void jMenuToolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuToolActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jMenuToolActionPerformed
    
    public final static Preferences prefs = Preferences.userNodeForPackage( TDMinerInterface.class );
    
    /**
     * This is the only way to get past the private constructor.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        final String mode = args.length < 1 ? "" : args[0];

        boolean displayflag = true;
        if (args.length == 1) {
            System.out.println("Starting TDMiner in " + mode);
        }
        else if (args.length == 4 && args[0].equals("-batch"))
        {
            displayflag = false;
            try
            {
                SimpleMiner.mine(args);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        if (displayflag)
        {
            final JWindow w = getSplashScreen();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    TDMinerInterface frame = new TDMinerInterface(mode);
                    frame.setVisible(true);
                    w.setVisible(false);
                    w.dispose();
                }
            });
        }
    }
    
    private static JWindow getSplashScreen() {
        JWindow w = new JWindow();
        if (prefs.getBoolean("splash?", true)) {
            w.getContentPane().add(new JLabel("Loading TDMiner 2.0. Please wait ...",
                    JLabel.CENTER), BorderLayout.CENTER);
            prefs.putBoolean("splash?", true);
        }
        int width = 200;
        int height = 50;
        Toolkit t = Toolkit.getDefaultToolkit();
        int wHeight = (int)t.getScreenSize().getHeight();
        int wWidth = (int)t.getScreenSize().getWidth();
        w.setBounds((wWidth - width)/2, (wHeight - height)/2, width, height);
        w.repaint();
        Thread.yield();
        w.setVisible(true);
        return w;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupExportMode;
    private javax.swing.ButtonGroup buttonGroupSortDiscoveryFactor;
    private javax.swing.ButtonGroup buttonGroupSortHarvestFactor;
    private edu.iisc.tdminer.gui.EventSeqLoaderPanel eventSeqLoaderPanel;
    private edu.iisc.tdminer.gui.FrequentEpisodeMinerPanel frequentEpisodeMinerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuFileExport;
    private javax.swing.JMenu jMenuFileImport;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemAllPair2D;
    private javax.swing.JMenuItem jMenuItemAllPair3D;
    private javax.swing.JMenuItem jMenuItemChangeOffset;
    private javax.swing.JMenuItem jMenuItemCorrelogram;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemExport;
    private javax.swing.JMenuItem jMenuItemLoadPositions;
    private javax.swing.JMenuItem jMenuItemLoadRefEpisodes;
    private javax.swing.JMenuItem jMenuItemSaveEpisodeSet;
    private javax.swing.JMenuItem jMenuItemSaveImagePrefuse;
    private javax.swing.JMenuItem jMenuItemTimeFrameConstraint;
    private javax.swing.JMenuItem jMenuItemWindowCount;
    private javax.swing.JMenuItem jMenuJitterEventStream;
    private javax.swing.JMenuItem jMenuOpen;
    private javax.swing.JMenu jMenuProspector;
    private javax.swing.JMenuItem jMenuSave;
    private javax.swing.JMenu jMenuSaveImage;
    private javax.swing.JMenu jMenuSettings;
    private javax.swing.JMenu jMenuSimulator;
    private javax.swing.JMenu jMenuTool;
    private javax.swing.JMenu jMenuVisualization;
    private javax.swing.JPanel jPanelStatus;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemSortDiscoverByNameLexical;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemSortDiscoveryByNameNumeric;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemSortDiscoveryByVote;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemSortHarvestByCount;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemSortHarvestByNameLexical;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemSortHarvestByNameNumeric;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane;
    private edu.iisc.tdminer.gui.PrefusePanel prefusePanel;
    private edu.iisc.tdminer.gui.simulator.SimulatorsMainPanel simulatorsMainPanel;
    private edu.iisc.tdminer.gui.StrengthPlotPanel strengthPlotPanel;
    private edu.iisc.tdminer.gui.VisualizationPanel visualizationPanel;
    // End of variables declaration//GEN-END:variables
    
    
    private void setDiscoveryFactorOrdinal(java.awt.event.ActionEvent evt, EventFactor.OrderMode mode ) {
        // create the nkeys on the factors and rebuilt the plot accordingly.
        StateInfo stateInfo = this.getStateInfo();
        if (stateInfo == null) return;
        SessionInfo session = stateInfo.getSession();
        if (session == null) return;
        IEventDataStream events = session.getSequence();
        if (events == null) return;
        EventFactor esFactor = events.getEventFactor();
        if (esFactor == null) return;
        esFactor.setOrdinal( mode );
        
        EpisodeSet episodes = session.getEpisodes();
        if (episodes == null) return;
        EventFactor epiFactor = episodes.getEventFactor();
        epiFactor.setOrdinal( mode );
        
        EpisodeInstanceSet harvest = stateInfo.getHarvest();
        if (harvest == null) return;
        EventFactor instFactor = harvest.getEventFactor();
        if (instFactor == null) return;
        instFactor.setOrdinal( mode );
        
        return;
    }
    
    private void setHarvestFactorOrdinal(java.awt.event.ActionEvent evt, EventFactor.OrderMode mode ) {
        // create the nkeys on the factors and rebuilt the plot accordingly.
        if (this.getStateInfo() == null) return;
        if (this.getStateInfo().getSession().getSequence().getEventFactor() == null) return;
        
        if (this.getStateInfo().getHarvest() == null) return;
        if (this.getStateInfo().getHarvest().getEventFactor() == null) return;
        this.getStateInfo().getHarvest().getEventFactor().setOrdinal(mode);
        
        return;
    }
    
}
