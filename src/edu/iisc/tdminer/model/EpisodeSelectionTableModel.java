/*
 * VisualizationDisplayTableModel.java
 *
 * Created on March 25, 2006, 2:55 PM
 *
 */

package edu.iisc.tdminer.model;

import edu.iisc.tdminercore.data.AbstractEpisode;
import edu.iisc.tdminercore.data.EventFactor;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Deb
 */
public class EpisodeSelectionTableModel extends AbstractTableModel
{
    private static boolean DEBUG = false;

    private List data;
    private EventFactor eventTypes;
    private String[] columnNames = {"Select", "Frequent Episodes", "Count"};
    
    Class[] types = new Class [] {
	Boolean.class, String.class, Integer.class
    };
    
    public static final int EPISODE_COLUMN = 1;
    public static final int FREQUENCY_COLUMN = 2;

    /** Creates a new instance of VisualizationDisplayTableModel */
    public EpisodeSelectionTableModel()
    {
    }
    
    /** Creates a new instance of VisualizationDisplayTableModel */
    public EpisodeSelectionTableModel(String[] columnNames)
    {
	this.columnNames = columnNames;
    }
    
    public final Object[] longValues = {"Frequent Episode discovered", new Integer(100000)};
    
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
	AbstractEpisode e = (AbstractEpisode)data.get(row);

	switch (col)
	{
	    case 0:
		retVal = new Boolean(e.isSelected());
		break;
	    case 1:
		retVal = e.toString(eventTypes);
		break;
	    case 2:
		retVal = new Integer(e.getVotes(0));
		break;
	}
	return retVal;
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
	if (col == 0)
	    return true;
	else
	    return false;
    }
    
	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
    @Override
    public void setValueAt(Object value, int row, int col)
    {
	boolean val = ((Boolean)value).booleanValue();
	AbstractEpisode e = (AbstractEpisode)data.get(row);
	e.setSelected(val);
	fireTableDataChanged();
    }
    public void setAll(Boolean value) {
        if (data != null)
        {
            for(Object o : data) 
            {
                AbstractEpisode e = (AbstractEpisode)o;
                e.setSelected(value);
            }
            fireTableDataChanged();
        }
    }
    
    public List getData()
    {
	return data;
    }
    
    public void setData(List data)
    {
	this.data = data;
	fireTableDataChanged();
    }
    
    private EventFactor getEventTypes()
    {
	return eventTypes;
    }
    
    public void setEventTypes(EventFactor eventTypes)
    {
	this.eventTypes = eventTypes;
    }
}
