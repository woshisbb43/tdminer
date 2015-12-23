/*
 * EpisodeExportDialog.java
 *
 * Created on January 9, 2007, 10:45 AM
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.IEpisode;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author  Debprakash Patnaik
 */
public class EpisodeExportDialog extends javax.swing.JDialog
{
    private Vector<EpisodeSelectEntry> selectionList = new Vector<EpisodeSelectEntry>();
    private EpisodeSet episodes;
    private boolean success = false;
    
    public int getTimeStampChoice()
    {
        int ret = -1;
        if (jRadioButtonFirst.isSelected()) ret = 0;
        else if (jRadioButtonLast.isSelected()) ret = 1;
        else if (jRadioButtonAvg.isSelected()) ret = 2;
        
        return ret;
    }
    
    /** Creates new form EpisodeExportDialog */
    public EpisodeExportDialog(java.awt.Frame frame, boolean modal, EpisodeSet episodes)
    {
        super(frame, modal);
        this.episodes = episodes;
        initComponents();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(300,400);
        if (frame != null)
        {
            int x = (int)(frame.getLocation().getX() + (frame.getSize().getWidth() - getSize().getWidth())/2);
            int y = (int)(frame.getLocation().getY() + (frame.getSize().getHeight() - getSize().getHeight())/2);
            setLocation(x,y);
        }
        
        for (int i = 1; i <= episodes.maxEpisodeSize(); i++)
        {
            EpisodeSelectEntry e = new EpisodeSelectEntry();
            e.episodeIndex = i;
            List list = episodes.getEpisodeList(i);
            if (list != null)
            {
                e.select = true;
                e.size = list.size();
            }
            else
            {
                e.select = false;
                e.size = 0;
            }
            selectionList.add(e);
        }
        
        
        jTable1.setModel(new TableModel()
        {
            public void addTableModelListener(TableModelListener l)
            {
            }
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
            public int getColumnCount()
            {
                return 2;
            }
            public String getColumnName(int columnIndex)
            {
                switch(columnIndex)
                {
                    case 0:
                        return "Select";
                    case 1:
                        return "Episode";
                }
                return null;
            }
            public int getRowCount()
            {
                return selectionList.size();
            }
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                EpisodeSelectEntry e = selectionList.get(rowIndex);
                switch(columnIndex)
                {
                    case 0:
                        return new Boolean(e.select);
                    case 1:
                        return new String(e.episodeIndex + " - node Episode (" + e.size + ")");
                }
                return null;
            }
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                if (columnIndex == 0) return true;
                return false;
            }
            public void removeTableModelListener(TableModelListener l)
            {
            }
            public void setValueAt(Object aValue, int rowIndex, int columnIndex)
            {
                if (columnIndex == 0 && aValue instanceof Boolean)
                {
                    EpisodeSelectEntry e = selectionList.get(rowIndex);
                    e.select = ((Boolean)aValue).booleanValue();
                }
            }
        });
        
        jTable1.getColumnModel().getColumn(0).setMaxWidth(40);
    }
    
    public boolean isCancelled()
    {
        return !success;
    }

    public void getEpisodesList(ArrayList<IEpisode> episodesList)
    {
        if (success)
        {
            boolean filterOn = jCheckBoxFilterEpisodes.isSelected();
            episodesList.clear();
            int n = 0;
            for (int i = episodes.maxEpisodeSize(); i >= 1; i--)
            {
                n = episodesList.size();
                EpisodeSelectEntry e = selectionList.get(i - 1);
                if (e.select)
                {
                    List<IEpisode> list = episodes.getEpisodeList(i);
                    if (list != null)
                    {
                        for (IEpisode eps : list)
                        {
                            boolean canAdd = true;
                            if (filterOn)
                            {
                                for (int j = 0; j < n; j++)
                                {
                                    IEpisode l = episodesList.get(j);
                                    if (isCovered(eps, l))
                                    {
                                        canAdd = false;
                                        break;
                                    }
                                }
                            }
                            if (canAdd) episodesList.add(eps);
                        }
                    }
                }
            }
        }
    }
    
    private boolean isCovered(IEpisode small, IEpisode big)
    {
        int[] stypes = small.getEventTypeIndices();
        int[] btypes = big.getEventTypeIndices();
        
        if (stypes.length > btypes.length)
        {
            int[] temp = stypes;
            stypes = btypes;
            btypes = temp;
        }
        int j = 0;
        for (int i = 0; i < btypes.length; i++)
        {
            if (stypes[j] == btypes[i])
            {
                j++;
                if (j >= stypes.length) return true;
            }
        }
        return false;
    }
    
    class EpisodeSelectEntry
    {
        boolean select;
        int episodeIndex;
        int size;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;
        
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jCheckBoxFilterEpisodes = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jRadioButtonFirst = new javax.swing.JRadioButton();
        jRadioButtonLast = new javax.swing.JRadioButton();
        jRadioButtonAvg = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export Options");
        jPanel1.setLayout(new java.awt.GridBagLayout());
        
        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), javax.swing.BorderFactory.createTitledBorder("Select Export Options")));
        jLabel1.setText("1. Select Episodes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel1, gridBagConstraints);
        
        jScrollPane1.setViewportView(jTable1);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jScrollPane1, gridBagConstraints);
        
        jLabel2.setText("2. Select Episode Time Stamp");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel2, gridBagConstraints);
        
        jCheckBoxFilterEpisodes.setSelected(true);
        jCheckBoxFilterEpisodes.setText("Filter Covered Episodes");
        jCheckBoxFilterEpisodes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxFilterEpisodes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jCheckBoxFilterEpisodes, gridBagConstraints);
        
        jPanel3.setLayout(new java.awt.GridLayout(1, 3, 2, 0));
        
        buttonGroup1.add(jRadioButtonFirst);
        jRadioButtonFirst.setText("<html>T<sub>First Event</sub></html>");
        jRadioButtonFirst.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonFirst.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel3.add(jRadioButtonFirst);
        
        buttonGroup1.add(jRadioButtonLast);
        jRadioButtonLast.setText("<html>T<sub>Last Event</sub></html>");
        jRadioButtonLast.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonLast.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel3.add(jRadioButtonLast);
        
        buttonGroup1.add(jRadioButtonAvg);
        jRadioButtonAvg.setSelected(true);
        jRadioButtonAvg.setText("<html>T<sub>Average</sub></html>");
        jRadioButtonAvg.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButtonAvg.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel3.add(jRadioButtonAvg);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jPanel3, gridBagConstraints);
        
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        
        jPanel2.setLayout(new java.awt.GridBagLayout());
        
        jButton1.setText("Ok");
        jButton1.setMinimumSize(new java.awt.Dimension(70, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(70, 25));
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton1, gridBagConstraints);
        
        jButton2.setText("Cancel");
        jButton2.setMinimumSize(new java.awt.Dimension(70, 25));
        jButton2.setPreferredSize(new java.awt.Dimension(70, 25));
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton2, gridBagConstraints);
        
        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);
        
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
// TODO add your handling code here:
        success = true;
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
// TODO add your handling code here:
        success = false;
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed
    
    
    
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBoxFilterEpisodes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButtonAvg;
    private javax.swing.JRadioButton jRadioButtonFirst;
    private javax.swing.JRadioButton jRadioButtonLast;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
// End of variables declaration//GEN-END:variables
    
}
