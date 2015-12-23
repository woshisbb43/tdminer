/*
 * IntervalInputPanel.java
 *
 * Created on May 3, 2006, 1:24 PM
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminercore.data.Interval;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author patnaik
 * @author phreed@gmail.com
 */
public class IntervalInputPanel extends javax.swing.JPanel
{
    private AbstractTableModel tableModel;
    private List<Interval> intervalsList = new ArrayList<Interval>();
    private JDialog dialog;
    private int dialogType;
    
    public static final int INTERVALS_DISP = 0;
    public static final int DURATION_DISP = 1;
    
    /** Creates new form IntervalInputPanel */
    public IntervalInputPanel(JDialog dialog, int type)
    {
        this.dialog = dialog;
        initComponents();
        
        this.dialogType = type;
        switch (dialogType)
        {
            case INTERVALS_DISP:
                setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Intervals"));
                jLabelHigh.setText("<html>T<sub>high</sub>=</html>");
                jLabelLow.setText("<html>T<sub>low</sub>=</html>");
                break;
            case DURATION_DISP:
                setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Durations"));
                jLabelHigh.setText("<html>D<sub>high</sub>=</html>");
                jLabelLow.setText("<html>D<sub>low</sub>=</html>");
                break;
            default:
                setBorder(javax.swing.BorderFactory.createTitledBorder(null, String.valueOf(this.dialogType)));
        }
    }
    
    private AbstractTableModel getTableModel()
    {
        if (tableModel == null)
        {
            tableModel = new AbstractTableModel()
            {
                private String[] columnNames = {"Select", "Interval"};
                
                Class[] types = new Class [] {
                    Boolean.class, String.class
                };
                
                public int getColumnCount()
                {
                    return columnNames.length;
                }
                
                public int getRowCount()
                {
                    if (intervalsList != null) return intervalsList.size();
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
                    
                    Interval iVal = (Interval)intervalsList.get(row);
                    switch (col)
                    {
                        case 0:
                            retVal = new Boolean(true);
                            break;
                        case 1:
                            retVal = iVal.toString();
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
                        intervalsList.remove(row);
                        fireTableDataChanged();
                    }
                }
            };
        }
        
        return tableModel;
    }
    
    /**
     * The intervals list changes based on the usage
     */
    public List getTimeIntervalsList()
    {
        return intervalsList;
    }
    public void setTimeIntervalsList(List intervalsList)
    {
        this.intervalsList = intervalsList;
    }
    
    public void parseIntervals()
    {
        try
        {
            List<Interval> newIntervalsList = new ArrayList<Interval>();
            String[] lines = jTextAreaIntervals.getText().split("\n");
            for(String line : lines)
            {
                if (line.length() < 1) continue;
                
                Interval ivl = Interval.parse(line);
                newIntervalsList.add(ivl);
            }
            intervalsList = newIntervalsList;
            tableModel.fireTableDataChanged();
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error in parsing intervals",
                    JOptionPane.ERROR_MESSAGE);
            jTabbedPaneIntervals.setSelectedIndex(1);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jTabbedPaneIntervals = new javax.swing.JTabbedPane();
        jPanelInteractive = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanelInteractiveInput = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabelLow = new javax.swing.JLabel();
        jTextFieldTLow = new javax.swing.JTextField();
        jLabelHigh = new javax.swing.JLabel();
        jTextFieldTHigh = new javax.swing.JTextField();
        jButtonAdd = new javax.swing.JButton();
        jPanelInteractiveAction = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jPanelBatch = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaIntervals = new javax.swing.JTextArea();
        jPanelBatchAction = new javax.swing.JPanel();
        jButtonOk1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout(5, 5));

        jTabbedPaneIntervals.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPaneIntervalsStateChanged(evt);
            }
        });

        jPanelInteractive.setLayout(new java.awt.BorderLayout());

        jTable1.setModel(getTableModel());
        jScrollPane1.setViewportView(jTable1);

        jPanelInteractive.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanelInteractiveInput.setLayout(new java.awt.BorderLayout(5, 5));

        jPanel2.setLayout(new java.awt.GridLayout(1, 5, 2, 0));

        jLabelLow.setText("<html>T<sub>low</sub>=</html>");
        jPanel2.add(jLabelLow);

        jPanel2.add(jTextFieldTLow);

        jLabelHigh.setText("<html>T<sub>high</sub>=</html>");
        jPanel2.add(jLabelHigh);

        jPanel2.add(jTextFieldTHigh);

        jButtonAdd.setText("Add");
        jButtonAdd.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jPanel2.add(jButtonAdd);

        jPanelInteractiveInput.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanelInteractiveAction.setLayout(new java.awt.GridBagLayout());

        jPanelInteractiveAction.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 20, 1, 20));
        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jPanelInteractiveAction.add(jButtonOk, new java.awt.GridBagConstraints());

        jPanelInteractiveInput.add(jPanelInteractiveAction, java.awt.BorderLayout.SOUTH);

        jPanelInteractive.add(jPanelInteractiveInput, java.awt.BorderLayout.SOUTH);

        jTabbedPaneIntervals.addTab("Intervals", jPanelInteractive);

        jPanelBatch.setLayout(new java.awt.BorderLayout());

        jTextAreaIntervals.setColumns(20);
        jTextAreaIntervals.setRows(5);
        jScrollPane2.setViewportView(jTextAreaIntervals);

        jPanelBatch.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jButtonOk1.setText("Ok");
        jButtonOk1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOk1ActionPerformed(evt);
            }
        });

        jPanelBatchAction.add(jButtonOk1);

        jPanelBatch.add(jPanelBatchAction, java.awt.BorderLayout.SOUTH);

        jTabbedPaneIntervals.addTab("Details", jPanelBatch);

        add(jTabbedPaneIntervals, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    private void jButtonOk1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOk1ActionPerformed
    {//GEN-HEADEREND:event_jButtonOk1ActionPerformed
        //Preprocessing followed by usual ok action
        parseIntervals();
        jButtonOkActionPerformed(evt);
    }//GEN-LAST:event_jButtonOk1ActionPerformed
    
    private void jTabbedPaneIntervalsStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jTabbedPaneIntervalsStateChanged
    {//GEN-HEADEREND:event_jTabbedPaneIntervalsStateChanged
// TODO add your handling code here:
        //System.out.println("Selected Pane = " + jTabbedPaneIntervals.getSelectedIndex());
        switch(jTabbedPaneIntervals.getSelectedIndex())
        {
            case 0:
                //Parse text into intervals
                parseIntervals();
                break;
            case 1:
                //Convert intervals into text
                StringBuffer buf = new StringBuffer();
                for(Interval ivl:intervalsList)
                {
                    buf.append(ivl.toString());
                    buf.append("\n");
                }
                jTextAreaIntervals.setText(buf.toString());
                break;
        }
        
    }//GEN-LAST:event_jTabbedPaneIntervalsStateChanged
    
    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOkActionPerformed
    {//GEN-HEADEREND:event_jButtonOkActionPerformed
        dialog.setVisible(false);
        dialog.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed
    
    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddActionPerformed
    {//GEN-HEADEREND:event_jButtonAddActionPerformed
        try
        {
            double tLow = Double.parseDouble(jTextFieldTLow.getText());
            double tHigh = Double.parseDouble(jTextFieldTHigh.getText());
            Interval iVal = new Interval(tLow, tHigh);
            intervalsList.add(iVal);
            tableModel.fireTableDataChanged();
            jTextFieldTLow.setText(jTextFieldTHigh.getText());
            jTextFieldTHigh.setText("");
            jTextFieldTHigh.requestFocusInWindow();
        }
        catch (NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(this, nfe.getMessage(), "Error reading parameters", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_jButtonAddActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JButton jButtonOk1;
    private javax.swing.JLabel jLabelHigh;
    private javax.swing.JLabel jLabelLow;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelBatch;
    private javax.swing.JPanel jPanelBatchAction;
    private javax.swing.JPanel jPanelInteractive;
    private javax.swing.JPanel jPanelInteractiveAction;
    private javax.swing.JPanel jPanelInteractiveInput;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPaneIntervals;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextAreaIntervals;
    private javax.swing.JTextField jTextFieldTHigh;
    private javax.swing.JTextField jTextFieldTLow;
    // End of variables declaration//GEN-END:variables
    
}
