/*
 * AddExternalStim.java
 *
 * Created on May 7, 2008, 12:57 AM
 */
package edu.iisc.tdminer.gui.simulator;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.simulation.ExternalInput;
import edu.iisc.tdminercore.simulation.ExternalStimulator;
import java.awt.Frame;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author  debprakash
 */
public class AddExternalStim extends javax.swing.JDialog
{
    private EventFactor eventTypes;
    private List<ExternalInput> elist;
    private List<ExternalStimulator> eStimlist;
    
    private class TableModel extends AbstractTableModel
    {
        public int getRowCount()
        {
            int rows = 0;
            if (elist != null)
            {
                rows = elist.size();
            }
            return rows;
        }

        public int getColumnCount()
        {
            return 3;
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Object ret = null;
            if (elist != null)
            {
                ExternalInput e = elist.get(rowIndex);
                switch(columnIndex)
                {
                    case 0:
                        ret = new Boolean(e.isSelected());
                        break;
                    case 1:
                        ret = eventTypes.getName(e.getNeuron());
                        break;
                    case 2:
                        ret = new String(e.getFile());
                        break;
                }
            }
            return ret;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex)
        {
            if (columnIndex == 0)
            {
                boolean val = ((Boolean)value).booleanValue();
                if (!val)
                {
                    elist.remove(rowIndex);
                }
                fireTableDataChanged();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return (columnIndex == 0);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch(columnIndex)
            {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
            }
            return null;
        }

        @Override
        public String getColumnName(int column)
        {
            switch(column)
            {
                case 0:
                    return "Sel";
                case 1:
                    return "Neuron";
                case 2:
                    return "File";
            }
            return null;
        }
    }

    private class ExtTableModel extends AbstractTableModel
    {
        public int getRowCount()
        {
            int rows = 0;
            if (eStimlist != null)
            {
                rows = eStimlist.size();
            }
            return rows;
        }

        public int getColumnCount()
        {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Object ret = null;
            if (elist != null)
            {
                ExternalStimulator e = eStimlist.get(rowIndex);
                switch(columnIndex)
                {
                    case 0:
                        ret = new Boolean(e.isSelected());
                        break;
                    case 1:
                        ret = new String(e.getFile());
                        break;
                }
            }
            return ret;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex)
        {
            if (columnIndex == 0)
            {
                boolean val = ((Boolean)value).booleanValue();
                if (!val)
                {
                    eStimlist.remove(rowIndex);
                }
                fireTableDataChanged();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return (columnIndex == 0);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch(columnIndex)
            {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
            }
            return null;
        }

        @Override
        public String getColumnName(int column)
        {
            switch(column)
            {
                case 0:
                    return "Sel";
                case 1:
                    return "File";
            }
            return null;
        }
    }
    /** Creates new form AddExternalStim */
    private AddExternalStim(java.awt.Frame parent, EventFactor eventTypes, 
            List<ExternalInput> elist, List<ExternalStimulator> eStimlist)
    {
        super(parent, true);
        this.setTitle("Select External Stimulation");
        this.eventTypes = eventTypes;
        this.elist = elist;
        this.eStimlist = eStimlist;
        initComponents();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for(int i = 0; i < eventTypes.getSize(); i++)
            model.addElement(eventTypes.get(i));
        jComboBoxEventType.setModel(model);
        jComboBoxEventType.setSelectedIndex(0);
        
        TableModel tableModel = new TableModel();
        jTableExtInput.setModel(tableModel);
        
        final ExtTableModel extTableModel = new ExtTableModel();
        jTableExtStim.setModel(extTableModel);
        
        TableColumn col = jTableExtStim.getColumnModel().getColumn(0);
	col.setMaxWidth(40);

        col = jTableExtInput.getColumnModel().getColumn(0);
	col.setMaxWidth(40);
        col = jTableExtInput.getColumnModel().getColumn(1);
	col.setMaxWidth(100);
    }

    public static void getExternInput(EventFactor eventTypes, Frame frame, 
            List<ExternalInput> elist, List<ExternalStimulator> eStimlist)
            throws Exception
    {
        AddExternalStim dialog = new AddExternalStim(frame, eventTypes, elist, eStimlist);
        dialog.setSize(460, 265);

        if (frame != null)
        {
            int x = (int) (frame.getLocation().getX() + (frame.getSize().getWidth() - dialog.getSize().getWidth()) / 2);
            int y = (int) (frame.getLocation().getY() + (frame.getSize().getHeight() - dialog.getSize().getHeight()) / 2);
            dialog.setLocation(x, y);
        }
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }


    private ExternalInput getExternInput() throws Exception
    {
        EventFactor.EventType type = (EventFactor.EventType)jComboBoxEventType.getSelectedItem();
        String filename = jTextFieldFileName.getText();
        if (filename == null || "".equals(filename)) throw new Exception("No input file was selected");
        ExternalInput e = new ExternalInput(type.id, filename);
        return e;
    }
    
    private ExternalStimulator getExternalStimulator() throws Exception
    {
        int size = eventTypes.getSize();
        String filename = jTextFieldExtFileName.getText();
        if (filename == null || "".equals(filename)) throw new Exception("No input file was selected");
        ExternalStimulator e = new ExternalStimulator(size, filename);
        double prob = Double.parseDouble(jTextFieldCondProb.getText());
        if (prob < 0 || prob > 1.0) throw new Exception("Invalid value [" + prob + "] for cond. probability");
        e.connectAll(prob);
        return e;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanel2 = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jTabbedPanelStim = new javax.swing.JTabbedPane();
        jPanelEnternal = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableExtStim = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldExtFileName = new javax.swing.JTextField();
        jButtonExtBrowse = new javax.swing.JButton();
        jButtonExtAdd = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldCondProb = new javax.swing.JTextField();
        jPanelInternal = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxEventType = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldFileName = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableExtInput = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonOk);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jTableExtStim.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTableExtStim);

        jLabel3.setText("Stimulation File :");

        jTextFieldExtFileName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jTextFieldExtFileNameActionPerformed(evt);
            }
        });

        jButtonExtBrowse.setText("Browse");
        jButtonExtBrowse.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonExtBrowseActionPerformed(evt);
            }
        });

        jButtonExtAdd.setText("Add");
        jButtonExtAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonExtAddActionPerformed(evt);
            }
        });

        jLabel4.setText("Cond. Probability:");
        jLabel4.setToolTipText("Conditional Probability");

        jTextFieldCondProb.setText("0.8");

        javax.swing.GroupLayout jPanelEnternalLayout = new javax.swing.GroupLayout(jPanelEnternal);
        jPanelEnternal.setLayout(jPanelEnternalLayout);
        jPanelEnternalLayout.setHorizontalGroup(
            jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEnternalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelEnternalLayout.createSequentialGroup()
                        .addGroup(jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelEnternalLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldExtFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE))
                            .addGroup(jPanelEnternalLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldCondProb, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonExtAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonExtBrowse, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanelEnternalLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel3, jLabel4});

        jPanelEnternalLayout.setVerticalGroup(
            jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEnternalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jButtonExtBrowse)
                    .addComponent(jTextFieldExtFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonExtAdd)
                    .addGroup(jPanelEnternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jTextFieldCondProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jTabbedPanelStim.addTab("External", jPanelEnternal);

        jLabel1.setText("Neuron/Event Type :");

        jLabel2.setText("External Stimulation File :");

        jTextFieldFileName.setEditable(false);

        jButtonBrowse.setText("Browse");
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonBrowseActionPerformed(evt);
            }
        });

        jButtonAdd.setText("Add");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAddActionPerformed(evt);
            }
        });

        jTableExtInput.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTableExtInput);

        javax.swing.GroupLayout jPanelInternalLayout = new javax.swing.GroupLayout(jPanelInternal);
        jPanelInternal.setLayout(jPanelInternalLayout);
        jPanelInternalLayout.setHorizontalGroup(
            jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInternalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelInternalLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(24, 24, 24)
                        .addComponent(jComboBoxEventType, 0, 204, Short.MAX_VALUE))
                    .addGroup(jPanelInternalLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelInternalLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jButtonAdd))
                            .addGroup(jPanelInternalLayout.createSequentialGroup()
                                .addComponent(jTextFieldFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                                .addGap(6, 6, 6)
                                .addComponent(jButtonBrowse)))))
                .addContainerGap())
            .addGroup(jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelInternalLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanelInternalLayout.setVerticalGroup(
            jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelInternalLayout.createSequentialGroup()
                .addContainerGap(102, Short.MAX_VALUE)
                .addGroup(jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxEventType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonBrowse)
                    .addComponent(jTextFieldFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAdd))
            .addGroup(jPanelInternalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelInternalLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .addGap(86, 86, 86)))
        );

        jTabbedPanelStim.addTab("Internal", jPanelInternal);

        getContentPane().add(jTabbedPanelStim, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonBrowseActionPerformed
    {//GEN-HEADEREND:event_jButtonBrowseActionPerformed
        JFileChooser jfc = new JFileChooser();
        int option = jfc.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION)
        {
            String filename = jfc.getSelectedFile().getAbsolutePath();
            jTextFieldFileName.setText(filename);
        }
}//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOkActionPerformed
    {//GEN-HEADEREND:event_jButtonOkActionPerformed
        this.setVisible(false);
        this.dispose();
}//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddActionPerformed
    {//GEN-HEADEREND:event_jButtonAddActionPerformed
        try
        {
            ExternalInput e = this.getExternInput();
            if (!elist.contains(e))
            {
                elist.add(e);
                ((TableModel)jTableExtInput.getModel()).fireTableDataChanged();
                jTextFieldFileName.setText("");
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Input already defined", "Error adding external input", JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error adding external input", JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_jButtonAddActionPerformed

    private void jTextFieldExtFileNameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldExtFileNameActionPerformed
    {//GEN-HEADEREND:event_jTextFieldExtFileNameActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jTextFieldExtFileNameActionPerformed

    private void jButtonExtBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonExtBrowseActionPerformed
    {//GEN-HEADEREND:event_jButtonExtBrowseActionPerformed
        JFileChooser jfc = new JFileChooser();
        int option = jfc.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION)
        {
            String filename = jfc.getSelectedFile().getAbsolutePath();
            jTextFieldExtFileName.setText(filename);
        }
    }//GEN-LAST:event_jButtonExtBrowseActionPerformed

    private void jButtonExtAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonExtAddActionPerformed
    {//GEN-HEADEREND:event_jButtonExtAddActionPerformed
        try
        {
            ExternalStimulator e = this.getExternalStimulator();
            if (!eStimlist.contains(e))
            {
                eStimlist.add(e);
                ((ExtTableModel)jTableExtStim.getModel()).fireTableDataChanged();
                jTextFieldExtFileName.setText("");
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Input already defined", "Error adding external Stimulus", JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error adding external Stimulus", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButtonExtAddActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonExtAdd;
    private javax.swing.JButton jButtonExtBrowse;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JComboBox jComboBoxEventType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelEnternal;
    private javax.swing.JPanel jPanelInternal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPanelStim;
    private javax.swing.JTable jTableExtInput;
    private javax.swing.JTable jTableExtStim;
    private javax.swing.JTextField jTextFieldCondProb;
    private javax.swing.JTextField jTextFieldExtFileName;
    private javax.swing.JTextField jTextFieldFileName;
    // End of variables declaration//GEN-END:variables
}
