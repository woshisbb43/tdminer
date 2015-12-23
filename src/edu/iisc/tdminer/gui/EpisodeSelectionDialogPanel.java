/*
 * EpisodeSelectionDialogPanel.java
 *
 * Created on March 28, 2006, 7:11 PM
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminer.model.EpisodeSelectionTableModel;
import edu.iisc.tdminer.model.FreqEpisodeTableModel;
import edu.iisc.tdminercore.counter.AbstractSerialCounterInterEventConst;
import edu.iisc.tdminercore.data.AbstractEpisode;
import edu.iisc.tdminercore.data.GeneralizedEpisode;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.data.episode.SerialEpisodeWithIntervals;
import edu.iisc.tdminercore.miner.SessionInfo;
import edu.iisc.tdminercore.util.SerialEpisodeParser;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

/**
 *
 * @author  Deb
 */
public class EpisodeSelectionDialogPanel extends javax.swing.JPanel implements TreeSelectionListener
{
    private EpisodeSet episodes;
    private List<Interval> ivlList;
    private IEventDataStream sequence;
    private SessionInfo session;
    private EpisodeSelectionTableModel model;
    private int columnIndex = -1;
    private boolean ascending = true;
    private JDialog dialog;
    private List<IEpisode> selectedEpisodes = new ArrayList<IEpisode>();
    
    /** Creates new form EpisodeSelectionDialogPanel */
    public EpisodeSelectionDialogPanel(SessionInfo session, JDialog dialog)
    {
        this.session = session;
	this.episodes = session.getEpisodes();
	this.sequence = session.getSequence();
        this.ivlList = session.getIntervalsList();
	this.dialog = dialog;
	initComponents();
	
	JTableHeader header = jTable1.getTableHeader();
	header.addMouseListener(new ColumnHeaderListener());
	
	model = (EpisodeSelectionTableModel)jTable1.getModel();
	jTree1.addTreeSelectionListener(this);
	if (episodes == null) {
           jTree1.setModel(null);
           return;
        }
	
        for (List<IEpisode> list : episodes.levels()) {
          if (list == null) continue;  
          for (IEpisode episode : list) { episode.setSelected(false); }  
        }
        displayResults();
    }

    public void displayResults()
    {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode(new EpisodeInfo("Frequent Episodes", null));
	model.setEventTypes(episodes.getEventFactor());
	DefaultMutableTreeNode selectedNode = null;

	for (int i = 2; i <= episodes.maxEpisodeSize(); i++)
	{
	    List list  = episodes.getEpisodeList(i);
	    if (list != null)
	    {
		Episode.sort(list, IEpisode.FREQUENCY_ORDER);
	    }
	    DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EpisodeInfo(i + " node Episodes", list));
	    root.add(node);
	    if (i == episodes.maxEpisodeSize())
	    {
		model.setData(list);
		jTable1.setModel(model);
		initColumnSizes(jTable1);
		selectedNode = node;
	    }
	}
	jTree1.setModel(new DefaultTreeModel(root));
        if (selectedNode != null)
        {
            jTree1.setSelectionPath(new TreePath(selectedNode.getPath()));
        }
    }
    
    public void valueChanged(TreeSelectionEvent e)
    {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)jTree1.getLastSelectedPathComponent();
	
	if (node == null) return;
	
	EpisodeInfo episodeInfo = (EpisodeInfo)node.getUserObject();
	Object linkedData = episodeInfo.getObject();
	
	if (linkedData instanceof List || linkedData == null)
	{
	    model.setData((List)linkedData);
	    jTable1.setModel(model);
	    initColumnSizes(jTable1);
	}
	columnIndex = -1;
	ascending = true;
    }
    
    
    private void initColumnSizes(JTable table)
    {
	EpisodeSelectionTableModel model = (EpisodeSelectionTableModel)table.getModel();
	TableColumn column = null;
	Component comp = null;
	int headerWidth = 0;
	int cellWidth = 0;
	TableCellRenderer headerRenderer =
		table.getTableHeader().getDefaultRenderer();
	
	column = table.getColumnModel().getColumn(0);
	column.setPreferredWidth(60);
	column.setMinWidth(60);
	column.setMaxWidth(75);
	column.setResizable(false);
	
	column = table.getColumnModel().getColumn(2);
	column.setPreferredWidth(75);
	column.setMinWidth(75);
	column.setMaxWidth(75);
	column.setResizable(true);
    }
    
    
    public List<IEpisode> getSelectedEpisodes()
    {
	return selectedEpisodes;
    }
    
    
    private class EpisodeInfo
    {
	public String name;
	public Object object;
	
	public EpisodeInfo(String name, Object object)
	{
	    this.name = name;
	    this.object = object;
	}
	
	public String toString()
	{
	    return name;
	}
	
	public Object getObject()
	{
	    return object;
	}
	
    }
    
    class ColumnHeaderListener extends MouseAdapter
    {
        @Override
	public void mouseClicked(MouseEvent evt)
	{
	    JTable table = ((JTableHeader)evt.getSource()).getTable();
	    TableColumnModel colModel = table.getColumnModel();
	    int vColIndex = colModel.getColumnIndexAtX(evt.getX());
	    int mColIndex = table.convertColumnIndexToModel(vColIndex);
	    if (vColIndex == -1)
	    {
		return;
	    }
	    Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
	    if (vColIndex == 0)
	    {
		headerRect.width -= 3;    // Hard-coded constant
	    }
	    else
	    {
		headerRect.grow(-3, 0);   // Hard-coded constant
	    }
	    if (!headerRect.contains(evt.getX(), evt.getY()))
	    {
		int vLeftColIndex = vColIndex;
		if (evt.getX() < headerRect.x)
		{
		    vLeftColIndex--;
		}
	    }
	    if (vColIndex == columnIndex)
	    {
		ascending = !ascending;
	    }
	    else
	    {
		ascending = true;
		columnIndex = vColIndex;
	    }
	    List list = model.getData();
	    if (columnIndex == FreqEpisodeTableModel.EPISODE_COLUMN && episodes != null)
	    {
		Episode.sort(list, IEpisode.DICTIONARY_ORDER, ascending, episodes.getEventFactor(), 0);
	    }
	    else if (columnIndex == FreqEpisodeTableModel.FREQUENCY_COLUMN && episodes != null)
	    {
		Episode.sort(list, IEpisode.FREQUENCY_ORDER, ascending, 0);
	    }
	    model.fireTableStructureChanged();
	    EpisodeSelectionDialogPanel.this.initColumnSizes(jTable1);
	}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButtonOk = new javax.swing.JButton();
        jButtonSelectAll = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldEpisode = new javax.swing.JTextField();
        jButtonAdd = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonOk, gridBagConstraints);

        jButtonSelectAll.setText("Select All");
        jButtonSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectAllActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.weightx = 0.5;
        add(jButtonSelectAll, gridBagConstraints);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jTable1.setModel(new EpisodeSelectionTableModel());
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(150, 322));
        jScrollPane2.setViewportView(jTree1);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.WEST);

        jTabbedPane1.addTab("Select Episodes", jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel4.setLayout(new java.awt.BorderLayout(5, 5));

        jLabel1.setText("Episode ( E.g 'A B') :");
        jPanel4.add(jLabel1, java.awt.BorderLayout.WEST);

        jTextFieldEpisode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEpisodeActionPerformed(evt);
            }
        });

        jPanel4.add(jTextFieldEpisode, java.awt.BorderLayout.CENTER);

        jButtonAdd.setText("Add");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jPanel4.add(jButtonAdd, java.awt.BorderLayout.EAST);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        jTabbedPane1.addTab("Add Episode", jPanel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(jTabbedPane1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectAllActionPerformed
        /** 
         * When the select button is presse all currently 
         * visible episodes are checked.
         */
        model.setAll(true);
    }//GEN-LAST:event_jButtonSelectAllActionPerformed

    private void jTextFieldEpisodeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldEpisodeActionPerformed
    {//GEN-HEADEREND:event_jTextFieldEpisodeActionPerformed
	String inputValue = jTextFieldEpisode.getText();
	if (inputValue != null)
	{
	    try
	    {
		IEpisode e = AbstractEpisode.getEpisode(inputValue, sequence.getEventFactor(), ivlList);
		selectedEpisodes.add(e);
		JOptionPane.showMessageDialog(this, "Successfully added " + e.toString(sequence.getEventFactor()), "Episode Added", JOptionPane.INFORMATION_MESSAGE);
		jTextFieldEpisode.setText("");
	    }
	    catch (Exception ex)
	    {
		JOptionPane.showMessageDialog(this, ex.getMessage(), "Error loading episodes", JOptionPane.ERROR_MESSAGE);
		ex.printStackTrace();
	    }
	}
    }//GEN-LAST:event_jTextFieldEpisodeActionPerformed
    
    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOkActionPerformed
    {//GEN-HEADEREND:event_jButtonOkActionPerformed
	if (episodes == null) {
            dialog.setVisible(false);
            dialog.dispose();
            return;
        }
	
        for( List<IEpisode> list : episodes.levels() ) 
        { 
            if (list == null) continue;  
        
            for( IEpisode e : list ) 
            {
                if (!(e.isSelected())) continue; 
            
                IEpisode epi = null;
                if (e.hasFiniteIntervals()) {
                    IEpisode epiIvl = new SerialEpisodeWithIntervals(e.size(), e);
                    for (int k = 0; k < e.size() - 1; k++) {
                        epiIvl.setIntervalId(k, e.getIntervalId(k));
                    }				
                    epi = epiIvl;
                }
                else if (e instanceof GeneralizedEpisode) {
                    epi = new GeneralizedEpisode(e.size(), e.getEventFactor(), e.getDurations());
                }
                else {
                    epi = new Episode(e.size(), e.getEventFactor());
                }

                for (int k = 0; k < e.size(); k++) {
                    epi.setEvent(k, e.getEvent(k));
                }
                epi.setVotes(e.getVotes());
                epi.setEstr(e.getEstr());
                epi.setBeta(e.getBeta());
                
                selectedEpisodes.add(epi);
                //System.out.println("epi = " + epi.toString(sequence.getEventTypes()));
            }
        }	
	dialog.setVisible(false);
	dialog.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed
    
    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddActionPerformed
    {//GEN-HEADEREND:event_jButtonAddActionPerformed
	String inputValue = jTextFieldEpisode.getText();
	if (inputValue != null)
	{
	    try
	    {
                IEpisode e;
                if (session.getCounter() instanceof AbstractSerialCounterInterEventConst)
                    e = SerialEpisodeParser.getSerialEpisode(inputValue, sequence.getEventFactor(), ivlList);
                else
                    e = Episode.getEpisode(inputValue, sequence.getEventFactor(), ivlList);
		selectedEpisodes.add(e);
		JOptionPane.showMessageDialog(this, "Successfully added " + e.toString(sequence.getEventFactor()), "Episode Added", JOptionPane.INFORMATION_MESSAGE);
		jTextFieldEpisode.setText("");
	    }
	    catch (Exception ex)
	    {
		JOptionPane.showMessageDialog(this, ex.getMessage(), "Error loading episodes", JOptionPane.ERROR_MESSAGE);
		ex.printStackTrace();
	    }
	}
    }//GEN-LAST:event_jButtonAddActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JButton jButtonSelectAll;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldEpisode;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
    
}
