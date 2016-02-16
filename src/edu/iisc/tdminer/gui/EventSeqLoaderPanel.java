/*
 * EventSeqLoaderPanel.java
 *
 * Created on March 16, 2006, 7:36 PM
 */

package edu.iisc.tdminer.gui;

import edu.iisc.tdminer.gui.reader.VisualCSVReader;
import edu.iisc.tdminer.util.Constants;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminer.data.StateInfo;
import edu.iisc.tdminer.model.FreqEpisodeTableModel;
import edu.iisc.tdminercore.reader.IEventStreamReader;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.util.TimeConstraint;
import edu.iisc.tdminer.util.ColumnSorter;
import edu.iisc.tdminer.util.HistogramUtil;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author  Deb
 */
public class EventSeqLoaderPanel extends javax.swing.JPanel implements ITaskPanel {
    private int columnIndex = -1;
    private boolean ascending = true;
    private JFileChooser fc;
    
    private StateInfo stateInfo;
    private JLabel jLabelStatus;
    private FreqEpisodeTableModel model;
    private IEventDataStream sequence;
    private JFrame frame;
    
    /** Creates new form EventSeqLoaderPanel */
    public EventSeqLoaderPanel() {
        initComponents();
        fc = new JFileChooser();
        
        JTableHeader header = jTableEventTypes.getTableHeader();
        header.addMouseListener(new ColumnHeaderListener());
        model = (FreqEpisodeTableModel)jTableEventTypes.getModel();
        HistogramUtil.initHistogram(histPanel, "Plot of event types vs frequency");
    }
    
    public void sortAllRowsBy(DefaultTableModel model, int colIndex, boolean ascending) {
        Vector data = model.getDataVector();
        Collections.sort(data, new ColumnSorter(colIndex, ascending));
        model.fireTableStructureChanged();
    }
    
    public void loadData() {
        Thread th = new Thread(new Runnable() {
            public void run() {
                String seqName = jTextFieldFileName.getText();
                if (seqName != null & !seqName.equals("")) {
                    try {
                        String legendFileName = null;
                        int seq_index = seqName.indexOf(Constants.SEQ_FILE_STR);
                        if (seq_index != -1) {
                            String prefix = seqName.substring(0, seq_index);
                            int lindex = seq_index + Constants.SEQ_FILE_STR.length();
                            String suffix = "";
                            if (lindex < seqName.length()) {
                                suffix = seqName.substring(lindex);
                            }
                            legendFileName = prefix + Constants.LEGEND_FILE_STR + suffix;
                            File legendFile = new File(legendFileName);
                            System.out.println("Legend File : " + legendFile.getAbsolutePath() +
                                    " exists = " + legendFile.exists());
                        }
                        
                        IEventStreamReader reader = null;
                        if (legendFileName != null) {
                            reader = new VisualCSVReader(frame, seqName, legendFileName);
                        } else {
                            reader = new VisualCSVReader(frame, seqName);
                        }
                        TimeConstraint<CONSTRAINT_MODE> constraints =
                                stateInfo.getSession().getTimeConstraints();
                        
                        InputStream in = new BufferedInputStream(
                                new ProgressMonitorInputStream(
                                EventSeqLoaderPanel.this, "Reading "
                                + seqName, new FileInputStream(seqName)));
                        
                        IEventDataStream seq = reader.read(in, constraints);
                        if (seq != null)
                        {
                            //System.out.println("Event stream loaded: will now check that it is sorted");
                            //seq.sort();
                            //System.out.println("Event stream is sorted");
                            stateInfo.getSession().setSequence(seq);
                            update();
                            jTextFieldFileName.setText(seqName);
                            sequence = stateInfo.getSession().getSequence();
                        }
                        else
                        {
                            jTextFieldFileName.setText("");
                        }
                    } catch (IOException ioe) {
                        jLabelStatus.setText("Error while loading event sequence");
                        JOptionPane.showMessageDialog(EventSeqLoaderPanel.this, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                    EventSeqLoaderPanel.this.repaint();
                    frame.setTitle("TDMiner - [" + seqName + "]");
                }
            }
        });
        th.start();
    }
    
    public void update() {
        IEventDataStream seq = stateInfo.getSession().getSequence();
        
        if (seq == null) return;
        
        EventFactor eventTypes = seq.getEventFactor();
        List<IEpisode> list = eventTypes.getEpisodeList();
        jLabelEventTypesNum.setText("Number of Event types = " + list.size());
        jLabelEventSeqLen.setText("Length of event sequence = " + seq.getSize());
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        jLabelTime.setText("Time = " + nf.format(seq.getSequenceStart()) +
                " to " + nf.format(seq.getSequenceEnd()));
        model.setEventTypes(eventTypes);
        model.setData(list, 0);
        
        jTableEventTypes.setModel(model);
        jLabelStatus.setText("Successfully loaded event sequence");
        histPanel.clear(0);
        HistogramUtil.changeDataUsingJTable(histPanel, model.getData(), model.getEventTypes(), "Plot of event types vs frequency");
        stateInfo.getSession().setEpisodes(null);
        jTextFieldFileName.setText("Simulation data loaded successfully");
        frame.setTitle("TDMiner - [Simulated Event Sequence]");
    }
    
    public void setJLabelStatus(JLabel jLabelStatus) {
        this.jLabelStatus = jLabelStatus;
    }
    
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
    
    public void setStateInfo(StateInfo stateInfo) {
        this.stateInfo = stateInfo;
    }
    
    public void refreshState() {
        if (sequence != stateInfo.getSession().getSequence()) {
            if (stateInfo.getSession().getSequence() != null) {
                this.sequence = stateInfo.getSession().getSequence();
                update();
            }
        }
    }
    
    public void handleTaskCompletion(int taskIndex) {
    }

    boolean openDataSequence()
    {
        if (Constants.CURRENT_DIR != null)
        {
            fc.setCurrentDirectory(Constants.CURRENT_DIR);
        }
        int returnVal = fc.showOpenDialog(EventSeqLoaderPanel.this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
            System.out.println("Open command cancelled by user.");
            return false;
        }
        File file = fc.getSelectedFile();

        //This is where a real application would open the file.
        System.out.println("Opening: " + file.getName() + ".");
        jTextFieldFileName.setText(file.getPath());
        loadData();
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuReset = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuSaveImage = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jLabelFile = new javax.swing.JLabel();
        jTextFieldFileName = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanelEventTypes = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableEventTypes = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabelEventTypesNum = new javax.swing.JLabel();
        jLabelEventSeqLen = new javax.swing.JLabel();
        jLabelTime = new javax.swing.JLabel();
        jPanelHistogram = new javax.swing.JPanel();
        jPanelHist = new javax.swing.JPanel();
        histPanel = new ptolemy.plot.Plot();

        jMenuReset.setText("Reset");
        jMenuReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuResetActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuReset);
        jPopupMenu1.add(jSeparator1);

        jMenuSaveImage.setText("Save Image...");
        jMenuSaveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSaveImageActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuSaveImage);

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("File Name"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanel1.setLayout(new java.awt.BorderLayout(5, 0));

        jLabelFile.setText("Event sequence file name:");
        jPanel1.add(jLabelFile, java.awt.BorderLayout.WEST);

        jTextFieldFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFileNameActionPerformed(evt);
            }
        });
        jPanel1.add(jTextFieldFileName, java.awt.BorderLayout.CENTER);

        jButtonBrowse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Open16.gif"))); // NOI18N
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonBrowse, java.awt.BorderLayout.EAST);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridLayout(1, 2, 10, 10));

        jPanelEventTypes.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Event Types"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        jPanelEventTypes.setLayout(new java.awt.BorderLayout(5, 5));

        jTableEventTypes.setModel(new FreqEpisodeTableModel(new String[] {"Sl. No.", "Event Type", "# No. of occurences"}));
        jScrollPane1.setViewportView(jTableEventTypes);

        jPanelEventTypes.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.GridLayout(3, 1));

        jLabelEventTypesNum.setText("Number of Event types = ");
        jPanel4.add(jLabelEventTypesNum);

        jLabelEventSeqLen.setText("Length of event sequence = ");
        jPanel4.add(jLabelEventSeqLen);

        jLabelTime.setText("Time = ");
        jPanel4.add(jLabelTime);

        jPanelEventTypes.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jPanel2.add(jPanelEventTypes);

        jPanelHistogram.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Histogram"), javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        jPanelHistogram.setLayout(new java.awt.BorderLayout(5, 5));

        jPanelHist.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanelHist.setLayout(new java.awt.GridLayout(1, 1));

        histPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                histPanelMouseReleased(evt);
            }
        });
        jPanelHist.add(histPanel);

        jPanelHistogram.add(jPanelHist, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanelHistogram);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
        
    private void jTextFieldFileNameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldFileNameActionPerformed
    {//GEN-HEADEREND:event_jTextFieldFileNameActionPerformed
        loadData();
    }//GEN-LAST:event_jTextFieldFileNameActionPerformed
        
    public void openEventStreamFile(java.awt.event.ActionEvent evt) {
        jButtonBrowseActionPerformed(evt);
    }
    
    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonBrowseActionPerformed
    {//GEN-HEADEREND:event_jButtonBrowseActionPerformed
        openDataSequence();
    }//GEN-LAST:event_jButtonBrowseActionPerformed
    
    private void jMenuResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuResetActionPerformed
        if (model.getData() == null) return;
        
        HistogramUtil.changeDataUsingJTable(histPanel, model.getData(), 
                model.getEventTypes(), "Plot of event types vs frequency");
    }//GEN-LAST:event_jMenuResetActionPerformed

    private void jMenuSaveImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveImageActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return (f.getName().toLowerCase().endsWith("png") || f.isDirectory());
            }
            public String getDescription() {
                return "Portable Network Graphics format (*.png)";
            }
        });
        if (Constants.CURRENT_DIR != null) fc.setCurrentDirectory(Constants.CURRENT_DIR);
        int ret = fc.showSaveDialog(this);
        Constants.CURRENT_DIR = fc.getCurrentDirectory();
        
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            int width = 500;
            int height = 500;
            BufferedImage image = histPanel.exportImage(new Rectangle(width, height));
            try {
                if (!f.getName().toLowerCase().endsWith(".png")) {
                    f = new File(f.getPath() + ".png");
                }
                ImageIO.write(image,"PNG",f);
            } catch(IOException ioe) {
                jLabelStatus.setText("Error while saving frequency histogram plot");
                JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error while saving frequency histogram plot", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuSaveImageActionPerformed

    private void histPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_histPanelMouseReleased
	if (evt.isPopupTrigger())
	{
	    jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
	}
    }//GEN-LAST:event_histPanelMouseReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ptolemy.plot.Plot histPanel;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabelEventSeqLen;
    private javax.swing.JLabel jLabelEventTypesNum;
    private javax.swing.JLabel jLabelFile;
    private javax.swing.JLabel jLabelTime;
    private javax.swing.JMenuItem jMenuReset;
    private javax.swing.JMenuItem jMenuSaveImage;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelEventTypes;
    private javax.swing.JPanel jPanelHist;
    private javax.swing.JPanel jPanelHistogram;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTableEventTypes;
    private javax.swing.JTextField jTextFieldFileName;
    // End of variables declaration//GEN-END:variables
    
    
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
            if (columnIndex == FreqEpisodeTableModel.EPISODE_COLUMN) {
                Episode.sort(list, IEpisode.DICTIONARY_ORDER, ascending, model.getEventTypes(), 0);
            } else if (columnIndex == FreqEpisodeTableModel.FREQUENCY_COLUMN) {
                Episode.sort(list, IEpisode.FREQUENCY_ORDER, ascending, 0);
            }
            model.fireTableStructureChanged();
            
            HistogramUtil.changeDataUsingJTable(histPanel, list, model.getEventTypes(), "Plot of event types vs frequency");
        }
    }
}


