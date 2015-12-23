/*
 * AddSynapsefunctions.java
 *
 * Created on July 17, 2008, 11:53 AM
 */

package edu.iisc.tdminer.gui.simulator;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.simulation.F;
import edu.iisc.tdminercore.simulation.FRamp;
import edu.iisc.tdminercore.simulation.ISimulator;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author  debprakash
 */
public class AddSynapsefunctions extends javax.swing.JDialog {
    ISimulator psim;
    int selectedRow;
    int selectedCol;
    EventFactor eventTypes;
    ArrayList<F> flist;
    MyComboboxModel srcModel, destModel;
    int selectedIndex = -1;

    /** Creates new form AddSynapsefunctions */
    public AddSynapsefunctions(Frame frame, ISimulator psim, int selectedRow, int selectedCol)
    {
        super(frame, true);
        this.psim = psim;
        this.selectedRow = selectedRow;
        this.selectedCol = selectedCol;
        eventTypes = psim.getEventFactor();
        flist = psim.getFlist();
        initComponents();
        if (frame != null)
        {
            int x = (int) (frame.getLocation().getX() + (frame.getSize().getWidth() - getSize().getWidth()) / 2);
            int y = (int) (frame.getLocation().getY() + (frame.getSize().getHeight() - getSize().getHeight()) / 2);
            setLocation(x, y);
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        TableModel tableModel = new TableModel();
        jTableFlist.setModel(tableModel);
        TableColumn col = jTableFlist.getColumnModel().getColumn(0);
	col.setMaxWidth(40);
        
        jComboBoxSrc.setModel(srcModel = new MyComboboxModel());
        jComboBoxDest.setModel(destModel = new MyComboboxModel());
        
        if (selectedRow != -1) 
        {
            jComboBoxSrc.setSelectedIndex(selectedRow + 1);
        }
        else
        {
            jComboBoxSrc.setSelectedItem("-");
        }
        if (selectedCol != -1) 
        {
            jComboBoxDest.setSelectedIndex(selectedCol + 1);
        }
        else
        {
            jComboBoxDest.setSelectedItem("-");
        }
        
        jTableFlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableFlist.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setInputs(jTableFlist.getSelectedRow());
            }            
        });
    }
    
    private class MyComboboxModel implements ComboBoxModel
    {
        ArrayList<ListDataListener> list = new ArrayList<ListDataListener>();
        String type;
        int index;

        public int getIndex()
        {
            return index;
        }
        
        public void setSelectedItem(Object anItem)
        {
            type = anItem.toString();
            index = eventTypes.getId(type);
        }

        public Object getSelectedItem()
        {
            return type;
        }

        public int getSize()
        {
            return eventTypes.getSize() + 1;
        }

        public Object getElementAt(int index)
        {
            if (index == 0) return "-";
            return eventTypes.getName(index - 1);
        }

        public void addListDataListener(ListDataListener l)
        {
            list.add(l);
        }

        public void removeListDataListener(ListDataListener l)
        {
            list.remove(l);
        }
        
    }
    
    private class TableModel extends AbstractTableModel
    {
        public int getRowCount()
        {
            int rows = 0;
            if (flist != null)
            {
                rows = flist.size();
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
            if (flist != null)
            {
                F f = flist.get(rowIndex);
                switch(columnIndex)
                {
                    case 0:
                        ret = new Boolean(true);
                        break;
                    case 1:
                        ret = eventTypes.getName(f.getSrc()) + "->" 
                                + eventTypes.getName(f.getDest()) + " : " + f.toString();
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
                    flist.remove(rowIndex);
                }
                fireTableDataChanged();
                clearInputs();
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
                    return "Synapse Function";
            }
            return null;
        }
    }

    private void clearInputs()
    {
        jTextFieldMaxProb.setText("");
        jTextFieldMinProb.setText("");
        jTextFieldNumber.setText("");
        jTextFieldShift.setText("");
        jTextFieldSize.setText("");
        jComboBoxSrc.setSelectedItem("-");
        jComboBoxDest.setSelectedItem("-");
        jComboBoxSrc.updateUI();
        jComboBoxDest.updateUI();
        selectedIndex = -1;
        jButtonAdd.setEnabled(true);
        jButtonModify.setEnabled(false);
    }

    private void setInputs(int index)
    {
        selectedIndex = index;
        if (index != -1)
        {
            F f = flist.get(index);

            jComboBoxSrc.setSelectedIndex(f.getSrc() + 1);
            jComboBoxDest.setSelectedIndex(f.getDest() + 1);
            jComboBoxSrc.updateUI();
            jComboBoxDest.updateUI();
            jButtonAdd.setEnabled(false);
            jButtonModify.setEnabled(true);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setGroupingUsed(false);
            nf.setMaximumFractionDigits(4);
            if (f instanceof FRamp)
            {
                FRamp framp = (FRamp)f;
                jTextFieldMaxProb.setText(nf.format(framp.getMax()));
                jTextFieldMinProb.setText(nf.format(framp.getMin()));
                jTextFieldNumber.setText(nf.format(framp.getSteps()));
                jTextFieldShift.setText(nf.format(framp.getShift()));
                jTextFieldSize.setText(nf.format(framp.getStep_size()));
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButtonDeleteAll = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableFlist = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jComboBoxSrc = new javax.swing.JComboBox();
        jComboBoxDest = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxFType = new javax.swing.JComboBox();
        jPanelTypeParams = new javax.swing.JPanel();
        jPanelRamp = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldMinProb = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldSize = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldMaxProb = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldShift = new javax.swing.JTextField();
        jTextFieldNumber = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jButtonNew = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jButtonModify = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add Synapse function");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Synapse functions"));

        jButtonDeleteAll.setText("Delete All");
        jButtonDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllActionPerformed(evt);
            }
        });

        jTableFlist.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTableFlist);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addComponent(jButtonDeleteAll)
                .addContainerGap(134, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteAll))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Function Properties"));

        jComboBoxSrc.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBoxDest.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("->");

        jLabel2.setText("Synapse:");

        jLabel3.setText("Type:");

        jComboBoxFType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ramp" }));

        jPanelTypeParams.setLayout(new java.awt.CardLayout());

        jLabel4.setText("Min. Value:");

        jTextFieldMinProb.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        jLabel5.setText("Step size (in sec):");

        jTextFieldSize.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        jLabel6.setText("Max. Value:");

        jTextFieldMaxProb.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        jLabel7.setText("Time Offset (in sec):");

        jTextFieldShift.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        jTextFieldNumber.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        jLabel8.setText("Number of Steps:");

        javax.swing.GroupLayout jPanelRampLayout = new javax.swing.GroupLayout(jPanelRamp);
        jPanelRamp.setLayout(jPanelRampLayout);
        jPanelRampLayout.setHorizontalGroup(
            jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRampLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(jTextFieldSize, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(jTextFieldMinProb, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextFieldMaxProb)
                    .addComponent(jTextFieldShift, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanelRampLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel4, jLabel5});

        jPanelRampLayout.setVerticalGroup(
            jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRampLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelRampLayout.createSequentialGroup()
                        .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextFieldMinProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextFieldSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelRampLayout.createSequentialGroup()
                        .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jTextFieldMaxProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jTextFieldShift, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelRampLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelTypeParams.add(jPanelRamp, "ramp");

        jButtonNew.setText("New");
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });

        jButtonAdd.setText("Add");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonModify.setText("Modify");
        jButtonModify.setEnabled(false);
        jButtonModify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxSrc, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxDest, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxFType, 0, 102, Short.MAX_VALUE))
            .addComponent(jPanelTypeParams, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(jButtonNew)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonModify)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonAdd, jButtonModify, jButtonNew});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jComboBoxSrc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)
                        .addComponent(jComboBoxDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3))
                    .addComponent(jComboBoxFType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelTypeParams, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonNew)
                    .addComponent(jButtonAdd)
                    .addComponent(jButtonModify))
                .addGap(11, 11, 11))
        );

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(137, 137, 137)
                .addComponent(jButtonClose)
                .addContainerGap(139, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClose))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
    this.setVisible(false);
    this.dispose();
}//GEN-LAST:event_jButtonCloseActionPerformed



private void jButtonDeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllActionPerformed
    flist.clear();
    jTableFlist.updateUI();
    clearInputs();
}//GEN-LAST:event_jButtonDeleteAllActionPerformed

private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
    clearInputs();
}//GEN-LAST:event_jButtonNewActionPerformed

private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
    try
    {
        int src = srcModel.getIndex();
        int dest = destModel.getIndex();
        if (src == -1 || dest == -1) throw new Exception("Src and Dest types not selected");
        double min = Double.parseDouble(jTextFieldMinProb.getText());
        double max = Double.parseDouble(jTextFieldMaxProb.getText());
        int steps = Integer.parseInt(jTextFieldNumber.getText());
        double step_size = Double.parseDouble(jTextFieldSize.getText());
        double shift = Double.parseDouble(jTextFieldShift.getText());
        FRamp framp = new FRamp(src, dest, min, max, steps, 
                step_size, shift);
        flist.add(framp);
        jTableFlist.updateUI();
        clearInputs();
    }
    catch(Exception nfe)
    {
        JOptionPane.showMessageDialog(this, nfe.getMessage(), 
                "Error creating synapse update function", JOptionPane.ERROR_MESSAGE);        
    }
}//GEN-LAST:event_jButtonAddActionPerformed

private void jButtonModifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModifyActionPerformed
    try
    {
        if (selectedIndex != -1)
        {
            F f = flist.get(selectedIndex);
            if (f instanceof FRamp)
            {
                FRamp framp = (FRamp)f;
                double min = Double.parseDouble(jTextFieldMinProb.getText());
                double max = Double.parseDouble(jTextFieldMaxProb.getText());
                int steps = Integer.parseInt(jTextFieldNumber.getText());
                double step_size = Double.parseDouble(jTextFieldSize.getText());
                double shift = Double.parseDouble(jTextFieldShift.getText());
                framp.setup(min, max, steps, step_size, shift);
            }
            jTableFlist.updateUI();
        }
    }
    catch(Exception nfe)
    {
        JOptionPane.showMessageDialog(this, nfe.getMessage(), 
                "Error creating synapse update function", JOptionPane.ERROR_MESSAGE);        
    }
}//GEN-LAST:event_jButtonModifyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonDeleteAll;
    private javax.swing.JButton jButtonModify;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JComboBox jComboBoxDest;
    private javax.swing.JComboBox jComboBoxFType;
    private javax.swing.JComboBox jComboBoxSrc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelRamp;
    private javax.swing.JPanel jPanelTypeParams;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableFlist;
    private javax.swing.JTextField jTextFieldMaxProb;
    private javax.swing.JTextField jTextFieldMinProb;
    private javax.swing.JTextField jTextFieldNumber;
    private javax.swing.JTextField jTextFieldShift;
    private javax.swing.JTextField jTextFieldSize;
    // End of variables declaration//GEN-END:variables

}
