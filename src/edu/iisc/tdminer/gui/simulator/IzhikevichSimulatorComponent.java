/*
 * IzhikevichSimulatorComponent
 *
 * Created on Nov 3, 2008 10:00 AM
 */
package edu.iisc.tdminer.gui.simulator;

import edu.iisc.tdminer.util.Constants;
import edu.iisc.tdminer.gui.AddEpisodePanel;
import edu.iisc.tdminer.model.ThreadedProgressManager;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminer.gui.ITaskPanel;
import edu.iisc.tdminer.gui.simulator.PoissonSimulatorComponent.SaveMode;
import edu.iisc.tdminer.model.SimulatorEpisodesTableModel;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.simulation.ISimulator;
import edu.iisc.tdminercore.simulation.IzhikevichModel;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;

/**
 *
 * @author  Deb
 */
public class IzhikevichSimulatorComponent extends javax.swing.JPanel implements ITaskPanel, ISimulatorPanel
{
    private IzhikevichModel sim = new IzhikevichModel();
    private StateInfo stateInfo;
    private JLabel jLabelStatus;
    private JFrame frame;
    private SimulatorEpisodesTableModel tableModel;
    private String eventsString;
    private int selectedRow;
    private int selectedCol;
    private String tip;
    int condition = 0;

    /** Creates new form SimulatorModelOnePanel */
    public IzhikevichSimulatorComponent()
    {
        initComponents();
	setDefaultValues();
	
	tableModel = (SimulatorEpisodesTableModel)jTableEpisodes.getModel();
	tableModel.setSimulatorPanel(this);
	simulatorMatrixPlot.setContainer(this);
	parseEventTypes();
	simulatorMatrixPlot.repaint();
	TableColumn col = jTableEpisodes.getColumnModel().getColumn(0);
	col.setMaxWidth(40);
    }

    public void setParameters()
    {
	sim.setTupdate(jTextFieldTUpdate.getText());
	sim.setTtotal(jTextFieldTtotal.getText());
        sim.setTstart(jTextFieldTStart.getText());
        
        sim.setStr_a(jTextFieldA.getText());
        sim.setStr_b(jTextFieldB.getText());
        sim.setStr_c(jTextFieldC.getText());
        sim.setStr_d(jTextFieldD.getText());
        sim.setStr_w_strong(jTextFieldISpike.getText());
        sim.setStr_I_min(jTextFieldILow.getText());
        sim.setStr_I_max(jTextFieldIHigh.getText());
        sim.setSynapticDelayLow(new Integer(jSpinnerSynapticDelayLow.getValue().toString()));
        sim.setSynapticDelayHigh(new Integer(jSpinnerSynapticDelayHigh.getValue().toString()));
        sim.setStrRandStim(jTextFieldRandStim.getText());
        sim.computeParameters();

        sim.interconnectType = jComboBoxInterconType.getSelectedIndex();
	sim.randomLow = Double.parseDouble(jTextFieldWeightLow.getText());
	sim.randomHigh = Double.parseDouble(jTextFieldWeightHigh.getText());
        sim.interConnectFactor = Double.parseDouble(jTextFieldInterConnect.getText());
    }

    public void resetUI()
    {
	jTextFieldEventTypes.setText(sim.getFieldEventTypes());
	jComboBoxInterconType.setSelectedIndex(sim.getInterconType());
        
	jTextFieldTUpdate.setText(sim.getTupdate());
	jTextFieldTtotal.setText(sim.getTtotal());
        jTextFieldTStart.setText(sim.getTstart());
	jTextFieldWeightLow.setText(sim.getStrLow());
	jTextFieldWeightHigh.setText(sim.getStrHigh());
        jTextFieldInterConnect.setText(sim.getInterConnect());
        
        jTextFieldA.setText(sim.getStr_a());
        jTextFieldB.setText(sim.getStr_b());
        jTextFieldC.setText(sim.getStr_c());
        jTextFieldD.setText(sim.getStr_d());
        jTextFieldISpike.setText(sim.getStr_w_strong());
        jTextFieldILow.setText(sim.getStr_I_min());
        jTextFieldIHigh.setText(sim.getStr_I_max());
        jSpinnerSynapticDelayLow.setValue(sim.getSynapticDelayLow());
        jSpinnerSynapticDelayHigh.setValue(sim.getSynapticDelayHigh());
        jTextFieldRandStim.setText(sim.getStrRandStim());
    }
    
    public void setDefaultValues()
    {
	sim.setDefaultValues();
	resetUI();
    }

    public ISimulator getSimulator()
    {
        return sim;
    }

    public void setSimulator(ISimulator sim)
    {
        this.sim = (IzhikevichModel) sim;
    }
    
    private void setSynapseFunction()
    {
        AddSynapsefunctions dialog = new AddSynapsefunctions(this.frame, 
                sim, selectedRow, selectedCol);
        dialog.setVisible(true);
        simulatorMatrixPlot.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemUpdate = new javax.swing.JMenuItem();
        jMenuItemSynapseFunctions = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemSave = new javax.swing.JMenuItem();
        jMenuItemSaveWeights = new javax.swing.JMenuItem();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldEventTypes = new javax.swing.JTextField();
        jPanel21 = new javax.swing.JPanel();
        jButtonUpdateEventTypes = new javax.swing.JButton();
        jButtonLoadEventTypes = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableEpisodes = new javax.swing.JTable();
        jPanel13 = new javax.swing.JPanel();
        jButtonReset = new javax.swing.JButton();
        jButtonAddEpisode = new javax.swing.JButton();
        jButtonInterconnect = new javax.swing.JButton();
        jButtonGenerate = new javax.swing.JButton();
        jPanelSimulatorSpecific = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanelRandomConnStrength = new javax.swing.JPanel();
        jLabelRandConnStrengthTitle = new javax.swing.JLabel();
        jPanelRandomConnStrengthRange = new javax.swing.JPanel();
        jTextFieldWeightLow = new javax.swing.JTextField();
        jLabelRandomConnStrengthBase = new javax.swing.JLabel();
        jTextFieldWeightHigh = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jComboBoxInterconType = new javax.swing.JComboBox();
        jPanel15 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldInterConnect = new javax.swing.JTextField();
        jPanelSimulatorPanel = new javax.swing.JPanel();
        jPanelLevel1 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldTUpdate = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldTtotal = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldTStart = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jPanelLevel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldA = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldB = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldC = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldD = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldILow = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldIHigh = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextFieldISpike = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        jSpinnerSynapticDelayLow = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jSpinnerSynapticDelayHigh = new javax.swing.JSpinner();
        jLabel23 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldRandStim = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jPanelLevel3 = new javax.swing.JPanel();
        jPanel29 = new javax.swing.JPanel();
        simulatorMatrixPlot = new edu.iisc.tdminer.gui.simulator.IzhikevichMatrixPlot();

        jMenuItemUpdate.setText("Change beta");
        jMenuItemUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUpdateActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemUpdate);

        jMenuItemSynapseFunctions.setText("Add Synapse Function");
        jMenuItemSynapseFunctions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSynapseFunctionsActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSynapseFunctions);
        jPopupMenu.add(jSeparator1);

        jMenuItemSave.setText("Save Image");
        jMenuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSave);

        jMenuItemSaveWeights.setText("Save beta's");
        jMenuItemSaveWeights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveWeightsActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSaveWeights);

        setMaximumSize(new java.awt.Dimension(247, 135));
        setPreferredSize(new java.awt.Dimension(247, 135));
        setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Select Event Types"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel5.setLayout(new java.awt.BorderLayout(5, 5));

        jLabel8.setText("Event Types (space separated):");
        jPanel5.add(jLabel8, java.awt.BorderLayout.WEST);

        jTextFieldEventTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEventTypesActionPerformed(evt);
            }
        });
        jPanel5.add(jTextFieldEventTypes, java.awt.BorderLayout.CENTER);

        jPanel21.setLayout(new java.awt.GridLayout(1, 2, 5, 5));

        jButtonUpdateEventTypes.setText("Update");
        jButtonUpdateEventTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateEventTypesActionPerformed(evt);
            }
        });
        jPanel21.add(jButtonUpdateEventTypes);

        jButtonLoadEventTypes.setText("Load");
        jButtonLoadEventTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadEventTypesActionPerformed(evt);
            }
        });
        jPanel21.add(jButtonLoadEventTypes);

        jPanel5.add(jPanel21, java.awt.BorderLayout.EAST);

        add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.LINE_AXIS));

        jPanel20.setMaximumSize(new java.awt.Dimension(800, 2147483647));
        jPanel20.setPreferredSize(new java.awt.Dimension(500, 350));
        jPanel20.setLayout(new java.awt.BorderLayout());

        jPanel12.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Episodes"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5))));
        jPanel12.setLayout(new java.awt.BorderLayout(5, 5));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(350, 85));

        jTableEpisodes.setModel(new SimulatorEpisodesTableModel());
        jScrollPane1.setViewportView(jTableEpisodes);

        jPanel12.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel13.setLayout(new java.awt.GridBagLayout());

        jButtonReset.setText("Reset Simulator");
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel13.add(jButtonReset, gridBagConstraints);

        jButtonAddEpisode.setText("Add Episode");
        jButtonAddEpisode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddEpisodeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel13.add(jButtonAddEpisode, gridBagConstraints);

        jButtonInterconnect.setText("Interconnect");
        jButtonInterconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInterconnectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel13.add(jButtonInterconnect, gridBagConstraints);

        jButtonGenerate.setText("Generate Sequence");
        jButtonGenerate.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel13.add(jButtonGenerate, gridBagConstraints);

        jPanel12.add(jPanel13, java.awt.BorderLayout.EAST);

        jPanel20.add(jPanel12, java.awt.BorderLayout.CENTER);

        jPanelSimulatorSpecific.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Select model parameters"), javax.swing.BorderFactory.createEmptyBorder(2, 1, 2, 1)));
        jPanelSimulatorSpecific.setLayout(new java.awt.BorderLayout(5, 5));

        jPanel14.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        jPanelRandomConnStrength.setLayout(new java.awt.BorderLayout());

        jLabelRandConnStrengthTitle.setText("Random Connection Strength   ");
        jPanelRandomConnStrength.add(jLabelRandConnStrengthTitle, java.awt.BorderLayout.WEST);

        jPanelRandomConnStrengthRange.setMinimumSize(new java.awt.Dimension(20, 19));
        jPanelRandomConnStrengthRange.setPreferredSize(new java.awt.Dimension(40, 19));
        jPanelRandomConnStrengthRange.setLayout(new java.awt.GridLayout(1, 0));

        jTextFieldWeightLow.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldWeightLow.setMinimumSize(new java.awt.Dimension(30, 19));
        jTextFieldWeightLow.setPreferredSize(new java.awt.Dimension(45, 19));
        jPanelRandomConnStrengthRange.add(jTextFieldWeightLow);

        jLabelRandomConnStrengthBase.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelRandomConnStrengthBase.setText("to");
        jLabelRandomConnStrengthBase.setToolTipText("The base connection strength");
        jPanelRandomConnStrengthRange.add(jLabelRandomConnStrengthBase);

        jTextFieldWeightHigh.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldWeightHigh.setMinimumSize(new java.awt.Dimension(30, 19));
        jTextFieldWeightHigh.setPreferredSize(new java.awt.Dimension(45, 19));
        jPanelRandomConnStrengthRange.add(jTextFieldWeightHigh);

        jPanelRandomConnStrength.add(jPanelRandomConnStrengthRange, java.awt.BorderLayout.CENTER);

        jPanel14.add(jPanelRandomConnStrength);

        jPanel16.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jLabel17.setText("Interconnection Type");
        jPanel16.add(jLabel17);

        jComboBoxInterconType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uniform dist.", "Binomial dist.", "Fully connected" }));
        jPanel16.add(jComboBoxInterconType);

        jPanel15.setLayout(new java.awt.BorderLayout());

        jLabel9.setText("Interconnect");
        jPanel15.add(jLabel9, java.awt.BorderLayout.WEST);

        jTextFieldInterConnect.setText("0.5");
        jPanel15.add(jTextFieldInterConnect, java.awt.BorderLayout.CENTER);

        jPanel16.add(jPanel15);

        jPanel14.add(jPanel16);

        jPanelSimulatorSpecific.add(jPanel14, java.awt.BorderLayout.SOUTH);

        jPanelSimulatorPanel.setLayout(new java.awt.BorderLayout());

        jPanelLevel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelLevel1.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jPanel1.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel1.setText("<html>T<sub>update</sub> = </html>");
        jLabel1.setToolTipText("(Time resolution of updates)");
        jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);

        jTextFieldTUpdate.setEditable(false);
        jTextFieldTUpdate.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTUpdate.setText("0.001");
        jTextFieldTUpdate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTUpdateKeyReleased(evt);
            }
        });
        jPanel1.add(jTextFieldTUpdate, java.awt.BorderLayout.CENTER);

        jLabel3.setText("sec");
        jPanel1.add(jLabel3, java.awt.BorderLayout.EAST);

        jPanelLevel1.add(jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel2.setText("<html>T<sub>total</sub> = </html>");
        jLabel2.setToolTipText("(Total duration of simulation)");
        jPanel2.add(jLabel2, java.awt.BorderLayout.WEST);

        jTextFieldTtotal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTtotal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTtotalKeyReleased(evt);
            }
        });
        jPanel2.add(jTextFieldTtotal, java.awt.BorderLayout.CENTER);

        jLabel4.setText("sec");
        jPanel2.add(jLabel4, java.awt.BorderLayout.EAST);

        jPanelLevel1.add(jPanel2);

        jPanel9.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel13.setText("<html>T<sub>start</sub> = </html>");
        jLabel13.setToolTipText("(Time resolution of updates)");
        jPanel9.add(jLabel13, java.awt.BorderLayout.WEST);

        jTextFieldTStart.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTStart.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTStartKeyReleased(evt);
            }
        });
        jPanel9.add(jTextFieldTStart, java.awt.BorderLayout.CENTER);

        jLabel14.setText("sec");
        jPanel9.add(jLabel14, java.awt.BorderLayout.EAST);

        jPanelLevel1.add(jPanel9);

        jPanelSimulatorPanel.add(jPanelLevel1, java.awt.BorderLayout.NORTH);

        jPanelLevel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel5.setText("Neuron: a = ");

        jTextFieldA.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldA.setText("0.02");

        jLabel6.setText("b = ");

        jTextFieldB.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldB.setText("0.2");

        jLabel7.setText("c = ");

        jTextFieldC.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldC.setText("-65");

        jLabel10.setText("d = ");

        jTextFieldD.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldD.setText("8");

        jLabel16.setText("Input Current: Noise range");

        jTextFieldILow.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldILow.setText("-5");

        jLabel18.setText("to");

        jTextFieldIHigh.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldIHigh.setText("5");

        jLabel19.setText("Spike");

        jTextFieldISpike.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldISpike.setText("20");

        jCheckBox1.setText("Enable STDP");
        jCheckBox1.setEnabled(false);

        jLabel20.setText("<html>T<sub>synapse low</sub> = </html>");
        jLabel20.setToolTipText("(Synaptic delay)");

        jSpinnerSynapticDelayLow.setMinimumSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayLow.setPreferredSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayLow.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jSpinnerSynapticDelayLowKeyReleased(evt);
            }
        });

        jLabel21.setText("<html>x T<sub>update</sub> sec</html>");

        jLabel22.setText("<html>T<sub>synapse high</sub> = </html>");
        jLabel22.setToolTipText("(Synaptic delay)");

        jSpinnerSynapticDelayHigh.setMinimumSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayHigh.setPreferredSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayHigh.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jSpinnerSynapticDelayHighKeyReleased(evt);
            }
        });

        jLabel23.setText("<html>x T<sub>update</sub> sec</html>");

        jLabel11.setText("Random Stimulation:");

        jTextFieldRandStim.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldRandStim.setText("20");

        jLabel12.setText("Hz");

        org.jdesktop.layout.GroupLayout jPanelLevel2Layout = new org.jdesktop.layout.GroupLayout(jPanelLevel2);
        jPanelLevel2.setLayout(jPanelLevel2Layout);
        jPanelLevel2Layout.setHorizontalGroup(
            jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelLevel2Layout.createSequentialGroup()
                .add(jLabel20)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSpinnerSynapticDelayLow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel21)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel22)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSpinnerSynapticDelayHigh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel23))
            .add(jPanelLevel2Layout.createSequentialGroup()
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldA, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanelLevel2Layout.createSequentialGroup()
                .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel16)
                    .add(jCheckBox1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelLevel2Layout.createSequentialGroup()
                        .add(jTextFieldILow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel18)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldIHigh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel19)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldISpike, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelLevel2Layout.createSequentialGroup()
                        .add(jLabel11)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldRandStim)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanelLevel2Layout.setVerticalGroup(
            jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelLevel2Layout.createSequentialGroup()
                .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jSpinnerSynapticDelayLow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jSpinnerSynapticDelayHigh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jTextFieldA, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel6)
                        .add(jTextFieldB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel7)
                        .add(jTextFieldC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel10)
                        .add(jTextFieldD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel16)
                    .add(jTextFieldILow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel18)
                    .add(jTextFieldIHigh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel19)
                    .add(jTextFieldISpike, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelLevel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel11)
                        .add(jTextFieldRandStim, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel12))
                    .add(jCheckBox1)))
        );

        jPanelSimulatorPanel.add(jPanelLevel2, java.awt.BorderLayout.CENTER);

        jPanelLevel3.setLayout(new java.awt.BorderLayout(15, 0));
        jPanelSimulatorPanel.add(jPanelLevel3, java.awt.BorderLayout.SOUTH);

        jPanelSimulatorSpecific.add(jPanelSimulatorPanel, java.awt.BorderLayout.CENTER);

        jPanel20.add(jPanelSimulatorSpecific, java.awt.BorderLayout.NORTH);

        jPanel11.add(jPanel20);

        jPanel29.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Pair-wise conditional probability"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel29.setMinimumSize(new java.awt.Dimension(400, 350));
        jPanel29.setPreferredSize(new java.awt.Dimension(500, 350));
        jPanel29.setLayout(new javax.swing.BoxLayout(jPanel29, javax.swing.BoxLayout.Y_AXIS));

        simulatorMatrixPlot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                simulatorMatrixPlotMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                simulatorMatrixPlotMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                simulatorMatrixPlotMouseReleased(evt);
            }
        });
        simulatorMatrixPlot.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                simulatorMatrixPlotMouseMoved(evt);
            }
        });
        simulatorMatrixPlot.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 2));
        jPanel29.add(simulatorMatrixPlot);

        jPanel11.add(jPanel29);

        add(jPanel11, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldTtotalKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldTtotalKeyReleased
    {//GEN-HEADEREND:event_jTextFieldTtotalKeyReleased
// TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTtotalKeyReleased

    private void jTextFieldTUpdateKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldTUpdateKeyReleased
    {//GEN-HEADEREND:event_jTextFieldTUpdateKeyReleased
    }//GEN-LAST:event_jTextFieldTUpdateKeyReleased

    private void jTextFieldTStartKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldTStartKeyReleased
    {//GEN-HEADEREND:event_jTextFieldTStartKeyReleased
        // TODO add your handling code here:
}//GEN-LAST:event_jTextFieldTStartKeyReleased

private void jTextFieldEventTypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEventTypesActionPerformed
    parseEventTypes();
}//GEN-LAST:event_jTextFieldEventTypesActionPerformed

private void jButtonUpdateEventTypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateEventTypesActionPerformed
    parseEventTypes();
}//GEN-LAST:event_jButtonUpdateEventTypesActionPerformed

private void jButtonLoadEventTypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadEventTypesActionPerformed
    JDialog dialog = new JDialog(frame, "Event types selection", true);
    dialog.setSize(500, 300);
    if (frame != null)
    {
        int x = (int) (frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth()) / 2);
        int y = (int) (frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight()) / 2);
        dialog.setLocation(x, y);
    }
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    LoadEventTypesPanel panel = new LoadEventTypesPanel(dialog);
    dialog.getContentPane().add(panel);
    dialog.setVisible(true);
    parseEventTypes();

}//GEN-LAST:event_jButtonLoadEventTypesActionPerformed

private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
    setDefaultValues();
    parseEventTypes();
}//GEN-LAST:event_jButtonResetActionPerformed

private void jButtonAddEpisodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddEpisodeActionPerformed
    parseEventTypes();
    if (sim.getEventFactor() == null)
    {
        return;
    }
    List episodesList = sim.getEpisodesList();
    try
    {
        List<IEpisode> eList =
                AddEpisodePanel.getEpisode(sim.getEventFactor(),
                frame, AddEpisodePanel.CallMode.SIMULATOR, null);
        if (eList != null)
        {
            for (IEpisode e : eList)
            {
                if (episodesList.contains(e))
                {
                    JOptionPane.showMessageDialog(this, "Episode " + e.toString(sim.getEventFactor()) + " already exists",
                            "Error adding episode",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                System.out.println("Added Episode :: " + e.toString(sim.getEventFactor()));
                episodesList.add(e);
                tableModel.fireTableDataChanged();
            }
        }
    }
    catch (Exception e)
    {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error adding episode", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_jButtonAddEpisodeActionPerformed

private void jButtonInterconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInterconnectActionPerformed
    try
    {
        setParameters();
        parseEventTypes();
        sim.interconnect();
        simulatorMatrixPlot.repaint();
        jLabelStatus.setText("Click on the connection matrix to view connection weight and modify using the pop up menu");
    }
    catch (NumberFormatException nfe)
    {
        jLabelStatus.setText("Error reading parameters");
        JOptionPane.showMessageDialog(this, nfe.getMessage(), "Error reading parameters", JOptionPane.ERROR_MESSAGE);
    }
    catch (Exception e)
    {
        jLabelStatus.setText("Error in interconnecting");
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error in interconnecting", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_jButtonInterconnectActionPerformed

private void jButtonGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateActionPerformed
    try
    {
        setParameters();
        sim.generateDataStream(new ThreadedProgressManager(this.frame, this)
        {

            @Override
            public void exceptionOccured(Exception e)
            {
                super.exceptionOccured(e);
                jButtonGenerate.setEnabled(true);
            }
        });
        jButtonGenerate.setEnabled(false);
    }
    catch (NumberFormatException nfe)
    {
        jLabelStatus.setText("Error reading parameters");
        JOptionPane.showMessageDialog(this, nfe.getMessage(), "Error reading parameters", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_jButtonGenerateActionPerformed

private void simulatorMatrixPlotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simulatorMatrixPlotMouseClicked
// TODO add your handling code here:
    if (evt.getClickCount() == 2)
    {
        setSelection(evt);
        setConnectionWeight(0);
        setSelection(evt);
    }
}//GEN-LAST:event_simulatorMatrixPlotMouseClicked

private void simulatorMatrixPlotMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simulatorMatrixPlotMousePressed
// TODO add your handling code here:
    if (evt.isPopupTrigger())
    {
        setSelection(evt);
        jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}//GEN-LAST:event_simulatorMatrixPlotMousePressed

private void simulatorMatrixPlotMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simulatorMatrixPlotMouseReleased
// TODO add your handling code here:
    if (evt.isPopupTrigger())
    {
        setSelection(evt);
        jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}//GEN-LAST:event_simulatorMatrixPlotMouseReleased

private void simulatorMatrixPlotMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simulatorMatrixPlotMouseMoved
    setSelection(evt);
    simulatorMatrixPlot.setToolTipText(tip);
}//GEN-LAST:event_simulatorMatrixPlotMouseMoved

private void jMenuItemUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUpdateActionPerformed
    setConnectionWeight(0);
}//GEN-LAST:event_jMenuItemUpdateActionPerformed

private void jMenuItemSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveActionPerformed
jPopupMenu.setVisible(false);
	JFileChooser fc = new JFileChooser();
	fc.setFileFilter(new FileFilter()
	{
	    public boolean accept(File f)
	    {
		return (f.isDirectory() || f.getName().toLowerCase().endsWith(".png"));
	    }
	    public String getDescription()
	    {
		return "Portable Networks graphics format (*.png)";
	    }
	});
	if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
	fc.showSaveDialog(this);
	Constants.CURRENT_DIR = fc.getCurrentDirectory();
	
	File f = fc.getSelectedFile();
	if (f != null)
	{
	    if (!f.getName().toLowerCase().endsWith(".png"))
	    {
		f = new File(f.getPath() + ".png");
	    }
	    int width = 610;
	    int height = 525;
	    BufferedImage image = simulatorMatrixPlot.exportImage(new Rectangle(width, height));
	    //BufferedImage image = sim.getImage(800, 800);
	    try
	    {
		ImageIO.write(image, "PNG", f);
	    }
	    catch(IOException ioe)
	    {
		jLabelStatus.setText("Error saving image");
		JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error saving image", JOptionPane.ERROR_MESSAGE);
	    }
	}
}//GEN-LAST:event_jMenuItemSaveActionPerformed

private void jMenuItemSaveWeightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveWeightsActionPerformed
    saveConnectionModel(SaveMode.txt);
}//GEN-LAST:event_jMenuItemSaveWeightsActionPerformed

private void jMenuItemSynapseFunctionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSynapseFunctionsActionPerformed
    setSynapseFunction();
}//GEN-LAST:event_jMenuItemSynapseFunctionsActionPerformed

private void jSpinnerSynapticDelayLowKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jSpinnerSynapticDelayLowKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jSpinnerSynapticDelayLowKeyReleased

private void jSpinnerSynapticDelayHighKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jSpinnerSynapticDelayHighKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jSpinnerSynapticDelayHighKeyReleased
             
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddEpisode;
    private javax.swing.JButton jButtonGenerate;
    private javax.swing.JButton jButtonInterconnect;
    private javax.swing.JButton jButtonLoadEventTypes;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonUpdateEventTypes;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBoxInterconType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelRandConnStrengthTitle;
    private javax.swing.JLabel jLabelRandomConnStrengthBase;
    private javax.swing.JMenuItem jMenuItemSave;
    private javax.swing.JMenuItem jMenuItemSaveWeights;
    private javax.swing.JMenuItem jMenuItemSynapseFunctions;
    private javax.swing.JMenuItem jMenuItemUpdate;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelLevel1;
    private javax.swing.JPanel jPanelLevel2;
    private javax.swing.JPanel jPanelLevel3;
    private javax.swing.JPanel jPanelRandomConnStrength;
    private javax.swing.JPanel jPanelRandomConnStrengthRange;
    private javax.swing.JPanel jPanelSimulatorPanel;
    private javax.swing.JPanel jPanelSimulatorSpecific;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner jSpinnerSynapticDelayHigh;
    private javax.swing.JSpinner jSpinnerSynapticDelayLow;
    private javax.swing.JTable jTableEpisodes;
    private javax.swing.JTextField jTextFieldA;
    private javax.swing.JTextField jTextFieldB;
    private javax.swing.JTextField jTextFieldC;
    private javax.swing.JTextField jTextFieldD;
    private javax.swing.JTextField jTextFieldEventTypes;
    private javax.swing.JTextField jTextFieldIHigh;
    private javax.swing.JTextField jTextFieldILow;
    private javax.swing.JTextField jTextFieldISpike;
    private javax.swing.JTextField jTextFieldInterConnect;
    private javax.swing.JTextField jTextFieldRandStim;
    private javax.swing.JTextField jTextFieldTStart;
    private javax.swing.JTextField jTextFieldTUpdate;
    private javax.swing.JTextField jTextFieldTtotal;
    private javax.swing.JTextField jTextFieldWeightHigh;
    private javax.swing.JTextField jTextFieldWeightLow;
    private edu.iisc.tdminer.gui.simulator.IzhikevichMatrixPlot simulatorMatrixPlot;
    // End of variables declaration//GEN-END:variables


    public void setStateInfo(StateInfo stateInfo)
    {
	this.stateInfo = stateInfo;
    }
    
    public void setJLabelStatus(JLabel jLabelStatus)
    {
	this.jLabelStatus = jLabelStatus;
    }
    
    public void setFrame(JFrame frame)
    {
	this.frame = frame;
    }

    private void setSelection(java.awt.event.MouseEvent evt)
    {
	int x = evt.getX();
	int y = evt.getY();
	selectedRow = simulatorMatrixPlot.locateRow(x);
	selectedCol = simulatorMatrixPlot.locateColumn(y);
	double w = sim.getWeight(selectedRow, selectedCol);
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(3);
	nf.setGroupingUsed(false);
	EventFactor l = sim.getEventFactor();
	
	if (selectedRow > -1 && selectedCol > -1 && selectedRow < l.getSize() && selectedCol < l.getSize())
	{
            IzhikevichModel psim = (IzhikevichModel)sim;
            int d = psim.getDelay(selectedRow, selectedCol);
            jLabelStatus.setText("Connection: " + l.getName(selectedRow) + " -> "
                    + l.getName(selectedCol) + " (weight = " + nf.format(w) + ", delay = " + d
                    + " ms");
	    tip = (l.getName(selectedRow) + " -> " + l.getName(selectedCol));
	}
	else if (selectedRow > -1 && selectedRow < l.getSize() && selectedCol == -1)
	{
	    jLabelStatus.setText("Event type: " + l.getName(selectedRow));
	    tip = (l.getName(selectedRow).toString());
	}
	else if (selectedCol > -1 && selectedRow == -1 && selectedCol < l.getSize())
	{
	    jLabelStatus.setText("Event type: " + l.getName(selectedCol));
	    tip = (l.getName(selectedCol).toString());
	}
	else
	{
	    jLabelStatus.setText("Co-ordinates x = " + x + ", y = " + y);
	    selectedRow = -1;
	    selectedCol = -1;
	    tip = null;
	}
    }
    
    private void parseEventTypes()
    {
	String events = jTextFieldEventTypes.getText().trim();
	if (!events.equals(eventsString))
	{
	    sim.getEpisodesList().clear();
	    tableModel.fireTableDataChanged();
	    String[] types = events.split("[ ]+");
	    EventFactor eventTypes = new EventFactor(types);
	    sim.setEventFactor(eventTypes);
	    tableModel.setEventTypes(sim.getEventFactor());
	    eventsString = events;
	    simulatorMatrixPlot.repaint();
	}
    }

    public void refreshState()
    {
        // do nothing
    }

    public void handleTaskCompletion(int taskIndex)
    {
	IEventDataStream sequence = sim.getEventStream();
	stateInfo.getSession().setSequence(sequence);
	stateInfo.getSession().setEpisodes(null);
	jButtonGenerate.setEnabled(true);
	jLabelStatus.setText("Simulation data successfully generated");
    }
    
    private void setConnectionWeight(int choice)
    {
	if (selectedCol > -1 && selectedRow > -1)
	{
	    EventFactor l = sim.getEventFactor();
	    NumberFormat nf = NumberFormat.getInstance();
	    nf.setMaximumFractionDigits(4);
	    nf.setGroupingUsed(false);
	    String ret = null;
	    switch(choice)
	    {
		case 0:
		    ret = JOptionPane.showInputDialog(frame, "Enter new weight for "
			    + l.getName(selectedRow) + "->" + l.getName(selectedCol)+ " connection",
			    nf.format(sim.getWeight(selectedRow, selectedCol)));
		    if (ret != null)
		    {
			try
			{
			    double val = Double.parseDouble(ret);
			    sim.setWeight(selectedRow, selectedCol, val);
			    simulatorMatrixPlot.repaint();
			}
			catch(NumberFormatException nfe)
			{
			    jLabelStatus.setText("Error changing weight");
			    JOptionPane.showMessageDialog(this, nfe.getMessage(), "Error changing weight", JOptionPane.ERROR_MESSAGE);
			}
		    }
		    break;
		case 1:
                    ret = JOptionPane.showInputDialog(frame, "Enter new conditional probability for "
                            + l.getName(selectedRow) + "->" + l.getName(selectedCol)+ " connection",
                            "nf.format(sim.getEStrong(selectedRow, selectedCol))");
                    if (ret != null)
                    {
                        try
                        {
                            double val = Double.parseDouble(ret);
                            //sim.setProbability(selectedRow, selectedCol, val);
                            simulatorMatrixPlot.repaint();
                        }
                        catch(NumberFormatException nfe)
                        {
                            jLabelStatus.setText("Error changing probabilty");
                            JOptionPane.showMessageDialog(this, nfe.getMessage(), "Error changing probabilty", JOptionPane.ERROR_MESSAGE);
                        }
                    }
		    break;
	    }
	}
	else
	{
	    JOptionPane.showMessageDialog(frame, "Select a connection from the matrix", "Error changing weight", JOptionPane.ERROR_MESSAGE);
	}
    }
    
    
    private void saveConnectionModel( SaveMode thatMode )
    {
	final SaveMode mode = thatMode;
	jPopupMenu.setVisible(false);
	JFileChooser fc = new JFileChooser();
	fc.setFileFilter(new FileFilter()
	{
	    public boolean accept(File f)
	    {
		return (f.isDirectory() || f.getName().toLowerCase().endsWith(mode.getSuffix()));
	    }
	    public String getDescription()
	    {
		return "Simulation model file (*" + mode.getSuffix() + ")";
	    }
	});
	if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
	fc.showSaveDialog(this);
	Constants.CURRENT_DIR = fc.getCurrentDirectory();
	
	File file = fc.getSelectedFile();
	if (file == null) return;
	
	if (!file.getName().toLowerCase().endsWith(mode.getSuffix()))
	{
	    file = new File(file.getPath() + mode.getSuffix());
	}
	
	try
	{
	    BufferedOutputStream persist = new BufferedOutputStream(new FileOutputStream(file));
	    switch (mode)
	    {
		case object:
		    ObjectOutputStream out = new ObjectOutputStream( persist );
		    out.writeObject(sim);
		    out.close();
		    break;
		    
		case xml:
		    XMLEncoder encoder =  new XMLEncoder(persist);
		    encoder.setExceptionListener(new ExceptionListener()
		    {
			public void exceptionThrown(Exception exception)
			{
			    exception.printStackTrace();
			}
		    });
		    
		    encoder.writeObject(sim);
		    encoder.close();
		    break;
                case txt:
                    PrintWriter writer = new PrintWriter(persist);
                    sim.saveConnectionMatrix(writer);
                    writer.close();
                    break;
		    
		default:
		    break;
	    }
	}
	catch(IOException ioe)
	{
	    ioe.printStackTrace();
	    jLabelStatus.setText("Error saving model");
	    JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error saving model", JOptionPane.ERROR_MESSAGE);
	}
    }
    
}
