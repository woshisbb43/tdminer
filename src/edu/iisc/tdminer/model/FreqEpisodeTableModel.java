/*
 * FreqEpisodeTableModel.java
 *
 * Created on March 19, 2006, 4:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.iisc.tdminer.model;

import edu.iisc.tdminercore.data.AbstractEpisode;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Deb
 */
public class FreqEpisodeTableModel extends AbstractTableModel
{
    private static boolean DEBUG = false;
    private EventFactor eventTypes;
    private String[] columnNames = {"Sl. No.", "Frequent Episode discovered", "Count"};
    
    Class[] types = new Class [] {
	Integer.class, String.class, String.class
    };
    
    public static final int EPISODE_COLUMN = 1;
    public static final int FREQUENCY_COLUMN = 2;
    private int index;
    
    public FreqEpisodeTableModel()
    {
	
    }
    
    public FreqEpisodeTableModel(String[] columnNames)
    {
	this.columnNames = columnNames;
    }
    
    private List<IEpisode> data;
    
    public final Object[] longValues = {"Frequent episodes discovered", new Integer(100000)};
    
    public int getColumnCount()
    {
	return columnNames.length;
    }
    
    public int getRowCount()
    {
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
	//IEpisode e = data.get(row);
        AbstractEpisode e = (AbstractEpisode)data.get(row);

	switch (col)
	{
	    case 0:
		retVal = new Integer(row + 1);
		break;
	    case 1:
		retVal = e.toString(eventTypes, index);
		break;
	    case 2:
		retVal = e.getVotes(index);
		break;
	}
	return retVal;
    }
    
    public IEpisode getEpisode(int row)
    {
        return data.get(row);
    }
    
	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the last column would contain text ("true"/"false"),
	 * rather than a check box.
	 */
    @Override
    public Class getColumnClass(int columnIndex)
    {
	return types [columnIndex];
    }
    
	/*
	 * Don't need to implement this method unless your table's
	 * editable.
	 */
    @Override
    public boolean isCellEditable(int row, int col)
    {
	    return false;
    }
    
	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
    @Override
    public void setValueAt(Object value, int row, int col)
    {
	fireTableCellUpdated(row, col);
    }
    
    public List getData()
    {
	return data;
    }
    
    public void setData(List<IEpisode> data, int index)
    {
	this.data = data;
        this.index = index;
	fireTableDataChanged();
    }
    
    public EventFactor getEventTypes()
    {
	return eventTypes;
    }
    
    public void setEventTypes(EventFactor eventTypes)
    {
	this.eventTypes = eventTypes;
    }
}
