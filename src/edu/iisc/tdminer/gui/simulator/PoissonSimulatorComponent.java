/*
 * SimulatorModelOnePanel.java
 *
 * Created on March 30, 2006, 2:08 PM
 */

package edu.iisc.tdminer.gui.simulator;

import edu.iisc.tdminer.gui.AddEpisodePanel;
import edu.iisc.tdminer.gui.ParentMenu;
import edu.iisc.tdminer.model.ThreadedProgressManager;
import edu.iisc.tdminer.util.Constants;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminer.gui.ITaskPanel;
import edu.iisc.tdminer.model.SimulatorEpisodesTableModel;
import edu.iisc.tdminercore.simulation.ExternalInput;
import edu.iisc.tdminercore.simulation.ExternalStimulator;
import edu.iisc.tdminercore.simulation.NetworkBasedSimulator;
import edu.iisc.tdminercore.simulation.PoissonSimulator;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;

/**
 *
 * @author  Deb
 */
public class PoissonSimulatorComponent extends javax.swing.JPanel implements ITaskPanel,ISimulatorPanel
{
    private PoissonSimulator sim = new PoissonSimulator();
    
    private StateInfo stateInfo;
    private JLabel jLabelStatus;
    private JFrame frame;
    private SimulatorEpisodesTableModel tableModel;
    private String eventsString;
    
    private int selectedRow;
    private int selectedCol;
    private String tip;
    
    /** Creates new form SimulatorModelOnePanel */
    public PoissonSimulatorComponent()
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
    
    public double getBaseConnStrength() {
        try {
            double frest = Double.parseDouble(jTextFieldFrest.getText());
            double tupdate = Double.parseDouble(this.jTextFieldTUpdate.getText());
            
            return (1 - Math.exp(-frest * tupdate));
        } catch (NumberFormatException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return 0.02;
    }
    
    private void setParameters()
    {
        sim.setTstart(jTextFieldTStart.getText());
	sim.setTupdate(jTextFieldTUpdate.getText());
	sim.setTtotal(jTextFieldTtotal.getText());
        sim.setEstrong(jTextFieldEstrong.getText());
	sim.setFrest(jTextFieldFrest.getText());
        sim.setSynapticDelayLow(new Integer(jSpinnerSynapticDelayLow.getValue().toString()));
        sim.setSynapticDelayHigh(new Integer(jSpinnerSynapticDelayHigh.getValue().toString()));
        sim.setTrefractory(jTextFieldTrefractory.getText());
        sim.setSlope(jTextFieldSlope.getText());
        sim.groupInterconnect = jCheckBoxInterconnectGroups.isSelected();
        sim.computeParameters();

	sim.interconnectType = jComboBoxInterconType.getSelectedIndex();
	sim.randomLow = Double.parseDouble(jTextFieldWeightLow.getText());
	sim.randomHigh = Double.parseDouble(jTextFieldWeightHigh.getText());
        sim.interConnectFactor = Double.parseDouble(jTextFieldInterConnect.getText());
    }
    
    @Override
    public void paint(Graphics g) 
    {
        Double base = getBaseConnStrength();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        this.jLabelRandomConnStrengthBase.setText( nf.format(base + 0.00005));
        this.jTextFieldWeightLow.setText(nf.format(base * 0.5 + 0.00005));
        this.jTextFieldWeightHigh.setText(nf.format(base * 2.0 + 0.00005));
        super.paint(g);
    }
    
    private void setDefaultValues()
    {
	sim.setDefaultValues();
	resetUI();
    }
    
    private void resetUI()
    {
	jTextFieldEventTypes.setText(sim.getFieldEventTypes());
	jComboBoxInterconType.setSelectedIndex(sim.getInterconType());
	jTextFieldWeightLow.setText(sim.getStrLow());
	jTextFieldWeightHigh.setText(sim.getStrHigh());
        jTextFieldInterConnect.setText(sim.getInterConnect());
        jTextFieldEstrong.setText(sim.getEstrong());
	jTextFieldTUpdate.setText(sim.getTupdate());
	jTextFieldTtotal.setText(sim.getTtotal());
        jTextFieldTStart.setText(sim.getTstart());
        jTextFieldFrest.setText(sim.getFrest());	
        jSpinnerSynapticDelayLow.setValue(sim.getSynapticDelayLow());
        jSpinnerSynapticDelayHigh.setValue(sim.getSynapticDelayHigh());
        jTextFieldTrefractory.setText(sim.getTrefractory());
        jTextFieldSlope.setText(sim.getSlope());
        jCheckBoxInterconnectGroups.setSelected(sim.groupInterconnect);
    }
    
    private void parseEventTypes()
    {
	String events = jTextFieldEventTypes.getText().trim();
        parseEventTypes(events);
    }
    
    public NetworkBasedSimulator getSimulator()
    {
        return sim;
    }

    public void setSimulator(NetworkBasedSimulator sim)
    {
        this.sim = (PoissonSimulator)sim;
    }
    
    private void parseEventTypes(String events)
    {
	if (!events.equals(eventsString))
	{
	    sim.getEpisodesList().clear();
	    tableModel.fireTableDataChanged();
	    String[] types = events.split("[ ]+");
	    sim.setEventFactor(new EventFactor(types));
	    tableModel.setEventTypes(sim.getEventFactor());
	    eventsString = events;
	    simulatorMatrixPlot.repaint();
	}
    }
    
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
	if (frame instanceof ParentMenu)
	{
	    ParentMenu parentmenu = (ParentMenu) frame;
	    this.initSettingsMenuComponents(parentmenu.getSimulatorSettingsMenu());
	}
	else
	{
	    System.out.println("Not a Parent Menu");
	}
	
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
	    
	    if (sim instanceof PoissonSimulator)
	    {
		PoissonSimulator psim = (PoissonSimulator)sim;
		int d = psim.getDelay(selectedRow, selectedCol);
		tip = (l.getName(selectedRow) + " -> " + l.getName(selectedCol));
		jLabelStatus.setText("Connection: " + l.getName(selectedRow) + " -> "
			+ l.getName(selectedCol) + " (weight = " + nf.format(w) + ", delay = " + d
			+ " T_update, E_strong = " + nf.format(psim.getEStrong(selectedRow, selectedCol)) + ")");
	    }
	    else
	    {
		jLabelStatus.setText("Connection: " + l.getName(selectedRow) + " -> "
			+ l.getName(selectedCol) + " weight = " + nf.format(w));
	    }
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
    
    private void setSynapseFunction()
    {
        AddSynapsefunctions dialog = new AddSynapsefunctions(this.frame, 
                sim, selectedRow, selectedCol);
        dialog.setVisible(true);
        simulatorMatrixPlot.repaint();
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
		    if (sim instanceof PoissonSimulator)
		    {
			PoissonSimulator psim = (PoissonSimulator)sim;
			ret = JOptionPane.showInputDialog(frame, "Enter new conditional probability for "
				+ l.getName(selectedRow) + "->" + l.getName(selectedCol)+ " connection",
				nf.format(psim.getEStrong(selectedRow, selectedCol)));
			if (ret != null)
			{
			    try
			    {
				double val = Double.parseDouble(ret);
				psim.setProbability(selectedRow, selectedCol, val);
				simulatorMatrixPlot.repaint();
			    }
			    catch(NumberFormatException nfe)
			    {
				jLabelStatus.setText("Error changing weight");
				JOptionPane.showMessageDialog(this, nfe.getMessage(), "Error changing weight", JOptionPane.ERROR_MESSAGE);
			    }
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
    
    private void setSynapticDelay()
    {
	if (selectedCol > -1 && selectedRow > -1)
	{
	    EventFactor l = sim.getEventFactor();
	    NumberFormat nf = NumberFormat.getInstance();
	    nf.setMaximumFractionDigits(4);
	    nf.setGroupingUsed(false);
	    
	    if (sim instanceof PoissonSimulator)
	    {
		PoissonSimulator psim = (PoissonSimulator)sim;
		int index = psim.getDelay(selectedRow, selectedCol);
		if (index < 1) index = 1;
		int ret = PoissonSynapticDelayDialog.getSynapticDelay("Synaptic delay for "
			+ l.getName(selectedRow) + "->" + l.getName(selectedCol)+ " connection", index, frame);
		if (ret != -1)
		{
		    try
		    {
			psim.setDelay(selectedRow, selectedCol, ret);
			simulatorMatrixPlot.repaint();
		    }
		    catch(NumberFormatException nfe)
		    {
			jLabelStatus.setText("Error changing synaptic delay");
			JOptionPane.showMessageDialog(this, nfe.getMessage(), "Error changing synaptic delay", JOptionPane.ERROR_MESSAGE);
		    }
		}
	    }
	}
	else
	{
	    JOptionPane.showMessageDialog(frame, "Select a connection from the matrix", "Error changing weight", JOptionPane.ERROR_MESSAGE);
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

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemSynapseFunctions = new javax.swing.JMenuItem();
        jMenuItemUpdateProb = new javax.swing.JMenuItem();
        jMenuItemDelay = new javax.swing.JMenuItem();
        jMenuItemUpdate = new javax.swing.JMenuItem();
        jMenuItemSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemSaveWeights = new javax.swing.JMenuItem();
        jMenuItemLoadWeights = new javax.swing.JMenuItem();
        jMenuItemSaveWeightsXml = new javax.swing.JMenuItem();
        jMenuItemLoadWeightsXml = new javax.swing.JMenuItem();
        jMenuItemSaveCsv = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldEventTypes = new javax.swing.JTextField();
        jPanel21 = new javax.swing.JPanel();
        jButtonUpdateEventTypes = new javax.swing.JButton();
        jButtonLoadEventTypes = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableEpisodes = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jButtonReset = new javax.swing.JButton();
        jButtonAddEpisode = new javax.swing.JButton();
        jButtonAddStim = new javax.swing.JButton();
        jButtonInterconnect = new javax.swing.JButton();
        jButtonGenerate = new javax.swing.JButton();
        jPanelSimulatorSpecific = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jComboBoxInterconType = new javax.swing.JComboBox();
        jPanel8 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldInterConnect = new javax.swing.JTextField();
        jPanelRandomConnStrength = new javax.swing.JPanel();
        jLabelRandConnStrengthTitle = new javax.swing.JLabel();
        jPanelRandomConnStrengthRange = new javax.swing.JPanel();
        jTextFieldWeightLow = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabelRandomConnStrengthBase = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldWeightHigh = new javax.swing.JTextField();
        jPanelSimulatorPanel = new javax.swing.JPanel();
        jPanelLevel1 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldTUpdate = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldTtotal = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldTStart = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jPanelLevel2 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldFrest = new javax.swing.JTextField();
        jButtonFiringRates = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldEstrong = new javax.swing.JTextField();
        jPanel14 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jTextFieldTrefractory = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jSpinnerSynapticDelayLow = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jSpinnerSynapticDelayHigh = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        jPanelLevel3 = new javax.swing.JPanel();
        jCheckBoxInterconnectGroups = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldSlope = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        simulatorMatrixPlot = new edu.iisc.tdminer.gui.simulator.PoissonMatrixPlot();

        jMenuItemSynapseFunctions.setText("Add Synapse Function");
        jMenuItemSynapseFunctions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSynapseFunctionsActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSynapseFunctions);

        jMenuItemUpdateProb.setText("Change conditional probability");
        jMenuItemUpdateProb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUpdateProbActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemUpdateProb);

        jMenuItemDelay.setText("Change synaptic delay");
        jMenuItemDelay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDelayActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemDelay);

        jMenuItemUpdate.setText("Change weight");
        jMenuItemUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUpdateActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemUpdate);

        jMenuItemSave.setText("Save Image");
        jMenuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSave);
        jPopupMenu.add(jSeparator1);

        jMenuItemSaveWeights.setText("Save model");
        jMenuItemSaveWeights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveWeightsActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSaveWeights);

        jMenuItemLoadWeights.setText("Load model");
        jMenuItemLoadWeights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadWeightsActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemLoadWeights);

        jMenuItemSaveWeightsXml.setText("Save model as xml");
        jMenuItemSaveWeightsXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveWeightsXmlActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSaveWeightsXml);

        jMenuItemLoadWeightsXml.setText("Load model from xml");
        jMenuItemLoadWeightsXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadWeightsXmlActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemLoadWeightsXml);

        jMenuItemSaveCsv.setText("Save model as csv");
        jMenuItemSaveCsv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveCsvActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemSaveCsv);

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Select Event Types"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel1.setLayout(new java.awt.BorderLayout(5, 5));

        jLabel1.setText("Event Types (space separated):");
        jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);

        jTextFieldEventTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEventTypesActionPerformed(evt);
            }
        });
        jPanel1.add(jTextFieldEventTypes, java.awt.BorderLayout.CENTER);

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

        jPanel1.add(jPanel21, java.awt.BorderLayout.EAST);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jPanel20.setMaximumSize(new java.awt.Dimension(800, 2147483647));
        jPanel20.setPreferredSize(new java.awt.Dimension(500, 350));
        jPanel20.setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Episodes"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5))));
        jPanel5.setLayout(new java.awt.BorderLayout(5, 5));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(350, 85));

        jTableEpisodes.setModel(new SimulatorEpisodesTableModel());
        jScrollPane1.setViewportView(jTableEpisodes);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel6.setLayout(new java.awt.GridBagLayout());

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
        jPanel6.add(jButtonReset, gridBagConstraints);

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
        jPanel6.add(jButtonAddEpisode, gridBagConstraints);

        jButtonAddStim.setText("Extn. Stim.");
        jButtonAddStim.setToolTipText("Add/Remove External Stimulation");
        jButtonAddStim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddStimActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel6.add(jButtonAddStim, gridBagConstraints);

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
        jPanel6.add(jButtonInterconnect, gridBagConstraints);

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
        jPanel6.add(jButtonGenerate, gridBagConstraints);

        jPanel5.add(jPanel6, java.awt.BorderLayout.EAST);

        jPanel20.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanelSimulatorSpecific.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Select model parameters"), javax.swing.BorderFactory.createEmptyBorder(2, 1, 2, 1)));
        jPanelSimulatorSpecific.setLayout(new java.awt.BorderLayout(5, 5));

        jPanel3.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        jPanel16.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jLabel15.setText("Interconnection Type");
        jPanel16.add(jLabel15);

        jComboBoxInterconType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uniform dist.", "Binomial dist.", "Fully connected" }));
        jPanel16.add(jComboBoxInterconType);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jLabel3.setText("Interconnect");
        jPanel8.add(jLabel3, java.awt.BorderLayout.WEST);

        jTextFieldInterConnect.setText("0.5");
        jPanel8.add(jTextFieldInterConnect, java.awt.BorderLayout.CENTER);

        jPanel16.add(jPanel8);

        jPanel3.add(jPanel16);

        jPanelRandomConnStrength.setLayout(new java.awt.GridLayout(1, 0));

        jLabelRandConnStrengthTitle.setText("Random connection strengths");
        jPanelRandomConnStrength.add(jLabelRandConnStrengthTitle);

        jPanelRandomConnStrengthRange.setMinimumSize(new java.awt.Dimension(20, 19));
        jPanelRandomConnStrengthRange.setPreferredSize(new java.awt.Dimension(40, 19));
        jPanelRandomConnStrengthRange.setLayout(new javax.swing.BoxLayout(jPanelRandomConnStrengthRange, javax.swing.BoxLayout.LINE_AXIS));

        jTextFieldWeightLow.setMinimumSize(new java.awt.Dimension(30, 19));
        jTextFieldWeightLow.setPreferredSize(new java.awt.Dimension(45, 19));
        jPanelRandomConnStrengthRange.add(jTextFieldWeightLow);

        jLabel4.setText("<");
        jPanelRandomConnStrengthRange.add(jLabel4);

        jLabelRandomConnStrengthBase.setText("base");
        jLabelRandomConnStrengthBase.setToolTipText("The base connection strength");
        jPanelRandomConnStrengthRange.add(jLabelRandomConnStrengthBase);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setLabelFor(jPanelRandomConnStrengthRange);
        jLabel2.setText("<");
        jPanelRandomConnStrengthRange.add(jLabel2);

        jTextFieldWeightHigh.setMinimumSize(new java.awt.Dimension(30, 19));
        jTextFieldWeightHigh.setPreferredSize(new java.awt.Dimension(45, 19));
        jPanelRandomConnStrengthRange.add(jTextFieldWeightHigh);

        jPanelRandomConnStrength.add(jPanelRandomConnStrengthRange);

        jPanel3.add(jPanelRandomConnStrength);

        jPanelSimulatorSpecific.add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanelSimulatorPanel.setLayout(new java.awt.BorderLayout(0, 5));

        jPanelLevel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelLevel1.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jPanel7.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel5.setText("<html>T<sub>update</sub> = </html>");
        jLabel5.setToolTipText("(Time resolution of updates)");
        jPanel7.add(jLabel5, java.awt.BorderLayout.WEST);

        jTextFieldTUpdate.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTUpdate.setText("0.001");
        jTextFieldTUpdate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTUpdateKeyReleased(evt);
            }
        });
        jPanel7.add(jTextFieldTUpdate, java.awt.BorderLayout.CENTER);

        jLabel6.setText("sec");
        jPanel7.add(jLabel6, java.awt.BorderLayout.EAST);

        jPanelLevel1.add(jPanel7);

        jPanel9.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel7.setText("<html>T<sub>total</sub> = </html>");
        jLabel7.setToolTipText("(Total duration of simulation)");
        jPanel9.add(jLabel7, java.awt.BorderLayout.WEST);

        jTextFieldTtotal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTtotal.setText("20.0");
        jTextFieldTtotal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTtotalKeyReleased(evt);
            }
        });
        jPanel9.add(jTextFieldTtotal, java.awt.BorderLayout.CENTER);

        jLabel8.setText("sec");
        jPanel9.add(jLabel8, java.awt.BorderLayout.EAST);

        jPanelLevel1.add(jPanel9);

        jPanel10.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel13.setText("<html>T<sub>start</sub> = </html>");
        jLabel13.setToolTipText("(Time resolution of updates)");
        jPanel10.add(jLabel13, java.awt.BorderLayout.WEST);

        jTextFieldTStart.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTStart.setText("0.0");
        jTextFieldTStart.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTStartKeyReleased(evt);
            }
        });
        jPanel10.add(jTextFieldTStart, java.awt.BorderLayout.CENTER);

        jLabel14.setText("sec");
        jPanel10.add(jLabel14, java.awt.BorderLayout.EAST);

        jPanelLevel1.add(jPanel10);

        jPanelSimulatorPanel.add(jPanelLevel1, java.awt.BorderLayout.NORTH);

        jPanelLevel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelLevel2.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        jPanel11.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jPanel12.setLayout(new java.awt.BorderLayout(2, 0));

        jLabel9.setText("<html>F<sub>rest</sub> = </html>");
        jLabel9.setToolTipText("(Resting firing rate of neurons)");
        jPanel12.add(jLabel9, java.awt.BorderLayout.WEST);

        jTextFieldFrest.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldFrest.setText("20");
        jTextFieldFrest.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTextFieldFrestInputMethodTextChanged(evt);
            }
        });
        jTextFieldFrest.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldFrestKeyReleased(evt);
            }
        });
        jPanel12.add(jTextFieldFrest, java.awt.BorderLayout.CENTER);

        jButtonFiringRates.setText("...");
        jButtonFiringRates.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonFiringRates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFiringRatesActionPerformed(evt);
            }
        });
        jPanel12.add(jButtonFiringRates, java.awt.BorderLayout.EAST);

        jPanel11.add(jPanel12);

        jPanel13.setLayout(new java.awt.BorderLayout(2, 0));

        jLabel11.setText("<html>E<sub>strong</sub> = </html>");
        jLabel11.setToolTipText("<html>Efficiency of a strong synapse (Probability of producing a spike in the post synaptic neuron in the interval T<sub>update</sub>, when the pre-synaptic neuron spike</html>");
        jPanel13.add(jLabel11, java.awt.BorderLayout.WEST);

        jTextFieldEstrong.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldEstrong.setText("0.95");
        jTextFieldEstrong.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldEstrongKeyReleased(evt);
            }
        });
        jPanel13.add(jTextFieldEstrong, java.awt.BorderLayout.CENTER);

        jPanel11.add(jPanel13);

        jPanel14.setLayout(new java.awt.BorderLayout(2, 0));

        jLabel12.setText("<html>T<sub>refractory</sub> = </html>");
        jLabel12.setToolTipText("(Refractory period)");
        jPanel14.add(jLabel12, java.awt.BorderLayout.WEST);

        jTextFieldTrefractory.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldTrefractory.setText("0.001");
        jTextFieldTrefractory.setPreferredSize(new java.awt.Dimension(60, 20));
        jTextFieldTrefractory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTrefractoryKeyReleased(evt);
            }
        });
        jPanel14.add(jTextFieldTrefractory, java.awt.BorderLayout.CENTER);

        jLabel16.setText("sec");
        jPanel14.add(jLabel16, java.awt.BorderLayout.EAST);

        jPanel11.add(jPanel14);

        jPanelLevel2.add(jPanel11);

        jPanel15.setLayout(new java.awt.BorderLayout(5, 0));

        jPanel17.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel17.setText("<html>T<sub>synapse low</sub> = </html>");
        jLabel17.setToolTipText("(Synaptic delay)");
        jPanel17.add(jLabel17, java.awt.BorderLayout.WEST);

        jSpinnerSynapticDelayLow.setMinimumSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayLow.setPreferredSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayLow.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jSpinnerSynapticDelayLowKeyReleased(evt);
            }
        });
        jPanel17.add(jSpinnerSynapticDelayLow, java.awt.BorderLayout.CENTER);

        jLabel18.setText("<html>x T<sub>update</sub> sec</html>");
        jPanel17.add(jLabel18, java.awt.BorderLayout.EAST);

        jPanel15.add(jPanel17, java.awt.BorderLayout.WEST);

        jPanel18.setLayout(new java.awt.BorderLayout(10, 0));

        jLabel19.setText("<html>T<sub>synapse high</sub> = </html>");
        jLabel19.setToolTipText("(Synaptic delay)");
        jPanel18.add(jLabel19, java.awt.BorderLayout.WEST);

        jSpinnerSynapticDelayHigh.setMinimumSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayHigh.setPreferredSize(new java.awt.Dimension(40, 20));
        jSpinnerSynapticDelayHigh.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jSpinnerSynapticDelayHighKeyReleased(evt);
            }
        });
        jPanel18.add(jSpinnerSynapticDelayHigh, java.awt.BorderLayout.CENTER);

        jLabel20.setText("<html>x T<sub>update</sub> sec</html>");
        jPanel18.add(jLabel20, java.awt.BorderLayout.EAST);

        jPanel15.add(jPanel18, java.awt.BorderLayout.EAST);

        jPanelLevel2.add(jPanel15);

        jPanelSimulatorPanel.add(jPanelLevel2, java.awt.BorderLayout.CENTER);

        jPanelLevel3.setLayout(new java.awt.BorderLayout(15, 0));

        jCheckBoxInterconnectGroups.setText("Interconnect within co-spiking groups");
        jCheckBoxInterconnectGroups.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxInterconnectGroups.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanelLevel3.add(jCheckBoxInterconnectGroups, java.awt.BorderLayout.WEST);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel21.setText("Slope of Sigmoid = ");
        jPanelLevel3.add(jLabel21, java.awt.BorderLayout.CENTER);

        jTextFieldSlope.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldSlope.setText("1.0");
        jTextFieldSlope.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanelLevel3.add(jTextFieldSlope, java.awt.BorderLayout.EAST);

        jPanelSimulatorPanel.add(jPanelLevel3, java.awt.BorderLayout.SOUTH);

        jPanelSimulatorSpecific.add(jPanelSimulatorPanel, java.awt.BorderLayout.CENTER);

        jPanel20.add(jPanelSimulatorSpecific, java.awt.BorderLayout.NORTH);

        jPanel2.add(jPanel20);

        jPanel4.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Pair-wise conditional probability"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel4.setMinimumSize(new java.awt.Dimension(400, 350));
        jPanel4.setPreferredSize(new java.awt.Dimension(500, 350));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

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
        jPanel4.add(simulatorMatrixPlot);

        jPanel2.add(jPanel4);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemUpdateProbActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemUpdateProbActionPerformed
    {//GEN-HEADEREND:event_jMenuItemUpdateProbActionPerformed
	setConnectionWeight(1);
    }//GEN-LAST:event_jMenuItemUpdateProbActionPerformed
    
    private void jMenuItemLoadWeightsXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadWeightsXmlActionPerformed
	loadConnectionModel(SaveMode.xml);
    }//GEN-LAST:event_jMenuItemLoadWeightsXmlActionPerformed
    
    private void jMenuItemSaveWeightsXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveWeightsXmlActionPerformed
	saveConnectionModel(SaveMode.xml);
    }//GEN-LAST:event_jMenuItemSaveWeightsXmlActionPerformed
    
    private void jMenuItemDelayActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemDelayActionPerformed
    {//GEN-HEADEREND:event_jMenuItemDelayActionPerformed
	setSynapticDelay();
    }//GEN-LAST:event_jMenuItemDelayActionPerformed
    
    private void simulatorMatrixPlotMouseMoved(java.awt.event.MouseEvent evt)//GEN-FIRST:event_simulatorMatrixPlotMouseMoved
    {//GEN-HEADEREND:event_simulatorMatrixPlotMouseMoved
	setSelection(evt);
	simulatorMatrixPlot.setToolTipText(tip);
}//GEN-LAST:event_simulatorMatrixPlotMouseMoved
    
    private void simulatorMatrixPlotMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_simulatorMatrixPlotMouseReleased
    {//GEN-HEADEREND:event_simulatorMatrixPlotMouseReleased
// TODO add your handling code here:
	if (evt.isPopupTrigger())
	{
	    setSelection(evt);
	    jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	}
}//GEN-LAST:event_simulatorMatrixPlotMouseReleased
    
    private void simulatorMatrixPlotMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_simulatorMatrixPlotMousePressed
    {//GEN-HEADEREND:event_simulatorMatrixPlotMousePressed
// TODO add your handling code here:
	if (evt.isPopupTrigger())
	{
	    setSelection(evt);
	    jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	}
}//GEN-LAST:event_simulatorMatrixPlotMousePressed
    
    private void simulatorMatrixPlotMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_simulatorMatrixPlotMouseClicked
    {//GEN-HEADEREND:event_simulatorMatrixPlotMouseClicked
// TODO add your handling code here:
	if (evt.getClickCount() == 2)
	{
	    setSelection(evt);
	    setConnectionWeight(1);
	    setSelection(evt);
	}
}//GEN-LAST:event_simulatorMatrixPlotMouseClicked
    
    private void jMenuItemLoadWeightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadWeightsActionPerformed
	loadConnectionModel(SaveMode.object);
    }//GEN-LAST:event_jMenuItemLoadWeightsActionPerformed
    
    enum SaveMode
    {
	xml(".xml"),
	object(".mdl"),
        txt(".txt");
	private String suffix;
	private SaveMode(String suffix)
	{ this.suffix = suffix; }
	public String getSuffix()
	{ return this.suffix; }
    }
    
    private void loadConnectionModel( SaveMode thatMode )
    {
	final SaveMode mode = thatMode;
	
	jPopupMenu.setVisible(false);
	JFileChooser fc = new JFileChooser();
	fc.setFileFilter(new FileFilter()
	{
	    public boolean accept(File file)
	    {
		return (file.isDirectory() || file.getName().toLowerCase().endsWith(mode.getSuffix()));
	    }
	    public String getDescription()
	    {
		return "Simulation model file (*"+mode.getSuffix()+")";
	    }
	});
	if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
	fc.showOpenDialog(this);
	Constants.CURRENT_DIR = fc.getCurrentDirectory();
	
	File file = fc.getSelectedFile();
	if (file == null) return;
	
	NetworkBasedSimulator sim = null;
	try
	{
	    BufferedInputStream persist =
		    new BufferedInputStream(new FileInputStream(file));
	    
	    switch (mode)
	    {
		case object:
		    ObjectInputStream in = new ObjectInputStream(persist);
		    
		    sim = (PoissonSimulator)in.readObject();
		    setSimulator(sim);
		    in.close();
		    
		    break;
		    
		case xml:
		    XMLDecoder decoder = new XMLDecoder( persist, null,
			    new ExceptionListener()
		    {
			public void exceptionThrown(Exception exception)
			{
			    exception.printStackTrace();
			}
		    });
		    sim = (PoissonSimulator)decoder.readObject();
		    setSimulator(sim);
		    decoder.close();
		    
		    break;
	    }
	}
	catch(IOException ioe)
	{
	    jLabelStatus.setText("Error saving model");
	    JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error loading model", JOptionPane.ERROR_MESSAGE);
	}
	catch(ClassNotFoundException cce)
	{
	    jLabelStatus.setText("Error saving model");
	    JOptionPane.showMessageDialog(this, cce.getMessage(), "Error loading model", JOptionPane.ERROR_MESSAGE);
	}
	if (sim == null) return;
	
	System.out.println(sim.getEventFactorString());
	sim.setFieldEventTypes(sim.getEventFactorString());
	
	resetUI();
	tableModel.fireTableDataChanged();
	simulatorMatrixPlot.repaint();
    }
    
    private void jMenuItemSaveWeightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveWeightsActionPerformed
	saveConnectionModel(SaveMode.object);
    }//GEN-LAST:event_jMenuItemSaveWeightsActionPerformed
    
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
    
    private void jButtonLoadEventTypesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLoadEventTypesActionPerformed
    {//GEN-HEADEREND:event_jButtonLoadEventTypesActionPerformed
	JDialog dialog = new JDialog(frame,"Event types selection", true);
	dialog.setSize(500,300);
	if (frame != null)
	{
	    int x = (int)(frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth())/2);
	    int y = (int)(frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight())/2);
	    dialog.setLocation(x,y);
	}
	dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	LoadEventTypesPanel panel = new LoadEventTypesPanel(dialog);
	dialog.getContentPane().add(panel);
	dialog.setVisible(true);
	
	String events = panel.getEventTypes();
	if (events != null)
	{
	    jTextFieldEventTypes.setText(events);
	    parseEventTypes(events);
	    simulatorMatrixPlot.repaint();
	}
    }//GEN-LAST:event_jButtonLoadEventTypesActionPerformed
    
    private void jMenuItemUpdateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemUpdateActionPerformed
    {//GEN-HEADEREND:event_jMenuItemUpdateActionPerformed
	setConnectionWeight(0);
    }//GEN-LAST:event_jMenuItemUpdateActionPerformed
    
    private void jMenuItemSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSaveActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSaveActionPerformed
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
    
    private void jButtonUpdateEventTypesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonUpdateEventTypesActionPerformed
    {//GEN-HEADEREND:event_jButtonUpdateEventTypesActionPerformed
// TODO add your handling code here:
	//System.out.println("size::"+jPanel4.getSize());
	parseEventTypes();
	simulatorMatrixPlot.repaint();
    }//GEN-LAST:event_jButtonUpdateEventTypesActionPerformed
    
    private void jTextFieldEventTypesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldEventTypesActionPerformed
    {//GEN-HEADEREND:event_jTextFieldEventTypesActionPerformed
// TODO add your handling code here:
	parseEventTypes();
	simulatorMatrixPlot.repaint();
    }//GEN-LAST:event_jTextFieldEventTypesActionPerformed
    
    private void jButtonGenerateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonGenerateActionPerformed
    {//GEN-HEADEREND:event_jButtonGenerateActionPerformed
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
    
    private void jButtonInterconnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonInterconnectActionPerformed
    {//GEN-HEADEREND:event_jButtonInterconnectActionPerformed
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
    
    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonResetActionPerformed
    {//GEN-HEADEREND:event_jButtonResetActionPerformed
	setDefaultValues();
	parseEventTypes();
	simulatorMatrixPlot.repaint();
	
    }//GEN-LAST:event_jButtonResetActionPerformed
    
    private void jButtonAddEpisodeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddEpisodeActionPerformed
    {//GEN-HEADEREND:event_jButtonAddEpisodeActionPerformed
	parseEventTypes();
	if (sim.getEventFactor() == null) return;
	
	List episodesList = sim.getEpisodesList();
	try
	{
	    List<IEpisode> eList =
		    AddEpisodePanel.getEpisode(sim.getEventFactor(),
		    frame, AddEpisodePanel.CallMode.SIMULATOR, null);
	    if (eList != null)
	    {
		for( IEpisode e : eList )
		{
		    if (episodesList.contains(e))
		    {
			JOptionPane.showMessageDialog(this, "Episode "
				+ e.toString(sim.getEventFactor())
				+ " already exists",
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
	catch(Exception e)
	{
	    JOptionPane.showMessageDialog(this, e.getMessage(), "Error adding episode", JOptionPane.ERROR_MESSAGE);
	}
    }//GEN-LAST:event_jButtonAddEpisodeActionPerformed

    private void jMenuItemSaveCsvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveCsvActionPerformed
        saveConnectionModel(SaveMode.txt);
    }//GEN-LAST:event_jMenuItemSaveCsvActionPerformed

    private void jButtonAddStimActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddStimActionPerformed
    {//GEN-HEADEREND:event_jButtonAddStimActionPerformed
        parseEventTypes();
        if (sim.getEventFactor() == null) return;
        
        List<ExternalInput> externalInputList = sim.getExternalInputList();
        List<ExternalStimulator> externalStimList = sim.getExternalStimulatorList();
        try
        {
            AddExternalStim.getExternInput(sim.getEventFactor(),frame, externalInputList, externalStimList);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error adding external input", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButtonAddStimActionPerformed

private void jMenuItemSynapseFunctionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSynapseFunctionsActionPerformed
    setSynapseFunction();
}//GEN-LAST:event_jMenuItemSynapseFunctionsActionPerformed

private void jTextFieldTUpdateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTUpdateKeyReleased
    updateUI();
}//GEN-LAST:event_jTextFieldTUpdateKeyReleased

private void jTextFieldTtotalKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTtotalKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldTtotalKeyReleased

private void jTextFieldTStartKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTStartKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldTStartKeyReleased

private void jTextFieldFrestInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTextFieldFrestInputMethodTextChanged

}//GEN-LAST:event_jTextFieldFrestInputMethodTextChanged

private void jTextFieldFrestKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFrestKeyReleased
    updateUI();
}//GEN-LAST:event_jTextFieldFrestKeyReleased

private void jTextFieldEstrongKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldEstrongKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldEstrongKeyReleased

private void jTextFieldTrefractoryKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTrefractoryKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jTextFieldTrefractoryKeyReleased

private void jSpinnerSynapticDelayLowKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jSpinnerSynapticDelayLowKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jSpinnerSynapticDelayLowKeyReleased

private void jSpinnerSynapticDelayHighKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jSpinnerSynapticDelayHighKeyReleased
// TODO add your handling code here:
}//GEN-LAST:event_jSpinnerSynapticDelayHighKeyReleased

private void jButtonFiringRatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFiringRatesActionPerformed
        parseEventTypes();
        if (sim.getEventFactor() == null) return;
        
        HashMap<Integer,Double> rates = sim.getBaseRates();
        try
        {
            BaseRateInput.setBaseFiringRates(sim.getEventFactor(), frame, rates);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error setting base firing rates", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
}//GEN-LAST:event_jButtonFiringRatesActionPerformed
    
    public void refreshState()
    {
	//Do nothing
    }
    
    public void handleTaskCompletion(int taskIndex)
    {
	IEventDataStream sequence = sim.getEventStream();
	stateInfo.getSession().setSequence(sequence);
	stateInfo.getSession().setEpisodes(null);
	jButtonGenerate.setEnabled(true);
	jLabelStatus.setText("Simulation data successfully generated");
    }
    
    private void initSettingsMenuComponents(JMenu jMenuSettings)
    {
        JMenu jMenuPoissonSimulator = new JMenu("Poisson Simulator");
        final JCheckBoxMenuItem jMenuItemIsNormalized = new JCheckBoxMenuItem("Normalize spikes");
        final JMenuItem jMenuItemLambdaNormalization = new JMenuItem("Set Episode normalization factor");

        jMenuItemIsNormalized.setSelected(sim.isNormalized);
        jMenuItemIsNormalized.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                sim.isNormalized = jMenuItemIsNormalized.isSelected();
                jMenuItemLambdaNormalization.setEnabled(sim.isNormalized);
            }
        });

        jMenuItemLambdaNormalization.setEnabled(sim.isNormalized);
        jMenuItemLambdaNormalization.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String value = JOptionPane.showInputDialog(frame,
                        "Enter Episode normalization factor (e.g. 1.5)",
                        sim.lambda_eps_factor);
                if ((value != null) && (value.length() > 0))
                {
                    try
                    {
                        sim.lambda_eps_factor = Double.parseDouble(value);
                    }
                    catch(NumberFormatException nfe)
                    {
                        sim.lambda_eps_factor = 1.5;
                        JOptionPane.showMessageDialog(frame, "Please enter a number",
                                "Error converting to number", JOptionPane.ERROR_MESSAGE);
                    }
                    System.out.println("psim.lambda_eps_factor = " + sim.lambda_eps_factor);
                }
            }
        });
        jMenuPoissonSimulator.add(jMenuItemIsNormalized);
        jMenuPoissonSimulator.add(jMenuItemLambdaNormalization);
        jMenuSettings.add(jMenuPoissonSimulator);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddEpisode;
    private javax.swing.JButton jButtonAddStim;
    private javax.swing.JButton jButtonFiringRates;
    private javax.swing.JButton jButtonGenerate;
    private javax.swing.JButton jButtonInterconnect;
    private javax.swing.JButton jButtonLoadEventTypes;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonUpdateEventTypes;
    private javax.swing.JCheckBox jCheckBoxInterconnectGroups;
    private javax.swing.JComboBox jComboBoxInterconType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelRandConnStrengthTitle;
    private javax.swing.JLabel jLabelRandomConnStrengthBase;
    private javax.swing.JMenuItem jMenuItemDelay;
    private javax.swing.JMenuItem jMenuItemLoadWeights;
    private javax.swing.JMenuItem jMenuItemLoadWeightsXml;
    private javax.swing.JMenuItem jMenuItemSave;
    private javax.swing.JMenuItem jMenuItemSaveCsv;
    private javax.swing.JMenuItem jMenuItemSaveWeights;
    private javax.swing.JMenuItem jMenuItemSaveWeightsXml;
    private javax.swing.JMenuItem jMenuItemSynapseFunctions;
    private javax.swing.JMenuItem jMenuItemUpdate;
    private javax.swing.JMenuItem jMenuItemUpdateProb;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
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
    private javax.swing.JTextField jTextFieldEstrong;
    private javax.swing.JTextField jTextFieldEventTypes;
    private javax.swing.JTextField jTextFieldFrest;
    private javax.swing.JTextField jTextFieldInterConnect;
    private javax.swing.JTextField jTextFieldSlope;
    private javax.swing.JTextField jTextFieldTStart;
    private javax.swing.JTextField jTextFieldTUpdate;
    private javax.swing.JTextField jTextFieldTrefractory;
    private javax.swing.JTextField jTextFieldTtotal;
    private javax.swing.JTextField jTextFieldWeightHigh;
    private javax.swing.JTextField jTextFieldWeightLow;
    private edu.iisc.tdminer.gui.simulator.PoissonMatrixPlot simulatorMatrixPlot;
    // End of variables declaration//GEN-END:variables
    
}
