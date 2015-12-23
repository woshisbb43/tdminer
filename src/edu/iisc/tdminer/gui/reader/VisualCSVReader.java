/*
 * VisualCSVReader.java
 *
 * Created on November 22, 2006, 3:56 PM
 */

package edu.iisc.tdminer.gui.reader;

import au.com.bytecode.opencsv.CSVReader;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import edu.iisc.tdminercore.data.IEventDataStream;
import edu.iisc.tdminercore.data.IEventDataStream.CONSTRAINT_MODE;
import edu.iisc.tdminercore.data.Interval;
import edu.iisc.tdminercore.reader.CsvEventStreamReader;
import edu.iisc.tdminercore.reader.IEventStreamReader;
import edu.iisc.tdminercore.util.TimeConstraint;

import java.util.List;
import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author  hzg3nc
 */
public class VisualCSVReader extends javax.swing.JDialog
   implements IEventStreamReader 
{
    // in order to match \s appropriately ' ' must come first
    private final String MISC = " \n\t\r";  

    private String streamName = null;
    private String legendName = null;
    private CSVReader reader;
    private Hashtable<String, String> legendMap = null;
    
    private int skiplines = 0;
    private String separator = ",";
    private char separatorChar = CSVReader.DEFAULT_SEPARATOR;
    private int eventTypeIndex = 0;
    private int startTimeIndex = 1;
    private int endTimeIndex = -1;
    
    private boolean isNotified = false;
    private boolean accept = false;
    
    private JList[] choiceList = new JList[4];
    
    private CSVModel tableModel = new CSVModel();
    private CSVModel eventTypeComboModel = new CSVModel();
    private CSVModel startTimeComboModel = new CSVModel();
    private CSVModel endTimeComboModel = new CSVModel();
    
    private ArrayList<ArrayList> table = new ArrayList<ArrayList>();
    private int columns = 0;
    
    public static final int MAX_ROWS = 20;
         
    public VisualCSVReader(Frame frame, String streamName, String legendName) {
        this(frame, streamName);
        this.legendName = legendName;
        
        try
        {
            File f = new File(legendName);
            if (f.exists())
            {
                BufferedReader in = new BufferedReader(new FileReader(legendName));
                this.legendMap = new Hashtable<String, String>();
                String line = null;
                while((line = in.readLine()) != null)
                {
                    line = line.trim();
                    if (line.length() == 0 || line.startsWith("#")) continue;
                    int index = line.indexOf(":");
                    if (index != -1)
                    {
                        String key = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();
                        if (value.indexOf('-') != -1)
                            legendMap.put(key, "[" + value + "]");
                        else
                            legendMap.put(key, value);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
        /** Creates new form VisualCSVReader */
    public VisualCSVReader(Frame frame, String streamName) {
        super(frame, true);
        this.streamName = streamName;
        processFile();
        
        initComponents();
        String fieldSeparatorHelp = 
                "any regular expression can be used\nfor example:\n "
                + ",(comma) the default\n"
                + ";(semicolon) fairly common\n"
                + "\\t  tab delimited\n "
                + "\\s* space delimited";
        
        jLabelFieldSeparator.setToolTipText(fieldSeparatorHelp);
        jTextFieldSeparator.setToolTipText(fieldSeparatorHelp);
        
        for(int i = 0; i < choiceList.length; i++) {
            JList list = new JList();
            choiceList[i] = list;
        }
        
        setSize(500,400);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        if (frame != null) {
            int x = (int)(frame.getLocation().getX() + (frame.getSize().getWidth() -
                    getSize().getWidth())/2);
            int y = (int)(frame.getLocation().getY() + (frame.getSize().getHeight() -
                    getSize().getHeight())/2);
            setLocation(x,y);
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    jComboBoxEventType.setSelectedIndex(1);
                } 
                catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage() + " Event type will have no default ");
                }
                try {
                    if (columns >= 2)
                        jComboBoxStartTime.setSelectedIndex(2);
                    else
                        jComboBoxStartTime.setSelectedIndex(0);
                } 
                catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage() + " Start time will have no default ");
                }
                try {
                    if (columns >= 3)
                        jComboBoxEndTime.setSelectedIndex(3);
                    else
                        jComboBoxEndTime.setSelectedIndex(0);
                } 
                catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage() + " Stop time will have no default ");
                }
                setVisible(true);
            }
        });
    }
    
    private void processFile() {
        try {
            File f = new File(this.streamName);
           
            if (this.separator.length() > 1) {
                Pattern pattern = Pattern.compile(this.separator);
                Matcher matcher = pattern.matcher(MISC);

                if (matcher.find()) {
                    separatorChar = matcher.group().charAt(0);
                }
                else {
                    separatorChar = this.separator.charAt(0);
                }
            }
            else 
            if (this.separator.length() == 1) {
                separatorChar = this.separator.charAt(0);
            }
            
            this.reader = new CSVReader(new FileReader(f), 
                    separatorChar, CSVReader.DEFAULT_QUOTE_CHARACTER, this.skiplines);
            String line = null;
            
            synchronized (table) {
                table.clear();
                columns = 0;

                String [] parts;
                for (int ix=0; ix < MAX_ROWS && (parts = reader.readNext()) != null; ix++) 
                {
                    // System.out.println(parts[0] + parts[1] + "etc...");
                    if (ix < skiplines) continue;
                    ArrayList<String> row = new ArrayList<String>();
                    try {
                        row.clear();
                        for (int jx = 0; jx < parts.length; jx++) {
                            row.add(parts[jx]);
                        }
                        if (row.size() > columns) columns = row.size();
                        table.add(row);
                    } catch (Exception e){
                        row.add(line);
                        if (row.size() > columns) columns = row.size();
                        table.add(row);
                    }
                }
            }
            reader.close();
        }
        catch(IOException ioe) {
            JOptionPane.showMessageDialog(this, ioe.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch(NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                    "Unable to convert text to numeric values", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void updateModels() {
        jTable1.setModel(new CSVModel());
        jComboBoxEventType.setModel(new CSVModel());
        jComboBoxStartTime.setModel(new CSVModel());
        jComboBoxEndTime.setModel(new CSVModel());
        jComboBoxEventType.setSelectedIndex(1);
        if (columns >= 2)
            jComboBoxStartTime.setSelectedIndex(2);
        else
            jComboBoxStartTime.setSelectedIndex(0);
        
        if (columns >= 3)
            jComboBoxEndTime.setSelectedIndex(3);
        else
            jComboBoxEndTime.setSelectedIndex(0);
    }
    
    private void validateInputs() throws Exception {
        eventTypeIndex = jComboBoxEventType.getSelectedIndex() - 1;
        startTimeIndex = jComboBoxStartTime.getSelectedIndex() - 1;
        endTimeIndex = jComboBoxEndTime.getSelectedIndex() - 1;
        
        if (eventTypeIndex == -1)
            throw new Exception("Event Type column not selected");
        
        if (startTimeIndex == -1)
            throw new Exception("Event Start time column not selected");
        
        if (eventTypeIndex == startTimeIndex || endTimeIndex == eventTypeIndex)
            throw new Exception("Same column chosen more than once");
    }
    
    public IEventDataStream read() throws IOException
    { return read((TimeConstraint<CONSTRAINT_MODE>)null); }
      
    public IEventDataStream read(TimeConstraint<CONSTRAINT_MODE> constraint) throws IOException
    {
        if (streamName != null) {
            return read(new FileInputStream(this.streamName), constraint);
        } else {
            throw new IOException("Event stream file not specified");
        }
    }
    
    public IEventDataStream read(InputStream in) throws IOException
    { return read(in, (TimeConstraint<CONSTRAINT_MODE>)null); }
    
    public IEventDataStream read(InputStream in,
           TimeConstraint<CONSTRAINT_MODE> constraints) throws IOException 
    {
        System.out.println("CVS read waiting for user input");
        synchronized (this) {
            while (!isNotified) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println("CVS read resumes");
        if (!accept) { return null; }
        
        CsvEventStreamReader reader = null;
        if (legendMap != null)
        {
            reader = new CsvEventStreamReader(this.reader, legendMap);
        }
        else
        {
            reader = new CsvEventStreamReader(this.reader);
            
        }
        reader.setEventTypeIndex(eventTypeIndex);
        reader.setStartIndex(startTimeIndex);
        reader.setStopIndex(endTimeIndex);
        return reader.read(in, constraints);
    }
    
    public TableModel getTableModel() {
        return tableModel;
    }
    
    public ComboBoxModel getEventTypeComboModel() {
        return eventTypeComboModel;
    }
    
    public ComboBoxModel getStartTimeComboModel() {
        return startTimeComboModel;
    }
    
    public ComboBoxModel getEndTimeModel() {
        return endTimeComboModel;
    }
    
    class CSVModel implements TableModel, ComboBoxModel {
        private Object selectedItem = null;
        public int getRowCount() {
            if (table != null) {
                //System.out.println("table.size() = " + table.size());
                return table.size();
            }
            return 0;
        }
        
        public int getColumnCount() {
            //System.out.println("PAINT columns = " + columns);
            return columns;
        }
        
        public String getColumnName(int columnIndex) {
            //System.out.println("Column " + (columnIndex + 1));
            return "Column " + (columnIndex + 1);
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            //System.out.println("(" + rowIndex + "," + columnIndex + ")");
            String ret = "";
            synchronized (table) {
                if (rowIndex < table.size()) {
                    ArrayList<String> row = table.get(rowIndex);
                    if (columnIndex < row.size()) {
                        ret = row.get(columnIndex);
                    }
                }
            }
            return ret;
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }
        
        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }
        
        public Object getSelectedItem() {
            return selectedItem;
        }
        
        public int getSize() {
            return columns + 1;
        }
        
        public Object getElementAt(int index) {
            if (index == 0) return "None";
            return "Column " + index;
        }
        
        public void addListDataListener(ListDataListener l) {
        }
        
        public void removeListDataListener(ListDataListener l) {
        }
        
        public void addTableModelListener(TableModelListener l) {
        }
        
        public void removeTableModelListener(TableModelListener l) {
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

        jPanelControl = new javax.swing.JPanel();
        jLabelFieldSeparator = new javax.swing.JLabel();
        jTextFieldSeparator = new javax.swing.JTextField();
        jLabelLinesSkip = new javax.swing.JLabel();
        jTextFieldLinesSkip = new javax.swing.JTextField();
        jLabelEventType = new javax.swing.JLabel();
        jComboBoxEventType = new javax.swing.JComboBox();
        jLabelStartTime = new javax.swing.JLabel();
        jComboBoxStartTime = new javax.swing.JComboBox();
        jLabelEndTime = new javax.swing.JLabel();
        jComboBoxEndTime = new javax.swing.JComboBox();
        jScrollPaneDisplaySample = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanelAction = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jPanelControl.setLayout(new java.awt.GridBagLayout());

        jLabelFieldSeparator.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelFieldSeparator.setLabelFor(jTextFieldSeparator);
        jLabelFieldSeparator.setText("Field Separator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanelControl.add(jLabelFieldSeparator, gridBagConstraints);

        jTextFieldSeparator.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSeparator.setText(",");
        jTextFieldSeparator.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTextFieldSeparatorInputMethodTextChanged(evt);
            }
        });
        jTextFieldSeparator.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldSeparatorKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldSeparatorKeyTyped(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanelControl.add(jTextFieldSeparator, gridBagConstraints);

        jLabelLinesSkip.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelLinesSkip.setLabelFor(jTextFieldLinesSkip);
        jLabelLinesSkip.setText("Lines to skip from start");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanelControl.add(jLabelLinesSkip, gridBagConstraints);

        jTextFieldLinesSkip.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldLinesSkip.setText("0");
        jTextFieldLinesSkip.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldLinesSkipKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanelControl.add(jTextFieldLinesSkip, gridBagConstraints);

        jLabelEventType.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelEventType.setText("Event Type column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanelControl.add(jLabelEventType, gridBagConstraints);

        jComboBoxEventType.setModel(getEventTypeComboModel());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanelControl.add(jComboBoxEventType, gridBagConstraints);

        jLabelStartTime.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelStartTime.setText("Start time column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanelControl.add(jLabelStartTime, gridBagConstraints);

        jComboBoxStartTime.setModel(getStartTimeComboModel());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanelControl.add(jComboBoxStartTime, gridBagConstraints);

        jLabelEndTime.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelEndTime.setText("End time column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelControl.add(jLabelEndTime, gridBagConstraints);

        jComboBoxEndTime.setModel(getEndTimeModel());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelControl.add(jComboBoxEndTime, gridBagConstraints);

        getContentPane().add(jPanelControl, java.awt.BorderLayout.NORTH);

        jTable1.setModel(getTableModel());
        jScrollPaneDisplaySample.setViewportView(jTable1);

        getContentPane().add(jScrollPaneDisplaySample, java.awt.BorderLayout.CENTER);

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jPanelAction.add(jButtonOk);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jPanelAction.add(jButtonCancel);

        getContentPane().add(jPanelAction, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jTextFieldSeparatorKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldSeparatorKeyReleased
        separator = jTextFieldSeparator.getText().trim();
        if (separator.length() > 0) {
            System.out.println("separator changed");
            processFile();
            updateModels();
            repaint();
        }
    }//GEN-LAST:event_jTextFieldSeparatorKeyReleased
    
    private void jTextFieldLinesSkipKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLinesSkipKeyReleased
// TODO add your handling code here:
        String skip = jTextFieldLinesSkip.getText();
        
        if (skip.length() > 0) {
            int i1 = jComboBoxEventType.getSelectedIndex();
            int i2 = jComboBoxStartTime.getSelectedIndex();
            int i3 = jComboBoxEndTime.getSelectedIndex();
            
            try {
                int newskip = Integer.parseInt(skip);
                if (newskip < 0 || newskip > MAX_ROWS) {
                    JOptionPane.showMessageDialog(this, "Valid values for skip are 0 to " + MAX_ROWS, "Error", JOptionPane.ERROR_MESSAGE);
                    jTextFieldLinesSkip.setText("" + skiplines);
                } else {
                    skiplines = newskip;
                    System.out.println("skip changed");
                    processFile();
                    i1 = (i1 <= columns)? i1: 1;
                    i2 = (i2 <= columns)? i2: 2;
                    i3 = (i3 <= columns)? i3: 0;
                    jComboBoxEventType.setSelectedIndex(i1);
                    jComboBoxStartTime.setSelectedIndex(i2);
                    jComboBoxEndTime.setSelectedIndex(i3);                    
                    repaint();
                }
            } catch(NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Unable to convert text to numeric values", "Error", JOptionPane.ERROR_MESSAGE);
                jTextFieldLinesSkip.setText("" + skiplines);
            }
        }
    }//GEN-LAST:event_jTextFieldLinesSkipKeyReleased
    
    private void jTextFieldSeparatorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldSeparatorKeyTyped
        
    }//GEN-LAST:event_jTextFieldSeparatorKeyTyped
    
    private void jTextFieldSeparatorInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTextFieldSeparatorInputMethodTextChanged
        
    }//GEN-LAST:event_jTextFieldSeparatorInputMethodTextChanged
    
    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
// TODO add your handling code here:
        setVisible(false);
        dispose();
        accept = false;
        synchronized (this) {
            isNotified = true;
            notifyAll();
        }
    }//GEN-LAST:event_jButtonCancelActionPerformed
    
    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOkActionPerformed
    {//GEN-HEADEREND:event_jButtonOkActionPerformed
        try {
            validateInputs();
            setVisible(false);
            dispose();
            accept = true;
            synchronized (this) {
                isNotified = true;
                notifyAll();
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonOkActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JComboBox jComboBoxEndTime;
    private javax.swing.JComboBox jComboBoxEventType;
    private javax.swing.JComboBox jComboBoxStartTime;
    private javax.swing.JLabel jLabelEndTime;
    private javax.swing.JLabel jLabelEventType;
    private javax.swing.JLabel jLabelFieldSeparator;
    private javax.swing.JLabel jLabelLinesSkip;
    private javax.swing.JLabel jLabelStartTime;
    private javax.swing.JPanel jPanelAction;
    private javax.swing.JPanel jPanelControl;
    private javax.swing.JScrollPane jScrollPaneDisplaySample;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldLinesSkip;
    private javax.swing.JTextField jTextFieldSeparator;
    // End of variables declaration//GEN-END:variables
    
}
