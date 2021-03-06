/*
 * EventTypeDispDialog.java
 *
 * Created on March 28, 2006, 1:25 PM
 */

package edu.iisc.tdminer.gui;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  patnaik
 */
public class EventTypeDispDialog extends javax.swing.JPanel
{
    /** Creates new form EventTypeDispDialog */
    public EventTypeDispDialog()
    {
	initComponents();
    }
    
    public void setValuesInList(List<String> list) 
    {
	DefaultTableModel model = (DefaultTableModel)jTable1.getModel();
	for (int i = 0; i < list.size(); i++)
	{
	    String eType = (String)list.get(i);
	    Integer index = new Integer(i);
	    Vector vect = new Vector();
	    vect.add(index);
	    vect.add(eType);
	    model.addRow(vect);
	}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Table of Event Types");
        add(jLabel1, java.awt.BorderLayout.NORTH);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Y Value", "Event Type"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean []
            {
                false, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
    
}
