/*
 * FrequentEpisodeMinerPanel.java
 *
 * Created on March 17, 2006, 1:09 PM
 */
package edu.iisc.tdminer.gui;

import edu.iisc.tdminer.model.ThreadedProgressManager;
import edu.iisc.tdminer.util.AlgoEntry;
import edu.iisc.tdminer.util.Algorithms;
import edu.iisc.tdminer.util.Constants;
import edu.iisc.tdminercore.candidate.AbstractCandidateGen;
import edu.iisc.tdminercore.counter.AbstractEpisodeCounter;
import edu.iisc.tdminercore.counter.AbstractSerialEpisodeCounter;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminer.gui.function.DisplayEpisodeStats;
import edu.iisc.tdminercore.miner.GenericMiner;
import edu.iisc.tdminer.model.FreqEpisodeTableModel;
import edu.iisc.tdminercore.counter.AbstractParallelEpisodeCounter;
import edu.iisc.tdminercore.counter.AbstractSerialCounterInterEventConst;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithIntervals;
import edu.iisc.tdminercore.counter.SerialEpisodeCounterWithRigidDelays;
import edu.iisc.tdminercore.data.AbstractEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.filter.ThresholdFilterType;
import edu.iisc.tdminercore.miner.SessionInfo;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author  patnaik
 */
public class FrequentEpisodeMinerPanel extends javax.swing.JPanel implements ITaskPanel, TreeSelectionListener
{

    private final static Preferences prefs = Preferences.userNodeForPackage(FrequentEpisodeMinerPanel.class);    //private int chunkLimit = 5000;
    private static final boolean DEBUG = false; // false to remove debugging
    private StateInfo stateInfo;
    private JLabel jLabelStatus;
    private FreqEpisodeTableModel model;
    private GenericMiner miner;
    private CardLayout card;
    private int columnIndex = -1;
    private boolean ascending = true;
    private long t;
    private static final int COMPLETE_MINING = 0;
    private static final int COUNTING_ONLY = 1;
    private int candidateSelectionIndex = COMPLETE_MINING;
    private boolean isSequenceMined = false;
    private IEventDataStream sequence;
    private JFrame frame;
    private List<AlgoEntry> algos = Algorithms.COUNTER_ALGOS;
    private Algorithms.CandidateGlossary[] candidates = Algorithms.CANDIDATE_GEN_ALGOS;

    public void fitDefaultParameters()
    {
        if (DEBUG)
        {
            System.out.println("FrequentEpisodeMinerPanel: fit default parameters");
        }
        int index = jComboBoxAlgorithm.getSelectedIndex();
        AlgoEntry entry = algos.get(index);


        jTextFieldEpisodeExp.setEnabled(true);
        jLabelEpisodeExp.setEnabled(true);

        jTextFieldIntervalExp.setEnabled(true);
        jPanelIntervalExpire.setEnabled(true);

        jLabelIntervalLow.setEnabled(true);
        jTextFieldIntervalExpLow.setEnabled(true);

        jCheckBoxAllowRepeats.setSelected(true);
        jCheckBoxAllowRepeats.setEnabled(true);

        jCheckBoxPermutations.setEnabled(true);

        jButtonSelectIntervals.setEnabled(true);
        jButtonSelectDurations.setEnabled(true);

        jTabbedPaneThresholdParameters.setSelectedIndex(0);

        if (entry.isFake())
        {
            return;
        }

        if (entry.getCandidateIndex() < jComboBoxCandidates.getItemCount())
        {
            jComboBoxCandidates.setSelectedIndex(entry.getCandidateIndex());
        }

        if (!entry.isEpsExpiryEnable())
        {
            //jTextFieldEpisodeExp.setText("0.0");
            jTextFieldEpisodeExp.setEnabled(false);
            jLabelEpisodeExp.setEnabled(false);
        }

        if (!entry.isIvlHighEnable())
        {
            //jTextFieldIntervalExp.setText("0.0");
            jTextFieldIntervalExp.setEnabled(false);
            jPanelIntervalExpire.setEnabled(false);
        }

        if (!entry.isIvlLowEnable())
        {
            //jTextFieldIntervalExpLow.setText("0.0");
            jLabelIntervalLow.setEnabled(false);
            jTextFieldIntervalExpLow.setEnabled(false);
        }

        if (!entry.isAllowRepeatedEventTypes())
        {
            jCheckBoxAllowRepeats.setSelected(false);
            jCheckBoxAllowRepeats.setEnabled(false);
        }

        if (entry.getCounter() instanceof AbstractSerialEpisodeCounter)
        {
            CardLayout layout = ((CardLayout) jPanelAutomaticThreshold.getLayout());
            layout.show(jPanelAutomaticThreshold, "serial");
            layout = ((CardLayout) jPanelNegThreshold.getLayout());
            layout.show(jPanelNegThreshold, "serial");
        }
        else if (entry.getCounter() instanceof AbstractParallelEpisodeCounter)
        {
            CardLayout layout = ((CardLayout) jPanelAutomaticThreshold.getLayout());
            layout.show(jPanelAutomaticThreshold, "parallel");
            layout = ((CardLayout) jPanelNegThreshold.getLayout());
            layout.show(jPanelNegThreshold, "parallel");
        }

        if (!(entry.getCounter() instanceof AbstractSerialEpisodeCounter))
        {
            jCheckBoxPermutations.setEnabled(false);
        }

        if (entry.getCounter() instanceof AbstractSerialCounterInterEventConst)
        {
            recursiveSetEnabled(jPanelBackPruning, true);
            jLabelTimeGranularity.setEnabled(true);
            jTextFieldTimeGranularity.setEnabled(true);
        }
        else
        {
            recursiveSetEnabled(jPanelBackPruning, false);
            jLabelTimeGranularity.setEnabled(false);
            jTextFieldTimeGranularity.setEnabled(false);
        }
        switch (entry.getConstraintType())
        {
            case AlgoEntry.DURATION_DISCOVERY:
            case AlgoEntry.DURATION_DISCOVERY_WITH_EXPIRY:
                jButtonSelectIntervals.setEnabled(false);
                jTabbedPaneThresholdParameters.setSelectedIndex(1);
                break;
            case AlgoEntry.INTER_EVENT_DISCOVERY:
                jButtonSelectDurations.setEnabled(false);
                break;
            case AlgoEntry.EXPLICIT:
            default:
                jButtonSelectIntervals.setEnabled(false);
                jButtonSelectDurations.setEnabled(false);
                break;
        }
    }

    /** Creates new form FrequentEpisodeMinerPanel */
    public FrequentEpisodeMinerPanel()
    {
        if (DEBUG)
        {
            System.out.println("FrequentEpisodeMinerPanel: <constructor>");
        }
        initComponents();
        
        JTableHeader header = jTableEpisodes.getTableHeader();
        header.addMouseListener(new ColumnHeaderListener());

        card = (CardLayout) jCardPanel.getLayout();

        for (int i = 0; i < algos.size(); i++)
        {
            jComboBoxAlgorithm.addItem(algos.get(i).toString());
        }
        jComboBoxAlgorithm.setSelectedIndex(0);

        for (int i = 0; i < candidates.length; i++)
        {
            jComboBoxCandidates.addItem(candidates[i].name);
        }

        miner = new GenericMiner();

        jTreeEpisodes.addTreeSelectionListener(this);
        fitDefaultParameters();
    }

    public FreqEpisodeTableModel getTableModel()
    {
        this.model = new FreqEpisodeTableModel();
        return model;
    }

    public void setStateInfo(StateInfo stateInfo)
    {
        this.stateInfo = stateInfo;
    }

    public void setJLabelStatus(JLabel jLabelStatus)
    {
        this.jLabelStatus = jLabelStatus;
    }

    public void setFrame(JFrame frame) throws Exception
    {
        if (frame instanceof ParentMenu)
        {
            ParentMenu parentmenu = (ParentMenu) frame;
            this.initSettingsMenuComponents(parentmenu.getProspectorSettingsMenu());
        }
        else
        {
            System.out.println("Not a Parent Menu");
        }
        this.frame = frame;
    }

    public void refreshState()
    {
        if (DEBUG)
        {
            System.out.println("FrequentEpisodeMinerPanel: refresh state");
        }
        double t = 0.0;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);

        SessionInfo sess = stateInfo.getSession();

        t = sess.getFrequencyThreshold(1);
        jTextFieldFreqThOneNode.setText(nf.format(t));

        t = sess.getFrequencyThreshold(2);
        jTextFieldFreqTh.setText(nf.format(t));

        ThresholdFilterType filterType = sess.getThresholdType();
        switch (filterType)
        {
            case EXPLICIT_DECAY:
                jTabbedPaneThresholdParameters.setSelectedIndex(1);
                break;
            case POISSON_BASED:
                jTabbedPaneThresholdParameters.setSelectedIndex(0);
                ((CardLayout) jPanelAutomaticThreshold.getLayout()).show(jPanelAutomaticThreshold, "parallel");
                break;
            case STRENGTH_BASED:
                jTabbedPaneThresholdParameters.setSelectedIndex(0);
                ((CardLayout) jPanelAutomaticThreshold.getLayout()).show(jPanelAutomaticThreshold, "serial");
                break;
        }

        if (sess.getSequence() != sequence)
        {
            sequence = sess.getSequence();
            model.setData(null, 0);
            jTreeEpisodes.setModel(null);
            jTextPaneDetails.setText("");
            sess.setEpisodes(null);
        }
    }

    /**
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes(JTable table)
    {
        if (DEBUG)
        {
            System.out.println("FrequentEpisodeMinerPanel: init column sizes");
        }
        FreqEpisodeTableModel model = (FreqEpisodeTableModel) table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;
        TableCellRenderer headerRenderer =
                table.getTableHeader().getDefaultRenderer();

        column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(75);
        column.setMinWidth(75);
        column.setMaxWidth(125);
        column.setResizable(false);

        column = table.getColumnModel().getColumn(2);
        column.setPreferredWidth(125);
        column.setMinWidth(125);
        column.setMaxWidth(150);
        column.setResizable(false);
    }

    /**
     * This is the main method.
     * It runs the miner on the data stream.
     */
    private void mineDataStream()
    {
        if (sequence == null || sequence.getSize() < 1)
        {
            jLabelStatus.setText("Error in data sequence");
            JOptionPane.showMessageDialog(this, "Please load a valid data sequence.", "Error in data sequence", JOptionPane.ERROR_MESSAGE);
            return;
        }

        jLabelStatus.setText("Started event stream mining ...");
        jCheckBoxPermutations.setSelected(true);
        System.out.println("Mining event seq of length = " + sequence.getSize());

        try
        {
            SessionInfo sess = stateInfo.getSession();

            AlgoEntry entry = this.algos.get(jComboBoxAlgorithm.getSelectedIndex());
            if (entry.isFake())
            {
                throw new Exception("Please select a counting algorithm");
            }

            jButtonMine.setEnabled(false);

            jTreeEpisodes.setModel(null);
            jTextPaneDetails.setText("");
            card.show(jCardPanel, "details");
            t = System.currentTimeMillis();

            double intervalExpiry = Double.parseDouble(jTextFieldIntervalExp.getText());
            double intervalExpiryLow = Double.parseDouble(jTextFieldIntervalExpLow.getText());
            double episodeExpiry = Double.parseDouble(jTextFieldEpisodeExp.getText());

            sess.setCurrentLevel(1);
            sess.setIntervalExpiry(intervalExpiry);
            sess.setIntervalExpiryLow(intervalExpiryLow);
            sess.setEpisodeExpiry(episodeExpiry);
            
            double timeGranularity = Double.parseDouble(jTextFieldTimeGranularity.getText());
            sess.setTimeGranularity(timeGranularity);

            boolean isSegmented = jCheckBoxSegment.isSelected();
            sess.setSegmented(isSegmented);
            double segment_len = Double.parseDouble(jTextFieldSegmentSize.getText());
            sess.setSegLen(segment_len);
            
            sess.setSelectedModel(jComboBoxModel.getSelectedIndex());

            AbstractEpisodeCounter counter = entry.getCounter();
            sess.setTrackEpisodes(false);
            sess.setCounter(counter);

            switch(jTabbedPaneThresholdParameters.getSelectedIndex())
            {
                case 0:
                    if (counter instanceof AbstractSerialEpisodeCounter)
                    {
                        sess.setThresholdType(ThresholdFilterType.STRENGTH_BASED);
                        double errorTypeI = Double.parseDouble(jTextFieldErrorTypeI.getText());
                        sess.setErrorTypeI(errorTypeI);
                        double eStrong = Double.parseDouble(jTextFieldEStrong.getText());
                        sess.setEStrong(eStrong);
                    }
                    else
                    {
                        sess.setThresholdType(ThresholdFilterType.POISSON_BASED);
                        double poissonError = Double.parseDouble(jTextFieldPoissonErrorProb.getText());
                        sess.setPoissonError(poissonError);
                    }
                    break;
                case 2:
                    if (counter instanceof AbstractSerialEpisodeCounter)
                    {
                        sess.setThresholdType(ThresholdFilterType.NEG_STRENGTH);
                        double errorTypeI = Double.parseDouble(jTextFieldErrorTypeI1.getText());
                        sess.setErrorTypeI(errorTypeI);
                        double eStrong = Double.parseDouble(jTextFieldEStrong1.getText());
                        sess.setEStrong(eStrong);
                    }
                    else
                    {
                        sess.setThresholdType(ThresholdFilterType.POISSON_BASED);
                        double poissonError = Double.parseDouble(jTextFieldPoissonErrorProb.getText());
                        sess.setPoissonError(poissonError);
                    }
                    break;
                default:
                    sess.setThresholdType(ThresholdFilterType.EXPLICIT_DECAY);
                    double freqDecay = Double.parseDouble(jTextFieldDecay.getText());
                    sess.setFreqDecay(freqDecay);
                    double frequencyThreshold = Double.parseDouble(jTextFieldFreqTh.getText());
                    sess.setFrequencyThreshold(2, frequencyThreshold);
                    double oneNodeFreqTh = Double.parseDouble(jTextFieldFreqThOneNode.getText());
                    sess.setFrequencyThreshold(1, oneNodeFreqTh);
                    break;
            }

            stateInfo.setCounterIndex(jComboBoxAlgorithm.getSelectedIndex());

            if (candidateSelectionIndex == COMPLETE_MINING)
            {
                int maxLevels = ((Integer) jSpinnerMaxLevels.getValue()).intValue();
                sess.setPlevels(maxLevels);

                AbstractCandidateGen candidateGenerator =
                        this.candidates[jComboBoxCandidates.getSelectedIndex()].functor;
                candidateGenerator.init(stateInfo.getSession());
                sess.setCandidateGenerator(candidateGenerator);
                sess.setAllowRepeat(jCheckBoxAllowRepeats.isSelected());

                stateInfo.setCandidateGenIndex(jComboBoxCandidates.getSelectedIndex());

                ThreadedProgressManager progmgr = new ThreadedProgressManager(
                        FrequentEpisodeMinerPanel.this.frame,
                        FrequentEpisodeMinerPanel.this);
                progmgr.setSession(sess);
                miner.mineSequenceThread(progmgr, sess);
            }
            else
            {
                EpisodeSet episodes = sess.getEpisodes();
                if (episodes != null)
                {
                    if (episodes.getEpisodeList(1) == null || episodes.getEpisodeList(1).size() == 0)
                    {
                        System.out.println("Adding all 1-node episodes");
                        EventFactor etypes = sequence.getEventFactor();
                        for(IEpisode e : etypes.getEpisodeList())
                            episodes.add(e);
                    }
                    ThreadedProgressManager progmgr = new ThreadedProgressManager(
                            FrequentEpisodeMinerPanel.this.frame,
                            FrequentEpisodeMinerPanel.this);

                    progmgr.setSession(sess);

                    miner.countEpisodesThread(episodes, progmgr, sess);
                }
                else
                {
                    jLabelStatus.setText("Error in episode counting");
                    JOptionPane.showMessageDialog(this, "Episode set to count is empty.",
                            "Error in episode counting", JOptionPane.ERROR_MESSAGE);
                    jButtonMine.setEnabled(true);
                }
            }

        }
        catch (Exception e)
        {
            jLabelStatus.setText("Error in input parameters");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error in input parameter",
                    JOptionPane.ERROR_MESSAGE);
            jButtonMine.setEnabled(true);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelBackPruning = new javax.swing.JPanel();
        jCheckBoxBackPruningEnabled = new javax.swing.JCheckBox();
        jTopPanel = new javax.swing.JPanel();
        jPanelSetAlgorithm = new javax.swing.JPanel();
        jPanelUserInputs = new javax.swing.JPanel();
        jPanelIntervalExpire = new javax.swing.JPanel();
        jTextFieldIntervalExpLow = new javax.swing.JTextField();
        jLabelIntervalLow = new javax.swing.JLabel();
        jTextFieldIntervalExp = new javax.swing.JTextField();
        jTextFieldEpisodeExp = new javax.swing.JTextField();
        jLabelEpisodeExp = new javax.swing.JLabel();
        jButtonSelectIntervals = new javax.swing.JButton();
        jLabelMaxLevels = new javax.swing.JLabel();
        jSpinnerMaxLevels = new javax.swing.JSpinner();
        jButtonSelectDurations = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxSegment = new javax.swing.JCheckBox();
        jLabelSeg1 = new javax.swing.JLabel();
        jTextFieldSegmentSize = new javax.swing.JTextField();
        jLabelSeg2 = new javax.swing.JLabel();
        jAlgorithmEntryPanel = new javax.swing.JPanel();
        jAlgorithmLabel = new javax.swing.JLabel();
        jComboBoxAlgorithm = new javax.swing.JComboBox();
        jPanelFreqThreshold = new javax.swing.JPanel();
        jTabbedPaneThresholdParameters = new javax.swing.JTabbedPane();
        jPanelAutomaticThreshold = new javax.swing.JPanel();
        jPanelEStrong = new javax.swing.JPanel();
        jLabelEStrong = new javax.swing.JLabel();
        jTextFieldEStrong = new javax.swing.JTextField();
        jLabelErrorTypeI = new javax.swing.JLabel();
        jTextFieldErrorTypeI = new javax.swing.JTextField();
        jPanelPoisson = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldPoissonErrorProb = new javax.swing.JTextField();
        jPanelDecayingThreshold = new javax.swing.JPanel();
        jLabelFreqThOne = new javax.swing.JLabel();
        jTextFieldFreqThOneNode = new javax.swing.JTextField();
        jLabelFreqTh = new javax.swing.JLabel();
        jTextFieldFreqTh = new javax.swing.JTextField();
        jLabelThDecay = new javax.swing.JLabel();
        jTextFieldDecay = new javax.swing.JTextField();
        jPanelNegThreshold = new javax.swing.JPanel();
        jPanelPoisson1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldPoissonErrorProb1 = new javax.swing.JTextField();
        jPanelEStrong1 = new javax.swing.JPanel();
        jLabelEStrong1 = new javax.swing.JLabel();
        jTextFieldEStrong1 = new javax.swing.JTextField();
        jLabelErrorTypeI1 = new javax.swing.JLabel();
        jTextFieldErrorTypeI1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jTextFieldTimeGranularity = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabelTimeGranularity = new javax.swing.JLabel();
        jTabbedPaneCandidates = new javax.swing.JTabbedPane();
        jPanelCandGen = new javax.swing.JPanel();
        jComboBoxCandidates = new javax.swing.JComboBox();
        jCheckBoxAllowRepeats = new javax.swing.JCheckBox();
        jCheckBoxPermutations = new javax.swing.JCheckBox();
        jPanelCandLoad = new javax.swing.JPanel();
        jButtonAddEpisode = new javax.swing.JButton();
        jButtonLoadEpisodeSet = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jComboBoxModel = new javax.swing.JComboBox();
        jReportPanel = new javax.swing.JPanel();
        jPanelActions = new javax.swing.JPanel();
        jButtonMine = new javax.swing.JButton();
        jButtonEpsStats = new javax.swing.JButton();
        jButtonSaveEpisodes = new javax.swing.JButton();
        jButtonClear = new javax.swing.JButton();
        jPanelResultCards = new javax.swing.JPanel();
        jCardPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPaneDetails = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableEpisodes = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeEpisodes = new javax.swing.JTree();

        jPanelBackPruning.setBorder(javax.swing.BorderFactory.createTitledBorder("Back Pruning"));
        jPanelBackPruning.setLayout(new javax.swing.BoxLayout(jPanelBackPruning, javax.swing.BoxLayout.Y_AXIS));

        jCheckBoxBackPruningEnabled.setText("Enable Heuristic Pruning");
        jCheckBoxBackPruningEnabled.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxBackPruningEnabled.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jCheckBoxBackPruningEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxBackPruningEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxBackPruningEnabledActionPerformed(evt);
            }
        });
        jPanelBackPruning.add(jCheckBoxBackPruningEnabled);

        jPanelSetAlgorithm.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Select algorithm"), javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        jPanelUserInputs.setBorder(javax.swing.BorderFactory.createTitledBorder("Constraints"));

        jPanelIntervalExpire.setBorder(javax.swing.BorderFactory.createTitledBorder("Interval expiry"));

        jTextFieldIntervalExpLow.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldIntervalExpLow.setText("0.0");
        jTextFieldIntervalExpLow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldIntervalExpLowActionPerformed(evt);
            }
        });
        jTextFieldIntervalExpLow.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldIntervalExpLowFocusGained(evt);
            }
        });

        jLabelIntervalLow.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelIntervalLow.setText("to");

        jTextFieldIntervalExp.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldIntervalExp.setText("0.0");
        jTextFieldIntervalExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldIntervalExpActionPerformed(evt);
            }
        });
        jTextFieldIntervalExp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldIntervalExpFocusGained(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelIntervalExpireLayout = new org.jdesktop.layout.GroupLayout(jPanelIntervalExpire);
        jPanelIntervalExpire.setLayout(jPanelIntervalExpireLayout);
        jPanelIntervalExpireLayout.setHorizontalGroup(
            jPanelIntervalExpireLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelIntervalExpireLayout.createSequentialGroup()
                .add(jTextFieldIntervalExpLow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelIntervalLow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldIntervalExp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanelIntervalExpireLayout.setVerticalGroup(
            jPanelIntervalExpireLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTextFieldIntervalExpLow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jLabelIntervalLow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jTextFieldIntervalExp)
        );

        jPanelIntervalExpireLayout.linkSize(new java.awt.Component[] {jTextFieldIntervalExp, jTextFieldIntervalExpLow}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jTextFieldEpisodeExp.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldEpisodeExp.setText("0.0");
        jTextFieldEpisodeExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEpisodeExpActionPerformed(evt);
            }
        });
        jTextFieldEpisodeExp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldEpisodeExpFocusGained(evt);
            }
        });

        jLabelEpisodeExp.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelEpisodeExp.setText("Episode expiry");
        jLabelEpisodeExp.setToolTipText("The total allowed time for the episode");

        jButtonSelectIntervals.setText("Enter Intervals");
        jButtonSelectIntervals.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelectIntervals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectIntervalsActionPerformed(evt);
            }
        });
        jButtonSelectIntervals.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButtonSelectIntervalsFocusGained(evt);
            }
        });

        jLabelMaxLevels.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelMaxLevels.setText("Episode size");

        jSpinnerMaxLevels.setFont(new java.awt.Font("Arial", 0, 11));
        // Create a number spinner that only handles values in the range [0,100]
        int min = 0;
        int max = 100;
        int step = 1;
        int initValue = 10;
        SpinnerModel jSpinnerModel = new SpinnerNumberModel(initValue, min, max, step);
        jSpinnerMaxLevels.setModel(jSpinnerModel);

        jButtonSelectDurations.setText("Enter Durations");
        jButtonSelectDurations.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelectDurations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectDurationsActionPerformed(evt);
            }
        });
        jButtonSelectDurations.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButtonSelectDurationsFocusGained(evt);
            }
        });

        jCheckBoxSegment.setText("Enable Segmenting");
        jCheckBoxSegment.setToolTipText("Enable data segmenting");
        jCheckBoxSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSegmentActionPerformed(evt);
            }
        });

        jLabelSeg1.setText("Size:");
        jLabelSeg1.setToolTipText("Segment size (in sec)");
        jLabelSeg1.setEnabled(false);

        jTextFieldSegmentSize.setText("2.0");
        jTextFieldSegmentSize.setEnabled(false);

        jLabelSeg2.setText("sec");
        jLabelSeg2.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabelSeg1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldSegmentSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jLabelSeg2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(127, 127, 127))
            .add(jPanel1Layout.createSequentialGroup()
                .add(jCheckBoxSegment)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jCheckBoxSegment)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelSeg1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldSegmentSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelSeg2))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanelUserInputsLayout = new org.jdesktop.layout.GroupLayout(jPanelUserInputs);
        jPanelUserInputs.setLayout(jPanelUserInputsLayout);
        jPanelUserInputsLayout.setHorizontalGroup(
            jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelUserInputsLayout.createSequentialGroup()
                .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelUserInputsLayout.createSequentialGroup()
                        .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanelUserInputsLayout.createSequentialGroup()
                                .add(jLabelEpisodeExp)
                                .add(3, 3, 3))
                            .add(jPanelUserInputsLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabelMaxLevels, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jTextFieldEpisodeExp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                            .add(jSpinnerMaxLevels))
                        .add(2, 2, 2)
                        .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jButtonSelectIntervals, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jButtonSelectDurations, 0, 0, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 14, Short.MAX_VALUE))
                    .add(jPanelUserInputsLayout.createSequentialGroup()
                        .add(jPanelIntervalExpire, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelUserInputsLayout.setVerticalGroup(
            jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelUserInputsLayout.createSequentialGroup()
                .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelIntervalExpire, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, 0, 56, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelUserInputsLayout.createSequentialGroup()
                        .add(jButtonSelectIntervals)
                        .add(6, 6, 6)
                        .add(jButtonSelectDurations))
                    .add(jPanelUserInputsLayout.createSequentialGroup()
                        .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabelEpisodeExp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jTextFieldEpisodeExp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanelUserInputsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSpinnerMaxLevels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabelMaxLevels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jPanelUserInputsLayout.linkSize(new java.awt.Component[] {jPanel1, jPanelIntervalExpire}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jAlgorithmLabel.setText("Algorithm");

        jComboBoxAlgorithm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxAlgorithmActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jAlgorithmEntryPanelLayout = new org.jdesktop.layout.GroupLayout(jAlgorithmEntryPanel);
        jAlgorithmEntryPanel.setLayout(jAlgorithmEntryPanelLayout);
        jAlgorithmEntryPanelLayout.setHorizontalGroup(
            jAlgorithmEntryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jAlgorithmEntryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jAlgorithmLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxAlgorithm, 0, 555, Short.MAX_VALUE))
        );
        jAlgorithmEntryPanelLayout.setVerticalGroup(
            jAlgorithmEntryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jAlgorithmEntryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jAlgorithmLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jComboBoxAlgorithm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanelFreqThreshold.setBorder(javax.swing.BorderFactory.createTitledBorder("Frequency Threshold"));

        jTabbedPaneThresholdParameters.setInheritsPopupMenu(true);

        jPanelAutomaticThreshold.setLayout(new java.awt.CardLayout());

        jPanelEStrong.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelEStrong.setAlignmentY(0.0F);

        jLabelEStrong.setText("Connection Strength");

        jTextFieldEStrong.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldEStrong.setText("0.2");
        jTextFieldEStrong.setMinimumSize(new java.awt.Dimension(22, 20));
        jTextFieldEStrong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEStrongActionPerformed(evt);
            }
        });
        jTextFieldEStrong.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldEStrongFocusLost(evt);
            }
        });

        jLabelErrorTypeI.setText("Type I Error");

        jTextFieldErrorTypeI.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldErrorTypeI.setText("0.050");
        jTextFieldErrorTypeI.setMinimumSize(new java.awt.Dimension(20, 20));
        jTextFieldErrorTypeI.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldErrorTypeIFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelEStrongLayout = new org.jdesktop.layout.GroupLayout(jPanelEStrong);
        jPanelEStrong.setLayout(jPanelEStrongLayout);
        jPanelEStrongLayout.setHorizontalGroup(
            jPanelEStrongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelEStrongLayout.createSequentialGroup()
                .add(jPanelEStrongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelEStrong)
                    .add(jLabelErrorTypeI))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEStrongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldErrorTypeI, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldEStrong, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanelEStrongLayout.setVerticalGroup(
            jPanelEStrongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelEStrongLayout.createSequentialGroup()
                .add(jPanelEStrongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelEStrong)
                    .add(jTextFieldEStrong, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEStrongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelErrorTypeI)
                    .add(jTextFieldErrorTypeI, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelAutomaticThreshold.add(jPanelEStrong, "serial");

        jLabel1.setText("Error for Parallel Episode");

        jTextFieldPoissonErrorProb.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldPoissonErrorProb.setText("0.01");

        org.jdesktop.layout.GroupLayout jPanelPoissonLayout = new org.jdesktop.layout.GroupLayout(jPanelPoisson);
        jPanelPoisson.setLayout(jPanelPoissonLayout);
        jPanelPoissonLayout.setHorizontalGroup(
            jPanelPoissonLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelPoissonLayout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldPoissonErrorProb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanelPoissonLayout.setVerticalGroup(
            jPanelPoissonLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelPoissonLayout.createSequentialGroup()
                .add(jPanelPoissonLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextFieldPoissonErrorProb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        jPanelAutomaticThreshold.add(jPanelPoisson, "parallel");

        jTabbedPaneThresholdParameters.addTab("Automatic Threshold", jPanelAutomaticThreshold);

        jPanelDecayingThreshold.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelDecayingThreshold.setAlignmentX(1.0F);

        jLabelFreqThOne.setText("First Node");

        jTextFieldFreqThOneNode.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldFreqThOneNode.setText("0.0");
        jTextFieldFreqThOneNode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldFreqThOneNodeFocusLost(evt);
            }
        });

        jLabelFreqTh.setText("Base Node");

        jTextFieldFreqTh.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldFreqTh.setText("0.0");
        jTextFieldFreqTh.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldFreqThFocusLost(evt);
            }
        });

        jLabelThDecay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelThDecay.setText("Decay");
        jLabelThDecay.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabelThDecay.setAlignmentX(0.5F);

        jTextFieldDecay.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldDecay.setText("1.0");
        jTextFieldDecay.setMinimumSize(new java.awt.Dimension(10, 6));
        jTextFieldDecay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldDecayFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelDecayingThresholdLayout = new org.jdesktop.layout.GroupLayout(jPanelDecayingThreshold);
        jPanelDecayingThreshold.setLayout(jPanelDecayingThresholdLayout);
        jPanelDecayingThresholdLayout.setHorizontalGroup(
            jPanelDecayingThresholdLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelDecayingThresholdLayout.createSequentialGroup()
                .add(jPanelDecayingThresholdLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelDecayingThresholdLayout.createSequentialGroup()
                        .add(jLabelThDecay)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldDecay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelDecayingThresholdLayout.createSequentialGroup()
                        .add(jLabelFreqThOne)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldFreqThOneNode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(11, 11, 11)
                .add(jLabelFreqTh)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldFreqTh, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE))
        );

        jPanelDecayingThresholdLayout.linkSize(new java.awt.Component[] {jLabelFreqThOne, jLabelThDecay}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanelDecayingThresholdLayout.setVerticalGroup(
            jPanelDecayingThresholdLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelDecayingThresholdLayout.createSequentialGroup()
                .add(jPanelDecayingThresholdLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelFreqThOne, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .add(jTextFieldFreqThOneNode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelFreqTh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldFreqTh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelDecayingThresholdLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelThDecay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldDecay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jTabbedPaneThresholdParameters.addTab("Fixed Threshold", jPanelDecayingThreshold);

        jPanelNegThreshold.setLayout(new java.awt.CardLayout());

        jLabel2.setText("Error for Parallel Episode");

        jTextFieldPoissonErrorProb1.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldPoissonErrorProb1.setText("0.01");

        org.jdesktop.layout.GroupLayout jPanelPoisson1Layout = new org.jdesktop.layout.GroupLayout(jPanelPoisson1);
        jPanelPoisson1.setLayout(jPanelPoisson1Layout);
        jPanelPoisson1Layout.setHorizontalGroup(
            jPanelPoisson1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelPoisson1Layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldPoissonErrorProb1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelPoisson1Layout.setVerticalGroup(
            jPanelPoisson1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelPoisson1Layout.createSequentialGroup()
                .add(jPanelPoisson1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextFieldPoissonErrorProb1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        jPanelNegThreshold.add(jPanelPoisson1, "parallel");

        jPanelEStrong1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelEStrong1.setAlignmentY(0.0F);

        jLabelEStrong1.setText("Connection Strength");

        jTextFieldEStrong1.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldEStrong1.setText("0.2");
        jTextFieldEStrong1.setMinimumSize(new java.awt.Dimension(22, 20));
        jTextFieldEStrong1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEStrong1ActionPerformed(evt);
            }
        });
        jTextFieldEStrong1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldEStrong1FocusLost(evt);
            }
        });

        jLabelErrorTypeI1.setText("Type I Error");

        jTextFieldErrorTypeI1.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldErrorTypeI1.setText("0.050");
        jTextFieldErrorTypeI1.setMinimumSize(new java.awt.Dimension(20, 20));
        jTextFieldErrorTypeI1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldErrorTypeI1FocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelEStrong1Layout = new org.jdesktop.layout.GroupLayout(jPanelEStrong1);
        jPanelEStrong1.setLayout(jPanelEStrong1Layout);
        jPanelEStrong1Layout.setHorizontalGroup(
            jPanelEStrong1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelEStrong1Layout.createSequentialGroup()
                .add(jPanelEStrong1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelEStrong1)
                    .add(jLabelErrorTypeI1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEStrong1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldErrorTypeI1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldEStrong1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)))
        );
        jPanelEStrong1Layout.setVerticalGroup(
            jPanelEStrong1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelEStrong1Layout.createSequentialGroup()
                .add(jPanelEStrong1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelEStrong1)
                    .add(jTextFieldEStrong1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelEStrong1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelErrorTypeI1)
                    .add(jTextFieldErrorTypeI1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelNegThreshold.add(jPanelEStrong1, "serial");

        jTabbedPaneThresholdParameters.addTab("Neg. Th.", jPanelNegThreshold);

        jTextFieldTimeGranularity.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTimeGranularity.setText("0.001");
        jTextFieldTimeGranularity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTimeGranularityActionPerformed(evt);
            }
        });
        jTextFieldTimeGranularity.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldTimeGranularityFocusLost(evt);
            }
        });

        jLabel3.setText("sec");

        jLabelTimeGranularity.setText("Time  Granularity");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jLabelTimeGranularity)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldTimeGranularity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addContainerGap(48, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabelTimeGranularity)
                .add(jTextFieldTimeGranularity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel3))
        );

        org.jdesktop.layout.GroupLayout jPanelFreqThresholdLayout = new org.jdesktop.layout.GroupLayout(jPanelFreqThreshold);
        jPanelFreqThreshold.setLayout(jPanelFreqThresholdLayout);
        jPanelFreqThresholdLayout.setHorizontalGroup(
            jPanelFreqThresholdLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jTabbedPaneThresholdParameters, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanelFreqThresholdLayout.setVerticalGroup(
            jPanelFreqThresholdLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelFreqThresholdLayout.createSequentialGroup()
                .add(jTabbedPaneThresholdParameters, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout jPanelSetAlgorithmLayout = new org.jdesktop.layout.GroupLayout(jPanelSetAlgorithm);
        jPanelSetAlgorithm.setLayout(jPanelSetAlgorithmLayout);
        jPanelSetAlgorithmLayout.setHorizontalGroup(
            jPanelSetAlgorithmLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSetAlgorithmLayout.createSequentialGroup()
                .add(jPanelFreqThreshold, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelUserInputs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jAlgorithmEntryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelSetAlgorithmLayout.setVerticalGroup(
            jPanelSetAlgorithmLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSetAlgorithmLayout.createSequentialGroup()
                .add(jAlgorithmEntryPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelSetAlgorithmLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanelUserInputs, 0, 169, Short.MAX_VALUE)
                    .add(jPanelFreqThreshold, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPaneCandidates.setPreferredSize(new java.awt.Dimension(150, 133));
        jTabbedPaneCandidates.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPaneCandidatesStateChanged(evt);
            }
        });

        jComboBoxCandidates.setEnabled(false);
        jComboBoxCandidates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCandidatesActionPerformed(evt);
            }
        });

        jCheckBoxAllowRepeats.setSelected(true);
        jCheckBoxAllowRepeats.setText("Allow repeated event types");
        jCheckBoxAllowRepeats.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxAllowRepeats.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jCheckBoxPermutations.setSelected(true);
        jCheckBoxPermutations.setText("Show all cyclic permutations");
        jCheckBoxPermutations.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxPermutations.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxPermutations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxPermutationsActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelCandGenLayout = new org.jdesktop.layout.GroupLayout(jPanelCandGen);
        jPanelCandGen.setLayout(jPanelCandGenLayout);
        jPanelCandGenLayout.setHorizontalGroup(
            jPanelCandGenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelCandGenLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelCandGenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxCandidates, 0, 229, Short.MAX_VALUE)
                    .add(jPanelCandGenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(jCheckBoxAllowRepeats, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jCheckBoxPermutations, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelCandGenLayout.setVerticalGroup(
            jPanelCandGenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelCandGenLayout.createSequentialGroup()
                .addContainerGap()
                .add(jComboBoxCandidates, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxAllowRepeats)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxPermutations)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneCandidates.addTab("Candidate generation", jPanelCandGen);

        jButtonAddEpisode.setText("Add an Episode");
        jButtonAddEpisode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddEpisodeActionPerformed(evt);
            }
        });

        jButtonLoadEpisodeSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Open16.gif"))); // NOI18N
        jButtonLoadEpisodeSet.setText("Load an Episode Set");
        jButtonLoadEpisodeSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadEpisodeSetActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelCandLoadLayout = new org.jdesktop.layout.GroupLayout(jPanelCandLoad);
        jPanelCandLoad.setLayout(jPanelCandLoadLayout);
        jPanelCandLoadLayout.setHorizontalGroup(
            jPanelCandLoadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelCandLoadLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelCandLoadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonAddEpisode, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonLoadEpisodeSet, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelCandLoadLayout.setVerticalGroup(
            jPanelCandLoadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelCandLoadLayout.createSequentialGroup()
                .addContainerGap()
                .add(jButtonAddEpisode)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonLoadEpisodeSet)
                .addContainerGap())
        );

        jTabbedPaneCandidates.addTab("Load Candidates", jPanelCandLoad);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Model (Optional)"));

        jComboBoxModel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Rigat Model", "Deb Model" }));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jComboBoxModel, 0, 240, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jComboBoxModel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jTopPanelLayout = new org.jdesktop.layout.GroupLayout(jTopPanel);
        jTopPanel.setLayout(jTopPanelLayout);
        jTopPanelLayout.setHorizontalGroup(
            jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTopPanelLayout.createSequentialGroup()
                .add(jPanelSetAlgorithm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jTabbedPaneCandidates, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)))
        );
        jTopPanelLayout.setVerticalGroup(
            jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTopPanelLayout.createSequentialGroup()
                .add(jTabbedPaneCandidates, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(jPanelSetAlgorithm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        jReportPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Results"), javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        jReportPanel.setLayout(new java.awt.BorderLayout(5, 5));

        jButtonMine.setText("Mine");
        jButtonMine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMineActionPerformed(evt);
            }
        });

        jButtonEpsStats.setText("Episode Stats");
        jButtonEpsStats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEpsStatsActionPerformed(evt);
            }
        });

        jButtonSaveEpisodes.setText("Save");
        jButtonSaveEpisodes.setEnabled(false);
        jButtonSaveEpisodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveEpisodesActionPerformed(evt);
            }
        });

        jButtonClear.setText("Clear");
        jButtonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelActionsLayout = new org.jdesktop.layout.GroupLayout(jPanelActions);
        jPanelActions.setLayout(jPanelActionsLayout);
        jPanelActionsLayout.setHorizontalGroup(
            jPanelActionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelActionsLayout.createSequentialGroup()
                .addContainerGap(482, Short.MAX_VALUE)
                .add(jButtonMine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonEpsStats)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonSaveEpisodes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonClear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelActionsLayout.setVerticalGroup(
            jPanelActionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelActionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jButtonMine)
                .add(jButtonEpsStats)
                .add(jButtonSaveEpisodes)
                .add(jButtonClear))
        );

        jReportPanel.add(jPanelActions, java.awt.BorderLayout.NORTH);

        jCardPanel.setLayout(new java.awt.CardLayout());

        jScrollPane3.setViewportView(jTextPaneDetails);

        jCardPanel.add(jScrollPane3, "details");

        jTableEpisodes.setModel(getTableModel());
        jTableEpisodes.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTableEpisodes.setIntercellSpacing(new java.awt.Dimension(2, 2));
        // Code to adjust column widths
        initColumnSizes(jTableEpisodes);
        jScrollPane2.setViewportView(jTableEpisodes);

        jCardPanel.add(jScrollPane2, "episode");

        org.jdesktop.layout.GroupLayout jPanelResultCardsLayout = new org.jdesktop.layout.GroupLayout(jPanelResultCards);
        jPanelResultCards.setLayout(jPanelResultCardsLayout);
        jPanelResultCardsLayout.setHorizontalGroup(
            jPanelResultCardsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 761, Short.MAX_VALUE)
            .add(jPanelResultCardsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jCardPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE))
        );
        jPanelResultCardsLayout.setVerticalGroup(
            jPanelResultCardsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 241, Short.MAX_VALUE)
            .add(jPanelResultCardsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jCardPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE))
        );

        jReportPanel.add(jPanelResultCards, java.awt.BorderLayout.CENTER);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(150, 322));

        jTreeEpisodes.setModel(null);
        jScrollPane1.setViewportView(jTreeEpisodes);

        org.jdesktop.layout.GroupLayout jPanel14Layout = new org.jdesktop.layout.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
        );

        jReportPanel.add(jPanel14, java.awt.BorderLayout.WEST);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTopPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jReportPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 948, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jTopPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jReportPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxBackPruningEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxBackPruningEnabledActionPerformed
        System.out.println("^.^: " + String.valueOf(jCheckBoxBackPruningEnabled.isSelected()));
        stateInfo.getSession().setBackPruningEnabled(jCheckBoxBackPruningEnabled.isSelected());
    }//GEN-LAST:event_jCheckBoxBackPruningEnabledActionPerformed

    private void jTextFieldTimeGranularityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTimeGranularityFocusLost
        try
        {
            double timeGranularity = Double.parseDouble(jTextFieldTimeGranularity.getText());
            stateInfo.getSession().setTimeGranularity(timeGranularity);
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(frame, "Please enter a number",
                    "Error converting to number", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jTextFieldTimeGranularityFocusLost

    private void jTextFieldErrorTypeIFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldErrorTypeIFocusLost
        try
        {
            double errorTypeI = Double.parseDouble(jTextFieldErrorTypeI.getText());
            stateInfo.getSession().setErrorTypeI(errorTypeI);
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(frame, "Please enter a number",
                    "Error converting to number", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jTextFieldErrorTypeIFocusLost

    private void jTextFieldEStrongFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldEStrongFocusLost
        try
        {
            double eStrong = Double.parseDouble(jTextFieldEStrong.getText());
            stateInfo.getSession().setEStrong(eStrong);
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(frame, "Please enter a number",
                    "Error converting to number", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jTextFieldEStrongFocusLost

    private void jTextFieldDecayFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDecayFocusLost
        try
        {
            double freqDecay = Double.parseDouble(jTextFieldDecay.getText());
            stateInfo.getSession().setFreqDecay(freqDecay);
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(frame, "Please enter a number",
                    "Error converting to number", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jTextFieldDecayFocusLost

    private void jTextFieldFreqThFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldFreqThFocusLost
        try
        {
            double frequencyThreshold = Double.parseDouble(jTextFieldFreqTh.getText());
            stateInfo.getSession().setFrequencyThreshold(2, frequencyThreshold);
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(frame, "Please enter a number",
                    "Error converting to number", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jTextFieldFreqThFocusLost

    private void jTextFieldFreqThOneNodeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldFreqThOneNodeFocusLost
        try
        {
            double oneNodeFreqTh = Double.parseDouble(jTextFieldFreqThOneNode.getText());
            stateInfo.getSession().setFrequencyThreshold(1, oneNodeFreqTh);
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(frame, "Please enter a number",
                    "Error converting to number", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jTextFieldFreqThOneNodeFocusLost

    private void jTextFieldEStrongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEStrongActionPerformed
    }//GEN-LAST:event_jTextFieldEStrongActionPerformed

    private void jButtonSelectIntervalsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButtonSelectIntervalsFocusGained
        
    }//GEN-LAST:event_jButtonSelectIntervalsFocusGained

    private void jTextFieldEpisodeExpFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldEpisodeExpFocusGained
        // This field is used by more than one algorithm.
        // It may be inappropriate to for a particular counter.
        int index = jComboBoxAlgorithm.getSelectedIndex();
        AlgoEntry entry = algos.get(index);

        if (entry.isSerial())
        {
            setActiveAlgorithm(edu.iisc.tdminercore.counter.NonOverlappedEpisodeCounter.class);
        }
        else
        {
            setActiveAlgorithm(edu.iisc.tdminercore.counter.ParallelEpisodesCounterWithExpiry.class);
        }
    }//GEN-LAST:event_jTextFieldEpisodeExpFocusGained

    private void jTextFieldIntervalExpFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldIntervalExpFocusGained
        setActiveAlgorithm(edu.iisc.tdminercore.counter.SerialTrueIntervalCounter.class);
    }//GEN-LAST:event_jTextFieldIntervalExpFocusGained

    private void jTextFieldIntervalExpLowFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldIntervalExpLowFocusGained
        setActiveAlgorithm(edu.iisc.tdminercore.counter.SerialTrueIntervalCounter.class);
    }//GEN-LAST:event_jTextFieldIntervalExpLowFocusGained

    private void jButtonEpsStatsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonEpsStatsActionPerformed
    {//GEN-HEADEREND:event_jButtonEpsStatsActionPerformed
        EpisodeSet episodes = stateInfo.getSession().getEpisodes();
        if (episodes != null)
        {
            int row = jTableEpisodes.getSelectedRow();
            if (row != -1)
            {
                IEpisode episode = model.getEpisode(row);
                EventFactor eventType = model.getEventTypes();
                DisplayEpisodeStats dialog = new DisplayEpisodeStats(frame, episode,
                        eventType, stateInfo.getSession().getCurrentSegIndex());
                dialog.setVisible(true);
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Select an episode from the results",
                        "No Episode selected", JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "The datasequence needs to be mined before viewing statistics",
                    "No Episodes found", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonEpsStatsActionPerformed

    private void jTextFieldEpisodeExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEpisodeExpActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldEpisodeExpActionPerformed

    /**
     * This method creates a dialog for monitoring and controlling the
     * datamining activity.
     */
    private void jButtonSelectIntervalsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSelectIntervalsActionPerformed
    {//GEN-HEADEREND:event_jButtonSelectIntervalsActionPerformed
        setActiveAlgorithm(edu.iisc.tdminercore.counter.SerialEpisodeCounterWithIntervals.class);
        int index = jComboBoxAlgorithm.getSelectedIndex();
        AlgoEntry entry = algos.get(index);
        
        AbstractEpisodeCounter counter = entry.getCounter();
        
        List<Interval> intervalsList = stateInfo.getSession().getIntervalsList();
        JDialog dialog = new JDialog(frame, "Interval selection", true);
        dialog.setSize(300, 400);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        if (counter instanceof SerialEpisodeCounterWithRigidDelays)
        {
            RigidDelayInputPanel inp = new RigidDelayInputPanel(dialog, stateInfo.getSession());
            dialog.getContentPane().add(inp);
            if (intervalsList != null)
            {
                inp.setTimeIntervalsList(intervalsList);
            }
            if (frame != null)
            {
                int x = (int) (frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth()) / 2);
                int y = (int) (frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight()) / 2);
                dialog.setLocation(x, y);
            }
            dialog.setVisible(true);
            intervalsList = inp.getTimeIntervalsList();
        }
        else
        {
            IntervalInputPanel inp = new IntervalInputPanel(dialog, IntervalInputPanel.INTERVALS_DISP);
            dialog.getContentPane().add(inp);
            if (intervalsList != null)
            {
                inp.setTimeIntervalsList(intervalsList);
            }
            if (frame != null)
            {
                int x = (int) (frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth()) / 2);
                int y = (int) (frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight()) / 2);
                dialog.setLocation(x, y);
            }
            dialog.setVisible(true);
            intervalsList = inp.getTimeIntervalsList();
        }

        stateInfo.getSession().setIntervalsList(intervalsList);

        // persist the settings
        Preferences prefs = Preferences.userNodeForPackage(TDMinerInterface.class);
        prefs.put("intervals", stateInfo.getSession().getIntervalsListMarshall());
    }//GEN-LAST:event_jButtonSelectIntervalsActionPerformed

    private void jCheckBoxPermutationsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxPermutationsActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxPermutationsActionPerformed
        EpisodeSet episodes = stateInfo.getSession().getEpisodes();
        if (jCheckBoxPermutations.isSelected() && episodes != null)
        {
            displayResults();
        }
        else
        {
            displayResultsWithoutPermutations();
        }
    }//GEN-LAST:event_jCheckBoxPermutationsActionPerformed

    private void jTextFieldIntervalExpLowActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldIntervalExpLowActionPerformed
    {//GEN-HEADEREND:event_jTextFieldIntervalExpLowActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldIntervalExpLowActionPerformed

    private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonClearActionPerformed
    {//GEN-HEADEREND:event_jButtonClearActionPerformed
        model.setData(null, 0);
        jTreeEpisodes.setModel(null);
        jTextPaneDetails.setText("");
        stateInfo.getSession().setEpisodes(null);
    }//GEN-LAST:event_jButtonClearActionPerformed

    private void jComboBoxAlgorithmActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jComboBoxAlgorithmActionPerformed
    {//GEN-HEADEREND:event_jComboBoxAlgorithmActionPerformed
        fitDefaultParameters();
    }//GEN-LAST:event_jComboBoxAlgorithmActionPerformed

    private void recursiveSetEnabled(Container p, boolean flag)
    {
        if (p.getComponentCount() > 0)
        {
            for (Component c : p.getComponents())
            {
                c.setEnabled(flag);
                if (c instanceof Container)
                {
                    recursiveSetEnabled((Container) c, flag);
                }
            }
        }
    }

    private void jTabbedPaneCandidatesStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jTabbedPaneCandidatesStateChanged
    {//GEN-HEADEREND:event_jTabbedPaneCandidatesStateChanged
        JTabbedPane pane = (JTabbedPane) evt.getSource();

        // Get current tab
        int sel = pane.getSelectedIndex();
        if (pane.getComponentAt(sel) == jPanelCandGen)
        {
            candidateSelectionIndex = COMPLETE_MINING;

            jLabelFreqTh.setEnabled(true);
            jLabelFreqThOne.setEnabled(true);
            jLabelThDecay.setEnabled(true);
            jTextFieldDecay.setEnabled(true);
            jTextFieldFreqTh.setEnabled(true);
            jTextFieldFreqThOneNode.setEnabled(true);

            recursiveSetEnabled(jPanelFreqThreshold, true);

            jLabelMaxLevels.setEnabled(true);
            jSpinnerMaxLevels.setEnabled(true);
            jButtonMine.setText("Mine");
        }
        else if (pane.getComponentAt(sel) == jPanelCandLoad)
        {
            candidateSelectionIndex = COUNTING_ONLY;
            jLabelFreqTh.setEnabled(false);
            jLabelFreqThOne.setEnabled(false);
            jLabelMaxLevels.setEnabled(false);
            jLabelThDecay.setEnabled(false);

            jTextFieldDecay.setEnabled(false);
            jTextFieldFreqTh.setEnabled(false);
            jTextFieldFreqThOneNode.setEnabled(false);
            jSpinnerMaxLevels.setEnabled(false);
            recursiveSetEnabled(jPanelFreqThreshold, false);

            jButtonMine.setText("Count");
        }
    }//GEN-LAST:event_jTabbedPaneCandidatesStateChanged

    private void jButtonAddEpisodeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddEpisodeActionPerformed
    {//GEN-HEADEREND:event_jButtonAddEpisodeActionPerformed
        if (DEBUG)
        {
            System.out.println("FrequentEpisodeMinerPanel: button add episode action performed");
        }
        IEventDataStream seq = sequence;
        if (seq == null)
        {
            jLabelStatus.setText("Error adding episodes");
            JOptionPane.showMessageDialog(this, "Events data stream is empty.", "Error adding episodes", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //String inputValue = JOptionPane.showInputDialog((Component)this, "Please enter an episode ( E.g. 'A B C D E') :");
        try
        {
            AbstractEpisodeCounter counter = null;
            int index = jComboBoxAlgorithm.getSelectedIndex();
            AlgoEntry entry = algos.get(index);
            if (!entry.isFake())
            {
                counter = entry.getCounter();
            }
            
            if (counter == null)
            {
                jLabelStatus.setText("Error adding episodes");
                JOptionPane.showMessageDialog(this, "Select a valid mining algorithm. " +
                        "Episode parsing depends in type of algorithm used", "Error adding episodes", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AddEpisodePanel.CallMode c = AddEpisodePanel.CallMode.COUNTING_OTHERS;
            if (counter instanceof AbstractSerialCounterInterEventConst)
                c = AddEpisodePanel.CallMode.COUNTING_WITH_INTERVALS;
            List<IEpisode> eList = AddEpisodePanel.getEpisode(seq.getEventFactor(), 
                    frame, c, stateInfo.getSession().getIntervalsList());

            EpisodeSet episodes = stateInfo.getSession().getEpisodes();
            if (episodes == null)
            {
                List episodesList = new ArrayList();
                EventFactor eventTypes = seq.getEventFactor();
                episodes = new EpisodeSet(episodesList, eventTypes);
                stateInfo.getSession().setEpisodes(episodes);
            }
            //episodes.addEpisode(inputValue);
            if (eList != null)
            {
                for (IEpisode e : eList)
                {
                    if (e == null)
                    {
                        continue;
                    }
                    episodes.addEpisode(e);
                    System.out.println("Added Episode :: " + e.toString(seq.getEventFactor()));
                }

                t = System.currentTimeMillis();
                isSequenceMined = false;
                displayResults();
            }
        }
        catch (Exception ex)
        {
            jLabelStatus.setText("Error adding episodes");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error adding episodes", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jButtonAddEpisodeActionPerformed

    private void jButtonLoadEpisodeSetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLoadEpisodeSetActionPerformed
    {//GEN-HEADEREND:event_jButtonLoadEpisodeSetActionPerformed
        IEventDataStream seq = sequence;
        if (seq == null)
        {
            jLabelStatus.setText("Error loading episodes");
            JOptionPane.showMessageDialog(this, "Events data stream is empty.",
                    "Error loading episodes", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        if (Constants.CURRENT_DIR != null)
        {
            fc.setCurrentDirectory(Constants.CURRENT_DIR);
        }
        int returnVal = fc.showOpenDialog(FrequentEpisodeMinerPanel.this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
            System.out.println("Load command cancelled by user.");
            return;
        }
        File file = fc.getSelectedFile();
        System.out.println("Loading from to : " + file);
        try
        {
            EpisodeSet episodes = EpisodeSet.buildEpisodeSet(file, seq.getEventFactor(),
                    stateInfo.getSession().getIntervalsList());
            stateInfo.getSession().setEpisodes(episodes);
            t = System.currentTimeMillis();
            isSequenceMined = false;
            displayResults();
        }
        catch (Exception ex)
        {
            jLabelStatus.setText("Error loading episodes");
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error loading episodes", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jButtonLoadEpisodeSetActionPerformed

    private void jButtonSaveEpisodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveEpisodesActionPerformed
        EpisodeSet episodes = stateInfo.getSession().getEpisodes();
        if (episodes == null)
        {
            jLabelStatus.setText("Error saving episodes");
            JOptionPane.showMessageDialog(this, "Episodes structure is empty.", "Error saving episodes", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        if (Constants.CURRENT_DIR != null)
        {
            fc.setCurrentDirectory(Constants.CURRENT_DIR);
        }
        int returnVal = fc.showSaveDialog(FrequentEpisodeMinerPanel.this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
            System.out.println("Save command cancelled by user.");
            return;
        }

        File file = fc.getSelectedFile();
        System.out.println("Saving to : " + file);
        try
        {
            PrintWriter out = new PrintWriter(file);
            out.println(episodes.toString(stateInfo.getSession()));
            out.close();
            JOptionPane.showMessageDialog(this.frame, "Episodes saved to " + file.getName());
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();
            jLabelStatus.setText("Error saving episodes");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error saving episodes", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSaveEpisodesActionPerformed

    private void jButtonMineActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonMineActionPerformed
    {//GEN-HEADEREND:event_jButtonMineActionPerformed
        mineDataStream();
    }//GEN-LAST:event_jButtonMineActionPerformed

    private void jComboBoxCandidatesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jComboBoxCandidatesActionPerformed
    {//GEN-HEADEREND:event_jComboBoxCandidatesActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxCandidatesActionPerformed

    private void jButtonSelectDurationsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSelectDurationsActionPerformed
    {//GEN-HEADEREND:event_jButtonSelectDurationsActionPerformed
        List<Interval> durationsList = stateInfo.getSession().getDurationsList();
        JDialog dialog = new JDialog(frame, "Durations selection", true);
        dialog.setSize(300, 400);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        IntervalInputPanel inp = new IntervalInputPanel(dialog, IntervalInputPanel.DURATION_DISP);
        if (durationsList != null)
        {
            inp.setTimeIntervalsList(durationsList);
        }
        dialog.getContentPane().add(inp);
        if (frame != null)
        {
            int x = (int) (frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth()) / 2);
            int y = (int) (frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight()) / 2);
            dialog.setLocation(x, y);
        }
        dialog.setVisible(true);

        durationsList = inp.getTimeIntervalsList();
        stateInfo.getSession().setDurationsList(durationsList);

        // persist the settings
        Preferences prefs = Preferences.userNodeForPackage(TDMinerInterface.class);
        prefs.put("durations", stateInfo.getSession().getDurationsListMarshall());
}//GEN-LAST:event_jButtonSelectDurationsActionPerformed

    private void jButtonSelectDurationsFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jButtonSelectDurationsFocusGained
    {//GEN-HEADEREND:event_jButtonSelectDurationsFocusGained
        setActiveAlgorithm(edu.iisc.tdminercore.counter.GeneralizedEpisodeCounter.class);
}//GEN-LAST:event_jButtonSelectDurationsFocusGained

private void jCheckBoxSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSegmentActionPerformed
    boolean flag = jCheckBoxSegment.isSelected();
    jLabelSeg1.setEnabled(flag);
    jLabelSeg2.setEnabled(flag);
    jTextFieldSegmentSize.setEnabled(flag);
}//GEN-LAST:event_jCheckBoxSegmentActionPerformed

private void jTextFieldTimeGranularityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTimeGranularityActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldTimeGranularityActionPerformed

private void jTextFieldEStrong1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEStrong1ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldEStrong1ActionPerformed

private void jTextFieldEStrong1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldEStrong1FocusLost
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldEStrong1FocusLost

private void jTextFieldErrorTypeI1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldErrorTypeI1FocusLost
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldErrorTypeI1FocusLost

private void jTextFieldIntervalExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldIntervalExpActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_jTextFieldIntervalExpActionPerformed
    
    public void handleTaskCompletion(int taskIndex) {
        jButtonMine.setEnabled(true);
        isSequenceMined = true;
        displayResults();
    }
    
    private class EpisodeInfo {
        public String name;
        public Object object;
        public int index;
        
        public EpisodeInfo(String name, Object object) {
            this.name = name;
            this.object = object;
        }
        
        public EpisodeInfo(String name, Object object, int index) {
            this.name = name;
            this.object = object;
            this.index = index;
        }

        public String toString() {
            return name;
        }
        
        public Object getObject() {
            return object;
        }
        
        public int getIndex() {
            return index;
        }
    }
    
    public void displayResults() {
        t = System.currentTimeMillis() - t;
        EpisodeSet episodes = stateInfo.getSession().getEpisodes();
        if (episodes == null) {
            System.out.println("No valid episodes to display");
            return;
        }
        String details = getMiningReport();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new EpisodeInfo("Frequent Episodes", details));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        for (int index = 0; index < stateInfo.getSession().getSegIndexLen(); index ++) {
            String title = nf.format(stateInfo.getSession().startTime(index)) + "-" 
                    + nf.format(stateInfo.getSession().endTime(index)) + " sec";
            DefaultMutableTreeNode root1 = new DefaultMutableTreeNode(title);
            for (int ix = 1; ix <= episodes.maxEpisodeSize(); ix++) {
                List list  = episodes.getEpisodeList(ix);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EpisodeInfo(ix + " node Episodes", list, index));
                root1.add(node);
            }
            root.add(root1);
        }        
        model.setEventTypes(episodes.getEventFactor());
        
        jTreeEpisodes.setModel(new DefaultTreeModel(root));
        card.show(jCardPanel, "details");
        jTextPaneDetails.setText(details);
        jButtonSaveEpisodes.setEnabled(true);
        jLabelStatus.setText("Mining completed at episode size = " + episodes.maxEpisodeSize());
    }
    
    
    public void displayResultsWithoutPermutations() {
        t = System.currentTimeMillis();
        EpisodeSet episodes = stateInfo.getSession().getEpisodes();
        //System.out.println(episodes);
        String details = getMiningReport();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new EpisodeInfo("Frequent Episodes", details));
        root.add(new DefaultMutableTreeNode(new EpisodeInfo("Details", details)));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        
        for (int index = 0; index < stateInfo.getSession().getSegIndexLen(); index ++) {
            String title = nf.format(stateInfo.getSession().startTime(index)) + "-" 
                    + nf.format(stateInfo.getSession().endTime(index)) + " sec";
            DefaultMutableTreeNode root1 = new DefaultMutableTreeNode(title);
            for (int ix = 1; ix <= episodes.maxEpisodeSize(); ix++) {
                List list  = episodes.getEpisodeList(ix);
                List newList = null;
                if (list != null) {
                    //Episode.sort(list, Episode.FREQUENCY_ORDER);
                    newList = new ArrayList();
                    for (int jx = 0; jx < list.size(); jx++) {
                        boolean flag = true;
                        AbstractEpisode refEps = (AbstractEpisode)list.get(jx);
                        for(int kx = 0; kx < newList.size(); kx++) {
                            AbstractEpisode chkEps = (AbstractEpisode)newList.get(kx);
                            if (chkEps.isPermutation(refEps)) {
                                if (chkEps.getVotes(index) < refEps.getVotes(index)) {
                                    newList.set(kx, refEps);
                                }
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            newList.add(refEps);
                        }
                    }
                }
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EpisodeInfo(ix + " node Episodes", newList));
                root1.add(node);
            }
            root.add(root1);
        }
        model.setEventTypes(episodes.getEventFactor());
        
        jTreeEpisodes.setModel(new DefaultTreeModel(root));
        card.show(jCardPanel, "details");
        jTextPaneDetails.setText(details);
        jButtonSaveEpisodes.setEnabled(true);
        jLabelStatus.setText("Mining completed at episode size = " + episodes.maxEpisodeSize());
    }
    
    private String getEpisodesReport(List list, double th) {
        int len = sequence.getSize();
        EventFactor eventTypes = sequence.getEventFactor();
        
        StringBuffer buf = new StringBuffer();
        String endl = System.getProperty("line.separator");
        buf.append("Frequent episode discovery report" + endl);
        buf.append("---------------------------------" + endl + endl);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        
        for (int index = 0; index < stateInfo.getSession().getSegIndexLen(); index++) {
            String title = nf.format(stateInfo.getSession().startTime(index)) + "-" 
                    + nf.format(stateInfo.getSession().endTime(index)) + " sec";
            buf.append(title + endl);
            if (list != null && list.size() > 0) {
                buf.append("No. of frequent episodes = " + list.size() + endl);
                switch (stateInfo.getSession().getThresholdType()) {
                    case EXPLICIT_DECAY:
                        buf.append("Frequency threshold = " + ((int)(len * th)) 
                                + " (" + nf.format(th)+ " out of " + len + ")" + endl);
                }
                int i = 0;
                AbstractEpisode e = (AbstractEpisode)list.get(i);
                int max = e.getVotes(index);
                AbstractEpisode maxEpisode = e;
                int min = e.getVotes(index);
                AbstractEpisode minEpisode = e;

                for (i = 1; i < list.size(); i++) {
                    e = (AbstractEpisode)list.get(i);
                    int count = e.getVotes(index);
                    if (count > max) {
                        max = count;
                        maxEpisode = e;
                    }

                    if (count < min) {
                        min = count;
                        minEpisode = e;
                    }
                }
                buf.append("Max. frequency = " + max + "  Episode : " + maxEpisode.toString(eventTypes) +  endl);
                buf.append("Min. frequency = " + min + "  Episode : " + minEpisode.toString(eventTypes) +  endl);
            }
        }
        
        return buf.toString();
    }
    
    public String getMiningReport() {
        NumberFormat nf = NumberFormat.getInstance();
        
        StringBuffer buf = new StringBuffer();
        String endl = System.getProperty("line.separator");
        buf.append("Frequent episode discovery report" + endl);
        buf.append("---------------------------------" + endl + endl);
        
        buf.append(new Date() + endl);
        
        if (isSequenceMined) {
            buf.append("Time taken = " + nf.format((double)(t/1000.0))+ " sec.");
        } else {
            buf.append("The episode set has not been mined yet." + endl);
            buf.append("Please note that some of the frequency counts shown may be " +
                    "(from file/user input) unreliable." + endl);
        }
        
        return buf.toString();
    }
    
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        jTreeEpisodes.getLastSelectedPathComponent();
        
        if (node == null) return;
        
        if (node.getUserObject() instanceof EpisodeInfo) {
            EpisodeInfo episodeInfo = (EpisodeInfo)node.getUserObject();
            Object linkedData = episodeInfo.getObject();
            if (linkedData instanceof String) {
                jTextPaneDetails.setText((String)linkedData);
                jTextPaneDetails.getCaret().setDot(0);
                card.show(jCardPanel, "details");
            } else if (linkedData instanceof List || linkedData == null) {
                int index = episodeInfo.getIndex();
                model.setData((List)linkedData, index);
                jTableEpisodes.setModel(model);
                initColumnSizes(jTableEpisodes);
                card.show(jCardPanel, "episode");
            }
        }
        columnIndex = -1;
        ascending = true;
    }
    
    
    class ColumnHeaderListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            JTable table = ((JTableHeader)evt.getSource()).getTable();
            TableColumnModel colModel = table.getColumnModel();
            
            // The index of the column whose header was clicked
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
            int mColIndex = table.convertColumnIndexToModel(vColIndex);
            
            // Return if not clicked on any column header
            if (vColIndex == -1) {
                return;
            }
            
            // Determine if mouse was clicked between column heads
            Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
            if (vColIndex == 0) {
                headerRect.width -= 3;    // Hard-coded constant
            } else {
                headerRect.grow(-3, 0);   // Hard-coded constant
            }
            if (!headerRect.contains(evt.getX(), evt.getY())) {
                // Mouse was clicked between column heads
                // vColIndex is the column head closest to the click
                
                // vLeftColIndex is the column head to the left of the click
                int vLeftColIndex = vColIndex;
                if (evt.getX() < headerRect.x) {
                    vLeftColIndex--;
                }
            }
            
            // Code to sort the tree using the column index
            if (vColIndex == columnIndex) {
                ascending = !ascending;
            } else {
                ascending = true;
                columnIndex = vColIndex;
            }
            List list = model.getData();
            int index = stateInfo.getSession().getCurrentSegIndex();
            if (columnIndex == FreqEpisodeTableModel.EPISODE_COLUMN) {
                Episode.sort(list, Episode.DICTIONARY_ORDER, ascending, model.getEventTypes(), index);
            } else if (columnIndex == FreqEpisodeTableModel.FREQUENCY_COLUMN) {
                Episode.sort(list, Episode.FREQUENCY_ORDER, ascending, index);
            }
            model.fireTableStructureChanged();
            FrequentEpisodeMinerPanel.this.initColumnSizes(jTableEpisodes);
        }
    }
    
    /**
     * If the algorithm isn't set and this field is filled in...
     * It is a pretty safe bet they user wants the serial interval algorithm.
     * @param instanceOf a predicate functor
     */
    private void setActiveAlgorithm( Class aclass ) {
        int index = jComboBoxAlgorithm.getSelectedIndex();
        AlgoEntry entry = algos.get(index);
        if (!entry.isFake()) return;
        
        entry = null;
        int algoIndex = 0;
        for (AlgoEntry algo : Algorithms.COUNTER_ALGOS) {
            AbstractEpisodeCounter counter = algo.getCounter();
            if ( aclass.isInstance(counter) ) {
                entry = algo;
                break;
            }
            algoIndex++;
        }
        if (entry == null) {
            System.err.println("No counter of the specified type available");
            return;
        }
        if (entry.isFake()) return;
        // int algoIndex = entry.getCandidateIndex();
        if (algoIndex < jComboBoxAlgorithm.getItemCount()) {
            jComboBoxAlgorithm.setSelectedIndex(algoIndex);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jAlgorithmEntryPanel;
    private javax.swing.JLabel jAlgorithmLabel;
    private javax.swing.JButton jButtonAddEpisode;
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonEpsStats;
    private javax.swing.JButton jButtonLoadEpisodeSet;
    private javax.swing.JButton jButtonMine;
    private javax.swing.JButton jButtonSaveEpisodes;
    private javax.swing.JButton jButtonSelectDurations;
    private javax.swing.JButton jButtonSelectIntervals;
    private javax.swing.JPanel jCardPanel;
    private javax.swing.JCheckBox jCheckBoxAllowRepeats;
    private javax.swing.JCheckBox jCheckBoxBackPruningEnabled;
    private javax.swing.JCheckBox jCheckBoxPermutations;
    private javax.swing.JCheckBox jCheckBoxSegment;
    private javax.swing.JComboBox jComboBoxAlgorithm;
    private javax.swing.JComboBox jComboBoxCandidates;
    private javax.swing.JComboBox jComboBoxModel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelEStrong;
    private javax.swing.JLabel jLabelEStrong1;
    private javax.swing.JLabel jLabelEpisodeExp;
    private javax.swing.JLabel jLabelErrorTypeI;
    private javax.swing.JLabel jLabelErrorTypeI1;
    private javax.swing.JLabel jLabelFreqTh;
    private javax.swing.JLabel jLabelFreqThOne;
    private javax.swing.JLabel jLabelIntervalLow;
    private javax.swing.JLabel jLabelMaxLevels;
    private javax.swing.JLabel jLabelSeg1;
    private javax.swing.JLabel jLabelSeg2;
    private javax.swing.JLabel jLabelThDecay;
    private javax.swing.JLabel jLabelTimeGranularity;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelActions;
    private javax.swing.JPanel jPanelAutomaticThreshold;
    private javax.swing.JPanel jPanelBackPruning;
    private javax.swing.JPanel jPanelCandGen;
    private javax.swing.JPanel jPanelCandLoad;
    private javax.swing.JPanel jPanelDecayingThreshold;
    private javax.swing.JPanel jPanelEStrong;
    private javax.swing.JPanel jPanelEStrong1;
    private javax.swing.JPanel jPanelFreqThreshold;
    private javax.swing.JPanel jPanelIntervalExpire;
    private javax.swing.JPanel jPanelNegThreshold;
    private javax.swing.JPanel jPanelPoisson;
    private javax.swing.JPanel jPanelPoisson1;
    private javax.swing.JPanel jPanelResultCards;
    private javax.swing.JPanel jPanelSetAlgorithm;
    private javax.swing.JPanel jPanelUserInputs;
    private javax.swing.JPanel jReportPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSpinner jSpinnerMaxLevels;
    private javax.swing.JTabbedPane jTabbedPaneCandidates;
    private javax.swing.JTabbedPane jTabbedPaneThresholdParameters;
    private javax.swing.JTable jTableEpisodes;
    private javax.swing.JTextField jTextFieldDecay;
    private javax.swing.JTextField jTextFieldEStrong;
    private javax.swing.JTextField jTextFieldEStrong1;
    private javax.swing.JTextField jTextFieldEpisodeExp;
    private javax.swing.JTextField jTextFieldErrorTypeI;
    private javax.swing.JTextField jTextFieldErrorTypeI1;
    private javax.swing.JTextField jTextFieldFreqTh;
    private javax.swing.JTextField jTextFieldFreqThOneNode;
    private javax.swing.JTextField jTextFieldIntervalExp;
    private javax.swing.JTextField jTextFieldIntervalExpLow;
    private javax.swing.JTextField jTextFieldPoissonErrorProb;
    private javax.swing.JTextField jTextFieldPoissonErrorProb1;
    private javax.swing.JTextField jTextFieldSegmentSize;
    private javax.swing.JTextField jTextFieldTimeGranularity;
    private javax.swing.JTextPane jTextPaneDetails;
    private javax.swing.JPanel jTopPanel;
    private javax.swing.JTree jTreeEpisodes;
    // End of variables declaration//GEN-END:variables
    
    
    // Variables used in the settings menu
    /**
     * This method connects a Prospector Settings specific menu to
     * the parent frame.
     * @param jMenuSettings the menu to which this menu will be attached.
     */
    private void initSettingsMenuComponents(javax.swing.JMenu jMenuSettings) {
        JMenuItem jChunkLimitMenuItem = new javax.swing.JMenuItem();
        jChunkLimitMenuItem.setText("Set Candidates Chunk Size...");
        jChunkLimitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int chunksize = stateInfo.getSession().getChunkLimit();
                String value = JOptionPane.showInputDialog(frame,
                        "Enter Candidate Chunk size (e.g. 5000)", Integer.toString(chunksize));
                
                if ((value != null) && (value.length() > 0)) {
                    try {
                        stateInfo.getSession().setChunkLimit(Integer.parseInt(value));
                    } catch(NumberFormatException nfe) {
                        stateInfo.getSession().setChunkLimit(5000);
                        JOptionPane.showMessageDialog(frame, "Please enter a number",
                                "Error converting to number", JOptionPane.ERROR_MESSAGE);
                    }
                    System.out.println("Alpha = " + stateInfo.getSession().getChunkLimit());
                }
            }
        });
        jMenuSettings.add(jChunkLimitMenuItem);
    }
}
