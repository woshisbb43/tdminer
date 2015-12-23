/*
 * DataGenSimulatorPanel.java
 *
 * Created on April 30, 2006, 9:22 AM
 */

package edu.iisc.tdminer.gui.simulator;

import edu.iisc.tdminer.model.ThreadedProgressManager;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminer.gui.ITaskPanel;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.simulation.DatagenSimulator;
import edu.iisc.tdminercore.simulation.ISimulator;
import edu.iisc.tdminercore.simulation.SimEpisode;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author  Deb
 */
public class DataGenSimulatorPanel extends javax.swing.JPanel implements ITaskPanel,ISimulatorPanel
{
    private StateInfo stateInfo;
    private JLabel jLabelStatus;
    private JFrame frame;
    private AbstractTableModel tableModel;
    
    private DatagenSimulator sim = new DatagenSimulator();
    
    /** Creates new form DataGenSimulatorPanel */
    public DataGenSimulatorPanel()
    {
        initComponents();
	TableColumn column = null;
	column = jTableEpisodeList.getColumnModel().getColumn(0);
	column.setPreferredWidth(50);
	column.setMinWidth(60);
	column.setMaxWidth(75);
	column.setResizable(false);

	column = jTableEpisodeList.getColumnModel().getColumn(2);
	column.setPreferredWidth(60);
	column.setMinWidth(60);
	column.setMaxWidth(80);
	column.setResizable(false);        
    }
    
    private void setParameters()
    {
        sim.noiseProb = Double.parseDouble(jTextFieldNoiseProb.getText());
        sim.N = Integer.parseInt(jTextFieldDataLen.getText());
        sim.setDeltaT(Double.parseDouble(jTextFieldDeltaT.getText()));
        sim.setEventFactor(jTextFieldEventTypes.getText().trim());
    }
    
    private void setDefaultValues()
    {
        jTextFieldDataLen.setText("10000");
        jTextFieldNoiseProb.setText("0.01");
        jTextFieldNewEpisode.setText("A (1.5-2.5) B (2.5-3.5) C (3.5-4.5) D");
        jTextFieldEpsDelay.setText("10.0-20.0");
        jTextFieldDeltaT.setText("0.01");
        jTextFieldDeltaT.setEnabled(true);
        jTextFieldEventTypes.setText("A B C D E F G H I J K L M N O P Q R S T U V W X Y Z");
        sim.getEpsiodeList().clear();
        tableModel.fireTableDataChanged();
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
        this.frame = frame;
    }
    
    private AbstractTableModel getTableModel()
    {
        if (tableModel == null)
        {
            tableModel = new AbstractTableModel()
            {
                private String[] columnNames = {"Select", "Episodes", "Count"};
                
                Class[] types = new Class [] {
                    Boolean.class, String.class, Integer.class
                };
                
                public int getColumnCount()
                {
                    return columnNames.length;
                }
                
                public int getRowCount()
                {
                    List data = sim.getEpsiodeList();
                    if (data != null) return data.size();
                    return 0;
                }
                
                @Override
                public String getColumnName(int col)
                {
                    return columnNames[col];
                }
                
                public Object getValueAt(int row, int col)
                {
                    Object retVal = null;
                    List data = sim.getEpsiodeList();
                    SimEpisode e = (SimEpisode)data.get(row);
                    
                    switch (col)
                    {
                        case 0:
                            retVal = new Boolean(e.isSelected());
                            break;
                        case 1:
                            retVal = e.toString();
                            break;
                        case 2:
                            retVal = new Integer(e.getCount());
                            break;
                    }
                    return retVal;
                }
                
                @Override
                public Class getColumnClass(int columnIndex)
                {
                    return types [columnIndex];
                }
                
                @Override
                public boolean isCellEditable(int row, int col)
                {
                    if (col == 0)
                        return true;
                    else
                        return false;
                }
                
                @Override
                public void setValueAt(Object value, int row, int col)
                {
                    boolean val = ((Boolean)value).booleanValue();
                    if (col == 0)
                    {
                        List data = sim.getEpsiodeList();
                        data.remove(row);
                        fireTableDataChanged();
                    }
                }
            };
        }
        
        return tableModel;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldEventTypes = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldNewEpisode = new javax.swing.JTextField();
        jButtonAddEpisode = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jCheckBoxShuffle = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldEpsDelay = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableEpisodeList = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldDataLen = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldDeltaT = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldNoiseProb = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jButtonReset = new javax.swing.JButton();
        jButtonGenerate = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout(5, 5));

        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Select Event Types"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel6.setLayout(new java.awt.BorderLayout(5, 5));

        jLabel1.setText("Event Types (space separated):");
        jPanel6.add(jLabel1, java.awt.BorderLayout.WEST);

        jTextFieldEventTypes.setText("A B C D E F G H I J K L M N O P Q R S T U V W X Y Z");
        jTextFieldEventTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEventTypesActionPerformed(evt);
            }
        });

        jPanel6.add(jTextFieldEventTypes, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel6, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.BorderLayout(0, 10));

        jPanel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Add/Remove Episodes"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel8.setLayout(new java.awt.BorderLayout(5, 5));

        jPanel9.setLayout(new java.awt.BorderLayout(5, 5));

        jLabel4.setText("Episode:");
        jPanel9.add(jLabel4, java.awt.BorderLayout.WEST);

        jTextFieldNewEpisode.setText("A (1.5-2.5) B (2.5-3.5) C (3.5-4.5) D");
        jPanel9.add(jTextFieldNewEpisode, java.awt.BorderLayout.CENTER);

        jButtonAddEpisode.setText("Add");
        jButtonAddEpisode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddEpisodeActionPerformed(evt);
            }
        });

        jPanel9.add(jButtonAddEpisode, java.awt.BorderLayout.EAST);

        jPanel8.add(jPanel9, java.awt.BorderLayout.NORTH);

        jPanel10.setLayout(new java.awt.BorderLayout(0, 10));

        jPanel12.setLayout(new java.awt.BorderLayout(15, 0));

        jPanel12.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        jCheckBoxShuffle.setText("Make parallel");
        jCheckBoxShuffle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxShuffle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel12.add(jCheckBoxShuffle, java.awt.BorderLayout.EAST);

        jLabel5.setText("Inter-Episode delay");
        jPanel12.add(jLabel5, java.awt.BorderLayout.WEST);

        jTextFieldEpsDelay.setText("10.0-20.0");
        jPanel12.add(jTextFieldEpsDelay, java.awt.BorderLayout.CENTER);

        jPanel10.add(jPanel12, java.awt.BorderLayout.CENTER);

        jPanel8.add(jPanel10, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel8, java.awt.BorderLayout.NORTH);

        jTableEpisodeList.setModel(getTableModel());
        jScrollPane2.setViewportView(jTableEpisodeList);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout(15, 15));

        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Generate event sequence"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel4.setLayout(new java.awt.GridLayout(1, 4, 5, 0));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("Length = ");
        jPanel4.add(jLabel2);

        jTextFieldDataLen.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldDataLen.setText("10000");
        jTextFieldDataLen.setMinimumSize(new java.awt.Dimension(80, 20));
        jPanel4.add(jTextFieldDataLen);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("<html>T<sub>update</sub> = </html>");
        jPanel4.add(jLabel6);

        jTextFieldDeltaT.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldDeltaT.setText("0.01");
        jTextFieldDeltaT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDeltaTActionPerformed(evt);
            }
        });

        jPanel4.add(jTextFieldDeltaT);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Noise = ");
        jPanel4.add(jLabel3);

        jTextFieldNoiseProb.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldNoiseProb.setText("0.01");
        jTextFieldNoiseProb.setMinimumSize(new java.awt.Dimension(35, 20));
        jPanel4.add(jTextFieldNoiseProb);

        jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));

        jButtonReset.setText("Reset");
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });

        jPanel5.add(jButtonReset);

        jButtonGenerate.setText("Generate");
        jButtonGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateActionPerformed(evt);
            }
        });

        jPanel5.add(jButtonGenerate);

        jPanel3.add(jPanel5, java.awt.BorderLayout.EAST);

        add(jPanel3, java.awt.BorderLayout.SOUTH);

    }// </editor-fold>//GEN-END:initComponents

    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
// TODO add your handling code here:
        setDefaultValues();
    }//GEN-LAST:event_jButtonResetActionPerformed

    private void jTextFieldDeltaTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDeltaTActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldDeltaTActionPerformed

    private void jButtonGenerateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonGenerateActionPerformed
    {//GEN-HEADEREND:event_jButtonGenerateActionPerformed
// TODO add your handling code here:
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
    
    private void jButtonAddEpisodeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddEpisodeActionPerformed
    {//GEN-HEADEREND:event_jButtonAddEpisodeActionPerformed
// TODO add your handling code here:
        try
        {
            double deltaT = Double.parseDouble(jTextFieldDeltaT.getText());
            jTextFieldDeltaT.setEnabled(false);
            String eps = jTextFieldNewEpisode.getText().trim();
            if (!eps.equals(""))
            {
                sim.addEpisode(jTextFieldNewEpisode.getText(), 
                        jTextFieldEpsDelay.getText(),
                        jCheckBoxShuffle.isSelected(),
                        deltaT);
                tableModel.fireTableDataChanged();
                jTextFieldNewEpisode.setText("");
                jTextFieldEpsDelay.setText("");
            }
        }
        catch (RuntimeException re)
        {
            jLabelStatus.setText("Error adding episode");
            JOptionPane.showMessageDialog(frame, re.getMessage(), "Error adding episode", JOptionPane.ERROR_MESSAGE);
        }        
    }//GEN-LAST:event_jButtonAddEpisodeActionPerformed
            
    private void jTextFieldEventTypesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldEventTypesActionPerformed
    {//GEN-HEADEREND:event_jTextFieldEventTypesActionPerformed
    }//GEN-LAST:event_jTextFieldEventTypesActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddEpisode;
    private javax.swing.JButton jButtonGenerate;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JCheckBox jCheckBoxShuffle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableEpisodeList;
    private javax.swing.JTextField jTextFieldDataLen;
    private javax.swing.JTextField jTextFieldDeltaT;
    private javax.swing.JTextField jTextFieldEpsDelay;
    private javax.swing.JTextField jTextFieldEventTypes;
    private javax.swing.JTextField jTextFieldNewEpisode;
    private javax.swing.JTextField jTextFieldNoiseProb;
    // End of variables declaration//GEN-END:variables
    
    public void refreshState()
    {
    }
    
    public void handleTaskCompletion(int taskIndex)
    {
	IEventDataStream sequence = sim.getEventStream();
	stateInfo.getSession().setSequence(sequence);
	stateInfo.getSession().setEpisodes(null);
	jButtonGenerate.setEnabled(true);
	jLabelStatus.setText("Simulation data successfully generated");
        tableModel.fireTableDataChanged();
    }

    public ISimulator getSimulator()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
